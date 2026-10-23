package co.edu.usco.aprendizaje_usco.modulos.diagnostico.event;

import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.NivelDimension;
import java.math.BigDecimal;
import java.util.List;

public record DiagnosticoFinalizadoEvent(
        Long usuarioId,
        Long intentoDiagnosticoId,
        List<ResultadoDimensionSnapshot> resultados) {

    public record ResultadoDimensionSnapshot(
            Long competenciaId,
            NivelDimension nivel,
            BigDecimal porcentaje) {
    }

}
