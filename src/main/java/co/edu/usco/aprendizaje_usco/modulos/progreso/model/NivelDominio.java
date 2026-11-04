package co.edu.usco.aprendizaje_usco.modulos.progreso.model;

import java.math.BigDecimal;

/**
 * Escala de nivel de dominio académico calculada a partir del porcentaje de aciertos:
 * - INICIAL: 0% a menos de 40%
 * - BASICO: 40% a menos de 60%
 * - INTERMEDIO: 60% a menos de 80%
 * - AVANZADO: 80% a 100%
 */
public enum NivelDominio {

    INICIAL,
    BASICO,
    INTERMEDIO,
    AVANZADO;

    public static NivelDominio desde(BigDecimal porcentaje) {
        if (porcentaje == null) {
            return null;
        }
        double valor = porcentaje.doubleValue();
        if (valor < 40.0) {
            return INICIAL;
        }
        if (valor < 60.0) {
            return BASICO;
        }
        if (valor < 80.0) {
            return INTERMEDIO;
        }
        return AVANZADO;
    }

}
