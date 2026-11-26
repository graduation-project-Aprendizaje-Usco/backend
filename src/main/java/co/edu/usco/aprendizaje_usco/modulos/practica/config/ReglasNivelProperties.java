package co.edu.usco.aprendizaje_usco.modulos.practica.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.practica.niveles")
public record ReglasNivelProperties(int aciertosParaSubir, int erroresParaBajar,
                                    int nivelInicialSinDiagnostico) {

    public ReglasNivelProperties {
        if (aciertosParaSubir < 1 || erroresParaBajar < 1) {
            throw new IllegalStateException("Los umbrales de nivel deben ser mayores que cero");
        }
        if (nivelInicialSinDiagnostico < 1 || nivelInicialSinDiagnostico > 3) {
            throw new IllegalStateException("El nivel inicial debe estar entre 1 y 3");
        }
    }

}
