package co.edu.usco.aprendizaje_usco.modulos.usuario.repository;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.ActivacionCuenta;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivacionCuentaRepository extends JpaRepository<ActivacionCuenta, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from ActivacionCuenta a join fetch a.usuario "
            + "where a.usuario.id = :usuarioId and a.usadaEn is null")
    Optional<ActivacionCuenta> findPendienteParaRenovar(@Param("usuarioId") Long usuarioId);

    @Query("select a.usuario.id from ActivacionCuenta a where a.usuario.id in :usuarioIds and a.usadaEn is null")
    List<Long> buscarUsuariosPendientes(@Param("usuarioIds") Collection<Long> usuarioIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from ActivacionCuenta a join fetch a.usuario where a.tokenHash = :tokenHash")
    Optional<ActivacionCuenta> findParaConsumo(@Param("tokenHash") String tokenHash);
}
