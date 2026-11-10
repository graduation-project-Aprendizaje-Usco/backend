package co.edu.usco.aprendizaje_usco.modulos.usuario.controller;

import co.edu.usco.aprendizaje_usco.common.error.ErrorResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.ResumenAdminResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.service.AdminUsuarioService;
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
@RequestMapping("/api/admin/resumen")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administración de usuarios")
public class AdminResumenController {

    private final AdminUsuarioService adminUsuarioService;

    public AdminResumenController(AdminUsuarioService adminUsuarioService) {
        this.adminUsuarioService = adminUsuarioService;
    }

    @GetMapping
    @Operation(summary = "Conteos del panel del administrador",
            description = "Usuarios por rol (total y activos), grupos, contenidos activos y ejercicios activos.")
    @ApiResponse(responseCode = "200", description = "Conteos")
    @ApiResponse(responseCode = "403", description = "Solo el administrador",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResumenAdminResponse resumen() {
        return adminUsuarioService.resumen();
    }
}
