package co.edu.usco.aprendizaje_usco.modulos.practica.service;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.NivelDificultad;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.CompetenciaRepository;
import co.edu.usco.aprendizaje_usco.modulos.practica.model.NivelEstudiante;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class NivelEstudianteServiceTest {

    @Autowired
    private NivelEstudianteService nivelService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private CompetenciaRepository competenciaRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void sinDiagnosticoIniciaEnNivelUnoYNoDuplicaElHistorial() {
        Long usuario = crearEstudiante("u20269999201@usco.edu.co");
        Long competencia = competenciaRepository.findByCodigo("ALGEBRAICA").orElseThrow().getId();

        NivelEstudiante primero = nivelService.obtenerOCrear(usuario, competencia);
        NivelEstudiante segundo = nivelService.obtenerOCrear(usuario, competencia);

        assertThat(primero.getNivel()).isEqualTo(NivelDificultad.BASICO);
        assertThat(segundo.getNivel()).isEqualTo(NivelDificultad.BASICO);
        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM historial_nivel WHERE usuario_id = ? AND competencia_id = ?""",
                Integer.class, usuario, competencia)).isEqualTo(1);
    }

    @Test
    void tomaElNivelInicialDelUltimoDiagnosticoPorCompetencia() {
        Long usuario = crearEstudiante("u20269999202@usco.edu.co");
        Long intento = crearDiagnosticoFinalizado(usuario);
        insertarResultado(intento, "ALGEBRAICA", "BAJO");
        insertarResultado(intento, "LOGICA", "MEDIO");
        insertarResultado(intento, "FUNCIONAL", "ALTO");

        assertThat(nivelService.obtenerOCrear(usuario, competenciaId("ALGEBRAICA")).getNivel())
                .isEqualTo(NivelDificultad.BASICO);
        assertThat(nivelService.obtenerOCrear(usuario, competenciaId("LOGICA")).getNivel())
                .isEqualTo(NivelDificultad.INTERMEDIO);
        assertThat(nivelService.obtenerOCrear(usuario, competenciaId("FUNCIONAL")).getNivel())
                .isEqualTo(NivelDificultad.AVANZADO);
    }

    @Test
    void tresAciertosActualizanElNivelDePractica() {
        Long usuario = crearEstudiante("u20269999203@usco.edu.co");
        Long competencia = competenciaId("ALGEBRAICA");

        nivelService.actualizarTrasPrimerIntento(usuario, competencia, true, null);
        nivelService.actualizarTrasPrimerIntento(usuario, competencia, true, null);
        NivelEstudiante despuesDeDos = nivelService.obtenerOCrear(usuario, competencia);
        assertThat(despuesDeDos.getNivel()).isEqualTo(NivelDificultad.BASICO);

        nivelService.actualizarTrasPrimerIntento(usuario, competencia, true, null);
        NivelEstudiante despuesDeTres = nivelService.obtenerOCrear(usuario, competencia);
        assertThat(despuesDeTres.getNivel()).isEqualTo(NivelDificultad.INTERMEDIO);
        assertThat(despuesDeTres.getRachaAciertos()).isZero();
    }

    private Long crearEstudiante(String correo) {
        return usuarioRepository.saveAndFlush(new Usuario(
                "Nivel", "Prueba", correo, "hash",
                rolRepository.findByNombre(NombreRol.ESTUDIANTE).orElseThrow())).getId();
    }

    private Long competenciaId(String codigo) {
        return competenciaRepository.findByCodigo(codigo).orElseThrow().getId();
    }

    private Long crearDiagnosticoFinalizado(Long usuarioId) {
        return jdbc.queryForObject("""
                INSERT INTO intento_diagnostico
                    (usuario_id, prueba_id, estado, iniciado_en, finalizado_en, puntaje_total, categoria)
                SELECT ?, id, 'FINALIZADO', now() - interval '1 hour', now(), 0, 'ALTO_RIESGO'
                FROM prueba_diagnostica WHERE activa
                RETURNING id""", Long.class, usuarioId);
    }

    private void insertarResultado(Long intentoId, String competencia, String nivel) {
        jdbc.update("""
                INSERT INTO resultado_dimension
                    (intento_id, competencia_id, aciertos, total, porcentaje, nivel)
                SELECT ?, id, 0, 8, 0, ?
                FROM competencia WHERE codigo = ?""", intentoId, nivel, competencia);
    }

}
