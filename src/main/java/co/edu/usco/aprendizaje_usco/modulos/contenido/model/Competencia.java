package co.edu.usco.aprendizaje_usco.modulos.contenido.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Tabla competencia (ALGEBRAICA, LOGICA, FUNCIONAL).
 *
 * El código es un String y no un enum a propósito: así se pueden agregar
 * competencias con una migración de datos sin tocar código (RNF-09).
 * Para referirse a las 3 actuales desde el código, usar las constantes de abajo.
 */
@Entity
@Table(name = "competencia")
public class Competencia {

    public static final String ALGEBRAICA = "ALGEBRAICA";
    public static final String LOGICA = "LOGICA";
    public static final String FUNCIONAL = "FUNCIONAL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    
    /** Requerido por JPA. */
    protected Competencia() {
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Competencia otra)) return false;
        return id != null && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return Competencia.class.hashCode();
    }

    @Override
    public String toString() {
        return "Competencia{id=" + id + ", codigo=" + codigo + "}";
    }

}
