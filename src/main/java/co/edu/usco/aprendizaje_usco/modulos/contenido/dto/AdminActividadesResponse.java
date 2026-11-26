package co.edu.usco.aprendizaje_usco.modulos.contenido.dto;

import co.edu.usco.aprendizaje_usco.modulos.contenido.model.TipoEjercicio;
import java.util.List;

/**
 * Catálogo completo de contenidos y ejercicios para el panel del administrador
 * (SCRUM-46). Es de solo lectura y no incluye respuestas correctas, pistas ni
 * explicaciones: el administrador consulta la gestión de los docentes sin
 * exponer las claves de los ejercicios.
 */
public record AdminActividadesResponse(
        List<ContenidoAdmin> contenidos,
        List<EjercicioAdmin> ejercicios) {

    public record ContenidoAdmin(
            Long id,
            String titulo,
            String tema,
            String asignatura,
            String competenciaCodigo,
            String competenciaNombre,
            boolean activo,
            long totalEjercicios,
            String creadoPorCorreo) {
    }

    public record EjercicioAdmin(
            Long id,
            String codigo,
            String contenidoTitulo,
            String competenciaCodigo,
            int nivel,
            TipoEjercicio tipo,
            boolean activo,
            String creadoPorCorreo) {
    }
}
