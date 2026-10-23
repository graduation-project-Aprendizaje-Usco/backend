package co.edu.usco.aprendizaje_usco.modulos.practica.event;

import co.edu.usco.aprendizaje_usco.modulos.diagnostico.event.DiagnosticoFinalizadoEvent;
import co.edu.usco.aprendizaje_usco.modulos.practica.service.NivelEstudianteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NivelInicialListener {

    private static final Logger log = LoggerFactory.getLogger(NivelInicialListener.class);

    private final NivelEstudianteService nivelEstudianteService;

    public NivelInicialListener(NivelEstudianteService nivelEstudianteService) {
        this.nivelEstudianteService = nivelEstudianteService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alFinalizarDiagnostico(DiagnosticoFinalizadoEvent evento) {
        try {
            nivelEstudianteService.inicializarDesdeResultados(
                    evento.usuarioId(), evento.intentoDiagnosticoId(), evento.resultados());
        } catch (RuntimeException ex) {
            // El diagnostico ya quedo finalizado; el motor hara la inicializacion lazy.
            log.warn("No se pudieron inicializar los niveles del diagnostico {}: {}",
                    evento.intentoDiagnosticoId(), ex.getMessage());
        }
    }

}
