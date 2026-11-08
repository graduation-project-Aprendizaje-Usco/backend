package co.edu.usco.aprendizaje_usco.modulos.progreso.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Competencia;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.CompetenciaRepository;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.EstadoIntento;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.IntentoDiagnostico;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.ResultadoDimension;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.repository.IntentoDiagnosticoRepository;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.repository.ResultadoDimensionRepository;
import co.edu.usco.aprendizaje_usco.modulos.practica.repository.IntentoPracticaRepository;
import co.edu.usco.aprendizaje_usco.modulos.progreso.dto.MetricasDimensionConsulta;
import co.edu.usco.aprendizaje_usco.modulos.progreso.dto.ProgresoDimensionResponse;
import co.edu.usco.aprendizaje_usco.modulos.progreso.dto.ProgresoResponse;
import co.edu.usco.aprendizaje_usco.modulos.progreso.dto.TendenciaSemanalProyeccion;
import co.edu.usco.aprendizaje_usco.modulos.progreso.model.EstadoVariacion;
import co.edu.usco.aprendizaje_usco.modulos.progreso.model.NivelDominio;
import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

class ProgresoServiceTest {

    private IntentoPracticaRepository intentoPracticaRepository;
    private CompetenciaRepository competenciaRepository;
    private IntentoDiagnosticoRepository intentoDiagnosticoRepository;
    private ResultadoDimensionRepository resultadoDimensionRepository;
    private ProgresoService service;

    private Competencia compAlgebra;
    private Competencia compLogica;
    private Competencia compFuncional;

    @BeforeEach
    void setUp() {
        intentoPracticaRepository = mock(IntentoPracticaRepository.class);
        competenciaRepository = mock(CompetenciaRepository.class);
        intentoDiagnosticoRepository = mock(IntentoDiagnosticoRepository.class);
        resultadoDimensionRepository = mock(ResultadoDimensionRepository.class);

        service = new ProgresoService(
                intentoPracticaRepository,
                competenciaRepository,
                intentoDiagnosticoRepository,
                resultadoDimensionRepository
        );

        compAlgebra = crearCompetenciaMock(1L, "ALGEBRAICA", "Álgebra");
        compLogica = crearCompetenciaMock(2L, "LOGICA", "Lógica");
        compFuncional = crearCompetenciaMock(3L, "FUNCIONAL", "Funcional");

        when(competenciaRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(compAlgebra, compLogica, compFuncional));
    }

    @Test
    @DisplayName("Estudiante sin actividad: totales en cero, niveles sin evaluar, sin actividad en dimensiones")
    void estudianteSinActividad() {
        Long usuarioId = 100L;
        when(intentoPracticaRepository.countByUsuarioId(usuarioId)).thenReturn(0L);
        when(intentoPracticaRepository.countEjerciciosCompletados(usuarioId)).thenReturn(0L);
        when(intentoPracticaRepository.findMetricasPorDimension(usuarioId)).thenReturn(List.of());
        when(intentoPracticaRepository.findTendenciaSemanal(usuarioId)).thenReturn(List.of());
        when(intentoDiagnosticoRepository.findFirstByUsuarioIdAndEstadoOrderByFinalizadoEnDesc(usuarioId, EstadoIntento.FINALIZADO))
                .thenReturn(Optional.empty());

        ProgresoResponse respuesta = service.obtenerProgreso(usuarioId);

        assertThat(respuesta.totalIntentos()).isEqualTo(0L);
        assertThat(respuesta.ejerciciosCompletados()).isEqualTo(0L);
        assertThat(respuesta.nivelGeneral()).isEqualTo("SIN_EVALUAR");
        assertThat(respuesta.porcentajeGeneral()).isNull();
        assertThat(respuesta.evaluacionParcial()).isFalse();
        assertThat(respuesta.tendenciaSemanal()).isEmpty();
        assertThat(respuesta.dimensiones()).hasSize(3);

        for (ProgresoDimensionResponse dim : respuesta.dimensiones()) {
            assertThat(dim.sinActividad()).isTrue();
            assertThat(dim.intentosEvaluados()).isEqualTo(0L);
            assertThat(dim.intentosCorrectos()).isEqualTo(0L);
            assertThat(dim.porcentajeAciertos()).isNull();
            assertThat(dim.nivel()).isNull();
            assertThat(dim.porcentajeDiagnostico()).isNull();
            assertThat(dim.variacionPuntosPorcentuales()).isNull();
            assertThat(dim.estadoVariacion()).isEqualTo(EstadoVariacion.DIAGNOSTICO_PENDIENTE.name());
        }
    }

