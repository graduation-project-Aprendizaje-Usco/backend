package co.edu.usco.aprendizaje_usco.modulos.diagnostico;

import co.edu.usco.aprendizaje_usco.modulos.diagnostico.config.UmbralesDiagnostico;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.config.UmbralesDiagnostico.Corte;

/** Los umbrales del instrumento v2.0, para las pruebas unitarias (los mismos de application.properties). */
public final class UmbralesV2 {

    public static final UmbralesDiagnostico UMBRALES =
            new UmbralesDiagnostico(new Corte(24, 9, 16), new Corte(8, 3, 6));

    private UmbralesV2() {
    }

}
