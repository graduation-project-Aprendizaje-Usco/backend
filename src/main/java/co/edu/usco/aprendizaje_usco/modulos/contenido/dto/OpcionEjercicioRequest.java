package co.edu.usco.aprendizaje_usco.modulos.contenido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OpcionEjercicioRequest(
        @NotBlank(message = "La letra de la opción es obligatoria")
        @Pattern(regexp = "[A-Da-d]", message = "La letra debe estar entre A y D")
        String letra,

        @NotBlank(message = "El texto de la opción es obligatorio")
        @Size(max = 500)
        String texto,

        @NotNull(message = "Indica si la opción es correcta")
        Boolean correcta) {
}
