package co.edu.usco.aprendizaje_usco.modulos.practica.dto;

public record ResultadoIntentoResponse(
        Long intentoId,
        int numeroIntento,
        boolean esCorrecta,
        boolean puedeReintentar,
        RetroalimentacionResponse retroalimentacion,
        NivelResponse nivel) {
}
