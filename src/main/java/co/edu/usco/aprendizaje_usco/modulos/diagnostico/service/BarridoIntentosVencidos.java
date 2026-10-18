package co.edu.usco.aprendizaje_usco.modulos.diagnostico.service;

import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.EstadoIntento;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.repository.IntentoDiagnosticoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Cierra los intentos que se quedaron EN_CURSO después de vencer (el estudiante
 * cerró el navegador o perdió la conexión). Así el resultado queda registrado para
 * el docente aunque el estudiante no vuelva a entrar.
 *
 * Se apaga con app.diagnostico.barrido.habilitado=false (las pruebas automáticas lo
 * apagan para que no interfiera; la prueba del barrido lo invoca directamente).
 */
@Component
@ConditionalOnProperty(name = "app.diagnostico.barrido.habilitado", havingValue = "true", matchIfMissing = true)
public class BarridoIntentosVencidos {

    private static final Logger log = LoggerFactory.getLogger(BarridoIntentosVencidos.class);

    private final IntentoDiagnosticoRepository intentoRepository;
    private final DiagnosticoService diagnosticoService;

    public BarridoIntentosVencidos(IntentoDiagnosticoRepository intentoRepository,
                                   DiagnosticoService diagnosticoService) {
        this.intentoRepository = intentoRepository;
        this.diagnosticoService = diagnosticoService;
    }

    /** Cada minuto por defecto (app.diagnostico.barrido.intervalo-ms). */
    @Scheduled(fixedDelayString = "${app.diagnostico.barrido.intervalo-ms:60000}",
               initialDelayString = "${app.diagnostico.barrido.intervalo-ms:60000}")
    public void ejecutar() {
        int finalizados = 0;
        for (Long id : intentoRepository.findIdsPorEstado(EstadoIntento.EN_CURSO)) {
            try {
                if (diagnosticoService.finalizarSiVencido(id)) {
                    finalizados++;
                }
            } catch (RuntimeException e) {
                log.error("No se pudo finalizar por tiempo el intento {}", id, e);
            }
        }
        if (finalizados > 0) {
            log.info("Barrido: {} intento(s) diagnóstico(s) finalizado(s) por tiempo", finalizados);
        }
    }

}
