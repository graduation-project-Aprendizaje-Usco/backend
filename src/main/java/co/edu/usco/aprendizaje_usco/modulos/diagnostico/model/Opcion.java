package co.edu.usco.aprendizaje_usco.modulos.diagnostico.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

/**
 * Tabla opcion: una de las 4 alternativas (A–D) de un ítem.
 * "esCorrecta" es la clave: NUNCA se envía al estudiante (ver OpcionResponse).
 */
@Entity
@Table(name = "opcion")
@Getter
public class Opcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "letra", nullable = false, length = 1)
    private String letra;

    @Column(name = "texto", nullable = false, length = 500)
    private String texto;

    @Column(name = "es_correcta", nullable = false)
    private boolean esCorrecta;

    /** Requerido por JPA. */
    protected Opcion() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Opcion otra)) return false;
        return id != null && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return Opcion.class.hashCode();
    }

    /** Sin "esCorrecta" a propósito: el toString termina en logs. */
    @Override
    public String toString() {
        return "Opcion{id=" + id + ", letra=" + letra + "}";
    }

}
