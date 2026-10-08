package co.edu.usco.aprendizaje_usco.common.error;


import co.edu.usco.aprendizaje_usco.common.error.ErrorResponse.CampoError;
import co.edu.usco.aprendizaje_usco.common.exception.AplicacionException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Traduce cualquier excepción lanzada desde un controlador al formato
 * estándar {@link ErrorResponse}.
 *
 * Regla de oro: en los 500 nunca se devuelve el mensaje ni la traza de la
 * excepción al cliente; se registran en el log del servidor.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------------------------------------------------------------- 400

    /** @Valid sobre un @RequestBody: devuelve la lista de campos inválidos. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacionDto(MethodArgumentNotValidException ex,
                                                              HttpServletRequest request) {
        List<CampoError> errores = new ArrayList<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errores.add(new CampoError(e.getField(), e.getDefaultMessage())));
        ex.getBindingResult().getGlobalErrors()
                .forEach(e -> errores.add(new CampoError(e.getObjectName(), e.getDefaultMessage())));
        return responder(HttpStatus.BAD_REQUEST, "La solicitud tiene campos inválidos", request, errores);
    }

    /** Validaciones sobre @PathVariable / @RequestParam (ej. @Min en un id). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> manejarValidacionParametros(HandlerMethodValidationException ex,
                                                                     HttpServletRequest request) {
        List<CampoError> errores = new ArrayList<>();
        ex.getParameterValidationResults().forEach(resultado -> {
            String parametro = resultado.getMethodParameter().getParameterName();
            resultado.getResolvableErrors()
                    .forEach(e -> errores.add(new CampoError(parametro, e.getDefaultMessage())));
        });
        return responder(HttpStatus.BAD_REQUEST, "La solicitud tiene parámetros inválidos", request, errores);
    }

    /** Validaciones con @Validated en servicios. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> manejarRestricciones(ConstraintViolationException ex,
                                                              HttpServletRequest request) {
        List<CampoError> errores = ex.getConstraintViolations().stream()
                .map(v -> new CampoError(ultimoNodo(v.getPropertyPath().toString()), v.getMessage()))
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "La solicitud tiene datos inválidos", request, errores);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarCuerpoIlegible(HttpMessageNotReadableException ex,
                                                               HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es un JSON válido", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> manejarTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                             HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getName() + "' tiene un formato inválido", request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> manejarParametroFaltante(MissingServletRequestParameterException ex,
                                                                  HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
                "Falta el parámetro obligatorio '" + ex.getParameterName() + "'", request);
    }

    // ---------------------------------------------------------------- 401 / 403
    // Si la seguridad falla dentro del controlador (ej. @PreAuthorize), la excepción
    // llega aquí; sin estos métodos terminaría en el 500 genérico de abajo.

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> manejarNoAutenticado(AuthenticationException ex,
                                                              HttpServletRequest request) {
        return responder(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión para continuar", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> manejarAccesoDenegado(AccessDeniedException ex,
                                                               HttpServletRequest request) {
        return responder(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta acción", request);
    }

    // ---------------------------------------------------------------- 404 / 405

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> manejarEntidadNoEncontrada(EntityNotFoundException ex,
                                                                    HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, "El recurso solicitado no existe", request);
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ErrorResponse> manejarRutaInexistente(Exception ex, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, "La ruta solicitada no existe", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> manejarMetodoNoPermitido(HttpRequestMethodNotSupportedException ex,
                                                                  HttpServletRequest request) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED,
                "El método " + ex.getMethod() + " no está permitido en esta ruta", request);
    }

    // ---------------------------------------------------------------- 409

    /** Violación de UNIQUE, FK o CHECK en la base. El detalle SQL solo va al log. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> manejarIntegridad(DataIntegrityViolationException ex,
                                                           HttpServletRequest request) {
        log.warn("Violación de integridad en {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT,
                "La operación entra en conflicto con datos existentes", request);
    }

    // ---------------------------------------------------------------- Propias

    /** RecursoNoEncontrado (404), Conflicto (409) y ReglaNegocio (422). */
    @ExceptionHandler(AplicacionException.class)
    public ResponseEntity<ErrorResponse> manejarAplicacion(AplicacionException ex, HttpServletRequest request) {
        return responder(ex.getEstado(), ex.getMessage(), request);
    }

    // ---------------------------------------------------------------- 500

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarInesperado(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado en {} {}", request.getMethod(), request.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado. Intenta de nuevo más tarde.", request);
    }

    // ---------------------------------------------------------------- utilidades

    private ResponseEntity<ErrorResponse> responder(HttpStatus status, String mensaje, HttpServletRequest request) {
        return responder(status, mensaje, request, List.of());
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus status, String mensaje, HttpServletRequest request,
                                                    List<CampoError> errores) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(status, mensaje, request.getRequestURI(), errores));
    }

    /** "registrar.dto.correo" → "correo". */
    private static String ultimoNodo(String ruta) {
        int punto = ruta.lastIndexOf('.');
        return punto >= 0 ? ruta.substring(punto + 1) : ruta;
    }

}