    @Test
    @DisplayName("Estudiante con práctica pero sin diagnóstico finalizado: variaciones en DIAGNOSTICO_PENDIENTE")
    void estudianteConPracticaSinDiagnostico() {
        Long usuarioId = 101L;
        when(intentoPracticaRepository.countByUsuarioId(usuarioId)).thenReturn(10L);
        when(intentoPracticaRepository.countEjerciciosCompletados(usuarioId)).thenReturn(6L);
        when(intentoPracticaRepository.findMetricasPorDimension(usuarioId)).thenReturn(List.of(
                new MetricasDimensionConsulta(1L, 10L, 8L) // 80% Álgebra -> AVANZADO
        ));
        when(intentoDiagnosticoRepository.findFirstByUsuarioIdAndEstadoOrderByFinalizadoEnDesc(usuarioId, EstadoIntento.FINALIZADO))
                .thenReturn(Optional.empty());

        ProgresoResponse respuesta = service.obtenerProgreso(usuarioId);

        assertThat(respuesta.totalIntentos()).isEqualTo(10L);
        assertThat(respuesta.ejerciciosCompletados()).isEqualTo(6L);
        assertThat(respuesta.evaluacionParcial()).isTrue();
        assertThat(respuesta.porcentajeGeneral()).isEqualByComparingTo(BigDecimal.valueOf(80.00).setScale(2));
        assertThat(respuesta.nivelGeneral()).isEqualTo("AVANZADO");

        ProgresoDimensionResponse algebra = respuesta.dimensiones().stream()
                .filter(d -> d.competenciaId().equals(1L)).findFirst().orElseThrow();
        assertThat(algebra.sinActividad()).isFalse();
        assertThat(algebra.intentosEvaluados()).isEqualTo(10L);
        assertThat(algebra.intentosCorrectos()).isEqualTo(8L);
        assertThat(algebra.porcentajeAciertos()).isEqualByComparingTo(BigDecimal.valueOf(80.00).setScale(2));
        assertThat(algebra.nivel()).isEqualTo("AVANZADO");
        assertThat(algebra.estadoVariacion()).isEqualTo(EstadoVariacion.DIAGNOSTICO_PENDIENTE.name());
        assertThat(algebra.variacionPuntosPorcentuales()).isNull();
    }

    @Test
    @DisplayName("Diagnóstico presente pero sin práctica en una dimensión: PRACTICA_PENDIENTE")
    void diagnosticoPresenteSinPracticaEnDimension() {
        Long usuarioId = 102L;
        IntentoDiagnostico diagMock = mock(IntentoDiagnostico.class);
        when(diagMock.getId()).thenReturn(50L);
        when(intentoDiagnosticoRepository.findFirstByUsuarioIdAndEstadoOrderByFinalizadoEnDesc(usuarioId, EstadoIntento.FINALIZADO))
                .thenReturn(Optional.of(diagMock));

        ResultadoDimension resAlgebra = mock(ResultadoDimension.class);
        when(resAlgebra.getCompetencia()).thenReturn(compAlgebra);
        when(resAlgebra.getPorcentaje()).thenReturn(BigDecimal.valueOf(62.50));

        when(resultadoDimensionRepository.findDelIntento(50L)).thenReturn(List.of(resAlgebra));
        when(intentoPracticaRepository.countByUsuarioId(usuarioId)).thenReturn(0L);
        when(intentoPracticaRepository.countEjerciciosCompletados(usuarioId)).thenReturn(0L);
        when(intentoPracticaRepository.findMetricasPorDimension(usuarioId)).thenReturn(List.of());

        ProgresoResponse respuesta = service.obtenerProgreso(usuarioId);

        ProgresoDimensionResponse algebra = respuesta.dimensiones().stream()
                .filter(d -> d.competenciaId().equals(1L)).findFirst().orElseThrow();
        assertThat(algebra.sinActividad()).isTrue();
        assertThat(algebra.porcentajeDiagnostico()).isEqualByComparingTo(BigDecimal.valueOf(62.50));
        assertThat(algebra.estadoVariacion()).isEqualTo(EstadoVariacion.PRACTICA_PENDIENTE.name());
        assertThat(algebra.variacionPuntosPorcentuales()).isNull();
    }

