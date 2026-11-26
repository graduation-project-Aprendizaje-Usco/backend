package co.edu.usco.aprendizaje_usco.modulos.diagnostico.service;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Doble clic en "Iniciar prueba": dos peticiones simultáneas del mismo estudiante
 * terminan en UN solo intento (el bloqueo de la fila del usuario las pone en fila).
 * Sin @Transactional porque cada hilo necesita su propia transacción real.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class InicioConcurrenteTest {

    private static final String CORREO = "u20263000001@usco.edu.co";

    @Autowired
    private DiagnosticoService diagnosticoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void limpiar() {
        jdbc.update("DELETE FROM intento_diagnostico WHERE usuario_id IN (SELECT id FROM usuario WHERE correo = ?)", CORREO);
        jdbc.update("DELETE FROM usuario WHERE correo = ?", CORREO);
    }

    @Test
    void dosIniciosSimultaneosCreanUnSoloIntento() throws Exception {
        Long usuarioId = usuarioRepository.save(new Usuario("Doble", "Clic", CORREO, "x",
                rolRepository.findByNombre(NombreRol.ESTUDIANTE).orElseThrow())).getId();

        CountDownLatch salida = new CountDownLatch(1);
        Callable<DiagnosticoService.Inicio> iniciar = () -> {
            salida.await();
            return diagnosticoService.iniciar(usuarioId);
        };
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            List<Future<DiagnosticoService.Inicio>> futuros = List.of(hilos.submit(iniciar), hilos.submit(iniciar));
            salida.countDown();
            DiagnosticoService.Inicio a = futuros.get(0).get();
            DiagnosticoService.Inicio b = futuros.get(1).get();

            assertThat(a.intento().id()).isEqualTo(b.intento().id());
            assertThat(List.of(a.creado(), b.creado())).containsExactlyInAnyOrder(true, false);
        } finally {
            hilos.shutdownNow();
        }
        assertThat(jdbc.queryForObject("SELECT count(*) FROM intento_diagnostico WHERE usuario_id = ?",
                Integer.class, usuarioId)).isEqualTo(1);
    }

}
