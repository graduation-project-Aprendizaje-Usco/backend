package co.edu.usco.aprendizaje_usco.modulos.practica.repository;

import co.edu.usco.aprendizaje_usco.modulos.practica.model.IntentoPractica;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IntentoPracticaRepository extends JpaRepository<IntentoPractica, Long> {

    Optional<IntentoPractica> findByUsuarioIdAndClaveIdempotencia(Long usuarioId, UUID claveIdempotencia);

    long countByUsuarioIdAndEjercicioId(Long usuarioId, Long ejercicioId);

    boolean existsByUsuarioIdAndEjercicioIdAndNumeroIntentoAndEsCorrectaTrue(
            Long usuarioId, Long ejercicioId, int numeroIntento);

    @Query("""
            select i from IntentoPractica i
            join fetch i.ejercicio e
            join fetch e.competencia
            where i.usuario.id = :usuarioId and i.numeroIntento = 1
            order by i.creadoEn desc""")
    List<IntentoPractica> findUltimosPrimerosIntentos(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("""
            select distinct i.ejercicio.id from IntentoPractica i
            where i.usuario.id = :usuarioId
              and i.numeroIntento = 1
              and i.esCorrecta = true""")
    Set<Long> findEjerciciosAcertadosEnPrimerIntento(@Param("usuarioId") Long usuarioId);

}