    @Test
    @DisplayName("Cálculo de variación y estados: MEJORA, RETROCESO y SIN_CAMBIO")
    void calculoVariacionEstados() {
        Long usuarioId = 103L;
        IntentoDiagnostico diagMock = mock(IntentoDiagnostico.class);
        when(diagMock.getId()).thenReturn(51L);
        when(intentoDiagnosticoRepository.findFirstByUsuarioIdAndEstadoOrderByFinalizadoEnDesc(usuarioId, EstadoIntento.FINALIZADO))
                .thenReturn(Optional.of(diagMock));

        ResultadoDimension rAlg = mock(ResultadoDimension.class);
        when(rAlg.getCompetencia()).thenReturn(compAlgebra);
        when(rAlg.getPorcentaje()).thenReturn(BigDecimal.valueOf(60.00));

        ResultadoDimension rLog = mock(ResultadoDimension.class);
        when(rLog.getCompetencia()).thenReturn(compLogica);
        when(rLog.getPorcentaje()).thenReturn(BigDecimal.valueOf(70.00));

        ResultadoDimension rFun = mock(ResultadoDimension.class);
        when(rFun.getCompetencia()).thenReturn(compFuncional);
        when(rFun.getPorcentaje()).thenReturn(BigDecimal.valueOf(50.00));

        when(resultadoDimensionRepository.findDelIntento(51L)).thenReturn(List.of(rAlg, rLog, rFun));

        // Álgebra: 4 de 5 = 80.00% -> Variación: 80.00 - 60.00 = +20.00 (MEJORA)
        // Lógica: 1 de 2 = 50.00% -> Variación: 50.00 - 70.00 = -20.00 (RETROCESO)
        // Funcional: 2 de 4 = 50.00% -> Variación: 50.00 - 50.00 = 0.00 (SIN_CAMBIO)
        when(intentoPracticaRepository.findMetricasPorDimension(usuarioId)).thenReturn(List.of(
                new MetricasDimensionConsulta(1L, 5L, 4L),
                new MetricasDimensionConsulta(2L, 2L, 1L),
                new MetricasDimensionConsulta(3L, 4L, 2L)
        ));

        ProgresoResponse respuesta = service.obtenerProgreso(usuarioId);

        ProgresoDimensionResponse dAlg = respuesta.dimensiones().stream()
                .filter(d -> d.competenciaId().equals(1L)).findFirst().orElseThrow();
        assertThat(dAlg.variacionPuntosPorcentuales()).isEqualByComparingTo(BigDecimal.valueOf(20.00));
        assertThat(dAlg.estadoVariacion()).isEqualTo(EstadoVariacion.MEJORA.name());

        ProgresoDimensionResponse dLog = respuesta.dimensiones().stream()
                .filter(d -> d.competenciaId().equals(2L)).findFirst().orElseThrow();
        assertThat(dLog.variacionPuntosPorcentuales()).isEqualByComparingTo(BigDecimal.valueOf(-20.00));
        assertThat(dLog.estadoVariacion()).isEqualTo(EstadoVariacion.RETROCESO.name());

        ProgresoDimensionResponse dFun = respuesta.dimensiones().stream()
                .filter(d -> d.competenciaId().equals(3L)).findFirst().orElseThrow();
        assertThat(dFun.variacionPuntosPorcentuales()).isEqualByComparingTo(BigDecimal.valueOf(0.00));
        assertThat(dFun.estadoVariacion()).isEqualTo(EstadoVariacion.SIN_CAMBIO.name());
    }

