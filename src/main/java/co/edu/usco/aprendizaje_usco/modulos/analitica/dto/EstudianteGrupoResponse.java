package co.edu.usco.aprendizaje_usco.modulos.analitica.dto;

import java.math.BigDecimal;
import java.util.List;

public record EstudianteGrupoResponse(
        Long id,
        String nombres,
        String apellidos,
        String correo,
        long intentos,
        long aciertos,
        BigDecimal promedio,
        BigDecimal porcentajeDiagnostico,
        String nivel,
        String categoriaDiagnostico,
        boolean sinActividad,
        boolean enRiesgo,
        List<String> motivosRiesgo) {
}
