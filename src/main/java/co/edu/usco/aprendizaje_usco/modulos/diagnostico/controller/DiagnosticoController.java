package co.edu.usco.aprendizaje_usco.modulos.diagnostico.controller;

import co.edu.usco.aprendizaje_usco.common.error.ErrorResponse;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.dto.IntentoResponse;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.dto.RespuestaGuardadaResponse;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.dto.RespuestaRequest;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.dto.ResultadoResponse;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.service.DiagnosticoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Bloque 4: el estudiante presenta la prueba diagnóstica.
 * El estudiante siempre es el dueño del token (claim "sub"): nunca se recibe un
 * id de usuario desde el cliente.
 */
@RestController
@RequestMapping("/api/diagnosticos/intentos")
@PreAuthorize("hasRole('ESTUDIANTE')")
@Tag(name = "Prueba diagnóstica", description = "Iniciar, responder y finalizar la prueba diagnóstica (solo estudiantes)")
public class DiagnosticoController {

    private final DiagnosticoService diagnosticoService;

    public DiagnosticoController(DiagnosticoService diagnosticoService) {
        this.diagnosticoService = diagnosticoService;
    }

    @PostMapping
    @Operation(summary = "Iniciar o retomar la prueba",
            description = "Crea el intento en la prueba activa (201). Si el estudiante ya tiene uno, "
                    + "en curso o finalizado, lo devuelve (200): cada prueba se presenta una sola vez.")
    @ApiResponse(responseCode = "201", description = "Intento creado")
    @ApiResponse(responseCode = "200", description = "El estudiante ya tenía un intento")
    @ApiResponse(responseCode = "404", description = "No hay una prueba activa",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<IntentoResponse> iniciar(@AuthenticationPrincipal Jwt jwt) {
        DiagnosticoService.Inicio inicio = diagnosticoService.iniciar(usuarioId(jwt));
        if (inicio.creado()) {
            return ResponseEntity.created(URI.create("/api/diagnosticos/intentos/" + inicio.intento().id()))
                    .body(inicio.intento());
        }
        return ResponseEntity.ok(inicio.intento());
    }

    @GetMapping("/actual")
    @Operation(summary = "Mi intento en la prueba activa",
            description = "200 con el intento, o 204 si el estudiante todavía no ha iniciado la prueba.")
    @ApiResponse(responseCode = "200", description = "Intento del estudiante")
    @ApiResponse(responseCode = "204", description = "Aún no ha iniciado la prueba", content = @Content)
    public ResponseEntity<IntentoResponse> actual(@AuthenticationPrincipal Jwt jwt) {
        return diagnosticoService.actual(usuarioId(jwt))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un intento",
            description = "Si está en curso trae las preguntas (sin claves) y las respuestas guardadas; "
                    + "si está finalizado trae el resultado. Si el tiempo ya se acabó, lo finaliza en ese momento.")
    @ApiResponse(responseCode = "200", description = "Intento")
    @ApiResponse(responseCode = "403", description = "El intento es de otro estudiante",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "El intento no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public IntentoResponse obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return diagnosticoService.obtener(id, usuarioId(jwt));
    }

    @GetMapping("/{id}/resultado")
    @Operation(summary = "Resultado de un intento",
            description = "Puntaje total, categoría y nivel por competencia. Solo existe cuando el intento está finalizado.")
    @ApiResponse(responseCode = "200", description = "Resultado del intento")
    @ApiResponse(responseCode = "403", description = "El intento es de otro estudiante",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "El intento no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "La prueba aún no ha finalizado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResultadoResponse resultado(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return diagnosticoService.resultado(id, usuarioId(jwt));
    }

    @PutMapping("/{id}/respuestas/{itemId}")
    @Operation(summary = "Responder (o cambiar la respuesta de) una pregunta")
    @ApiResponse(responseCode = "200", description = "Respuesta guardada")
    @ApiResponse(responseCode = "400", description = "Falta opcionId",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "El intento es de otro estudiante",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "El intento o la pregunta no existen",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "El intento ya está finalizado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "Se acabó el tiempo, o la opción no es de esa pregunta",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public RespuestaGuardadaResponse responder(@PathVariable Long id, @PathVariable Long itemId,
                                               @Valid @RequestBody RespuestaRequest request,
                                               @AuthenticationPrincipal Jwt jwt) {
        return diagnosticoService.responder(id, itemId, request.opcionId(), usuarioId(jwt));
    }

    @PostMapping("/{id}/finalizar")
    @Operation(summary = "Entregar la prueba",
            description = "Califica y devuelve el resultado. Si ya estaba finalizada, devuelve el mismo resultado.")
    @ApiResponse(responseCode = "200", description = "Intento finalizado con su resultado")
    @ApiResponse(responseCode = "403", description = "El intento es de otro estudiante",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "El intento no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public IntentoResponse finalizar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return diagnosticoService.finalizar(id, usuarioId(jwt));
    }

    private static Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

}
