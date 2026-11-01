package co.edu.usco.aprendizaje_usco.modulos.usuario.repository;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Grupo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrupoRepository extends JpaRepository<Grupo, Long> {

    boolean existsByCodigo(String codigo);

    List<Grupo> findAllByOrderByCodigoAsc();

    List<Grupo> findAllByDocente_IdOrderByCodigoAsc(Long docenteId);
}
