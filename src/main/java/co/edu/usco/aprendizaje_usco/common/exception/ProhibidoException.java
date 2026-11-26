package co.edu.usco.aprendizaje_usco.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 403: el usuario inició sesión y tiene el rol correcto, pero el recurso no es suyo.
 * Ejemplo: un estudiante intenta ver o responder el intento de otro estudiante.
 * (Los 403 por rol los produce @PreAuthorize, no esta excepción.)
 */
public class ProhibidoException extends AplicacionException {

    public ProhibidoException(String mensaje) {
        super(HttpStatus.FORBIDDEN, mensaje);
    }

}
