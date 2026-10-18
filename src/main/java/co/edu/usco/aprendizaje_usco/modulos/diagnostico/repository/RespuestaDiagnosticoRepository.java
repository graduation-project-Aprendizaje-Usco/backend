package co.edu.usco.aprendizaje_usco.modulos.diagnostico.repository;

import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.RespuestaDiagnostico;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RespuestaDiagnosticoRepository extends JpaRepository<RespuestaDiagnostico, Long> {

    @Query("""
            select r from RespuestaDiagnostico r
            join fetch r.item join fetch r.opcion
            where r.intento.id = :intentoId""")
    List<RespuestaDiagnostico> findDelIntento(@Param("intentoId") Long intentoId);

    Optional<RespuestaDiagnostico> findByIntentoIdAndItemId(Long intentoId, Long itemId);

}
