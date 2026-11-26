package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** Listado de estudiantes que el docente carga para invitar a un grupo. */
public record InvitacionLoteRequest(
        @NotEmpty(message = "El listado no puede estar vacío")
        List<@Valid InvitacionRequest> estudiantes) {
}
