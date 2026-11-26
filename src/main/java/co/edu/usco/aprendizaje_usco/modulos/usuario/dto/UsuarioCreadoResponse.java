package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

/** Respuesta de POST /api/admin/usuarios; nunca contiene contraseñas ni tokens. */
public record UsuarioCreadoResponse(UsuarioAdminResponse usuario) {
}
