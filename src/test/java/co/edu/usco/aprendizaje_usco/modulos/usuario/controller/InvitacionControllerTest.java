package co.edu.usco.aprendizaje_usco.modulos.usuario.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.GrupoRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.InvitacionRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.RegistroRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.ProgramaAcademico;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Rol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.service.GrupoService;
import co.edu.usco.aprendizaje_usco.modulos.usuario.service.InvitacionService;
import co.edu.usco.aprendizaje_usco.security.JwtService;
import co.edu.usco.aprendizaje_usco.security.UsuarioAutenticado;
import jakarta.persistence.EntityManager;
import java.util.List;
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

@SpringBootTest(properties = "app.registro.invitacion-obligatoria=true")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class InvitacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private InvitacionService invitacionService;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    private Usuario docente;
    private Usuario otroDocente;
    private Long grupoId;

    @BeforeEach
    void prepararDatos() {
        docente = guardar("Docente", "Invitaciones", "docente.inv@usco.edu.co", NombreRol.DOCENTE);
        otroDocente = guardar("Otro", "Docente", "otro.inv@usco.edu.co", NombreRol.DOCENTE);
        grupoId = grupoService.crear(docente.getId(), new GrupoRequest("INV-2026", "Grupo invitaciones")).id();
    }

    @Test
    void docenteCargaListadoYVeEstadoDeInvitaciones() throws Exception {
        mockMvc.perform(json(post("/api/grupos/{id}/invitaciones", grupoId), """
                        {"estudiantes":[
                          {"correo":"u20261111111@usco.edu.co","nombres":"Ana","apellidos":"Uno"},
                          {"correo":"u20262222222@usco.edu.co","nombres":"Beto","apellidos":"Dos"}
                        ]}""").header("Authorization", bearer(docente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].estado").value("INVITADO"))
                .andExpect(jsonPath("$[0].correo").exists());

        mockMvc.perform(get("/api/grupos/{id}/invitaciones", grupoId)
                        .header("Authorization", bearer(docente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/grupos/{id}/invitaciones", grupoId)
                        .header("Authorization", bearer(otroDocente)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unaInvitacionVigenteRegistraAlEstudianteYLoInscribeEnElGrupo() throws Exception {
        InvitacionService.InvitacionCreada creada = invitacionService.crearLote(grupoId, docente.getId(), false,
                List.of(new InvitacionRequest("u20263333333@usco.edu.co", "Carla", "Tres"))).get(0);

        mockMvc.perform(get("/api/auth/invitacion").param("token", creada.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("u20263333333@usco.edu.co"))
                .andExpect(jsonPath("$.grupo").value("Grupo invitaciones"));

        mockMvc.perform(json(post("/api/auth/registro"), """
                        {"nombres":"Carla","apellidos":"Tres","correo":"u20263333333@usco.edu.co",
                         "password":"Clave2026*","programaAcademico":"%s","tokenInvitacion":"%s"}"""
                        .formatted(ProgramaAcademico.INGENIERIA_DE_SOFTWARE.getNombre(), creada.token())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("ESTUDIANTE"));

        Long estudianteId = usuarioRepository.findByCorreo("u20263333333@usco.edu.co").orElseThrow().getId();
        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM grupo_estudiante WHERE grupo_id = ? AND estudiante_id = ?""",
                Integer.class, grupoId, estudianteId)).isEqualTo(1);
        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM invitacion WHERE grupo_id = ?
                AND correo = 'u20263333333@usco.edu.co' AND usada_en IS NOT NULL""",
                Integer.class, grupoId)).isEqualTo(1);

        mockMvc.perform(get("/api/auth/invitacion").param("token", creada.token()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void registroSinInvitacionCorreoAjenooVencidaFalla() throws Exception {
        mockMvc.perform(json(post("/api/auth/registro"), """
                        {"nombres":"Sin","apellidos":"Invitación","correo":"u20264444444@usco.edu.co",
                         "password":"Clave2026*","programaAcademico":"%s"}"""
                        .formatted(ProgramaAcademico.INGENIERIA_DE_SOFTWARE.getNombre())))
                .andExpect(status().isUnprocessableEntity());

        InvitacionService.InvitacionCreada creada = invitacionService.crearLote(grupoId, docente.getId(), false,
                List.of(new InvitacionRequest("u20265555555@usco.edu.co", "Dora", "Cinco"))).get(0);

        mockMvc.perform(json(post("/api/auth/registro"), """
                        {"nombres":"Dora","apellidos":"Cinco","correo":"u20266666666@usco.edu.co",
                         "password":"Clave2026*","programaAcademico":"%s","tokenInvitacion":"%s"}"""
                        .formatted(ProgramaAcademico.INGENIERIA_DE_SOFTWARE.getNombre(), creada.token())))
                .andExpect(status().isUnprocessableEntity());

        jdbc.update("UPDATE invitacion SET vence_en = now() - interval '1 day' WHERE token_hash IS NOT NULL AND grupo_id = ?",
                grupoId);
        entityManager.clear();
        mockMvc.perform(get("/api/auth/invitacion").param("token", creada.token()))
                .andExpect(status().isUnprocessableEntity());
    }

    private Usuario guardar(String nombres, String apellidos, String correo, NombreRol rol) {
        Rol entidadRol = rolRepository.findByNombre(rol).orElseThrow();
        Usuario usuario = new Usuario(nombres, apellidos, correo, "$2a$10$hashDePrueba", entidadRol);
        return usuarioRepository.saveAndFlush(usuario);
    }

    private String bearer(Usuario usuario) {
        return "Bearer " + jwtService.emitir(UsuarioAutenticado.desde(usuario)).valor();
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder json(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder peticion, String cuerpo) {
        return peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
    }
}
