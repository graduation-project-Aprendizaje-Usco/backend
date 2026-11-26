package co.edu.usco.aprendizaje_usco.modulos.progreso.dto;

import co.edu.usco.aprendizaje_usco.modulos.progreso.model.TipoActividad;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Una fila del historial. Los campos que no aplican al tipo van en null y no se envían:
 *  - DIAGNOSTICO: aciertos/total y categoria (con filtro de competencia, los de esa competencia).
 *  - PRACTICA: correcto, nivel, tiempoSeg y pistaUsada.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HistorialItemResponse(

        @Schema(example = "PRACTICA")
        TipoActividad tipo,

        @Schema(description = "Id del intento (de diagnóstico o de práctica)", example = "15")
        Long id,

        @Schema(example = "2026-10-20T15:30:00Z")
        Instant fecha,

        @Schema(description = "Prueba diagnóstica o contenido del ejercicio", example = "Factorización")
        String actividad,

        @Schema(example = "ALGEBRAICA")
        String competenciaCodigo,

        @Schema(example = "Competencia algebraica")
        String competenciaNombre,

        @Schema(example = "16")
        Integer aciertos,

        @Schema(example = "24")
        Integer total,

        @Schema(example = "NIVEL_ADECUADO")
        String categoria,

        @Schema(example = "true")
        Boolean correcto,

        @Schema(example = "2")
        Integer nivel,

        @Schema(example = "48")
        Integer tiempoSeg,

        @Schema(example = "false")
        Boolean pistaUsada) {
}
