package co.edu.usco.aprendizaje_usco.modulos.analitica.dto;

import java.time.Instant;
import java.util.List;

public record DiagnosticoEstudianteResponse(
        Long intentoId,
        Instant finalizadoEn,
        int puntaje,
        int total,
        String categoria,
        List<DimensionEstudianteResponse> dimensiones) {
}
