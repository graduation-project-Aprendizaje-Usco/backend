package co.edu.usco.aprendizaje_usco.modulos.diagnostico.service;

import static co.edu.usco.aprendizaje_usco.modulos.diagnostico.UmbralesV2.UMBRALES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.CategoriaDesempeno;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.NivelDimension;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.service.CalculadoraPuntaje.PreguntaCalificable;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.service.CalculadoraPuntaje.ResultadoCalculado;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.service.CalculadoraPuntaje.ResultadoDimensionCalculado;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Bloque 5: cálculo del puntaje, sin base de datos. Cubre los límites de cada
 * categoría de la tabla del instrumento v2.0.
 */
class CalculadoraPuntajeTest {

    private static final long ALGEBRAICA = 1;
    private static final long LOGICA = 2;
    private static final long FUNCIONAL = 3;

    /** 24 preguntas como la prueba real: ítems 1-8 algebraica, 9-16 lógica, 17-24 funcional. La clave del ítem i es la opción 100+i. */
    private static List<PreguntaCalificable> prueba() {
        List<PreguntaCalificable> preguntas = new ArrayList<>();
        for (long i = 1; i <= 24; i++) {
            long competencia = i <= 8 ? ALGEBRAICA : i <= 16 ? LOGICA : FUNCIONAL;
            preguntas.add(new PreguntaCalificable(i, competencia, 100 + i));
        }
        return preguntas;
    }

    /** Responde bien los ítems indicados y mal (opción 999) todos los demás. */
    private static Map<Long, Long> correctas(long... items) {
        Map<Long, Long> respuestas = new HashMap<>();
        for (long i = 1; i <= 24; i++) {
            respuestas.put(i, 999L);
        }
        for (long i : items) {
            respuestas.put(i, 100 + i);
        }
        return respuestas;
    }

    private static long[] primeros(int n) {
        long[] items = new long[n];
        for (int i = 0; i < n; i++) {
            items[i] = i + 1;
        }
        return items;
    }

    private static ResultadoDimensionCalculado dimension(ResultadoCalculado r, long competencia) {
        return r.dimensiones().stream().filter(d -> d.competenciaId() == competencia).findFirst().orElseThrow();
    }

    @Test
    void todasCorrectasDa24YNivelAdecuado() {
        ResultadoCalculado r = CalculadoraPuntaje.calcular(prueba(), correctas(primeros(24)), UMBRALES);

        assertThat(r.puntaje()).isEqualTo(24);
        assertThat(r.total()).isEqualTo(24);
        assertThat(r.categoria()).isEqualTo(CategoriaDesempeno.NIVEL_ADECUADO);
        assertThat(r.dimensiones()).hasSize(3).allSatisfy(d -> {
            assertThat(d.aciertos()).isEqualTo(8);
            assertThat(d.total()).isEqualTo(8);
            assertThat(d.porcentaje()).isEqualByComparingTo("100.00");
            assertThat(d.nivel()).isEqualTo(NivelDimension.ALTO);
        });
    }

    @Test
    void sinResponderNadaDa0YAltoRiesgo() {
        ResultadoCalculado r = CalculadoraPuntaje.calcular(prueba(), Map.of(), UMBRALES);

        assertThat(r.puntaje()).isZero();
        assertThat(r.categoria()).isEqualTo(CategoriaDesempeno.ALTO_RIESGO);
        assertThat(r.dimensiones()).allSatisfy(d -> {
            assertThat(d.aciertos()).isZero();
            assertThat(d.porcentaje()).isEqualByComparingTo("0.00");
            assertThat(d.nivel()).isEqualTo(NivelDimension.BAJO);
        });
    }

    /** Límites de la tabla global: 0-9, 10-16, 17-24. */
    @ParameterizedTest(name = "{0} aciertos → {1}")
    @CsvSource({
            "0, ALTO_RIESGO", "9, ALTO_RIESGO",
            "10, RIESGO_MODERADO", "16, RIESGO_MODERADO",
            "17, NIVEL_ADECUADO", "24, NIVEL_ADECUADO"})
    void categoriaGlobalEnLosLimites(int aciertos, CategoriaDesempeno esperada) {
        ResultadoCalculado r = CalculadoraPuntaje.calcular(prueba(), correctas(primeros(aciertos)), UMBRALES);

        assertThat(r.puntaje()).isEqualTo(aciertos);
        assertThat(r.categoria()).isEqualTo(esperada);
    }

