package co.edu.usco.aprendizaje_usco.modulos.diagnostico.model;

import co.edu.usco.aprendizaje_usco.modulos.diagnostico.config.UmbralesDiagnostico.Corte;

/**
 * Clasificación global del intento. Los cortes NO están en el código: vienen de
 * app.diagnostico.umbrales.global.* (ver UmbralesDiagnostico). Con los valores
 * del instrumento v2.0 (escala 0 a 24):
 *
 * <pre>
 *   0 – 9   ALTO_RIESGO      Alto riesgo académico
 *  10 – 16  RIESGO_MODERADO  Riesgo moderado
 *  17 – 24  NIVEL_ADECUADO   Nivel adecuado
 * </pre>
 */
public enum CategoriaDesempeno {

    // El orden importa: corresponde a los niveles 0, 1 y 2 de Corte.nivel().
    ALTO_RIESGO("Alto riesgo académico"),
    RIESGO_MODERADO("Riesgo moderado"),
    NIVEL_ADECUADO("Nivel adecuado");

    private final String descripcion;

    CategoriaDesempeno(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * @param aciertos respuestas correctas
     * @param total    preguntas calificadas (mayor que 0)
     * @param corte    umbrales globales configurados
     */
    public static CategoriaDesempeno desde(int aciertos, int total, Corte corte) {
        return values()[corte.nivel(aciertos, total)];
    }

}
