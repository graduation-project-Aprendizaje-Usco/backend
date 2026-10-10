package co.edu.usco.aprendizaje_usco.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Rol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * SCRUM-82: PasswordEncoder + UsuarioDetailsService + AuthenticationManager.
 * Arma los mismos beans de SecurityConfig con un repositorio simulado:
 * no levanta Spring ni la base de datos.
 */
class AutenticacionTest {

    private static final String CORREO = "ana@usco.edu.co";
    private static final String CLAVE = "Clave2026*";

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private Usuario ana;

    @BeforeEach
    void configurar() {
        usuarioRepository = mock(UsuarioRepository.class);
        SecurityConfig config = new SecurityConfig(null);
        passwordEncoder = config.passwordEncoder();
        authenticationManager = config.authenticationManager(
                new UsuarioDetailsService(usuarioRepository), passwordEncoder);

        Rol estudiante = mock(Rol.class);
        when(estudiante.getNombre()).thenReturn(NombreRol.ESTUDIANTE);
        ana = new Usuario("Ana", "Pérez", CORREO, passwordEncoder.encode(CLAVE), estudiante);
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(ana));
    }

    @Test
    void contrasenaCorrectaAutenticaConElRolDelUsuario() {
        Authentication resultado = autenticar(CORREO, CLAVE);

        assertThat(resultado.isAuthenticated()).isTrue();
        assertThat(resultado.getPrincipal()).isInstanceOf(UsuarioAutenticado.class);
        UsuarioAutenticado usuario = (UsuarioAutenticado) resultado.getPrincipal();
        assertThat(usuario.getCorreo()).isEqualTo(CORREO);
        assertThat(usuario.getRol()).isEqualTo(NombreRol.ESTUDIANTE);
        // Spring Security 7 agrega además "FACTOR_PASSWORD" (con qué factor se autenticó).
        assertThat(resultado.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_ESTUDIANTE");
        // Después de autenticar, ni la contraseña ni el hash quedan en memoria.
        assertThat(resultado.getCredentials()).isNull();
        assertThat(usuario.getPassword()).isNull();
    }

    @Test
    void contrasenaIncorrectaLanzaBadCredentials() {
        assertThatThrownBy(() -> autenticar(CORREO, "otraClave"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void usuarioInactivoLanzaBadCredentialsAunqueLaContrasenaSeaCorrecta() {
        ana.desactivar();

        assertThatThrownBy(() -> autenticar(CORREO, CLAVE))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void correoNoRegistradoLanzaBadCredentials() {
        assertThatThrownBy(() -> autenticar("nadie@usco.edu.co", CLAVE))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void correoConMayusculasYEspaciosSeNormaliza() {
        assertThat(autenticar("  Ana@USCO.edu.co ", CLAVE).isAuthenticated()).isTrue();
    }

    @Test
    void laContrasenaSeGuardaComoHashBcrypt() {
        assertThat(ana.getPasswordHash()).startsWith("$2a$10$").isNotEqualTo(CLAVE);
    }

    private Authentication autenticar(String correo, String clave) {
        return authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(correo, clave));
    }

}
