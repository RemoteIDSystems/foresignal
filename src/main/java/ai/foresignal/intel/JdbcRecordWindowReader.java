package ai.foresignal.intel;

import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.source.spi.Geo;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@link RecordWindowReader} over {@code record_version} (prompt 105 post-review): one bounded query per
 * source and kind, filtered by observation time and by the geometry's bounding box.
 */
@Component
public class JdbcRecordWindowReader implements RecordWindowReader {

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private static final String SQL = "SELECT source_id, external_id, kind, seq, version, observed_at,"
            + " ST_AsText(geom) AS wkt, payload::text AS payload FROM record_version"
            + " WHERE source_id = ? AND kind = ? AND tombstone = false AND observed_at >= ? AND observed_at <= ?"
            + " AND geom IS NOT NULL AND geom && ST_MakeEnvelope(?, ?, ?, ?, 4326)"
            + " ORDER BY seq DESC LIMIT ?";

    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the reader. */
    public JdbcRecordWindowReader(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    @Override
    public List<HeadSnapshot> versions(String sourceId, String kind, Instant from, Instant to, Envelope bbox,
            int limit) {
        if (bbox == null || bbox.isNull() || from == null || to == null || limit <= 0) {
            return List.of();
        }
        List<HeadSnapshot> out = new ArrayList<>();
        jdbc.query(SQL, rs -> {
            String text = rs.getString("payload");
            Map<String, Object> payload = text == null ? Map.of() : json.readValue(text, MAP);
            Instant observed = rs.getObject("observed_at", java.time.OffsetDateTime.class).toInstant();
            out.add(new HeadSnapshot(rs.getString("source_id"), rs.getString("external_id"), rs.getString("kind"),
                    rs.getLong("seq"), rs.getInt("version"), observed, false, payload, wkt(rs.getString("wkt"))));
        }, sourceId, kind, Timestamp.from(from), Timestamp.from(to), bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(),
                bbox.getMaxY(), limit);
        return out;
    }

    private static Geometry wkt(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            Geometry geometry = new WKTReader(Geo.factory()).read(text);
            geometry.setSRID(Geo.SRID);
            return geometry;
        } catch (Exception e) {
            return null;
        }
    }
}
