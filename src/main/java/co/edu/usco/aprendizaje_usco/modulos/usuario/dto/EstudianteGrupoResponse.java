package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

public record EstudianteGrupoResponse(
        @Schema(example = "12") Long id,
        @Schema(example = "Ana María") String nombres,
        @Schema(example = "Pérez") String apellidos,
        @Schema(example = "u20201234567@usco.edu.co") String correo) {

    public static EstudianteGrupoResponse desde(Usuario usuario) {
        return new EstudianteGrupoResponse(
                usuario.getId(), usuario.getNombres(), usuario.getApellidos(), usuario.getCorreo());
    }
}
