package co.edu.usco.aprendizaje_usco.modulos.usuario.repository;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Importante: los correos se guardan en minúsculas. Antes de buscar, normalizar
 * con Usuario.normalizarCorreo(correo), o "Ana@USCO.edu.co" no encontrará nada.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Trae también el rol en la misma consulta (lo necesita el login para el JWT). */
    @EntityGraph(attributePaths = "rol")
    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

}
