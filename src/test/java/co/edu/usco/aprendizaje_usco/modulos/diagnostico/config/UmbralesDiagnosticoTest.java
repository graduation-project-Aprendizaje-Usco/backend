package co.edu.usco.aprendizaje_usco.modulos.diagnostico.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.usco.aprendizaje_usco.modulos.diagnostico.UmbralesV2;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.CategoriaDesempeno;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.NivelDimension;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

/**
 * Tarea "Reglas configurables de categorización": los umbrales salen de las
 * propiedades app.diagnostico.umbrales.* y cambiarlos cambia la categoría sin
 * tocar código. Se usa el Binder de Spring Boot (el mismo que lee
 * application.properties), sin levantar la aplicación.
 */
class UmbralesDiagnosticoTest {

    private static Map<String, String> propiedadesV2() {
        Map<String, String> p = new HashMap<>();
        p.put("app.diagnostico.umbrales.global.escala", "24");
        p.put("app.diagnostico.umbrales.global.maximo-alto-riesgo", "9");
        p.put("app.diagnostico.umbrales.global.maximo-riesgo-moderado", "16");
        p.put("app.diagnostico.umbrales.dimension.escala", "8");
        p.put("app.diagnostico.umbrales.dimension.maximo-alto-riesgo", "3");
        p.put("app.diagnostico.umbrales.dimension.maximo-riesgo-moderado", "6");
        return p;
    }

    private static UmbralesDiagnostico enlazar(Map<String, String> propiedades) {
        return new Binder(new MapConfigurationPropertySource(propiedades))
                .bindOrCreate("app.diagnostico.umbrales", UmbralesDiagnostico.class);
    }

    @Test
    void seLeenDeLasPropiedades() {
        assertThat(enlazar(propiedadesV2())).isEqualTo(UmbralesV2.UMBRALES);
    }

    @Test
    void cambiarUnUmbralCambiaLaCategoriaSinTocarCodigo() {
        assertThat(CategoriaDesempeno.desde(10, 24, enlazar(propiedadesV2()).global()))
                .isEqualTo(CategoriaDesempeno.RIESGO_MODERADO);

        Map<String, String> masExigente = propiedadesV2();
        masExigente.put("app.diagnostico.umbrales.global.maximo-alto-riesgo", "12");
        assertThat(CategoriaDesempeno.desde(10, 24, enlazar(masExigente).global()))
                .isEqualTo(CategoriaDesempeno.ALTO_RIESGO);

        Map<String, String> dimensionMasExigente = propiedadesV2();
        dimensionMasExigente.put("app.diagnostico.umbrales.dimension.maximo-riesgo-moderado", "7");
        assertThat(NivelDimension.desde(7, 8, enlazar(dimensionMasExigente).dimension()))
                .isEqualTo(NivelDimension.MEDIO);
    }

    @Test
    void unosUmbralesIncoherentesImpidenArrancar() {
        Map<String, String> alReves = propiedadesV2();
        alReves.put("app.diagnostico.umbrales.global.maximo-alto-riesgo", "17");

        assertThatThrownBy(() -> enlazar(alReves))
                .isInstanceOf(BindException.class)
                .rootCause().hasMessageContaining("incoherentes");
    }

    @Test
    void faltaUnValor() {
        Map<String, String> incompleto = propiedadesV2();
        incompleto.remove("app.diagnostico.umbrales.dimension.escala");

        assertThatThrownBy(() -> enlazar(incompleto))
                .isInstanceOf(BindException.class)
                .rootCause().hasMessageContaining("escala");
    }

}
