package co.edu.usco.aprendizaje_usco.modulos.practica.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class PracticaControllerTest {

    private static final String BASE = "/api/practica";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private JdbcTemplate jdbc;

    private Usuario estudiante;
    private String tokenEstudiante;
    private String tokenDocente;

    @BeforeEach
    void preparar() {
        estudiante = crearUsuario("Ana", "u20269999101@usco.edu.co", NombreRol.ESTUDIANTE);
        tokenEstudiante = token(estudiante);
        tokenDocente = token(crearUsuario("Diana", "u20269999102@usco.edu.co", NombreRol.DOCENTE));
    }

    @Test
    void siguienteExigeAsignaturaValida() throws Exception {
        mockMvc.perform(get(BASE + "/siguiente").header("Authorization", tokenEstudiante))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get(BASE + "/siguiente")
                        .param("asignatura", "CALCULO_VECTORIAL")
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isBadRequest());
    }

    @Test
    void siguienteDevuelveUnEjercicioSinClaveNiExplicacion() throws Exception {
        mockMvc.perform(get(BASE + "/siguiente")
                        .param("asignatura", "CALCULO_DIFERENCIAL")
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nivel").isNumber())
                .andExpect(jsonPath("$.opciones[0].texto").exists())
                .andExpect(jsonPath("$.opciones[0].esCorrecta").doesNotExist())
                .andExpect(jsonPath("$.explicacion").doesNotExist())
                .andExpect(jsonPath("$.respuestaNumerica").doesNotExist());
    }

    @Test
    void soloElEstudiantePuedeUsarLaPractica() throws Exception {
        mockMvc.perform(get(BASE + "/siguiente")
                        .param("asignatura", "CALCULO_DIFERENCIAL")
                        .header("Authorization", tokenDocente))
                .andExpect(status().isForbidden());
    }

    @Test
    void siNoHayEjerciciosDeLaAsignaturaResponde404() throws Exception {
        jdbc.update("""
                UPDATE ejercicio SET activo = false
                WHERE contenido_id IN (SELECT id FROM contenido WHERE asignatura = 'CALCULO_INTEGRAL')""");

        mockMvc.perform(get(BASE + "/siguiente")
                        .param("asignatura", "CALCULO_INTEGRAL")
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isNotFound());
    }

    @Test
    void primerAciertoDevuelveExplicacionYPersisteElIntento() throws Exception {
        long ejercicio = idEjercicio("DIF-ALG-1-01");
        long opcion = opcionCorrecta(ejercicio);

        mockMvc.perform(post(BASE + "/intentos")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(ejercicio, opcion, null, UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroIntento").value(1))
                .andExpect(jsonPath("$.esCorrecta").value(true))
                .andExpect(jsonPath("$.puedeReintentar").value(false))
                .andExpect(jsonPath("$.retroalimentacion.tipo").value("EXPLICACION"));

        assertThatIntentos(ejercicio, 1);
    }

    @Test
    void primerFalloDevuelvePistaYPermiteReintento() throws Exception {
        long ejercicio = idEjercicio("DIF-ALG-1-01");
        long opcion = opcionIncorrecta(ejercicio);

        mockMvc.perform(post(BASE + "/intentos")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(ejercicio, opcion, null, UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.esCorrecta").value(false))
                .andExpect(jsonPath("$.puedeReintentar").value(true))
                .andExpect(jsonPath("$.retroalimentacion.tipo").value("PISTA"))
                .andExpect(jsonPath("$.retroalimentacion.texto").value(containsString("Suma")));

        assertThatIntentos(ejercicio, 1);
    }

    @Test
    void elSegundoIntentoCorrectoNoCuentaComoDominioYNoPermiteUnTercero() throws Exception {
        long ejercicio = idEjercicio("DIF-ALG-1-01");
        long incorrecta = opcionIncorrecta(ejercicio);
        long correcta = opcionCorrecta(ejercicio);

        mockMvc.perform(post(BASE + "/intentos")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(ejercicio, incorrecta, null, UUID.randomUUID())))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE + "/intentos")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(ejercicio, correcta, null, UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroIntento").value(2))
                .andExpect(jsonPath("$.esCorrecta").value(true))
                .andExpect(jsonPath("$.retroalimentacion.tipo").value("EXPLICACION"));

        mockMvc.perform(post(BASE + "/intentos")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(ejercicio, correcta, null, UUID.randomUUID())))
                .andExpect(status().isConflict());
    }

    @Test
    void elSegundoFalloDevuelveLaExplicacionCompletaYSinOtroReintento() throws Exception {
        long ejercicio = idEjercicio("DIF-ALG-1-01");
        long incorrecta = opcionIncorrecta(ejercicio);

        mockMvc.perform(post(BASE + "/intentos")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(ejercicio, incorrecta, null, UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.retroalimentacion.tipo").value("PISTA"));

        mockMvc.perform(post(BASE + "/intentos")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(ejercicio, incorrecta, null, UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroIntento").value(2))
                .andExpect(jsonPath("$.esCorrecta").value(false))
                .andExpect(jsonPath("$.puedeReintentar").value(false))
                .andExpect(jsonPath("$.retroalimentacion.tipo").value("EXPLICACION"))
                .andExpect(jsonPath("$.retroalimentacion.texto").value(containsString("5x-4")));

        assertThatIntentos(ejercicio, 2);
    }

    @Test
    void cadaIntentoQuedaConsultableConLosCincoDatosDeTrazabilidad() throws Exception {
        long ejercicio = idEjercicio("DIF-ALG-1-01");
        long opcion = opcionIncorrecta(ejercicio);
        UUID clave = UUID.randomUUID();

        mockMvc.perform(post(BASE + "/intentos")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(ejercicio, opcion, null, clave, 17)))
                .andExpect(status().isCreated());

        Map<String, Object> fila = jdbc.queryForMap("""
                SELECT usuario_id, ejercicio_id, es_correcta, creado_en, tiempo_seg, pista_usada
                FROM intento_practica WHERE clave_idempotencia = ?""", clave);

        assertThat(((Number) fila.get("usuario_id")).longValue()).isEqualTo(estudiante.getId());
        assertThat(((Number) fila.get("ejercicio_id")).longValue()).isEqualTo(ejercicio);
        assertThat(fila.get("es_correcta")).isEqualTo(false);
        assertThat(fila.get("creado_en")).isNotNull();
        assertThat(((Number) fila.get("tiempo_seg")).intValue()).isEqualTo(17);
        assertThat(fila.get("pista_usada")).isEqualTo(false);
    }

    @Test
    void laMismaClaveDeIdempotenciaNoDuplicaElIntento() throws Exception {
        long ejercicio = idEjercicio("DIF-ALG-1-01");
        long opcion = opcionCorrecta(ejercicio);
        UUID clave = UUID.randomUUID();
        String cuerpo = cuerpo(ejercicio, opcion, null, clave);

        mockMvc.perform(post(BASE + "/intentos")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE + "/intentos")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isOk());

        assertThatIntentos(ejercicio, 1);
    }

    @Test
    void aceptaUnaRespuestaNumericaDentroDeLaTolerancia() throws Exception {
        long ejercicio = idEjercicio("DIF-ALG-1-04");

        mockMvc.perform(post(BASE + "/intentos")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(ejercicio, null, "5", UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.esCorrecta").value(true));
    }

    @Test
    void rechazaUnaOpcionDeOtroEjercicio() throws Exception {
        long ejercicio = idEjercicio("DIF-ALG-1-01");
        long opcionAjena = opcionCorrecta(idEjercicio("DIF-ALG-1-02"));

        mockMvc.perform(post(BASE + "/intentos")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(ejercicio, opcionAjena, null, UUID.randomUUID())))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void rechazaUnCuerpoSinRespuesta() throws Exception {
        long ejercicio = idEjercicio("DIF-ALG-1-01");

        mockMvc.perform(post(BASE + "/intentos")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ejercicioId": %d, "tiempoSeg": 10, "claveIdempotencia": "%s"}
                                """.formatted(ejercicio, UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    private void assertThatIntentos(long ejercicio, int esperados) {
        Integer total = jdbc.queryForObject(
                "SELECT count(*) FROM intento_practica WHERE ejercicio_id = ?", Integer.class, ejercicio);
        org.assertj.core.api.Assertions.assertThat(total).isEqualTo(esperados);
    }

    private long idEjercicio(String codigo) {
        return jdbc.queryForObject("SELECT id FROM ejercicio WHERE codigo = ?", Long.class, codigo);
    }

    private long opcionCorrecta(long ejercicioId) {
        return jdbc.queryForObject("""
                SELECT id FROM opcion_ejercicio WHERE ejercicio_id = ? AND es_correcta""", Long.class, ejercicioId);
    }

    private long opcionIncorrecta(long ejercicioId) {
        return jdbc.queryForObject("""
                SELECT id FROM opcion_ejercicio WHERE ejercicio_id = ? AND NOT es_correcta LIMIT 1""",
                Long.class, ejercicioId);
    }

    private static String cuerpo(long ejercicioId, Long opcionId, String respuestaNumerica, UUID clave) {
        return cuerpo(ejercicioId, opcionId, respuestaNumerica, clave, 10);
    }

    private static String cuerpo(long ejercicioId, Long opcionId, String respuestaNumerica, UUID clave,
                                 int tiempoSeg) {
        String respuesta = opcionId != null
                ? "\"opcionId\": " + opcionId
                : "\"respuestaNumerica\": " + respuestaNumerica;
        return """
                {"ejercicioId": %d, %s, "tiempoSeg": %d, "pistaUsada": false, "claveIdempotencia": "%s"}
                """.formatted(ejercicioId, respuesta, tiempoSeg, clave);
    }

    private Usuario crearUsuario(String nombre, String correo, NombreRol nombreRol) {
        Rol rol = rolRepository.findByNombre(nombreRol).orElseThrow();
        return usuarioRepository.saveAndFlush(new Usuario(
                nombre, "Prueba", correo, "hash", rol));
    }

    private String token(Usuario usuario) {
        return "Bearer " + jwtService.emitir(UsuarioAutenticado.desde(usuario)).valor();
    }

}
