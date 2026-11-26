package co.edu.usco.aprendizaje_usco.modulos.analitica.dto;

import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.ResumenAdminResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReporteAdminResponse(
        ResumenAdminResponse resumenSistema,
        LocalDate desde,
        LocalDate hasta,
        Practica practica,
        Diagnostico diagnostico) {

    public record Practica(
            long intentos,
            long aciertos,
            BigDecimal porcentajeAcierto,
            long estudiantesConActividad) {
    }

    public record Diagnostico(
            long finalizados,
            BigDecimal puntajePromedio,
            BigDecimal porcentajePromedio,
            long estudiantesConActividad,
            List<DistribucionDiagnostico> distribucion) {
    }

    public record DistribucionDiagnostico(String categoria, long cantidad) {
    }
}
