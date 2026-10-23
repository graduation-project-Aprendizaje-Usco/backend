package co.edu.usco.aprendizaje_usco.modulos.practica.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Ejercicio;
import co.edu.usco.aprendizaje_usco.modulos.practica.dto.RetroalimentacionResponse;
import org.junit.jupiter.api.Test;

class FeedbackServiceTest {

    private final FeedbackService feedback = new FeedbackService();

    @Test
    void primerFalloMuestraPistaYErrorComun() {
        RetroalimentacionResponse respuesta = feedback.seleccionar(ejercicio(), 1, false);

        assertThat(respuesta.tipo()).isEqualTo("PISTA");
        assertThat(respuesta.texto()).isEqualTo("Pista de prueba");
        assertThat(respuesta.errorComun()).isEqualTo("Error comun");
    }

    @Test
    void segundoFalloMuestraExplicacion() {
        RetroalimentacionResponse respuesta = feedback.seleccionar(ejercicio(), 2, false);

        assertThat(respuesta.tipo()).isEqualTo("EXPLICACION");
        assertThat(respuesta.texto()).isEqualTo("Explicacion de prueba");
    }

    @Test
    void aciertoMuestraExplicacion() {
        RetroalimentacionResponse respuesta = feedback.seleccionar(ejercicio(), 1, true);

        assertThat(respuesta.tipo()).isEqualTo("EXPLICACION");
        assertThat(respuesta.errorComun()).isNull();
    }

    private static Ejercicio ejercicio() {
        Ejercicio ejercicio = mock(Ejercicio.class);
        when(ejercicio.getPista()).thenReturn("Pista de prueba");
        when(ejercicio.getExplicacion()).thenReturn("Explicacion de prueba");
        when(ejercicio.getErrorComun()).thenReturn("Error comun");
        return ejercicio;
    }

}
