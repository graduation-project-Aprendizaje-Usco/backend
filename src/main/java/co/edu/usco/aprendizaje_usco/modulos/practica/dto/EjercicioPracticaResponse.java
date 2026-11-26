package co.edu.usco.aprendizaje_usco.modulos.practica.dto;

import co.edu.usco.aprendizaje_usco.modulos.contenido.dto.CompetenciaResponse;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.TipoEjercicio;
import java.util.List;

public record EjercicioPracticaResponse(
        Long id,
        String codigo,
        String enunciado,
        TipoEjercicio tipo,
        int nivel,
        CompetenciaResponse competencia,
        List<OpcionPracticaResponse> opciones,
        String pista) {
}
