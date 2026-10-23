package co.edu.usco.aprendizaje_usco.modulos.practica.model;

import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Competencia;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.NivelDificultad;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
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
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "nivel_estudiante")
@Getter
public class NivelEstudiante {

    @EmbeddedId
    private NivelEstudianteId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("competenciaId")
    @JoinColumn(name = "competencia_id", nullable = false)
    private Competencia competencia;

    @Column(name = "nivel", nullable = false)
    private NivelDificultad nivel;

    @Column(name = "racha_aciertos", nullable = false)
    private short rachaAciertos;

    @Column(name = "racha_errores", nullable = false)
    private short rachaErrores;

    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    protected NivelEstudiante() {
    }

    public NivelEstudiante(Usuario usuario, Competencia competencia, NivelDificultad nivel,
                           int rachaAciertos, int rachaErrores) {
        this.usuario = Objects.requireNonNull(usuario);
        this.competencia = Objects.requireNonNull(competencia);
        this.id = new NivelEstudianteId(usuario.getId(), competencia.getId());
        this.nivel = Objects.requireNonNull(nivel);
        this.rachaAciertos = (short) rachaAciertos;
        this.rachaErrores = (short) rachaErrores;
    }

    public void actualizar(NivelDificultad nivel, int rachaAciertos, int rachaErrores) {
        this.nivel = Objects.requireNonNull(nivel);
        this.rachaAciertos = (short) rachaAciertos;
        this.rachaErrores = (short) rachaErrores;
    }

}
