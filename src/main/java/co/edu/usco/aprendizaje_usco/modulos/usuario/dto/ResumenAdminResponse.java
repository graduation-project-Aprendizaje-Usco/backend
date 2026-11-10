package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import java.util.List;

/**
 * Conteos del panel del administrador (SCRUM-46). Los grupos se cuentan todos:
 * la tabla grupo no tiene estado activo/inactivo.
 */
public record ResumenAdminResponse(
        List<UsuariosPorRol> usuariosPorRol,
        long grupos,
        long contenidosActivos,
        long ejerciciosActivos) {

    public record UsuariosPorRol(NombreRol rol, long total, long activos) {
    }
}
