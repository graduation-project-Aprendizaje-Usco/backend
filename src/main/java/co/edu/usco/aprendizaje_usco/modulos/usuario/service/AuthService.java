package co.edu.usco.aprendizaje_usco.modulos.usuario.service;

import co.edu.usco.aprendizaje_usco.common.exception.ConflictoException;
import co.edu.usco.aprendizaje_usco.common.exception.NoAutenticadoException;
import co.edu.usco.aprendizaje_usco.common.exception.ReglaNegocioException;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.AuthResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.LoginRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.RegistroRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.UsuarioResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.ProgramaAcademico;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Rol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import co.edu.usco.aprendizaje_usco.security.JwtService;
import co.edu.usco.aprendizaje_usco.security.UsuarioAutenticado;
import java.time.Duration;
import java.time.Instant;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Login, registro de estudiantes y consulta del usuario de la sesión (RF-01). */
@Service
public class AuthService {

    /** Mismo mensaje para correo inexistente, contraseña errada o usuario inactivo. */
    static final String CREDENCIALES_INVALIDAS = "Correo o contraseña incorrectos";
    static final String SESION_INVALIDA = "Debes iniciar sesión para continuar";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
                       UsuarioRepository usuarioRepository, RolRepository rolRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication autenticacion;
        try {
            autenticacion = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.correo(), request.password()));
        } catch (AuthenticationException ex) {
            throw new NoAutenticadoException(CREDENCIALES_INVALIDAS);
        }

        UsuarioAutenticado usuario = (UsuarioAutenticado) autenticacion.getPrincipal();
        JwtService.TokenEmitido token = jwtService.emitir(usuario);
        long segundos = Math.max(0, Duration.between(Instant.now(), token.expiraEn()).toSeconds());

        return new AuthResponse(token.valor(), AuthResponse.TIPO_BEARER, segundos, UsuarioResponse.desde(usuario));
    }

    /** Registro público: siempre crea un ESTUDIANTE activo. */
    @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {
        String correo = Usuario.normalizarCorreo(request.correo());
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ConflictoException("El correo ya está registrado");
        }

        Rol estudiante = rolRepository.findByNombre(NombreRol.ESTUDIANTE)
                .orElseThrow(() -> new IllegalStateException("Falta el rol ESTUDIANTE en la base de datos"));
        Usuario usuario = new Usuario(request.nombres().trim(), request.apellidos().trim(), correo,
                passwordEncoder.encode(request.password()), estudiante);
        // Se guarda el nombre oficial del programa (ej. "ingeniería civil" → "Ingeniería Civil").
        usuario.setProgramaAcademico(nombreOficialDelPrograma(request.programaAcademico()));

        // saveAndFlush: si dos registros con el mismo correo llegan a la vez, la base
        // rechaza el segundo (uq_usuario_correo) y el GlobalExceptionHandler responde 409.
        return UsuarioResponse.desde(usuarioRepository.saveAndFlush(usuario));
    }

    /** Usuario dueño del token. Si ya no existe o fue desactivado, la sesión deja de valer. */
    @Transactional(readOnly = true)
    public UsuarioResponse usuarioActual(Long id) {
        return usuarioRepository.findById(id)
                .filter(Usuario::estaActivo)
                .map(UsuarioResponse::desde)
                .orElseThrow(() -> new NoAutenticadoException(SESION_INVALIDA));
    }

    /** @Valid ya garantiza que el programa es de la lista; aquí solo se toma su nombre oficial. */
    private static String nombreOficialDelPrograma(String texto) {
        return ProgramaAcademico.desdeNombre(texto)
                .map(ProgramaAcademico::getNombre)
                .orElseThrow(() -> new ReglaNegocioException("Elige un programa académico de la lista"));
    }

}
