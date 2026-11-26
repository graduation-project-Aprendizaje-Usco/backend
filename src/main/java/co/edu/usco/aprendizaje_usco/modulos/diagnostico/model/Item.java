package co.edu.usco.aprendizaje_usco.modulos.diagnostico.model;

import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Competencia;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;

/**
 * Tabla item: una pregunta de la prueba diagnóstica, con sus 4 opciones.
 * El orden de presentación es "numero" (1 a 24).
 */
@Entity
@Table(name = "item")
@Getter
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prueba_id", nullable = false)
    private PruebaDiagnostica prueba;

    @Column(name = "numero", nullable = false)
    private Integer numero;

    /** Texto con notación matemática entre $...$ (MathText en el frontend). */
    @Column(name = "enunciado", nullable = false, length = 1000)
    private String enunciado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competencia_id", nullable = false)
    private Competencia competencia;

    /** Ruta de la gráfica en el frontend, o null. */
    @Column(name = "imagen", length = 200)
    private String imagen;

    @Column(name = "imagen_alt", length = 500)
    private String imagenAlt;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    @OrderBy("letra ASC")
    private List<Opcion> opciones = new ArrayList<>();

    /** Requerido por JPA. */
    protected Item() {
    }

    /** La clave de la pregunta (exactamente una por ítem, índice uq_opcion_una_correcta). */
    public Optional<Opcion> opcionCorrecta() {
        return opciones.stream().filter(Opcion::isEsCorrecta).findFirst();
    }

    public Optional<Opcion> opcion(Long opcionId) {
        return opciones.stream().filter(o -> o.getId().equals(opcionId)).findFirst();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item otro)) return false;
        return id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Item.class.hashCode();
    }

    @Override
    public String toString() {
        return "Item{id=" + id + ", numero=" + numero + "}";
    }

}
