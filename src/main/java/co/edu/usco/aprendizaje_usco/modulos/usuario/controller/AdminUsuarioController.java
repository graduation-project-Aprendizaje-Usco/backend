package co.edu.usco.aprendizaje_usco.modulos.usuario.controller;

import co.edu.usco.aprendizaje_usco.common.error.ErrorResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.ActualizarUsuarioRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.EstadoUsuarioRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.UsuarioAdminRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.UsuarioAdminResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.UsuarioCreadoResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.EstadoUsuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.service.AdminUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
@RequestMapping("/api/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administración de usuarios", description = "Crear, editar, cambiar el rol y desactivar usuarios (solo ADMIN)")
@ApiResponse(responseCode = "401", description = "No autenticado",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
@ApiResponse(responseCode = "403", description = "Solo el administrador",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public class AdminUsuarioController {

    private final AdminUsuarioService adminUsuarioService;

    public AdminUsuarioController(AdminUsuarioService adminUsuarioService) {
        this.adminUsuarioService = adminUsuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios",
            description = "Filtros opcionales: texto en nombres, apellidos o correo; rol; estado.")
    public List<UsuarioAdminResponse> listar(@RequestParam(required = false) String buscar,
                                             @RequestParam(required = false) NombreRol rol,
                                             @RequestParam(required = false) EstadoUsuario estado) {
        return adminUsuarioService.listar(buscar, rol, estado);
    }

    @PostMapping
    @Operation(summary = "Crear un usuario",
            description = "Envía un enlace de un solo uso para que la persona configure su contraseña.")
    @ApiResponse(responseCode = "201", description = "Usuario creado")
    @ApiResponse(responseCode = "409", description = "El correo ya está registrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "Estudiante sin correo de estudiante o sin programa",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "Resend no está configurado o no pudo aceptar el correo",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<UsuarioCreadoResponse> crear(@Valid @RequestBody UsuarioAdminRequest request) {
        UsuarioCreadoResponse creado = adminUsuarioService.crear(request);
        return ResponseEntity.created(URI.create("/api/admin/usuarios/" + creado.usuario().id())).body(creado);
    }

    @PostMapping("/{id}/reenviar-activacion")
    @Operation(summary = "Reenviar el enlace para configurar la contraseña",
            description = "Solo está disponible mientras la persona no haya definido su contraseña.")
    @ApiResponse(responseCode = "204", description = "Correo enviado")
    @ApiResponse(responseCode = "404", description = "El usuario no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "La cuenta ya configuró su contraseña",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "Resend no está configurado o no pudo aceptar el correo",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> reenviarActivacion(@PathVariable Long id) {
        adminUsuarioService.reenviarActivacion(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar nombres, apellidos, rol y programa")
    @ApiResponse(responseCode = "404", description = "El usuario no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "El administrador intenta quitarse su propio rol",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public UsuarioAdminResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarUsuarioRequest request,
                                           @AuthenticationPrincipal Jwt jwt) {
        return adminUsuarioService.actualizar(usuarioId(jwt), id, request);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar un usuario",
            description = "Un usuario INACTIVO no puede iniciar sesión. El administrador no puede desactivarse a sí mismo.")
    @ApiResponse(responseCode = "404", description = "El usuario no existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "El administrador intenta desactivarse a sí mismo",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public UsuarioAdminResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoUsuarioRequest request,
                                              @AuthenticationPrincipal Jwt jwt) {
        return adminUsuarioService.cambiarEstado(usuarioId(jwt), id, request.estado());
    }

    private static Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
