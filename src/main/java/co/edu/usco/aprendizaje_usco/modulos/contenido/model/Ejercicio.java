package co.edu.usco.aprendizaje_usco.modulos.contenido.model;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "ejercicio")
@Getter
public class Ejercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contenido_id", nullable = false)
    private Contenido contenido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competencia_id", nullable = false)
    private Competencia competencia;

    @Column(name = "codigo", nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(name = "nivel", nullable = false)
    private NivelDificultad nivel;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoEjercicio tipo;

    @Column(name = "enunciado", nullable = false, length = 2000)
    private String enunciado;

    @Column(name = "respuesta_numerica", precision = 20, scale = 6)
    private BigDecimal respuestaNumerica;

    @Column(name = "tolerancia", precision = 20, scale = 6)
    private BigDecimal tolerancia;

    @Column(name = "pista", nullable = false, length = 1000)
    private String pista;

    @Column(name = "explicacion", nullable = false, length = 2000)
    private String explicacion;

    @Column(name = "error_comun", length = 1000)
    private String errorComun;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por")
    private Usuario actualizadoPor;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    @OneToMany(mappedBy = "ejercicio", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("letra ASC")
    private List<OpcionEjercicio> opciones = new ArrayList<>();

    protected Ejercicio() {
    }

    public Ejercicio(Contenido contenido, String codigo, NivelDificultad nivel, TipoEjercicio tipo,
                     String enunciado, BigDecimal respuestaNumerica, BigDecimal tolerancia,
                     String pista, String explicacion, String errorComun, Usuario creadoPor) {
        this.contenido = Objects.requireNonNull(contenido);
        this.competencia = contenido.getCompetencia();
        this.codigo = Objects.requireNonNull(codigo).trim();
        this.nivel = Objects.requireNonNull(nivel);
        this.tipo = Objects.requireNonNull(tipo);
        this.enunciado = Objects.requireNonNull(enunciado).trim();
        this.respuestaNumerica = respuestaNumerica;
        this.tolerancia = tolerancia;
        this.pista = Objects.requireNonNull(pista).trim();
        this.explicacion = Objects.requireNonNull(explicacion).trim();
        this.errorComun = errorComun;
        this.creadoPor = creadoPor;
    }

    public void agregarOpcion(OpcionEjercicio opcion) {
        opciones.add(opcion);
    }

    public void reemplazarOpciones(List<OpcionEjercicio> nuevas) {
        opciones.clear();
        opciones.addAll(nuevas);
    }

    public void actualizarContenido(Contenido contenido) {
        this.contenido = Objects.requireNonNull(contenido);
        this.competencia = contenido.getCompetencia();
    }

    public void actualizarTipoRespuesta(TipoEjercicio tipo, BigDecimal respuestaNumerica, BigDecimal tolerancia) {
        this.tipo = Objects.requireNonNull(tipo);
        this.respuestaNumerica = respuestaNumerica;
        this.tolerancia = tolerancia;
    }

    public void actualizarDatos(String enunciado, NivelDificultad nivel, String pista, String explicacion,
                                String errorComun, Usuario actualizadoPor) {
        this.enunciado = Objects.requireNonNull(enunciado).trim();
        this.nivel = Objects.requireNonNull(nivel);
        this.pista = Objects.requireNonNull(pista).trim();
        this.explicacion = Objects.requireNonNull(explicacion).trim();
        this.errorComun = errorComun;
        this.actualizadoPor = actualizadoPor;
    }

    public Optional<OpcionEjercicio> opcionCorrecta() {
        return opciones.stream().filter(OpcionEjercicio::isEsCorrecta).findFirst();
    }

    public Optional<OpcionEjercicio> opcion(Long opcionId) {
        return opciones.stream().filter(o -> o.getId().equals(opcionId)).findFirst();
    }

    public boolean esNumerico() {
        return tipo == TipoEjercicio.NUMERICA;
    }

    public void desactivar() {
        this.activo = false;
    }

    public void activar() {
        this.activo = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ejercicio otro)) return false;
        return id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Ejercicio.class.hashCode();
    }

    @Override
    public String toString() {
        return "Ejercicio{id=" + id + ", codigo=" + codigo + ", nivel=" + nivel + "}";
    }

}
