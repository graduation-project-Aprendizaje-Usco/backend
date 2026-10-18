package co.edu.usco.aprendizaje_usco.modulos.diagnostico.model;

import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Competencia;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.Getter;

/**
 * Tabla resultado_dimension: aciertos de un intento en una competencia
 * (algebraica, lógica o funcional), con su porcentaje y nivel. Una fila por
 * (intento, competencia). Se crean al finalizar el intento y no cambian.
 */
@Entity
@Table(name = "resultado_dimension")
@Getter
public class ResultadoDimension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intento_id", nullable = false)
    private IntentoDiagnostico intento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competencia_id", nullable = false)
    private Competencia competencia;

    @Column(name = "aciertos", nullable = false)
    private Integer aciertos;

    @Column(name = "total", nullable = false)
    private Integer total;

    @Column(name = "porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel", nullable = false, length = 10)
    private NivelDimension nivel;

    /** Requerido por JPA. */
    protected ResultadoDimension() {
    }

    public ResultadoDimension(IntentoDiagnostico intento, Competencia competencia, int aciertos, int total,
                              BigDecimal porcentaje, NivelDimension nivel) {
        this.intento = Objects.requireNonNull(intento);
        this.competencia = Objects.requireNonNull(competencia);
        this.aciertos = aciertos;
        this.total = total;
        this.porcentaje = Objects.requireNonNull(porcentaje);
        this.nivel = Objects.requireNonNull(nivel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResultadoDimension otro)) return false;
        return id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return ResultadoDimension.class.hashCode();
    }

    @Override
    public String toString() {
        return "ResultadoDimension{id=" + id + ", aciertos=" + aciertos + "/" + total + ", nivel=" + nivel + "}";
    }

}
