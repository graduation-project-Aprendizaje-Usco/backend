package co.edu.usco.aprendizaje_usco.modulos.contenido.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Competencia;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Contenido;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.CompetenciaRepository;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.ContenidoRepository;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.EjercicioRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.ProgramaAcademico;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Rol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import co.edu.usco.aprendizaje_usco.security.JwtService;
import co.edu.usco.aprendizaje_usco.security.UsuarioAutenticado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class AdminActividadesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CompetenciaRepository competenciaRepository;

    @Autowired
    private ContenidoRepository contenidoRepository;

    @Autowired
    private EjercicioRepository ejercicioRepository;

    @Autowired
    private JwtService jwtService;

    private Usuario admin;
    private Usuario docente;
    private Usuario estudiante;

    @BeforeEach
    void prepararDatos() {
        admin = guardar("Ada", "Admin", "ada.actividades@usco.edu.co", NombreRol.ADMIN);
        docente = guardar("Diego", "Docente", "diego.actividades@usco.edu.co", NombreRol.DOCENTE);
        estudiante = guardar("Eva", "Estudiante", "u20260000001@usco.edu.co", NombreRol.ESTUDIANTE);
    }

    @Test
    void sinTokenResponde401YLosDemasRoles403() throws Exception {
        mockMvc.perform(get("/api/admin/actividades")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/actividades").header("Authorization", bearer(docente)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/actividades").header("Authorization", bearer(estudiante)))
                .andExpect(status().isForbidden());
    }

    @Test
    void devuelveTodosLosContenidosYEjerciciosSinClaves() throws Exception {
        Competencia competencia = competenciaRepository.findByCodigo(Competencia.ALGEBRAICA).orElseThrow();
        contenidoRepository.saveAndFlush(new Contenido(
                competencia, "CALCULO_DIFERENCIAL", "Tema admin", "Contenido del docente",
                "Creado para la prueba del panel", docente));

        mockMvc.perform(get("/api/admin/actividades").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenidos.length()").value((int) contenidoRepository.count()))
                .andExpect(jsonPath("$.ejercicios.length()").value((int) ejercicioRepository.count()))
                .andExpect(jsonPath("$.contenidos[?(@.creadoPorCorreo == 'diego.actividades@usco.edu.co')]").exists())
                .andExpect(jsonPath("$.contenidos[?(@.titulo == 'Contenido del docente')]").exists())
                .andExpect(jsonPath("$.ejercicios[0].codigo").exists())
                .andExpect(jsonPath("$.ejercicios[0].contenidoTitulo").exists())
                .andExpect(jsonPath("$.ejercicios[0].competenciaCodigo").exists())
                .andExpect(jsonPath("$.ejercicios[0].nivel").exists())
                .andExpect(jsonPath("$.ejercicios[0].tipo").exists())
                .andExpect(jsonPath("$.ejercicios[0].opciones").doesNotExist())
                .andExpect(jsonPath("$.ejercicios[0].respuestaNumerica").doesNotExist())
                .andExpect(jsonPath("$.ejercicios[0].pista").doesNotExist())
                .andExpect(jsonPath("$.ejercicios[0].explicacion").doesNotExist());
    }

    private Usuario guardar(String nombres, String apellidos, String correo, NombreRol nombreRol) {
        Rol rol = rolRepository.findByNombre(nombreRol).orElseThrow();
        Usuario usuario = new Usuario(nombres, apellidos, correo, "$2a$10$hashDePrueba", rol);
        if (nombreRol == NombreRol.ESTUDIANTE) {
            usuario.setProgramaAcademico(ProgramaAcademico.INGENIERIA_DE_SOFTWARE.getNombre());
        }
        return usuarioRepository.saveAndFlush(usuario);
    }

    private String bearer(Usuario usuario) {
        return "Bearer " + jwtService.emitir(UsuarioAutenticado.desde(usuario)).valor();
    }
}
