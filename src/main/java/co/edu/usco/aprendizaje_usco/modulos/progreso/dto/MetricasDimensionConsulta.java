package co.edu.usco.aprendizaje_usco.modulos.progreso.dto;

/**
 * Proyección de agregación JPQL para métricas por competencia/dimensión.
 */
public record MetricasDimensionConsulta(
        Long competenciaId,
        long intentosEvaluados,
        long intentosCorrectos
) {
}
