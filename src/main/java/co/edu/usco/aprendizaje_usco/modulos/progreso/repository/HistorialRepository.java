package co.edu.usco.aprendizaje_usco.modulos.progreso.repository;

import co.edu.usco.aprendizaje_usco.modulos.progreso.dto.HistorialItemResponse;
import co.edu.usco.aprendizaje_usco.modulos.progreso.model.TipoActividad;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Historial = diagnósticos finalizados + intentos de práctica de un estudiante, en una sola
 * lista. Se usa SQL (UNION ALL) porque mezcla dos tablas que JPA no relaciona entre sí.
 * Los filtros se agregan solo si llegan, y siempre como parámetros (nunca concatenados).
 */
@Repository
public class HistorialRepository {

    /** Filtros ya resueltos por el servicio. Cualquiera puede ser null (= sin filtro). */
    public record Filtro(long estudianteId, TipoActividad tipo, String competencia, Instant desde, Instant hasta) {
    }

    private final NamedParameterJdbcTemplate jdbc;

    public HistorialRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<HistorialItemResponse> buscar(Filtro filtro, int pagina, int tamano) {
        MapSqlParameterSource params = parametros(filtro)
                .addValue("limite", tamano)
                .addValue("desplazamiento", (long) pagina * tamano);
        return jdbc.query("SELECT * FROM (" + union(filtro) + ") h" + filtroDeFecha(filtro)
                + " ORDER BY fecha DESC, tipo, id DESC LIMIT :limite OFFSET :desplazamiento", params, this::mapear);
    }

    public long contar(Filtro filtro) {
        Long total = jdbc.queryForObject("SELECT count(*) FROM (" + union(filtro) + ") h" + filtroDeFecha(filtro),
                parametros(filtro), Long.class);
        return total == null ? 0 : total;
    }

    /** ¿El estudiante está inscrito en algún grupo de este docente? */
    public boolean estudianteEnGruposDelDocente(long docenteId, long estudianteId) {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
                SELECT EXISTS (SELECT 1 FROM grupo g JOIN grupo_estudiante ge ON ge.grupo_id = g.id
                               WHERE g.docente_id = :docente AND ge.estudiante_id = :estudiante)""",
                new MapSqlParameterSource().addValue("docente", docenteId).addValue("estudiante", estudianteId),
                Boolean.class));
    }

    private static String union(Filtro filtro) {
        List<String> partes = new ArrayList<>();
        if (filtro.tipo() == null || filtro.tipo() == TipoActividad.DIAGNOSTICO) partes.add(diagnosticos(filtro));
        if (filtro.tipo() == null || filtro.tipo() == TipoActividad.PRACTICA) partes.add(practicas(filtro));
        return String.join(" UNION ALL ", partes);
    }

    /** Con filtro de competencia, el diagnóstico muestra los aciertos de esa competencia (resultado_dimension). */
    private static String diagnosticos(Filtro filtro) {
        boolean porCompetencia = filtro.competencia() != null;
        return """
                SELECT 'DIAGNOSTICO' AS tipo, i.id, i.finalizado_en AS fecha, p.nombre AS actividad,
                       %s AS competencia_codigo, %s AS competencia_nombre,
                       %s AS aciertos, %s AS total, i.categoria,
                       CAST(NULL AS BOOLEAN) AS correcto, CAST(NULL AS INTEGER) AS nivel,
                       CAST(NULL AS INTEGER) AS tiempo_seg, CAST(NULL AS BOOLEAN) AS pista_usada
                FROM intento_diagnostico i
                JOIN prueba_diagnostica p ON p.id = i.prueba_id
                %s
                WHERE i.usuario_id = :estudiante AND i.estado = 'FINALIZADO'
                """.formatted(
                porCompetencia ? "c.codigo" : "CAST(NULL AS VARCHAR)",
                porCompetencia ? "c.nombre" : "CAST(NULL AS VARCHAR)",
                porCompetencia ? "rd.aciertos" : "i.puntaje_total",
                porCompetencia ? "rd.total"
                        : "(SELECT CAST(sum(t.total) AS INTEGER) FROM resultado_dimension t WHERE t.intento_id = i.id)",
                porCompetencia ? """
                        JOIN resultado_dimension rd ON rd.intento_id = i.id
                        JOIN competencia c ON c.id = rd.competencia_id AND c.codigo = :competencia""" : "");
    }

    private static String practicas(Filtro filtro) {
        return """
                SELECT 'PRACTICA' AS tipo, ip.id, ip.creado_en AS fecha, co.titulo AS actividad,
                       c.codigo AS competencia_codigo, c.nombre AS competencia_nombre,
                       CAST(NULL AS INTEGER) AS aciertos, CAST(NULL AS INTEGER) AS total, CAST(NULL AS VARCHAR) AS categoria,
                       ip.es_correcta AS correcto, CAST(ip.nivel_ejercicio AS INTEGER) AS nivel,
                       ip.tiempo_seg, ip.pista_usada
                FROM intento_practica ip
                JOIN ejercicio e ON e.id = ip.ejercicio_id
                JOIN contenido co ON co.id = e.contenido_id
                JOIN competencia c ON c.id = ip.competencia_id
                WHERE ip.usuario_id = :estudiante
                """ + (filtro.competencia() != null ? " AND c.codigo = :competencia" : "");
    }

    private static String filtroDeFecha(Filtro filtro) {
        List<String> condiciones = new ArrayList<>();
        if (filtro.desde() != null) condiciones.add("fecha >= :desde");
        if (filtro.hasta() != null) condiciones.add("fecha < :hasta");
        return condiciones.isEmpty() ? "" : " WHERE " + String.join(" AND ", condiciones);
    }

    private static MapSqlParameterSource parametros(Filtro filtro) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("estudiante", filtro.estudianteId());
        if (filtro.competencia() != null) params.addValue("competencia", filtro.competencia());
        // Instant → timestamptz: se pasa como OffsetDateTime en UTC para que el driver no lo interprete en la zona local.
        if (filtro.desde() != null) params.addValue("desde", filtro.desde().atOffset(ZoneOffset.UTC));
        if (filtro.hasta() != null) params.addValue("hasta", filtro.hasta().atOffset(ZoneOffset.UTC));
        return params;
    }

    private HistorialItemResponse mapear(ResultSet rs, int fila) throws SQLException {
        return new HistorialItemResponse(
                TipoActividad.valueOf(rs.getString("tipo")),
                rs.getLong("id"),
                rs.getObject("fecha", OffsetDateTime.class).toInstant(),
                rs.getString("actividad"),
                rs.getString("competencia_codigo"),
                rs.getString("competencia_nombre"),
                rs.getObject("aciertos", Integer.class),
                rs.getObject("total", Integer.class),
                rs.getString("categoria"),
                rs.getObject("correcto", Boolean.class),
                rs.getObject("nivel", Integer.class),
                rs.getObject("tiempo_seg", Integer.class),
                rs.getObject("pista_usada", Boolean.class));
    }

}
