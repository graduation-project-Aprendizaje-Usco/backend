package co.edu.usco.aprendizaje_usco.security;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Busca al usuario por correo para el login.
 *
 * Un usuario INACTIVO se trata igual que uno que no existe: así el login responde
 * siempre el mismo error genérico y no revela si el correo está registrado
 * (criterio de SCRUM-16). DaoAuthenticationProvider convierte la
 * UsernameNotFoundException en BadCredentialsException.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioAutenticado loadUserByUsername(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(Usuario.normalizarCorreo(correo))
                .filter(Usuario::estaActivo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado o inactivo"));
        return UsuarioAutenticado.desde(usuario);
    }

}
