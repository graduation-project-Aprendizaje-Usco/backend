package co.edu.usco.aprendizaje_usco.modulos.diagnostico.repository;

import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.Item;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Ítems activos de una prueba en el orden de presentación, con sus opciones y su
     * competencia en la misma consulta (evita una consulta por ítem).
     */
    @Query("""
            select distinct i from Item i
            join fetch i.competencia
            left join fetch i.opciones
            where i.prueba.id = :pruebaId and i.activo = true
            order by i.numero""")
    List<Item> findActivosConOpciones(@Param("pruebaId") Long pruebaId);

    @Query("""
            select i from Item i
            left join fetch i.opciones
            where i.id = :itemId and i.prueba.id = :pruebaId and i.activo = true""")
    Optional<Item> findActivoDePrueba(@Param("itemId") Long itemId, @Param("pruebaId") Long pruebaId);

    long countByPruebaIdAndActivoTrue(Long pruebaId);

}
