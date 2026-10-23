package co.edu.usco.aprendizaje_usco.modulos.practica.service;

import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Ejercicio;
import co.edu.usco.aprendizaje_usco.modulos.practica.dto.RetroalimentacionResponse;
import org.springframework.stereotype.Service;

@Service
public class FeedbackService {

    public RetroalimentacionResponse seleccionar(Ejercicio ejercicio, int numeroIntento, boolean correcto) {
        if (correcto) {
            return new RetroalimentacionResponse("EXPLICACION", ejercicio.getExplicacion(), null);
        }
        if (numeroIntento == 1) {
            return new RetroalimentacionResponse("PISTA", ejercicio.getPista(), ejercicio.getErrorComun());
        }
        return new RetroalimentacionResponse("EXPLICACION", ejercicio.getExplicacion(), ejercicio.getErrorComun());
    }

}