    /** Límites por dimensión: 0-3, 4-6, 7-8 (se varían solo los ítems de lógica, 9 a 16). */
    @ParameterizedTest(name = "{0} de 8 → {1}")
    @CsvSource({
            "0, BAJO, 0.00", "3, BAJO, 37.50",
            "4, MEDIO, 50.00", "6, MEDIO, 75.00",
            "7, ALTO, 87.50", "8, ALTO, 100.00"})
    void nivelPorDimensionEnLosLimites(int aciertos, NivelDimension esperado, String porcentaje) {
        long[] items = new long[aciertos];
        for (int i = 0; i < aciertos; i++) {
            items[i] = 9 + i;
        }
        ResultadoDimensionCalculado logica = dimension(CalculadoraPuntaje.calcular(prueba(), correctas(items), UMBRALES), LOGICA);

        assertThat(logica.aciertos()).isEqualTo(aciertos);
        assertThat(logica.total()).isEqualTo(8);
        assertThat(logica.nivel()).isEqualTo(esperado);
        assertThat(logica.porcentaje()).isEqualByComparingTo(porcentaje);
    }

    @Test
    void cadaDimensionSeCalificaPorSeparado() {
        // 8 algebraica + 4 lógica + 0 funcional = 12 → riesgo moderado.
        ResultadoCalculado r = CalculadoraPuntaje.calcular(prueba(), correctas(primeros(12)), UMBRALES);

        assertThat(r.puntaje()).isEqualTo(12);
        assertThat(r.categoria()).isEqualTo(CategoriaDesempeno.RIESGO_MODERADO);
        assertThat(dimension(r, ALGEBRAICA).nivel()).isEqualTo(NivelDimension.ALTO);
        assertThat(dimension(r, LOGICA).nivel()).isEqualTo(NivelDimension.MEDIO);
        assertThat(dimension(r, FUNCIONAL).nivel()).isEqualTo(NivelDimension.BAJO);
        assertThat(r.dimensiones()).extracting(ResultadoDimensionCalculado::competenciaId)
                .containsExactly(ALGEBRAICA, LOGICA, FUNCIONAL);
    }

    @Test
    void unaRespuestaDeOtraPreguntaNoCuenta() {
        // Al ítem 1 se le "responde" con la clave del ítem 2: no es acierto.
        ResultadoCalculado r = CalculadoraPuntaje.calcular(prueba(), Map.of(1L, 102L), UMBRALES);

        assertThat(r.puntaje()).isZero();
    }

    @Test
    void elPorcentajeSeRedondeaADosDecimales() {
        assertThat(CalculadoraPuntaje.porcentaje(1, 3)).isEqualTo(new BigDecimal("33.33"));
        assertThat(CalculadoraPuntaje.porcentaje(2, 3)).isEqualTo(new BigDecimal("66.67"));
        assertThat(CalculadoraPuntaje.porcentaje(5, 8)).isEqualTo(new BigDecimal("62.50"));
    }

    @Test
    void conOtroTotalLosCortesSonProporcionales() {
        // Si se desactiva un ítem (23 preguntas), 9/23 sigue siendo alto riesgo y 17/23 adecuado.
        List<PreguntaCalificable> preguntas = prueba().subList(0, 23);
        assertThat(CalculadoraPuntaje.calcular(preguntas, correctas(primeros(8)), UMBRALES).categoria())
                .isEqualTo(CategoriaDesempeno.ALTO_RIESGO);
        assertThat(CalculadoraPuntaje.calcular(preguntas, correctas(primeros(16)), UMBRALES).categoria())
                .isEqualTo(CategoriaDesempeno.NIVEL_ADECUADO);
    }

    @Test
    void sinPreguntasEsUnError() {
        assertThatThrownBy(() -> CalculadoraPuntaje.calcular(List.of(), Map.of(), UMBRALES))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
