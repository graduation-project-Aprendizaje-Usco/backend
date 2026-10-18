package co.edu.usco.aprendizaje_usco.modulos.diagnostico.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Cuerpo de PUT /api/diagnosticos/intentos/{id}/respuestas/{itemId}. */
public record RespuestaRequest(
        @NotNull(message = "Debes elegir una opción")
        @Positive(message = "La opción no es válida")
        Long opcionId) {
}
