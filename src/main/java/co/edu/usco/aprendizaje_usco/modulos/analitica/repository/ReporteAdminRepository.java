package co.edu.usco.aprendizaje_usco.modulos.analitica.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReporteAdminRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ReporteAdminRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record PracticaAgregada(long intentos, long aciertos, long estudiantesConActividad) {
    }

    public record DiagnosticoAgregado(
            long finalizados,
            BigDecimal puntajePromedio,
            BigDecimal porcentajePromedio,
            long estudiantesConActividad) {
    }

    public record CategoriaAgregada(String categoria, long cantidad) {
    }

    public PracticaAgregada practica(Instant desde, Instant hasta) {
        StringBuilder sql = new StringBuilder("""
                SELECT count(*) AS intentos,
                       count(*) FILTER (WHERE es_correcta) AS aciertos,
                       count(DISTINCT usuario_id) AS estudiantes_activos
                FROM intento_practica
                WHERE true
                """);
        MapSqlParameterSource params = agregarRango(desde, hasta);
        agregarFiltrosFecha(sql, params, "creado_en");
        return jdbc.queryForObject(sql.toString(), params, (rs, row) ->
                new PracticaAgregada(
                        rs.getLong("intentos"),
                        rs.getLong("aciertos"),
                        rs.getLong("estudiantes_activos")));
    }

    public DiagnosticoAgregado diagnostico(Instant desde, Instant hasta) {
        StringBuilder sql = new StringBuilder("""
                SELECT count(*) AS finalizados,
                       avg(i.puntaje_total) AS puntaje_promedio,
                       avg(100.0 * i.puntaje_total / nullif(rd.total_preguntas, 0)) AS porcentaje_promedio,
                       count(DISTINCT i.usuario_id) AS estudiantes_activos
                FROM intento_diagnostico i
                LEFT JOIN (
                    SELECT intento_id, sum(total) AS total_preguntas
                    FROM resultado_dimension
                    GROUP BY intento_id
                ) rd ON rd.intento_id = i.id
                WHERE i.estado = 'FINALIZADO'
                """);
        MapSqlParameterSource params = agregarRango(desde, hasta);
        agregarFiltrosFecha(sql, params, "i.finalizado_en");
        return jdbc.queryForObject(sql.toString(), params, (rs, row) ->
                new DiagnosticoAgregado(
                        rs.getLong("finalizados"),
                        rs.getBigDecimal("puntaje_promedio"),
                        rs.getBigDecimal("porcentaje_promedio"),
                        rs.getLong("estudiantes_activos")));
    }

    public List<CategoriaAgregada> distribucionDiagnosticos(Instant desde, Instant hasta) {
        StringBuilder sql = new StringBuilder("""
                SELECT i.categoria, count(*) AS cantidad
                FROM intento_diagnostico i
                WHERE i.estado = 'FINALIZADO'
                """);
        MapSqlParameterSource params = agregarRango(desde, hasta);
        agregarFiltrosFecha(sql, params, "i.finalizado_en");
        sql.append(" GROUP BY i.categoria ORDER BY i.categoria");
        return jdbc.query(sql.toString(), params, (rs, row) ->
                new CategoriaAgregada(rs.getString("categoria"), rs.getLong("cantidad")));
    }

    private static MapSqlParameterSource agregarRango(Instant desde, Instant hasta) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (desde != null) {
            params.addValue("desde", desde.atOffset(java.time.ZoneOffset.UTC));
        }
        if (hasta != null) {
            params.addValue("hasta", hasta.atOffset(java.time.ZoneOffset.UTC));
        }
        return params;
    }

    private static void agregarFiltrosFecha(StringBuilder sql, MapSqlParameterSource params, String columna) {
        if (params.hasValue("desde")) {
            sql.append(" AND ").append(columna).append(" >= :desde");
        }
        if (params.hasValue("hasta")) {
            sql.append(" AND ").append(columna).append(" < :hasta");
        }
    }
}
