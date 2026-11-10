package co.edu.usco.aprendizaje_usco.modulos.contenido.service;

import co.edu.usco.aprendizaje_usco.common.exception.ConflictoException;
import co.edu.usco.aprendizaje_usco.common.exception.ProhibidoException;
import co.edu.usco.aprendizaje_usco.common.exception.RecursoNoEncontradoException;
import co.edu.usco.aprendizaje_usco.common.exception.SolicitudInvalidaException;
import co.edu.usco.aprendizaje_usco.modulos.contenido.dto.ContenidoRequest;
import co.edu.usco.aprendizaje_usco.modulos.contenido.dto.ContenidoResponse;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Competencia;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Contenido;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.CompetenciaRepository;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.ContenidoRepository;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.EjercicioRepository;
import co.edu.usco.aprendizaje_usco.modulos.practica.config.PracticaProperties;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ContenidoService {

    private final ContenidoRepository contenidoRepository;
    private final EjercicioRepository ejercicioRepository;
    private final CompetenciaRepository competenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PracticaProperties practicaProperties;

    public ContenidoService(ContenidoRepository contenidoRepository,
                            EjercicioRepository ejercicioRepository,
                            CompetenciaRepository competenciaRepository,
                            UsuarioRepository usuarioRepository,
                            PracticaProperties practicaProperties) {
        this.contenidoRepository = contenidoRepository;
        this.ejercicioRepository = ejercicioRepository;
        this.competenciaRepository = competenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.practicaProperties = practicaProperties;
    }

    public List<ContenidoResponse> listar(Long docenteId, String asignatura, String competencia,
                                          Boolean activo, String texto) {
        String asignaturaNormalizada = normalizarAsignatura(asignatura);
        String competenciaNormalizada = normalizarCompetencia(competencia);
        String busqueda = texto == null || texto.isBlank() ? "" : texto.trim();
        return contenidoRepository.buscarDelDocente(docenteId, asignaturaNormalizada,
                        competenciaNormalizada, activo, busqueda).stream()
                .map(contenido -> ContenidoResponse.desde(contenido,
                        ejercicioRepository.countByContenidoId(contenido.getId())))
                .toList();
    }

    @Transactional
    public ContenidoResponse crear(Long docenteId, ContenidoRequest request) {
        Competencia competencia = competencia(request.competencia());
        String asignatura = validarAsignatura(request.asignatura());
        if (contenidoRepository.findByAsignaturaAndTemaAndTitulo(
                asignatura, request.tema().trim(), request.titulo().trim()).isPresent()) {
            throw new ConflictoException("Ya existe un contenido con esa asignatura, tema y título");
        }
        Usuario docente = usuarioRepository.getReferenceById(docenteId);
        Contenido contenido = contenidoRepository.saveAndFlush(new Contenido(
                competencia, asignatura, request.tema(), request.titulo(),
                request.descripcion(), docente));
        return ContenidoResponse.desde(contenido, 0);
    }

    @Transactional
    public ContenidoResponse actualizar(Long docenteId, Long contenidoId, ContenidoRequest request) {
        Contenido contenido = propio(contenidoId, docenteId);
        contenido.actualizar(request.tema(), request.titulo(), request.descripcion());
        contenidoRepository.saveAndFlush(contenido);
        return ContenidoResponse.desde(contenido,
                ejercicioRepository.countByContenidoId(contenido.getId()));
    }

    @Transactional
    public ContenidoResponse cambiarEstado(Long docenteId, Long contenidoId, boolean activo) {
        Contenido contenido = propio(contenidoId, docenteId);
        if (activo) {
            contenido.activar();
        } else {
            contenido.desactivar();
        }
        contenidoRepository.saveAndFlush(contenido);
        return ContenidoResponse.desde(contenido,
                ejercicioRepository.countByContenidoId(contenido.getId()));
    }

    @Transactional
    public void eliminar(Long docenteId, Long contenidoId) {
        Contenido contenido = propio(contenidoId, docenteId);
        if (ejercicioRepository.existsByContenidoId(contenidoId)) {
            throw new ConflictoException(
                    "El contenido tiene ejercicios; desactívalo en lugar de eliminarlo");
        }
        contenidoRepository.delete(contenido);
    }

    private Contenido propio(Long contenidoId, Long docenteId) {
        return contenidoRepository.findByIdAndCreadoPor_Id(contenidoId, docenteId)
                .orElseGet(() -> {
                    if (contenidoRepository.existsById(contenidoId)) {
                        throw new ProhibidoException("No tienes permiso para gestionar este contenido");
                    }
                    throw new RecursoNoEncontradoException("Contenido", contenidoId);
                });
    }

    private Competencia competencia(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new SolicitudInvalidaException("La competencia es obligatoria");
        }
        return competenciaRepository.findByCodigo(codigo.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new SolicitudInvalidaException("Competencia inválida: " + codigo));
    }

    private String validarAsignatura(String asignatura) {
        if (asignatura == null || asignatura.isBlank()) {
            throw new SolicitudInvalidaException("La asignatura es obligatoria");
        }
        if (!practicaProperties.esAsignaturaValida(asignatura)) {
            throw new SolicitudInvalidaException("La asignatura '" + asignatura + "' no es válida");
        }
        return practicaProperties.normalizar(asignatura);
    }

    private String normalizarAsignatura(String asignatura) {
        if (asignatura == null || asignatura.isBlank()) {
            return null;
        }
        if (!practicaProperties.esAsignaturaValida(asignatura)) {
            throw new SolicitudInvalidaException("La asignatura '" + asignatura + "' no es válida");
        }
        return practicaProperties.normalizar(asignatura);
    }

    private static String normalizarCompetencia(String competencia) {
        return competencia == null || competencia.isBlank()
                ? null : competencia.trim().toUpperCase(Locale.ROOT);
    }
}
