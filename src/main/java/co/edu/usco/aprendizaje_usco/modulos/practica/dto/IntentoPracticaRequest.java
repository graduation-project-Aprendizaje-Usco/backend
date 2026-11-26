package co.edu.usco.aprendizaje_usco.modulos.practica.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record IntentoPracticaRequest(
        @NotNull Long ejercicioId,
        Long opcionId,
        BigDecimal respuestaNumerica,
        @NotNull @Min(0) @Max(86400) Integer tiempoSeg,
        Boolean pistaUsada,
        @NotNull UUID claveIdempotencia) {
}
