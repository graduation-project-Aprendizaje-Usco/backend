package co.edu.usco.aprendizaje_usco.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;

/**
 * Formato único de error de la API.
 *
 * <pre>
 * {
 *   "timestamp": "2026-10-08T14:30:00Z",
 *   "status": 400,
 *   "message": "La solicitud tiene campos inválidos",
 *   "path": "/api/auth/registro",
 *   "errores": [ { "campo": "correo", "mensaje": "debe ser una dirección de correo válida" } ]
 * }
 * </pre>
 *
 * "errores" solo aparece cuando hay errores de validación.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(

        String timestamp,
        int status,
        String message,
        String path,
        List<CampoError> errores ) {

    public static ErrorResponse of(HttpStatus status, String message, String path) {
    return of(status, message, path, List.of());
    }

    public static ErrorResponse of(HttpStatus status, String message, String path, List<CampoError> errores) {
        return new ErrorResponse(Instant.now().toString(), status.value(), message, path, errores);
    }

    /**
     * Error de un campo concreto. A propósito NO incluye el valor rechazado,
     * para no devolver contraseñas u otros datos sensibles en la respuesta.
     */
    public record CampoError(String campo, String mensaje) {
    }

}
