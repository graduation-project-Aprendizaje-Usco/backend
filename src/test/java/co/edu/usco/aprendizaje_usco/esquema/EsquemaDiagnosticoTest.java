package co.edu.usco.aprendizaje_usco.esquema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.Item;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.PruebaDiagnostica;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.repository.ItemRepository;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.repository.PruebaDiagnosticaRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bloque 3: tablas del diagnóstico (V3) y datos de la prueba v2.0
 * (V4). Comprueba los datos cargados y que la base rechace lo que
 * el modelo no permite, aunque alguien escriba directo con SQL.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class EsquemaDiagnosticoTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PruebaDiagnosticaRepository pruebaRepository;

    @Autowired
    private ItemRepository itemRepository;

    // ------------------------------------------------------------ datos de la prueba

    @Test
    void hayUnaPruebaActivaDe36Minutos() {
        PruebaDiagnostica prueba = pruebaRepository.findFirstByActivaTrue().orElseThrow();

        assertThat(prueba.getVersion()).isEqualTo("2.0");
        assertThat(prueba.getTiempoLimite()).isEqualTo(Duration.ofMinutes(36));
    }

    @Test
    void laPruebaTiene24ItemsEnOrdenConCuatroOpcionesYUnaClave() {
        PruebaDiagnostica prueba = pruebaRepository.findFirstByActivaTrue().orElseThrow();
        List<Item> items = itemRepository.findActivosConOpciones(prueba.getId());

        assertThat(items).extracting(Item::getNumero)
                .containsExactlyElementsOf(java.util.stream.IntStream.rangeClosed(1, 24).boxed().toList());
        assertThat(items).allSatisfy(item -> {
            assertThat(item.getOpciones()).extracting("letra").containsExactly("A", "B", "C", "D");
            assertThat(item.getOpciones()).filteredOn("esCorrecta", true).hasSize(1);
        });
    }

    @Test
    void ochoItemsPorCompetencia() {
        List<Map<String, Object>> filas = jdbc.queryForList("""
                SELECT c.codigo, count(*) AS n FROM item i JOIN competencia c ON c.id = i.competencia_id
                GROUP BY c.codigo ORDER BY c.codigo""");

        assertThat(filas).extracting(f -> f.get("codigo") + "=" + f.get("n"))
                .containsExactly("ALGEBRAICA=8", "FUNCIONAL=8", "LOGICA=8");
    }

    @Test
    void lasClavesSonLasDeLaTablaDeEspecificaciones() {
        String claves = String.join("", jdbc.queryForList("""
                SELECT o.letra FROM item i JOIN opcion o ON o.item_id = i.id AND o.es_correcta
                ORDER BY i.numero""", String.class));

        // Ítems 1 a 24, en orden.
        assertThat(claves).isEqualTo("BAAACABC" + "BCCBCBBC" + "CBBBBBBC");
    }

    @Test
    void losItemsConGraficaTienenTextoAlternativo() {
        List<Map<String, Object>> filas = jdbc.queryForList(
                "SELECT numero, imagen, imagen_alt FROM item WHERE imagen IS NOT NULL ORDER BY numero");

        assertThat(filas).extracting(f -> f.get("numero")).containsExactly(17, 18, 19);
        assertThat(filas).allSatisfy(f -> assertThat((String) f.get("imagen_alt")).isNotBlank());
    }

    /**
     * docs/datos/items_diagnostico.csv es la fuente revisable del banco de ítems y
     * V4__datos_prueba_diagnostica.sql es su migración. Si alguien corrige uno sin el otro, esta prueba falla.
     */
    @Test
    void laMigracionCoincideConElCsvDeItems() throws IOException {
        List<String> lineas = Files.readAllLines(Path.of("docs/datos/items_diagnostico.csv"), StandardCharsets.UTF_8);
        List<List<String>> filas = lineas.stream().skip(1).filter(l -> !l.isBlank())
                .map(EsquemaDiagnosticoTest::separarCsv).toList();
        assertThat(filas).hasSize(24);

        for (List<String> csv : filas) {
            int numero = Integer.parseInt(csv.get(0));
            Map<String, Object> item = jdbc.queryForMap("""
                    SELECT c.codigo, i.enunciado, coalesce(i.imagen, '') AS imagen, coalesce(i.imagen_alt, '') AS imagen_alt
                    FROM item i JOIN competencia c ON c.id = i.competencia_id
                    JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.activa
                    WHERE i.numero = ?""", numero);
            List<String> opciones = jdbc.queryForList("""
                    SELECT o.texto FROM opcion o JOIN item i ON i.id = o.item_id
                    JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.activa
                    WHERE i.numero = ? ORDER BY o.letra""", String.class, numero);
            String clave = jdbc.queryForObject("""
                    SELECT o.letra FROM opcion o JOIN item i ON i.id = o.item_id
                    JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.activa
                    WHERE i.numero = ? AND o.es_correcta""", String.class, numero);

            assertThat(item.get("codigo")).as("dimensión del ítem %d", numero).isEqualTo(csv.get(1));
            assertThat(item.get("enunciado")).as("enunciado del ítem %d", numero).isEqualTo(csv.get(2));
            assertThat(opciones).as("opciones del ítem %d", numero).containsExactlyElementsOf(csv.subList(3, 7));
            assertThat(clave).as("clave del ítem %d", numero).isEqualTo(csv.get(7));
            assertThat(item.get("imagen")).as("imagen del ítem %d", numero).isEqualTo(csv.get(8));
            assertThat(item.get("imagen_alt")).as("texto alternativo del ítem %d", numero).isEqualTo(csv.get(9));
        }
    }

    /** Separa una línea CSV con ";" respetando los campos entre comillas (formato de Excel). */
    private static List<String> separarCsv(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean entreComillas = false;
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (entreComillas) {
                if (c == '"' && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    actual.append('"');
                    i++;
                } else if (c == '"') {
                    entreComillas = false;
                } else {
                    actual.append(c);
                }
            } else if (c == '"') {
                entreComillas = true;
            } else if (c == ';') {
                campos.add(actual.toString());
                actual.setLength(0);
            } else if (c != '\uFEFF') {
                actual.append(c);
            }
        }
        campos.add(actual.toString());
        return campos;
    }

    // ------------------------------------------------------------ restricciones

    @Test
    void noPermiteDosPruebasActivas() {
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO prueba_diagnostica (nombre, version, tiempo_limite_min, activa)
                VALUES ('Otra', '1.0', 30, TRUE)"""))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void noPermiteDosOpcionesCorrectasEnUnItem() {
        assertThatThrownBy(() -> jdbc.update("""
                UPDATE opcion SET es_correcta = TRUE
                WHERE letra = 'D' AND item_id = (SELECT id FROM item WHERE numero = 1)"""))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void unItemConImagenExigeTextoAlternativo() {
        assertThatThrownBy(() -> jdbc.update("UPDATE item SET imagen_alt = NULL WHERE numero = 17"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void noPermiteDosIntentosEnCursoDelMismoEstudiante() {
        Long usuario = crearEstudiante();
        jdbc.update("INSERT INTO intento_diagnostico (usuario_id, prueba_id) SELECT ?, id FROM prueba_diagnostica WHERE activa", usuario);

        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO intento_diagnostico (usuario_id, prueba_id) SELECT ?, id FROM prueba_diagnostica WHERE activa", usuario))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void unIntentoFinalizadoExigePuntajeYCategoria() {
        Long usuario = crearEstudiante();

        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO intento_diagnostico (usuario_id, prueba_id, estado, finalizado_en)
                SELECT ?, id, 'FINALIZADO', now() FROM prueba_diagnostica WHERE activa""", usuario))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void laRespuestaDebeSerUnaOpcionDelMismoItem() {
        Long usuario = crearEstudiante();
        Long intento = jdbc.queryForObject("""
                INSERT INTO intento_diagnostico (usuario_id, prueba_id)
                SELECT ?, id FROM prueba_diagnostica WHERE activa RETURNING id""", Long.class, usuario);

        // Ítem 1 respondido con una opción del ítem 2: lo impide fk_respuesta_opcion_item.
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO respuesta_diagnostico (intento_id, item_id, opcion_id)
                SELECT ?, (SELECT id FROM item WHERE numero = 1),
                       (SELECT o.id FROM opcion o JOIN item i ON i.id = o.item_id WHERE i.numero = 2 AND o.letra = 'A')""",
                intento))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Long crearEstudiante() {
        return jdbc.queryForObject("""
                INSERT INTO usuario (nombres, apellidos, correo, password_hash, rol_id)
                SELECT 'Esquema', 'Prueba', 'u20269999999@usco.edu.co', 'x', id FROM rol WHERE nombre = 'ESTUDIANTE'
                RETURNING id""", Long.class);
    }

}
