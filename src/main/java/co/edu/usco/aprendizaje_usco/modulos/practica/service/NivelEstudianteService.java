package co.edu.usco.aprendizaje_usco.modulos.practica.service;

import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Competencia;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.NivelDificultad;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.CompetenciaRepository;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.event.DiagnosticoFinalizadoEvent;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.EstadoIntento;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.IntentoDiagnostico;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.ResultadoDimension;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.repository.ResultadoDimensionRepository;
import co.edu.usco.aprendizaje_usco.modulos.practica.config.ReglasNivelProperties;
import co.edu.usco.aprendizaje_usco.modulos.practica.dto.NivelResponse;
import co.edu.usco.aprendizaje_usco.modulos.practica.model.HistorialNivel;
import co.edu.usco.aprendizaje_usco.modulos.practica.model.IntentoPractica;
import co.edu.usco.aprendizaje_usco.modulos.practica.model.MotivoCambioNivel;
import co.edu.usco.aprendizaje_usco.modulos.practica.model.NivelEstudiante;
import co.edu.usco.aprendizaje_usco.modulos.practica.model.NivelEstudianteId;
import co.edu.usco.aprendizaje_usco.modulos.practica.repository.HistorialNivelRepository;
import co.edu.usco.aprendizaje_usco.modulos.practica.repository.NivelEstudianteRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NivelEstudianteService {

    private final NivelEstudianteRepository nivelRepository;
    private final HistorialNivelRepository historialRepository;
    private final UsuarioRepository usuarioRepository;
    private final CompetenciaRepository competenciaRepository;
    private final ResultadoDimensionRepository resultadoRepository;
    private final ReglasNivelProperties reglas;
    private final EntityManager entityManager;

    public NivelEstudianteService(NivelEstudianteRepository nivelRepository,
                                  HistorialNivelRepository historialRepository,
                                  UsuarioRepository usuarioRepository,
                                  CompetenciaRepository competenciaRepository,
                                  ResultadoDimensionRepository resultadoRepository,
                                  ReglasNivelProperties reglas,
                                  EntityManager entityManager) {
        this.nivelRepository = nivelRepository;
        this.historialRepository = historialRepository;
        this.usuarioRepository = usuarioRepository;
        this.competenciaRepository = competenciaRepository;
        this.resultadoRepository = resultadoRepository;
        this.reglas = reglas;
        this.entityManager = entityManager;
    }

    /**
     * Inicializa los niveles de un diagnostico recien finalizado. Es idempotente:
     * si el estudiante ya tiene un nivel de practica, no lo sobrescribe.
     */
    @Transactional
    public void inicializarDesdeResultados(Long usuarioId, Long intentoDiagnosticoId,
                                           List<DiagnosticoFinalizadoEvent.ResultadoDimensionSnapshot> resultados) {
        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);
        IntentoDiagnostico intento = entityManager.getReference(IntentoDiagnostico.class, intentoDiagnosticoId);
        for (DiagnosticoFinalizadoEvent.ResultadoDimensionSnapshot resultado : resultados) {
            NivelEstudianteId id = new NivelEstudianteId(usuarioId, resultado.competenciaId());
            if (nivelRepository.existsById(id)) {
                continue;
            }
            Competencia competencia = competenciaRepository.getReferenceById(resultado.competenciaId());
            NivelDificultad nivel = nivelDesdeDiagnostico(resultado.nivel());
            NivelEstudiante nuevo = nivelRepository.save(new NivelEstudiante(usuario, competencia, nivel, 0, 0));
            historialRepository.save(new HistorialNivel(usuario, competencia, null, nivel,
                    MotivoCambioNivel.DIAGNOSTICO, null, intento));
        }
    }

    @Transactional
    public NivelEstudiante obtenerOCrear(Long usuarioId, Long competenciaId) {
        NivelEstudianteId id = new NivelEstudianteId(usuarioId, competenciaId);
        return nivelRepository.findById(id).orElseGet(() -> crearInicial(usuarioId, competenciaId));
    }

    @Transactional
    public NivelEstudiante actualizarTrasPrimerIntento(Long usuarioId, Long competenciaId,
                                                       boolean correcto, IntentoPractica intento) {
        NivelEstudiante actual = obtenerOCrear(usuarioId, competenciaId);
        NivelEstudiante bloqueado = nivelRepository.findParaActualizar(actual.getId()).orElse(actual);
        NivelDificultad nivelAnterior = bloqueado.getNivel();

        NivelService.Cambio cambio = NivelService.evaluar(
                new NivelService.Estado(bloqueado.getNivel(), bloqueado.getRachaAciertos(), bloqueado.getRachaErrores()),
                correcto, reglas);
        bloqueado.actualizar(cambio.nivel(), cambio.rachaAciertos(), cambio.rachaErrores());
        NivelEstudiante guardado = nivelRepository.save(bloqueado);

        if (cambio.cambio()) {
            historialRepository.save(new HistorialNivel(bloqueado.getUsuario(), bloqueado.getCompetencia(),
                    nivelAnterior, cambio.nivel(), cambio.motivo(), intento, null));
        }
        return guardado;
    }

    @Transactional(readOnly = true)
    public NivelResponse respuesta(Long usuarioId, Long competenciaId) {
        NivelEstudianteId id = new NivelEstudianteId(usuarioId, competenciaId);
        NivelEstudiante nivel = nivelRepository.findById(id).orElseThrow();
        return new NivelResponse(nivel.getCompetencia().getCodigo(), nivel.getNivel().getValor(),
                nivel.getRachaAciertos(), nivel.getRachaErrores());
    }

    private NivelEstudiante crearInicial(Long usuarioId, Long competenciaId) {
        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);
        Competencia competencia = competenciaRepository.getReferenceById(competenciaId);

        Optional<ResultadoDimension> diagnostico = resultadoRepository
                .findFinalizadosDelUsuario(usuarioId, EstadoIntento.FINALIZADO).stream()
                .filter(r -> r.getCompetencia().getId().equals(competenciaId))
                .findFirst();

        NivelDificultad nivel = diagnostico
                .map(r -> nivelDesdeDiagnostico(r.getNivel()))
                .orElseGet(() -> NivelDificultad.desde(reglas.nivelInicialSinDiagnostico()));
        MotivoCambioNivel motivo = diagnostico.isPresent()
                ? MotivoCambioNivel.DIAGNOSTICO
                : MotivoCambioNivel.INICIALIZACION;
        IntentoDiagnostico intento = diagnostico.map(ResultadoDimension::getIntento).orElse(null);

        NivelEstudiante nuevo = nivelRepository.save(new NivelEstudiante(usuario, competencia, nivel, 0, 0));
        historialRepository.save(new HistorialNivel(usuario, competencia, null, nivel, motivo, null, intento));
        return nuevo;
    }

    private static NivelDificultad nivelDesdeDiagnostico(co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.NivelDimension nivel) {
        return switch (nivel) {
            case BAJO -> NivelDificultad.BASICO;
            case MEDIO -> NivelDificultad.INTERMEDIO;
            case ALTO -> NivelDificultad.AVANZADO;
        };
    }

}
