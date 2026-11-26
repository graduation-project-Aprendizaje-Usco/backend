package co.edu.usco.aprendizaje_usco.modulos.diagnostico.service;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.repository.IntentoDiagnosticoRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * El barrido finaliza por tiempo los intentos abandonados y deja en paz los que
 * todavía tienen tiempo.
 *
 * SIN @Transactional: el barrido usa una transacción por intento, como en
 * producción. Por eso los datos se borran a mano en {@link #limpiar()}.
 * En las pruebas el @Scheduled está apagado (app.diagnostico.barrido.habilitado=false),
 * así que aquí se crea el componente y se invoca directamente.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class BarridoIntentosVencidosTest {

    private static final String CORREO_VENCIDO = "u20262000001@usco.edu.co";
    private static final String CORREO_A_TIEMPO = "u20262000002@usco.edu.co";

    @Autowired
    private DiagnosticoService diagnosticoService;

    @Autowired
    private IntentoDiagnosticoRepository intentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void limpiar() {
        jdbc.update("DELETE FROM intento_diagnostico WHERE usuario_id IN (SELECT id FROM usuario WHERE correo IN (?, ?))",
                CORREO_VENCIDO, CORREO_A_TIEMPO);
        jdbc.update("DELETE FROM usuario WHERE correo IN (?, ?)", CORREO_VENCIDO, CORREO_A_TIEMPO);
    }

    @Test
    void finalizaPorTiempoSoloLosIntentosVencidos() {
        long vencido = diagnosticoService.iniciar(crear(CORREO_VENCIDO).getId()).intento().id();
        long aTiempo = diagnosticoService.iniciar(crear(CORREO_A_TIEMPO).getId()).intento().id();
        jdbc.update("UPDATE intento_diagnostico SET iniciado_en = now() - interval '40 minutes' WHERE id = ?", vencido);

        new BarridoIntentosVencidos(intentoRepository, diagnosticoService).ejecutar();

        Map<String, Object> cerrado = jdbc.queryForMap(
                "SELECT estado, finalizado_por_tiempo, puntaje_total, categoria FROM intento_diagnostico WHERE id = ?", vencido);
        assertThat(cerrado).containsEntry("estado", "FINALIZADO")
                .containsEntry("finalizado_por_tiempo", true)
                .containsEntry("puntaje_total", 0)
                .containsEntry("categoria", "ALTO_RIESGO");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM resultado_dimension WHERE intento_id = ?", Integer.class, vencido))
                .isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT estado FROM intento_diagnostico WHERE id = ?", String.class, aTiempo))
                .isEqualTo("EN_CURSO");
    }

    private Usuario crear(String correo) {
        return usuarioRepository.save(new Usuario("Barrido", "Prueba", correo, "x",
                rolRepository.findByNombre(NombreRol.ESTUDIANTE).orElseThrow()));
    }

}
