package co.edu.usco.aprendizaje_usco.common.exception;


import org.springframework.http.HttpStatus;

/**
 * Base de las excepciones propias de la aplicación. Cada subclase define el
 * código HTTP con el que la traduce el GlobalExceptionHandler, y su mensaje
 * SÍ se muestra al cliente: escribirlo pensando en el usuario final.
 */
public abstract class AplicacionException extends RuntimeException {

    private final HttpStatus estado;

    protected AplicacionException(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public HttpStatus getEstado() {
        return estado;
    }

}
