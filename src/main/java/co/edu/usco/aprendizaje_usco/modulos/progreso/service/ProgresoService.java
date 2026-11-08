package co.edu.usco.aprendizaje_usco.modulos.progreso.service;

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
import co.edu.usco.aprendizaje_usco.modulos.progreso.dto.TendenciaSemanalResponse;
import co.edu.usco.aprendizaje_usco.modulos.progreso.model.EstadoVariacion;
import co.edu.usco.aprendizaje_usco.modulos.progreso.model.NivelDominio;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de calcular y consolidar los seis indicadores de progreso
 * académico del estudiante autenticado.
 */
@Service
@Transactional(readOnly = true)
public class ProgresoService {

    private static final int UMBRAL_INTENTOS_NIVEL_GENERAL = 3;
    private static final Locale LOCALE_ES_CO = Locale.of("es", "CO");

    private final IntentoPracticaRepository intentoPracticaRepository;
    private final CompetenciaRepository competenciaRepository;
    private final IntentoDiagnosticoRepository intentoDiagnosticoRepository;
    private final ResultadoDimensionRepository resultadoDimensionRepository;

    public ProgresoService(IntentoPracticaRepository intentoPracticaRepository,
                           CompetenciaRepository competenciaRepository,
                           IntentoDiagnosticoRepository intentoDiagnosticoRepository,
                           ResultadoDimensionRepository resultadoDimensionRepository) {
        this.intentoPracticaRepository = intentoPracticaRepository;
        this.competenciaRepository = competenciaRepository;
        this.intentoDiagnosticoRepository = intentoDiagnosticoRepository;
        this.resultadoDimensionRepository = resultadoDimensionRepository;
    }

