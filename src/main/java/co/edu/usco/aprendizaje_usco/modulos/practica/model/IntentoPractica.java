package co.edu.usco.aprendizaje_usco.modulos.practica.model;

import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Competencia;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Ejercicio;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.NivelDificultad;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.OpcionEjercicio;
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
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "intento_practica")
@Getter
public class IntentoPractica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ejercicio_id", nullable = false)
    private Ejercicio ejercicio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competencia_id", nullable = false)
    private Competencia competencia;

    @Column(name = "nivel_ejercicio", nullable = false)
    private NivelDificultad nivelEjercicio;

    @Column(name = "numero_intento", nullable = false)
    private short numeroIntento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opcion_ejercicio_id")
    private OpcionEjercicio opcionEjercicio;

    @Column(name = "respuesta_numerica", precision = 20, scale = 6)
    private BigDecimal respuestaNumerica;

    @Column(name = "es_correcta", nullable = false)
    private boolean esCorrecta;

    @Column(name = "tiempo_seg", nullable = false)
    private int tiempoSeg;

    @Column(name = "pista_usada", nullable = false)
    private boolean pistaUsada;

    @Column(name = "clave_idempotencia", nullable = false)
    private UUID claveIdempotencia;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    protected IntentoPractica() {
    }

    public IntentoPractica(Usuario usuario, Ejercicio ejercicio, int numeroIntento,
                           OpcionEjercicio opcionEjercicio, BigDecimal respuestaNumerica,
                           boolean esCorrecta, int tiempoSeg, boolean pistaUsada,
                           UUID claveIdempotencia) {
        this.usuario = Objects.requireNonNull(usuario);
        this.ejercicio = Objects.requireNonNull(ejercicio);
        this.competencia = ejercicio.getCompetencia();
        this.nivelEjercicio = ejercicio.getNivel();
        this.numeroIntento = (short) numeroIntento;
        this.opcionEjercicio = opcionEjercicio;
        this.respuestaNumerica = respuestaNumerica;
        this.esCorrecta = esCorrecta;
        this.tiempoSeg = tiempoSeg;
        this.pistaUsada = pistaUsada;
        this.claveIdempotencia = Objects.requireNonNull(claveIdempotencia);
    }

    public boolean esPrimerIntento() {
        return numeroIntento == 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntentoPractica otro)) return false;
        return id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return IntentoPractica.class.hashCode();
    }

}
