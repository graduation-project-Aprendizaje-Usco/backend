package co.edu.usco.aprendizaje_usco.modulos.practica.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.practica.dto.IntentoPracticaRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Rol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class PracticaTransaccionTest {

    @MockitoBean
    private NivelEstudianteService nivelEstudianteService;

    @Autowired
    private PracticaService practicaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void siFallaLaActualizacionDeNivelElIntentoNoQuedaRegistrado() {
        Long usuarioId = crearEstudiante();
        Long ejercicioId = jdbc.queryForObject(
                "SELECT id FROM ejercicio WHERE codigo = 'DIF-ALG-1-01'", Long.class);
        Long opcionId = jdbc.queryForObject("""
                SELECT id FROM opcion_ejercicio WHERE ejercicio_id = ? AND es_correcta""",
                Long.class, ejercicioId);
        UUID clave = UUID.randomUUID();

        when(nivelEstudianteService.actualizarTrasPrimerIntento(
                anyLong(), anyLong(), anyBoolean(), any()))
                .thenThrow(new IllegalStateException("fallo simulado al actualizar el nivel"));

        IntentoPracticaRequest request = new IntentoPracticaRequest(
                ejercicioId, opcionId, null, 12, false, clave);

        assertThatThrownBy(() -> practicaService.responder(usuarioId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fallo simulado");

        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM intento_practica
                WHERE usuario_id = ? AND clave_idempotencia = ?""",
                Integer.class, usuarioId, clave)).isZero();
    }

    private Long crearEstudiante() {
        Rol rol = rolRepository.findByNombre(NombreRol.ESTUDIANTE).orElseThrow();
        return usuarioRepository.saveAndFlush(new Usuario(
                "Transaccion", "Prueba", "u20269999302@usco.edu.co", "hash", rol)).getId();
    }

}
