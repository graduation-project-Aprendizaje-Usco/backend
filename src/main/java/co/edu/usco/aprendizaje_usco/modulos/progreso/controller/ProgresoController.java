package co.edu.usco.aprendizaje_usco.modulos.progreso.controller;

import co.edu.usco.aprendizaje_usco.modulos.progreso.dto.ProgresoResponse;
import co.edu.usco.aprendizaje_usco.modulos.progreso.service.ProgresoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/progreso")
@PreAuthorize("hasRole('ESTUDIANTE')")
@Tag(name = "Progreso del estudiante", description = "Indicadores de progreso academico (solo estudiantes)")
public class ProgresoController {

    private final ProgresoService progresoService;

    public ProgresoController(ProgresoService progresoService) {
        this.progresoService = progresoService;
    }

    @GetMapping("/me")
    @Operation(summary = "Indicadores de progreso academico del estudiante autenticado")
    public ProgresoResponse miProgreso(@AuthenticationPrincipal Jwt jwt) {
        return progresoService.obtenerProgreso(usuarioId(jwt));
    }

    private static Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

}
