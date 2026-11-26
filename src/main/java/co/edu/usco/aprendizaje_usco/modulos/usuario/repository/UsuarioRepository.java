package co.edu.usco.aprendizaje_usco.modulos.usuario.repository;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.EstadoUsuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import jakarta.persistence.LockModeType;
import java.util.List;
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

    long countByRol_Nombre(NombreRol rol);

    long countByRol_NombreAndEstado(NombreRol rol, EstadoUsuario estado);

    /**
     * Lista del administrador. Cada filtro es opcional (null = sin filtro);
     * {@code texto} llega ya en minúsculas y con comodines ("%ana%").
     */
    @EntityGraph(attributePaths = "rol")
    @Query("""
            select u from Usuario u
            where (:rol is null or u.rol.nombre = :rol)
              and (:estado is null or u.estado = :estado)
              and (:texto is null or lower(concat(u.nombres, ' ', u.apellidos, ' ', u.correo)) like :texto)
            order by u.apellidos, u.nombres""")
    List<Usuario> buscarParaAdmin(@Param("rol") NombreRol rol, @Param("estado") EstadoUsuario estado,
                                  @Param("texto") String texto);

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
