package co.edu.usco.aprendizaje_usco.modulos.practica.service;

import co.edu.usco.aprendizaje_usco.common.exception.SolicitudInvalidaException;
import co.edu.usco.aprendizaje_usco.modulos.practica.config.PracticaProperties;
import org.springframework.stereotype.Component;

@Component
public class AsignaturaValidator {

    private final PracticaProperties properties;

    public AsignaturaValidator(PracticaProperties properties) {
        this.properties = properties;
    }

    public String validar(String asignatura) {
        if (asignatura == null || asignatura.isBlank()) {
            throw new SolicitudInvalidaException("El parametro asignatura es obligatorio");
        }
        if (!properties.esAsignaturaValida(asignatura)) {
            throw new SolicitudInvalidaException("La asignatura '" + asignatura + "' no es valida");
        }
        return properties.normalizar(asignatura);
    }

}
