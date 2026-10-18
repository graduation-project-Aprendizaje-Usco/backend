package co.edu.usco.aprendizaje_usco.modulos.diagnostico.dto;

import java.time.Instant;
import java.util.List;

/**
 * Estado de un intento de la prueba diagnóstica.
 *
 * <ul>
 *   <li>EN_CURSO: trae las "respuestas" ya guardadas y todos los "items" (sin las
 *       claves), para poder retomar la prueba; "resultado" es null.</li>
 *   <li>FINALIZADO: "items" va vacío y "resultado" trae el puntaje.</li>
 * </ul>
 * "items" va de último a propósito: es la parte larga del JSON, y así el progreso
 * (totalRespondidas, respuestas) se ve primero al revisar la respuesta a mano.
 * "segundosRestantes" lo calcula el servidor: el frontend lo usa para el
 * cronómetro, pero quien decide si el tiempo se acabó es siempre el backend.
 */
public record IntentoResponse(
        Long id,
        String estado,
        String prueba,
        Instant iniciadoEn,
        Instant venceEn,
        long segundosRestantes,
        int totalItems,
        int totalRespondidas,
        List<RespuestaGuardadaResponse> respuestas,
        ResultadoResponse resultado,
        List<ItemDiagnosticoResponse> items) {
}
