package co.edu.usco.aprendizaje_usco.modulos.contenido.repository;

import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Ejercicio;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.NivelDificultad;
import co.edu.usco.aprendizaje_usco.modulos.contenido.model.TipoEjercicio;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EjercicioRepository extends JpaRepository<Ejercicio, Long> {

    @Query("""
            select e from Ejercicio e
            join fetch e.contenido c
            join fetch e.competencia
            where e.activo = true and c.activo = true and c.asignatura = :asignatura
            order by e.id""")
    List<Ejercicio> findActivosPorAsignatura(@Param("asignatura") String asignatura);

    @Query("""
            select distinct e from Ejercicio e
            join fetch e.contenido c
            join fetch e.competencia
            left join fetch e.opciones
            where e.id = :id and e.activo = true and c.activo = true""")
    Optional<Ejercicio> findActivoConOpciones(@Param("id") Long id);

    Optional<Ejercicio> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByContenidoId(Long contenidoId);

    long countByContenidoId(Long contenidoId);

    long countByCreadoPorIsNull();

    long countByCreadoPorIsNullAndContenidoAsignatura(String asignatura);

    @Query("""
            select distinct e from Ejercicio e
            join fetch e.contenido c
            join fetch e.competencia
            left join fetch e.opciones
            where e.id = :id and e.creadoPor.id = :docenteId""")
    Optional<Ejercicio> findPropioConOpciones(@Param("id") Long id, @Param("docenteId") Long docenteId);

    @Query("""
            select distinct e from Ejercicio e
            join fetch e.contenido c
            join fetch e.competencia
            left join fetch e.opciones
            where e.creadoPor.id = :docenteId
              and (:contenidoId is null or c.id = :contenidoId)
              and (:competencia is null or e.competencia.codigo = :competencia)
              and (:nivel is null or e.nivel = :nivel)
              and (:tipo is null or e.tipo = :tipo)
              and (:activo is null or e.activo = :activo)
              and (:texto = ''
                   or lower(e.enunciado) like lower(concat('%', :texto, '%'))
                   or lower(e.codigo) like lower(concat('%', :texto, '%')))
            order by e.codigo, e.id""")
    List<Ejercicio> buscarDelDocente(@Param("docenteId") Long docenteId,
                                     @Param("contenidoId") Long contenidoId,
                                     @Param("competencia") String competencia,
                                     @Param("nivel") NivelDificultad nivel,
                                     @Param("tipo") TipoEjercicio tipo,
                                     @Param("activo") Boolean activo,
                                     @Param("texto") String texto);

}
