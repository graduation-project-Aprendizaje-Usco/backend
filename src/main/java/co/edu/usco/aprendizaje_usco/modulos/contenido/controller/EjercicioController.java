package co.edu.usco.aprendizaje_usco.modulos.contenido.controller;

import co.edu.usco.aprendizaje_usco.modulos.contenido.dto.EjercicioRequest;
import co.edu.usco.aprendizaje_usco.modulos.contenido.dto.EjercicioResponse;
import co.edu.usco.aprendizaje_usco.modulos.contenido.dto.EstadoRequest;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.TipoEjercicio;
import co.edu.usco.aprendizaje_usco.modulos.contenido.service.EjercicioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ejercicios")
@PreAuthorize("hasRole('DOCENTE')")
@Tag(name = "Ejercicios", description = "Gestión de ejercicios propios del docente")
public class EjercicioController {

    private final EjercicioService ejercicioService;

    public EjercicioController(EjercicioService ejercicioService) {
        this.ejercicioService = ejercicioService;
    }

    @GetMapping
    @Operation(summary = "Listar ejercicios del docente")
    public List<EjercicioResponse> listar(
            @RequestParam(required = false) Long contenidoId,
            @RequestParam(required = false) String competencia,
            @RequestParam(required = false) Integer nivel,
            @RequestParam(required = false) TipoEjercicio tipo,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String q,
            @AuthenticationPrincipal Jwt jwt) {
        return ejercicioService.listar(usuarioId(jwt), contenidoId, competencia, nivel, tipo, activo, q);
    }

    @PostMapping
    @Operation(summary = "Crear un ejercicio en un contenido propio")
    public ResponseEntity<EjercicioResponse> crear(@Valid @RequestBody EjercicioRequest request,
                                                    @AuthenticationPrincipal Jwt jwt) {
        EjercicioResponse ejercicio = ejercicioService.crear(usuarioId(jwt), request);
        return ResponseEntity.created(URI.create("/api/ejercicios/" + ejercicio.id())).body(ejercicio);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar un ejercicio propio")
    public EjercicioResponse actualizar(@PathVariable Long id,
                                        @Valid @RequestBody EjercicioRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        return ejercicioService.actualizar(usuarioId(jwt), id, request);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar un ejercicio propio")
    public EjercicioResponse cambiarEstado(@PathVariable Long id,
                                           @Valid @RequestBody EstadoRequest request,
                                           @AuthenticationPrincipal Jwt jwt) {
        return ejercicioService.cambiarEstado(usuarioId(jwt), id, request.activo());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un ejercicio sin intentos")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        ejercicioService.eliminar(usuarioId(jwt), id);
        return ResponseEntity.noContent().build();
    }

    private static Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
