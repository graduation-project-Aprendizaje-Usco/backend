package co.edu.usco.aprendizaje_usco.modulos.analitica.service;

import co.edu.usco.aprendizaje_usco.common.exception.RecursoNoEncontradoException;
import co.edu.usco.aprendizaje_usco.common.exception.ReglaNegocioException;
import co.edu.usco.aprendizaje_usco.common.exception.SolicitudInvalidaException;
import co.edu.usco.aprendizaje_usco.modulos.analitica.dto.DetalleEstudianteGrupoResponse;
import co.edu.usco.aprendizaje_usco.modulos.analitica.dto.DiagnosticoEstudianteResponse;
import co.edu.usco.aprendizaje_usco.modulos.analitica.dto.DimensionEstudianteResponse;
import co.edu.usco.aprendizaje_usco.modulos.analitica.dto.DimensionGrupoResponse;
import co.edu.usco.aprendizaje_usco.modulos.analitica.dto.DistribucionCategoriaResponse;
import co.edu.usco.aprendizaje_usco.modulos.analitica.dto.EstudianteGrupoResponse;
import co.edu.usco.aprendizaje_usco.modulos.analitica.dto.GrupoInfoResponse;
import co.edu.usco.aprendizaje_usco.modulos.analitica.dto.ResumenGrupoResponse;
import co.edu.usco.aprendizaje_usco.modulos.analitica.repository.AnaliticaGrupoRepository;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Competencia;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.CompetenciaRepository;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.CategoriaDesempeno;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.repository.ResultadoDimensionRepository;
import co.edu.usco.aprendizaje_usco.modulos.progreso.model.NivelDominio;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Grupo;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.GrupoEstudiante;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.GrupoEstudianteId;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.GrupoEstudianteRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.service.GrupoService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Indicadores de un grupo para el docente (RF-10). El servicio agrega las
 * metricas de practica y el ultimo diagnostico de los estudiantes inscritos.
 */
@Service
@Transactional(readOnly = true)
public class IndicadoresGrupoService {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final BigDecimal CINCUENTA = BigDecimal.valueOf(50);

    private final GrupoService grupoService;
    private final GrupoEstudianteRepository grupoEstudianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CompetenciaRepository competenciaRepository;
    private final ResultadoDimensionRepository resultadoRepository;
    private final AnaliticaGrupoRepository analiticaRepository;

    public IndicadoresGrupoService(GrupoService grupoService,
                                   GrupoEstudianteRepository grupoEstudianteRepository,
                                   UsuarioRepository usuarioRepository,
                                   CompetenciaRepository competenciaRepository,
                                   ResultadoDimensionRepository resultadoRepository,
                                   AnaliticaGrupoRepository analiticaRepository) {
        this.grupoService = grupoService;
        this.grupoEstudianteRepository = grupoEstudianteRepository;
        this.usuarioRepository = usuarioRepository;
        this.competenciaRepository = competenciaRepository;
        this.resultadoRepository = resultadoRepository;
        this.analiticaRepository = analiticaRepository;
    }

    public ResumenGrupoResponse resumen(Long docenteId, Long grupoId, String competencia,
                                        LocalDate desde, LocalDate hasta) {
        Grupo grupo = grupoService.obtenerGrupoDelDocente(grupoId, docenteId);
        List<Competencia> competencias = competencias(competencia);
        Rango rango = rango(desde, hasta);
        List<Usuario> estudiantes = estudiantes(grupoId);
        String codigoCompetencia = competencia == null ? null : competencia.trim().toUpperCase(Locale.ROOT);

        if (estudiantes.isEmpty()) {
            return resumenVacio(grupo, competencias);
        }

        List<Long> ids = estudiantes.stream().map(Usuario::getId).toList();
        List<AnaliticaGrupoRepository.MetricasEstudianteDimension> metricas =
                analiticaRepository.metricasPorEstudiante(ids, codigoCompetencia, rango.desde(), rango.hasta());
        Map<Long, AnaliticaGrupoRepository.DiagnosticoResumen> diagnosticos =
                analiticaRepository.ultimosDiagnosticos(ids, rango.desde(), rango.hasta()).stream()
                        .collect(Collectors.toMap(
                                AnaliticaGrupoRepository.DiagnosticoResumen::estudianteId,
                                Function.identity()));

        Map<Long, List<AnaliticaGrupoRepository.MetricasEstudianteDimension>> porEstudiante =
                metricas.stream().collect(Collectors.groupingBy(
                        AnaliticaGrupoRepository.MetricasEstudianteDimension::estudianteId));
        Map<Long, List<AnaliticaGrupoRepository.MetricasEstudianteDimension>> porCompetencia =
                metricas.stream().collect(Collectors.groupingBy(
                        AnaliticaGrupoRepository.MetricasEstudianteDimension::competenciaId));

        List<EstudianteGrupoResponse> filas = estudiantes.stream()
                .map(estudiante -> fila(estudiante,
                        porEstudiante.getOrDefault(estudiante.getId(), List.of()),
                        diagnosticos.get(estudiante.getId())))
                .toList();

        List<BigDecimal> promedios = filas.stream()
                .map(EstudianteGrupoResponse::promedio)
                .filter(promedio -> promedio != null)
                .toList();

        List<DimensionGrupoResponse> dimensiones = competencias.stream()
                .map(competenciaActual -> dimension(competenciaActual,
                        porCompetencia.getOrDefault(competenciaActual.getId(), List.of())))
                .toList();

        long conActividad = filas.stream().filter(fila -> !fila.sinActividad()).count();
        long enRiesgo = filas.stream().filter(EstudianteGrupoResponse::enRiesgo).count();

        return new ResumenGrupoResponse(
                new GrupoInfoResponse(grupo.getId(), grupo.getCodigo(), grupo.getNombre()),
                filas.size(),
                conActividad,
                promedio(promedios),
                dimensiones,
                distribucion(filas),
                enRiesgo,
                filas);
    }

