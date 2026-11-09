package co.edu.usco.aprendizaje_usco.modulos.analitica.repository;

import co.edu.usco.aprendizaje_usco.modulos.diagnostico.model.CategoriaDesempeno;
import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Consultas batch de la analitica por grupo. Se usa SQL porque las metricas
 * cruzan intentos de practica, diagnosticos y competencias, y se necesita evitar
 * una consulta por estudiante (N+1).
 */
@Repository
public class AnaliticaGrupoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AnaliticaGrupoRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record MetricasEstudianteDimension(
            Long estudianteId,
            Long competenciaId,
            String codigo,
            String nombre,
            long intentos,
            long aciertos) {
    }

    public record DiagnosticoResumen(
            Long estudianteId,
            Long intentoId,
            CategoriaDesempeno categoria,
            int puntaje,
            int total,
            Instant finalizadoEn) {
    }

    /** Aciertos e intentos de practica por estudiante y competencia. */
    public List<MetricasEstudianteDimension> metricasPorEstudiante(
            List<Long> estudiantes, String competencia, Instant desde, Instant hasta) {
        if (estudiantes.isEmpty()) {
            return List.of();
        }

        StringBuilder sql = new StringBuilder("""
                SELECT ip.usuario_id, ip.competencia_id, c.codigo, c.nombre,
                       count(*) AS intentos,
                       count(*) FILTER (WHERE ip.es_correcta) AS aciertos
                FROM intento_practica ip
                JOIN competencia c ON c.id = ip.competencia_id
                WHERE ip.usuario_id IN (:estudiantes)
                """);
        MapSqlParameterSource params = new MapSqlParameterSource("estudiantes", estudiantes);
        agregarFiltros(sql, params, "ip.creado_en", competencia, desde, hasta);
        sql.append("""

                GROUP BY ip.usuario_id, ip.competencia_id, c.codigo, c.nombre
                ORDER BY ip.usuario_id, ip.competencia_id
                """);

        return jdbc.query(sql.toString(), params, (rs, i) -> new MetricasEstudianteDimension(
                rs.getLong("usuario_id"),
                rs.getLong("competencia_id"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getLong("intentos"),
                rs.getLong("aciertos")));
    }

    /** Ultimo diagnostico finalizado de cada estudiante, dentro del periodo. */
    public List<DiagnosticoResumen> ultimosDiagnosticos(
            List<Long> estudiantes, Instant desde, Instant hasta) {
        if (estudiantes.isEmpty()) {
            return List.of();
        }

        StringBuilder sql = new StringBuilder("""
                SELECT DISTINCT ON (i.usuario_id)
                       i.usuario_id, i.id, i.categoria, i.puntaje_total, i.finalizado_en,
                       COALESCE((SELECT sum(rd.total) FROM resultado_dimension rd WHERE rd.intento_id = i.id), 0)
                           AS total
                FROM intento_diagnostico i
                WHERE i.usuario_id IN (:estudiantes)
                  AND i.estado = 'FINALIZADO'
                """);
        MapSqlParameterSource params = new MapSqlParameterSource("estudiantes", estudiantes);
        if (desde != null) {
            sql.append(" AND i.finalizado_en >= :desde");
            params.addValue("desde", desde.atOffset(java.time.ZoneOffset.UTC));
        }
        if (hasta != null) {
            sql.append(" AND i.finalizado_en < :hasta");
            params.addValue("hasta", hasta.atOffset(java.time.ZoneOffset.UTC));
        }
        sql.append(" ORDER BY i.usuario_id, i.finalizado_en DESC, i.id DESC");

        return jdbc.query(sql.toString(), params, (rs, i) -> new DiagnosticoResumen(
                rs.getLong("usuario_id"),
                rs.getLong("id"),
                CategoriaDesempeno.valueOf(rs.getString("categoria")),
                rs.getInt("puntaje_total"),
                rs.getInt("total"),
                rs.getTimestamp("finalizado_en").toInstant()));
    }

    private static void agregarFiltros(StringBuilder sql, MapSqlParameterSource params,
                                       String columnaFecha, String competencia,
                                       Instant desde, Instant hasta) {
        if (competencia != null) {
            sql.append(" AND c.codigo = :competencia");
            params.addValue("competencia", competencia);
        }
        if (desde != null) {
            sql.append(" AND ").append(columnaFecha).append(" >= :desde");
            params.addValue("desde", desde.atOffset(java.time.ZoneOffset.UTC));
        }
        if (hasta != null) {
            sql.append(" AND ").append(columnaFecha).append(" < :hasta");
            params.addValue("hasta", hasta.atOffset(java.time.ZoneOffset.UTC));
        }
    }
}
