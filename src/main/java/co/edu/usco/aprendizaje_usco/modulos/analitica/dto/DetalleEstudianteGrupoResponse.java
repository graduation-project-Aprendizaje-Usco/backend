package co.edu.usco.aprendizaje_usco.modulos.analitica.dto;

import java.util.List;

public record DetalleEstudianteGrupoResponse(
        EstudianteGrupoResponse estudiante,
        List<DimensionEstudianteResponse> dimensiones,
        DiagnosticoEstudianteResponse ultimoDiagnostico) {
}
