package co.edu.usco.aprendizaje_usco.modulos.progreso.dto;

import java.math.BigDecimal;

/**
 * Resumen de indicadores de progreso para una dimensión (competencia) específica.
 */
public record ProgresoDimensionResponse(
        Long competenciaId,
        String codigo,
        String nombre,
        long intentosEvaluados,
        long intentosCorrectos,
        BigDecimal porcentajeAciertos,
        String nivel,
        BigDecimal porcentajeDiagnostico,
        BigDecimal variacionPuntosPorcentuales,
        String estadoVariacion,
        boolean sinActividad
) {
}
