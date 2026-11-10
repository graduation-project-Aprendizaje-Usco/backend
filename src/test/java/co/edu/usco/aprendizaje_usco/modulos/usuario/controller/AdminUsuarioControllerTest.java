package co.edu.usco.aprendizaje_usco.modulos.usuario.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
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
    void creaUnDocenteConContrasenaTemporalConLaQuePuedeIniciarSesion() throws Exception {
        String respuesta = mockMvc.perform(json(post("/api/admin/usuarios"), nuevoDocente("Nuevo.Docente@USCO.edu.co"))
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.correo").value("nuevo.docente@usco.edu.co"))
                .andExpect(jsonPath("$.usuario.rol").value("DOCENTE"))
                .andExpect(jsonPath("$.usuario.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.usuario.programaAcademico").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String temporal = JsonPath.read(respuesta, "$.passwordTemporal");
        assertThat(temporal).hasSize(12).matches(".*\\p{L}.*").matches(".*\\d.*");

        mockMvc.perform(json(post("/api/auth/login"),
                        "{\"correo\":\"nuevo.docente@usco.edu.co\",\"password\":\"" + temporal + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.rol").value("DOCENTE"));
    }

    @Test
    void rechazaCorreoRepetidoYEstudianteSinCorreoDeEstudianteOSinPrograma() throws Exception {
        String token = bearer(admin);
        mockMvc.perform(json(post("/api/admin/usuarios"), nuevoDocente("diego.docente@usco.edu.co")).header("Authorization", token))
                .andExpect(status().isConflict());
        mockMvc.perform(json(post("/api/admin/usuarios"),
                        nuevo("eva.otra@usco.edu.co", "ESTUDIANTE", "Ingeniería Civil")).header("Authorization", token))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(json(post("/api/admin/usuarios"), nuevo("u20250000002@usco.edu.co", "ESTUDIANTE", null))
                        .header("Authorization", token))
                .andExpect(status().isUnprocessableEntity());
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
        String temporal = JsonPath.read(mockMvc.perform(json(post("/api/admin/usuarios"), nuevoDocente("temporal@usco.edu.co"))
                        .header("Authorization", bearer(admin)))
                .andReturn().getResponse().getContentAsString(), "$.passwordTemporal");
        Long id = usuarioRepository.findByCorreo("temporal@usco.edu.co").orElseThrow().getId();
        String login = "{\"correo\":\"temporal@usco.edu.co\",\"password\":\"" + temporal + "\"}";

        mockMvc.perform(json(patch("/api/admin/usuarios/{id}/estado", id), "{\"estado\":\"INACTIVO\"}")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"));
        mockMvc.perform(json(post("/api/auth/login"), login)).andExpect(status().isUnauthorized());

        mockMvc.perform(json(patch("/api/admin/usuarios/{id}/estado", id), "{\"estado\":\"ACTIVO\"}")
                .header("Authorization", bearer(admin)));
        mockMvc.perform(json(post("/api/auth/login"), login)).andExpect(status().isOk());
    }

    @Test
    void elAdministradorNoPuedeDesactivarseNiQuitarseElRol() throws Exception {
        mockMvc.perform(json(patch("/api/admin/usuarios/{id}/estado", admin.getId()), "{\"estado\":\"INACTIVO\"}")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("No puedes desactivar tu propia cuenta"));
        mockMvc.perform(json(put("/api/admin/usuarios/{id}", admin.getId()), edicion("DOCENTE", null))
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isUnprocessableEntity());
        Usuario guardado = usuarioRepository.findByCorreo("ada.admin@usco.edu.co").orElseThrow();
        assertThat(guardado.estaActivo()).isTrue();
        assertThat(guardado.getRol().getNombre()).isEqualTo(NombreRol.ADMIN);
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
}
