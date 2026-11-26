package co.edu.usco.aprendizaje_usco.common.exception;


import org.springframework.http.HttpStatus;

/**
 * 422: los datos tienen el formato correcto pero violan una regla del negocio.
 * Ejemplos: responder un diagnóstico con el tiempo vencido, desactivar al propio administrador.
 *
 * Diferencia con 400: el 400 es para datos mal formados o que no pasan @Valid;
 * el 422 es para datos válidos que la lógica de negocio no permite.
 */
public class ReglaNegocioException extends AplicacionException {

    public ReglaNegocioException(String mensaje) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, mensaje);
    }

}
