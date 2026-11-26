package co.edu.usco.aprendizaje_usco.modulos.diagnostico.repository;

import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.EstadoIntento;
import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.ResultadoDimension;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResultadoDimensionRepository extends JpaRepository<ResultadoDimension, Long> {

    @Query("""
            select r from ResultadoDimension r
            join fetch r.competencia c
            where r.intento.id = :intentoId
            order by c.id""")
    List<ResultadoDimension> findDelIntento(@Param("intentoId") Long intentoId);

    @Query("""
            select r from ResultadoDimension r
            join fetch r.competencia c
            join fetch r.intento i
            where i.usuario.id = :usuarioId and i.estado = :estado
            order by i.finalizadoEn desc, c.id""")
    List<ResultadoDimension> findFinalizadosDelUsuario(@Param("usuarioId") Long usuarioId,
                                                       @Param("estado") EstadoIntento estado);

}