    public ProgresoResponse obtenerProgreso(Long usuarioId) {
        // Indicador 2: Total de intentos del estudiante
        long totalIntentos = intentoPracticaRepository.countByUsuarioId(usuarioId);

        // Indicador 3: Ejercicios completados (acertados en intento 1 o 2 sin duplicados)
        long ejerciciosCompletados = intentoPracticaRepository.countEjerciciosCompletados(usuarioId);

        // Todas las competencias del catálogo ordenadas por ID
        List<Competencia> competencias = competenciaRepository.findAll(Sort.by("id"));

        // Métricas de práctica agrupadas por dimensión
        Map<Long, MetricasDimensionConsulta> metricasPorCompetencia = intentoPracticaRepository
                .findMetricasPorDimension(usuarioId).stream()
                .collect(Collectors.toMap(MetricasDimensionConsulta::competenciaId, Function.identity()));

        // Último diagnóstico finalizado del estudiante (para Indicador 5)
        Optional<IntentoDiagnostico> ultimoDiagnosticoOpt = intentoDiagnosticoRepository
                .findFirstByUsuarioIdAndEstadoOrderByFinalizadoEnDesc(usuarioId, EstadoIntento.FINALIZADO);

        Map<Long, ResultadoDimension> diagnosticoPorCompetencia;
        if (ultimoDiagnosticoOpt.isPresent()) {
            diagnosticoPorCompetencia = resultadoDimensionRepository
                    .findDelIntento(ultimoDiagnosticoOpt.get().getId()).stream()
                    .collect(Collectors.toMap(r -> r.getCompetencia().getId(), Function.identity()));
        } else {
            diagnosticoPorCompetencia = Collections.emptyMap();
        }

        // Construir dimensiones y acumular para Nivel General (Indicadores 1, 4, 5)
        List<ProgresoDimensionResponse> dimensiones = new ArrayList<>();
        List<BigDecimal> porcentajesActivosNivelGeneral = new ArrayList<>();

        for (Competencia comp : competencias) {
            MetricasDimensionConsulta metricas = metricasPorCompetencia.get(comp.getId());
            long intentosEvaluados = metricas != null ? metricas.intentosEvaluados() : 0L;
            long intentosCorrectos = metricas != null ? metricas.intentosCorrectos() : 0L;
            boolean sinActividad = (intentosEvaluados == 0);

            BigDecimal porcentajeAciertos = null;
            String nivelDimension = null;

            if (!sinActividad) {
                porcentajeAciertos = calcularPorcentaje(intentosCorrectos, intentosEvaluados);
                NivelDominio nivelDominio = NivelDominio.desde(porcentajeAciertos);
                nivelDimension = nivelDominio != null ? nivelDominio.name() : null;

                // Indicador 4: Umbral para nivel general (mínimo 3 intentos evaluados)
                if (intentosEvaluados >= UMBRAL_INTENTOS_NIVEL_GENERAL) {
                    porcentajesActivosNivelGeneral.add(porcentajeAciertos);
                }
            }

            // Indicador 5: Variación frente al diagnóstico
            BigDecimal porcentajeDiagnostico = null;
            BigDecimal variacionPuntosPorcentuales = null;
            String estadoVariacion;

            if (ultimoDiagnosticoOpt.isEmpty()) {
                estadoVariacion = EstadoVariacion.DIAGNOSTICO_PENDIENTE.name();
            } else {
                ResultadoDimension resDiag = diagnosticoPorCompetencia.get(comp.getId());
                if (resDiag != null) {
                    porcentajeDiagnostico = resDiag.getPorcentaje();
                }

                if (sinActividad) {
                    estadoVariacion = EstadoVariacion.PRACTICA_PENDIENTE.name();
                } else if (porcentajeDiagnostico != null) {
                    variacionPuntosPorcentuales = porcentajeAciertos.subtract(porcentajeDiagnostico);
                    int cmp = variacionPuntosPorcentuales.compareTo(BigDecimal.ZERO);
                    if (cmp > 0) {
                        estadoVariacion = EstadoVariacion.MEJORA.name();
                    } else if (cmp < 0) {
                        estadoVariacion = EstadoVariacion.RETROCESO.name();
                    } else {
                        estadoVariacion = EstadoVariacion.SIN_CAMBIO.name();
                    }
                } else {
                    estadoVariacion = EstadoVariacion.DIAGNOSTICO_PENDIENTE.name();
                }
            }

            dimensiones.add(new ProgresoDimensionResponse(
                    comp.getId(),
                    comp.getCodigo(),
                    comp.getNombre(),
                    intentosEvaluados,
                    intentosCorrectos,
                    porcentajeAciertos,
                    nivelDimension,
                    porcentajeDiagnostico,
                    variacionPuntosPorcentuales,
                    estadoVariacion,
                    sinActividad
            ));
        }

        // Indicador 4: Nivel general y porcentaje general (macro-promedio)
        String nivelGeneral;
        BigDecimal porcentajeGeneral = null;
        boolean evaluacionParcial = false;

        if (porcentajesActivosNivelGeneral.isEmpty()) {
            nivelGeneral = "SIN_EVALUAR";
        } else {
            BigDecimal suma = BigDecimal.ZERO;
            for (BigDecimal p : porcentajesActivosNivelGeneral) {
                suma = suma.add(p);
            }
            porcentajeGeneral = suma.divide(
                    BigDecimal.valueOf(porcentajesActivosNivelGeneral.size()), 2, RoundingMode.HALF_UP);
            nivelGeneral = NivelDominio.desde(porcentajeGeneral).name();
            evaluacionParcial = (porcentajesActivosNivelGeneral.size() < competencias.size());
        }

        // Indicador 6: Tendencia semanal
        List<TendenciaSemanalResponse> tendenciaSemanal = new ArrayList<>();
        List<TendenciaSemanalProyeccion> proyeccionesSemana = intentoPracticaRepository.findTendenciaSemanal(usuarioId);

        for (TendenciaSemanalProyeccion proy : proyeccionesSemana) {
            LocalDate fechaInicio = proy.getFechaInicio();
            int anio = fechaInicio.get(IsoFields.WEEK_BASED_YEAR);
            int semana = fechaInicio.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);

            long evalSemana = proy.getIntentosEvaluados();
            long corrSemana = proy.getIntentosCorrectos();
            BigDecimal porcSemana = calcularPorcentaje(corrSemana, evalSemana);

            String mes = fechaInicio.getMonth().getDisplayName(TextStyle.SHORT, LOCALE_ES_CO);
            String mesFormateado = capitalizarMes(mes);
            String etiqueta = "Semana " + semana + " (" + fechaInicio.getDayOfMonth() + " " + mesFormateado + ")";

            tendenciaSemanal.add(new TendenciaSemanalResponse(
                    anio,
                    semana,
                    fechaInicio,
                    etiqueta,
                    evalSemana,
                    corrSemana,
                    porcSemana
            ));
        }

        return new ProgresoResponse(
                totalIntentos,
                ejerciciosCompletados,
                nivelGeneral,
                porcentajeGeneral,
                evaluacionParcial,
                dimensiones,
                tendenciaSemanal
        );
    }

    private static BigDecimal calcularPorcentaje(long aciertos, long total) {
        if (total <= 0) {
            return null;
        }
        return BigDecimal.valueOf(aciertos * 100L)
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private static String capitalizarMes(String mes) {
        if (mes == null || mes.isEmpty()) {
            return "";
        }
        String limpio = mes.endsWith(".") ? mes.substring(0, mes.length() - 1) : mes;
        return limpio.substring(0, 1).toUpperCase(LOCALE_ES_CO) + limpio.substring(1).toLowerCase(LOCALE_ES_CO);
    }

}
