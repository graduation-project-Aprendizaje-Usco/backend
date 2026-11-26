package co.edu.usco.aprendizaje_usco.modulos.usuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.edu.usco.aprendizaje_usco.common.exception.ConflictoException;
import co.edu.usco.aprendizaje_usco.common.exception.NoAutenticadoException;
import co.edu.usco.aprendizaje_usco.common.exception.ReglaNegocioException;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.AuthResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.LoginRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.RegistroRequest;
import co.edu.usco.aprendizaje_usco.modulos.usuario.dto.UsuarioResponse;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Rol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import co.edu.usco.aprendizaje_usco.security.JwtService;
import co.edu.usco.aprendizaje_usco.security.UsuarioAutenticado;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * SCRUM-85: pruebas de servicio del login. Todas las dependencias son simuladas
 * (Mockito): no levanta Spring ni la base de datos.
 */
class AuthServiceTest {

    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private UsuarioRepository usuarioRepository;
    private RolRepository rolRepository;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;
    private Rol rolEstudiante;

    @BeforeEach
    void configurar() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = mock(JwtService.class);
        usuarioRepository = mock(UsuarioRepository.class);
        rolRepository = mock(RolRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authService = new AuthService(authenticationManager, jwtService, usuarioRepository,
                rolRepository, passwordEncoder, mock(InvitacionService.class), false);

        rolEstudiante = mock(Rol.class);
        when(rolEstudiante.getNombre()).thenReturn(NombreRol.ESTUDIANTE);
    }

    // ------------------------------------------------------------ login

    @Test
    void loginCorrectoDevuelveTokenYDatosDelUsuario() {
        UsuarioAutenticado ana = UsuarioAutenticado.desde(usuario(7L, "ana@usco.edu.co", true));
        when(authenticationManager.authenticate(any()))
                .thenReturn(UsernamePasswordAuthenticationToken.authenticated(ana, null, List.of()));
        when(jwtService.emitir(ana))
                .thenReturn(new JwtService.TokenEmitido("token-firmado", Instant.now().plusSeconds(7200)));

        AuthResponse respuesta = authService.login(new LoginRequest("ana@usco.edu.co", "Clave2026*"));

        assertThat(respuesta.token()).isEqualTo("token-firmado");
        assertThat(respuesta.tipo()).isEqualTo("Bearer");
        assertThat(respuesta.expiraEn()).isBetween(7190L, 7200L);
        assertThat(respuesta.usuario())
                .isEqualTo(new UsuarioResponse(7L, "Ana", "ana@usco.edu.co", NombreRol.ESTUDIANTE));
    }

    @Test
    void contrasenaErradaLanza401GenericoYNoEmiteToken() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("ana@usco.edu.co", "otra")))
                .isInstanceOf(NoAutenticadoException.class)
                .hasMessage(AuthService.CREDENCIALES_INVALIDAS);
        verifyNoInteractions(jwtService);
    }

    // ------------------------------------------------------------ registro

    @Test
    void registroConCorreoRepetidoLanza409YNoGuarda() {
        when(usuarioRepository.existsByCorreo("ana@usco.edu.co")).thenReturn(true);

        // El correo se compara normalizado: "  ANA@usco.edu.co " es el mismo que ya existe.
        assertThatThrownBy(() -> authService.registrar(registro("  ANA@usco.edu.co ")))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("El correo ya está registrado");
        verify(usuarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void registroCreaEstudianteConLaContrasenaCifrada() {
        when(rolRepository.findByNombre(NombreRol.ESTUDIANTE)).thenReturn(Optional.of(rolEstudiante));
        when(passwordEncoder.encode("Clave2026*")).thenReturn("$2a$10$hash");
        when(usuarioRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse respuesta = authService.registrar(registro("Beto@USCO.edu.co"));

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).saveAndFlush(guardado.capture());
        assertThat(guardado.getValue().getCorreo()).isEqualTo("beto@usco.edu.co");
        assertThat(guardado.getValue().getPasswordHash()).isEqualTo("$2a$10$hash");
        assertThat(guardado.getValue().getRol()).isSameAs(rolEstudiante);
        assertThat(guardado.getValue().estaActivo()).isTrue();
        // Se guarda el nombre oficial del programa aunque llegue con otras mayúsculas o espacios.
        assertThat(guardado.getValue().getProgramaAcademico()).isEqualTo("Ingeniería de Software");
        assertThat(respuesta.rol()).isEqualTo(NombreRol.ESTUDIANTE);
    }

    @Test
    void registroConProgramaFueraDeLaListaLanza422YNoGuarda() {
        // Normalmente @Valid lo rechaza antes con 400; el servicio no confía en eso.
        when(rolRepository.findByNombre(NombreRol.ESTUDIANTE)).thenReturn(Optional.of(rolEstudiante));
        RegistroRequest medicina = new RegistroRequest("Beto", "Gómez", "u20201000002@usco.edu.co",
                "Clave2026*", "Medicina");

        assertThatThrownBy(() -> authService.registrar(medicina))
                .isInstanceOf(ReglaNegocioException.class);
        verify(usuarioRepository, never()).saveAndFlush(any());
    }

    // ------------------------------------------------------------ /me

    @Test
    void usuarioInactivoYaNoTieneSesionValida() {
        Usuario inactivo = usuario(7L, "ana@usco.edu.co", false);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(inactivo));

        assertThatThrownBy(() -> authService.usuarioActual(7L))
                .isInstanceOf(NoAutenticadoException.class)
                .hasMessage(AuthService.SESION_INVALIDA);
    }

    @Test
    void usuarioActualDevuelveSusDatos() {
        Usuario ana = usuario(7L, "ana@usco.edu.co", true);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(ana));

        assertThat(authService.usuarioActual(7L))
                .isEqualTo(new UsuarioResponse(7L, "Ana", "ana@usco.edu.co", NombreRol.ESTUDIANTE));
    }

    // ------------------------------------------------------------ apoyo

    private Usuario usuario(Long id, String correo, boolean activo) {
        Usuario usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn(id);
        when(usuario.getNombres()).thenReturn("Ana");
        when(usuario.getCorreo()).thenReturn(correo);
        when(usuario.getRol()).thenReturn(rolEstudiante);
        when(usuario.estaActivo()).thenReturn(activo);
        return usuario;
    }

    private static RegistroRequest registro(String correo) {
        return new RegistroRequest("Beto", "Gómez", correo, "Clave2026*", "  ingeniería de SOFTWARE ");
    }

}
