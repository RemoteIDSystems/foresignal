package ai.foresignal.weather;

import ai.foresignal.source.spi.Geo;
import ai.foresignal.store.query.PgArrays;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link WeatherRecords} in SQL (prompt 056). Current reads join {@code record_head} and {@code record_live} only
 * (invariant HIST-2) with the current payload {@code jsonb_strip_nulls(h.payload || l.kinematic)}; period reads use
 * {@code record_version} by seq range. Distances are geodesic ({@code geography}).
 */
@Component
public class JdbcWeatherRecords implements WeatherRecords {

    private static final String CURRENT = "SELECT h.source_id, h.external_id, h.kind, h.seq, l.observed_at,"
            + " jsonb_strip_nulls(h.payload || l.kinematic)::text AS payload, ST_AsBinary(l.geom) AS geom";

    private static final String JOIN = " FROM record_head h JOIN record_live l USING (source_id, external_id)";

    private static final String VERSION = "SELECT v.source_id, v.external_id, v.kind, v.seq, v.observed_at,"
            + " v.payload::text AS payload, ST_AsBinary(v.geom) AS geom, CAST(NULL AS double precision) AS km"
            + " FROM record_version v";

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final NamedParameterJdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the reader. */
    public JdbcWeatherRecords(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = new NamedParameterJdbcTemplate(jdbc);
        this.json = json;
    }

    @Override
    public List<Row> nearby(String sourceId, String kind, Geometry geometry, double radiusKm, int limit) {
        String sql = CURRENT + ", ST_Distance(l.geom::geography, ST_GeomFromWKB(:g, 4326)::geography) / 1000.0 AS km"
                + JOIN + " WHERE NOT h.tombstone AND h.source_id = :source AND h.kind = :kind AND l.geom IS NOT NULL"
                + " AND ST_DWithin(l.geom::geography, ST_GeomFromWKB(:g, 4326)::geography, :meters)"
                + " ORDER BY km, h.external_id LIMIT :limit";
        return jdbc.query(sql, new MapSqlParameterSource("g", wkb(geometry))
                .addValue("source", sourceId)
                .addValue("kind", kind)
                .addValue("meters", radiusKm * 1000.0)
                .addValue("limit", Math.max(1, limit)), this::row);
    }

    @Override
    public List<Row> intersecting(String sourceId, String kind, Geometry geometry, Instant at, int limit) {
        String sql = CURRENT + ", CAST(NULL AS double precision) AS km" + JOIN
                + " WHERE NOT h.tombstone AND h.source_id = :source AND h.kind = :kind AND l.geom IS NOT NULL"
                + " AND ST_Intersects(l.geom, ST_GeomFromWKB(:g, 4326))"
                + " AND (l.valid_until IS NULL OR l.valid_until > :at)"
                + " ORDER BY h.external_id LIMIT :limit";
        return jdbc.query(sql, new MapSqlParameterSource("g", wkb(geometry))
                .addValue("source", sourceId)
                .addValue("kind", kind)
                .addValue("at", Timestamp.from(at))
                .addValue("limit", Math.max(1, limit)), this::row);
    }

    @Override
    public Optional<Row> head(String sourceId, String externalId) {
        String sql = CURRENT + ", CAST(NULL AS double precision) AS km" + JOIN
                + " WHERE NOT h.tombstone AND h.source_id = :source AND h.external_id = :id";
        List<Row> rows = jdbc.query(sql, new MapSqlParameterSource("source", sourceId).addValue("id", externalId),
                this::row);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    @Override
    public List<Row> versions(String sourceId, String externalId, long fromSeq, long toSeq, int limit) {
        MapSqlParameterSource params = new MapSqlParameterSource("source", sourceId)
                .addValue("id", externalId)
                .addValue("from", fromSeq)
                .addValue("to", toSeq)
                .addValue("limit", Math.max(1, limit));
        List<Row> out = new ArrayList<>(jdbc.query(VERSION
                + " WHERE v.source_id = :source AND v.external_id = :id AND v.seq <= :from AND NOT v.tombstone"
                + " ORDER BY v.seq DESC LIMIT 1", params, this::row));
        out.addAll(jdbc.query(VERSION
                + " WHERE v.source_id = :source AND v.external_id = :id AND v.seq > :from AND v.seq <= :to"
                + " AND NOT v.tombstone ORDER BY v.seq LIMIT :limit", params, this::row));
        return out;
    }

    @Override
    public List<Row> entered(List<String> sources, List<String> kinds, Geometry area, long fromSeq, long toSeq,
            int limit) {
        if (sources.isEmpty() || kinds.isEmpty()) {
            return List.of();
        }
        String sql = "SELECT * FROM (SELECT DISTINCT ON (v.source_id, v.external_id) v.source_id, v.external_id, v.kind,"
                + " v.seq, v.observed_at, v.payload::text AS payload, ST_AsBinary(v.geom) AS geom,"
                + " CAST(NULL AS double precision) AS km"
                + " FROM record_version v LEFT JOIN record_version p ON p.seq = v.prev_seq"
                + " WHERE v.seq > :from AND v.seq <= :to AND v.source_id = ANY(CAST(:sources AS text[]))"
                + " AND v.kind = ANY(CAST(:kinds AS text[])) AND NOT v.tombstone AND v.geom IS NOT NULL"
                + " AND ST_Intersects(v.geom, ST_GeomFromWKB(:g, 4326))"
                + " AND (p.seq IS NULL OR p.tombstone OR p.geom IS NULL OR NOT ST_Intersects(p.geom, ST_GeomFromWKB(:g, 4326)))"
                + " ORDER BY v.source_id, v.external_id, v.seq) e ORDER BY e.observed_at, e.seq LIMIT :limit";
        return jdbc.query(sql, new MapSqlParameterSource("from", fromSeq)
                .addValue("to", toSeq)
                .addValue("sources", PgArrays.text(sources))
                .addValue("kinds", PgArrays.text(kinds))
                .addValue("g", wkb(area))
                .addValue("limit", Math.max(1, limit)), this::row);
    }

    private static byte[] wkb(Geometry geometry) {
        return new WKBWriter().write(geometry);
    }

    private Row row(ResultSet rs, int n) throws SQLException {
        Timestamp observed = rs.getTimestamp("observed_at");
        String text = rs.getString("payload");
        Map<String, Object> payload = text == null ? Map.of() : json.readValue(text, MAP);
        byte[] bytes = rs.getBytes("geom");
        Geometry geometry = null;
        if (bytes != null) {
            try {
                geometry = new WKBReader(Geo.factory()).read(bytes);
            } catch (ParseException e) {
                throw new SQLException("Unreadable geometry of " + rs.getString("external_id"), e);
            }
        }
        double km = rs.getDouble("km");
        Double distance = rs.wasNull() ? null : km;
        return new Row(rs.getString("source_id"), rs.getString("external_id"), rs.getString("kind"), rs.getLong("seq"),
                observed == null ? null : observed.toInstant(), payload, geometry, distance);
    }
}
