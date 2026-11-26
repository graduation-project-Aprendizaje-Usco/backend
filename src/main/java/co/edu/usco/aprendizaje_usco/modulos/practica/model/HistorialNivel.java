package co.edu.usco.aprendizaje_usco.modulos.practica.model;

import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Competencia;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.NivelDificultad;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.IntentoDiagnostico;
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
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "historial_nivel")
@Getter
public class HistorialNivel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competencia_id", nullable = false)
    private Competencia competencia;

    @Column(name = "nivel_anterior")
    private NivelDificultad nivelAnterior;

    @Column(name = "nivel_nuevo", nullable = false)
    private NivelDificultad nivelNuevo;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false, length = 30)
    private MotivoCambioNivel motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intento_practica_id")
    private IntentoPractica intentoPractica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intento_diagnostico_id")
    private IntentoDiagnostico intentoDiagnostico;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    protected HistorialNivel() {
    }

    public HistorialNivel(Usuario usuario, Competencia competencia, NivelDificultad nivelAnterior,
                          NivelDificultad nivelNuevo, MotivoCambioNivel motivo,
                          IntentoPractica intentoPractica, IntentoDiagnostico intentoDiagnostico) {
        this.usuario = Objects.requireNonNull(usuario);
        this.competencia = Objects.requireNonNull(competencia);
        this.nivelAnterior = nivelAnterior;
        this.nivelNuevo = Objects.requireNonNull(nivelNuevo);
        this.motivo = Objects.requireNonNull(motivo);
        this.intentoPractica = intentoPractica;
        this.intentoDiagnostico = intentoDiagnostico;
    }

}
