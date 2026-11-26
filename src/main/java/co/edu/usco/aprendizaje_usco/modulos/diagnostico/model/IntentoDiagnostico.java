package co.edu.usco.aprendizaje_usco.modulos.diagnostico.model;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
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
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

/**
 * Tabla intento_diagnostico: una presentación de la prueba por un estudiante.
 *
 * Reglas que aplica la entidad (y que la base vuelve a exigir con CHECK):
 *  - nace EN_CURSO, sin puntaje ni categoría;
 *  - vence en iniciadoEn + tiempo límite de la prueba;
 *  - al finalizar guarda la fecha, el puntaje, la categoría y si fue por tiempo,
 *    y ya no cambia más (finalizar dos veces es un error de programación).
 * El índice uq_intento_en_curso impide dos intentos EN_CURSO del mismo estudiante en la misma prueba.
 */
@Entity
@Table(name = "intento_diagnostico")
@Getter
public class IntentoDiagnostico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prueba_id", nullable = false)
    private PruebaDiagnostica prueba;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 12)
    private EstadoIntento estado = EstadoIntento.EN_CURSO;

    @Column(name = "iniciado_en", nullable = false, updatable = false)
    private Instant iniciadoEn;

    @Column(name = "finalizado_en")
    private Instant finalizadoEn;

    @Column(name = "finalizado_por_tiempo", nullable = false)
    private boolean finalizadoPorTiempo;

    @Column(name = "puntaje_total")
    private Integer puntajeTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", length = 20)
    private CategoriaDesempeno categoria;

    /** Requerido por JPA. */
    protected IntentoDiagnostico() {
    }

    public IntentoDiagnostico(Usuario usuario, PruebaDiagnostica prueba, Instant iniciadoEn) {
        this.usuario = Objects.requireNonNull(usuario);
        this.prueba = Objects.requireNonNull(prueba);
        this.iniciadoEn = Objects.requireNonNull(iniciadoEn);
    }

    public boolean estaEnCurso() {
        return estado == EstadoIntento.EN_CURSO;
    }

    public boolean perteneceA(Long usuarioId) {
        return usuario.getId().equals(usuarioId);
    }

    /** Momento en que se acaba el tiempo. */
    public Instant venceEn() {
        return iniciadoEn.plus(prueba.getTiempoLimite());
    }

    /** true si sigue EN_CURSO pero ya se le acabó el tiempo (más la tolerancia dada). */
    public boolean estaVencido(Instant ahora, Duration tolerancia) {
        return estaEnCurso() && ahora.isAfter(venceEn().plus(tolerancia));
    }

    /** Segundos que le quedan (0 si ya venció o está finalizado). */
    public long segundosRestantes(Instant ahora) {
        if (!estaEnCurso()) {
            return 0;
        }
        return Math.max(0, Duration.between(ahora, venceEn()).toSeconds());
    }

    /**
     * Cierra el intento con su resultado. Si fue por tiempo, la fecha de fin es el
     * vencimiento (no el momento en que el sistema lo notó).
     */
    public void finalizar(int puntajeTotal, CategoriaDesempeno categoria, Instant ahora, boolean porTiempo) {
        if (!estaEnCurso()) {
            throw new IllegalStateException("El intento " + id + " ya estaba finalizado");
        }
        this.estado = EstadoIntento.FINALIZADO;
        this.puntajeTotal = puntajeTotal;
        this.categoria = Objects.requireNonNull(categoria);
        this.finalizadoPorTiempo = porTiempo;
        Instant fin = porTiempo ? venceEn() : ahora;
        // Nunca antes del inicio (ck_intento_fechas), aunque el reloj del servidor haya cambiado.
        this.finalizadoEn = fin.isBefore(iniciadoEn) ? iniciadoEn : fin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntentoDiagnostico otro)) return false;
        return id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return IntentoDiagnostico.class.hashCode();
    }

    @Override
    public String toString() {
        return "IntentoDiagnostico{id=" + id + ", estado=" + estado + ", puntajeTotal=" + puntajeTotal + "}";
    }

}
