package co.edu.usco.aprendizaje_usco.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 404: el recurso pedido no existe (o el usuario no debe saber que existe).
 *
 * <pre>
 * Ejercicio ejercicio = repo.findById(id)
 *         .orElseThrow(() -> new RecursoNoEncontradoException("Ejercicio", id));
 * </pre>
 */
public class RecursoNoEncontradoException extends AplicacionException {

    public RecursoNoEncontradoException(String mensaje) {
        super(HttpStatus.NOT_FOUND, mensaje);
    }

    public RecursoNoEncontradoException(String recurso, Object id) {
        this(recurso + " con id " + id + " no encontrado");
    }

}
