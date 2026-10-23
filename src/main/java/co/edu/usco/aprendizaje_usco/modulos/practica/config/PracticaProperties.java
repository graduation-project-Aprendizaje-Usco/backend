package co.edu.usco.aprendizaje_usco.modulos.practica.config;

import java.util.List;
import java.util.Locale;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.practica")
public record PracticaProperties(List<String> asignaturas) {

    public PracticaProperties {
        if (asignaturas == null || asignaturas.isEmpty()) {
            throw new IllegalStateException("Falta configurar app.practica.asignaturas");
        }
        asignaturas = asignaturas.stream()
                .map(a -> a.trim().toUpperCase(Locale.ROOT))
                .filter(a -> !a.isEmpty())
                .distinct()
                .toList();
    }

    public boolean esAsignaturaValida(String asignatura) {
        return asignatura != null && asignaturas.contains(asignatura.trim().toUpperCase(Locale.ROOT));
    }

    public String normalizar(String asignatura) {
        return asignatura == null ? null : asignatura.trim().toUpperCase(Locale.ROOT);
    }

}
