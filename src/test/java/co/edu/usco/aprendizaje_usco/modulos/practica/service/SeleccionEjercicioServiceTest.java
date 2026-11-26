package co.edu.usco.aprendizaje_usco.modulos.practica.service;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.usco.aprendizaje_usco.modulos.contenido.model.NivelDificultad;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.NivelDimension;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SeleccionEjercicioServiceTest {

    private final SeleccionEjercicioService motor = new SeleccionEjercicioService();
    private final Instant ahora = Instant.parse("2026-10-22T12:00:00Z");

    @Test
    void priorizaLaCompetenciaDebilDelDiagnostico() {
        SeleccionEjercicioService.Estado estado = new SeleccionEjercicioService.Estado(
                Map.of(1L, NivelDificultad.BASICO, 2L, NivelDificultad.INTERMEDIO),
                Map.of(1L, NivelDimension.BAJO, 2L, NivelDimension.ALTO),
                List.of(), Set.of(), Set.of(), ahora);

        var seleccion = motor.seleccionar(estado, candidatos()).orElseThrow();

        assertThat(seleccion.ejercicio().competenciaId()).isEqualTo(1L);
        assertThat(seleccion.motivo()).contains("REFUERZO_DIAGNOSTICO");
    }

    @Test
    void noRepiteUnEjercicioAcertadoMientrasHayaOtro() {
        SeleccionEjercicioService.Estado estado = new SeleccionEjercicioService.Estado(
                Map.of(1L, NivelDificultad.BASICO),
                Map.of(1L, NivelDimension.BAJO),
                List.of(), Set.of(11L), Set.of(), ahora);

        var seleccion = motor.seleccionar(estado, candidatos()).orElseThrow();

        assertThat(seleccion.ejercicio().id()).isNotEqualTo(11L);
    }

    @Test
    void evitaRepetirLosUltimosEjercicios() {
        SeleccionEjercicioService.Historial reciente = new SeleccionEjercicioService.Historial(
                11L, 1L, false, ahora.minusSeconds(60));
        SeleccionEjercicioService.Estado estado = new SeleccionEjercicioService.Estado(
                Map.of(1L, NivelDificultad.BASICO),
                Map.of(1L, NivelDimension.BAJO),
                List.of(reciente), Set.of(), Set.of(11L), ahora);

        var seleccion = motor.seleccionar(estado, candidatos()).orElseThrow();

        assertThat(seleccion.ejercicio().id()).isNotEqualTo(11L);
    }

    @Test
    void devuelveVacioSiNoHayCandidatos() {
        SeleccionEjercicioService.Estado estado = new SeleccionEjercicioService.Estado(
                Map.of(), Map.of(), List.of(), Set.of(), Set.of(), ahora);

        assertThat(motor.seleccionar(estado, List.of())).isEmpty();
    }

    @Test
    void usaNivelAdyacenteSiNoHayEjerciciosDelNivelActual() {
        SeleccionEjercicioService.Estado estado = new SeleccionEjercicioService.Estado(
                Map.of(1L, NivelDificultad.AVANZADO),
                Map.of(1L, NivelDimension.BAJO),
                List.of(), Set.of(), Set.of(), ahora);
        List<SeleccionEjercicioService.Candidato> soloBasico = List.of(
                new SeleccionEjercicioService.Candidato(21L, 1L, "ALGEBRAICA", NivelDificultad.BASICO));

        var seleccion = motor.seleccionar(estado, soloBasico).orElseThrow();

        assertThat(seleccion.ejercicio().id()).isEqualTo(21L);
        assertThat(seleccion.motivo()).contains("SIN_NIVEL");
    }

    @Test
    void esDeterministaAnteElMismoEstado() {
        SeleccionEjercicioService.Estado estado = new SeleccionEjercicioService.Estado(
                Map.of(1L, NivelDificultad.BASICO),
                Map.of(1L, NivelDimension.BAJO),
                List.of(), Set.of(), Set.of(), ahora);

        var primera = motor.seleccionar(estado, candidatos()).orElseThrow();
        var segunda = motor.seleccionar(estado, candidatos()).orElseThrow();

        assertThat(primera.ejercicio().id()).isEqualTo(segunda.ejercicio().id());
    }

    private static List<SeleccionEjercicioService.Candidato> candidatos() {
        return List.of(
                new SeleccionEjercicioService.Candidato(11L, 1L, "ALGEBRAICA", NivelDificultad.BASICO),
                new SeleccionEjercicioService.Candidato(12L, 1L, "ALGEBRAICA", NivelDificultad.BASICO),
                new SeleccionEjercicioService.Candidato(21L, 2L, "LOGICA", NivelDificultad.INTERMEDIO),
                new SeleccionEjercicioService.Candidato(22L, 2L, "LOGICA", NivelDificultad.INTERMEDIO));
    }

}
