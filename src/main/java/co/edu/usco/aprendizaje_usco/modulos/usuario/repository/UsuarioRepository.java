package co.edu.usco.aprendizaje_usco.modulos.usuario.repository;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Importante: los correos se guardan en minúsculas. Antes de buscar, normalizar
 * con Usuario.normalizarCorreo(correo), o "Ana@USCO.edu.co" no encontrará nada.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Trae también el rol en la misma consulta (lo necesita el login para el JWT). */
    @EntityGraph(attributePaths = "rol")
    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    /**
     * Bloquea la fila del usuario durante la transacción. Se usa al registrar un
     * intento de práctica para que dos envíos simultáneos del mismo estudiante
     * (el doble clic o un reintento de red) se atiendan en serie y la clave de
     * idempotencia se evalúe después de que el primero haya terminado.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> findParaBloquear(@Param("id") Long id);

}
