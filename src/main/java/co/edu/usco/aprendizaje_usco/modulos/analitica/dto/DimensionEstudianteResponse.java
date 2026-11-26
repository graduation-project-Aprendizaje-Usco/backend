package co.edu.usco.aprendizaje_usco.modulos.analitica.dto;

import java.math.BigDecimal;

public record DimensionEstudianteResponse(
        String codigo,
        String nombre,
        long intentos,
        long aciertos,
        BigDecimal promedio,
        String nivel) {
}
