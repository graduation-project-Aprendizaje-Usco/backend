package co.edu.usco.aprendizaje_usco.modulos.progreso.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Registro de rendimiento semanal para el gráfico de tendencia temporal.
 */
public record TendenciaSemanalResponse(
        int anio,
        int semana,
        LocalDate fechaInicio,
        String etiqueta,
        long intentosEvaluados,
        long intentosCorrectos,
        BigDecimal porcentajeAciertos
) {
}
