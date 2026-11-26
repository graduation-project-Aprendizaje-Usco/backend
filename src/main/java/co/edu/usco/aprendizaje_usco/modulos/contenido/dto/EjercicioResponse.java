package co.edu.usco.aprendizaje_usco.modulos.contenido.dto;

import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Ejercicio;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.TipoEjercicio;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record EjercicioResponse(
        Long id,
        String codigo,
        Long contenidoId,
        String contenidoTitulo,
        String competenciaCodigo,
        String competenciaNombre,
        int nivel,
        TipoEjercicio tipo,
        String enunciado,
        List<OpcionEjercicioResponse> opciones,
        BigDecimal respuestaNumerica,
        BigDecimal tolerancia,
        String pista,
        String explicacion,
        String errorComun,
        boolean activo,
        Instant creadoEn,
        Instant actualizadoEn) {

    public static EjercicioResponse desde(Ejercicio ejercicio) {
        List<OpcionEjercicioResponse> opciones = ejercicio.getOpciones().stream()
                .map(opcion -> new OpcionEjercicioResponse(
                        opcion.getId(), opcion.getLetra(), opcion.getTexto(), opcion.isEsCorrecta()))
                .toList();
        return new EjercicioResponse(
                ejercicio.getId(),
                ejercicio.getCodigo(),
                ejercicio.getContenido().getId(),
                ejercicio.getContenido().getTitulo(),
                ejercicio.getCompetencia().getCodigo(),
                ejercicio.getCompetencia().getNombre(),
                ejercicio.getNivel().getValor(),
                ejercicio.getTipo(),
                ejercicio.getEnunciado(),
                opciones,
                ejercicio.getRespuestaNumerica(),
                ejercicio.getTolerancia(),
                ejercicio.getPista(),
                ejercicio.getExplicacion(),
                ejercicio.getErrorComun(),
                ejercicio.isActivo(),
                ejercicio.getCreadoEn(),
                ejercicio.getActualizadoEn());
    }
}
