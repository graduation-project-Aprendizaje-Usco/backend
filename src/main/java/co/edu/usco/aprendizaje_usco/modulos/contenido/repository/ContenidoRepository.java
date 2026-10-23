package co.edu.usco.aprendizaje_usco.modulos.contenido.repository;

import co.edu.usco.aprendizaje_usco.modulos.contenido.model.Contenido;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContenidoRepository extends JpaRepository<Contenido, Long> {

    Optional<Contenido> findByAsignaturaAndTemaAndTitulo(String asignatura, String tema, String titulo);

}
