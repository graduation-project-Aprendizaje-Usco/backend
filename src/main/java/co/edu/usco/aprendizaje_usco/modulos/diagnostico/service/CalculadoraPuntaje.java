package co.edu.usco.aprendizaje_usco.modulos.diagnostico.service;

import co.edu.usco.aprendizaje_usco.modulos.diagnostico.config.UmbralesDiagnostico;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.CategoriaDesempeno;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.NivelDimension;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Bloque 5: calcula el puntaje de un intento. Es lógica pura (sin base de datos ni
 * Spring) para poder probar cada límite de la escala con pruebas unitarias rápidas.
 *
 * Reglas del instrumento v2.0:
 *  - respuesta correcta = 1 punto, incorrecta = 0;
 *  - una pregunta sin responder cuenta como incorrecta;
 *  - puntaje global de 0 a 24 → {@link CategoriaDesempeno};
 *  - puntaje por competencia de 0 a 8 → {@link NivelDimension}, más el porcentaje
 *    de aciertos con 2 decimales (redondeo HALF_UP);
 *  - los cortes de cada categoría llegan como parámetro ({@link UmbralesDiagnostico},
 *    configurables en application.properties).
 */
public final class CalculadoraPuntaje {

    private CalculadoraPuntaje() {
    }

    /** Una pregunta con lo necesario para calificarla. */
    public record PreguntaCalificable(long itemId, long competenciaId, long opcionCorrectaId) {
    }

    public record ResultadoDimensionCalculado(long competenciaId, int aciertos, int total,
                                              BigDecimal porcentaje, NivelDimension nivel) {
    }

    public record ResultadoCalculado(int puntaje, int total, CategoriaDesempeno categoria,
                                     List<ResultadoDimensionCalculado> dimensiones) {
    }

    /**
     * @param preguntas  las preguntas calificadas (no vacía)
     * @param respuestas itemId → opcionId elegida; los ítems que no aparecen no se respondieron
     * @param umbrales   cortes global y por competencia
     */
    public static ResultadoCalculado calcular(Collection<PreguntaCalificable> preguntas,
                                              Map<Long, Long> respuestas,
                                              UmbralesDiagnostico umbrales) {
        Objects.requireNonNull(respuestas);
        Objects.requireNonNull(umbrales);
        if (preguntas == null || preguntas.isEmpty()) {
            throw new IllegalArgumentException("No hay preguntas para calificar");
        }

        // competenciaId → [aciertos, total], en el orden en que aparecen las competencias.
        Map<Long, int[]> porCompetencia = new LinkedHashMap<>();
        int puntaje = 0;
        for (PreguntaCalificable pregunta : preguntas) {
            boolean acerto = Objects.equals(respuestas.get(pregunta.itemId()), pregunta.opcionCorrectaId());
            int[] cuenta = porCompetencia.computeIfAbsent(pregunta.competenciaId(), id -> new int[2]);
            cuenta[1]++;
            if (acerto) {
                cuenta[0]++;
                puntaje++;
            }
        }

        List<ResultadoDimensionCalculado> dimensiones = new ArrayList<>();
        porCompetencia.forEach((competenciaId, cuenta) -> dimensiones.add(new ResultadoDimensionCalculado(
                competenciaId, cuenta[0], cuenta[1], porcentaje(cuenta[0], cuenta[1]),
                NivelDimension.desde(cuenta[0], cuenta[1], umbrales.dimension()))));

        int total = preguntas.size();
        return new ResultadoCalculado(puntaje, total, CategoriaDesempeno.desde(puntaje, total, umbrales.global()),
                List.copyOf(dimensiones));
    }

    /** 100 * aciertos / total con 2 decimales: 5 de 8 → 62.50; 1 de 3 → 33.33. */
    static BigDecimal porcentaje(int aciertos, int total) {
        return BigDecimal.valueOf(aciertos * 100L)
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

}
