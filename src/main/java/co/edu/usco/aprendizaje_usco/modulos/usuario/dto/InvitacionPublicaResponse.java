package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Invitacion;

/** Datos mínimos que la pantalla de registro puede precargar desde el enlace. */
public record InvitacionPublicaResponse(
        String correo,
        String nombres,
        String apellidos,
        String grupo) {

    public static InvitacionPublicaResponse desde(Invitacion invitacion) {
        return new InvitacionPublicaResponse(invitacion.getCorreo(), invitacion.getNombres(),
                invitacion.getApellidos(), invitacion.getGrupo().getNombre());
    }
}
