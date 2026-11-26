package co.edu.usco.aprendizaje_usco.common.exception;

import org.springframework.http.HttpStatus;

/** 503: un servicio requerido para completar la operación no está disponible. */
public class ServicioNoDisponibleException extends AplicacionException {

    public ServicioNoDisponibleException(String mensaje) {
        super(HttpStatus.SERVICE_UNAVAILABLE, mensaje);
    }
}
