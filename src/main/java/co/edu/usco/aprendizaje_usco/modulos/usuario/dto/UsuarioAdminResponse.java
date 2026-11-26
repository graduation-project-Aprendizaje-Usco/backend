package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.EstadoUsuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import java.time.Instant;

/** Usuario visto por el administrador. Nunca incluye la contraseña ni su hash. */
public record UsuarioAdminResponse(
        Long id,
        String nombres,
        String apellidos,
        String correo,
        NombreRol rol,
        String programaAcademico,
        EstadoUsuario estado,
        Instant creadoEn,
        boolean configuracionPendiente) {

    /** Llamar dentro de una transacción: el rol del usuario se carga de forma diferida. */
    public static UsuarioAdminResponse desde(Usuario usuario) {
        return desde(usuario, false);
    }

    public static UsuarioAdminResponse desde(Usuario usuario, boolean configuracionPendiente) {
        return new UsuarioAdminResponse(usuario.getId(), usuario.getNombres(), usuario.getApellidos(),
                usuario.getCorreo(), usuario.getRol().getNombre(), usuario.getProgramaAcademico(),
                usuario.getEstado(), usuario.getCreadoEn(), configuracionPendiente);
    }
}
