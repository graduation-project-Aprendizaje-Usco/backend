package co.edu.usco.aprendizaje_usco.modulos.analitica.dto;

import java.math.BigDecimal;
import java.util.List;

public record EstudianteReporteResponse(
        Long id,
        String nombres,
        String apellidos,
        long intentos,
        long aciertos,
        BigDecimal promedio,
        String nivel,
        String categoriaDiagnostico,
        boolean sinActividad,
        boolean enRiesgo,
        List<String> motivosRiesgo) {
}
