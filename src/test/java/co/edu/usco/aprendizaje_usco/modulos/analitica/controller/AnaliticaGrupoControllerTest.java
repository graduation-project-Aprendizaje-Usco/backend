package co.edu.usco.aprendizaje_usco.modulos.analitica.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Rol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import co.edu.usco.aprendizaje_usco.security.JwtService;
import co.edu.usco.aprendizaje_usco.security.UsuarioAutenticado;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** SCRUM-39: indicadores de grupo para el docente, con seguridad real y PostgreSQL. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class AnaliticaGrupoControllerTest {

    private static final String BASE = "/api/analitica/grupos";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JdbcTemplate jdbc;

    private Usuario ana;
    private Usuario beto;
    private Usuario carla;
    private Usuario docente;
    private Long grupoId;
    private String tokenDocente;
    private String tokenOtroDocente;
    private String tokenEstudiante;
    private String tokenAdmin;

    @BeforeEach
    void preparar() {
        ana = crear("Ana", "Alvarez", "u20261100001@usco.edu.co", NombreRol.ESTUDIANTE);
        beto = crear("Beto", "Bravo", "u20261100002@usco.edu.co", NombreRol.ESTUDIANTE);
        carla = crear("Carla", "Castro", "u20261100003@usco.edu.co", NombreRol.ESTUDIANTE);
        docente = crear("Diana", "Docente", "diana.analitica@usco.edu.co", NombreRol.DOCENTE);
        Usuario otroDocente = crear("Omar", "Otro", "omar.analitica@usco.edu.co", NombreRol.DOCENTE);
        Usuario admin = crear("Admin", "Sistema", "admin.analitica@usco.edu.co", NombreRol.ADMIN);

        tokenDocente = token(docente);
        tokenOtroDocente = token(otroDocente);
        tokenEstudiante = token(ana);
        tokenAdmin = token(admin);
        grupoId = crearGrupo(docente, "ANALITICA-1", "Cálculo Diferencial", ana, beto);
    }

    @Test
    void resumenCalculaPromediosDistribucionYEstudiantesEnRiesgo() throws Exception {
        intento(ana, "DIF-ALG-1-01", 1, true, "2026-08-10T15:00:00Z");
        intento(ana, "DIF-ALG-1-01", 2, false, "2026-08-11T15:00:00Z");
        intento(beto, "DIF-ALG-1-02", 1, true, "2026-08-10T15:00:00Z");
        intento(beto, "DIF-ALG-1-02", 2, true, "2026-08-11T15:00:00Z");
        diagnostico(ana, "ALTO_RIESGO", "2026-08-09T15:00:00Z", "ALGEBRAICA", 2, "BAJO");
        diagnostico(beto, "NIVEL_ADECUADO", "2026-08-09T15:00:00Z", "ALGEBRAICA", 7, "ALTO");

        resumen()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEstudiantes").value(2))
                .andExpect(jsonPath("$.estudiantesConActividad").value(2))
                .andExpect(jsonPath("$.promedioGeneral").value(75.0))
                .andExpect(jsonPath("$.estudiantesEnRiesgo").value(1))
                .andExpect(jsonPath("$.dimensiones[0].codigo").value("ALGEBRAICA"))
                .andExpect(jsonPath("$.dimensiones[0].promedio").value(75.0))
                .andExpect(jsonPath("$.dimensiones[0].estudiantesEvaluados").value(2))
                .andExpect(jsonPath("$.estudiantes[0].nombres").value("Ana"))
                .andExpect(jsonPath("$.estudiantes[0].categoriaDiagnostico").value("ALTO_RIESGO"))
                .andExpect(jsonPath("$.estudiantes[0].enRiesgo").value(true))
                .andExpect(jsonPath("$.estudiantes[1].nombres").value("Beto"))
                .andExpect(jsonPath("$.estudiantes[1].categoriaDiagnostico").value("NIVEL_ADECUADO"))
                .andExpect(jsonPath("$.distribucionCategorias[*].categoria").value(contains(
                        "ALTO_RIESGO", "RIESGO_MODERADO", "NIVEL_ADECUADO", "SIN_DIAGNOSTICO")));
    }

    @Test
    void calculaDimensionMasDebilTopDeErroresYEvolucion() throws Exception {
        intento(ana, "DIF-ALG-1-01", 1, false, "2026-08-10T15:00:00Z");
        intento(ana, "DIF-ALG-1-01", 2, false, "2026-08-11T15:00:00Z");
        intento(beto, "DIF-LOG-1-01", 1, false, "2026-08-12T15:00:00Z");
        intento(beto, "DIF-LOG-1-01", 2, true, "2026-08-13T15:00:00Z");

        resumen()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dimensionMasDebil.codigo").value("ALGEBRAICA"))
                .andExpect(jsonPath("$.topEjerciciosErrores.length()").value(2))
                .andExpect(jsonPath("$.topEjerciciosErrores[0].codigo").value("DIF-ALG-1-01"))
                .andExpect(jsonPath("$.topEjerciciosErrores[0].errores").value(2))
                .andExpect(jsonPath("$.topEjerciciosErrores[1].codigo").value("DIF-LOG-1-01"))
                .andExpect(jsonPath("$.evolucionGeneral.length()").value(1))
                .andExpect(jsonPath("$.evolucionGeneral[0].intentosEvaluados").value(4))
                .andExpect(jsonPath("$.evolucionGeneral[0].intentosCorrectos").value(1));
    }

    @Test
    void filtraPorCompetenciaYPorRangoDeFechas() throws Exception {
        intento(ana, "DIF-ALG-1-01", 1, true, "2026-08-10T15:00:00Z");
        intento(ana, "DIF-ALG-1-01", 2, false, "2026-08-11T15:00:00Z");
        intento(ana, "DIF-LOG-1-01", 1, true, "2026-01-10T15:00:00Z");
        diagnostico(ana, "RIESGO_MODERADO", "2026-08-09T15:00:00Z", "ALGEBRAICA", 5, "MEDIO");

        mockMvc.perform(get(BASE + "/" + grupoId)
                        .header("Authorization", tokenDocente)
                        .param("competencia", "ALGEBRAICA")
                        .param("desde", "2026-08-01")
                        .param("hasta", "2026-08-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dimensiones.length()").value(1))
                .andExpect(jsonPath("$.dimensiones[0].codigo").value("ALGEBRAICA"))
                .andExpect(jsonPath("$.estudiantes[0].intentos").value(2))
                .andExpect(jsonPath("$.estudiantes[0].promedio").value(50.0));
    }

    @Test
    void elRangoExcluyeIntentosYDiagnosticosDeOtroPeriodo() throws Exception {
        intento(ana, "DIF-ALG-1-01", 1, true, "2026-01-10T15:00:00Z");
        diagnostico(ana, "NIVEL_ADECUADO", "2026-01-09T15:00:00Z", "ALGEBRAICA", 7, "ALTO");

        mockMvc.perform(get(BASE + "/" + grupoId)
                        .header("Authorization", tokenDocente)
                        .param("desde", "2026-08-01")
                        .param("hasta", "2026-08-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estudiantes[0].intentos").value(0))
                .andExpect(jsonPath("$.estudiantes[0].sinActividad").value(true))
                .andExpect(jsonPath("$.estudiantes[0].categoriaDiagnostico").value("SIN_DIAGNOSTICO"));
    }

    @Test
    void soloElDocenteDuenoPuedeConsultarElGrupo() throws Exception {
        mockMvc.perform(get(BASE + "/" + grupoId).header("Authorization", tokenOtroDocente))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(BASE + "/" + grupoId).header("Authorization", tokenEstudiante))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(BASE + "/" + grupoId).header("Authorization", tokenAdmin))
                .andExpect(status().isForbidden());
    }

    @Test
    void unGrupoInexistenteDa404() throws Exception {
        mockMvc.perform(get(BASE + "/999999").header("Authorization", tokenDocente))
                .andExpect(status().isNotFound());
    }

    @Test
    void unGrupoSinEstudiantesDevuelveTotalesEnCero() throws Exception {
        Long vacio = crearGrupo(docente, "ANALITICA-VACIO", "Grupo vacío");

        mockMvc.perform(get(BASE + "/" + vacio).header("Authorization", tokenDocente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEstudiantes").value(0))
                .andExpect(jsonPath("$.estudiantesConActividad").value(0))
                .andExpect(jsonPath("$.promedioGeneral").value(nullValue()))
                .andExpect(jsonPath("$.estudiantes").isEmpty());
    }

    @Test
    void detalleDevuelveDimensionesYUltimoDiagnostico() throws Exception {
        intento(ana, "DIF-ALG-1-01", 1, true, "2026-08-10T15:00:00Z");
        diagnostico(ana, "ALTO_RIESGO", "2026-08-09T15:00:00Z", "ALGEBRAICA", 2, "BAJO");

        mockMvc.perform(get(BASE + "/" + grupoId + "/estudiantes/" + ana.getId())
                        .header("Authorization", tokenDocente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estudiante.id").value(ana.getId()))
                .andExpect(jsonPath("$.estudiante.promedio").value(100.0))
                .andExpect(jsonPath("$.dimensiones.length()").value(3))
                .andExpect(jsonPath("$.ultimoDiagnostico.categoria").value("ALTO_RIESGO"))
                .andExpect(jsonPath("$.ultimoDiagnostico.dimensiones[0].codigo").value("ALGEBRAICA"));
    }

    @Test
    void detalleRechazaUnEstudianteQueNoEstaEnElGrupo() throws Exception {
        mockMvc.perform(get(BASE + "/" + grupoId + "/estudiantes/" + carla.getId())
                        .header("Authorization", tokenDocente))
                .andExpect(status().isNotFound());
    }

    @Test
    void validaCompetenciaYOrdenDeFechas() throws Exception {
        mockMvc.perform(get(BASE + "/" + grupoId)
                        .header("Authorization", tokenDocente)
                        .param("competencia", "NO_EXISTE"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get(BASE + "/" + grupoId)
                        .header("Authorization", tokenDocente)
                        .param("desde", "2026-09-01")
                        .param("hasta", "2026-08-01"))
                .andExpect(status().isUnprocessableContent());
    }

    private ResultActions resumen() throws Exception {
        return mockMvc.perform(get(BASE + "/" + grupoId)
                .header("Authorization", tokenDocente));
    }

    private Usuario crear(String nombres, String apellidos, String correo, NombreRol nombreRol) {
        Rol rol = rolRepository.findByNombre(nombreRol).orElseThrow();
        return usuarioRepository.saveAndFlush(new Usuario(nombres, apellidos, correo, "hash", rol));
    }

    private Long crearGrupo(Usuario docente, String codigo, String nombre, Usuario... estudiantes) {
        Long id = jdbc.queryForObject("""
                INSERT INTO grupo (codigo, nombre, docente_id) VALUES (?, ?, ?) RETURNING id""",
                Long.class, codigo, nombre, docente.getId());
        for (Usuario estudiante : estudiantes) {
            jdbc.update("INSERT INTO grupo_estudiante (grupo_id, estudiante_id) VALUES (?, ?)",
                    id, estudiante.getId());
        }
        return id;
    }

    private void intento(Usuario estudiante, String codigo, int numeroIntento, boolean correcta, String fecha) {
        jdbc.update("""
                INSERT INTO intento_practica
                    (usuario_id, ejercicio_id, competencia_id, nivel_ejercicio, numero_intento,
                     opcion_ejercicio_id, es_correcta, tiempo_seg, clave_idempotencia, creado_en)
                SELECT ?, e.id, e.competencia_id, e.nivel, ?, o.id, ?, 15, ?, ?
                FROM ejercicio e
                JOIN opcion_ejercicio o ON o.ejercicio_id = e.id AND o.letra = 'A'
                WHERE e.codigo = ?""",
                estudiante.getId(), numeroIntento, correcta, UUID.randomUUID(),
                Timestamp.from(Instant.parse(fecha)), codigo);
    }

    private void diagnostico(Usuario estudiante, String categoria, String fecha, String competencia,
                             int aciertos, String nivel) {
        Instant fin = Instant.parse(fecha);
        Long intento = jdbc.queryForObject("""
                INSERT INTO intento_diagnostico
                    (usuario_id, prueba_id, estado, iniciado_en, finalizado_en, puntaje_total, categoria)
                SELECT ?, id, 'FINALIZADO', ?, ?, ?, ?
                FROM prueba_diagnostica WHERE activa
                RETURNING id""",
                Long.class, estudiante.getId(), Timestamp.from(fin.minusSeconds(3600)),
                Timestamp.from(fin), aciertos, categoria);
        BigDecimal porcentaje = BigDecimal.valueOf(aciertos * 100L / 8)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        jdbc.update("""
                INSERT INTO resultado_dimension
                    (intento_id, competencia_id, aciertos, total, porcentaje, nivel)
                SELECT ?, id, ?, 8, ?, ? FROM competencia WHERE codigo = ?""",
                intento, aciertos, porcentaje, nivel, competencia);
    }

    private String token(Usuario usuario) {
        return "Bearer " + jwtService.emitir(UsuarioAutenticado.desde(usuario)).valor();
    }
}
