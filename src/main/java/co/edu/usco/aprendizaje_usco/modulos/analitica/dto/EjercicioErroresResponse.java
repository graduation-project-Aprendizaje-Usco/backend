package co.edu.usco.aprendizaje_usco.modulos.analitica.dto;

import java.math.BigDecimal;

public record EjercicioErroresResponse(
        Long ejercicioId,
        String codigo,
        String enunciado,
        String competenciaCodigo,
        String competenciaNombre,
        int nivel,
        long intentos,
        long errores,
        BigDecimal tasaError) {
}
