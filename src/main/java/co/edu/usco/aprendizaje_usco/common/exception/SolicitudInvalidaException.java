package co.edu.usco.aprendizaje_usco.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 400: parametros o combinaciones de datos que no respetan el contrato.
 */
public class SolicitudInvalidaException extends AplicacionException {

    public SolicitudInvalidaException(String mensaje) {
        super(HttpStatus.BAD_REQUEST, mensaje);
    }

}
