package co.edu.usco.aprendizaje_usco.modulos.usuario.service;

import co.edu.usco.aprendizaje_usco.common.exception.ConflictoException;
import co.edu.usco.aprendizaje_usco.common.exception.RecursoNoEncontradoException;
import co.edu.usco.aprendizaje_usco.common.exception.ReglaNegocioException;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.ContenidoRepository;
import co.edu.usco.aprendizaje_usco.modulos.contenido.repository.EjercicioRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.ActualizarUsuarioRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.RegistroRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.ResumenAdminResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.UsuarioAdminRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.UsuarioAdminResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.UsuarioCreadoResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.EstadoUsuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.ProgramaAcademico;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Rol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.GrupoRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import java.util.Base64;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Administración de usuarios y roles (RF-13, SCRUM-45). Solo la usa el rol ADMIN. */
@Service
public class AdminUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final GrupoRepository grupoRepository;
    private final ContenidoRepository contenidoRepository;
    private final EjercicioRepository ejercicioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivacionCuentaService activacionCuentaService;
    private final SecureRandom aleatorio = new SecureRandom();

    public AdminUsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                               GrupoRepository grupoRepository, ContenidoRepository contenidoRepository,
                               EjercicioRepository ejercicioRepository, PasswordEncoder passwordEncoder,
                               ActivacionCuentaService activacionCuentaService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.grupoRepository = grupoRepository;
        this.contenidoRepository = contenidoRepository;
        this.ejercicioRepository = ejercicioRepository;
        this.passwordEncoder = passwordEncoder;
        this.activacionCuentaService = activacionCuentaService;
    }

    /** Conteos del panel del administrador (SCRUM-46). */
    @Transactional(readOnly = true)
    public ResumenAdminResponse resumen() {
        List<ResumenAdminResponse.UsuariosPorRol> porRol = Arrays.stream(NombreRol.values())
                .map(rol -> new ResumenAdminResponse.UsuariosPorRol(rol,
                        usuarioRepository.countByRol_Nombre(rol),
                        usuarioRepository.countByRol_NombreAndEstado(rol, EstadoUsuario.ACTIVO)))
                .toList();
        return new ResumenAdminResponse(porRol, grupoRepository.count(),
                contenidoRepository.countByActivoTrue(), ejercicioRepository.countByActivoTrueAndContenidoActivoTrue());
    }

    @Transactional(readOnly = true)
    public List<UsuarioAdminResponse> listar(String buscar, NombreRol rol, EstadoUsuario estado) {
        String texto = buscar == null || buscar.isBlank()
                ? null : "%" + buscar.trim().toLowerCase(Locale.ROOT) + "%";
        List<Usuario> encontrados = usuarioRepository.buscarParaAdmin(rol, estado, texto);
        Set<Long> pendientes = Set.copyOf(activacionCuentaService.usuariosPendientes(
                encontrados.stream().map(Usuario::getId).toList()));
        return encontrados.stream()
                .map(usuario -> UsuarioAdminResponse.desde(usuario, pendientes.contains(usuario.getId())))
                .toList();
    }

    @Transactional
    public UsuarioCreadoResponse crear(UsuarioAdminRequest request) {
        String correo = Usuario.normalizarCorreo(request.correo());
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ConflictoException("El correo ya está registrado");
        }
        String programa = programaParaRol(request.rol(), correo, request.programaAcademico());
        String claveAleatoria = generarClaveAleatoria();
        Usuario usuario = new Usuario(request.nombres().trim(), request.apellidos().trim(), correo,
                passwordEncoder.encode(claveAleatoria), rol(request.rol()));
        usuario.setProgramaAcademico(programa);
        // saveAndFlush: si llega el mismo correo a la vez, uq_usuario_correo responde 409.
        usuarioRepository.saveAndFlush(usuario);
        activacionCuentaService.crearYEnviar(usuario);
        return new UsuarioCreadoResponse(UsuarioAdminResponse.desde(usuario, true));
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
        return UsuarioAdminResponse.desde(usuario, activacionCuentaService.usuariosPendientes(List.of(usuarioId))
                .contains(usuarioId));
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
        return UsuarioAdminResponse.desde(usuario, activacionCuentaService.usuariosPendientes(List.of(usuarioId))
                .contains(usuarioId));
    }

    @Transactional
    public void reenviarActivacion(Long usuarioId) {
        buscar(usuarioId);
        activacionCuentaService.reenviar(usuarioId);
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
    private String generarClaveAleatoria() {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
