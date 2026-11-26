package co.edu.usco.aprendizaje_usco.security;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Usuario que Spring Security maneja durante el login.
 *
 * Copia solo lo necesario de la entidad Usuario (id, correo, nombres, rol) para
 * no depender de una sesión de JPA abierta. Con estos datos se arman después los
 * claims del JWT (sub = id, correo, rol).
 *
 * La autoridad es "ROLE_" + rol, que es lo que espera @PreAuthorize("hasRole('DOCENTE')").
 */
public final class UsuarioAutenticado implements UserDetails, CredentialsContainer {

    private final Long id;
    private final String correo;
    private final String nombres;
    private final NombreRol rol;
    private String passwordHash;

    private UsuarioAutenticado(Long id, String correo, String nombres, NombreRol rol, String passwordHash) {
        this.id = id;
        this.correo = correo;
        this.nombres = nombres;
        this.rol = rol;
        this.passwordHash = passwordHash;
    }

    /** El rol debe venir cargado (UsuarioRepository.findByCorreo lo trae en la misma consulta). */
    public static UsuarioAutenticado desde(Usuario usuario) {
        return new UsuarioAutenticado(
                usuario.getId(),
                usuario.getCorreo(),
                usuario.getNombres(),
                usuario.getRol().getNombre(),
                usuario.getPasswordHash());
    }

    public Long getId() {
        return id;
    }

    public String getCorreo() {
        return correo;
    }

    public String getNombres() {
        return nombres;
    }

    public NombreRol getRol() {
        return rol;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    /** Para Spring Security el "username" es el correo. */
    @Override
    public String getUsername() {
        return correo;
    }

    /** Spring Security lo llama después de autenticar: el hash no queda en memoria. */
    @Override
    public void eraseCredentials() {
        this.passwordHash = null;
    }

    /** Sin passwordHash a propósito: el toString termina en logs. */
    @Override
    public String toString() {
        return "UsuarioAutenticado{id=" + id + ", correo=" + correo + ", rol=" + rol + "}";
    }

}
