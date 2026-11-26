package co.edu.usco.aprendizaje_usco.modulos.progreso.controller;

import co.edu.usco.aprendizaje_usco.common.error.ErrorResponse;
import co.edu.usco.aprendizaje_usco.modulos.progreso.dto.HistorialItemResponse;
import co.edu.usco.aprendizaje_usco.modulos.progreso.dto.PaginaResponse;
import co.edu.usco.aprendizaje_usco.modulos.progreso.model.TipoActividad;
import co.edu.usco.aprendizaje_usco.modulos.progreso.service.HistorialService;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Historial de resultados (SCRUM-41, RF-15). */
@RestController
@RequestMapping("/api/historial")
@Tag(name = "Historial", description = "Diagnósticos e intentos de práctica de un estudiante")
public class HistorialController {

    private final HistorialService historialService;

    public HistorialController(HistorialService historialService) {
        this.historialService = historialService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ESTUDIANTE','DOCENTE')")
    @Operation(summary = "Consultar el historial",
            description = "Del más reciente al más antiguo, 20 por página. El estudiante ve el suyo; el docente "
                    + "indica estudianteId y solo puede consultar estudiantes de sus grupos.")
    @ApiResponse(responseCode = "200", description = "Página del historial")
    @ApiResponse(responseCode = "400", description = "Parámetro con formato inválido (tipo, fecha o página)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Historial de otro estudiante, o de un estudiante fuera de los grupos del docente",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "Docente sin estudianteId, o 'desde' posterior a 'hasta'",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PaginaResponse<HistorialItemResponse> consultar(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "DIAGNOSTICO o PRACTICA") @RequestParam(required = false) TipoActividad tipo,
            @Parameter(description = "ALGEBRAICA, LOGICA o FUNCIONAL") @RequestParam(required = false) String competencia,
            @Parameter(description = "Desde (incluido), AAAA-MM-DD")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @Parameter(description = "Hasta (incluido), AAAA-MM-DD")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @Parameter(description = "Página, desde 0") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Solo docentes: estudiante a consultar") @RequestParam(required = false) Long estudianteId) {
        return historialService.consultar(Long.parseLong(jwt.getSubject()),
                NombreRol.valueOf(jwt.getClaimAsString(JwtService.CLAIM_ROL)),
                estudianteId, tipo, competencia, desde, hasta, page);
    }

}
