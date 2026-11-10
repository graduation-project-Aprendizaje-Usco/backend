package co.edu.usco.aprendizaje_usco.modulos.contenido.controller;

import co.edu.usco.aprendizaje_usco.modulos.contenido.dto.ContenidoRequest;
import co.edu.usco.aprendizaje_usco.modulos.contenido.dto.ContenidoResponse;
import co.edu.usco.aprendizaje_usco.modulos.contenido.dto.EstadoRequest;
import co.edu.usco.aprendizaje_usco.modulos.contenido.service.ContenidoService;
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
@RequestMapping("/api/contenidos")
@PreAuthorize("hasRole('DOCENTE')")
@Tag(name = "Contenidos", description = "Gestión de contenidos propios del docente")
public class ContenidoController {

    private final ContenidoService contenidoService;

    public ContenidoController(ContenidoService contenidoService) {
        this.contenidoService = contenidoService;
    }

    @GetMapping
    @Operation(summary = "Listar contenidos del docente")
    public List<ContenidoResponse> listar(
            @RequestParam(required = false) String asignatura,
            @RequestParam(required = false) String competencia,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String q,
            @AuthenticationPrincipal Jwt jwt) {
        return contenidoService.listar(usuarioId(jwt), asignatura, competencia, activo, q);
    }

    @PostMapping
    @Operation(summary = "Crear un contenido")
    public ResponseEntity<ContenidoResponse> crear(@Valid @RequestBody ContenidoRequest request,
                                                    @AuthenticationPrincipal Jwt jwt) {
        ContenidoResponse contenido = contenidoService.crear(usuarioId(jwt), request);
        return ResponseEntity.created(URI.create("/api/contenidos/" + contenido.id())).body(contenido);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar tema, título y descripción de un contenido propio")
    public ContenidoResponse actualizar(@PathVariable Long id,
                                        @Valid @RequestBody ContenidoRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        return contenidoService.actualizar(usuarioId(jwt), id, request);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar un contenido propio")
    public ContenidoResponse cambiarEstado(@PathVariable Long id,
                                           @Valid @RequestBody EstadoRequest request,
                                           @AuthenticationPrincipal Jwt jwt) {
        return contenidoService.cambiarEstado(usuarioId(jwt), id, request.activo());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un contenido sin ejercicios")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        contenidoService.eliminar(usuarioId(jwt), id);
        return ResponseEntity.noContent().build();
    }

    private static Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
