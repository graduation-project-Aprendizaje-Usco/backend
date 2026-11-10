package co.edu.usco.aprendizaje_usco.modulos.contenido.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/** SCRUM-39/40: CRUD de contenidos y ejercicios con seguridad y PostgreSQL reales. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class ActividadesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JdbcTemplate jdbc;

    private Usuario estudiante;
    private String tokenDocente;
    private String tokenOtroDocente;
    private String tokenEstudiante;
    private String tokenAdmin;

    @BeforeEach
    void preparar() {
        estudiante = crear("Ana", "Alvarez", "u20261200001@usco.edu.co", NombreRol.ESTUDIANTE);
        Usuario docente = crear("Diana", "Docente", "diana.actividades@usco.edu.co", NombreRol.DOCENTE);
        Usuario otro = crear("Omar", "Otro", "omar.actividades@usco.edu.co", NombreRol.DOCENTE);
        Usuario admin = crear("Admin", "Sistema", "admin.actividades@usco.edu.co", NombreRol.ADMIN);
        tokenDocente = token(docente);
        tokenOtroDocente = token(otro);
        tokenEstudiante = token(estudiante);
        tokenAdmin = token(admin);
    }

    @Test
    void creaYListaContenidosPropios() throws Exception {
        Long contenido = crearContenido("Contenido de prueba");

        mockMvc.perform(get("/api/contenidos").header("Authorization", tokenDocente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(contenido))
                .andExpect(jsonPath("$[0].titulo").value("Contenido de prueba"))
                .andExpect(jsonPath("$[0].activo").value(true))
                .andExpect(jsonPath("$[0].totalEjercicios").value(0));

        mockMvc.perform(get("/api/contenidos").header("Authorization", tokenOtroDocente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void editaElTituloDeUnContenidoPropio() throws Exception {
        Long contenido = crearContenido("Contenido original");

        mockMvc.perform(put("/api/contenidos/" + contenido)
                        .header("Authorization", tokenDocente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contenidoRequest("Contenido editado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Contenido editado"));
    }

    @Test
    void creaEjercicioNumerico() throws Exception {
        Long contenido = crearContenido("Contenido numérico");

        mockMvc.perform(post("/api/ejercicios")
                        .header("Authorization", tokenDocente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contenidoId":%d,"codigo":"DOC-NUM-1","nivel":2,"tipo":"NUMERICA",
                                 "enunciado":"Calcula 2 + 2","respuestaNumerica":4,"tolerancia":0.01,
                                 "pista":"Suma","explicacion":"Cuatro"}
                                """.formatted(contenido)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("NUMERICA"))
                .andExpect(jsonPath("$.opciones").isEmpty())
                .andExpect(jsonPath("$.respuestaNumerica").value(4.0));
    }

    @Test
    void creaEjercicioDeOpcionMultipleYLoDevuelveEnLaLista() throws Exception {
        Long contenido = crearContenido("Contenido de prueba");
        MvcResult resultado = mockMvc.perform(post("/api/ejercicios")
                        .header("Authorization", tokenDocente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ejercicioMultiple(contenido, "DOC-TEST-1", "¿Cuánto es 2 + 2?")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("DOC-TEST-1"))
                .andExpect(jsonPath("$.opciones.length()").value(4))
                .andExpect(jsonPath("$.opciones[0].correcta").value(true))
                .andReturn();

        Long id = numero(JsonPath.read(resultado.getResponse().getContentAsString(), "$.id"));
        mockMvc.perform(get("/api/ejercicios").header("Authorization", tokenDocente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].contenidoId").value(contenido));
    }

    @Test
    void validaOpcionesYRespuestaNumerica() throws Exception {
        Long contenido = crearContenido("Contenido de prueba");

        mockMvc.perform(post("/api/ejercicios")
                        .header("Authorization", tokenDocente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contenidoId":%d,"nivel":1,"tipo":"OPCION_MULTIPLE",
                                 "enunciado":"Prueba","opciones":[{"letra":"A","texto":"a","correcta":true}],
                                 "pista":"p","explicacion":"e"}
                                """.formatted(contenido)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/ejercicios")
                        .header("Authorization", tokenDocente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contenidoId":%d,"nivel":1,"tipo":"NUMERICA",
                                 "enunciado":"Prueba","opciones":[{"letra":"A","texto":"a","correcta":true}],
                                 "respuestaNumerica":2,"tolerancia":0.01,"pista":"p","explicacion":"e"}
                                """.formatted(contenido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazaCodigoDuplicado() throws Exception {
        Long contenido = crearContenido("Contenido de prueba");
        mockMvc.perform(post("/api/ejercicios")
                        .header("Authorization", tokenDocente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ejercicioMultiple(contenido, "DOC-DUP", "Primero")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ejercicios")
                        .header("Authorization", tokenDocente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ejercicioMultiple(contenido, "DOC-DUP", "Segundo")))
                .andExpect(status().isConflict());
    }

    @Test
    void otroDocenteYLosDemasRolesNoPuedenGestionar() throws Exception {
        Long contenido = crearContenido("Contenido de prueba");

        mockMvc.perform(put("/api/contenidos/" + contenido)
                        .header("Authorization", tokenOtroDocente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contenidoRequest("Contenido ajeno")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/ejercicios").header("Authorization", tokenEstudiante))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/contenidos").header("Authorization", tokenAdmin))
                .andExpect(status().isForbidden());
    }

    @Test
    void eliminaEjercicioSinIntentos() throws Exception {
        Long contenido = crearContenido("Contenido de prueba");
        Long ejercicio = crearEjercicio(contenido, "DOC-DEL-1");

        mockMvc.perform(delete("/api/ejercicios/" + ejercicio).header("Authorization", tokenDocente))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/ejercicios").header("Authorization", tokenDocente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void noEliminaContenidoConEjerciciosYLoPuedeDesactivar() throws Exception {
        Long contenido = crearContenido("Contenido de prueba");
        crearEjercicio(contenido, "DOC-DEL-2");

        mockMvc.perform(delete("/api/contenidos/" + contenido).header("Authorization", tokenDocente))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/api/contenidos/" + contenido + "/estado")
                        .header("Authorization", tokenDocente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    void ejercicioConIntentosNoSeEliminaPeroSeDesactiva() throws Exception {
        Long contenido = crearContenido("Contenido de prueba");
        Long ejercicio = crearEjercicio(contenido, "DOC-INT-1");
        insertarIntento(ejercicio);

        mockMvc.perform(delete("/api/ejercicios/" + ejercicio).header("Authorization", tokenDocente))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("desactívalo")));

        mockMvc.perform(patch("/api/ejercicios/" + ejercicio + "/estado")
                        .header("Authorization", tokenDocente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    void conIntentosSoloPermiteCambiarCamposNoClave() throws Exception {
        Long contenido = crearContenido("Contenido de prueba");
        Long ejercicio = crearEjercicio(contenido, "DOC-EDIT-1");
        insertarIntento(ejercicio);

        mockMvc.perform(put("/api/ejercicios/" + ejercicio)
                        .header("Authorization", tokenDocente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ejercicioMultiple(contenido, "DOC-EDIT-1", "Enunciado actualizado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enunciado").value("Enunciado actualizado"));

        String opcionCambiada = ejercicioMultiple(contenido, "DOC-EDIT-1", "Enunciado actualizado")
                .replace("\"texto\":\"Opcion A\"", "\"texto\":\"Opcion A cambiada\"");
        mockMvc.perform(put("/api/ejercicios/" + ejercicio)
                        .header("Authorization", tokenDocente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(opcionCambiada))
                .andExpect(status().isConflict());
    }

    @Test
    void elMotorSoloUsaEjerciciosActivos() throws Exception {
        jdbc.update("UPDATE ejercicio SET activo = false WHERE creado_por IS NULL");
        Long contenido = crearContenido("Contenido de práctica");
        Long ejercicio = crearEjercicio(contenido, "DOC-MOTOR-1");

        mockMvc.perform(get("/api/practica/siguiente")
                        .param("asignatura", "CALCULO_DIFERENCIAL")
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ejercicio))
                .andExpect(jsonPath("$.codigo").value("DOC-MOTOR-1"));

        mockMvc.perform(patch("/api/ejercicios/" + ejercicio + "/estado")
                        .header("Authorization", tokenDocente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":false}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/practica/siguiente")
                        .param("asignatura", "CALCULO_DIFERENCIAL")
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isNotFound());
    }

    private Long crearContenido(String titulo) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/contenidos")
                        .header("Authorization", tokenDocente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contenidoRequest(titulo)))
                .andExpect(status().isCreated())
                .andReturn();
        return numero(JsonPath.read(resultado.getResponse().getContentAsString(), "$.id"));
    }

    private Long crearEjercicio(Long contenidoId, String codigo) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/ejercicios")
                        .header("Authorization", tokenDocente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ejercicioMultiple(contenidoId, codigo, "Enunciado de prueba")))
                .andExpect(status().isCreated())
                .andReturn();
        return numero(JsonPath.read(resultado.getResponse().getContentAsString(), "$.id"));
    }

    private void insertarIntento(Long ejercicioId) {
        jdbc.update("""
                INSERT INTO intento_practica
                    (usuario_id, ejercicio_id, competencia_id, nivel_ejercicio, numero_intento,
                     opcion_ejercicio_id, es_correcta, tiempo_seg, clave_idempotencia)
                SELECT ?, e.id, e.competencia_id, e.nivel, 1, o.id, TRUE, 10, ?
                FROM ejercicio e
                JOIN opcion_ejercicio o ON o.ejercicio_id = e.id AND o.es_correcta
                WHERE e.id = ?""",
                estudiante.getId(), UUID.randomUUID(), ejercicioId);
    }

    private static String contenidoRequest(String titulo) {
        return """
                {"competencia":"ALGEBRAICA","asignatura":"CALCULO_DIFERENCIAL",
                 "tema":"Tema de prueba","titulo":"%s","descripcion":"Descripción"}
                """.formatted(titulo);
    }

    private static String ejercicioMultiple(Long contenidoId, String codigo, String enunciado) {
        return """
                {"contenidoId":%d,"codigo":"%s","nivel":1,"tipo":"OPCION_MULTIPLE",
                 "enunciado":"%s","opciones":[
                   {"letra":"A","texto":"Opcion A","correcta":true},
                   {"letra":"B","texto":"Opcion B","correcta":false},
                   {"letra":"C","texto":"Opcion C","correcta":false},
                   {"letra":"D","texto":"Opcion D","correcta":false}],
                 "pista":"Pista","explicacion":"Explicacion","errorComun":"Error"}
                """.formatted(contenidoId, codigo, enunciado);
    }

    private static Long numero(Object valor) {
        return ((Number) valor).longValue();
    }

    private Usuario crear(String nombres, String apellidos, String correo, NombreRol nombreRol) {
        Rol rol = rolRepository.findByNombre(nombreRol).orElseThrow();
        return usuarioRepository.saveAndFlush(new Usuario(nombres, apellidos, correo, "hash", rol));
    }

    private String token(Usuario usuario) {
        return "Bearer " + jwtService.emitir(UsuarioAutenticado.desde(usuario)).valor();
    }
}
