package co.edu.usco.aprendizaje_usco.modulos.usuario.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "grupo_estudiante")
@Getter
public class GrupoEstudiante {

    @EmbeddedId
    private GrupoEstudianteId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("grupoId")
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("estudianteId")
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Usuario estudiante;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    protected GrupoEstudiante() {
    }

    public GrupoEstudiante(Grupo grupo, Usuario estudiante) {
        this.grupo = Objects.requireNonNull(grupo);
        this.estudiante = Objects.requireNonNull(estudiante);
        this.id = new GrupoEstudianteId(grupo.getId(), estudiante.getId());
    }
}
