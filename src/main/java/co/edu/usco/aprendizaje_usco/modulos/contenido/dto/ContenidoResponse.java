package co.edu.usco.aprendizaje_usco.modulos.contenido.dto;

import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Contenido;
import java.time.Instant;

public record ContenidoResponse(
        Long id,
        String competenciaCodigo,
        String competenciaNombre,
        String asignatura,
        String tema,
        String titulo,
        String descripcion,
        boolean activo,
        long totalEjercicios,
        Instant creadoEn,
        Instant actualizadoEn) {

    public static ContenidoResponse desde(Contenido contenido, long totalEjercicios) {
        return new ContenidoResponse(
                contenido.getId(),
                contenido.getCompetencia().getCodigo(),
                contenido.getCompetencia().getNombre(),
                contenido.getAsignatura(),
                contenido.getTema(),
                contenido.getTitulo(),
                contenido.getDescripcion(),
                contenido.isActivo(),
                totalEjercicios,
                contenido.getCreadoEn(),
                contenido.getActualizadoEn());
    }
}
