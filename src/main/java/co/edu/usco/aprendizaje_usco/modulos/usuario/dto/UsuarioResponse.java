package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Datos públicos del usuario: lo que guarda el frontend en AuthContext
 * ({ id, nombres, correo, rol }). Nunca incluye la contraseña ni su hash.
 */
public record UsuarioResponse(

        @Schema(example = "1")
        Long id,

        @Schema(example = "Ana María")
        String nombres,

        @Schema(example = "ana.perez@usco.edu.co")
        String correo,

        @Schema(example = "ESTUDIANTE")
        NombreRol rol) {

    /** Llamar dentro de una transacción: el rol del usuario se carga de forma diferida. */
    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombres(), usuario.getCorreo(),
                usuario.getRol().getNombre());
    }

    public static UsuarioResponse desde(UsuarioAutenticado usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombres(), usuario.getCorreo(), usuario.getRol());
    }

}
