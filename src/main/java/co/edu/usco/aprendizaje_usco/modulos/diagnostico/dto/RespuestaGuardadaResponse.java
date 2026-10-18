package co.edu.usco.aprendizaje_usco.modulos.diagnostico.dto;

import java.time.Instant;

/** Respuesta ya registrada de un ítem (para retomar la prueba donde se dejó). */
public record RespuestaGuardadaResponse(Long itemId, Long opcionId, Instant respondidaEn) {
}
