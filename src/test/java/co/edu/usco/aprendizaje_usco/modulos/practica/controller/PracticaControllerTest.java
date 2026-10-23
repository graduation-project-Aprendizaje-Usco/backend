package co.edu.usco.aprendizaje_usco.modulos.practica.controller;

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

    private String tokenEstudiante;
    private String tokenDocente;

    @BeforeEach
    void preparar() {
        tokenEstudiante = token(crearUsuario("Ana", "u20269999101@usco.edu.co", NombreRol.ESTUDIANTE));
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
        String respuesta = opcionId != null
                ? "\"opcionId\": " + opcionId
                : "\"respuestaNumerica\": " + respuestaNumerica;
        return """
                {"ejercicioId": %d, %s, "tiempoSeg": 10, "pistaUsada": false, "claveIdempotencia": "%s"}
                """.formatted(ejercicioId, respuesta, clave);
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
