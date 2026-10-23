package co.edu.usco.aprendizaje_usco.modulos.practica.service;

import co.edu.usco.aprendizaje_usco.modulos.contenido.model.NivelDificultad;
import co.edu.usco.aprendizaje_usco.modulos.practica.config.ReglasNivelProperties;
import co.edu.usco.aprendizaje_usco.modulos.practica.model.MotivoCambioNivel;

/**
 * Reglas puras de evolucion del nivel de practica. No conoce JPA ni HTTP.
 */
public final class NivelService {

    public record Estado(NivelDificultad nivel, int rachaAciertos, int rachaErrores) {
    }

    public record Cambio(NivelDificultad nivel, int rachaAciertos, int rachaErrores,
                         boolean cambio, MotivoCambioNivel motivo) {
    }

    private NivelService() {
    }

    public static Cambio evaluar(Estado actual, boolean correcto, ReglasNivelProperties reglas) {
        NivelDificultad nivel = actual.nivel();
        int aciertos = actual.rachaAciertos();
        int errores = actual.rachaErrores();

        if (correcto) {
            aciertos++;
            errores = 0;
            if (aciertos >= reglas.aciertosParaSubir()) {
                if (nivel != NivelDificultad.AVANZADO) {
                    return new Cambio(nivel.siguiente(), 0, 0, true, MotivoCambioNivel.RACHA_ACIERTOS);
                }
                aciertos = 0;
            }
        } else {
            errores++;
            aciertos = 0;
            if (errores >= reglas.erroresParaBajar()) {
                if (nivel != NivelDificultad.BASICO) {
                    return new Cambio(nivel.anterior(), 0, 0, true, MotivoCambioNivel.RACHA_ERRORES);
                }
                errores = 0;
            }
        }

        return new Cambio(nivel, aciertos, errores, false, null);
    }

}
