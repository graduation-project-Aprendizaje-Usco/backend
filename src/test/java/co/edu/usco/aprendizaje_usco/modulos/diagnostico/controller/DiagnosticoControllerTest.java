package co.edu.usco.aprendizaje_usco.modulos.diagnostico.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.usco.aprendizaje_usco.TestcontainersConfiguration;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.UmbralesV2;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.config.UmbralesDiagnostico;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import co.edu.usco.aprendizaje_usco.security.JwtService;
import co.edu.usco.aprendizaje_usco.security.UsuarioAutenticado;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bloques 4 y 5: la prueba diagnóstica de punta a punta (MockMvc + seguridad real +
 * PostgreSQL). Cada prueba se deshace al terminar (@Transactional).
 *
 * Para simular que se acabó el tiempo se "atrasa" iniciado_en con SQL (ver
 * {@link #atrasarInicio}), en vez de esperar 36 minutos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class DiagnosticoControllerTest {

    private static final String BASE = "/api/diagnosticos/intentos";

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

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UmbralesDiagnostico umbrales;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tokenAna;
    private String tokenBeto;
    private String tokenDocente;

    /** número de ítem → [itemId, id de la opción correcta, id de una opción incorrecta]. */
    private final Map<Integer, long[]> items = new LinkedHashMap<>();

    @BeforeEach
    void preparar() {
        tokenAna = token(crear("Ana", "u20261000001@usco.edu.co", NombreRol.ESTUDIANTE));
        tokenBeto = token(crear("Beto", "u20261000002@usco.edu.co", NombreRol.ESTUDIANTE));
        tokenDocente = token(crear("Diana", "diana.docente@usco.edu.co", NombreRol.DOCENTE));

        jdbc.query("""
                SELECT i.numero, i.id AS item_id,
                       max(o.id) FILTER (WHERE o.es_correcta)     AS correcta,
                       max(o.id) FILTER (WHERE NOT o.es_correcta) AS incorrecta
                FROM item i JOIN opcion o ON o.item_id = i.id
                JOIN prueba_diagnostica p ON p.id = i.prueba_id AND p.activa
                GROUP BY i.numero, i.id ORDER BY i.numero""",
                rs -> {
                    items.put(rs.getInt("numero"),
                            new long[] {rs.getLong("item_id"), rs.getLong("correcta"), rs.getLong("incorrecta")});
                });
    }

    // ------------------------------------------------------------ iniciar

    @Test
    void iniciarCreaElIntentoYUnSegundoInicioDevuelveElMismo() throws Exception {
        ResultActionsConId creado = iniciar(tokenAna);
        long id = creado
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("EN_CURSO"))
                .andExpect(jsonPath("$.totalItems").value(24))
                .andExpect(jsonPath("$.items.length()").value(24))
                .andExpect(jsonPath("$.items[0].numero").value(1))
                .andExpect(jsonPath("$.items[0].opciones.length()").value(4))
                .andExpect(jsonPath("$.items[0].dimension.codigo").value("ALGEBRAICA"))
                .andExpect(jsonPath("$.items[16].imagen").value("/img/diagnostico/item-17.png"))
                .andExpect(jsonPath("$.segundosRestantes", allOf(greaterThan(36 * 60 - 30), lessThanOrEqualTo(36 * 60))))
                .andExpect(jsonPath("$.totalRespondidas").value(0))
                .andExpect(jsonPath("$.respuestas.length()").value(0))
                .andExpect(jsonPath("$.resultado").doesNotExist())
                .andReturnId();
        creado.andExpect(header().string("Location", endsWith(BASE + "/" + id)));

        iniciar(tokenAna)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void lasPreguntasNoRevelanLaRespuestaCorrecta() throws Exception {
        iniciar(tokenAna)
                .andExpect(status().isCreated())
                // Ningún campo del JSON delata la clave (el ítem 24 sí dice "correcta" en su enunciado).
                .andExpect(content().string(not(containsString("\"esCorrecta\""))))
                .andExpect(content().string(not(containsString("\"correcta\""))))
                .andExpect(content().string(not(containsString("\"clave\""))))
                .andExpect(jsonPath("$.items[0].opciones[0].length()").value(3));
    }

    @Test
    void actualDevuelve204SinIntentoY200ConIntento() throws Exception {
        mockMvc.perform(get(BASE + "/actual").header("Authorization", tokenAna))
                .andExpect(status().isNoContent());

        long id = idDeIniciar(tokenAna);

        mockMvc.perform(get(BASE + "/actual").header("Authorization", tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    // ------------------------------------------------------------ responder

    @Test
    void responderGuardaYPermiteCambiarLaRespuesta() throws Exception {
        long id = idDeIniciar(tokenAna);
        long[] item1 = items.get(1);

        responder(tokenAna, id, item1[0], item1[2])
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId").value(item1[0]))
                .andExpect(jsonPath("$.opcionId").value(item1[2]));
        responder(tokenAna, id, item1[0], item1[1]).andExpect(status().isOk());

        mockMvc.perform(get(BASE + "/" + id).header("Authorization", tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRespondidas").value(1))
                .andExpect(jsonPath("$.respuestas.length()").value(1))
                .andExpect(jsonPath("$.respuestas[0].itemId").value(item1[0]))
                .andExpect(jsonPath("$.respuestas[0].opcionId").value(item1[1]))
                // Las 24 preguntas siguen llegando: el frontend las necesita para retomar la prueba.
                .andExpect(jsonPath("$.items.length()").value(24));
    }

    @Test
    void lasRespuestasVanAntesQueLasPreguntasEnElJson() throws Exception {
        long id = idDeIniciar(tokenAna);
        responder(tokenAna, id, items.get(1)[0], items.get(1)[1]);

        String cuerpo = mockMvc.perform(get(BASE + "/" + id).header("Authorization", tokenAna))
                .andReturn().getResponse().getContentAsString();

        assertThat(cuerpo.indexOf("\"respuestas\"")).isLessThan(cuerpo.indexOf("\"items\""));
    }

    // ------------------------------------------------------------ resultado

    @Test
    void elResultadoSoloExisteAlFinalizar() throws Exception {
        long id = idDeIniciar(tokenAna);
        mockMvc.perform(get(BASE + "/" + id + "/resultado").header("Authorization", tokenAna))
                .andExpect(status().isConflict());

        for (long[] item : items.values()) {
            responder(tokenAna, id, item[0], item[1]);
        }
        finalizar(tokenAna, id);

        mockMvc.perform(get(BASE + "/" + id + "/resultado").header("Authorization", tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puntaje").value(24))
                .andExpect(jsonPath("$.categoria").value("NIVEL_ADECUADO"))
                .andExpect(jsonPath("$.dimensiones.length()").value(3))
                .andExpect(jsonPath("$.dimensiones[0].nivel").value("ALTO"));
        mockMvc.perform(get(BASE + "/" + id + "/resultado").header("Authorization", tokenBeto))
                .andExpect(status().isForbidden());
    }

    @Test
    void elResultadoDeUnIntentoVencidoLoFinalizaPorTiempo() throws Exception {
        long id = idDeIniciar(tokenAna);
        atrasarInicio(id, 37 * 60);

        mockMvc.perform(get(BASE + "/" + id + "/resultado").header("Authorization", tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalizadoPorTiempo").value(true))
                .andExpect(jsonPath("$.categoria").value("ALTO_RIESGO"));
    }

    @Test
    void losUmbralesConfiguradosSonLosDelInstrumento() {
        assertThat(umbrales).isEqualTo(UmbralesV2.UMBRALES);
    }

    @Test
    void unaOpcionDeOtraPreguntaDa422() throws Exception {
        long id = idDeIniciar(tokenAna);

        responder(tokenAna, id, items.get(1)[0], items.get(2)[1])
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("La opción elegida no pertenece a esta pregunta"));
    }

    @Test
    void unaPreguntaQueNoEsDeLaPruebaDa404() throws Exception {
        long id = idDeIniciar(tokenAna);

        responder(tokenAna, id, 999_999, items.get(1)[1]).andExpect(status().isNotFound());
    }

    @Test
    void responderSinOpcionDa400() throws Exception {
        long id = idDeIniciar(tokenAna);

        mockMvc.perform(put(BASE + "/" + id + "/respuestas/" + items.get(1)[0])
                        .header("Authorization", tokenAna)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[0].campo").value("opcionId"));
    }

    // ------------------------------------------------------------ finalizar y puntaje

    @Test
    void todasCorrectasDa24YNivelAdecuado() throws Exception {
        long id = idDeIniciar(tokenAna);
        for (long[] item : items.values()) {
            responder(tokenAna, id, item[0], item[1]).andExpect(status().isOk());
        }

        finalizar(tokenAna, id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADO"))
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.segundosRestantes").value(0))
                .andExpect(jsonPath("$.resultado.puntaje").value(24))
                .andExpect(jsonPath("$.resultado.total").value(24))
                .andExpect(jsonPath("$.resultado.categoria").value("NIVEL_ADECUADO"))
                .andExpect(jsonPath("$.resultado.categoriaDescripcion").value("Nivel adecuado"))
                .andExpect(jsonPath("$.resultado.finalizadoPorTiempo").value(false))
                .andExpect(jsonPath("$.resultado.dimensiones.length()").value(3))
                .andExpect(jsonPath("$.resultado.dimensiones[0].codigo").value("ALGEBRAICA"))
                .andExpect(jsonPath("$.resultado.dimensiones[0].aciertos").value(8))
                .andExpect(jsonPath("$.resultado.dimensiones[0].porcentaje").value(100.0))
                .andExpect(jsonPath("$.resultado.dimensiones[0].nivel").value("ALTO"));
    }

    @Test
    void puntajeMixtoCalificaCadaCompetencia() throws Exception {
        long id = idDeIniciar(tokenAna);
        // Correctas: ítems 1-8 (algebraica) y 9-12 (lógica). Incorrectas: 13-16. Sin responder: 17-24.
        for (int numero = 1; numero <= 16; numero++) {
            long[] item = items.get(numero);
            responder(tokenAna, id, item[0], numero <= 12 ? item[1] : item[2]).andExpect(status().isOk());
        }

        finalizar(tokenAna, id)
                .andExpect(jsonPath("$.resultado.puntaje").value(12))
                .andExpect(jsonPath("$.resultado.categoria").value("RIESGO_MODERADO"))
                .andExpect(jsonPath("$.resultado.dimensiones[?(@.codigo == 'ALGEBRAICA')].nivel").value("ALTO"))
                .andExpect(jsonPath("$.resultado.dimensiones[?(@.codigo == 'LOGICA')].aciertos").value(4))
                .andExpect(jsonPath("$.resultado.dimensiones[?(@.codigo == 'LOGICA')].porcentaje").value(50.0))
                .andExpect(jsonPath("$.resultado.dimensiones[?(@.codigo == 'LOGICA')].nivel").value("MEDIO"))
                .andExpect(jsonPath("$.resultado.dimensiones[?(@.codigo == 'FUNCIONAL')].nivel").value("BAJO"));

        Integer filas = jdbc.queryForObject(
                "SELECT count(*) FROM resultado_dimension WHERE intento_id = ?", Integer.class, id);
        assertThat(filas).isEqualTo(3);
    }

    @Test
    void sinResponderNadaQuedaEnAltoRiesgo() throws Exception {
        long id = idDeIniciar(tokenAna);

        finalizar(tokenAna, id)
                .andExpect(jsonPath("$.resultado.puntaje").value(0))
                .andExpect(jsonPath("$.resultado.categoria").value("ALTO_RIESGO"));
    }

    @Test
    void finalizarEsIdempotenteYLuegoNoSePuedeResponder() throws Exception {
        long id = idDeIniciar(tokenAna);
        responder(tokenAna, id, items.get(1)[0], items.get(1)[1]);
        finalizar(tokenAna, id).andExpect(jsonPath("$.resultado.puntaje").value(1));

        finalizar(tokenAna, id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado.puntaje").value(1));
        responder(tokenAna, id, items.get(2)[0], items.get(2)[1])
                .andExpect(status().isConflict());
        // Volver a "iniciar" devuelve el intento finalizado: la prueba se presenta una vez.
        iniciar(tokenAna)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.estado").value("FINALIZADO"));
    }

    // ------------------------------------------------------------ tiempo límite

    @Test
    void alConsultarUnIntentoVencidoSeFinalizaPorTiempo() throws Exception {
        long id = idDeIniciar(tokenAna);
        responder(tokenAna, id, items.get(1)[0], items.get(1)[1]);
        atrasarInicio(id, 37 * 60);

        String cuerpo = mockMvc.perform(get(BASE + "/" + id).header("Authorization", tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADO"))
                .andExpect(jsonPath("$.resultado.finalizadoPorTiempo").value(true))
                .andExpect(jsonPath("$.resultado.puntaje").value(1))
                .andReturn().getResponse().getContentAsString();

        // La fecha de fin es el vencimiento, no el momento en que se consultó.
        String venceEn = JsonPath.read(cuerpo, "$.venceEn");
        String finalizadoEn = JsonPath.read(cuerpo, "$.resultado.finalizadoEn");
        assertThat(java.time.Instant.parse(finalizadoEn)).isEqualTo(java.time.Instant.parse(venceEn));
    }

    @Test
    void responderDespuesDelTiempoDa422YFinalizaElIntento() throws Exception {
        long id = idDeIniciar(tokenAna);
        atrasarInicio(id, 37 * 60);

        responder(tokenAna, id, items.get(1)[0], items.get(1)[1])
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Se acabó el tiempo de la prueba; la respuesta no se guardó"));

        mockMvc.perform(get(BASE + "/" + id).header("Authorization", tokenAna))
                .andExpect(jsonPath("$.estado").value("FINALIZADO"))
                .andExpect(jsonPath("$.resultado.finalizadoPorTiempo").value(true))
                .andExpect(jsonPath("$.respuestas.length()").value(0));
    }

    @Test
    void unaRespuestaQueLlegaDentroDeLaToleranciaSeAcepta() throws Exception {
        long id = idDeIniciar(tokenAna);
        atrasarInicio(id, 36 * 60 + 2); // 2 s después del vencimiento; la tolerancia es de 5 s

        responder(tokenAna, id, items.get(1)[0], items.get(1)[1]).andExpect(status().isOk());
    }

    // ------------------------------------------------------------ acceso

    @Test
    void otroEstudianteNoPuedeVerResponderNiFinalizarElIntento() throws Exception {
        long id = idDeIniciar(tokenAna);

        mockMvc.perform(get(BASE + "/" + id).header("Authorization", tokenBeto))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Este intento pertenece a otro estudiante"));
        responder(tokenBeto, id, items.get(1)[0], items.get(1)[1]).andExpect(status().isForbidden());
        finalizar(tokenBeto, id).andExpect(status().isForbidden());
    }

    @Test
    void unIntentoQueNoExisteDa404() throws Exception {
        mockMvc.perform(get(BASE + "/999999").header("Authorization", tokenAna))
                .andExpect(status().isNotFound());
    }

    @Test
    void unDocenteNoPuedePresentarLaPrueba() throws Exception {
        iniciar(tokenDocente).andExpect(status().isForbidden());
        mockMvc.perform(get(BASE + "/actual").header("Authorization", tokenDocente))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinTokenDa401() throws Exception {
        mockMvc.perform(post(BASE)).andExpect(status().isUnauthorized());
        mockMvc.perform(get(BASE + "/actual")).andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------ flujo completo (SCRUM-143)

    @Test
    void flujoCompletoDesdeElLoginHastaElResultado() throws Exception {
        // Token obtenido con el login real (correo + contraseña), no emitido a mano.
        usuarioRepository.save(new Usuario("Carla", "Prueba", "u20261000003@usco.edu.co",
                passwordEncoder.encode("Clave2026*"), rolRepository.findByNombre(NombreRol.ESTUDIANTE).orElseThrow()));
        String login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\": \"u20261000003@usco.edu.co\", \"password\": \"Clave2026*\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = "Bearer " + JsonPath.read(login, "$.token");

        long id = idDeIniciar(token);
        mockMvc.perform(get(BASE + "/actual").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        // 20 correctas (ítems 1 a 20) y 4 incorrectas (21 a 24).
        for (var item : items.entrySet()) {
            long opcion = item.getKey() <= 20 ? item.getValue()[1] : item.getValue()[2];
            responder(token, id, item.getValue()[0], opcion).andExpect(status().isOk());
        }
        mockMvc.perform(get(BASE + "/" + id).header("Authorization", token))
                .andExpect(jsonPath("$.totalRespondidas").value(24));

        finalizar(token, id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADO"));
        mockMvc.perform(get(BASE + "/" + id + "/resultado").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puntaje").value(20))
                .andExpect(jsonPath("$.total").value(24))
                .andExpect(jsonPath("$.categoria").value("NIVEL_ADECUADO"))
                .andExpect(jsonPath("$.finalizadoPorTiempo").value(false))
                .andExpect(jsonPath("$.dimensiones.length()").value(3));
    }

    // ------------------------------------------------------------ utilidades

    private Usuario crear(String nombre, String correo, NombreRol rol) {
        return usuarioRepository.save(new Usuario(nombre, "Prueba", correo, "x",
                rolRepository.findByNombre(rol).orElseThrow()));
    }

    private String token(Usuario usuario) {
        return "Bearer " + jwtService.emitir(UsuarioAutenticado.desde(usuario)).valor();
    }

    private ResultActionsConId iniciar(String token) throws Exception {
        return new ResultActionsConId(mockMvc.perform(post(BASE).header("Authorization", token)));
    }

    private long idDeIniciar(String token) throws Exception {
        return iniciar(token).andExpect(status().isCreated()).andReturnId();
    }

    private ResultActions responder(String token, long intentoId, long itemId, long opcionId) throws Exception {
        return mockMvc.perform(put(BASE + "/" + intentoId + "/respuestas/" + itemId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"opcionId\": " + opcionId + "}"));
    }

    private ResultActions finalizar(String token, long intentoId) throws Exception {
        return mockMvc.perform(post(BASE + "/" + intentoId + "/finalizar").header("Authorization", token));
    }

    /**
     * Simula que el intento empezó hace {@code segundos} segundos. flush() antes y
     * clear() después: todas las peticiones de la prueba comparten la transacción,
     * así que Hibernate tiene el intento en memoria y no vería el cambio hecho con SQL.
     */
    private void atrasarInicio(long intentoId, long segundos) {
        entityManager.flush();
        jdbc.update("UPDATE intento_diagnostico SET iniciado_en = clock_timestamp() - make_interval(secs => ?) WHERE id = ?",
                (double) segundos, intentoId);
        entityManager.clear();
    }

    /** ResultActions con un atajo para leer el "id" del cuerpo. */
    private record ResultActionsConId(ResultActions acciones) {

        ResultActionsConId andExpect(org.springframework.test.web.servlet.ResultMatcher matcher) throws Exception {
            acciones.andExpect(matcher);
            return this;
        }

        long andReturnId() throws Exception {
            Number id = JsonPath.read(acciones.andReturn().getResponse().getContentAsString(), "$.id");
            return id.longValue();
        }

    }

}
