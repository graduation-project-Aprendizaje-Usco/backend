package co.edu.usco.aprendizaje_usco.modulos.usuario.service;

import co.edu.usco.aprendizaje_usco.common.exception.ConflictoException;
import co.edu.usco.aprendizaje_usco.common.exception.NoAutenticadoException;
import co.edu.usco.aprendizaje_usco.common.exception.ProhibidoException;
import co.edu.usco.aprendizaje_usco.common.exception.RecursoNoEncontradoException;
import co.edu.usco.aprendizaje_usco.common.exception.ReglaNegocioException;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.EstudianteGrupoResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.GrupoRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.GrupoResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.InscripcionEstudianteRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Grupo;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.GrupoEstudiante;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.GrupoEstudianteId;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.GrupoEstudianteRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.GrupoRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GrupoService {

    private static final String SESION_INVALIDA = "Debes iniciar sesión para continuar";

    private final GrupoRepository grupoRepository;
    private final GrupoEstudianteRepository grupoEstudianteRepository;
    private final UsuarioRepository usuarioRepository;

    public GrupoService(GrupoRepository grupoRepository,
                        GrupoEstudianteRepository grupoEstudianteRepository,
                        UsuarioRepository usuarioRepository) {
        this.grupoRepository = grupoRepository;
        this.grupoEstudianteRepository = grupoEstudianteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public GrupoResponse crear(Long docenteId, GrupoRequest request) {
        Usuario docente = usuarioRepository.findById(docenteId)
                .filter(Usuario::estaActivo)
                .orElseThrow(() -> new NoAutenticadoException(SESION_INVALIDA));
        if (docente.getRol().getNombre() != NombreRol.DOCENTE) {
            throw new ProhibidoException("Solo un docente puede crear grupos");
        }

        String codigo = request.codigo().trim().toUpperCase(Locale.ROOT);
        if (grupoRepository.existsByCodigo(codigo)) {
            throw new ConflictoException("Ya existe un grupo con ese código");
        }
        return GrupoResponse.desde(grupoRepository.saveAndFlush(
                new Grupo(codigo, request.nombre(), docente)));
    }

    @Transactional(readOnly = true)
    public List<GrupoResponse> listar(Long solicitanteId, boolean administrador) {
        List<Grupo> grupos = administrador
                ? grupoRepository.findAllByOrderByCodigoAsc()
                : grupoRepository.findAllByDocente_IdOrderByCodigoAsc(solicitanteId);
        return grupos.stream().map(GrupoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<EstudianteGrupoResponse> estudiantes(Long grupoId, Long solicitanteId, boolean administrador) {
        Grupo grupo = grupoAutorizado(grupoId, solicitanteId, administrador);
        return grupoEstudianteRepository.buscarPorGrupo(grupo.getId()).stream()
                .map(GrupoEstudiante::getEstudiante)
                .map(EstudianteGrupoResponse::desde)
                .toList();
    }

    @Transactional
    public EstudianteGrupoResponse inscribir(Long grupoId, Long solicitanteId, boolean administrador,
                                              InscripcionEstudianteRequest request) {
        Grupo grupo = grupoAutorizado(grupoId, solicitanteId, administrador);
        String correo = Usuario.normalizarCorreo(request.correo());
        Usuario estudiante = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante con correo " + correo + " no encontrado"));
        if (estudiante.getRol().getNombre() != NombreRol.ESTUDIANTE) {
            throw new ReglaNegocioException("El correo no pertenece a un estudiante");
        }
        if (!estudiante.estaActivo()) {
            throw new ReglaNegocioException("No se puede inscribir un estudiante inactivo");
        }

        if (grupoEstudianteRepository.existsById(
                new GrupoEstudianteId(grupo.getId(), estudiante.getId()))) {
            throw new ConflictoException("El estudiante ya está inscrito en este grupo");
        }
        grupoEstudianteRepository.saveAndFlush(new GrupoEstudiante(grupo, estudiante));
        return EstudianteGrupoResponse.desde(estudiante);
    }

    private Grupo grupoAutorizado(Long grupoId, Long solicitanteId, boolean administrador) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Grupo", grupoId));
        if (!administrador && !grupo.getDocente().getId().equals(solicitanteId)) {
            throw new ProhibidoException("No tienes permiso para gestionar este grupo");
        }
        return grupo;
    }
}
