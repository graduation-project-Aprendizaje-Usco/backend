package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

/**
 * Respuesta de POST /api/admin/usuarios. La contraseña temporal solo viaja en
 * esta respuesta: no se guarda en claro ni se puede volver a consultar.
 */
public record UsuarioCreadoResponse(UsuarioAdminResponse usuario, String passwordTemporal) {

    /** Sin la contraseña a propósito: el toString de un record termina en logs. */
    @Override
    public String toString() {
        return "UsuarioCreadoResponse{usuario=" + usuario.id() + "}";
    }
}
