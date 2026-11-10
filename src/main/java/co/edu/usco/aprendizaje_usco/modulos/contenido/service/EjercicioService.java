package co.edu.usco.aprendizaje_usco.modulos.contenido.service;

import co.edu.usco.aprendizaje_usco.common.exception.ConflictoException;
import co.edu.usco.aprendizaje_usco.common.exception.ProhibidoException;
import co.edu.usco.aprendizaje_usco.common.exception.RecursoNoEncontradoException;
import co.edu.usco.aprendizaje_usco.common.exception.ReglaNegocioException;
import co.edu.usco.aprendizaje_usco.common.exception.SolicitudInvalidaException;
import co.edu.usco.aprendizaje_usco.modulos.contenido.dto.EjercicioRequest;
import co.edu.usco.aprendizaje_usco.modulos.contenido.dto.EjercicioResponse;
import co.edu.usco.aprendizaje_usco.modulos.contenido.dto.OpcionEjercicioRequest;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Contenido;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Ejercicio;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.NivelDificultad;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.OpcionEjercicio;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.TipoEjercicio;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.ContenidoRepository;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.EjercicioRepository;
import co.edu.usco.aprendizaje_usco.modulos.practica.repository.IntentoPracticaRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EjercicioService {

    private static final Set<String> LETRAS = Set.of("A", "B", "C", "D");

    private final EjercicioRepository ejercicioRepository;
    private final ContenidoRepository contenidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final IntentoPracticaRepository intentoRepository;

    public EjercicioService(EjercicioRepository ejercicioRepository,
                            ContenidoRepository contenidoRepository,
                            UsuarioRepository usuarioRepository,
                            IntentoPracticaRepository intentoRepository) {
        this.ejercicioRepository = ejercicioRepository;
        this.contenidoRepository = contenidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.intentoRepository = intentoRepository;
    }

    public List<EjercicioResponse> listar(Long docenteId, Long contenidoId, String competencia,
                                          Integer nivel, TipoEjercicio tipo, Boolean activo, String texto) {
        NivelDificultad nivelFiltro = null;
        if (nivel != null) {
            if (nivel < 1 || nivel > 3) {
                throw new SolicitudInvalidaException("El nivel debe estar entre 1 y 3");
            }
            nivelFiltro = NivelDificultad.desde(nivel);
        }
        String competenciaFiltro = competencia == null || competencia.isBlank()
                ? null : competencia.trim().toUpperCase(Locale.ROOT);
        String busqueda = texto == null || texto.isBlank() ? "" : texto.trim();

        return ejercicioRepository.buscarDelDocente(docenteId, contenidoId, competenciaFiltro,
                        nivelFiltro, tipo, activo, busqueda).stream()
                .map(EjercicioResponse::desde)
                .toList();
    }

    @Transactional
    public EjercicioResponse crear(Long docenteId, EjercicioRequest request) {
        Contenido contenido = contenidoPropio(request.contenidoId(), docenteId);
        if (!contenido.isActivo()) {
            throw new ReglaNegocioException("No se puede crear un ejercicio en un contenido inactivo");
        }
        DatosValidados datos = validar(request);
        String codigo = codigoNuevo(request.codigo());
        Usuario docente = usuarioRepository.getReferenceById(docenteId);

        Ejercicio ejercicio = new Ejercicio(
                contenido,
                codigo,
                datos.nivel(),
                request.tipo(),
                request.enunciado(),
                datos.respuestaNumerica(),
                datos.tolerancia(),
                request.pista(),
                request.explicacion(),
                request.errorComun(),
                docente);
        ejercicio.reemplazarOpciones(opciones(ejercicio, datos.opciones()));
        return EjercicioResponse.desde(ejercicioRepository.saveAndFlush(ejercicio));
    }

    @Transactional
    public EjercicioResponse actualizar(Long docenteId, Long ejercicioId, EjercicioRequest request) {
        Ejercicio ejercicio = propio(ejercicioId, docenteId);
        if (!ejercicio.getContenido().getId().equals(request.contenidoId())) {
            throw new ConflictoException("El contenido de un ejercicio no se puede cambiar");
        }
        if (request.codigo() != null && !request.codigo().isBlank()
                && !request.codigo().trim().equalsIgnoreCase(ejercicio.getCodigo())) {
            throw new ConflictoException("El código de un ejercicio no se puede cambiar");
        }

        DatosValidados datos = validar(request);
        boolean tieneIntentos = intentoRepository.existsByEjercicioId(ejercicioId);

        if (tieneIntentos) {
            if (ejercicio.getTipo() != request.tipo()
                    || !mismaRespuesta(ejercicio.getRespuestaNumerica(), datos.respuestaNumerica())
                    || !mismaRespuesta(ejercicio.getTolerancia(), datos.tolerancia())
                    || !mismasOpciones(ejercicio, datos.opciones())) {
                throw new ConflictoException(
                        "El ejercicio tiene intentos: solo se pueden cambiar enunciado, nivel, pista, "
                                + "explicación y error común");
            }
        } else {
            ejercicio.actualizarTipoRespuesta(request.tipo(), datos.respuestaNumerica(), datos.tolerancia());
            ejercicio.reemplazarOpciones(opciones(ejercicio, datos.opciones()));
        }

        ejercicio.actualizarDatos(request.enunciado(), datos.nivel(), request.pista(),
                request.explicacion(), request.errorComun(),
                usuarioRepository.getReferenceById(docenteId));
        return EjercicioResponse.desde(ejercicioRepository.saveAndFlush(ejercicio));
    }

    @Transactional
    public EjercicioResponse cambiarEstado(Long docenteId, Long ejercicioId, boolean activo) {
        Ejercicio ejercicio = propio(ejercicioId, docenteId);
        if (activo) {
            ejercicio.activar();
        } else {
            ejercicio.desactivar();
        }
        return EjercicioResponse.desde(ejercicioRepository.saveAndFlush(ejercicio));
    }

    @Transactional
    public void eliminar(Long docenteId, Long ejercicioId) {
        Ejercicio ejercicio = propio(ejercicioId, docenteId);
        if (intentoRepository.existsByEjercicioId(ejercicioId)) {
            throw new ConflictoException(
                    "El ejercicio tiene intentos; desactívalo en lugar de eliminarlo");
        }
        ejercicioRepository.delete(ejercicio);
    }

    private Ejercicio propio(Long ejercicioId, Long docenteId) {
        return ejercicioRepository.findPropioConOpciones(ejercicioId, docenteId)
                .orElseGet(() -> {
                    if (ejercicioRepository.existsById(ejercicioId)) {
                        throw new ProhibidoException("No tienes permiso para gestionar este ejercicio");
                    }
                    throw new RecursoNoEncontradoException("Ejercicio", ejercicioId);
                });
    }

    private Contenido contenidoPropio(Long contenidoId, Long docenteId) {
        return contenidoRepository.findByIdAndCreadoPor_Id(contenidoId, docenteId)
                .orElseGet(() -> {
                    if (contenidoRepository.existsById(contenidoId)) {
                        throw new ProhibidoException("No tienes permiso para usar este contenido");
                    }
                    throw new RecursoNoEncontradoException("Contenido", contenidoId);
                });
    }

    private static DatosValidados validar(EjercicioRequest request) {
        NivelDificultad nivel = NivelDificultad.desde(request.nivel());
        if (request.tipo() == TipoEjercicio.NUMERICA) {
            if (request.opciones() != null && !request.opciones().isEmpty()) {
                throw new SolicitudInvalidaException("Un ejercicio numérico no lleva opciones");
            }
            if (request.respuestaNumerica() == null) {
                throw new SolicitudInvalidaException("La respuesta numérica es obligatoria");
            }
            if (request.tolerancia() == null || request.tolerancia().compareTo(BigDecimal.ZERO) <= 0) {
                throw new SolicitudInvalidaException("La tolerancia debe ser mayor que cero");
            }
            return new DatosValidados(nivel, request.respuestaNumerica(), request.tolerancia(), List.of());
        }

        if (request.respuestaNumerica() != null || request.tolerancia() != null) {
            throw new SolicitudInvalidaException("Un ejercicio de opción múltiple no lleva respuesta numérica");
        }
        List<OpcionEjercicioRequest> opciones = request.opciones() == null ? List.of() : request.opciones();
        if (opciones.size() != 4) {
            throw new SolicitudInvalidaException("El ejercicio debe tener exactamente 4 opciones");
        }
        Map<String, OpcionEjercicioRequest> porLetra = opciones.stream()
                .collect(Collectors.toMap(
                        opcion -> opcion.letra().trim().toUpperCase(Locale.ROOT),
                        Function.identity(),
                        (a, b) -> {
                            throw new SolicitudInvalidaException("Las letras de las opciones no se pueden repetir");
                        }));
        if (!porLetra.keySet().equals(LETRAS)) {
            throw new SolicitudInvalidaException("Las opciones deben ser A, B, C y D");
        }
        long correctas = porLetra.values().stream().filter(opcion -> Boolean.TRUE.equals(opcion.correcta())).count();
        if (correctas != 1) {
            throw new SolicitudInvalidaException("Debe haber exactamente una opción correcta");
        }
        List<OpcionEjercicioRequest> ordenadas = porLetra.values().stream()
                .sorted(Comparator.comparing(opcion -> opcion.letra().toUpperCase(Locale.ROOT)))
                .toList();
        return new DatosValidados(nivel, null, null, ordenadas);
    }

    private static List<OpcionEjercicio> opciones(Ejercicio ejercicio, List<OpcionEjercicioRequest> opciones) {
        return opciones.stream()
                .map(opcion -> new OpcionEjercicio(
                        ejercicio,
                        opcion.letra(),
                        opcion.texto(),
                        Boolean.TRUE.equals(opcion.correcta())))
                .toList();
    }

    private static boolean mismasOpciones(Ejercicio ejercicio, List<OpcionEjercicioRequest> nuevas) {
        List<OpcionEjercicio> actuales = ejercicio.getOpciones().stream()
                .sorted(Comparator.comparing(OpcionEjercicio::getLetra))
                .toList();
        if (actuales.size() != nuevas.size()) {
            return false;
        }
        for (int i = 0; i < actuales.size(); i++) {
            OpcionEjercicio actual = actuales.get(i);
            OpcionEjercicioRequest nueva = nuevas.get(i);
            if (!actual.getLetra().equalsIgnoreCase(nueva.letra())
                    || !actual.getTexto().equals(nueva.texto().trim())
                    || actual.isEsCorrecta() != Boolean.TRUE.equals(nueva.correcta())) {
                return false;
            }
        }
        return true;
    }

    private static boolean mismaRespuesta(BigDecimal actual, BigDecimal nueva) {
        if (actual == null || nueva == null) {
            return actual == nueva;
        }
        return actual.compareTo(nueva) == 0;
    }

    private String codigoNuevo(String solicitado) {
        if (solicitado != null && !solicitado.isBlank()) {
            String codigo = solicitado.trim().toUpperCase(Locale.ROOT);
            if (ejercicioRepository.existsByCodigo(codigo)) {
                throw new ConflictoException("Ya existe un ejercicio con ese código");
            }
            return codigo;
        }
        String codigo;
        do {
            codigo = "DOC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        } while (ejercicioRepository.existsByCodigo(codigo));
        return codigo;
    }

    private record DatosValidados(
            NivelDificultad nivel,
            BigDecimal respuestaNumerica,
            BigDecimal tolerancia,
            List<OpcionEjercicioRequest> opciones) {
    }
}
