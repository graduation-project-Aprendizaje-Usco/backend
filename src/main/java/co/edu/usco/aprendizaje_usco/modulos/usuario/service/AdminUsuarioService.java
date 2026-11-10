package co.edu.usco.aprendizaje_usco.modulos.usuario.service;

import co.edu.usco.aprendizaje_usco.common.exception.ConflictoException;
import co.edu.usco.aprendizaje_usco.common.exception.RecursoNoEncontradoException;
import co.edu.usco.aprendizaje_usco.common.exception.ReglaNegocioException;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.ActualizarUsuarioRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.RegistroRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.UsuarioAdminRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.UsuarioAdminResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.UsuarioCreadoResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.EstadoUsuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.ProgramaAcademico;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Rol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Administración de usuarios y roles (RF-13, SCRUM-45). Solo la usa el rol ADMIN. */
@Service
public class AdminUsuarioService {

    // Sin 0/O, 1/l/I para que la contraseña temporal se pueda dictar o copiar sin confusiones.
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final int LARGO_PASSWORD_TEMPORAL = 12;

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom aleatorio = new SecureRandom();

    public AdminUsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                               PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioAdminResponse> listar(String buscar, NombreRol rol, EstadoUsuario estado) {
        String texto = buscar == null || buscar.isBlank()
                ? null : "%" + buscar.trim().toLowerCase(Locale.ROOT) + "%";
        return usuarioRepository.buscarParaAdmin(rol, estado, texto).stream()
                .map(UsuarioAdminResponse::desde)
                .toList();
    }

    @Transactional
    public UsuarioCreadoResponse crear(UsuarioAdminRequest request) {
        String correo = Usuario.normalizarCorreo(request.correo());
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ConflictoException("El correo ya está registrado");
        }
        String programa = programaParaRol(request.rol(), correo, request.programaAcademico());
        String passwordTemporal = generarPasswordTemporal();
        Usuario usuario = new Usuario(request.nombres().trim(), request.apellidos().trim(), correo,
                passwordEncoder.encode(passwordTemporal), rol(request.rol()));
        usuario.setProgramaAcademico(programa);
        // saveAndFlush: si llega el mismo correo a la vez, uq_usuario_correo responde 409.
        return new UsuarioCreadoResponse(UsuarioAdminResponse.desde(usuarioRepository.saveAndFlush(usuario)),
                passwordTemporal);
    }

    @Transactional
    public UsuarioAdminResponse actualizar(Long adminId, Long usuarioId, ActualizarUsuarioRequest request) {
        Usuario usuario = buscar(usuarioId);
        // Quitarse el rol ADMIN dejaría al administrador sin acceso a esta misma pantalla.
        if (usuarioId.equals(adminId) && request.rol() != NombreRol.ADMIN) {
            throw new ReglaNegocioException("No puedes quitarte el rol de administrador");
        }
        usuario.setProgramaAcademico(programaParaRol(request.rol(), usuario.getCorreo(), request.programaAcademico()));
        usuario.setNombres(request.nombres().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setRol(rol(request.rol()));
        return UsuarioAdminResponse.desde(usuario);
    }

    @Transactional
    public UsuarioAdminResponse cambiarEstado(Long adminId, Long usuarioId, EstadoUsuario estado) {
        if (usuarioId.equals(adminId) && estado == EstadoUsuario.INACTIVO) {
            throw new ReglaNegocioException("No puedes desactivar tu propia cuenta");
        }
        Usuario usuario = buscar(usuarioId);
        if (estado == EstadoUsuario.ACTIVO) {
            usuario.activar();
        } else {
            usuario.desactivar();
        }
        return UsuarioAdminResponse.desde(usuario);
    }

    private Usuario buscar(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", usuarioId));
    }

    private Rol rol(NombreRol nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("Falta el rol " + nombre + " en la base de datos"));
    }

    /**
     * Un estudiante necesita correo de estudiante y un programa de la lista (las mismas
     * reglas del registro público); docentes y administradores no llevan programa.
     */
    private static String programaParaRol(NombreRol rol, String correo, String programa) {
        if (rol != NombreRol.ESTUDIANTE) {
            return null;
        }
        if (!correo.matches(RegistroRequest.PATRON_CORREO_ESTUDIANTE)) {
            throw new ReglaNegocioException(RegistroRequest.MENSAJE_CORREO_ESTUDIANTE);
        }
        return ProgramaAcademico.desdeNombre(programa)
                .map(ProgramaAcademico::getNombre)
                .orElseThrow(() -> new ReglaNegocioException("Elige un programa académico de la lista"));
    }

    /** Cumple las reglas del registro: al menos una letra y un número. */
    private String generarPasswordTemporal() {
        String password;
        do {
            StringBuilder sb = new StringBuilder(LARGO_PASSWORD_TEMPORAL);
            for (int i = 0; i < LARGO_PASSWORD_TEMPORAL; i++) {
                sb.append(ALFABETO.charAt(aleatorio.nextInt(ALFABETO.length())));
            }
            password = sb.toString();
        } while (!password.matches(RegistroRequest.PATRON_PASSWORD));
        return password;
    }
}
