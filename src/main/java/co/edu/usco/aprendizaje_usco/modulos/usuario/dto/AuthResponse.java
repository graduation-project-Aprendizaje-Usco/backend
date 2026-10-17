package co.edu.usco.aprendizaje_usco.modulos.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Respuesta de POST /api/auth/login (contrato de SCRUM-81). */
public record AuthResponse(

        @Schema(description = "JWT para el header Authorization", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token,

        @Schema(example = "Bearer")
        String tipo,

        @Schema(description = "Segundos que faltan para que el token venza", example = "7200")
        long expiraEn,

        UsuarioResponse usuario) {

    public static final String TIPO_BEARER = "Bearer";

}
