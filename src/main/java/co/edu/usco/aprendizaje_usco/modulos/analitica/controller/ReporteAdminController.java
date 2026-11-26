package co.edu.usco.aprendizaje_usco.modulos.analitica.controller;

import co.edu.usco.aprendizaje_usco.modulos.analitica.dto.ReporteAdminResponse;
import co.edu.usco.aprendizaje_usco.modulos.analitica.service.ReporteAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reportes")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Reportes administrativos")
public class ReporteAdminController {

    private final ReporteAdminService reporteService;

    public ReporteAdminController(ReporteAdminService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping
    @Operation(summary = "Reporte agregado de actividad y diagnósticos",
            description = "Conteos del sistema y métricas agregadas filtrables por fechas. No devuelve datos personales.")
    public ReporteAdminResponse reporte(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.generar(desde, hasta);
    }
}
