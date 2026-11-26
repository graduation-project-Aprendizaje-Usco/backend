package co.edu.usco.aprendizaje_usco.modulos.contenido.dto;

import co.edu.usco.aprendizaje_usco.modulos.contenido.model.TipoEjercicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record EjercicioRequest(
        @NotNull(message = "El contenido es obligatorio")
        Long contenidoId,

        @Size(max = 40)
        String codigo,

        @NotNull(message = "El nivel es obligatorio")
        @Min(value = 1, message = "El nivel debe estar entre 1 y 3")
        @Max(value = 3, message = "El nivel debe estar entre 1 y 3")
        Integer nivel,

        @NotNull(message = "El tipo es obligatorio")
        TipoEjercicio tipo,

        @NotBlank(message = "El enunciado es obligatorio")
        @Size(max = 2000)
        String enunciado,

        List<@Valid OpcionEjercicioRequest> opciones,

        BigDecimal respuestaNumerica,

        BigDecimal tolerancia,

        @NotBlank(message = "La pista es obligatoria")
        @Size(max = 1000)
        String pista,

        @NotBlank(message = "La explicación es obligatoria")
        @Size(max = 2000)
        String explicacion,

        @Size(max = 1000)
        String errorComun) {
}
