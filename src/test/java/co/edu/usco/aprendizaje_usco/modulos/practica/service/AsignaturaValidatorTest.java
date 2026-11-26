package co.edu.usco.aprendizaje_usco.modulos.practica.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.usco.aprendizaje_usco.common.exception.SolicitudInvalidaException;
import co.edu.usco.aprendizaje_usco.modulos.practica.config.PracticaProperties;
import java.util.List;
import org.junit.jupiter.api.Test;

class AsignaturaValidatorTest {

    private final AsignaturaValidator validator = new AsignaturaValidator(
            new PracticaProperties(List.of("CALCULO_DIFERENCIAL", "CALCULO_INTEGRAL")));

    @Test
    void aceptaYNormalizaUnaAsignaturaValida() {
        assertThat(validator.validar(" calculo_diferencial ")).isEqualTo("CALCULO_DIFERENCIAL");
    }

    @Test
    void rechazaUnaAsignaturaAusente() {
        assertThatThrownBy(() -> validator.validar(" "))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void rechazaUnaAsignaturaDesconocida() {
        assertThatThrownBy(() -> validator.validar("CALCULO_VECTORIAL"))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

}
