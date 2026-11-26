package co.edu.usco.aprendizaje_usco.modulos.usuario.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class GrupoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private JwtService jwtService;

    private Usuario docente;
    private Usuario otroDocente;
    private Usuario estudiante;
    private Usuario administrador;
    private Long grupoId;

    @BeforeEach
    void prepararDatos() {
        docente = guardarUsuario("Docente", "Uno", "docente1@usco.edu.co", NombreRol.DOCENTE);
        otroDocente = guardarUsuario("Docente", "Dos", "docente2@usco.edu.co", NombreRol.DOCENTE);
        estudiante = guardarUsuario("Estudiante", "Uno", "u20201234567@usco.edu.co", NombreRol.ESTUDIANTE);
        administrador = guardarUsuario("Admin", "Uno", "admin@usco.edu.co", NombreRol.ADMIN);
        grupoId = grupoService.crear(docente.getId(),
                new GrupoRequest("GRUPO-UNO", "Grupo de prueba")).id();
    }

    @Test
    void solicitudesSinTokenDevuelven401() throws Exception {
        mockMvc.perform(get("/api/grupos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void docenteSoloListaSusGrupos() throws Exception {
        grupoService.crear(otroDocente.getId(), new GrupoRequest("GRUPO-DOS", "Otro grupo"));

        mockMvc.perform(get("/api/grupos").header("Authorization", bearer(docente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("GRUPO-UNO"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    void docenteAjenoRecibe403AlConsultarEstudiantes() throws Exception {
        mockMvc.perform(get("/api/grupos/{id}/estudiantes", grupoId)
                        .header("Authorization", bearer(otroDocente)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void docenteAjenoNoPuedeInscribirEnGrupoDeOtro() throws Exception {
        mockMvc.perform(post("/api/grupos/{id}/estudiantes", grupoId)
                        .header("Authorization", bearer(otroDocente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"u20201234567@usco.edu.co"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void estudianteNoPuedeGestionarGrupos() throws Exception {
        mockMvc.perform(get("/api/grupos").header("Authorization", bearer(estudiante)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void docenteResponsableInscribirYConsultarEstudiante() throws Exception {
        mockMvc.perform(post("/api/grupos/{id}/estudiantes", grupoId)
                        .header("Authorization", bearer(docente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"u20201234567@usco.edu.co"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(estudiante.getId()))
                .andExpect(jsonPath("$.correo").value(estudiante.getCorreo()));

        mockMvc.perform(get("/api/grupos/{id}/estudiantes", grupoId)
                        .header("Authorization", bearer(docente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].correo").value(estudiante.getCorreo()));
    }

    @Test
    void inscripcionDuplicadaDevuelve409() throws Exception {
        String cuerpo = """
                {"correo":"u20201234567@usco.edu.co"}
                """;
        mockMvc.perform(post("/api/grupos/{id}/estudiantes", grupoId)
                        .header("Authorization", bearer(docente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/grupos/{id}/estudiantes", grupoId)
                        .header("Authorization", bearer(docente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void administradorPuedeInscribirEnCualquierGrupo() throws Exception {
        mockMvc.perform(post("/api/grupos/{id}/estudiantes", grupoId)
                        .header("Authorization", bearer(administrador))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"u20201234567@usco.edu.co"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void soloPermiteInscribirEstudiantesYValidaElCorreo() throws Exception {
        mockMvc.perform(post("/api/grupos/{id}/estudiantes", grupoId)
                        .header("Authorization", bearer(docente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"docente2@usco.edu.co"}
                                """))
                .andExpect(status().isUnprocessableContent());

        mockMvc.perform(post("/api/grupos/{id}/estudiantes", grupoId)
                        .header("Authorization", bearer(docente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"no-es-un-correo"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void docenteCreaGrupoConSuIdentidadAutenticada() throws Exception {
        mockMvc.perform(post("/api/grupos")
                        .header("Authorization", bearer(docente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"GRUPO-NUEVO","nombre":"Nuevo grupo"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("GRUPO-NUEVO"))
                .andExpect(jsonPath("$.docenteCorreo").value(docente.getCorreo()));
    }

    @Test
    void codigoDeGrupoDuplicadoDevuelve409() throws Exception {
        mockMvc.perform(post("/api/grupos")
                        .header("Authorization", bearer(docente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"GRUPO-UNO","nombre":"Código duplicado"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void swaggerPublicaLasRutasDeGrupos() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/grupos'].post").exists())
                .andExpect(jsonPath("$.paths['/api/grupos/{grupoId}/estudiantes'].post").exists());
    }

    private Usuario guardarUsuario(String nombres, String apellidos, String correo, NombreRol nombreRol) {
        Rol rol = rolRepository.findByNombre(nombreRol).orElseThrow();
        return usuarioRepository.saveAndFlush(
                new Usuario(nombres, apellidos, correo, "$2a$10$hashDePrueba", rol));
    }

    private String bearer(Usuario usuario) {
        return "Bearer " + jwtService.emitir(UsuarioAutenticado.desde(usuario)).valor();
    }
}
