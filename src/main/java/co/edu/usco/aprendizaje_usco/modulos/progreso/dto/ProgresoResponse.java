package co.edu.usco.aprendizaje_usco.modulos.progreso.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Respuesta integral con los seis indicadores de progreso del estudiante autenticado.
 */
public record ProgresoResponse(
        long totalIntentos,
        long ejerciciosCompletados,
        String nivelGeneral,
        BigDecimal porcentajeGeneral,
        boolean evaluacionParcial,
        List<ProgresoDimensionResponse> dimensiones,
        List<TendenciaSemanalResponse> tendenciaSemanal
) {
}
