package co.edu.usco.aprendizaje_usco.modulos.progreso.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class ProgresoControllerTest {

    private static final String ENDPOINT = "/api/progreso/me";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private JdbcTemplate jdbc;

    private Usuario estudiante;
    private Usuario otroEstudiante;
    private String tokenEstudiante;
    private String tokenDocente;

    @BeforeEach
    void preparar() {
        estudiante = crearUsuario("Carlos", "u20268888001@usco.edu.co", NombreRol.ESTUDIANTE);
        otroEstudiante = crearUsuario("Beatriz", "u20268888002@usco.edu.co", NombreRol.ESTUDIANTE);
        tokenEstudiante = token(estudiante);
        tokenDocente = token(crearUsuario("Profesor", "u20268888003@usco.edu.co", NombreRol.DOCENTE));
    }

    @Test
    @DisplayName("Petición sin token responde 401 Unauthorized")
    void sinTokenResponde401() throws Exception {
        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Rol DOCENTE responde 403 Forbidden")
    void rolDocenteResponde403() throws Exception {
        mockMvc.perform(get(ENDPOINT).header("Authorization", tokenDocente))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Estudiante sin actividad recibe indicadores en cero y SIN_EVALUAR")
    void estudianteSinActividad() throws Exception {
        mockMvc.perform(get(ENDPOINT).header("Authorization", tokenEstudiante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIntentos").value(0))
                .andExpect(jsonPath("$.ejerciciosCompletados").value(0))
                .andExpect(jsonPath("$.nivelGeneral").value("SIN_EVALUAR"))
                .andExpect(jsonPath("$.porcentajeGeneral").doesNotExist())
                .andExpect(jsonPath("$.evaluacionParcial").value(false))
                .andExpect(jsonPath("$.dimensiones.length()").value(3))
                .andExpect(jsonPath("$.dimensiones[0].sinActividad").value(true))
                .andExpect(jsonPath("$.dimensiones[0].intentosEvaluados").value(0))
                .andExpect(jsonPath("$.dimensiones[0].intentosCorrectos").value(0))
                .andExpect(jsonPath("$.dimensiones[0].porcentajeAciertos").doesNotExist())
                .andExpect(jsonPath("$.dimensiones[0].nivel").doesNotExist())
                .andExpect(jsonPath("$.dimensiones[0].estadoVariacion").value("DIAGNOSTICO_PENDIENTE"))
                .andExpect(jsonPath("$.tendenciaSemanal").isEmpty());
    }

    @Test
    @DisplayName("Parámetro usuarioId en URL o query param es completamente ignorado")
    void ignoraParametroUsuarioId() throws Exception {
        // Insertar 1 intento para otroEstudiante
        insertarIntento(otroEstudiante.getId(), idEjercicio("DIF-ALG-1-01"), 1L, (short) 1, true, Instant.now());

        // Carlos consulta /me?usuarioId=otroEstudiante.getId(), debe devolver 0 intentos (los de Carlos)
        mockMvc.perform(get(ENDPOINT)
                        .param("usuarioId", String.valueOf(otroEstudiante.getId()))
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIntentos").value(0));
    }

    @Test
    @DisplayName("Cálculo completo con diagnóstico y práctica: variaciones, ejercicios completados y tendencia semanal")
    void calculoCompletoConDiagnosticoYPractica() throws Exception {
        Long usuarioId = estudiante.getId();
        Long compAlg = idCompetencia("ALGEBRAICA");
        Long compLog = idCompetencia("LOGICA");
        Long compFun = idCompetencia("FUNCIONAL");

        // 1. Crear diagnóstico finalizado para el estudiante
        long pruebaId = jdbc.queryForObject("SELECT id FROM prueba_diagnostica LIMIT 1", Long.class);
        long intentoDiagId = jdbc.queryForObject("""
                INSERT INTO intento_diagnostico (usuario_id, prueba_id, estado, iniciado_en, finalizado_en, puntaje_total, categoria)
                VALUES (?, ?, 'FINALIZADO', now() - interval '10 days', now() - interval '10 days', 18, 'NIVEL_ADECUADO')
                RETURNING id""", Long.class, usuarioId, pruebaId);

        // Diagnóstico previo: Álgebra 50%, Lógica 75%, Funcional 60%
        jdbc.update("""
                INSERT INTO resultado_dimension (intento_id, competencia_id, aciertos, total, porcentaje, nivel)
                VALUES (?, ?, 4, 8, 50.00, 'MEDIO'),
                       (?, ?, 6, 8, 75.00, 'ALTO'),
                       (?, ?, 5, 8, 62.50, 'MEDIO')""",
                intentoDiagId, compAlg,
                intentoDiagId, compLog,
                intentoDiagId, compFun);

        // 2. Práctica:
        // Ejercicio 1 (Álgebra): Intento 1 incorrecto, Intento 2 correcto (cuenta como 1 ejercicio completado)
        long ej1 = idEjercicio("DIF-ALG-1-01");
        Instant haceTresSemanas = Instant.now().minus(21, ChronoUnit.DAYS);
        insertarIntento(usuarioId, ej1, compAlg, (short) 1, false, haceTresSemanas);
        insertarIntento(usuarioId, ej1, compAlg, (short) 2, true, haceTresSemanas.plus(5, ChronoUnit.MINUTES));

        // Ejercicio 2 (Álgebra): Intento 1 correcto (cuenta como 1 ejercicio completado)
        long ej2 = idEjercicio("DIF-ALG-1-02");
        insertarIntento(usuarioId, ej2, compAlg, (short) 1, true, haceTresSemanas.plus(10, ChronoUnit.MINUTES));

        // Ejercicio 3 (Álgebra): Intento 1 incorrecto, Intento 2 incorrecto (NO cuenta como completado)
        long ej3 = idEjercicio("DIF-ALG-1-03");
        insertarIntento(usuarioId, ej3, compAlg, (short) 1, false, haceTresSemanas.plus(15, ChronoUnit.MINUTES));
        insertarIntento(usuarioId, ej3, compAlg, (short) 2, false, haceTresSemanas.plus(20, ChronoUnit.MINUTES));

        // Total intentos en Álgebra: 5 intentos, 2 correctos -> 40.00% (BASICO)
        // Variación Álgebra: 40.00 - 50.00 = -10.00 (RETROCESO)

        // Ejercicio 4 (Lógica): 3 intentos correctos (Semana actual)
        long ejLog1 = idEjercicio("DIF-LOG-1-01");
        long ejLog2 = idEjercicio("DIF-LOG-1-02");
        long ejLog3 = idEjercicio("DIF-LOG-1-03");
        Instant hoy = Instant.now();
        insertarIntento(usuarioId, ejLog1, compLog, (short) 1, true, hoy);
        insertarIntento(usuarioId, ejLog2, compLog, (short) 1, true, hoy);
        insertarIntento(usuarioId, ejLog3, compLog, (short) 1, true, hoy);

        // Total intentos en Lógica: 3 intentos, 3 correctos -> 100.00% (AVANZADO)
        // Variación Lógica: 100.00 - 75.00 = +25.00 (MEJORA)

        // Funcional: 0 intentos (sinActividad: true, PRACTICA_PENDIENTE)

        // Totales globales:
        // Total intentos: 5 + 3 = 8
        // Ejercicios completados: ej1, ej2, ejLog1, ejLog2, ejLog3 = 5 únicos
        // Nivel general: Álgebra (40.00%) y Lógica (100.00%) tienen >= 3 intentos.
        // Macro-promedio: (40.00 + 100.00) / 2 = 70.00% (INTERMEDIO).
        // evaluacionParcial: true (Funcional no tiene intentos).

        mockMvc.perform(get(ENDPOINT).header("Authorization", tokenEstudiante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIntentos").value(8))
                .andExpect(jsonPath("$.ejerciciosCompletados").value(5))
                .andExpect(jsonPath("$.nivelGeneral").value("INTERMEDIO"))
                .andExpect(jsonPath("$.porcentajeGeneral").value(70.0))
                .andExpect(jsonPath("$.evaluacionParcial").value(true))
                // Álgebra:
                .andExpect(jsonPath("$.dimensiones[?(@.codigo=='ALGEBRAICA')].intentosEvaluados").value(5))
                .andExpect(jsonPath("$.dimensiones[?(@.codigo=='ALGEBRAICA')].intentosCorrectos").value(2))
                .andExpect(jsonPath("$.dimensiones[?(@.codigo=='ALGEBRAICA')].porcentajeAciertos").value(40.0))
                .andExpect(jsonPath("$.dimensiones[?(@.codigo=='ALGEBRAICA')].nivel").value("BASICO"))
                .andExpect(jsonPath("$.dimensiones[?(@.codigo=='ALGEBRAICA')].variacionPuntosPorcentuales").value(-10.0))
                .andExpect(jsonPath("$.dimensiones[?(@.codigo=='ALGEBRAICA')].estadoVariacion").value("RETROCESO"))
                // Lógica:
                .andExpect(jsonPath("$.dimensiones[?(@.codigo=='LOGICA')].intentosEvaluados").value(3))
                .andExpect(jsonPath("$.dimensiones[?(@.codigo=='LOGICA')].intentosCorrectos").value(3))
                .andExpect(jsonPath("$.dimensiones[?(@.codigo=='LOGICA')].porcentajeAciertos").value(100.0))
                .andExpect(jsonPath("$.dimensiones[?(@.codigo=='LOGICA')].nivel").value("AVANZADO"))
                .andExpect(jsonPath("$.dimensiones[?(@.codigo=='LOGICA')].variacionPuntosPorcentuales").value(25.0))
                .andExpect(jsonPath("$.dimensiones[?(@.codigo=='LOGICA')].estadoVariacion").value("MEJORA"))
                // Funcional:
                .andExpect(jsonPath("$.dimensiones[?(@.codigo=='FUNCIONAL')].sinActividad").value(true))
                .andExpect(jsonPath("$.dimensiones[?(@.codigo=='FUNCIONAL')].estadoVariacion").value("PRACTICA_PENDIENTE"))
                // Tendencia semanal (2 semanas distintas con actividad):
                .andExpect(jsonPath("$.tendenciaSemanal.length()").value(2));
    }

    private void insertarIntento(Long usuarioId, Long ejercicioId, Long competenciaId,
                                 short numeroIntento, boolean esCorrecta, Instant creadoEn) {
        long opcionId = jdbc.queryForObject(
                "SELECT id FROM opcion_ejercicio WHERE ejercicio_id = ? LIMIT 1", Long.class, ejercicioId);
        jdbc.update("""
                INSERT INTO intento_practica (
                    usuario_id, ejercicio_id, competencia_id, nivel_ejercicio,
                    numero_intento, opcion_ejercicio_id, es_correcta, tiempo_seg,
                    pista_usada, clave_idempotencia, creado_en
                ) VALUES (?, ?, ?, 1, ?, ?, ?, 15, false, ?, ?)""",
                usuarioId, ejercicioId, competenciaId, numeroIntento, opcionId,
                esCorrecta, UUID.randomUUID(), Timestamp.from(creadoEn));
    }

    private long idEjercicio(String codigo) {
        return jdbc.queryForObject("SELECT id FROM ejercicio WHERE codigo = ?", Long.class, codigo);
    }

    private long idCompetencia(String codigo) {
        return jdbc.queryForObject("SELECT id FROM competencia WHERE codigo = ?", Long.class, codigo);
    }

    private Usuario crearUsuario(String nombre, String correo, NombreRol nombreRol) {
        Rol rol = rolRepository.findByNombre(nombreRol).orElseThrow();
        return usuarioRepository.saveAndFlush(new Usuario(
                nombre, "Prueba", correo, "hash", rol));
    }

    private String token(Usuario usuario) {
        return "Bearer " + jwtService.emitir(UsuarioAutenticado.desde(usuario)).valor();
    }

}
