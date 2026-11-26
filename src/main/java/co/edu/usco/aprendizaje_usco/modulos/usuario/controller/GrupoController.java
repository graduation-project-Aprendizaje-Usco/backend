package co.edu.usco.aprendizaje_usco.modulos.usuario.controller;

import co.edu.usco.aprendizaje_usco.common.error.ErrorResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.EstudianteGrupoResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.GrupoRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.GrupoResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.InscripcionEstudianteRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.service.GrupoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grupos")
@Tag(name = "Grupos académicos", description = "Grupos docentes e inscripción de estudiantes")
public class GrupoController {

    private final GrupoService grupoService;

    public GrupoController(GrupoService grupoService) {
        this.grupoService = grupoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCENTE')")
    @Operation(summary = "Crear un grupo propio",
            description = "El docente autenticado queda asignado como responsable; no se recibe un id de docente.")
    @ApiResponse(responseCode = "201", description = "Grupo creado")
    @ApiResponse(responseCode = "401", description = "No autenticado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Solo los docentes pueden crear grupos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "El código de grupo ya existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<GrupoResponse> crear(@Valid @RequestBody GrupoRequest request,
                                                @AuthenticationPrincipal Jwt jwt) {
        GrupoResponse grupo = grupoService.crear(usuarioId(jwt), request);
        return ResponseEntity.created(URI.create("/api/grupos/" + grupo.id())).body(grupo);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCENTE','ADMIN')")
    @Operation(summary = "Consultar grupos autorizados",
            description = "El docente solo ve sus grupos; el administrador consulta todos.")
    @ApiResponse(responseCode = "200", description = "Lista de grupos")
    @ApiResponse(responseCode = "401", description = "No autenticado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Rol no autorizado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public List<GrupoResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return grupoService.listar(usuarioId(jwt), esAdministrador(jwt));
    }

    @GetMapping("/{grupoId}/estudiantes")
    @PreAuthorize("hasAnyRole('DOCENTE','ADMIN')")
    @Operation(summary = "Consultar estudiantes de un grupo",
            description = "El docente solo puede consultar estudiantes de sus propios grupos; ADMIN puede consultar cualquiera.")
    @ApiResponse(responseCode = "200", description = "Estudiantes inscritos")
    @ApiResponse(responseCode = "403", description = "El grupo pertenece a otro docente",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "El grupo no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public List<EstudianteGrupoResponse> estudiantes(@PathVariable Long grupoId,
                                                      @AuthenticationPrincipal Jwt jwt) {
        return grupoService.estudiantes(grupoId, usuarioId(jwt), esAdministrador(jwt));
    }

    @PostMapping("/{grupoId}/estudiantes")
    @PreAuthorize("hasAnyRole('DOCENTE','ADMIN')")
    @Operation(summary = "Inscribir un estudiante en un grupo",
            description = "El grupo se identifica por la ruta y el estudiante por correo. "
                    + "Un docente solo puede inscribir en sus grupos; ADMIN puede inscribir en cualquiera.")
    @ApiResponse(responseCode = "201", description = "Estudiante inscrito")
    @ApiResponse(responseCode = "400", description = "Correo inválido",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "El grupo pertenece a otro docente",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Grupo o estudiante no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "El estudiante ya está inscrito",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "El usuario no es un estudiante activo",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<EstudianteGrupoResponse> inscribir(@PathVariable Long grupoId,
            @Valid @RequestBody InscripcionEstudianteRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        EstudianteGrupoResponse estudiante = grupoService.inscribir(
                grupoId, usuarioId(jwt), esAdministrador(jwt), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(estudiante);
    }

    private static Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    private static boolean esAdministrador(Jwt jwt) {
        return "ADMIN".equals(jwt.getClaimAsString("rol"));
    }
}
