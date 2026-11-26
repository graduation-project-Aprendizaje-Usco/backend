package co.edu.usco.aprendizaje_usco.modulos.usuario.service;

import co.edu.usco.aprendizaje_usco.common.exception.ConflictoException;
import co.edu.usco.aprendizaje_usco.common.exception.RecursoNoEncontradoException;
import co.edu.usco.aprendizaje_usco.common.exception.ReglaNegocioException;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.ActivacionCuenta;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.ActivacionCuentaRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Emisión y consumo de enlaces para definir la contraseña inicial de una cuenta. */
@Service
public class ActivacionCuentaService {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final ActivacionCuentaRepository activacionRepository;
    private final CorreoInvitacionService correoService;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final Duration vigencia;
    private final String urlBase;

    public ActivacionCuentaService(ActivacionCuentaRepository activacionRepository,
                                   CorreoInvitacionService correoService,
                                   PasswordEncoder passwordEncoder,
                                   Clock clock,
                                   @Value("${app.activacion-cuenta.vigencia-horas:168}") long vigenciaHoras,
                                   @Value("${app.activacion-cuenta.url-base:http://localhost:5173/activar-cuenta}")
                                   String urlBase) {
        this.activacionRepository = activacionRepository;
        this.correoService = correoService;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.vigencia = Duration.ofHours(vigenciaHoras);
        this.urlBase = urlBase;
    }

    public List<Long> usuariosPendientes(Collection<Long> usuarioIds) {
        if (usuarioIds.isEmpty()) {
            return List.of();
        }
        return activacionRepository.buscarUsuariosPendientes(usuarioIds);
    }

    @Transactional
    public void crearYEnviar(Usuario usuario) {
        String token = generarToken();
        ActivacionCuenta activacion = new ActivacionCuenta(usuario, hash(token), clock.instant().plus(vigencia));
        activacionRepository.saveAndFlush(activacion);
        correoService.enviarConfiguracionCuenta(usuario.getCorreo(), usuario.getNombres(), enlace(token),
                vigencia.toHours());
    }

    @Transactional
    public void reenviar(Long usuarioId) {
        ActivacionCuenta activacion = activacionRepository.findPendienteParaRenovar(usuarioId)
                .orElseThrow(() -> new ConflictoException("La cuenta ya definió su contraseña"));
        String token = generarToken();
        activacion.renovar(hash(token), clock.instant().plus(vigencia));
        activacionRepository.saveAndFlush(activacion);
        Usuario usuario = activacion.getUsuario();
        correoService.enviarConfiguracionCuenta(usuario.getCorreo(), usuario.getNombres(), enlace(token),
                vigencia.toHours());
    }

    @Transactional
    public void activar(String token, String nuevaPassword) {
        if (token == null || token.isBlank()) {
            throw new ReglaNegocioException("El enlace de activación no es válido");
        }
        ActivacionCuenta activacion = activacionRepository.findParaConsumo(hash(token))
                .orElseThrow(() -> new RecursoNoEncontradoException("Enlace de activación no válido"));
        if (!activacion.estaVigente(clock.instant())) {
            throw new ReglaNegocioException(activacion.estaPendiente()
                    ? "El enlace de activación venció; solicita al administrador que lo reenvíe"
                    : "El enlace de activación ya fue utilizado");
        }
        activacion.getUsuario().setPasswordHash(passwordEncoder.encode(nuevaPassword));
        activacion.marcarUsada(clock.instant());
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
