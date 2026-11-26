package co.edu.usco.aprendizaje_usco.integracion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Ejercicio;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.EjercicioRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.ProgramaAcademico;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Rol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * Recorrido end-to-end de los tres roles a través de la API real (SCRUM-140).
 * Cada prueba usa los endpoints y el login reales; solo el administrador inicial
 * se crea como fixture, porque no existe un registro público de administradores.
 */
@SpringBootTest(properties = {"MAIL_HOST=smtp.test", "MAIL_USERNAME=test-user", "MAIL_PASSWORD=test-password"})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class FlujosCompletosE2ETest {

    private static final String PASSWORD = "Clave2026*";
    private static final String ASIGNATURA = "CALCULO_DIFERENCIAL";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EjercicioRepository ejercicioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JavaMailSender mailSender;

    @Test
    void administradorGestionaUsuariosYRoles() throws Exception {
        Usuario admin = crearUsuario("Ada", "Admin E2E", "admin.e2e@usco.edu.co", NombreRol.ADMIN, null);
        String tokenAdmin = login("admin.e2e@usco.edu.co", PASSWORD);

        mockMvc.perform(json(post("/api/admin/usuarios"),
                        nuevoUsuario("Docente", "E2E", "docente.e2e@usco.edu.co", "DOCENTE", null))
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.rol").value("DOCENTE"))
                .andExpect(jsonPath("$.passwordTemporal").doesNotExist());
        activarCuenta("docente.e2e@usco.edu.co", "ClaveInicial2026");
        String passwordDocente = "ClaveInicial2026";

        mockMvc.perform(json(post("/api/auth/login"),
                        "{\"correo\":\"docente.e2e@usco.edu.co\",\"password\":\"" + passwordDocente + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.rol").value("DOCENTE"));

        String estudianteJson = mockMvc.perform(json(post("/api/admin/usuarios"),
                        nuevoUsuario("Estudiante", "E2E", "u20269999991@usco.edu.co", "ESTUDIANTE",
                                ProgramaAcademico.INGENIERIA_DE_SOFTWARE.getNombre()))
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.rol").value("ESTUDIANTE"))
                .andReturn().getResponse().getContentAsString();
        Number estudianteId = JsonPath.read(estudianteJson, "$.usuario.id");
        activarCuenta("u20269999991@usco.edu.co", "ClaveInicial2026");
        String passwordEstudiante = "ClaveInicial2026";

        mockMvc.perform(json(patch("/api/admin/usuarios/{id}/estado", estudianteId.longValue()),
                        "{\"estado\":\"INACTIVO\"}").header("Authorization", tokenAdmin))
                .andExpect(status().isOk());
        mockMvc.perform(json(post("/api/auth/login"),
                        "{\"correo\":\"u20269999991@usco.edu.co\",\"password\":\"" + passwordEstudiante + "\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(json(patch("/api/admin/usuarios/{id}/estado", estudianteId.longValue()),
                        "{\"estado\":\"ACTIVO\"}").header("Authorization", tokenAdmin))
                .andExpect(status().isOk());
        mockMvc.perform(json(post("/api/auth/login"),
                        "{\"correo\":\"u20269999991@usco.edu.co\",\"password\":\"" + passwordEstudiante + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(json(patch("/api/admin/usuarios/{id}/estado", admin.getId()),
                        "{\"estado\":\"INACTIVO\"}").header("Authorization", tokenAdmin))
                .andExpect(status().is(422));

        mockMvc.perform(get("/api/admin/usuarios")
                        .param("rol", "DOCENTE")
                        .param("buscar", "docente.e2e")
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].correo").value("docente.e2e@usco.edu.co"));

        mockMvc.perform(get("/api/admin/resumen").header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuariosPorRol.length()").value(3));
        mockMvc.perform(get("/api/admin/actividades").header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenidos.length()").value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.ejercicios.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void flujoCompletoEstudianteDocenteYAdministrador() throws Exception {
        crearUsuario("Ada", "Admin E2E", "admin.flujo@usco.edu.co", NombreRol.ADMIN, null);
        String tokenAdmin = login("admin.flujo@usco.edu.co", PASSWORD);

        mockMvc.perform(json(post("/api/admin/usuarios"),
                        nuevoUsuario("Diego", "Docente E2E", "docente.flujo@usco.edu.co", "DOCENTE", null))
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.passwordTemporal").doesNotExist());
        activarCuenta("docente.flujo@usco.edu.co", "ClaveInicial2026");
        String passwordDocente = "ClaveInicial2026";
        String tokenDocente = login("docente.flujo@usco.edu.co", passwordDocente);

        String estudianteJson = mockMvc.perform(json(post("/api/admin/usuarios"),
                        nuevoUsuario("Eva", "Estudiante E2E", "u20269999992@usco.edu.co", "ESTUDIANTE",
                                ProgramaAcademico.INGENIERIA_DE_SOFTWARE.getNombre()))
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number estudianteId = JsonPath.read(estudianteJson, "$.usuario.id");
        activarCuenta("u20269999992@usco.edu.co", "ClaveInicial2026");
        String passwordEstudiante = "ClaveInicial2026";
        String tokenEstudiante = login("u20269999992@usco.edu.co", passwordEstudiante);

        String grupoJson = mockMvc.perform(json(post("/api/grupos"),
                        "{\"codigo\":\"E2E-2026\",\"nombre\":\"Grupo E2E\"}")
                        .header("Authorization", tokenDocente))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.docenteCorreo").value("docente.flujo@usco.edu.co"))
                .andReturn().getResponse().getContentAsString();
        Number grupoId = JsonPath.read(grupoJson, "$.id");

        mockMvc.perform(json(post("/api/grupos/{id}/estudiantes", grupoId.longValue()),
                        "{\"correo\":\"u20269999992@usco.edu.co\"}")
                        .header("Authorization", tokenDocente))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.correo").value("u20269999992@usco.edu.co"));

        mockMvc.perform(get("/api/grupos/{id}/estudiantes", grupoId.longValue())
                        .header("Authorization", tokenDocente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(estudianteId.intValue()));

        // Diagnóstico: iniciar, responder los 24 ítems y finalizar.
        String intentoJson = mockMvc.perform(post("/api/diagnosticos/intentos")
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("EN_CURSO"))
                .andExpect(jsonPath("$.items.length()").value(24))
                .andReturn().getResponse().getContentAsString();
        Number intentoId = JsonPath.read(intentoJson, "$.id");
        List<Number> itemIds = JsonPath.read(intentoJson, "$.items[*].id");
        List<Number> opcionIds = JsonPath.read(intentoJson, "$.items[*].opciones[0].id");
        assertThat(itemIds).hasSize(24);

        for (int i = 0; i < itemIds.size(); i++) {
            mockMvc.perform(json(put("/api/diagnosticos/intentos/{intentoId}/respuestas/{itemId}",
                            intentoId.longValue(), itemIds.get(i).longValue()),
                            "{\"opcionId\":" + opcionIds.get(i).longValue() + "}")
                            .header("Authorization", tokenEstudiante))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/diagnosticos/intentos/{id}/finalizar", intentoId.longValue())
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADO"))
                .andExpect(jsonPath("$.resultado.puntaje").exists())
                .andExpect(jsonPath("$.resultado.dimensiones.length()").value(3));

        // Práctica: resolver tres ejercicios con retroalimentación.
        for (int i = 0; i < 3; i++) {
            String ejercicioJson = mockMvc.perform(get("/api/practica/siguiente")
                            .param("asignatura", ASIGNATURA)
                            .header("Authorization", tokenEstudiante))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            Number ejercicioId = JsonPath.read(ejercicioJson, "$.id");
            Ejercicio ejercicio = ejercicioRepository.findActivoConOpciones(ejercicioId.longValue()).orElseThrow();
            String cuerpo;
            if (ejercicio.esNumerico()) {
                cuerpo = "{\"ejercicioId\":" + ejercicioId.longValue()
                        + ",\"opcionId\":null,\"respuestaNumerica\":"
                        + ejercicio.getRespuestaNumerica().toPlainString()
                        + ",\"tiempoSeg\":5,\"pistaUsada\":false"
                        + ",\"claveIdempotencia\":\"" + UUID.randomUUID() + "\"}";
            } else {
                cuerpo = "{\"ejercicioId\":" + ejercicioId.longValue()
                        + ",\"opcionId\":" + ejercicio.opcionCorrecta().orElseThrow().getId()
                        + ",\"respuestaNumerica\":null,\"tiempoSeg\":5,\"pistaUsada\":false"
                        + ",\"claveIdempotencia\":\"" + UUID.randomUUID() + "\"}";
            }
            mockMvc.perform(json(post("/api/practica/intentos"), cuerpo)
                            .header("Authorization", tokenEstudiante))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.esCorrecta").value(true))
                    .andExpect(jsonPath("$.retroalimentacion").exists());
        }

        mockMvc.perform(get("/api/progreso/me").header("Authorization", tokenEstudiante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIntentos").value(3))
                .andExpect(jsonPath("$.ejerciciosCompletados").value(3));

        mockMvc.perform(get("/api/historial").header("Authorization", tokenEstudiante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(4));

        // El docente ve al estudiante de su grupo, su analítica y su historial.
        mockMvc.perform(get("/api/analitica/grupos/{id}", grupoId.longValue())
                        .header("Authorization", tokenDocente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEstudiantes").value(1))
                .andExpect(jsonPath("$.estudiantesConActividad").value(1))
                .andExpect(jsonPath("$.estudiantes[0].correo").value("u20269999992@usco.edu.co"));

        mockMvc.perform(get("/api/historial")
                        .param("estudianteId", String.valueOf(estudianteId.longValue()))
                        .header("Authorization", tokenDocente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(4));

        mockMvc.perform(get("/api/admin/resumen").header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grupos").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    private Usuario crearUsuario(String nombres, String apellidos, String correo, NombreRol rol,
                                 String programaAcademico) {
        Rol entidadRol = rolRepository.findByNombre(rol).orElseThrow();
        Usuario usuario = new Usuario(nombres, apellidos, correo, passwordEncoder.encode(PASSWORD), entidadRol);
        if (programaAcademico != null) {
            usuario.setProgramaAcademico(programaAcademico);
        }
        return usuarioRepository.saveAndFlush(usuario);
    }

    private String login(String correo, String password) throws Exception {
        MvcResult resultado = mockMvc.perform(json(post("/api/auth/login"),
                        "{\"correo\":\"" + correo + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String token = JsonPath.read(resultado.getResponse().getContentAsString(), "$.token");
        return "Bearer " + token;
    }

    private void activarCuenta(String correo, String password) throws Exception {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, atLeastOnce()).send(captor.capture());
        String token = captor.getAllValues().stream()
                .filter(mensaje -> mensaje.getTo() != null && List.of(mensaje.getTo()).contains(correo))
                .map(SimpleMailMessage::getText)
                .map(texto -> {
                    Matcher matcher = Pattern.compile("token=([A-Za-z0-9_-]+)").matcher(texto);
                    return matcher.find() ? matcher.group(1) : null;
                })
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No llegó enlace de activación a " + correo));
        mockMvc.perform(json(post("/api/auth/activar-cuenta"),
                        "{\"token\":\"" + token + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isNoContent());
    }

    private static String nuevoUsuario(String nombres, String apellidos, String correo, String rol,
                                       String programaAcademico) {
        return """
                {"nombres":"%s","apellidos":"%s","correo":"%s","rol":"%s","programaAcademico":%s}"""
                .formatted(nombres, apellidos, correo, rol,
                        programaAcademico == null ? "null" : "\"" + programaAcademico + "\"");
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder json(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder peticion, String cuerpo) {
        return peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
    }
}
