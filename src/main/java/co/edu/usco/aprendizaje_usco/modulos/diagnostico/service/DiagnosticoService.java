package co.edu.usco.aprendizaje_usco.modulos.diagnostico.service;

import co.edu.usco.aprendizaje_usco.common.exception.ConflictoException;
import co.edu.usco.aprendizaje_usco.common.exception.NoAutenticadoException;
import co.edu.usco.aprendizaje_usco.common.exception.ProhibidoException;
import co.edu.usco.aprendizaje_usco.common.exception.RecursoNoEncontradoException;
import co.edu.usco.aprendizaje_usco.common.exception.ReglaNegocioException;
import co.edu.usco.aprendizaje_usco.common.util.MezcladorOpciones;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Competencia;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.config.UmbralesDiagnostico;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.dto.DimensionResponse;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.event.DiagnosticoFinalizadoEvent;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.dto.IntentoResponse;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.dto.ItemDiagnosticoResponse;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.dto.OpcionResponse;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.dto.RespuestaGuardadaResponse;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.dto.ResultadoDimensionResponse;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.dto.ResultadoResponse;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.EstadoIntento;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.IntentoDiagnostico;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.Item;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.Opcion;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.PruebaDiagnostica;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.RespuestaDiagnostico;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.ResultadoDimension;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.repository.IntentoDiagnosticoRepository;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.repository.ItemRepository;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.repository.PruebaDiagnosticaRepository;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.repository.RespuestaDiagnosticoRepository;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.repository.ResultadoDimensionRepository;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.service.CalculadoraPuntaje.PreguntaCalificable;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.service.CalculadoraPuntaje.ResultadoCalculado;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.service.CalculadoraPuntaje.ResultadoDimensionCalculado;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bloque 4: aplicación de la prueba diagnóstica (iniciar, responder, finalizar).
 *
 * Reglas:
 *  - Un estudiante presenta UNA vez cada prueba: si ya tiene intento (en curso o
 *    finalizado), "iniciar" devuelve ese mismo intento.
 *  - El tiempo lo controla el servidor. Un intento vencido se finaliza "por tiempo"
 *    en cuanto alguien lo toca (iniciar, consultar, responder) o, a más tardar, en el
 *    siguiente barrido programado (BarridoIntentosVencidos).
 *  - Se acepta una respuesta que llega hasta {@code app.diagnostico.gracia-segundos}
 *    después del vencimiento (latencia de red); pasada esa tolerancia, se rechaza.
 *  - Las claves (opción correcta) nunca salen en las respuestas de la API.
 *  - Las opciones se barajan por intento: la letra que ve el estudiante es la
 *    posición mostrada, no la letra almacenada. La calificación usa el id.
 */
@Service
public class DiagnosticoService {

    private static final Logger log = LoggerFactory.getLogger(DiagnosticoService.class);

    private final PruebaDiagnosticaRepository pruebaRepository;
    private final ItemRepository itemRepository;
    private final IntentoDiagnosticoRepository intentoRepository;
    private final RespuestaDiagnosticoRepository respuestaRepository;
    private final ResultadoDimensionRepository resultadoRepository;
    private final Clock clock;
    private final Duration gracia;
    private final UmbralesDiagnostico umbrales;
    private final ApplicationEventPublisher eventPublisher;

    public DiagnosticoService(PruebaDiagnosticaRepository pruebaRepository,
                              ItemRepository itemRepository,
                              IntentoDiagnosticoRepository intentoRepository,
                              RespuestaDiagnosticoRepository respuestaRepository,
                              ResultadoDimensionRepository resultadoRepository,
                              Clock clock,
                              UmbralesDiagnostico umbrales,
                              ApplicationEventPublisher eventPublisher,
                              @Value("${app.diagnostico.gracia-segundos:5}") long graciaSegundos) {
        this.pruebaRepository = pruebaRepository;
        this.itemRepository = itemRepository;
        this.intentoRepository = intentoRepository;
        this.respuestaRepository = respuestaRepository;
        this.resultadoRepository = resultadoRepository;
        this.clock = clock;
        this.gracia = Duration.ofSeconds(graciaSegundos);
        this.umbrales = umbrales;
        this.eventPublisher = eventPublisher;
    }

