package co.edu.usco.aprendizaje_usco.modulos.analitica.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReporteGrupoResponse(
        GrupoInfoResponse grupo,
        long totalEstudiantes,
        long estudiantesConActividad,
        BigDecimal promedioGeneral,
        List<DimensionGrupoResponse> dimensiones,
        List<DistribucionCategoriaResponse> distribucionCategorias,
        long estudiantesEnRiesgo,
        List<EstudianteReporteResponse> estudiantes,
        List<EjercicioErroresResponse> topEjerciciosErrores,
        List<EvolucionSemanalResponse> evolucionGeneral) {
}
