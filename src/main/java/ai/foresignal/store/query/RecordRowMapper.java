package ai.foresignal.store.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.jdbc.core.RowMapper;

/**
 * Reads {@link RecordRow}s by column label. Every read query selects the full label set of
 * {@link #LABELS}, with typed {@code NULL}s for what it does not have, so one mapper serves them all.
 */
public final class RecordRowMapper implements RowMapper<RecordRow> {

    /** The shared instance. */
    public static final RecordRowMapper INSTANCE = new RecordRowMapper();

    /** Every label with the SQL type of its {@code NULL} placeholder, in select order. */
    public static final Map<String, String> LABELS = labels();

    private RecordRowMapper() {}

    private static Map<String, String> labels() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("source_id", "text");
        m.put("external_id", "text");
        m.put("kind", "text");
        m.put("version", "integer");
        m.put("seq", "bigint");
        m.put("observed_at", "timestamptz");
        m.put("persisted_observed_at", "timestamptz");
        m.put("event_time", "timestamptz");
        m.put("valid_from", "timestamptz");
        m.put("valid_until", "timestamptz");
        m.put("tombstone", "boolean");
        m.put("title", "text");
        m.put("geom_out", "text");
        m.put("payload_out", "text");
        m.put("title_payload", "text");
        m.put("quality_out", "text");
        m.put("live", "boolean");
        m.put("qa_flags", "integer");
        m.put("dist_m", "double precision");
        m.put("observer_out", "text");
        m.put("ingested_at", "timestamptz");
        m.put("received_at", "timestamptz");
        m.put("persist_reason", "smallint");
        m.put("prev_seq", "bigint");
        m.put("history_tier", "smallint");
        m.put("batch_id", "bigint");
        m.put("bx1", "double precision");
        m.put("by1", "double precision");
        m.put("bx2", "double precision");
        m.put("by2", "double precision");
        return java.util.Collections.unmodifiableMap(m);
    }

    @Override
    public RecordRow mapRow(ResultSet rs, int rowNum) throws SQLException {
        Double bx1 = dbl(rs, "bx1");
        double[] bbox = bx1 == null ? null : new double[] {bx1, dbl(rs, "by1"), dbl(rs, "bx2"), dbl(rs, "by2")};
        return new RecordRow(
                rs.getString("source_id"),
                rs.getString("external_id"),
                rs.getString("kind"),
                rs.getInt("version"),
                rs.getLong("seq"),
                instant(rs, "observed_at"),
                instant(rs, "persisted_observed_at"),
                instant(rs, "event_time"),
                instant(rs, "valid_from"),
                instant(rs, "valid_until"),
                rs.getBoolean("tombstone"),
                rs.getString("title"),
                rs.getString("geom_out"),
                rs.getString("payload_out"),
                rs.getString("title_payload"),
                rs.getString("quality_out"),
                bool(rs, "live"),
                rs.getInt("qa_flags"),
                dbl(rs, "dist_m"),
                rs.getString("observer_out"),
                instant(rs, "ingested_at"),
                instant(rs, "received_at"),
                shrt(rs, "persist_reason"),
                lng(rs, "prev_seq"),
                shrt(rs, "history_tier"),
                lng(rs, "batch_id"),
                bbox);
    }

    static Instant instant(ResultSet rs, String label) throws SQLException {
        OffsetDateTime value = rs.getObject(label, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }

    private static Double dbl(ResultSet rs, String label) throws SQLException {
        double value = rs.getDouble(label);
        return rs.wasNull() ? null : value;
    }

    private static Long lng(ResultSet rs, String label) throws SQLException {
        long value = rs.getLong(label);
        return rs.wasNull() ? null : value;
    }

    private static Short shrt(ResultSet rs, String label) throws SQLException {
        short value = rs.getShort(label);
        return rs.wasNull() ? null : value;
    }

    private static Boolean bool(ResultSet rs, String label) throws SQLException {
        boolean value = rs.getBoolean(label);
        return rs.wasNull() ? null : value;
    }
}
