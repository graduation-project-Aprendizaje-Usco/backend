package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Grupo;
import io.swagger.v3.oas.annotations.media.Schema;

public record GrupoResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "CALCULO-2026-1") String codigo,
        @Schema(example = "Cálculo Diferencial - Grupo 01") String nombre,
        @Schema(example = "docente@usco.edu.co") String docenteCorreo) {

    public static GrupoResponse desde(Grupo grupo) {
        return new GrupoResponse(grupo.getId(), grupo.getCodigo(), grupo.getNombre(), grupo.getDocente().getCorreo());
    }
}
