package co.edu.usco.aprendizaje_usco.modulos.usuario.controller;

import co.edu.usco.aprendizaje_usco.common.error.ErrorResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.InvitacionLoteRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.InvitacionResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.service.CorreoInvitacionService;
import co.edu.usco.aprendizaje_usco.modulos.usuario.service.InvitacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Invitaciones de estudiantes por grupo (#152/#153/#155). */
@RestController
@RequestMapping("/api/grupos/{grupoId}/invitaciones")
@PreAuthorize("hasAnyRole('DOCENTE','ADMIN')")
@Tag(name = "Invitaciones", description = "Carga, estado y reenvío de invitaciones de estudiantes")
public class InvitacionController {

    private final InvitacionService invitacionService;
    private final CorreoInvitacionService correoService;

    public InvitacionController(InvitacionService invitacionService,
                                CorreoInvitacionService correoService) {
        this.invitacionService = invitacionService;
        this.correoService = correoService;
    }

    @GetMapping
    @Operation(summary = "Listar invitaciones del grupo", description = "Solo el docente dueño o ADMIN.")
    public List<InvitacionResponse> listar(@PathVariable Long grupoId,
                                           @AuthenticationPrincipal Jwt jwt) {
        return invitacionService.listar(grupoId, usuarioId(jwt), esAdministrador(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cargar el listado de estudiantes e invitarlos",
            description = "Cada fila se valida antes de guardar. Se envía un enlace de un solo uso.")
    @ApiResponse(responseCode = "201", description = "Invitaciones creadas")
    @ApiResponse(responseCode = "409", description = "El correo ya tiene cuenta o la invitación ya se usó",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "Correo o nombres inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "Resend no está configurado o no pudo aceptar el correo",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public List<InvitacionResponse> cargar(@PathVariable Long grupoId,
                                            @Valid @RequestBody InvitacionLoteRequest request,
                                            @AuthenticationPrincipal Jwt jwt) {
        List<InvitacionService.InvitacionCreada> creadas = invitacionService.crearLote(
                grupoId, usuarioId(jwt), esAdministrador(jwt), request.estudiantes());
        for (InvitacionService.InvitacionCreada creada : creadas) {
            correoService.enviar(creada.invitacion().correo(), creada.enlace());
        }
        return creadas.stream().map(InvitacionService.InvitacionCreada::invitacion).toList();
    }

    @PostMapping("/{invitacionId}/reenviar")
    @Operation(summary = "Reenviar una invitación", description = "Genera un token nuevo y vuelve a enviar el enlace.")
    @ApiResponse(responseCode = "503", description = "Resend no está configurado o no pudo aceptar el correo",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public InvitacionResponse reenviar(@PathVariable Long grupoId, @PathVariable Long invitacionId,
                                       @AuthenticationPrincipal Jwt jwt) {
        InvitacionService.InvitacionCreada creada = invitacionService.reenviar(
                grupoId, invitacionId, usuarioId(jwt), esAdministrador(jwt));
        correoService.enviar(creada.invitacion().correo(), creada.enlace());
        return creada.invitacion();
    }

    private static Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    private static boolean esAdministrador(Jwt jwt) {
        return "ADMIN".equals(jwt.getClaimAsString("rol"));
    }
}