    @Test
    @DisplayName("Umbral de 3 intentos: dimensiones con menos de 3 no participan en el nivel general")
    void umbralNivelGeneral() {
        Long usuarioId = 104L;
        // Álgebra: 10 de 10 = 100.00% (>= 3 intentos, ACTIVA)
        // Lógica: 2 de 2 = 100.00% (< 3 intentos, INACTIVA para nivel general)
        // Funcional: 0 intentos (INACTIVA)
        when(intentoPracticaRepository.findMetricasPorDimension(usuarioId)).thenReturn(List.of(
                new MetricasDimensionConsulta(1L, 10L, 10L),
                new MetricasDimensionConsulta(2L, 2L, 2L)
        ));

        ProgresoResponse respuesta = service.obtenerProgreso(usuarioId);

        // Solo Álgebra entra en el macro-promedio: promedio = 100.00
        assertThat(respuesta.evaluacionParcial()).isTrue();
        assertThat(respuesta.porcentajeGeneral()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        assertThat(respuesta.nivelGeneral()).isEqualTo("AVANZADO");

        // Lógica sí tiene su nivel individual calculado
        ProgresoDimensionResponse logica = respuesta.dimensiones().stream()
                .filter(d -> d.competenciaId().equals(2L)).findFirst().orElseThrow();
        assertThat(logica.nivel()).isEqualTo("AVANZADO");
        assertThat(logica.intentosEvaluados()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Macro-promedio no ponderado: previene que una dimensión domine el nivel general")
    void macroPromedioNoPonderado() {
        Long usuarioId = 105L;
        // Álgebra: 90 correctas de 100 intentos = 90.00%
        // Lógica: 2 correctas de 4 intentos = 50.00%
        // Funcional: 1 correcta de 4 intentos = 25.00%
        // Si fuera ponderado: (90+2+1)/(100+4+4) = 93/108 = 86.11% (AVANZADO sesgado)
        // Macro-promedio no ponderado: (90.00 + 50.00 + 25.00) / 3 = 165.00 / 3 = 55.00% (BASICO justo)
        when(intentoPracticaRepository.findMetricasPorDimension(usuarioId)).thenReturn(List.of(
                new MetricasDimensionConsulta(1L, 100L, 90L),
                new MetricasDimensionConsulta(2L, 4L, 2L),
                new MetricasDimensionConsulta(3L, 4L, 1L)
        ));

        ProgresoResponse respuesta = service.obtenerProgreso(usuarioId);

        assertThat(respuesta.evaluacionParcial()).isFalse();
        assertThat(respuesta.porcentajeGeneral()).isEqualByComparingTo(BigDecimal.valueOf(55.00));
        assertThat(respuesta.nivelGeneral()).isEqualTo("BASICO");
    }

    @Test
    @DisplayName("Clasificación de los cuatro niveles de dominio")
    void clasificacionCuatroNiveles() {
        assertThat(NivelDominio.desde(BigDecimal.valueOf(0.00))).isEqualTo(NivelDominio.INICIAL);
        assertThat(NivelDominio.desde(BigDecimal.valueOf(39.99))).isEqualTo(NivelDominio.INICIAL);
        assertThat(NivelDominio.desde(BigDecimal.valueOf(40.00))).isEqualTo(NivelDominio.BASICO);
        assertThat(NivelDominio.desde(BigDecimal.valueOf(59.99))).isEqualTo(NivelDominio.BASICO);
        assertThat(NivelDominio.desde(BigDecimal.valueOf(60.00))).isEqualTo(NivelDominio.INTERMEDIO);
        assertThat(NivelDominio.desde(BigDecimal.valueOf(79.99))).isEqualTo(NivelDominio.INTERMEDIO);
        assertThat(NivelDominio.desde(BigDecimal.valueOf(80.00))).isEqualTo(NivelDominio.AVANZADO);
        assertThat(NivelDominio.desde(BigDecimal.valueOf(100.00))).isEqualTo(NivelDominio.AVANZADO);
        assertThat(NivelDominio.desde(null)).isNull();
    }

    @Test
    @DisplayName("Tendencia semanal: solo semanas activas, orden cronológico y formato de etiqueta")
    void tendenciaSemanal() {
        Long usuarioId = 106L;
        TendenciaSemanalProyeccion s1 = mock(TendenciaSemanalProyeccion.class);
        when(s1.getFechaInicio()).thenReturn(LocalDate.of(2026, 10, 12));
        when(s1.getIntentosEvaluados()).thenReturn(10L);
        when(s1.getIntentosCorrectos()).thenReturn(7L);

        TendenciaSemanalProyeccion s2 = mock(TendenciaSemanalProyeccion.class);
        when(s2.getFechaInicio()).thenReturn(LocalDate.of(2026, 10, 26));
        when(s2.getIntentosEvaluados()).thenReturn(8L);
        when(s2.getIntentosCorrectos()).thenReturn(6L);

        when(intentoPracticaRepository.findTendenciaSemanal(usuarioId)).thenReturn(List.of(s1, s2));

        ProgresoResponse respuesta = service.obtenerProgreso(usuarioId);

        assertThat(respuesta.tendenciaSemanal()).hasSize(2);
        assertThat(respuesta.tendenciaSemanal().get(0).anio()).isEqualTo(2026);
        assertThat(respuesta.tendenciaSemanal().get(0).semana()).isEqualTo(42);
        assertThat(respuesta.tendenciaSemanal().get(0).porcentajeAciertos()).isEqualByComparingTo(BigDecimal.valueOf(70.00));
        assertThat(respuesta.tendenciaSemanal().get(0).etiqueta()).contains("Semana 42");

        assertThat(respuesta.tendenciaSemanal().get(1).anio()).isEqualTo(2026);
        assertThat(respuesta.tendenciaSemanal().get(1).semana()).isEqualTo(44);
        assertThat(respuesta.tendenciaSemanal().get(1).porcentajeAciertos()).isEqualByComparingTo(BigDecimal.valueOf(75.00));
        assertThat(respuesta.tendenciaSemanal().get(1).etiqueta()).contains("Semana 44");
    }

    private static Competencia crearCompetenciaMock(Long id, String codigo, String nombre) {
        Competencia c = mock(Competencia.class);
        when(c.getId()).thenReturn(id);
        when(c.getCodigo()).thenReturn(codigo);
        when(c.getNombre()).thenReturn(nombre);
        return c;
    }

}
