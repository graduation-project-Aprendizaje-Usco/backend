package co.edu.usco.aprendizaje_usco.modulos.analitica.controller;

import co.edu.usco.aprendizaje_usco.modulos.analitica.dto.DetalleEstudianteGrupoResponse;
import co.edu.usco.aprendizaje_usco.modulos.analitica.dto.ResumenGrupoResponse;
import co.edu.usco.aprendizaje_usco.modulos.analitica.service.IndicadoresGrupoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Indicadores de grupo para el docente (RF-10, SCRUM-39). */
@RestController
@RequestMapping("/api/analitica/grupos")
@PreAuthorize("hasRole('DOCENTE')")
@Tag(name = "Analítica de grupos", description = "Indicadores de desempeño de los grupos del docente")
public class AnaliticaGrupoController {

    private final IndicadoresGrupoService indicadoresService;

    public AnaliticaGrupoController(IndicadoresGrupoService indicadoresService) {
        this.indicadoresService = indicadoresService;
    }

    @GetMapping("/{grupoId}")
    @Operation(summary = "Resumen de indicadores de un grupo",
            description = "Promedio general y por dimensión, distribución de categorías, estudiantes en riesgo "
                    + "y tabla de estudiantes. Solo el docente dueño del grupo puede consultarlo.")
    public ResumenGrupoResponse resumen(
            @PathVariable Long grupoId,
            @Parameter(description = "ALGEBRAICA, LOGICA o FUNCIONAL")
            @RequestParam(required = false) String competencia,
            @Parameter(description = "Desde (incluido), AAAA-MM-DD")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @Parameter(description = "Hasta (incluido), AAAA-MM-DD")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @AuthenticationPrincipal Jwt jwt) {
        return indicadoresService.resumen(usuarioId(jwt), grupoId, competencia, desde, hasta);
    }

    @GetMapping("/{grupoId}/estudiantes/{estudianteId}")
    @Operation(summary = "Detalle de un estudiante del grupo",
            description = "Métricas de práctica por dimensión y último diagnóstico del estudiante, "
                    + "siempre dentro de un grupo del docente autenticado.")
    public DetalleEstudianteGrupoResponse detalle(
            @PathVariable Long grupoId,
            @PathVariable Long estudianteId,
            @Parameter(description = "ALGEBRAICA, LOGICA o FUNCIONAL")
            @RequestParam(required = false) String competencia,
            @Parameter(description = "Desde (incluido), AAAA-MM-DD")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @Parameter(description = "Hasta (incluido), AAAA-MM-DD")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @AuthenticationPrincipal Jwt jwt) {
        return indicadoresService.detalle(usuarioId(jwt), grupoId, estudianteId, competencia, desde, hasta);
    }

    private static Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
