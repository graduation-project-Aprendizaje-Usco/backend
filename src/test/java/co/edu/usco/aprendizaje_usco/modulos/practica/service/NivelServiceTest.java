package co.edu.usco.aprendizaje_usco.modulos.practica.service;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.usco.aprendizaje_usco.modulos.contenido.model.NivelDificultad;
import co.edu.usco.aprendizaje_usco.modulos.practica.config.ReglasNivelProperties;
import co.edu.usco.aprendizaje_usco.modulos.practica.model.MotivoCambioNivel;
import org.junit.jupiter.api.Test;

class NivelServiceTest {

    private final ReglasNivelProperties reglas = new ReglasNivelProperties(3, 3, 1);

    @Test
    void tresAciertosSeguidosSubenUnNivel() {
        NivelService.Cambio primero = NivelService.evaluar(estado(2, 0, 0), true, reglas);
        NivelService.Cambio segundo = NivelService.evaluar(new NivelService.Estado(primero.nivel(), primero.rachaAciertos(), primero.rachaErrores()), true, reglas);
        NivelService.Cambio tercero = NivelService.evaluar(new NivelService.Estado(segundo.nivel(), segundo.rachaAciertos(), segundo.rachaErrores()), true, reglas);

        assertThat(primero.cambio()).isFalse();
        assertThat(segundo.cambio()).isFalse();
        assertThat(tercero.cambio()).isTrue();
        assertThat(tercero.nivel()).isEqualTo(NivelDificultad.AVANZADO);
        assertThat(tercero.motivo()).isEqualTo(MotivoCambioNivel.RACHA_ACIERTOS);
    }

    @Test
    void tresErroresSeguidosBajanUnNivel() {
        NivelService.Cambio primero = NivelService.evaluar(estado(2, 0, 0), false, reglas);
        NivelService.Cambio segundo = NivelService.evaluar(new NivelService.Estado(primero.nivel(), primero.rachaAciertos(), primero.rachaErrores()), false, reglas);
        NivelService.Cambio tercero = NivelService.evaluar(new NivelService.Estado(segundo.nivel(), segundo.rachaAciertos(), segundo.rachaErrores()), false, reglas);

        assertThat(tercero.cambio()).isTrue();
        assertThat(tercero.nivel()).isEqualTo(NivelDificultad.BASICO);
        assertThat(tercero.motivo()).isEqualTo(MotivoCambioNivel.RACHA_ERRORES);
    }

    @Test
    void noSubeDelNivelAvanzado() {
        NivelService.Cambio cambio = NivelService.evaluar(estado(3, 2, 0), true, reglas);

        assertThat(cambio.nivel()).isEqualTo(NivelDificultad.AVANZADO);
        assertThat(cambio.cambio()).isFalse();
        assertThat(cambio.rachaAciertos()).isZero();
    }

    @Test
    void noBajaDelNivelBasico() {
        NivelService.Cambio cambio = NivelService.evaluar(estado(1, 0, 2), false, reglas);

        assertThat(cambio.nivel()).isEqualTo(NivelDificultad.BASICO);
        assertThat(cambio.cambio()).isFalse();
        assertThat(cambio.rachaErrores()).isZero();
    }

    @Test
    void unErrorReiniciaLaRachaDeAciertos() {
        NivelService.Cambio cambio = NivelService.evaluar(estado(2, 2, 0), false, reglas);

        assertThat(cambio.rachaAciertos()).isZero();
        assertThat(cambio.rachaErrores()).isEqualTo(1);
    }

    private static NivelService.Estado estado(int nivel, int aciertos, int errores) {
        return new NivelService.Estado(NivelDificultad.desde(nivel), aciertos, errores);
    }

}
