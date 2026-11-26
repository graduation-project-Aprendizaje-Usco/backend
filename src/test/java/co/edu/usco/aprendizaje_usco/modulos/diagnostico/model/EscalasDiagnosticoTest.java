package co.edu.usco.aprendizaje_usco.modulos.diagnostico.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.usco.aprendizaje_usco.modulos.diagnostico.UmbralesV2;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.config.UmbralesDiagnostico.Corte;
import org.junit.jupiter.api.Test;

/** Escalas del instrumento v2.0 (tablas de clasificación global y por dimensión). */
class EscalasDiagnosticoTest {

    private static final Corte GLOBAL = UmbralesV2.UMBRALES.global();
    private static final Corte DIMENSION = UmbralesV2.UMBRALES.dimension();

    @Test
    void categoriaGlobalSegunLaTabla() {
        for (int puntaje = 0; puntaje <= 24; puntaje++) {
            CategoriaDesempeno esperada = puntaje <= 9 ? CategoriaDesempeno.ALTO_RIESGO
                    : puntaje <= 16 ? CategoriaDesempeno.RIESGO_MODERADO
                    : CategoriaDesempeno.NIVEL_ADECUADO;
            assertThat(CategoriaDesempeno.desde(puntaje, 24, GLOBAL)).as("puntaje %d", puntaje).isEqualTo(esperada);
        }
    }

    @Test
    void nivelPorDimensionSegunLaTabla() {
        for (int aciertos = 0; aciertos <= 8; aciertos++) {
            NivelDimension esperado = aciertos <= 3 ? NivelDimension.BAJO
                    : aciertos <= 6 ? NivelDimension.MEDIO
                    : NivelDimension.ALTO;
            assertThat(NivelDimension.desde(aciertos, 8, DIMENSION)).as("aciertos %d", aciertos).isEqualTo(esperado);
        }
    }

    @Test
    void lasDescripcionesSonLasDelInstrumento() {
        assertThat(CategoriaDesempeno.ALTO_RIESGO.getDescripcion()).isEqualTo("Alto riesgo académico");
        assertThat(CategoriaDesempeno.RIESGO_MODERADO.getDescripcion()).isEqualTo("Riesgo moderado");
        assertThat(CategoriaDesempeno.NIVEL_ADECUADO.getDescripcion()).isEqualTo("Nivel adecuado");
    }

    @Test
    void rechazaPuntajesImposibles() {
        assertThatThrownBy(() -> CategoriaDesempeno.desde(25, 24, GLOBAL)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CategoriaDesempeno.desde(-1, 24, GLOBAL)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NivelDimension.desde(0, 0, DIMENSION)).isInstanceOf(IllegalArgumentException.class);
    }

}
