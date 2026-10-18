package co.edu.usco.aprendizaje_usco.modulos.diagnostico.repository;

import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.PruebaDiagnostica;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PruebaDiagnosticaRepository extends JpaRepository<PruebaDiagnostica, Long> {

    /** La prueba que se aplica hoy (como máximo hay una: uq_prueba_una_activa). */
    Optional<PruebaDiagnostica> findFirstByActivaTrue();

}