    /** Resultado de iniciar: el intento y si se acaba de crear (201) o ya existía (200). */
    public record Inicio(IntentoResponse intento, boolean creado) {
    }

    // ------------------------------------------------------------------ operaciones

    /** Crea el intento del estudiante en la prueba activa, o devuelve el que ya tiene. */
    @Transactional
    public Inicio iniciar(Long usuarioId) {
        PruebaDiagnostica prueba = pruebaRepository.findFirstByActivaTrue()
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No hay una prueba diagnóstica disponible en este momento"));

        // Bloquea al estudiante: dos clics seguidos en "Iniciar" no crean dos intentos.
        Usuario usuario = intentoRepository.bloquearUsuario(usuarioId)
                .orElseThrow(() -> new NoAutenticadoException("Debes iniciar sesión para continuar"));

        Optional<IntentoDiagnostico> existente =
                intentoRepository.findFirstByUsuarioIdAndPruebaIdOrderByIniciadoEnDesc(usuarioId, prueba.getId());
        if (existente.isPresent()) {
            IntentoDiagnostico intento = existente.get();
            finalizarSiVencido(intento);
            return new Inicio(armar(intento), false);
        }

        IntentoDiagnostico nuevo = intentoRepository.save(new IntentoDiagnostico(usuario, prueba, ahora()));
        log.info("Intento diagnóstico {} iniciado por el usuario {}", nuevo.getId(), usuarioId);
        return new Inicio(armar(nuevo), true);
    }

    /** El intento del estudiante en la prueba activa, si lo tiene. */
    @Transactional
    public Optional<IntentoResponse> actual(Long usuarioId) {
        return pruebaRepository.findFirstByActivaTrue()
                .flatMap(prueba -> intentoRepository
                        .findFirstByUsuarioIdAndPruebaIdOrderByIniciadoEnDesc(usuarioId, prueba.getId()))
                .map(intento -> intentoRepository.findParaActualizar(intento.getId()).orElseThrow())
                .map(intento -> {
                    finalizarSiVencido(intento);
                    return armar(intento);
                });
    }

    @Transactional
    public IntentoResponse obtener(Long intentoId, Long usuarioId) {
        IntentoDiagnostico intento = cargarPropio(intentoId, usuarioId);
        finalizarSiVencido(intento);
        return armar(intento);
    }

    /**
     * Solo el resultado (puntaje y categorías) de un intento finalizado.
     * Si el tiempo ya se acabó, lo finaliza en ese momento; si sigue en curso, 409.
     */
    @Transactional
    public ResultadoResponse resultado(Long intentoId, Long usuarioId) {
        IntentoDiagnostico intento = cargarPropio(intentoId, usuarioId);
        finalizarSiVencido(intento);
        if (intento.estaEnCurso()) {
            throw new ConflictoException("La prueba aún no ha finalizado; el resultado estará disponible al entregarla");
        }
        return resultado(intento);
    }

