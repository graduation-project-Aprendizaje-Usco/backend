package co.edu.usco.aprendizaje_usco.modulos.progreso.dto;

import java.time.LocalDate;

/**
 * Proyección de consulta nativa de agregación semanal de intentos.
 */
public interface TendenciaSemanalProyeccion {

    LocalDate getFechaInicio();

    long getIntentosEvaluados();

    long getIntentosCorrectos();

}