    public DetalleEstudianteGrupoResponse detalle(Long docenteId, Long grupoId, Long estudianteId,
                                                   String competencia, LocalDate desde, LocalDate hasta) {
        grupoService.obtenerGrupoDelDocente(grupoId, docenteId);
        if (!grupoService.esDocenteDe(docenteId, estudianteId)) {
            throw new RecursoNoEncontradoException("El estudiante no pertenece al grupo");
        }
        if (!grupoEstudianteRepository.existsById(new GrupoEstudianteId(grupoId, estudianteId))) {
            throw new RecursoNoEncontradoException("El estudiante no pertenece al grupo");
        }

        Usuario estudiante = usuarioRepository.findById(estudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante", estudianteId));
        List<Competencia> competencias = competencias(competencia);
        Rango rango = rango(desde, hasta);
        String codigoCompetencia = competencia == null ? null : competencia.trim().toUpperCase(Locale.ROOT);

        List<AnaliticaGrupoRepository.MetricasEstudianteDimension> metricas =
                analiticaRepository.metricasPorEstudiante(List.of(estudianteId), codigoCompetencia,
                        rango.desde(), rango.hasta());
        Map<Long, AnaliticaGrupoRepository.MetricasEstudianteDimension> porCompetencia = metricas.stream()
                .collect(Collectors.toMap(
                        AnaliticaGrupoRepository.MetricasEstudianteDimension::competenciaId,
                        Function.identity()));
        Optional<AnaliticaGrupoRepository.DiagnosticoResumen> diagnostico =
                analiticaRepository.ultimosDiagnosticos(List.of(estudianteId), rango.desde(), rango.hasta())
                        .stream().findFirst();

        List<DimensionEstudianteResponse> dimensiones = competencias.stream()
                .map(competenciaActual -> dimensionEstudiante(competenciaActual,
                        porCompetencia.get(competenciaActual.getId())))
                .toList();

        EstudianteGrupoResponse fila = fila(estudiante, metricas, diagnostico.orElse(null));

        DiagnosticoEstudianteResponse ultimoDiagnostico = diagnostico
                .map(d -> new DiagnosticoEstudianteResponse(
                        d.intentoId(),
                        d.finalizadoEn(),
                        d.puntaje(),
                        d.total(),
                        d.categoria().name(),
                        dimensionesDiagnostico(d.intentoId(), codigoCompetencia)))
                .orElse(null);

        return new DetalleEstudianteGrupoResponse(fila, dimensiones, ultimoDiagnostico);
    }

    private List<Usuario> estudiantes(Long grupoId) {
        return grupoEstudianteRepository.buscarPorGrupo(grupoId).stream()
                .map(GrupoEstudiante::getEstudiante)
                .sorted(Comparator.comparing(Usuario::getApellidos).thenComparing(Usuario::getNombres))
                .toList();
    }

    private List<Competencia> competencias(String competencia) {
        if (competencia == null || competencia.isBlank()) {
            return competenciaRepository.findAll(Sort.by("id"));
        }
        String codigo = competencia.trim().toUpperCase(Locale.ROOT);
        return competenciaRepository.findByCodigo(codigo)
                .map(List::of)
                .orElseThrow(() -> new SolicitudInvalidaException(
                        "Competencia inválida: " + competencia));
    }

    private static Rango rango(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        Instant inicio = desde == null ? null : desde.atStartOfDay(ZONA).toInstant();
        Instant fin = hasta == null ? null : hasta.plusDays(1).atStartOfDay(ZONA).toInstant();
        return new Rango(inicio, fin);
    }

    private static ResumenGrupoResponse resumenVacio(Grupo grupo, List<Competencia> competencias) {
        List<DimensionGrupoResponse> dimensiones = competencias.stream()
                .map(c -> new DimensionGrupoResponse(c.getCodigo(), c.getNombre(), null, 0, 0, 0))
                .toList();
        return new ResumenGrupoResponse(
                new GrupoInfoResponse(grupo.getId(), grupo.getCodigo(), grupo.getNombre()),
                0, 0, null, dimensiones,
                distribucion(List.of()), 0, List.of());
    }

    private static EstudianteGrupoResponse fila(
            Usuario estudiante,
            List<AnaliticaGrupoRepository.MetricasEstudianteDimension> metricas,
            AnaliticaGrupoRepository.DiagnosticoResumen diagnostico) {
        long intentos = metricas.stream()
                .mapToLong(AnaliticaGrupoRepository.MetricasEstudianteDimension::intentos)
                .sum();
        long aciertos = metricas.stream()
                .mapToLong(AnaliticaGrupoRepository.MetricasEstudianteDimension::aciertos)
                .sum();
        BigDecimal promedio = porcentaje(aciertos, intentos);
        String nivel = promedio == null ? "SIN_EVALUAR" : NivelDominio.desde(promedio).name();
        String categoria = diagnostico == null ? "SIN_DIAGNOSTICO" : diagnostico.categoria().name();
        List<String> motivos = new ArrayList<>();
        if (diagnostico != null && diagnostico.categoria() == CategoriaDesempeno.ALTO_RIESGO) {
            motivos.add("Diagnóstico en alto riesgo");
        }
        if (promedio != null && promedio.compareTo(CINCUENTA) < 0) {
            motivos.add("Promedio de práctica menor al 50 %");
        }
        return new EstudianteGrupoResponse(
                estudiante.getId(),
                estudiante.getNombres(),
                estudiante.getApellidos(),
                estudiante.getCorreo(),
                intentos,
                aciertos,
                promedio,
                nivel,
                categoria,
                intentos == 0,
                !motivos.isEmpty(),
                motivos);
    }

    private static DimensionGrupoResponse dimension(
            Competencia competencia,
            List<AnaliticaGrupoRepository.MetricasEstudianteDimension> metricas) {
        List<BigDecimal> promedios = metricas.stream()
                .map(metrica -> porcentaje(metrica.aciertos(), metrica.intentos()))
                .filter(promedio -> promedio != null)
                .toList();
        long intentos = metricas.stream()
                .mapToLong(AnaliticaGrupoRepository.MetricasEstudianteDimension::intentos)
                .sum();
        long aciertos = metricas.stream()
                .mapToLong(AnaliticaGrupoRepository.MetricasEstudianteDimension::aciertos)
                .sum();
        return new DimensionGrupoResponse(
                competencia.getCodigo(),
                competencia.getNombre(),
                promedio(promedios),
                promedios.size(),
                intentos,
                aciertos);
    }

    private static DimensionEstudianteResponse dimensionEstudiante(
            Competencia competencia,
            AnaliticaGrupoRepository.MetricasEstudianteDimension metrica) {
        if (metrica == null) {
            return new DimensionEstudianteResponse(competencia.getCodigo(), competencia.getNombre(),
                    0, 0, null, "SIN_EVALUAR");
        }
        BigDecimal promedio = porcentaje(metrica.aciertos(), metrica.intentos());
        return new DimensionEstudianteResponse(
                competencia.getCodigo(),
                competencia.getNombre(),
                metrica.intentos(),
                metrica.aciertos(),
                promedio,
                promedio == null ? "SIN_EVALUAR" : NivelDominio.desde(promedio).name());
    }

    private List<DimensionEstudianteResponse> dimensionesDiagnostico(Long intentoId, String competencia) {
        return resultadoRepository.findDelIntento(intentoId).stream()
                .filter(resultado -> competencia == null
                        || resultado.getCompetencia().getCodigo().equals(competencia))
                .map(resultado -> new DimensionEstudianteResponse(
                        resultado.getCompetencia().getCodigo(),
                        resultado.getCompetencia().getNombre(),
                        resultado.getTotal(),
                        resultado.getAciertos(),
                        resultado.getPorcentaje(),
                        resultado.getNivel().name()))
                .toList();
    }

    private static List<DistribucionCategoriaResponse> distribucion(List<EstudianteGrupoResponse> filas) {
        Map<String, Long> conteos = new LinkedHashMap<>();
        for (CategoriaDesempeno categoria : CategoriaDesempeno.values()) {
            conteos.put(categoria.name(), 0L);
        }
        conteos.put("SIN_DIAGNOSTICO", 0L);
        for (EstudianteGrupoResponse fila : filas) {
            conteos.merge(fila.categoriaDiagnostico(), 1L, Long::sum);
        }
        long total = filas.size();
        return conteos.entrySet().stream()
                .map(entry -> new DistribucionCategoriaResponse(
                        entry.getKey(),
                        entry.getValue(),
                        total == 0 ? BigDecimal.ZERO.setScale(2)
                                : BigDecimal.valueOf(entry.getValue() * 100L)
                                        .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)))
                .toList();
    }

    private static BigDecimal promedio(List<BigDecimal> valores) {
        if (valores.isEmpty()) {
            return null;
        }
        BigDecimal suma = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return suma.divide(BigDecimal.valueOf(valores.size()), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal porcentaje(long aciertos, long intentos) {
        if (intentos == 0) {
            return null;
        }
        return BigDecimal.valueOf(aciertos * 100L)
                .divide(BigDecimal.valueOf(intentos), 2, RoundingMode.HALF_UP);
    }

    private record Rango(Instant desde, Instant hasta) {
    }
}
