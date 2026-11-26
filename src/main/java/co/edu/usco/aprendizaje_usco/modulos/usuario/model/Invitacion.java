package co.edu.usco.aprendizaje_usco.modulos.usuario.model;

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
import lombok.Getter;

/** Invitación de un estudiante a un grupo (RF-01/RF-13, #150). */
@Entity
@Table(name = "invitacion")
@Getter
public class Invitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @Column(name = "correo", nullable = false, length = 150)
    private String correo;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "vence_en", nullable = false)
    private Instant venceEn;

    @Column(name = "usada_en")
    private Instant usadaEn;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn = Instant.now();

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn = Instant.now();

    protected Invitacion() {
    }

    public Invitacion(Grupo grupo, String correo, String nombres, String apellidos,
                      String tokenHash, Instant venceEn) {
        this.grupo = grupo;
        this.correo = Usuario.normalizarCorreo(correo);
        this.nombres = nombres.trim();
        this.apellidos = apellidos.trim();
        this.tokenHash = tokenHash;
        this.venceEn = venceEn;
    }

    public void renovar(String nuevoTokenHash, String nombres, String apellidos, Instant nuevoVence) {
        this.tokenHash = nuevoTokenHash;
        this.nombres = nombres.trim();
        this.apellidos = apellidos.trim();
        this.venceEn = nuevoVence;
        this.usadaEn = null;
        this.actualizadoEn = Instant.now();
    }

    public void marcarUsada(Instant ahora) {
        this.usadaEn = ahora;
        this.actualizadoEn = ahora;
    }

    public boolean estaUsada() {
        return usadaEn != null;
    }

    public boolean estaVigente(Instant ahora) {
        return !estaUsada() && venceEn.isAfter(ahora);
    }
}
