package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Define la contraseña de una cuenta creada por administración. */
public record ActivarCuentaRequest(
        @NotBlank(message = "El enlace de activación es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9_-]{43}$", message = "El enlace de activación no es válido")
        String token,

        @Schema(example = "MiClave2026*")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        @Pattern(regexp = "^(?=.*\\p{L})(?=.*\\d).*$",
                message = "La contraseña debe tener al menos una letra y un número")
        String password) {

    @Override
    public String toString() {
        return "ActivarCuentaRequest{token=<redacted>, password=<redacted>}";
    }
}
