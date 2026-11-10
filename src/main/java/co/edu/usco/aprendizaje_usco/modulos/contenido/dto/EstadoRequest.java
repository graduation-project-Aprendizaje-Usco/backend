package co.edu.usco.aprendizaje_usco.modulos.contenido.dto;

import jakarta.validation.constraints.NotNull;

public record EstadoRequest(@NotNull(message = "El estado es obligatorio") Boolean activo) {
}
