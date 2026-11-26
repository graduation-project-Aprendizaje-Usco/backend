package co.edu.usco.aprendizaje_usco.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MezcladorOpcionesTest {

    private static final List<String> OPCIONES = List.of("A", "B", "C", "D");

    @Test
    void laMismaSemillaMantieneElOrden() {
        List<String> primera = MezcladorOpciones.mezclar(OPCIONES, 7L, 3L);
        List<String> segunda = MezcladorOpciones.mezclar(OPCIONES, 7L, 3L);

        assertThat(segunda).containsExactlyElementsOf(primera);
    }

    @Test
    void semillasDistintasCambianElOrden() {
        List<String> intenteUno = MezcladorOpciones.mezclar(OPCIONES, 7L, 3L);
        List<String> intenteDos = MezcladorOpciones.mezclar(OPCIONES, 7L, 4L);

        assertThat(intenteDos).isNotEqualTo(intenteUno);
    }

    @Test
    void conservaTodasLasOpcionesUnaSolaVez() {
        assertThat(MezcladorOpciones.mezclar(OPCIONES, 1L, 2L))
                .containsExactlyInAnyOrderElementsOf(OPCIONES);
    }

    @Test
    void noModificaLaListaOriginal() {
        List<String> original = new ArrayList<>(OPCIONES);

        MezcladorOpciones.mezclar(original, 1L, 2L);

        assertThat(original).containsExactlyElementsOf(OPCIONES);
    }

    @Test
    void laLetraDependeDeLaPosicionMostrada() {
        assertThat(MezcladorOpciones.letra(0)).isEqualTo("A");
        assertThat(MezcladorOpciones.letra(1)).isEqualTo("B");
        assertThat(MezcladorOpciones.letra(2)).isEqualTo("C");
        assertThat(MezcladorOpciones.letra(3)).isEqualTo("D");
    }
}
