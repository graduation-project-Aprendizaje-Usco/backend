package co.edu.usco.aprendizaje_usco.modulos.diagnostico.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Umbrales de categorización de la prueba diagnóstica (propiedades app.diagnostico.umbrales.*).
 * Se cambian en application.properties (o con variables de entorno) sin tocar código.
 *
 * Valores del instrumento v2.0:
 * <pre>
 *  global    (escala 24):  0–9 alto riesgo · 10–16 riesgo moderado · 17–24 nivel adecuado
 *  dimension (escala 8):   0–3 bajo        · 4–6 medio             · 7–8 alto
 * </pre>
 *
 * Si los valores no son coherentes, la aplicación no arranca (mejor eso que categorizar mal).
 * Un cambio solo afecta a los intentos que se finalicen después: la categoría se guarda al finalizar.
 *
 * @param global    cortes del puntaje total → CategoriaDesempeno
 * @param dimension cortes del puntaje por competencia → NivelDimension
 */
@ConfigurationProperties("app.diagnostico.umbrales")
public record UmbralesDiagnostico(Corte global, Corte dimension) {

    public UmbralesDiagnostico {
        if (global == null) {
            throw new IllegalStateException("Falta configurar app.diagnostico.umbrales.global.*");
        }
        if (dimension == null) {
            throw new IllegalStateException("Falta configurar app.diagnostico.umbrales.dimension.*");
        }
    }

    /**
     * Cortes de una escala de 0 a {@code escala} puntos, en tres niveles:
     * 0..maximoAltoRiesgo, maximoAltoRiesgo+1..maximoRiesgoModerado y el resto.
     *
     * Los cortes se aplican en proporción al total de preguntas calificadas: con el
     * total completo coinciden exactamente con la tabla, y si se desactiva un ítem se
     * mantiene el mismo criterio (9/24 sigue siendo el 37,5 %).
     */
    public record Corte(Integer escala, Integer maximoAltoRiesgo, Integer maximoRiesgoModerado) {

        public Corte {
            if (escala == null || maximoAltoRiesgo == null || maximoRiesgoModerado == null) {
                throw new IllegalStateException(
                        "Cada umbral del diagnóstico necesita escala, maximo-alto-riesgo y maximo-riesgo-moderado");
            }
            if (!(0 <= maximoAltoRiesgo && maximoAltoRiesgo < maximoRiesgoModerado && maximoRiesgoModerado < escala)) {
                throw new IllegalStateException("Umbrales del diagnóstico incoherentes: se requiere "
                        + "0 <= maximo-alto-riesgo < maximo-riesgo-moderado < escala, y llegó escala=" + escala
                        + ", maximo-alto-riesgo=" + maximoAltoRiesgo
                        + ", maximo-riesgo-moderado=" + maximoRiesgoModerado);
            }
        }

        /**
         * @return 0 (alto riesgo), 1 (riesgo moderado) o 2 (adecuado)
         * @throws IllegalArgumentException si no se cumple 0 <= aciertos <= total y total > 0
         */
        public int nivel(int aciertos, int total) {
            if (total <= 0 || aciertos < 0 || aciertos > total) {
                throw new IllegalArgumentException("Puntaje inválido: " + aciertos + " de " + total);
            }
            // aciertos/total <= max/escala  ⇔  aciertos*escala <= max*total (sin decimales).
            if ((long) aciertos * escala <= (long) maximoAltoRiesgo * total) {
                return 0;
            }
            if ((long) aciertos * escala <= (long) maximoRiesgoModerado * total) {
                return 1;
            }
            return 2;
        }

    }

}
