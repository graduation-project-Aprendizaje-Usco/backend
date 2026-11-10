package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cuerpo de PUT /api/admin/usuarios/{id}. El correo no se cambia: identifica la cuenta. */
public record ActualizarUsuarioRequest(
        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100, message = "Los nombres pueden tener máximo 100 caracteres")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100, message = "Los apellidos pueden tener máximo 100 caracteres")
        String apellidos,

        @Schema(example = "DOCENTE")
        @NotNull(message = "El rol es obligatorio")
        NombreRol rol,

        @Schema(example = "Ingeniería de Software", description = "Solo para estudiantes")
        String programaAcademico) {
}
