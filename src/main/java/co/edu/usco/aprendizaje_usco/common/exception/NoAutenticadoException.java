package co.edu.usco.aprendizaje_usco.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 401: credenciales incorrectas o sesión no válida.
 * Ejemplo: login con contraseña errada. El mensaje debe ser genérico:
 * nunca indicar si el correo existe o si la cuenta está inactiva.
 */
public class NoAutenticadoException extends AplicacionException {

    public NoAutenticadoException(String mensaje) {
        super(HttpStatus.UNAUTHORIZED, mensaje);
    }

}
