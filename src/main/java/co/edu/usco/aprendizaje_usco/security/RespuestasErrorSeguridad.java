package co.edu.usco.aprendizaje_usco.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Respuestas 401 y 403 que genera Spring Security ANTES de llegar a los
 * controladores (petición sin token, token inválido, rol sin permiso en una
 * regla de SecurityConfig). Usan el mismo formato que ErrorResponse para que
 * el frontend maneje todos los errores igual.
 *
 * El JSON se escribe a mano a propósito: así esta clase no depende de la
 * versión de Jackson (Spring Boot 4 usa Jackson 3).
 */
@Component
public class RespuestasErrorSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    /** 401: no hay token, o es inválido o está vencido. */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        escribir(response, request, HttpStatus.UNAUTHORIZED, "Debes iniciar sesión para continuar");
    }

    /** 403: hay token válido pero el rol no tiene permiso. */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        escribir(response, request, HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta acción");
    }

    private void escribir(HttpServletResponse response, HttpServletRequest request,
                          HttpStatus status, String mensaje) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String json = "{\"timestamp\":\"" + Instant.now() + "\","
                + "\"status\":" + status.value() + ","
                + "\"message\":\"" + escapar(mensaje) + "\","
                + "\"path\":\"" + escapar(request.getRequestURI()) + "\"}";
        response.getWriter().write(json);
    }

    private static String escapar(String texto) {
        StringBuilder sb = new StringBuilder(texto.length());
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

}
