package co.edu.usco.aprendizaje_usco.modulos.practica.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class NivelEstudianteId implements Serializable {

    private Long usuarioId;
    private Long competenciaId;

    protected NivelEstudianteId() {
    }

    public NivelEstudianteId(Long usuarioId, Long competenciaId) {
        this.usuarioId = usuarioId;
        this.competenciaId = competenciaId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getCompetenciaId() {
        return competenciaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NivelEstudianteId otro)) return false;
        return Objects.equals(usuarioId, otro.usuarioId)
                && Objects.equals(competenciaId, otro.competenciaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuarioId, competenciaId);
    }

}
