package co.edu.usco.aprendizaje_usco.modulos.usuario.repository;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.GrupoEstudiante;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.GrupoEstudianteId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GrupoEstudianteRepository extends JpaRepository<GrupoEstudiante, GrupoEstudianteId> {

    @Query("""
            select ge from GrupoEstudiante ge
            join fetch ge.estudiante
            where ge.grupo.id = :grupoId
            order by ge.estudiante.apellidos, ge.estudiante.nombres
            """)
    List<GrupoEstudiante> buscarPorGrupo(@Param("grupoId") Long grupoId);
}
