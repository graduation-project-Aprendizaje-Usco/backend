package co.edu.usco.aprendizaje_usco.modulos.usuario.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@Getter
@EqualsAndHashCode
public class GrupoEstudianteId implements Serializable {

    private Long grupoId;
    private Long estudianteId;

    protected GrupoEstudianteId() {
    }

    public GrupoEstudianteId(Long grupoId, Long estudianteId) {
        this.grupoId = grupoId;
        this.estudianteId = estudianteId;
    }
}
