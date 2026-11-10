package co.edu.usco.aprendizaje_usco.modulos.contenido.repository;

import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Contenido;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContenidoRepository extends JpaRepository<Contenido, Long> {

    Optional<Contenido> findByAsignaturaAndTemaAndTitulo(String asignatura, String tema, String titulo);

    @EntityGraph(attributePaths = "competencia")
    Optional<Contenido> findByIdAndCreadoPor_Id(Long id, Long docenteId);

    @Query("""
            select c from Contenido c
            join fetch c.competencia
            where c.creadoPor.id = :docenteId
              and (:asignatura is null or c.asignatura = :asignatura)
              and (:competencia is null or c.competencia.codigo = :competencia)
              and (:activo is null or c.activo = :activo)
              and (:texto = ''
                   or lower(c.titulo) like lower(concat('%', :texto, '%'))
                   or lower(c.tema) like lower(concat('%', :texto, '%')))
            order by c.titulo, c.id""")
    List<Contenido> buscarDelDocente(@Param("docenteId") Long docenteId,
                                     @Param("asignatura") String asignatura,
                                     @Param("competencia") String competencia,
                                     @Param("activo") Boolean activo,
                                     @Param("texto") String texto);

}
