package co.edu.usco.aprendizaje_usco.modulos.diagnostico.dto;

/**
 * Opción tal como la ve el estudiante: NUNCA incluye si es la correcta.
 * La letra es la posición mostrada (A–D) después del barajado por intento,
 * no la letra almacenada en la base; la calificación usa el id.
 */
public record OpcionResponse(Long id, String letra, String texto) {
}
