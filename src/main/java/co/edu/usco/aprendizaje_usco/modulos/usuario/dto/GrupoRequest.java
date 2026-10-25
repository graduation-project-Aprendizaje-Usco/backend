package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GrupoRequest(
        @Schema(example = "CALCULO-2026-1")
        @NotBlank
        @Size(max = 30)
        @Pattern(regexp = "(?i)^[A-Z0-9][A-Z0-9_-]{1,29}$",
                message = "Usa letras, números, guion o guion bajo")
        String codigo,

        @Schema(example = "Cálculo Diferencial - Grupo 01")
        @NotBlank
        @Size(max = 100)
        String nombre) {
}
