package co.edu.usco.aprendizaje_usco.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 409: la operación choca con el estado actual de los datos.
 * Ejemplos: correo ya registrado, intento duplicado, intento ya finalizado.
 */

public class ConflictoException extends AplicacionException {

    public ConflictoException(String mensaje) {
        super(HttpStatus.CONFLICT, mensaje);
    }

}
