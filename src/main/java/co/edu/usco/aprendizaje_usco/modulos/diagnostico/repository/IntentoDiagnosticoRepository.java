package co.edu.usco.aprendizaje_usco.modulos.diagnostico.repository;

import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.EstadoIntento;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.IntentoDiagnostico;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IntentoDiagnosticoRepository extends JpaRepository<IntentoDiagnostico, Long> {

    /**
     * Carga el intento bloqueando su fila (SELECT ... FOR UPDATE) hasta el fin de la
     * transacción: así dos respuestas o una respuesta y la finalización del mismo
     * intento nunca se cruzan.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from IntentoDiagnostico i join fetch i.prueba where i.id = :id")
    Optional<IntentoDiagnostico> findParaActualizar(@Param("id") Long id);

    /**
     * Bloquea la fila del estudiante: dos "iniciar prueba" simultáneos del mismo
     * estudiante se atienden uno tras otro y el segundo encuentra el intento ya creado.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :usuarioId")
    Optional<Usuario> bloquearUsuario(@Param("usuarioId") Long usuarioId);

    /** El intento más reciente del estudiante en esa prueba (EN_CURSO o FINALIZADO). */
    Optional<IntentoDiagnostico> findFirstByUsuarioIdAndPruebaIdOrderByIniciadoEnDesc(Long usuarioId, Long pruebaId);

    Optional<IntentoDiagnostico> findFirstByUsuarioIdAndEstadoOrderByFinalizadoEnDesc(Long usuarioId, EstadoIntento estado);

    /** Ids de los intentos que siguen EN_CURSO (para el barrido de intentos vencidos). */
    @Query("select i.id from IntentoDiagnostico i where i.estado = :estado")
    List<Long> findIdsPorEstado(@Param("estado") EstadoIntento estado);

}
