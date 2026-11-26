package co.edu.usco.aprendizaje_usco.modulos.usuario.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.GrupoRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Rol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.service.GrupoService;
import co.edu.usco.aprendizaje_usco.security.JwtService;
import co.edu.usco.aprendizaje_usco.security.UsuarioAutenticado;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {"MAIL_HOST=smtp.test", "MAIL_USERNAME=test-user", "MAIL_PASSWORD=test-password"})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class AdminUsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private JavaMailSender mailSender;

    private Usuario admin;
    private Usuario docente;
    private Usuario estudiante;

    @BeforeEach
    void prepararDatos() {
        admin = guardar("Ada", "Admin", "ada.admin@usco.edu.co", NombreRol.ADMIN);
        docente = guardar("Diego", "Docente", "diego.docente@usco.edu.co", NombreRol.DOCENTE);
        estudiante = guardar("Eva", "Estudiante", "u20250000001@usco.edu.co", NombreRol.ESTUDIANTE);
    }

    @Test
    void sinTokenResponde401YOtrosRolesReciben403EnTodosLosEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/admin/usuarios/{id}/reenviar-activacion", estudiante.getId()))
                .andExpect(status().isUnauthorized());
        for (Usuario noAdmin : new Usuario[] {docente, estudiante}) {
            String token = bearer(noAdmin);
            mockMvc.perform(get("/api/admin/usuarios").header("Authorization", token))
                    .andExpect(status().isForbidden());
            mockMvc.perform(json(post("/api/admin/usuarios"), nuevoDocente("x@usco.edu.co")).header("Authorization", token))
                    .andExpect(status().isForbidden());
            mockMvc.perform(json(put("/api/admin/usuarios/{id}", estudiante.getId()), edicion("ESTUDIANTE", "Ingeniería Civil"))
                            .header("Authorization", token))
                    .andExpect(status().isForbidden());
            mockMvc.perform(json(patch("/api/admin/usuarios/{id}/estado", estudiante.getId()), "{\"estado\":\"INACTIVO\"}")
                            .header("Authorization", token))
                    .andExpect(status().isForbidden());
            mockMvc.perform(post("/api/admin/usuarios/{id}/reenviar-activacion", estudiante.getId())
                            .header("Authorization", token))
                    .andExpect(status().isForbidden());
        }
        assertThat(usuarioRepository.findById(estudiante.getId()).orElseThrow().estaActivo()).isTrue();
    }

    @Test
    void listaConBusquedaYFiltrosPorRolYEstado() throws Exception {
        estudiante.desactivar();
        usuarioRepository.saveAndFlush(estudiante);

        mockMvc.perform(get("/api/admin/usuarios").param("rol", "DOCENTE").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.correo == 'diego.docente@usco.edu.co')]").exists())
                .andExpect(jsonPath("$[?(@.rol != 'DOCENTE')]").isEmpty());
        mockMvc.perform(get("/api/admin/usuarios").param("estado", "INACTIVO").header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$[?(@.estado != 'INACTIVO')]").isEmpty())
                .andExpect(jsonPath("$[?(@.correo == 'u20250000001@usco.edu.co')]").exists());
        mockMvc.perform(get("/api/admin/usuarios").param("buscar", "DIEGO").header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].apellidos").value("Docente"))
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
    }

    @Test
    void creaUnDocenteSinDevolverContrasenaYPermiteConfigurarlaUnaSolaVez() throws Exception {
        String respuesta = mockMvc.perform(json(post("/api/admin/usuarios"), nuevoDocente("Nuevo.Docente@USCO.edu.co"))
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.correo").value("nuevo.docente@usco.edu.co"))
                .andExpect(jsonPath("$.usuario.rol").value("DOCENTE"))
                .andExpect(jsonPath("$.usuario.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.usuario.configuracionPendiente").value(true))
                .andExpect(jsonPath("$.passwordTemporal").doesNotExist())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.usuario.programaAcademico").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        Number usuarioId = JsonPath.read(respuesta, "$.usuario.id");
        String token = tokenDelCorreo("nuevo.docente@usco.edu.co");
        String hashGuardado = jdbcTemplate.queryForObject(
                "select token_hash from activacion_cuenta where usuario_id = ?", String.class, usuarioId.longValue());
        assertThat(hashGuardado).hasSize(64).doesNotContain(token);

        mockMvc.perform(json(post("/api/auth/login"),
                        "{\"correo\":\"nuevo.docente@usco.edu.co\",\"password\":\"ClaveInicial2026\"}"))
                .andExpect(status().isUnauthorized());
        activarCuenta(token, "ClaveInicial2026");
        mockMvc.perform(json(post("/api/auth/login"),
                        "{\"correo\":\"nuevo.docente@usco.edu.co\",\"password\":\"ClaveInicial2026\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.rol").value("DOCENTE"));
        mockMvc.perform(json(post("/api/auth/activar-cuenta"),
                        "{\"token\":\"" + token + "\",\"password\":\"ClaveOtra2026\"}"))
                .andExpect(status().is(422));
        mockMvc.perform(get("/api/admin/usuarios").param("buscar", "nuevo.docente")
                        .header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$[0].configuracionPendiente").value(false));
    }

    @Test
    void reenviaElEnlacePendienteYElAnteriorDejaDeSerValido() throws Exception {
        String respuesta = mockMvc.perform(json(post("/api/admin/usuarios"), nuevoDocente("reenvio@usco.edu.co"))
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number usuarioId = JsonPath.read(respuesta, "$.usuario.id");
        String tokenAnterior = tokenDelCorreo("reenvio@usco.edu.co");

        mockMvc.perform(post("/api/admin/usuarios/{id}/reenviar-activacion", usuarioId.longValue())
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());
        String tokenNuevo = tokenDelCorreo("reenvio@usco.edu.co");

        mockMvc.perform(json(post("/api/auth/activar-cuenta"),
                        "{\"token\":\"" + tokenAnterior + "\",\"password\":\"ClaveInicial2026\"}"))
                .andExpect(status().isNotFound());
        activarCuenta(tokenNuevo, "ClaveInicial2026");
        mockMvc.perform(post("/api/admin/usuarios/{id}/reenviar-activacion", usuarioId.longValue())
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isConflict());
    }

    @Test
    void enlaceVencidoNoSeConsumeYPuedeRenovarseDesdeAdministracion() throws Exception {
        String respuesta = mockMvc.perform(json(post("/api/admin/usuarios"), nuevoDocente("vencido@usco.edu.co"))
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number usuarioId = JsonPath.read(respuesta, "$.usuario.id");
        String tokenVencido = tokenDelCorreo("vencido@usco.edu.co");
        jdbcTemplate.update("update activacion_cuenta set vence_en = now() - interval '1 minute' where usuario_id = ?",
                usuarioId.longValue());
        entityManager.clear();

        mockMvc.perform(json(post("/api/auth/activar-cuenta"),
                        "{\"token\":\"" + tokenVencido + "\",\"password\":\"ClaveInicial2026\"}"))
                .andExpect(status().isUnprocessableContent());
        mockMvc.perform(post("/api/admin/usuarios/{id}/reenviar-activacion", usuarioId.longValue())
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());
        activarCuenta(tokenDelCorreo("vencido@usco.edu.co"), "ClaveInicial2026");
    }

    @Test
    void rechazaCorreoRepetidoYEstudianteSinCorreoDeEstudianteOSinPrograma() throws Exception {
        String token = bearer(admin);
        mockMvc.perform(json(post("/api/admin/usuarios"), nuevoDocente("diego.docente@usco.edu.co")).header("Authorization", token))
                .andExpect(status().isConflict());
        mockMvc.perform(json(post("/api/admin/usuarios"),
                        nuevo("eva.otra@usco.edu.co", "ESTUDIANTE", "Ingeniería Civil")).header("Authorization", token))
                .andExpect(status().is(422));
        mockMvc.perform(json(post("/api/admin/usuarios"), nuevo("u20250000002@usco.edu.co", "ESTUDIANTE", null))
                        .header("Authorization", token))
                .andExpect(status().is(422));
        mockMvc.perform(json(post("/api/admin/usuarios"), "{\"nombres\":\"\",\"correo\":\"no-es-correo\"}")
                        .header("Authorization", token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void editaDatosYCambiaElRol() throws Exception {
        mockMvc.perform(json(put("/api/admin/usuarios/{id}", docente.getId()), edicion("ADMIN", null))
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Nombre Editado"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
        mockMvc.perform(json(put("/api/admin/usuarios/{id}", 999_999L), edicion("DOCENTE", null))
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void unUsuarioDesactivadoNoPuedeIniciarSesionYAlReactivarloSi() throws Exception {
        String respuesta = mockMvc.perform(json(post("/api/admin/usuarios"), nuevoDocente("temporal@usco.edu.co"))
                        .header("Authorization", bearer(admin)))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(respuesta, "$.usuario.id");
        activarCuenta(tokenDelCorreo("temporal@usco.edu.co"), "ClaveInicial2026");
        String login = "{\"correo\":\"temporal@usco.edu.co\",\"password\":\"ClaveInicial2026\"}";

        mockMvc.perform(json(patch("/api/admin/usuarios/{id}/estado", id.longValue()), "{\"estado\":\"INACTIVO\"}")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"));
        mockMvc.perform(json(post("/api/auth/login"), login)).andExpect(status().isUnauthorized());

        mockMvc.perform(json(patch("/api/admin/usuarios/{id}/estado", id.longValue()), "{\"estado\":\"ACTIVO\"}")
                .header("Authorization", bearer(admin)));
        mockMvc.perform(json(post("/api/auth/login"), login)).andExpect(status().isOk());
    }

    @Test
    void elAdministradorNoPuedeDesactivarseNiQuitarseElRol() throws Exception {
        mockMvc.perform(json(patch("/api/admin/usuarios/{id}/estado", admin.getId()), "{\"estado\":\"INACTIVO\"}")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message").value("No puedes desactivar tu propia cuenta"));
        mockMvc.perform(json(put("/api/admin/usuarios/{id}", admin.getId()), edicion("DOCENTE", null))
                        .header("Authorization", bearer(admin)))
                .andExpect(status().is(422));
        Usuario guardado = usuarioRepository.findByCorreo("ada.admin@usco.edu.co").orElseThrow();
        assertThat(guardado.estaActivo()).isTrue();
        assertThat(guardado.getRol().getNombre()).isEqualTo(NombreRol.ADMIN);
    }

    @Test
    void elResumenCuentaUsuariosPorRolGruposYActividadesActivas() throws Exception {
        mockMvc.perform(get("/api/admin/resumen").header("Authorization", bearer(docente)))
                .andExpect(status().isForbidden());
        String antes = mockMvc.perform(get("/api/admin/resumen").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Usuario inactivo = guardar("Ina", "Activa", "u20250000009@usco.edu.co", NombreRol.ESTUDIANTE);
        inactivo.desactivar();
        usuarioRepository.saveAndFlush(inactivo);
        grupoService.crear(docente.getId(), new GrupoRequest("RESUMEN-1", "Grupo del resumen"));

        String despues = mockMvc.perform(get("/api/admin/resumen").header("Authorization", bearer(admin)))
                .andReturn().getResponse().getContentAsString();
        String estudiantes = "$.usuariosPorRol[?(@.rol == 'ESTUDIANTE')]";
        assertThat(primero(despues, estudiantes + ".total")).isEqualTo(primero(antes, estudiantes + ".total") + 1);
        assertThat(primero(despues, estudiantes + ".activos")).isEqualTo(primero(antes, estudiantes + ".activos"));
        assertThat((Integer) JsonPath.read(despues, "$.grupos")).isEqualTo((Integer) JsonPath.read(antes, "$.grupos") + 1);
        // Mismo criterio que el motor: el ejercicio y su contenido activos.
        assertThat(((Number) JsonPath.read(despues, "$.ejerciciosActivos")).longValue()).isEqualTo(jdbcTemplate.queryForObject("""
                select count(*) from ejercicio e join contenido c on c.id = e.contenido_id
                where e.activo and c.activo""", Long.class));
        assertThat(((Number) JsonPath.read(despues, "$.contenidosActivos")).longValue())
                .isEqualTo(jdbcTemplate.queryForObject("select count(*) from contenido where activo", Long.class));
    }

    /** Un filtro de JsonPath devuelve una lista: toma el único valor. */
    private static int primero(String json, String ruta) {
        java.util.List<Integer> valores = JsonPath.read(json, ruta);
        return valores.get(0);
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder peticion, String cuerpo) {
        return peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
    }

    private static String nuevoDocente(String correo) {
        return nuevo(correo, "DOCENTE", null);
    }

    private static String nuevo(String correo, String rol, String programa) {
        return """
                {"nombres":"Nuevo","apellidos":"Usuario","correo":"%s","rol":"%s","programaAcademico":%s}"""
                .formatted(correo, rol, programa == null ? "null" : "\"" + programa + "\"");
    }

    private static String edicion(String rol, String programa) {
        return """
                {"nombres":"Nombre Editado","apellidos":"Apellido","rol":"%s","programaAcademico":%s}"""
                .formatted(rol, programa == null ? "null" : "\"" + programa + "\"");
    }

    private Usuario guardar(String nombres, String apellidos, String correo, NombreRol nombreRol) {
        Rol rol = rolRepository.findByNombre(nombreRol).orElseThrow();
        Usuario usuario = new Usuario(nombres, apellidos, correo, "$2a$10$hashDePrueba", rol);
        if (nombreRol == NombreRol.ESTUDIANTE) {
            usuario.setProgramaAcademico("Ingeniería de Software");
        }
        return usuarioRepository.saveAndFlush(usuario);
    }

    private String bearer(Usuario usuario) {
        return "Bearer " + jwtService.emitir(UsuarioAutenticado.desde(usuario)).valor();
    }

    private void activarCuenta(String token, String password) throws Exception {
        mockMvc.perform(json(post("/api/auth/activar-cuenta"),
                        "{\"token\":\"" + token + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isNoContent());
    }

    private String tokenDelCorreo(String correo) {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, atLeastOnce()).send(captor.capture());
        return captor.getAllValues().stream()
                .filter(mensaje -> mensaje.getTo() != null && java.util.List.of(mensaje.getTo()).contains(correo))
                .map(SimpleMailMessage::getText)
                .map(texto -> {
                    Matcher matcher = Pattern.compile("token=([A-Za-z0-9_-]+)").matcher(texto);
                    return matcher.find() ? matcher.group(1) : null;
                })
                .filter(java.util.Objects::nonNull)
                .reduce((primero, ultimo) -> ultimo)
                .orElseThrow(() -> new AssertionError("El correo no contenía enlace de activación para " + correo));
    }
}
