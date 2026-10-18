package co.edu.usco.aprendizaje_usco.modulos.diagnostico.dto;

import java.math.BigDecimal;

public record ResultadoDimensionResponse(
        String codigo,
        String nombre,
        int aciertos,
        int total,
        BigDecimal porcentaje,
        String nivel,
        String nivelDescripcion) {
}
