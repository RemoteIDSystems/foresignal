package ai.foresignal.collect;

import ai.foresignal.ingest.FetchLogEntry;
import ai.foresignal.store.CoverageSample;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Writes the {@code fetch_log} rows of fetches that ingested nothing: NOT_MODIFIED, empty OK,
 * FAILED, REFUSED and DISCARDED.
 *
 * <p>A fetch that returned records gets its row from the ingest writer instead, in the same
 * transaction as its versions (Core-Data-Model §6.2 step 8), so its counts and seq range are exact.
 * {@code fetch_log} is not covered by invariant I1, so these rows written outside the writer do not
 * conflict with the single writer. {@code ingested_at} stays null, which is how a reader tells the
 * two kinds of row apart.
 */
@Component
public class FetchLogWriter {

    private static final String INSERT =
            """
            INSERT INTO fetch_log (source_id, trigger, status, started_at, received_at, ingested_at, http_status,
                                   request_count, url_redacted, etag, bytes, body_sha256, adapter_version, licence,
                                   principal, records_seen, rejected, late, skipped, versions_written, live_updates,
                                   touches, from_seq, to_seq, coverage, error)
            VALUES (?, ?, ?, ?, ?, NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0, 0, 0, 0, 0, NULL, NULL, ?::jsonb, ?)
            RETURNING id
            """;

    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the writer. */
    public FetchLogWriter(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /**
     * Inserts one row.
     *
     * @param entry the fetch's provenance and status; {@code trigger}, {@code status} and
     *     {@code startedAt} must be set
     * @param recordsSeen records the adapter returned (non-zero only for DISCARDED)
     * @return the row's id
     */
    public long write(FetchLogEntry entry, int recordsSeen) {
        if (entry.trigger() == null || entry.status() == null || entry.startedAt() == null) {
            throw new IllegalArgumentException("A fetch_log row needs a trigger, a status and a start time");
        }
        CoverageSample coverage = CoverageSample.from(entry.coverage());
        String coverageJson = coverage == null ? null : json.writeValueAsString(coverage);
        List<Long> ids =
                jdbc.query(
                        con -> {
                            PreparedStatement ps = con.prepareStatement(INSERT);
                            ps.setString(1, entry.sourceId());
                            ps.setString(2, entry.trigger().name());
                            ps.setString(3, entry.status().name());
                            setInstant(ps, 4, entry.startedAt());
                            setInstant(ps, 5, entry.receivedAt());
                            setInteger(ps, 6, entry.httpStatus());
                            ps.setInt(7, entry.requestCount());
                            setString(ps, 8, entry.urlRedacted());
                            setString(ps, 9, entry.etag());
                            if (entry.bytes() == null) {
                                ps.setNull(10, Types.BIGINT);
                            } else {
                                ps.setLong(10, entry.bytes());
                            }
                            setString(ps, 11, entry.bodySha256());
                            setString(ps, 12, entry.adapterVersion());
                            setString(ps, 13, entry.licence());
                            setString(ps, 14, entry.principal());
                            ps.setInt(15, Math.max(0, recordsSeen));
                            setString(ps, 16, coverageJson);
                            setString(ps, 17, entry.error());
                            return ps;
                        },
                        (rs, row) -> rs.getLong(1));
        return ids.get(0);
    }

    private static void setInstant(PreparedStatement ps, int index, Instant value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.TIMESTAMP_WITH_TIMEZONE);
        } else {
            ps.setObject(index, OffsetDateTime.ofInstant(value, ZoneOffset.UTC));
        }
    }

    private static void setInteger(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private static void setString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }
}
