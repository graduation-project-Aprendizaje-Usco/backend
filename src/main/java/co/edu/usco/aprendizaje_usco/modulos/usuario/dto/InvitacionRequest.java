package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Una fila del listado que carga el docente. */
public record InvitacionRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        @Size(max = 150)
        String correo,

        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100)
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100)
        String apellidos) {

    public InvitacionRequest {
        correo = correo == null ? null : correo.trim();
        nombres = nombres == null ? null : nombres.trim();
        apellidos = apellidos == null ? null : apellidos.trim();
    }
}
