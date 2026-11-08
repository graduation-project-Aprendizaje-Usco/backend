package co.edu.usco.aprendizaje_usco.modulos.progreso.service;

import co.edu.usco.aprendizaje_usco.common.exception.ReglaNegocioException;
import co.edu.usco.aprendizaje_usco.modulos.progreso.dto.HistorialItemResponse;
import co.edu.usco.aprendizaje_usco.modulos.progreso.dto.PaginaResponse;
import co.edu.usco.aprendizaje_usco.modulos.progreso.model.TipoActividad;
import co.edu.usco.aprendizaje_usco.modulos.progreso.repository.HistorialRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Historial de resultados (SCRUM-41, RF-15).
 *  - ESTUDIANTE: solo el suyo (si manda estudianteId, debe ser el propio).
 *  - DOCENTE: debe indicar estudianteId, y ese estudiante tiene que estar en uno de sus grupos.
 */
@Service
public class HistorialService {

    /** Criterio de la tarjeta: 20 por página. */
    public static final int TAMANO_PAGINA = 20;

    // shortcut: las fechas del filtro se interpretan en la hora de Colombia; si la app sale del país, recibir la zona del cliente.
    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private final HistorialRepository historialRepository;

    public HistorialService(HistorialRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<HistorialItemResponse> consultar(long usuarioId, NombreRol rol, Long estudianteId,
                                                           TipoActividad tipo, String competencia,
                                                           LocalDate desde, LocalDate hasta, int pagina) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        long estudiante = estudianteAConsultar(usuarioId, rol, estudianteId);

        HistorialRepository.Filtro filtro = new HistorialRepository.Filtro(
                estudiante,
                tipo,
                competencia == null || competencia.isBlank() ? null : competencia.trim().toUpperCase(Locale.ROOT),
                desde == null ? null : desde.atStartOfDay(ZONA).toInstant(),
                hasta == null ? null : hasta.plusDays(1).atStartOfDay(ZONA).toInstant()); // "hasta" incluye ese día

        return PaginaResponse.de(historialRepository.buscar(filtro, pagina, TAMANO_PAGINA), pagina, TAMANO_PAGINA,
                historialRepository.contar(filtro));
    }

    private long estudianteAConsultar(long usuarioId, NombreRol rol, Long estudianteId) {
        if (rol == NombreRol.ESTUDIANTE) {
            if (estudianteId != null && estudianteId != usuarioId) {
                throw new AccessDeniedException("Un estudiante solo puede ver su propio historial");
            }
            return usuarioId;
        }
        if (estudianteId == null) {
            throw new ReglaNegocioException("Indica el estudiante cuyo historial quieres ver (estudianteId)");
        }
        if (!historialRepository.estudianteEnGruposDelDocente(usuarioId, estudianteId)) {
            throw new AccessDeniedException("El estudiante no pertenece a ninguno de tus grupos");
        }
        return estudianteId;
    }

}
