package ai.foresignal.derive;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link DerivedRecords} over {@code record_head ⋈ record_live} (prompt 052, invariant HIST-2). The
 * current payload is {@code jsonb_strip_nulls(h.payload || l.kinematic)}, as every current read uses.
 */
@Component
public class JdbcDerivedRecords implements DerivedRecords {

    private static final String SELECT = "SELECT h.source_id, h.external_id, h.kind, h.seq, h.version, l.observed_at,"
            + " h.tombstone, jsonb_strip_nulls(h.payload || l.kinematic)::text AS payload, ST_AsBinary(l.geom) AS geom"
            + " FROM record_head h JOIN record_live l USING (source_id, external_id)";

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the reader. */
    public JdbcDerivedRecords(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    @Override
    public List<HeadSnapshot> heads(String sourceId, String kind) {
        if (kind == null) {
            return jdbc.query(SELECT + " WHERE h.source_id = ? ORDER BY h.external_id", this::row, sourceId);
        }
        return jdbc.query(SELECT + " WHERE h.source_id = ? AND h.kind = ? ORDER BY h.external_id", this::row,
                sourceId, kind);
    }

    @Override
    public List<HeadSnapshot> current(Collection<String> sources, Collection<String> kinds, int limit) {
        if (sources == null || sources.isEmpty()) {
            return List.of();
        }
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE NOT h.tombstone AND h.source_id = ANY (?)");
        args.add(sources.toArray(String[]::new));
        if (kinds != null && !kinds.isEmpty()) {
            sql.append(" AND h.kind = ANY (?)");
            args.add(kinds.toArray(String[]::new));
        }
        sql.append(" ORDER BY h.source_id, h.external_id LIMIT ?");
        args.add(Math.max(1, limit));
        return jdbc.query(con -> {
            var ps = con.prepareStatement(sql.toString());
            int i = 1;
            for (Object arg : args) {
                if (arg instanceof String[] array) {
                    ps.setArray(i++, con.createArrayOf("text", array));
                } else {
                    ps.setObject(i++, arg);
                }
            }
            return ps;
        }, this::row);
    }

    @Override
    public List<HeadSnapshot> current(Collection<String> sources, Collection<String> kinds,
            org.locationtech.jts.geom.Envelope bbox, int limit) {
        if (sources == null || sources.isEmpty()) {
            return List.of();
        }
        if (bbox == null || bbox.isNull()) {
            return current(sources, kinds, limit);
        }
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT)
                .append(" WHERE NOT h.tombstone AND h.source_id = ANY (?)")
                .append(" AND l.geom && ST_MakeEnvelope(?, ?, ?, ?, 4326)");
        args.add(sources.toArray(String[]::new));
        args.add(bbox.getMinX());
        args.add(bbox.getMinY());
        args.add(bbox.getMaxX());
        args.add(bbox.getMaxY());
        if (kinds != null && !kinds.isEmpty()) {
            sql.append(" AND h.kind = ANY (?)");
            args.add(kinds.toArray(String[]::new));
        }
        sql.append(" ORDER BY h.source_id, h.external_id LIMIT ?");
        args.add(Math.max(1, limit));
        return jdbc.query(con -> {
            var ps = con.prepareStatement(sql.toString());
            int i = 1;
            for (Object arg : args) {
                if (arg instanceof String[] array) {
                    ps.setArray(i++, con.createArrayOf("text", array));
                } else {
                    ps.setObject(i++, arg);
                }
            }
            return ps;
        }, this::row);
    }

    @Override
    public Optional<HeadSnapshot> get(String sourceId, String externalId) {
        List<HeadSnapshot> rows = jdbc.query(SELECT + " WHERE h.source_id = ? AND h.external_id = ?", this::row,
                sourceId, externalId);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    private HeadSnapshot row(ResultSet rs, int n) throws SQLException {
        Timestamp observed = rs.getTimestamp("observed_at");
        Instant observedAt = observed == null ? null : observed.toInstant();
        String text = rs.getString("payload");
        Map<String, Object> payload = text == null ? Map.of() : json.readValue(text, MAP);
        byte[] wkb = rs.getBytes("geom");
        Geometry geometry = null;
        if (wkb != null) {
            try {
                geometry = new WKBReader(ai.foresignal.source.spi.Geo.factory()).read(wkb);
            } catch (ParseException e) {
                throw new SQLException("Unreadable geometry of " + rs.getString("external_id"), e);
            }
        }
        return new HeadSnapshot(rs.getString("source_id"), rs.getString("external_id"), rs.getString("kind"),
                rs.getLong("seq"), rs.getInt("version"), observedAt, rs.getBoolean("tombstone"), payload, geometry);
    }
}
