package co.edu.usco.aprendizaje_usco.modulos.contenido.repository;

import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Competencia;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetenciaRepository extends JpaRepository<Competencia, Long> {

    Optional<Competencia> findByCodigo(String codigo);

}