    /**
     * Guarda (o cambia) la respuesta de un ítem.
     *
     * noRollbackFor: si el tiempo ya se acabó, el intento se finaliza por tiempo y
     * ADEMÁS se responde 422; sin esto, la excepción desharía la finalización.
     */
    @Transactional(noRollbackFor = ReglaNegocioException.class)
    public RespuestaGuardadaResponse responder(Long intentoId, Long itemId, Long opcionId, Long usuarioId) {
        IntentoDiagnostico intento = cargarPropio(intentoId, usuarioId);
        if (!intento.estaEnCurso()) {
            throw new ConflictoException("La prueba ya fue finalizada; no se pueden cambiar las respuestas");
        }
        if (finalizarSiVencido(intento)) {
            throw new ReglaNegocioException("Se acabó el tiempo de la prueba; la respuesta no se guardó");
        }

        Item item = itemRepository.findActivoDePrueba(itemId, intento.getPrueba().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("La pregunta no existe en esta prueba"));
        Opcion opcion = item.opcion(opcionId)
                .orElseThrow(() -> new ReglaNegocioException("La opción elegida no pertenece a esta pregunta"));

        Instant ahora = ahora();
        RespuestaDiagnostico respuesta = respuestaRepository.findByIntentoIdAndItemId(intentoId, itemId)
                .map(existente -> {
                    existente.cambiar(opcion, ahora);
                    return existente;
                })
                .orElseGet(() -> respuestaRepository.save(new RespuestaDiagnostico(intento, item, opcion, ahora)));
        return new RespuestaGuardadaResponse(itemId, opcion.getId(), respuesta.getRespondidaEn());
    }

    /**
     * El estudiante entrega la prueba. Es idempotente: si ya estaba finalizada
     * (por él o por tiempo), devuelve el mismo resultado sin recalcular.
     */
    @Transactional
    public IntentoResponse finalizar(Long intentoId, Long usuarioId) {
        IntentoDiagnostico intento = cargarPropio(intentoId, usuarioId);
        if (intento.estaEnCurso() && !finalizarSiVencido(intento)) {
            calificarYCerrar(intento, false);
            log.info("Intento diagnóstico {} finalizado por el estudiante", intentoId);
        }
        return armar(intento);
    }

    /**
     * Usado por el barrido programado: finaliza por tiempo un intento vencido.
     * Cada intento va en su propia transacción para que un error en uno no frene los demás.
     *
     * @return true si lo finalizó
     */
    @Transactional
    public boolean finalizarSiVencido(Long intentoId) {
        return intentoRepository.findParaActualizar(intentoId)
                .map(this::finalizarSiVencido)
                .orElse(false);
    }

    // ------------------------------------------------------------------ reglas internas

