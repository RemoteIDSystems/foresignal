package ai.foresignal.trigger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import org.springframework.jdbc.core.RowMapper;

/**
 * One version a rule statement selected ({@link RuleSqlBuilder#matches}).
 *
 * @param seq the version's seq
 * @param prevSeq the predecessor's seq, or null
 * @param sourceId the source
 * @param externalId the entity
 * @param kind the kind
 * @param version the per-entity version
 * @param persistReason the stored persist reason code
 * @param observedAt when it was observed
 * @param eventTime the event time, or null
 * @param validFrom the start of validity, or null
 * @param validUntil the end of validity, or null
 * @param tombstone true for a tombstone
 * @param geometry the geometry as GeoJSON text, or null
 * @param payload the payload as JSON text
 * @param edge {@code match}, {@code enter} or {@code exit}
 */
public record MatchRow(
        long seq,
        Long prevSeq,
        String sourceId,
        String externalId,
        String kind,
        int version,
        short persistReason,
        Instant observedAt,
        Instant eventTime,
        Instant validFrom,
        Instant validUntil,
        boolean tombstone,
        String geometry,
        String payload,
        String edge) {

    /** Reads the columns of {@link RuleSqlBuilder#matches}. */
    public static final RowMapper<MatchRow> MAPPER = MatchRow::read;

    /** The entity key {@code source \u0000 externalId}, unique per entity. */
    public String entityKey() {
        return sourceId + '\u0000' + externalId;
    }

    private static MatchRow read(ResultSet rs, int row) throws SQLException {
        long prev = rs.getLong("prev_seq");
        Long prevSeq = rs.wasNull() ? null : prev;
        return new MatchRow(
                rs.getLong("seq"),
                prevSeq,
                rs.getString("source_id"),
                rs.getString("external_id"),
                rs.getString("kind"),
                rs.getInt("version"),
                rs.getShort("persist_reason"),
                instant(rs, "observed_at"),
                instant(rs, "event_time"),
                instant(rs, "valid_from"),
                instant(rs, "valid_until"),
                rs.getBoolean("tombstone"),
                rs.getString("geometry"),
                rs.getString("payload"),
                rs.getString("edge"));
    }

    /** The {@code timestamptz} column {@code label} as an instant, or null. */
    static Instant instant(ResultSet rs, String label) throws SQLException {
        OffsetDateTime value = rs.getObject(label, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }
}
