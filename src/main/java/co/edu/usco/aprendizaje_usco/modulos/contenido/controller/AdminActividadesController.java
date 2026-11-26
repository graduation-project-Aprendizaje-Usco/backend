package co.edu.usco.aprendizaje_usco.modulos.contenido.controller;

import co.edu.usco.aprendizaje_usco.common.error.ErrorResponse;
import co.edu.usco.aprendizaje_usco.modulos.contenido.dto.AdminActividadesResponse;
import co.edu.usco.aprendizaje_usco.modulos.contenido.service.AdminActividadesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/actividades")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administración de actividades",
        description = "Catálogo de contenidos y ejercicios para el panel del administrador (solo ADMIN)")
@ApiResponse(responseCode = "401", description = "No autenticado",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
@ApiResponse(responseCode = "403", description = "Solo el administrador",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public class AdminActividadesController {

    private final AdminActividadesService adminActividadesService;

    public AdminActividadesController(AdminActividadesService adminActividadesService) {
        this.adminActividadesService = adminActividadesService;
    }

    @GetMapping
    @Operation(summary = "Listar contenidos y ejercicios de todos los docentes",
            description = "Vista de solo lectura: no incluye respuestas correctas, pistas ni explicaciones.")
    @ApiResponse(responseCode = "200", description = "Catálogo completo")
    public AdminActividadesResponse listar() {
        return adminActividadesService.listar();
    }
}
