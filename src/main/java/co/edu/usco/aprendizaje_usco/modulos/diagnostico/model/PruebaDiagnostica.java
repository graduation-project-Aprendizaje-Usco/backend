package co.edu.usco.aprendizaje_usco.modulos.diagnostico.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import lombok.Getter;

/**
 * Tabla prueba_diagnostica: una versión del instrumento. Solo una puede estar
 * activa (índice uq_prueba_una_activa). La crean las migraciones de datos; la
 * aplicación solo la lee.
 */
@Entity
@Table(name = "prueba_diagnostica")
@Getter
public class PruebaDiagnostica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "version", nullable = false, length = 20)
    private String version;

    @Column(name = "tiempo_limite_min", nullable = false)
    private Integer tiempoLimiteMin;

    @Column(name = "activa", nullable = false)
    private boolean activa;

    @Column(name = "creado_en", nullable = false, insertable = false, updatable = false)
    private Instant creadoEn;

    /** Requerido por JPA. */
    protected PruebaDiagnostica() {
    }

    public Duration getTiempoLimite() {
        return Duration.ofMinutes(tiempoLimiteMin);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PruebaDiagnostica otra)) return false;
        return id != null && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return PruebaDiagnostica.class.hashCode();
    }

    @Override
    public String toString() {
        return "PruebaDiagnostica{id=" + id + ", version=" + version + ", activa=" + activa + "}";
    }

}
