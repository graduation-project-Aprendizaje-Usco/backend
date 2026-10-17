package co.edu.usco.aprendizaje_usco.modulos.usuario.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Rol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * SCRUM-85: pruebas de controlador del login con MockMvc, la seguridad real y
 * PostgreSQL en Docker (Testcontainers).
 *
 * @Transactional: los usuarios creados en cada prueba se borran al terminarla.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    private static final String CLAVE = "Clave2026*";
    private static final String CREDENCIALES_INVALIDAS = "Correo o contraseña incorrectos";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void crearUsuarios() {
        Rol estudiante = rolRepository.findByNombre(NombreRol.ESTUDIANTE).orElseThrow();
        usuarioRepository.save(new Usuario("Ana", "Pérez", "ana@usco.edu.co", passwordEncoder.encode(CLAVE), estudiante));

        Usuario inactivo = new Usuario("Beto", "Gómez", "beto@usco.edu.co", passwordEncoder.encode(CLAVE), estudiante);
        inactivo.desactivar();
        usuarioRepository.save(inactivo);
    }

    // ------------------------------------------------- las 5 pruebas de la tarjeta

    @Test
    void loginCorrectoDevuelve200ConTokenYUsuario() throws Exception {
        login("ana@usco.edu.co", CLAVE)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEn").value(greaterThan(7000)))
                .andExpect(jsonPath("$.usuario.id").isNumber())
                .andExpect(jsonPath("$.usuario.nombres").value("Ana"))
                .andExpect(jsonPath("$.usuario.correo").value("ana@usco.edu.co"))
                .andExpect(jsonPath("$.usuario.rol").value("ESTUDIANTE"))
                // La contraseña nunca viaja de vuelta, ni en claro ni como hash.
                .andExpect(jsonPath("$.usuario.password").doesNotExist())
                .andExpect(content().string(not(containsString("$2a$"))));
    }

    @Test
    void contrasenaErradaDevuelve401ConMensajeGenerico() throws Exception {
        login("ana@usco.edu.co", "otraClave")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value(CREDENCIALES_INVALIDAS))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
    }

    @Test
    void usuarioInactivoDevuelve401ConElMismoMensaje() throws Exception {
        // Con la contraseña correcta: el mensaje no debe revelar que la cuenta existe.
        login("beto@usco.edu.co", CLAVE)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(CREDENCIALES_INVALIDAS));
    }

    @Test
    void registroConCorreoRepetidoDevuelve409() throws Exception {
        registro("  ANA@usco.edu.co ")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("El correo ya está registrado"));
    }

    @Test
    void meSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Debes iniciar sesión para continuar"));
    }

    // ------------------------------------------------- casos adicionales

    @Test
    void correoInexistenteDevuelveElMismo401() throws Exception {
        login("nadie@usco.edu.co", CLAVE)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(CREDENCIALES_INVALIDAS));
    }

    @Test
    void loginConCamposVaciosDevuelve400ConLosCampos() throws Exception {
        login("", "")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.length()").value(2));
    }

    @Test
    void registroNuevoDevuelve201ComoEstudianteYPuedeIniciarSesion() throws Exception {
        registro("  Carla@USCO.edu.co ")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.correo").value("carla@usco.edu.co"))
                .andExpect(jsonPath("$.rol").value("ESTUDIANTE"));

        login("carla@usco.edu.co", CLAVE).andExpect(status().isOk());
    }

    @Test
    void meConElTokenDelLoginDevuelveElUsuario() throws Exception {
        String respuesta = login("ana@usco.edu.co", CLAVE).andReturn().getResponse().getContentAsString();
        String token = respuesta.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("ana@usco.edu.co"))
                .andExpect(jsonPath("$.rol").value("ESTUDIANTE"));
    }

    // ------------------------------------------------- apoyo

    private ResultActions login(String correo, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"correo": "%s", "password": "%s"}
                        """.formatted(correo, password)));
    }

    private ResultActions registro(String correo) throws Exception {
        return mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombres": "Carla", "apellidos": "Ruiz", "correo": "%s", "password": "%s"}
                        """.formatted(correo, CLAVE)));
    }

}
