package co.edu.usco.aprendizaje_usco.modulos.diagnostico.dto;

import java.time.Instant;
import java.util.List;

/**
 * Resultado de un intento finalizado: puntaje global (0 a 24), categoría y el
 * detalle por competencia.
 */
public record ResultadoResponse(
        int puntaje,
        int total,
        String categoria,
        String categoriaDescripcion,
        boolean finalizadoPorTiempo,
        Instant finalizadoEn,
        List<ResultadoDimensionResponse> dimensiones) {
}
