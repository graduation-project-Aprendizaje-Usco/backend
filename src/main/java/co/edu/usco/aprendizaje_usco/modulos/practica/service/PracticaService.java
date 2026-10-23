package co.edu.usco.aprendizaje_usco.modulos.practica.service;

import co.edu.usco.aprendizaje_usco.common.exception.ConflictoException;
import co.edu.usco.aprendizaje_usco.common.exception.RecursoNoEncontradoException;
import co.edu.usco.aprendizaje_usco.common.exception.ReglaNegocioException;
import co.edu.usco.aprendizaje_usco.common.exception.SolicitudInvalidaException;
import co.edu.usco.aprendizaje_usco.modulos.contenido.dto.CompetenciaResponse;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Ejercicio;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.OpcionEjercicio;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.EjercicioRepository;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.EstadoIntento;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.ResultadoDimension;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.repository.ResultadoDimensionRepository;
import co.edu.usco.aprendizaje_usco.modulos.practica.dto.EjercicioPracticaResponse;
import co.edu.usco.aprendizaje_usco.modulos.practica.dto.IntentoPracticaRequest;
import co.edu.usco.aprendizaje_usco.modulos.practica.dto.NivelResponse;
import co.edu.usco.aprendizaje_usco.modulos.practica.dto.OpcionPracticaResponse;
import co.edu.usco.aprendizaje_usco.modulos.practica.dto.ResultadoIntentoResponse;
import co.edu.usco.aprendizaje_usco.modulos.practica.model.IntentoPractica;
import co.edu.usco.aprendizaje_usco.modulos.practica.model.NivelEstudiante;
import co.edu.usco.aprendizaje_usco.modulos.practica.repository.IntentoPracticaRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PracticaService {

    private static final int VENTANA_INTENTOS = 10;

    private final EjercicioRepository ejercicioRepository;
    private final IntentoPracticaRepository intentoRepository;
    private final ResultadoDimensionRepository resultadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NivelEstudianteService nivelEstudianteService;
    private final SeleccionEjercicioService seleccionService;
    private final FeedbackService feedbackService;
    private final Clock clock;

    public PracticaService(EjercicioRepository ejercicioRepository,
                           IntentoPracticaRepository intentoRepository,
                           ResultadoDimensionRepository resultadoRepository,
                           UsuarioRepository usuarioRepository,
                           NivelEstudianteService nivelEstudianteService,
                           SeleccionEjercicioService seleccionService,
                           FeedbackService feedbackService,
                           Clock clock) {
        this.ejercicioRepository = ejercicioRepository;
        this.intentoRepository = intentoRepository;
        this.resultadoRepository = resultadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.nivelEstudianteService = nivelEstudianteService;
        this.seleccionService = seleccionService;
        this.feedbackService = feedbackService;
        this.clock = clock;
    }

    public record Registro(ResultadoIntentoResponse respuesta, boolean creado) {
    }

    @Transactional
    public EjercicioPracticaResponse siguiente(Long usuarioId, String asignatura) {
        List<Ejercicio> candidatos = ejercicioRepository.findActivosPorAsignatura(asignatura);
        if (candidatos.isEmpty()) {
            throw new RecursoNoEncontradoException("No hay ejercicios disponibles para " + asignatura);
        }

        List<SeleccionEjercicioService.Candidato> opciones = candidatos.stream()
                .map(e -> new SeleccionEjercicioService.Candidato(
                        e.getId(), e.getCompetencia().getId(), e.getCompetencia().getCodigo(), e.getNivel()))
                .toList();

        SeleccionEjercicioService.Estado estado = construirEstado(usuarioId, opciones);
        SeleccionEjercicioService.Seleccion seleccion = seleccionService.seleccionar(estado, opciones)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No hay ejercicios disponibles para " + asignatura));

        Ejercicio ejercicio = ejercicioRepository.findActivoConOpciones(seleccion.ejercicio().id())
                .orElseThrow(() -> new RecursoNoEncontradoException("El ejercicio seleccionado no esta disponible"));
        return aRespuesta(ejercicio);
    }

    @Transactional
    public Registro responder(Long usuarioId, IntentoPracticaRequest request) {
        if (request.ejercicioId() == null) {
            throw new SolicitudInvalidaException("El ejercicioId es obligatorio");
        }
        if (request.claveIdempotencia() == null) {
            throw new SolicitudInvalidaException("La clave de idempotencia es obligatoria");
        }
        IntentosPrevios previos = intentosPrevios(usuarioId, request);
        if (previos.existente() != null) {
            return new Registro(respuestaExistente(usuarioId, previos.existente()), false);
        }

        Ejercicio ejercicio = ejercicioRepository.findActivoConOpciones(request.ejercicioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El ejercicio no existe o no esta activo"));
        validarTipoRespuesta(ejercicio, request);

        if (previos.numeroIntento() > 2) {
            throw new ConflictoException("El ejercicio ya tiene dos intentos registrados");
        }
        if (previos.numeroIntento() == 2 && previos.primerIntentoCorrecto()) {
            throw new ConflictoException("El ejercicio ya fue acertado en el primer intento");
        }

        OpcionEjercicio opcion = null;
        BigDecimal respuestaNumerica = null;
        boolean correcta;

        if (ejercicio.esNumerico()) {
            respuestaNumerica = request.respuestaNumerica();
            correcta = evaluarNumerica(ejercicio, respuestaNumerica);
        } else {
            opcion = ejercicio.opcion(request.opcionId())
                    .orElseThrow(() -> new ReglaNegocioException("La opcion no pertenece a este ejercicio"));
            correcta = opcion.isEsCorrecta();
        }

        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);
        IntentoPractica intento = new IntentoPractica(usuario, ejercicio, previos.numeroIntento(),
                opcion, respuestaNumerica, correcta,
                request.tiempoSeg() == null ? 0 : request.tiempoSeg(),
                Boolean.TRUE.equals(request.pistaUsada()),
                request.claveIdempotencia());
        intento = intentoRepository.save(intento);

        if (intento.esPrimerIntento()) {
            nivelEstudianteService.actualizarTrasPrimerIntento(
                    usuarioId, ejercicio.getCompetencia().getId(), correcta, intento);
        }

        NivelResponse nivel = nivelEstudianteService.respuesta(usuarioId, ejercicio.getCompetencia().getId());
        int numero = intento.getNumeroIntento();
        boolean puedeReintentar = !correcta && numero == 1;
        return new Registro(new ResultadoIntentoResponse(
                intento.getId(), numero, correcta, puedeReintentar,
                feedbackService.seleccionar(ejercicio, numero, correcta), nivel), true);
    }

    private IntentosPrevios intentosPrevios(Long usuarioId, IntentoPracticaRequest request) {
        return intentoRepository.findByUsuarioIdAndClaveIdempotencia(usuarioId, request.claveIdempotencia())
                .map(existente -> new IntentosPrevios(existente, 0, false))
                .orElseGet(() -> {
                    long realizados = intentoRepository.countByUsuarioIdAndEjercicioId(usuarioId, request.ejercicioId());
                    boolean primerCorrecto = realizados >= 1 && intentoRepository
                            .existsByUsuarioIdAndEjercicioIdAndNumeroIntentoAndEsCorrectaTrue(
                                    usuarioId, request.ejercicioId(), 1);
                    return new IntentosPrevios(null, (int) realizados + 1, primerCorrecto);
                });
    }

    private ResultadoIntentoResponse respuestaExistente(Long usuarioId, IntentoPractica intento) {
        int numero = intento.getNumeroIntento();
        boolean puedeReintentar = !intento.isEsCorrecta() && numero == 1;
        NivelResponse nivel = nivelEstudianteService.respuesta(usuarioId, intento.getCompetencia().getId());
        return new ResultadoIntentoResponse(intento.getId(), numero, intento.isEsCorrecta(), puedeReintentar,
                feedbackService.seleccionar(intento.getEjercicio(), numero, intento.isEsCorrecta()), nivel);
    }

    private void validarTipoRespuesta(Ejercicio ejercicio, IntentoPracticaRequest request) {
        boolean tieneOpcion = request.opcionId() != null;
        boolean tieneNumero = request.respuestaNumerica() != null;
        if (tieneOpcion == tieneNumero) {
            throw new SolicitudInvalidaException("Debes enviar exactamente una respuesta: opcionId o respuestaNumerica");
        }
        if (ejercicio.esNumerico() && !tieneNumero) {
            throw new SolicitudInvalidaException("Este ejercicio espera una respuesta numerica");
        }
        if (!ejercicio.esNumerico() && !tieneOpcion) {
            throw new SolicitudInvalidaException("Este ejercicio espera una opcion");
        }
        if (request.claveIdempotencia() == null) {
            throw new SolicitudInvalidaException("La clave de idempotencia es obligatoria");
        }
    }

    private boolean evaluarNumerica(Ejercicio ejercicio, BigDecimal respuesta) {
        if (ejercicio.getRespuestaNumerica() == null || ejercicio.getTolerancia() == null) {
            throw new ReglaNegocioException("El ejercicio no tiene una respuesta numerica configurada");
        }
        return respuesta.subtract(ejercicio.getRespuestaNumerica()).abs()
                .compareTo(ejercicio.getTolerancia()) <= 0;
    }

    private SeleccionEjercicioService.Estado construirEstado(
            Long usuarioId, List<SeleccionEjercicioService.Candidato> candidatos) {
        Map<Long, co.edu.usco.aprendizaje_usco.modulos.contenido.model.NivelDificultad> niveles = new LinkedHashMap<>();
        for (Long competenciaId : candidatos.stream().map(SeleccionEjercicioService.Candidato::competenciaId).distinct().toList()) {
            NivelEstudiante nivel = nivelEstudianteService.obtenerOCrear(usuarioId, competenciaId);
            niveles.put(competenciaId, nivel.getNivel());
        }

        Map<Long, co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.NivelDimension> diagnostico = new LinkedHashMap<>();
        for (ResultadoDimension resultado : resultadoRepository
                .findFinalizadosDelUsuario(usuarioId, EstadoIntento.FINALIZADO)) {
            diagnostico.putIfAbsent(resultado.getCompetencia().getId(), resultado.getNivel());
        }

        List<IntentoPractica> recientes = intentoRepository.findUltimosPrimerosIntentos(
                usuarioId, PageRequest.of(0, VENTANA_INTENTOS));
        List<SeleccionEjercicioService.Historial> historial = recientes.stream()
                .map(i -> new SeleccionEjercicioService.Historial(
                        i.getEjercicio().getId(), i.getCompetencia().getId(), i.isEsCorrecta(), i.getCreadoEn()))
                .toList();
        Set<Long> recientesIds = recientes.stream().map(i -> i.getEjercicio().getId()).collect(java.util.stream.Collectors.toSet());
        Set<Long> acertados = intentoRepository.findEjerciciosAcertadosEnPrimerIntento(usuarioId);
        Instant ahora = clock.instant();

        return new SeleccionEjercicioService.Estado(niveles, diagnostico, historial, acertados, recientesIds, ahora);
    }

    private static EjercicioPracticaResponse aRespuesta(Ejercicio ejercicio) {
        List<OpcionPracticaResponse> opciones = ejercicio.getOpciones().stream()
                .map(o -> new OpcionPracticaResponse(o.getId(), o.getLetra(), o.getTexto()))
                .toList();
        return new EjercicioPracticaResponse(
                ejercicio.getId(), ejercicio.getCodigo(), ejercicio.getEnunciado(), ejercicio.getTipo(),
                ejercicio.getNivel().getValor(),
                new CompetenciaResponse(ejercicio.getCompetencia().getCodigo(), ejercicio.getCompetencia().getNombre()),
                opciones, ejercicio.getPista());
    }

    private record IntentosPrevios(IntentoPractica existente, int numeroIntento, boolean primerIntentoCorrecto) {
    }

}
