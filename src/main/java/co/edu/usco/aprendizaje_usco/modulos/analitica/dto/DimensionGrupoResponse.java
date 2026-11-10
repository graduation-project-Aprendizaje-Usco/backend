package co.edu.usco.aprendizaje_usco.modulos.analitica.dto;

import java.math.BigDecimal;

public record DimensionGrupoResponse(
        String codigo,
        String nombre,
        BigDecimal promedio,
        long estudiantesEvaluados,
        long intentosEvaluados,
        long intentosCorrectos) {
}
