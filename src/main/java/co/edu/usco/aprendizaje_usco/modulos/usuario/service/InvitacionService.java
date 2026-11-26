package co.edu.usco.aprendizaje_usco.modulos.usuario.service;

import co.edu.usco.aprendizaje_usco.common.exception.ConflictoException;
import co.edu.usco.aprendizaje_usco.common.exception.ProhibidoException;
import co.edu.usco.aprendizaje_usco.common.exception.RecursoNoEncontradoException;
import co.edu.usco.aprendizaje_usco.common.exception.ReglaNegocioException;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.InvitacionRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.InvitacionPublicaResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.InvitacionResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.RegistroRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Grupo;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.GrupoEstudiante;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.GrupoEstudianteId;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Invitacion;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.GrupoEstudianteRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.GrupoRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.InvitacionRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Carga, reenvío y consumo de invitaciones de estudiantes (#150). */
@Service
public class InvitacionService {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    public record InvitacionCreada(InvitacionResponse invitacion, String token, String enlace) {
    }

    private final InvitacionRepository invitacionRepository;
    private final GrupoRepository grupoRepository;
    private final GrupoEstudianteRepository grupoEstudianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final Clock clock;
    private final int vigenciaDias;
    private final String urlBase;

    public InvitacionService(InvitacionRepository invitacionRepository,
                             GrupoRepository grupoRepository,
                             GrupoEstudianteRepository grupoEstudianteRepository,
                             UsuarioRepository usuarioRepository,
                             Clock clock,
                             @Value("${app.invitaciones.vigencia-dias:7}") int vigenciaDias,
                             @Value("${app.invitaciones.url-base:http://localhost:5173/registro}") String urlBase) {
        this.invitacionRepository = invitacionRepository;
        this.grupoRepository = grupoRepository;
        this.grupoEstudianteRepository = grupoEstudianteRepository;
        this.usuarioRepository = usuarioRepository;
        this.clock = clock;
        this.vigenciaDias = vigenciaDias;
        this.urlBase = urlBase;
    }

    @Transactional
    public List<InvitacionCreada> crearLote(Long grupoId, Long solicitanteId, boolean administrador,
                                            List<InvitacionRequest> filas) {
        Grupo grupo = grupoAutorizado(grupoId, solicitanteId, administrador);
        validarFilas(filas);
        List<InvitacionCreada> creadas = new ArrayList<>();
        Instant ahora = clock.instant();
        for (InvitacionRequest fila : filas) {
            creadas.add(crearOrenovar(grupo, fila, ahora));
        }
        return creadas;
    }

    @Transactional(readOnly = true)
    public List<InvitacionResponse> listar(Long grupoId, Long solicitanteId, boolean administrador) {
        grupoAutorizado(grupoId, solicitanteId, administrador);
        return invitacionRepository.listarPorGrupo(grupoId).stream()
                .map(invitacion -> InvitacionResponse.desde(invitacion, clock.instant()))
                .toList();
    }

    @Transactional
    public InvitacionCreada reenviar(Long grupoId, Long invitacionId, Long solicitanteId,
                                     boolean administrador) {
        grupoAutorizado(grupoId, solicitanteId, administrador);
        Invitacion invitacion = invitacionRepository.findById(invitacionId)
                .filter(i -> i.getGrupo().getId().equals(grupoId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Invitación", invitacionId));
        if (invitacion.estaUsada()) {
            throw new ConflictoException("El estudiante ya se registró con esta invitación");
        }
        return renovar(invitacion, Instant.now(clock));
    }

    @Transactional(readOnly = true)
    public InvitacionPublicaResponse obtenerPublica(String token) {
        Invitacion invitacion = porToken(token);
        validarVigente(invitacion);
        return InvitacionPublicaResponse.desde(invitacion);
    }

    @Transactional(readOnly = true)
    public Invitacion validarParaRegistro(String token, String correo) {
        Invitacion invitacion = porToken(token);
        validarVigente(invitacion);
        if (!invitacion.getCorreo().equals(Usuario.normalizarCorreo(correo))) {
            throw new ReglaNegocioException("El correo no coincide con la invitación");
        }
        return invitacion;
    }

    @Transactional
    public void registrar(Invitacion invitacion, Usuario estudiante) {
        GrupoEstudianteId id = new GrupoEstudianteId(invitacion.getGrupo().getId(), estudiante.getId());
        if (!grupoEstudianteRepository.existsById(id)) {
            grupoEstudianteRepository.saveAndFlush(new GrupoEstudiante(invitacion.getGrupo(), estudiante));
        }
        invitacion.marcarUsada(Instant.now(clock));
        invitacionRepository.saveAndFlush(invitacion);
    }

    private InvitacionCreada crearOrenovar(Grupo grupo, InvitacionRequest fila, Instant ahora) {
        String correo = Usuario.normalizarCorreo(fila.correo());
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ConflictoException("El estudiante ya tiene cuenta; inscríbelo directamente en el grupo");
        }
        Invitacion invitacion = invitacionRepository.findByGrupoIdAndCorreo(grupo.getId(), correo)
                .orElse(null);
        if (invitacion != null) {
            if (invitacion.estaUsada()) {
                throw new ConflictoException("El estudiante ya se registró con esta invitación");
            }
            return renovar(invitacion, ahora, fila);
        }
        String token = generarToken();
        Invitacion nueva = new Invitacion(grupo, correo, fila.nombres(), fila.apellidos(),
                hash(token), ahora.plus(Duration.ofDays(vigenciaDias)));
        invitacionRepository.saveAndFlush(nueva);
        return new InvitacionCreada(InvitacionResponse.desde(nueva, ahora), token, enlace(token));
    }

    private InvitacionCreada renovar(Invitacion invitacion, Instant ahora) {
        return renovar(invitacion, ahora,
                new InvitacionRequest(invitacion.getCorreo(), invitacion.getNombres(), invitacion.getApellidos()));
    }

    private InvitacionCreada renovar(Invitacion invitacion, Instant ahora, InvitacionRequest fila) {
        String token = generarToken();
        invitacion.renovar(hash(token), fila.nombres(), fila.apellidos(),
                ahora.plus(Duration.ofDays(vigenciaDias)));
        invitacionRepository.saveAndFlush(invitacion);
        return new InvitacionCreada(InvitacionResponse.desde(invitacion, ahora), token, enlace(token));
    }

    private Invitacion porToken(String token) {
        if (token == null || token.isBlank()) {
            throw new ReglaNegocioException("La invitación es obligatoria");
        }
        return invitacionRepository.findByTokenHash(hash(token))
                .orElseThrow(() -> new RecursoNoEncontradoException("Invitación no válida"));
    }

    private void validarVigente(Invitacion invitacion) {
        if (invitacion.estaUsada()) {
            throw new ReglaNegocioException("La invitación ya fue utilizada");
        }
        if (!invitacion.estaVigente(clock.instant())) {
            throw new ReglaNegocioException("La invitación venció");
        }
    }

    private Grupo grupoAutorizado(Long grupoId, Long solicitanteId, boolean administrador) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Grupo", grupoId));
        if (!administrador && !grupo.getDocente().getId().equals(solicitanteId)) {
            throw new ProhibidoException("No tienes permiso para gestionar este grupo");
        }
        return grupo;
    }

    private static void validarFilas(List<InvitacionRequest> filas) {
        Set<String> correos = new HashSet<>();
        for (InvitacionRequest fila : filas) {
            String correo = Usuario.normalizarCorreo(fila.correo());
            if (correo == null || !correo.matches(RegistroRequest.PATRON_CORREO_ESTUDIANTE)) {
                throw new ReglaNegocioException(RegistroRequest.MENSAJE_CORREO_ESTUDIANTE);
            }
            if (!correos.add(correo)) {
                throw new ReglaNegocioException("El listado repite el correo " + correo);
            }
        }
    }

    private String enlace(String token) {
        String separador = urlBase.contains("?") ? "&" : "?";
        return urlBase + separador + "token=" + token;
    }

    private static String generarToken() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
