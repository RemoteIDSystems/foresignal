package ai.foresignal.testsupport.ingest;

import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Marks a source as covered for the coverage-gated window's tests (prompt 100).
 *
 * <p>{@code CoverageGate} returns {@code unknown} when {@code source_health.last_data_at} is null or
 * stale, when there are no {@code fetch_log} rows in the window, or when the OK/NOT_MODIFIED share
 * is below {@code coverageMin}. {@link TestRecords#ingestAt} writes a {@code fetch_log} row through
 * the writer but never touches {@code source_health.last_data_at} (only
 * {@code HealthTracker.recordSuccess} does, on the collection path), so a test that wants the gate
 * to pass must call {@link #markCovered}.
 */
public final class CoverageSupport {

    private final JdbcTemplate jdbc;

    /** Creates the helper. */
    public CoverageSupport(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Makes {@code sourceId} pass the coverage gate over {@code [from, to)}: writes a fresh
     * {@code source_health} row (data at {@code to}, a stable coverage ring) and OK/NOT_MODIFIED
     * {@code fetch_log} rows inside the window, so the OK share is 1.0 and the newest coverage is not
     * a drop.
     *
     * @param sourceId the source
     * @param from the window start (inclusive)
     * @param to the window end (exclusive)
     */
    public void markCovered(String sourceId, Instant from, Instant to) {
        Timestamp at = Timestamp.from(to);
        jdbc.update("INSERT INTO source_health (source_id, last_attempt_at, last_success_at, last_data_at,"
                        + " consecutive_failures, recent_record_counts, recent_coverage, adapter_state)"
                        + " VALUES (?, ?, ?, ?, 0, CAST(? AS jsonb), CAST(? AS jsonb), CAST('{}' AS jsonb))"
                        + " ON CONFLICT (source_id) DO UPDATE SET last_attempt_at = EXCLUDED.last_attempt_at,"
                        + " last_success_at = EXCLUDED.last_success_at, last_data_at = EXCLUDED.last_data_at,"
                        + " consecutive_failures = 0, recent_record_counts = EXCLUDED.recent_record_counts,"
                        + " recent_coverage = EXCLUDED.recent_coverage",
                sourceId, at, at, at, "[1,1,1,1]",
                "[{\"receivers\":5},{\"receivers\":5},{\"receivers\":5},{\"receivers\":5}]");
        fetch(sourceId, "OK", from);
        fetch(sourceId, "NOT_MODIFIED", to.minusSeconds(1));
    }

    private void fetch(String sourceId, String status, Instant startedAt) {
        jdbc.update("INSERT INTO fetch_log (source_id, trigger, status, started_at, received_at, request_count,"
                        + " records_seen, rejected, late, skipped, versions_written, live_updates, touches)"
                        + " VALUES (?, 'MANUAL', ?, ?, ?, 0, 0, 0, 0, 0, 0, 0, 0)",
                sourceId, status, Timestamp.from(startedAt), Timestamp.from(startedAt));
    }

    /** Registers the helper beside {@link TestIngestSources}. */
    @TestConfiguration
    public static class Config {

        /** The helper bean. */
        @Bean
        public CoverageSupport coverageSupport(JdbcTemplate jdbc) {
            return new CoverageSupport(jdbc);
        }
    }
}
