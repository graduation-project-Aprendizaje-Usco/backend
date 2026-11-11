package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Invitacion;
import java.time.Instant;

/** Invitación vista por el docente; nunca incluye el token. */
public record InvitacionResponse(
        Long id,
        String correo,
        String nombres,
        String apellidos,
        String estado,
        Instant venceEn,
        Instant usadaEn) {

    public static InvitacionResponse desde(Invitacion invitacion, Instant ahora) {
        String estado = invitacion.estaUsada() ? "REGISTRADO"
                : invitacion.estaVigente(ahora) ? "INVITADO" : "VENCIDA";
        return new InvitacionResponse(invitacion.getId(), invitacion.getCorreo(), invitacion.getNombres(),
                invitacion.getApellidos(), estado, invitacion.getVenceEn(), invitacion.getUsadaEn());
    }
}
