package co.edu.usco.aprendizaje_usco.modulos.usuario.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "activacion_cuenta")
@Getter
public class ActivacionCuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "vence_en", nullable = false)
    private Instant venceEn;

    @Column(name = "usada_en")
    private Instant usadaEn;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    protected ActivacionCuenta() {
    }

    public ActivacionCuenta(Usuario usuario, String tokenHash, Instant venceEn) {
        this.usuario = usuario;
        this.tokenHash = tokenHash;
        this.venceEn = venceEn;
    }

    public boolean estaVigente(Instant ahora) {
        return usadaEn == null && venceEn.isAfter(ahora);
    }

    public boolean estaPendiente() {
        return usadaEn == null;
    }

    public void renovar(String tokenHash, Instant venceEn) {
        this.tokenHash = tokenHash;
        this.venceEn = venceEn;
    }

    public void marcarUsada(Instant ahora) {
        this.usadaEn = ahora;
    }
}
