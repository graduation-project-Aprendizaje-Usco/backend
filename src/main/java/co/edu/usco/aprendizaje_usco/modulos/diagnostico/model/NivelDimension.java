package co.edu.usco.aprendizaje_usco.modulos.diagnostico.model;

import co.edu.usco.aprendizaje_usco.modulos.diagnostico.config.UmbralesDiagnostico.Corte;

/**
 * Nivel por competencia. Los cortes vienen de app.diagnostico.umbrales.dimension.*
 * (ver UmbralesDiagnostico). Con los valores del instrumento v2.0 (escala 0 a 8):
 *
 * <pre>
 *  0 – 3  BAJO   Nivel bajo (alto riesgo)
 *  4 – 6  MEDIO  Nivel medio (riesgo moderado)
 *  7 – 8  ALTO   Nivel alto (adecuado)
 * </pre>
 */
public enum NivelDimension {

    // El orden importa: corresponde a los niveles 0, 1 y 2 de Corte.nivel().
    BAJO("Nivel bajo (alto riesgo)"),
    MEDIO("Nivel medio (riesgo moderado)"),
    ALTO("Nivel alto (adecuado)");

    private final String descripcion;

    NivelDimension(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static NivelDimension desde(int aciertos, int total, Corte corte) {
        return values()[corte.nivel(aciertos, total)];
    }

}
