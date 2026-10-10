package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de POST /api/auth/registro. Solo registra estudiantes: el rol no se
 * recibe, lo asigna el servidor.
 */
public record RegistroRequest(

        @Schema(example = "Ana María")
        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100)
        String nombres,

        @Schema(example = "Pérez Gómez")
        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100)
        String apellidos,

        @Schema(example = "ana.perez@usco.edu.co")
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        @Size(max = 150)
        String correo,

        // Máximo 72: BCrypt ignora lo que pase de 72 bytes.
        @Schema(example = "MiClave2026*")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String password,

        @Schema(example = "Ingeniería de Software", nullable = true)
        @Size(max = 120)
        String programaAcademico) {

    /** Quita espacios del correo antes de validarlo (ej. al copiar y pegar). */
    public RegistroRequest {
        correo = correo == null ? null : correo.trim();
    }

    /** Sin la contraseña a propósito: el toString de un record termina en logs. */
    @Override
    public String toString() {
        return "RegistroRequest{correo=" + correo + "}";
    }

}
