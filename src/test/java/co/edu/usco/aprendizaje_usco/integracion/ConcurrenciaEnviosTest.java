package co.edu.usco.aprendizaje_usco.integracion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.service.DiagnosticoService;
import co.edu.usco.aprendizaje_usco.modulos.practica.dto.IntentoPracticaRequest;
import co.edu.usco.aprendizaje_usco.modulos.practica.service.PracticaService;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/** Prueba de concurrencia de #149: 50 envíos simultáneos no duplican registros. */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class ConcurrenciaEnviosTest {

    private static final int ENVIOS = 50;

    @Autowired
    private PracticaService practicaService;

    @Autowired
    private DiagnosticoService diagnosticoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void cincuentaEnviosDePracticaConLaMismaClaveCreanUnSoloIntento() throws Exception {
        Long usuarioId = crearEstudiante("u20269999701@usco.edu.co");
        Long ejercicioId = jdbc.queryForObject(
                "SELECT id FROM ejercicio WHERE codigo = 'DIF-ALG-1-01'", Long.class);
        Long opcionId = jdbc.queryForObject("""
                SELECT id FROM opcion_ejercicio WHERE ejercicio_id = ? AND es_correcta""",
                Long.class, ejercicioId);
        UUID clave = UUID.randomUUID();
        IntentoPracticaRequest request = new IntentoPracticaRequest(
                ejercicioId, opcionId, null, 12, false, clave);

        List<PracticaService.Registro> registros = ejecutar(usuarioId,
                () -> practicaService.responder(usuarioId, request));

        assertThat(registros).hasSize(ENVIOS);
        assertThat(registros.stream().filter(PracticaService.Registro::creado)).hasSize(1);
        assertThat(registros).extracting(r -> r.respuesta().intentoId())
                .containsOnly(registros.get(0).respuesta().intentoId());
        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM intento_practica
                WHERE usuario_id = ? AND clave_idempotencia = ?""",
                Integer.class, usuarioId, clave)).isEqualTo(1);
    }

    @Test
    void cincuentaRespuestasDelMismoItemDejanUnaSolaRespuesta() throws Exception {
        Long usuarioId = crearEstudiante("u20269999702@usco.edu.co");
        Long intentoId = diagnosticoService.iniciar(usuarioId).intento().id();
        Long itemId = jdbc.queryForObject(
                "SELECT id FROM item WHERE activo ORDER BY numero LIMIT 1", Long.class);
        Long opcionId = jdbc.queryForObject(
                "SELECT id FROM opcion WHERE item_id = ? ORDER BY letra LIMIT 1", Long.class, itemId);

        List<Object> respuestas = ejecutar(usuarioId,
                () -> diagnosticoService.responder(intentoId, itemId, opcionId, usuarioId));

        assertThat(respuestas).hasSize(ENVIOS);
        assertThat(respuestas).doesNotContainNull();
        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM respuesta_diagnostico
                WHERE intento_id = ? AND item_id = ?""",
                Integer.class, intentoId, itemId)).isEqualTo(1);
    }

    private <T> List<T> ejecutar(Long usuarioId, java.util.concurrent.Callable<T> tarea) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(ENVIOS);
        try {
            CountDownLatch listos = new CountDownLatch(ENVIOS);
            CountDownLatch salida = new CountDownLatch(1);
            List<Future<T>> futuros = new ArrayList<>();
            for (int i = 0; i < ENVIOS; i++) {
                futuros.add(pool.submit(() -> {
                    listos.countDown();
                    salida.await();
                    return tarea.call();
                }));
            }
            assertThat(listos.await(10, TimeUnit.SECONDS)).isTrue();
            salida.countDown();
            List<T> resultados = new ArrayList<>();
            for (Future<T> futuro : futuros) {
                resultados.add(futuro.get(60, TimeUnit.SECONDS));
            }
            return resultados;
        } finally {
            pool.shutdownNow();
        }
    }

    private Long crearEstudiante(String correo) {
        return usuarioRepository.saveAndFlush(new Usuario(
                "Concurrente", "Prueba", correo, "hash",
                rolRepository.findByNombre(NombreRol.ESTUDIANTE).orElseThrow())).getId();
    }
}
