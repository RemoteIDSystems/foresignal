package ai.foresignal.inbound.edp.binding;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads the stored versions of EDP items for the binding router, its backfills and previews (prompt 138). Reads only
 * {@code record_version}; heartbeat re-persists and tombstones are skipped.
 */
@Component
public class BindingRecords {

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private static final String COLUMNS = "SELECT seq, source_id, external_id, kind, version, observed_at, event_time,"
            + " ingested_at, ST_X(ST_PointOnSurface(geom)) AS lon, ST_Y(ST_PointOnSurface(geom)) AS lat,"
            + " payload::text AS payload FROM record_version";

    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the reader. */
    public BindingRecords(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /** The versions of {@code sources} with {@code from < seq <= to}, in seq order. */
    public List<BoundItem> versions(Set<String> sources, long from, long to) {
        if (sources.isEmpty() || from >= to) {
            return List.of();
        }
        return jdbc.query(COLUMNS + " WHERE seq > ? AND seq <= ? AND source_id = ANY(?) AND tombstone = false"
                + " AND persist_reason <> 8 ORDER BY seq", (rs, i) -> item(rs), from, to,
                sources.toArray(String[]::new));
    }

    /**
     * The newest versions of one stream at or below {@code maxSeq}, returned oldest first.
     *
     * @param sourceId the provider
     * @param kind the stream's kind
     * @param streamId the stream
     * @param limit the most versions
     * @param maxSeq the highest seq read
     */
    public List<BoundItem> recent(String sourceId, String kind, String streamId, int limit, long maxSeq) {
        List<BoundItem> out = new ArrayList<>(jdbc.query(COLUMNS + " WHERE source_id = ? AND kind = ?"
                + " AND external_id LIKE ? ESCAPE '\\' AND seq <= ? AND tombstone = false AND persist_reason <> 8"
                + " ORDER BY seq DESC LIMIT ?", (rs, i) -> item(rs), sourceId, kind, like(streamId) + ":%", maxSeq,
                limit));
        Collections.reverse(out);
        return out;
    }

    private BoundItem item(java.sql.ResultSet rs) throws java.sql.SQLException {
        String text = rs.getString("payload");
        Map<String, Object> payload = text == null ? Map.of() : json.readValue(text, MAP);
        return new BoundItem(rs.getLong("seq"), rs.getString("source_id"), rs.getString("external_id"),
                rs.getString("kind"), (Integer) rs.getObject("version"),
                instant(rs.getObject("observed_at", OffsetDateTime.class)),
                instant(rs.getObject("event_time", OffsetDateTime.class)),
                instant(rs.getObject("ingested_at", OffsetDateTime.class)), (Double) rs.getObject("lon"),
                (Double) rs.getObject("lat"), payload);
    }

    private static Instant instant(OffsetDateTime t) {
        return t == null ? null : t.toInstant();
    }

    /** Escapes {@code %}, {@code _} and {@code \} for a LIKE pattern. */
    static String like(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
