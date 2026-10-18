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
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

/**
 * Tabla respuesta_diagnostico: la opción que eligió el estudiante en un ítem.
 * Hay una sola por (intento, ítem) — uq_respuesta_intento_item —; si el
 * estudiante cambia de opción se actualiza esta misma fila (ver {@link #cambiar}).
 */
@Entity
@Table(name = "respuesta_diagnostico")
@Getter
public class RespuestaDiagnostico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intento_id", nullable = false)
    private IntentoDiagnostico intento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opcion_id", nullable = false)
    private Opcion opcion;

    @Column(name = "respondida_en", nullable = false)
    private Instant respondidaEn;

    /** Requerido por JPA. */
    protected RespuestaDiagnostico() {
    }

    public RespuestaDiagnostico(IntentoDiagnostico intento, Item item, Opcion opcion, Instant respondidaEn) {
        this.intento = Objects.requireNonNull(intento);
        this.item = Objects.requireNonNull(item);
        cambiar(opcion, respondidaEn);
    }

    /** Elige otra opción del MISMO ítem y actualiza la marca de tiempo. */
    public void cambiar(Opcion nueva, Instant cuando) {
        Objects.requireNonNull(nueva);
        if (!nueva.getItem().equals(item)) {
            throw new IllegalArgumentException("La opción " + nueva.getId() + " no pertenece al ítem " + item.getId());
        }
        this.opcion = nueva;
        this.respondidaEn = Objects.requireNonNull(cuando);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RespuestaDiagnostico otra)) return false;
        return id != null && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return RespuestaDiagnostico.class.hashCode();
    }

    @Override
    public String toString() {
        return "RespuestaDiagnostico{id=" + id + "}";
    }

}
