package co.edu.usco.aprendizaje_usco.modulos.contenido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContenidoRequest(
        @NotBlank(message = "La competencia es obligatoria")
        @Size(max = 30)
        String competencia,

        @NotBlank(message = "La asignatura es obligatoria")
        @Size(max = 80)
        String asignatura,

        @NotBlank(message = "El tema es obligatorio")
        @Size(max = 120)
        String tema,

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 150)
        String titulo,

        @Size(max = 500)
        String descripcion) {
}
