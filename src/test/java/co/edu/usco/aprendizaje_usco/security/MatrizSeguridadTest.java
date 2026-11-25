package co.edu.usco.aprendizaje_usco.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.ProgramaAcademico;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Rol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/** Matriz endpoint × rol y verificación de datos sensibles (#148). */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class MatrizSeguridadTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario admin;
    private Usuario docente;
    private Usuario estudiante;

    @BeforeEach
    void prepararDatos() {
        admin = guardar("Ada", "Admin", "ada.matriz@usco.edu.co", NombreRol.ADMIN);
        docente = guardar("Diego", "Docente", "diego.matriz@usco.edu.co", NombreRol.DOCENTE);
        estudiante = guardar("Eva", "Estudiante", "u20269999801@usco.edu.co", NombreRol.ESTUDIANTE);
    }

    @Test
    void sinTokenResponde401EnEndpointsProtegidos() throws Exception {
        mockMvc.perform(get("/api/progreso/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/usuarios")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/grupos")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/practica/siguiente").param("asignatura", "CALCULO_DIFERENCIAL"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void estudianteSoloEntraASusEndpoints() throws Exception {
        String token = bearer(estudiante);
        mockMvc.perform(get("/api/progreso/me").header("Authorization", token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/diagnosticos/intentos/actual").header("Authorization", token))
                .andExpect(status().is2xxSuccessful());
        mockMvc.perform(get("/api/practica/siguiente").param("asignatura", "CALCULO_DIFERENCIAL")
                        .header("Authorization", token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/usuarios").header("Authorization", token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/grupos").header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    void docenteSoloEntraASusEndpoints() throws Exception {
        String token = bearer(docente);
        mockMvc.perform(get("/api/grupos").header("Authorization", token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/usuarios").header("Authorization", token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/progreso/me").header("Authorization", token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/practica/siguiente").param("asignatura", "CALCULO_DIFERENCIAL")
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorSoloEntraASusEndpoints() throws Exception {
        String token = bearer(admin);
        mockMvc.perform(get("/api/admin/usuarios").header("Authorization", token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/resumen").header("Authorization", token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/actividades").header("Authorization", token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/practica/siguiente").param("asignatura", "CALCULO_DIFERENCIAL")
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/progreso/me").header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    void lasRespuestasNoExponenDatosSensibles() throws Exception {
        MvcResult usuarios = mockMvc.perform(get("/api/admin/usuarios").header("Authorization", bearer(admin)))
                .andExpect(status().isOk()).andReturn();
        assertThat(usuarios.getResponse().getContentAsString())
                .doesNotContain("passwordHash").doesNotContain("\"password\"");

        MvcResult intento = mockMvc.perform(post("/api/diagnosticos/intentos")
                        .header("Authorization", bearer(estudiante)))
                .andExpect(status().isCreated()).andReturn();
        assertThat(intento.getResponse().getContentAsString()).doesNotContain("esCorrecta");

        MvcResult siguiente = mockMvc.perform(get("/api/practica/siguiente")
                        .param("asignatura", "CALCULO_DIFERENCIAL")
                        .header("Authorization", bearer(estudiante)))
                .andExpect(status().isOk()).andReturn();
        assertThat(siguiente.getResponse().getContentAsString())
                .doesNotContain("esCorrecta").doesNotContain("respuestaNumerica").doesNotContain("explicacion");

        MvcResult actividades = mockMvc.perform(get("/api/admin/actividades")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk()).andReturn();
        assertThat(actividades.getResponse().getContentAsString())
                .doesNotContain("respuestaNumerica").doesNotContain("explicacion").doesNotContain("opciones");
    }

    private Usuario guardar(String nombres, String apellidos, String correo, NombreRol rol) {
        Rol entidadRol = rolRepository.findByNombre(rol).orElseThrow();
        Usuario usuario = new Usuario(nombres, apellidos, correo, "$2a$10$hashDePrueba", entidadRol);
        if (rol == NombreRol.ESTUDIANTE) {
            usuario.setProgramaAcademico(ProgramaAcademico.INGENIERIA_DE_SOFTWARE.getNombre());
        }
        return usuarioRepository.saveAndFlush(usuario);
    }

    private String bearer(Usuario usuario) {
        return "Bearer " + jwtService.emitir(UsuarioAutenticado.desde(usuario)).valor();
    }
}
