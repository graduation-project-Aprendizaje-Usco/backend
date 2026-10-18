package co.edu.usco.aprendizaje_usco.modulos.diagnostico.model;

/** Estado de un intento de la prueba diagnóstica (CHECK ck_intento_estado). */
public enum EstadoIntento {
    /** El estudiante lo está presentando; acepta respuestas mientras no venza el tiempo. */
    EN_CURSO,
    /** Cerrado (por el estudiante o al vencer el tiempo); ya tiene puntaje y categoría. */
    FINALIZADO
}
