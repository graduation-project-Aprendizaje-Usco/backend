package co.edu.usco.aprendizaje_usco.esquema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Competencia;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Contenido;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Ejercicio;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.NivelDificultad;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.TipoEjercicio;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.CompetenciaRepository;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.ContenidoRepository;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.EjercicioRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Rol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class EsquemaPracticaTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private CompetenciaRepository competenciaRepository;

    @Autowired
    private ContenidoRepository contenidoRepository;

    @Autowired
    private EjercicioRepository ejercicioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Test
    void laSemillaTiene90EjerciciosYLaDistribucionAcordada() {
        assertThat(jdbc.queryForObject("SELECT count(*) FROM ejercicio WHERE creado_por IS NULL", Integer.class))
                .isEqualTo(90);
        assertThat(jdbc.queryForList("""
                SELECT c.asignatura, count(*) AS total
                FROM ejercicio e JOIN contenido c ON c.id = e.contenido_id
                WHERE e.creado_por IS NULL
                GROUP BY c.asignatura ORDER BY c.asignatura"""))
                .extracting(f -> f.get("asignatura") + "=" + f.get("total"))
                .containsExactly("CALCULO_DIFERENCIAL=45", "CALCULO_INTEGRAL=45");
    }

    @Test
    void cadaCombinacionTieneCincoEjercicios() {
        var filas = jdbc.queryForList("""
                SELECT c.asignatura, e.competencia_id, e.nivel, count(*) AS total
                FROM ejercicio e JOIN contenido c ON c.id = e.contenido_id
                WHERE e.creado_por IS NULL
                GROUP BY c.asignatura, e.competencia_id, e.nivel
                ORDER BY c.asignatura, e.competencia_id, e.nivel""");

        assertThat(filas).hasSize(18);
        assertThat(filas).allSatisfy(fila -> assertThat(((Number) fila.get("total")).intValue()).isEqualTo(5));
    }

    @Test
    void losEjerciciosDeOpcionMultipleTienenCuatroOpcionesYUnaCorrecta() {
        var ejercicios = jdbc.queryForList("""
                SELECT e.id, e.codigo FROM ejercicio e WHERE e.tipo = 'OPCION_MULTIPLE'""");
        assertThat(ejercicios).isNotEmpty();
        for (Map<String, Object> ejercicio : ejercicios) {
            Long id = ((Number) ejercicio.get("id")).longValue();
            assertThat(jdbc.queryForObject(
                    "SELECT count(*) FROM opcion_ejercicio WHERE ejercicio_id = ?", Integer.class, id))
                    .as("opciones del ejercicio %s", ejercicio.get("codigo")).isEqualTo(4);
            assertThat(jdbc.queryForObject(
                    "SELECT count(*) FROM opcion_ejercicio WHERE ejercicio_id = ? AND es_correcta", Integer.class, id))
                    .as("clave del ejercicio %s", ejercicio.get("codigo")).isEqualTo(1);
        }
    }

    @Test
    void losEjerciciosNumericosNoTienenOpciones() {
        var numericos = jdbc.queryForList("SELECT id, codigo FROM ejercicio WHERE tipo = 'NUMERICA'");
        assertThat(numericos).isNotEmpty();
        for (Map<String, Object> ejercicio : numericos) {
            Long id = ((Number) ejercicio.get("id")).longValue();
            assertThat(jdbc.queryForObject(
                    "SELECT count(*) FROM opcion_ejercicio WHERE ejercicio_id = ?", Integer.class, id))
                    .as("opciones del ejercicio %s", ejercicio.get("codigo")).isZero();
        }
    }

    @Test
    void noPermiteUnNivelFueraDeRango() {
        Long contenido = jdbc.queryForObject(
                "SELECT id FROM contenido WHERE asignatura = 'CALCULO_DIFERENCIAL' AND activo LIMIT 1", Long.class);
        Long competencia = jdbc.queryForObject(
                "SELECT competencia_id FROM contenido WHERE id = ?", Long.class, contenido);

        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, pista, explicacion)
                VALUES (?, ?, 'TEST-NIVEL-4', 4, 'NUMERICA', 'x', 'p', 'e')""", contenido, competencia))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void noPermiteTipoYRespuestaIncoherentes() {
        Long contenido = jdbc.queryForObject(
                "SELECT id FROM contenido WHERE asignatura = 'CALCULO_DIFERENCIAL' AND activo LIMIT 1", Long.class);
        Long competencia = jdbc.queryForObject(
                "SELECT competencia_id FROM contenido WHERE id = ?", Long.class, contenido);

        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO ejercicio (contenido_id, competencia_id, codigo, nivel, tipo, enunciado, pista, explicacion)
                VALUES (?, ?, 'TEST-NUM-SIN-RESPUESTA', 1, 'NUMERICA', 'x', 'p', 'e')""", contenido, competencia))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void noPermiteDosIntentosConLaMismaClaveDeIdempotencia() {
        Long usuario = crearEstudiante();
        Long ejercicio = jdbc.queryForObject("SELECT id FROM ejercicio WHERE codigo = 'DIF-ALG-1-04'", Long.class);
        UUID clave = UUID.randomUUID();

        jdbc.update("""
                INSERT INTO intento_practica
                (usuario_id, ejercicio_id, competencia_id, nivel_ejercicio, numero_intento,
                 respuesta_numerica, es_correcta, tiempo_seg, clave_idempotencia)
                SELECT ?, e.id, e.competencia_id, e.nivel, 1, 5, TRUE, 10, ?
                FROM ejercicio e WHERE e.id = ?""", usuario, clave, ejercicio);

        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO intento_practica
                (usuario_id, ejercicio_id, competencia_id, nivel_ejercicio, numero_intento,
                 respuesta_numerica, es_correcta, tiempo_seg, clave_idempotencia)
                SELECT ?, e.id, e.competencia_id, e.nivel, 1, 5, TRUE, 10, ?
                FROM ejercicio e WHERE e.id = ?""", usuario, clave, ejercicio))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void laOpcionDelIntentoDebePertenecerAlEjercicio() {
        Long usuario = crearEstudiante();
        Long ejercicio = jdbc.queryForObject("SELECT id FROM ejercicio WHERE codigo = 'DIF-ALG-1-01'", Long.class);
        Long opcionAjena = jdbc.queryForObject("""
                SELECT o.id FROM opcion_ejercicio o JOIN ejercicio e ON e.id = o.ejercicio_id
                WHERE e.codigo = 'DIF-ALG-1-02' LIMIT 1""", Long.class);

        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO intento_practica
                (usuario_id, ejercicio_id, competencia_id, nivel_ejercicio, numero_intento,
                 opcion_ejercicio_id, es_correcta, tiempo_seg, clave_idempotencia)
                SELECT ?, e.id, e.competencia_id, e.nivel, 1, ?, FALSE, 10, ?
                FROM ejercicio e WHERE e.id = ?""", usuario, opcionAjena, UUID.randomUUID(), ejercicio))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void elCsvSemillaCoincideConLaBase() throws IOException {
        List<String> lineas = Files.readAllLines(Path.of("docs/datos/ejercicios_practica.csv"), StandardCharsets.UTF_8);
        List<List<String>> filas = lineas.stream().skip(1).filter(l -> !l.isBlank())
                .map(EsquemaPracticaTest::separarCsv).toList();
        assertThat(filas).hasSize(90);

        for (List<String> csv : filas) {
            String codigo = csv.get(0);
            Map<String, Object> fila = jdbc.queryForMap("""
                    SELECT e.enunciado, e.tipo, e.nivel, e.respuesta_numerica, e.tolerancia,
                           e.pista, e.explicacion, e.error_comun, c.asignatura, comp.codigo AS dimension
                    FROM ejercicio e
                    JOIN contenido c ON c.id = e.contenido_id
                    JOIN competencia comp ON comp.id = e.competencia_id
                    WHERE e.codigo = ?""", codigo);

            assertThat(fila.get("asignatura")).as("asignatura de %s", codigo).isEqualTo(csv.get(1));
            assertThat(fila.get("dimension")).as("dimension de %s", codigo).isEqualTo(csv.get(3));
            assertThat(((Number) fila.get("nivel")).intValue()).as("nivel de %s", codigo)
                    .isEqualTo(Integer.parseInt(csv.get(4)));
            assertThat(fila.get("tipo")).as("tipo de %s", codigo).isEqualTo(csv.get(5));
            assertThat(fila.get("enunciado")).as("enunciado de %s", codigo).isEqualTo(csv.get(6));
            assertThat(fila.get("pista")).as("pista de %s", codigo).isEqualTo(csv.get(13));
            assertThat(fila.get("explicacion")).as("explicacion de %s", codigo).isEqualTo(csv.get(14));
            assertThat(fila.get("error_comun")).as("error comun de %s", codigo).isEqualTo(csv.get(15));

            if ("OPCION_MULTIPLE".equals(csv.get(5))) {
                List<Map<String, Object>> opciones = jdbc.queryForList("""
                        SELECT letra, texto, es_correcta FROM opcion_ejercicio o
                        JOIN ejercicio e ON e.id = o.ejercicio_id
                        WHERE e.codigo = ? ORDER BY letra""", codigo);
                assertThat(opciones).extracting(o -> o.get("letra") + "=" + o.get("texto"))
                        .containsExactly(
                                "A=" + csv.get(7), "B=" + csv.get(8),
                                "C=" + csv.get(9), "D=" + csv.get(10));
                assertThat(opciones.stream().filter(o -> (Boolean) o.get("es_correcta")).count())
                        .as("clave de %s", codigo).isEqualTo(1);
                assertThat(opciones.stream().filter(o -> (Boolean) o.get("es_correcta"))
                        .findFirst().orElseThrow().get("letra")).isEqualTo(csv.get(11));
            } else {
                assertThat(new BigDecimal(fila.get("respuesta_numerica").toString()))
                        .as("respuesta de %s", codigo).isEqualByComparingTo(csv.get(11));
                assertThat(new BigDecimal(fila.get("tolerancia").toString()))
                        .as("tolerancia de %s", codigo).isEqualByComparingTo(csv.get(12));
                assertThat(jdbc.queryForObject("""
                        SELECT count(*) FROM opcion_ejercicio o JOIN ejercicio e ON e.id = o.ejercicio_id
                        WHERE e.codigo = ?""", Integer.class, codigo)).isZero();
            }
        }
    }

    @Test
    void sePuedeInsertarUnEjercicioSinMigracionYDesactivarlo() {
        Competencia competencia = competenciaRepository.findByCodigo("ALGEBRAICA").orElseThrow();
        Contenido contenido = contenidoRepository.saveAndFlush(new Contenido(
                competencia, "CALCULO_DIFERENCIAL", "Tema dinamico", "Contenido dinamico", null, null));
        Ejercicio ejercicio = ejercicioRepository.saveAndFlush(new Ejercicio(
                contenido, "DIN-1-01", NivelDificultad.BASICO, TipoEjercicio.NUMERICA,
                "Calcula $2+2$.", new BigDecimal("4"), new BigDecimal("0.0001"),
                "Suma.", "Cuatro.", null, null));

        assertThat(ejercicioRepository.findByCodigo("DIN-1-01")).isPresent();
        assertThat(ejercicioRepository.findActivosPorAsignatura("CALCULO_DIFERENCIAL"))
                .extracting(Ejercicio::getCodigo).contains("DIN-1-01");

        ejercicio.desactivar();
        ejercicioRepository.saveAndFlush(ejercicio);
        assertThat(ejercicioRepository.findActivosPorAsignatura("CALCULO_DIFERENCIAL"))
                .extracting(Ejercicio::getCodigo).doesNotContain("DIN-1-01");
    }

    private Long crearEstudiante() {
        Rol rol = rolRepository.findByNombre(NombreRol.ESTUDIANTE).orElseThrow();
        Usuario usuario = usuarioRepository.saveAndFlush(new Usuario(
                "Prueba", "Practica", "u20269999001@usco.edu.co", "hash", rol));
        return usuario.getId();
    }

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

}
