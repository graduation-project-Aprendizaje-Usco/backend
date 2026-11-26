package co.edu.usco.aprendizaje_usco.modulos.practica.service;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.practica.dto.IntentoPracticaRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Rol;
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

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class PracticaIdempotenciaConcurrenteTest {

    private static final int ENVIOS = 8;

    @Autowired
    private PracticaService practicaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void variosEnviosSimultaneosConLaMismaClaveCreanUnSoloIntento() throws Exception {
        Long usuarioId = crearEstudiante();
        Long ejercicioId = jdbc.queryForObject(
                "SELECT id FROM ejercicio WHERE codigo = 'DIF-ALG-1-01'", Long.class);
        Long opcionId = jdbc.queryForObject("""
                SELECT id FROM opcion_ejercicio WHERE ejercicio_id = ? AND es_correcta""",
                Long.class, ejercicioId);
        UUID clave = UUID.randomUUID();
        IntentoPracticaRequest request = new IntentoPracticaRequest(
                ejercicioId, opcionId, null, 12, false, clave);

        ExecutorService pool = Executors.newFixedThreadPool(ENVIOS);
        try {
            CountDownLatch listos = new CountDownLatch(ENVIOS);
            CountDownLatch salida = new CountDownLatch(1);
            List<Future<PracticaService.Registro>> futuros = new ArrayList<>();
            for (int i = 0; i < ENVIOS; i++) {
                futuros.add(pool.submit(() -> {
                    listos.countDown();
                    salida.await();
                    return practicaService.responder(usuarioId, request);
                }));
            }

            assertThat(listos.await(10, TimeUnit.SECONDS)).isTrue();
            salida.countDown();

            List<PracticaService.Registro> registros = new ArrayList<>();
            for (Future<PracticaService.Registro> futuro : futuros) {
                registros.add(futuro.get(30, TimeUnit.SECONDS));
            }

            assertThat(registros).allSatisfy(registro -> {
                assertThat(registro.respuesta().esCorrecta()).isTrue();
                assertThat(registro.respuesta().numeroIntento()).isEqualTo(1);
            });
            assertThat(registros.stream().filter(PracticaService.Registro::creado)).hasSize(1);
            assertThat(registros)
                    .extracting(registro -> registro.respuesta().intentoId())
                    .containsOnly(registros.get(0).respuesta().intentoId());
        } finally {
            pool.shutdownNow();
        }

        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM intento_practica
                WHERE usuario_id = ? AND clave_idempotencia = ?""",
                Integer.class, usuarioId, clave)).isEqualTo(1);
    }

    private Long crearEstudiante() {
        Rol rol = rolRepository.findByNombre(NombreRol.ESTUDIANTE).orElseThrow();
        return usuarioRepository.saveAndFlush(new Usuario(
                "Concurrente", "Prueba", "u20269999301@usco.edu.co", "hash", rol)).getId();
    }

}
