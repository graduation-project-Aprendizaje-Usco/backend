package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de POST /api/auth/login. */
public record LoginRequest(

        @Schema(example = "estudiante@aprendizaje.test")
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        @Size(max = 150)
        String correo,

        @Schema(example = "Estudiante2026*")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(max = 72)
        String password) {

    /** Quita espacios del correo antes de validarlo (ej. al copiar y pegar). */
    public LoginRequest {
        correo = correo == null ? null : correo.trim();
    }

    /** Sin la contraseña a propósito: el toString de un record termina en logs. */
    @Override
    public String toString() {
        return "LoginRequest{correo=" + correo + "}";
    }

}
