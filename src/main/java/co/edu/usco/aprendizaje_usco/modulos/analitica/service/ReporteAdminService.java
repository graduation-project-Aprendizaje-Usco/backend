package co.edu.usco.aprendizaje_usco.modulos.analitica.service;

import co.edu.usco.aprendizaje_usco.common.exception.ReglaNegocioException;
import co.edu.usco.aprendizaje_usco.modulos.analitica.dto.ReporteAdminResponse;
import co.edu.usco.aprendizaje_usco.modulos.analitica.repository.ReporteAdminRepository;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.CategoriaDesempeno;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.ResumenAdminResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.service.AdminUsuarioService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReporteAdminService {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private final AdminUsuarioService adminUsuarioService;
    private final ReporteAdminRepository reporteRepository;

    public ReporteAdminService(AdminUsuarioService adminUsuarioService, ReporteAdminRepository reporteRepository) {
        this.adminUsuarioService = adminUsuarioService;
        this.reporteRepository = reporteRepository;
    }

    public ReporteAdminResponse generar(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }

        Instant inicio = desde == null ? null : desde.atStartOfDay(ZONA).toInstant();
        Instant fin = hasta == null ? null : hasta.plusDays(1).atStartOfDay(ZONA).toInstant();
        ReporteAdminRepository.PracticaAgregada practica = reporteRepository.practica(inicio, fin);
        ReporteAdminRepository.DiagnosticoAgregado diagnostico = reporteRepository.diagnostico(inicio, fin);
        Map<String, Long> categorias = new LinkedHashMap<>();
        for (CategoriaDesempeno categoria : CategoriaDesempeno.values()) {
            categorias.put(categoria.name(), 0L);
        }
        reporteRepository.distribucionDiagnosticos(inicio, fin)
                .forEach(fila -> categorias.put(fila.categoria(), fila.cantidad()));

        return new ReporteAdminResponse(
                adminUsuarioService.resumen(),
                desde,
                hasta,
                new ReporteAdminResponse.Practica(
                        practica.intentos(),
                        practica.aciertos(),
                        porcentaje(practica.aciertos(), practica.intentos()),
                        practica.estudiantesConActividad()),
                new ReporteAdminResponse.Diagnostico(
                        diagnostico.finalizados(),
                        diagnostico.puntajePromedio(),
                        diagnostico.porcentajePromedio(),
                        diagnostico.estudiantesConActividad(),
                        categorias.entrySet().stream()
                                .map(entry -> new ReporteAdminResponse.DistribucionDiagnostico(
                                        entry.getKey(), entry.getValue()))
                                .toList()));
    }

    private static BigDecimal porcentaje(long aciertos, long intentos) {
        if (intentos == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(aciertos)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(intentos), 2, RoundingMode.HALF_UP);
    }
}
