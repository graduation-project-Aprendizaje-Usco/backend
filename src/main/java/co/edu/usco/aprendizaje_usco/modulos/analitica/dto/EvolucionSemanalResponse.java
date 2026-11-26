package co.edu.usco.aprendizaje_usco.modulos.analitica.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EvolucionSemanalResponse(
        int anio,
        int semana,
        LocalDate fechaInicio,
        long intentosEvaluados,
        long intentosCorrectos,
        BigDecimal promedio) {
}
