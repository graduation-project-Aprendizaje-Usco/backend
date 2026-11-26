package co.edu.usco.aprendizaje_usco.modulos.analitica.dto;

import java.math.BigDecimal;

public record DistribucionCategoriaResponse(
        String categoria,
        long cantidad,
        BigDecimal porcentaje) {
}
