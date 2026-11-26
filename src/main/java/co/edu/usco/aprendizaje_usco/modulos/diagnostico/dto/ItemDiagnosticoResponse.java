package co.edu.usco.aprendizaje_usco.modulos.diagnostico.dto;

import java.util.List;

/**
 * Pregunta de la prueba para el estudiante. El enunciado y las opciones pueden
 * traer notación matemática entre $...$ (se muestra con MathText). Si la pregunta
 * tiene gráfica, "imagen" es una ruta del frontend (ej. /img/diagnostico/item-17.png).
 * Las opciones llegan barajadas por intento y con letras A–D según su posición.
 */
public record ItemDiagnosticoResponse(
        Long id,
        Integer numero,
        String enunciado,
        String imagen,
        String imagenAlt,
        DimensionResponse dimension,
        List<OpcionResponse> opciones) {
}
