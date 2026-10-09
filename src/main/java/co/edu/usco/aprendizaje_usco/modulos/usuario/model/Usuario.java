package co.edu.usco.aprendizaje_usco.modulos.usuario.model;

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
import java.time.Instant;
import java.util.Locale;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Tabla usuario.
 *
 * Reglas que aplica la propia entidad:
 *  - el correo se guarda sin espacios y en minúsculas (la base lo exige con ck_usuario_correo_minuscula);
 *  - un usuario nuevo nace ACTIVO;
 *  - creado_en y actualizado_en los llena Hibernate.
 *
 * Nunca devolver esta entidad en un controlador: usar un DTO sin passwordHash.
 */
@Entity
@Table(name = "usuario")
@Getter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombres", nullable = false, length = 100)
    @Setter
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    @Setter
    private String apellidos;

    @Column(name = "correo", nullable = false, unique = true, length = 150)
    private String correo;

    @Column(name = "password_hash", nullable = false, length = 100)
    @Setter
    private String passwordHash;

    // LAZY: el rol solo se carga cuando se necesita. Para el login usar
    // UsuarioRepository.findByCorreo, que ya trae el rol en la misma consulta.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rol_id", nullable = false)
    @Setter
    private Rol rol;

    @Column(name = "programa_academico", length = 120)
    @Setter
    private String programaAcademico;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    /** Requerido por JPA. */
    protected Usuario() {
    }

    public Usuario(String nombres, String apellidos, String correo, String passwordHash, Rol rol) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        setCorreo(correo);
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    /** Normaliza el correo: "  Ana@USCO.edu.co " → "ana@usco.edu.co". */
    public static String normalizarCorreo(String correo) {
        return correo == null ? null : correo.trim().toLowerCase(Locale.ROOT);
    }

    public boolean estaActivo() {
        return estado == EstadoUsuario.ACTIVO;
    }

    public void activar() {
        this.estado = EstadoUsuario.ACTIVO;
    }

    public void desactivar() {
        this.estado = EstadoUsuario.INACTIVO;
    }

    // Conserva la normalización del correo en todas las asignaciones.
    public void setCorreo(String correo) {
        this.correo = normalizarCorreo(correo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario otro)) return false;
        return id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Usuario.class.hashCode();
    }

    /** Sin passwordHash a propósito: el toString termina en logs. */
    @Override
    public String toString() {
        return "Usuario{id=" + id + ", correo=" + correo + ", estado=" + estado + "}";
    }

}