    /** Carga el intento bloqueado y verifica que sea del estudiante (404 / 403). */
    private IntentoDiagnostico cargarPropio(Long intentoId, Long usuarioId) {
        IntentoDiagnostico intento = intentoRepository.findParaActualizar(intentoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El intento no existe"));
        if (!intento.perteneceA(usuarioId)) {
            throw new ProhibidoException("Este intento pertenece a otro estudiante");
        }
        return intento;
    }

    private boolean finalizarSiVencido(IntentoDiagnostico intento) {
        if (!intento.estaVencido(ahora(), gracia)) {
            return false;
        }
        calificarYCerrar(intento, true);
        log.info("Intento diagnóstico {} finalizado por tiempo", intento.getId());
        return true;
    }

    /** Bloque 5: califica con CalculadoraPuntaje, guarda el detalle por competencia y cierra el intento. */
    private void calificarYCerrar(IntentoDiagnostico intento, boolean porTiempo) {
        List<Item> items = itemRepository.findActivosConOpciones(intento.getPrueba().getId());
        Map<Long, Competencia> competencias = items.stream()
                .map(Item::getCompetencia)
                .collect(Collectors.toMap(Competencia::getId, Function.identity(), (a, b) -> a));

        List<PreguntaCalificable> preguntas = items.stream()
                .map(item -> new PreguntaCalificable(item.getId(), item.getCompetencia().getId(),
                        item.opcionCorrecta()
                                .orElseThrow(() -> new IllegalStateException("El ítem " + item.getId() + " no tiene clave"))
                                .getId()))
                .toList();
        Map<Long, Long> respuestas = respuestaRepository.findDelIntento(intento.getId()).stream()
                .collect(Collectors.toMap(r -> r.getItem().getId(), r -> r.getOpcion().getId()));

        ResultadoCalculado resultado = CalculadoraPuntaje.calcular(preguntas, respuestas, umbrales);
        for (ResultadoDimensionCalculado dimension : resultado.dimensiones()) {
            resultadoRepository.save(new ResultadoDimension(intento, competencias.get(dimension.competenciaId()),
                    dimension.aciertos(), dimension.total(), dimension.porcentaje(), dimension.nivel()));
        }
        intento.finalizar(resultado.puntaje(), resultado.categoria(), ahora(), porTiempo);
        intentoRepository.save(intento);

        eventPublisher.publishEvent(new DiagnosticoFinalizadoEvent(
                intento.getUsuario().getId(), intento.getId(),
                resultado.dimensiones().stream()
                        .map(d -> new DiagnosticoFinalizadoEvent.ResultadoDimensionSnapshot(
                                d.competenciaId(), d.nivel(), d.porcentaje()))
                        .toList()));
    }

    // ------------------------------------------------------------------ armado de la respuesta

    private IntentoResponse armar(IntentoDiagnostico intento) {
        PruebaDiagnostica prueba = intento.getPrueba();
        Instant ahora = ahora();
        boolean enCurso = intento.getEstado() == EstadoIntento.EN_CURSO;

        List<ItemDiagnosticoResponse> items = enCurso
                ? itemRepository.findActivosConOpciones(prueba.getId()).stream()
                        .map(item -> item(intento.getId(), item)).toList()
                : List.of();
        int totalItems = enCurso ? items.size() : (int) itemRepository.countByPruebaIdAndActivoTrue(prueba.getId());
        List<RespuestaGuardadaResponse> respuestas = respuestaRepository.findDelIntento(intento.getId()).stream()
                .map(r -> new RespuestaGuardadaResponse(r.getItem().getId(), r.getOpcion().getId(), r.getRespondidaEn()))
                .sorted((a, b) -> Long.compare(a.itemId(), b.itemId()))
                .toList();

        return new IntentoResponse(intento.getId(), intento.getEstado().name(),
                prueba.getNombre(), intento.getIniciadoEn(), intento.venceEn(), intento.segundosRestantes(ahora),
                totalItems, respuestas.size(), respuestas, enCurso ? null : resultado(intento), items);
    }

    private ResultadoResponse resultado(IntentoDiagnostico intento) {
        List<ResultadoDimension> dimensiones = resultadoRepository.findDelIntento(intento.getId());
        int total = dimensiones.stream().mapToInt(ResultadoDimension::getTotal).sum();
        return new ResultadoResponse(intento.getPuntajeTotal(), total,
                intento.getCategoria().name(), intento.getCategoria().getDescripcion(),
                intento.isFinalizadoPorTiempo(), intento.getFinalizadoEn(),
                dimensiones.stream().map(d -> new ResultadoDimensionResponse(
                        d.getCompetencia().getCodigo(), d.getCompetencia().getNombre(),
                        d.getAciertos(), d.getTotal(), d.getPorcentaje(),
                        d.getNivel().name(), d.getNivel().getDescripcion())).toList());
    }

    private static ItemDiagnosticoResponse item(long intentoId, Item item) {
        List<Opcion> opciones = MezcladorOpciones.mezclar(item.getOpciones(), intentoId, item.getId());
        List<OpcionResponse> opcionesPresentadas = new ArrayList<>(opciones.size());
        for (int posicion = 0; posicion < opciones.size(); posicion++) {
            Opcion opcion = opciones.get(posicion);
            opcionesPresentadas.add(new OpcionResponse(
                    opcion.getId(), MezcladorOpciones.letra(posicion), opcion.getTexto()));
        }
        return new ItemDiagnosticoResponse(item.getId(), item.getNumero(), item.getEnunciado(),
                item.getImagen(), item.getImagenAlt(),
                new DimensionResponse(item.getCompetencia().getCodigo(), item.getCompetencia().getNombre()),
                opcionesPresentadas);
    }

    private Instant ahora() {
        return clock.instant();
    }

}
