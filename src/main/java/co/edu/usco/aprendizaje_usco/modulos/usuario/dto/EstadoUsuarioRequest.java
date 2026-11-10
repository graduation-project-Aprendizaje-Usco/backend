package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.EstadoUsuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** Cuerpo de PATCH /api/admin/usuarios/{id}/estado. */
public record EstadoUsuarioRequest(
        @Schema(example = "INACTIVO")
        @NotNull(message = "El estado es obligatorio")
        EstadoUsuario estado) {
}
