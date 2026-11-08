package co.edu.usco.aprendizaje_usco.modulos.practica.repository;

import co.edu.usco.aprendizaje_usco.modulos.practica.model.IntentoPractica;
import co.edu.usco.aprendizaje_usco.modulos.progreso.dto.MetricasDimensionConsulta;
import co.edu.usco.aprendizaje_usco.modulos.progreso.dto.TendenciaSemanalProyeccion;
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

    long countByUsuarioId(Long usuarioId);

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

    @Query("""
            select count(distinct i.ejercicio.id) from IntentoPractica i
            where i.usuario.id = :usuarioId and i.esCorrecta = true""")
    long countEjerciciosCompletados(@Param("usuarioId") Long usuarioId);

    @Query("""
            select new co.edu.usco.aprendizaje_usco.modulos.progreso.dto.MetricasDimensionConsulta(
                i.competencia.id,
                count(i.id),
                sum(case when i.esCorrecta = true then 1L else 0L end)
            )
            from IntentoPractica i
            where i.usuario.id = :usuarioId
            group by i.competencia.id""")
    List<MetricasDimensionConsulta> findMetricasPorDimension(@Param("usuarioId") Long usuarioId);

    @Query(value = """
            SELECT 
                CAST(DATE_TRUNC('week', creado_en) AS DATE) AS fechaInicio,
                COUNT(id) AS intentosEvaluados,
                COUNT(CASE WHEN es_correcta THEN 1 END) AS intentosCorrectos
            FROM intento_practica
            WHERE usuario_id = :usuarioId
            GROUP BY DATE_TRUNC('week', creado_en)
            ORDER BY fechaInicio ASC""", nativeQuery = true)
    List<TendenciaSemanalProyeccion> findTendenciaSemanal(@Param("usuarioId") Long usuarioId);

}
