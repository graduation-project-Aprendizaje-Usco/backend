package co.edu.usco.aprendizaje_usco.modulos.practica.service;

import co.edu.usco.aprendizaje_usco.modulos.contenido.model.NivelDificultad;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.NivelDimension;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Motor de seleccion determinista. No conoce JPA, HTTP ni aleatoriedad.
 * Recibe el estado del estudiante y los candidatos ya filtrados por asignatura.
 */
@Service
public final class SeleccionEjercicioService {

    public record Candidato(Long id, Long competenciaId, String competenciaCodigo, NivelDificultad nivel) {
    }

    public record Historial(Long ejercicioId, Long competenciaId, boolean correcto, Instant creadoEn) {
    }

    public record Estado(
            Map<Long, NivelDificultad> niveles,
            Map<Long, NivelDimension> diagnostico,
            List<Historial> recientes,
            Set<Long> ejerciciosAcertados,
            Set<Long> ejerciciosRecientes,
            Instant ahora) {
    }

    public record Seleccion(
            Candidato ejercicio,
            NivelDificultad nivelObjetivo,
            String motivo,
            Map<Long, Double> puntajes) {
    }

    public Optional<Seleccion> seleccionar(Estado estado, List<Candidato> candidatos) {
        if (candidatos == null || candidatos.isEmpty()) {
            return Optional.empty();
        }

        Map<Long, List<Candidato>> porCompetencia = new LinkedHashMap<>();
        Map<Long, String> codigos = new HashMap<>();
        for (Candidato candidato : candidatos) {
            porCompetencia.computeIfAbsent(candidato.competenciaId(), id -> new ArrayList<>()).add(candidato);
            codigos.putIfAbsent(candidato.competenciaId(), candidato.competenciaCodigo());
        }

        Map<Long, Double> puntajes = new HashMap<>();
        Long elegida = null;
        int mejorPrioridad = -1;
        double mejorScore = Double.NEGATIVE_INFINITY;
        int mejorIntentos = Integer.MAX_VALUE;

        for (Long competenciaId : porCompetencia.keySet()) {
            Metricas metricas = metricas(estado, competenciaId);
            puntajes.put(competenciaId, metricas.score());

            boolean mejor = metricas.prioridad() > mejorPrioridad
                    || (metricas.prioridad() == mejorPrioridad && metricas.score() > mejorScore)
                    || (metricas.prioridad() == mejorPrioridad && metricas.score() == mejorScore
                        && metricas.intentos() < mejorIntentos)
                    || (metricas.prioridad() == mejorPrioridad && metricas.score() == mejorScore
                        && metricas.intentos() == mejorIntentos
                        && (elegida == null || competenciaId < elegida));

            if (mejor) {
                elegida = competenciaId;
                mejorPrioridad = metricas.prioridad();
                mejorScore = metricas.score();
                mejorIntentos = metricas.intentos();
            }
        }

        if (elegida == null) {
            return Optional.empty();
        }

        NivelDificultad nivelObjetivo = estado.niveles().getOrDefault(elegida, NivelDificultad.BASICO);
        List<Candidato> pool = porCompetencia.get(elegida);
        String motivo = motivoPrioridad(estado, elegida);

        List<Candidato> sinAcertados = pool.stream()
                .filter(c -> !estado.ejerciciosAcertados().contains(c.id()))
                .toList();
        if (!sinAcertados.isEmpty()) {
            pool = sinAcertados;
        } else {
            motivo = motivo + "+SIN_NUEVOS";
        }

        List<Candidato> sinRecientes = pool.stream()
                .filter(c -> !estado.ejerciciosRecientes().contains(c.id()))
                .toList();
        if (!sinRecientes.isEmpty()) {
            pool = sinRecientes;
        } else {
            motivo = motivo + "+REPETICION";
        }

        List<Candidato> enNivel = pool.stream()
                .filter(c -> c.nivel() == nivelObjetivo)
                .toList();
        if (enNivel.isEmpty()) {
            enNivel = pool.stream()
                    .filter(c -> c.nivel() == nivelObjetivo.anterior() || c.nivel() == nivelObjetivo.siguiente())
                    .toList();
            if (!enNivel.isEmpty()) {
                pool = enNivel;
                motivo = motivo + "+NIVEL_ADYACENTE";
            } else {
                motivo = motivo + "+SIN_NIVEL";
            }
        } else {
            pool = enNivel;
        }

        Candidato elegido = pool.stream()
                .min(Comparator
                        .comparingInt((Candidato c) -> c.nivel() == nivelObjetivo ? 0 : 1)
                        .thenComparingInt(c -> estado.ejerciciosAcertados().contains(c.id()) ? 1 : 0)
                        .thenComparingInt(c -> estado.ejerciciosRecientes().contains(c.id()) ? 1 : 0)
                        .thenComparingLong(Candidato::id))
                .orElseThrow();

        return Optional.of(new Seleccion(elegido, nivelObjetivo, motivo, Map.copyOf(puntajes)));
    }

    private static Metricas metricas(Estado estado, Long competenciaId) {
        List<Historial> historia = estado.recientes().stream()
                .filter(h -> h.competenciaId().equals(competenciaId))
                .toList();
        int intentos = historia.size();
        int aciertos = (int) historia.stream().filter(Historial::correcto).count();

        double precision = (aciertos + 1.0) / (intentos + 2.0);
        NivelDimension nivelDiagnostico = estado.diagnostico().get(competenciaId);
        double necesidad = nivelDiagnostico == NivelDimension.BAJO ? 1.0
                : nivelDiagnostico == NivelDimension.MEDIO ? 0.6
                : nivelDiagnostico == NivelDimension.ALTO ? 0.3
                : 0.5;

        NivelDificultad nivel = estado.niveles().getOrDefault(competenciaId, NivelDificultad.BASICO);
        double nivelRelativo = (3 - nivel.getValor()) / 2.0;
        double rezago = rezago(estado, competenciaId);
        double score = 0.45 * necesidad + 0.35 * (1 - precision) + 0.10 * nivelRelativo + 0.10 * rezago;

        int prioridad;
        if (nivelDiagnostico == NivelDimension.BAJO || (intentos > 0 && precision < 0.5)) {
            prioridad = 3;
        } else if (nivelDiagnostico == NivelDimension.MEDIO || (intentos > 0 && precision < 0.7)) {
            prioridad = 2;
        } else {
            prioridad = 1;
        }

        return new Metricas(prioridad, score, intentos);
    }

    private static double rezago(Estado estado, Long competenciaId) {
        Optional<Instant> ultima = estado.recientes().stream()
                .filter(h -> h.competenciaId().equals(competenciaId))
                .map(Historial::creadoEn)
                .max(Instant::compareTo);
        if (ultima.isEmpty()) {
            return 1.0;
        }
        long dias = Math.max(0, Duration.between(ultima.get(), estado.ahora()).toDays());
        return Math.min(dias / 7.0, 1.0);
    }

    private static String motivoPrioridad(Estado estado, Long competenciaId) {
        NivelDimension diagnostico = estado.diagnostico().get(competenciaId);
        if (diagnostico == NivelDimension.BAJO) {
            return "REFUERZO_DIAGNOSTICO";
        }
        boolean recienteBajo = estado.recientes().stream()
                .filter(h -> h.competenciaId().equals(competenciaId))
                .mapToInt(h -> h.correcto() ? 1 : 0)
                .average()
                .orElse(1.0) < 0.5;
        return recienteBajo ? "REFUERZO_RECIENTE" : "PROGRESION";
    }

    private record Metricas(int prioridad, double score, int intentos) {
    }

}
