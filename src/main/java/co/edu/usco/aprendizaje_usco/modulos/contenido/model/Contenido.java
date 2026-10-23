package co.edu.usco.aprendizaje_usco.modulos.contenido.model;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Agrupacion tematica de ejercicios. Una asignatura y una competencia por contenido.
 * Los datos semilla tienen creadoPor = null; los creados por docentes lo llenan.
 */
@Entity
@Table(name = "contenido")
@Getter
public class Contenido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competencia_id", nullable = false)
    private Competencia competencia;

    @Column(name = "asignatura", nullable = false, length = 80)
    private String asignatura;

    @Column(name = "tema", nullable = false, length = 120)
    private String tema;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    protected Contenido() {
    }

    public Contenido(Competencia competencia, String asignatura, String tema, String titulo,
                     String descripcion, Usuario creadoPor) {
        this.competencia = Objects.requireNonNull(competencia);
        this.asignatura = Objects.requireNonNull(asignatura).trim().toUpperCase();
        this.tema = Objects.requireNonNull(tema).trim();
        this.titulo = Objects.requireNonNull(titulo).trim();
        this.descripcion = descripcion;
        this.creadoPor = creadoPor;
    }

    public void desactivar() {
        this.activo = false;
    }

    public void activar() {
        this.activo = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contenido otro)) return false;
        return id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Contenido.class.hashCode();
    }

}
