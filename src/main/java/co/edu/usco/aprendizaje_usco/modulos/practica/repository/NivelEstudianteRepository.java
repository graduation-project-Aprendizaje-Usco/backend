package co.edu.usco.aprendizaje_usco.modulos.practica.repository;

import co.edu.usco.aprendizaje_usco.modulos.practica.model.NivelEstudiante;
import co.edu.usco.aprendizaje_usco.modulos.practica.model.NivelEstudianteId;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NivelEstudianteRepository extends JpaRepository<NivelEstudiante, NivelEstudianteId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select n from NivelEstudiante n
            join fetch n.competencia
            join fetch n.usuario
            where n.id = :id""")
    Optional<NivelEstudiante> findParaActualizar(@Param("id") NivelEstudianteId id);

    @Query("""
            select n from NivelEstudiante n
            join fetch n.competencia
            where n.usuario.id = :usuarioId""")
    List<NivelEstudiante> findDelUsuario(@Param("usuarioId") Long usuarioId);

}
