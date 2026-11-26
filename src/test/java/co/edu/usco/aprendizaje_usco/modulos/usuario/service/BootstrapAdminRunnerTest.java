package co.edu.usco.aprendizaje_usco.modulos.usuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Rol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

class BootstrapAdminRunnerTest {

    private UsuarioRepository usuarioRepository;
    private RolRepository rolRepository;
    private PasswordEncoder passwordEncoder;
    private ApplicationArguments arguments;
    private Rol rolAdmin;

    @BeforeEach
    void configurar() {
        usuarioRepository = mock(UsuarioRepository.class);
        rolRepository = mock(RolRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        arguments = mock(ApplicationArguments.class);
        rolAdmin = mock(Rol.class);
        when(rolAdmin.getNombre()).thenReturn(NombreRol.ADMIN);
    }

    @Test
    void sinCredencialesNoConsultaNiCreaUsuarios() throws Exception {
        runner("", "").run(arguments);

        verifyNoInteractions(usuarioRepository, rolRepository, passwordEncoder);
    }

    @Test
    void credencialesParcialesDetienenElArranque() {
        assertThatThrownBy(() -> runner("admin@example.org", "").run(arguments))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tanto BOOTSTRAP_ADMIN_EMAIL como BOOTSTRAP_ADMIN_PASSWORD");

        verifyNoInteractions(usuarioRepository, rolRepository, passwordEncoder);
    }

    @Test
    void creaAdminNormalizadoConContrasenaCodificada() throws Exception {
        when(rolRepository.findByNombreParaBloquear(NombreRol.ADMIN)).thenReturn(Optional.of(rolAdmin));
        when(usuarioRepository.findByCorreo("admin@example.org")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("ClaveRobusta2026!")).thenReturn("$2a$10$hash");
        when(usuarioRepository.saveAndFlush(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        runner("  ADMIN@Example.org ", "ClaveRobusta2026!").run(arguments);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).saveAndFlush(captor.capture());
        Usuario creado = captor.getValue();
        assertThat(creado.getCorreo()).isEqualTo("admin@example.org");
        assertThat(creado.getPasswordHash()).isEqualTo("$2a$10$hash").isNotEqualTo("ClaveRobusta2026!");
        assertThat(creado.getNombres()).isEqualTo("Admin");
        assertThat(creado.getApellidos()).isEqualTo("Inicial");
        assertThat(creado.getRol()).isSameAs(rolAdmin);
        assertThat(creado.estaActivo()).isTrue();

        InOrder orden = inOrder(rolRepository, usuarioRepository);
        orden.verify(rolRepository).findByNombreParaBloquear(NombreRol.ADMIN);
        orden.verify(usuarioRepository).findByCorreo("admin@example.org");
    }

    @Test
    void adminExistenteNoSeModificaNiSeReemplazaSuContrasena() throws Exception {
        Usuario existente = mock(Usuario.class);
        when(existente.getRol()).thenReturn(rolAdmin);
        when(rolRepository.findByNombreParaBloquear(NombreRol.ADMIN)).thenReturn(Optional.of(rolAdmin));
        when(usuarioRepository.findByCorreo("admin@example.org")).thenReturn(Optional.of(existente));

        runner("admin@example.org", "ClaveRobusta2026!").run(arguments);

        verify(usuarioRepository, never()).saveAndFlush(any(Usuario.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void noPromueveUnaCuentaExistenteDeOtroRol() {
        Rol rolEstudiante = mock(Rol.class);
        when(rolEstudiante.getNombre()).thenReturn(NombreRol.ESTUDIANTE);
        Usuario existente = mock(Usuario.class);
        when(existente.getRol()).thenReturn(rolEstudiante);
        when(rolRepository.findByNombreParaBloquear(NombreRol.ADMIN)).thenReturn(Optional.of(rolAdmin));
        when(usuarioRepository.findByCorreo("admin@example.org")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> runner("admin@example.org", "ClaveRobusta2026!").run(arguments))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya pertenece a un usuario que no es ADMIN");

        verify(usuarioRepository, never()).saveAndFlush(any(Usuario.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void rechazaContrasenaDebilAntesDeConsultarLaBase() {
        assertThatThrownBy(() -> runner("admin@example.org", "corta1").run(arguments))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BOOTSTRAP_ADMIN_PASSWORD");

        verifyNoInteractions(usuarioRepository, rolRepository, passwordEncoder);
    }

    private BootstrapAdminRunner runner(String email, String password) {
        return new BootstrapAdminRunner(usuarioRepository, rolRepository, passwordEncoder,
                email, password, " Admin ", "Inicial");
    }

}