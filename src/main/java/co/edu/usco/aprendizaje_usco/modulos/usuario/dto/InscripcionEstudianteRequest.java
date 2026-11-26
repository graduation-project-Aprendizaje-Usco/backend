package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InscripcionEstudianteRequest(
        @Schema(example = "u20201234567@usco.edu.co")
        @NotBlank
        @Email
        @Size(max = 150)
        String correo) {
}
