package co.edu.usco.aprendizaje_usco.modulos.progreso.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import co.edu.usco.aprendizaje_usco.security.JwtService;
import co.edu.usco.aprendizaje_usco.security.UsuarioAutenticado;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
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

/**
 * SCRUM-168: GET /api/historial con la seguridad real y PostgreSQL (Testcontainers).
 * El diagnóstico se presenta por la API; los intentos de práctica se insertan con SQL
 * sobre los ejercicios de la migración V6 para controlar sus fechas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class HistorialControllerTest {

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

    @Autowired
    private EntityManager entityManager;

    private Usuario ana;
    private Usuario beto;
    private String tokenAna;
    private String tokenBeto;
    private String tokenDocente;
    private String tokenOtroDocente;
    private String tokenAdmin;

    /** id y competencia (código) de los ejercicios sembrados, en orden. */
    private List<Object[]> ejercicios;

    @BeforeEach
    void preparar() {
        ana = crear("Ana", "u20261000011@usco.edu.co", NombreRol.ESTUDIANTE);
        beto = crear("Beto", "u20261000012@usco.edu.co", NombreRol.ESTUDIANTE);
        Usuario docente = crear("Diana", "diana.historial@usco.edu.co", NombreRol.DOCENTE);
        tokenAna = token(ana);
        tokenBeto = token(beto);
        tokenDocente = token(docente);
        tokenOtroDocente = token(crear("Omar", "omar.historial@usco.edu.co", NombreRol.DOCENTE));
        tokenAdmin = token(crear("Admin", "admin.historial@usco.edu.co", NombreRol.ADMIN));

        // Diana tiene un grupo con Ana; Beto no está en ningún grupo de Diana.
        Long grupo = jdbc.queryForObject("INSERT INTO grupo (codigo, nombre, docente_id) VALUES ('HIST-1', 'Cálculo 1', ?) RETURNING id",
                Long.class, docente.getId());
        jdbc.update("INSERT INTO grupo_estudiante (grupo_id, estudiante_id) VALUES (?, ?)", grupo, ana.getId());

        ejercicios = jdbc.query("""
                SELECT e.id, c.codigo FROM ejercicio e JOIN competencia c ON c.id = e.competencia_id
                WHERE e.activo ORDER BY e.id""", (rs, i) -> new Object[] {rs.getLong(1), rs.getString(2)});
    }

    // ------------------------------------------------------------ lo que ve el estudiante

    @Test
    void elEstudianteVeSusDiagnosticosYPracticasDelMasRecienteAlMasAntiguo() throws Exception {
        practica(ana, 0, true, "2026-10-01T15:00:00Z");
        practica(ana, 1, false, "2026-10-03T15:00:00Z");
        presentarDiagnosticoSinResponder(tokenAna); // fecha de hoy: queda de primera

        historial(tokenAna, "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(3))
                .andExpect(jsonPath("$.tamano").value(20))
                .andExpect(jsonPath("$.contenido[*].tipo", contains("DIAGNOSTICO", "PRACTICA", "PRACTICA")))
                .andExpect(jsonPath("$.contenido[0].aciertos").value(0))
                .andExpect(jsonPath("$.contenido[0].total").value(24))
                .andExpect(jsonPath("$.contenido[0].categoria").value("ALTO_RIESGO"))
                .andExpect(jsonPath("$.contenido[0].correcto").doesNotExist())
                .andExpect(jsonPath("$.contenido[1].correcto").value(false))
                .andExpect(jsonPath("$.contenido[1].nivel").isNumber())
                .andExpect(jsonPath("$.contenido[1].tiempoSeg").value(40))
                .andExpect(jsonPath("$.contenido[1].competenciaCodigo").isString())
                .andExpect(jsonPath("$.contenido[2].correcto").value(true));
    }

    @Test
    void noMezclaElHistorialDeOtroEstudiante() throws Exception {
        practica(beto, 0, true, "2026-10-01T15:00:00Z");

        historial(tokenAna, "").andExpect(jsonPath("$.totalElementos").value(0));
    }

    // ------------------------------------------------------------ filtros

    @Test
    void filtraPorTipoDeActividad() throws Exception {
        practica(ana, 0, true, "2026-10-01T15:00:00Z");
        presentarDiagnosticoSinResponder(tokenAna);

        historial(tokenAna, "tipo=PRACTICA")
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].tipo").value("PRACTICA"));
        historial(tokenAna, "tipo=DIAGNOSTICO")
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].tipo").value("DIAGNOSTICO"));
    }

    @Test
    void conCompetenciaElDiagnosticoMuestraLosAciertosDeEsaCompetencia() throws Exception {
        String competencia = (String) ejercicios.get(0)[1];
        practica(ana, 0, true, "2026-10-01T15:00:00Z");
        practica(ana, indiceDeOtraCompetencia(competencia), true, "2026-10-02T15:00:00Z");
        presentarDiagnosticoSinResponder(tokenAna);

        historial(tokenAna, "competencia=" + competencia.toLowerCase())
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.contenido[*].competenciaCodigo", everyItem(is(competencia))))
                .andExpect(jsonPath("$.contenido[0].tipo").value("DIAGNOSTICO"))
                .andExpect(jsonPath("$.contenido[0].total").value(8));
    }

    @Test
    void filtraPorFechasIncluyendoAmbosExtremos() throws Exception {
        practica(ana, 0, true, "2026-10-01T15:00:00Z");  // 1 de octubre (Colombia)
        practica(ana, 1, true, "2026-10-05T15:00:00Z");  // 5 de octubre
        practica(ana, 2, true, "2026-10-10T15:00:00Z");  // 10 de octubre

        historial(tokenAna, "desde=2026-10-01&hasta=2026-10-05")
                .andExpect(jsonPath("$.totalElementos").value(2));
        historial(tokenAna, "desde=2026-10-06")
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    void desdePosteriorAHastaDa422() throws Exception {
        historial(tokenAna, "desde=2026-10-10&hasta=2026-10-01")
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void unTipoInexistenteDa400() throws Exception {
        historial(tokenAna, "tipo=EXAMEN").andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------ paginación

    @Test
    void pagina20ResultadosPorPagina() throws Exception {
        for (int i = 0; i < 25; i++) {
            practica(ana, i, i % 2 == 0, Instant.parse("2026-10-01T15:00:00Z").plusSeconds(i * 3600L).toString());
        }

        historial(tokenAna, "")
                .andExpect(jsonPath("$.contenido.length()").value(20))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.totalElementos").value(25))
                .andExpect(jsonPath("$.totalPaginas").value(2));
        historial(tokenAna, "page=1")
                .andExpect(jsonPath("$.contenido.length()").value(5))
                .andExpect(jsonPath("$.pagina").value(1));
        historial(tokenAna, "page=-1").andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------ acceso

    @Test
    void unEstudianteNoPuedeVerElHistorialDeOtro() throws Exception {
        historial(tokenAna, "estudianteId=" + beto.getId()).andExpect(status().isForbidden());
        historial(tokenAna, "estudianteId=" + ana.getId()).andExpect(status().isOk());
    }

    @Test
    void elDocenteVeSoloAEstudiantesDeSusGrupos() throws Exception {
        practica(ana, 0, true, "2026-10-01T15:00:00Z");

        historial(tokenDocente, "estudianteId=" + ana.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1));
        historial(tokenDocente, "estudianteId=" + beto.getId()).andExpect(status().isForbidden());
        historial(tokenOtroDocente, "estudianteId=" + ana.getId()).andExpect(status().isForbidden());
        historial(tokenDocente, "").andExpect(status().isUnprocessableContent());
    }

    @Test
    void sinTokenDa401YElAdministradorNoTieneAcceso() throws Exception {
        mockMvc.perform(get("/api/historial")).andExpect(status().isUnauthorized());
        historial(tokenAdmin, "").andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------ utilidades

    private Usuario crear(String nombre, String correo, NombreRol rol) {
        return usuarioRepository.save(new Usuario(nombre, "Prueba", correo, "x", rolRepository.findByNombre(rol).orElseThrow()));
    }

    private String token(Usuario usuario) {
        return "Bearer " + jwtService.emitir(UsuarioAutenticado.desde(usuario)).valor();
    }

    private ResultActions historial(String token, String consulta) throws Exception {
        return mockMvc.perform(get("/api/historial?" + consulta).header("Authorization", token));
    }

    /** Intento de práctica sobre el ejercicio sembrado número {@code indice}, con fecha fija. */
    private void practica(Usuario estudiante, int indice, boolean correcto, String fecha) {
        jdbc.update("""
                INSERT INTO intento_practica (usuario_id, ejercicio_id, competencia_id, nivel_ejercicio, numero_intento,
                                              respuesta_numerica, es_correcta, tiempo_seg, clave_idempotencia, creado_en)
                SELECT ?, e.id, e.competencia_id, e.nivel, 1, 1, ?, 40, ?, ? FROM ejercicio e WHERE e.id = ?""",
                estudiante.getId(), correcto, UUID.randomUUID(), Timestamp.from(Instant.parse(fecha)), ejercicios.get(indice)[0]);
    }

    private int indiceDeOtraCompetencia(String competencia) {
        for (int i = 0; i < ejercicios.size(); i++) {
            if (!competencia.equals(ejercicios.get(i)[1])) return i;
        }
        throw new IllegalStateException("La semilla solo tiene ejercicios de una competencia");
    }

    /** Inicia y entrega el diagnóstico por la API, sin responder nada (0/24). */
    private void presentarDiagnosticoSinResponder(String token) throws Exception {
        String intento = mockMvc.perform(post("/api/diagnosticos/intentos").header("Authorization", token))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(intento, "$.id");
        mockMvc.perform(post("/api/diagnosticos/intentos/" + id + "/finalizar").header("Authorization", token))
                .andExpect(status().isOk());
        // Todas las peticiones de la prueba comparten la transacción: se escribe lo que Hibernate
        // tiene en memoria para que la consulta SQL del historial lo vea (en producción hay commit).
        entityManager.flush();
    }

}
