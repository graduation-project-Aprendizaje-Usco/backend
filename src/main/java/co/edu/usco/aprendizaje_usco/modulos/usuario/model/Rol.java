package co.edu.usco.aprendizaje_usco.modulos.usuario.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Tabla rol. Los 3 registros los crea la migración V2__datos_base_roles_competencias.sql. 
 * La aplicación no permite crear ni borrar roles, solo los consulta (no hay setters públicos).
 */
@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "nombre", nullable = false, unique = true, length = 20)
    private NombreRol nombre;

    /** Requerido por JPA. */
    protected Rol() {
    }

    public Long getId() {
        return id;
    }

    public NombreRol getNombre() {
        return nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rol otro)) return false;
        return id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Rol.class.hashCode();
    }

    @Override
    public String toString() {
        return "Rol{id=" + id + ", nombre=" + nombre + "}";
    }
}
