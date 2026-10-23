package co.edu.usco.aprendizaje_usco.modulos.contenido.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.Getter;

@Entity
@Table(name = "opcion_ejercicio")
@Getter
public class OpcionEjercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ejercicio_id", nullable = false)
    private Ejercicio ejercicio;

    @Column(name = "letra", nullable = false, length = 1)
    private String letra;

    @Column(name = "texto", nullable = false, length = 500)
    private String texto;

    @Column(name = "es_correcta", nullable = false)
    private boolean esCorrecta;

    protected OpcionEjercicio() {
    }

    public OpcionEjercicio(Ejercicio ejercicio, String letra, String texto, boolean esCorrecta) {
        this.ejercicio = Objects.requireNonNull(ejercicio);
        this.letra = Objects.requireNonNull(letra).trim().toUpperCase();
        this.texto = Objects.requireNonNull(texto).trim();
        this.esCorrecta = esCorrecta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OpcionEjercicio otra)) return false;
        return id != null && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return OpcionEjercicio.class.hashCode();
    }

    @Override
    public String toString() {
        return "OpcionEjercicio{id=" + id + ", letra=" + letra + "}";
    }

}
