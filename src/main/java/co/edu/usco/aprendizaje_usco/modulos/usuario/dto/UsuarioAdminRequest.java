package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de POST /api/admin/usuarios. La contraseña no se recibe: el servidor
 * genera una clave aleatoria no compartida hasta que configure la suya por correo.
 * Para un ESTUDIANTE el correo debe ser de estudiante y el
 * programa académico es obligatorio (lo valida AdminUsuarioService).
 */
public record UsuarioAdminRequest(
        @Schema(example = "Laura")
        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100, message = "Los nombres pueden tener máximo 100 caracteres")
        String nombres,

        @Schema(example = "Gómez Ruiz")
        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100, message = "Los apellidos pueden tener máximo 100 caracteres")
        String apellidos,

        @Schema(example = "laura.gomez@usco.edu.co")
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        @Size(max = 150)
        String correo,

        @Schema(example = "DOCENTE")
        @NotNull(message = "El rol es obligatorio")
        NombreRol rol,

        @Schema(example = "Ingeniería de Software", description = "Solo para estudiantes")
        String programaAcademico) {

    /** Quita espacios del correo antes de validarlo. */
    public UsuarioAdminRequest {
        correo = correo == null ? null : correo.trim();
    }
}
