package ai.foresignal.retention;

import java.sql.Timestamp;
import java.time.Duration;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

/**
 * Steps 5 and 6 (Core-Data-Model §13): {@code fetch_log} rows that wrote no versions go after 7 days,
 * the others after {@code retention.provenance}; {@code seq_clock} minutes older than the largest
 * window plus a day go.
 */
@Component
public class ProvenanceStep implements RetentionStep {

    /** How long fetch rows without versions are kept. */
    static final Duration EMPTY_FETCH_KEEP = Duration.ofDays(7);

    private final RetentionResolver resolver;

    /** Creates the step. */
    public ProvenanceStep(RetentionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public String name() {
        return "provenance";
    }

    @Override
    public int order() {
        return 500;
    }

    @Override
    public void run(RetentionRun run) {
        long empty = run.jdbc().update(
                "DELETE FROM fetch_log WHERE versions_written = 0 AND started_at < :t",
                new MapSqlParameterSource("t", Timestamp.from(run.now().minus(EMPTY_FETCH_KEEP))));
        Duration largest = resolver.largestWindow();
        if (run.largestStandardWindow().compareTo(largest) > 0) {
            largest = run.largestStandardWindow();
        }
        // Provenance (prompt 109): a batch's license outlives no version it describes, so rows that
        // wrote versions are kept for max(retention.provenance, the largest version window).
        Duration keep = run.settings().provenance();
        if (largest.compareTo(keep) > 0) {
            keep = largest;
        }
        long versioned = run.jdbc().update(
                "DELETE FROM fetch_log WHERE versions_written > 0 AND started_at < :t",
                new MapSqlParameterSource("t", Timestamp.from(run.now().minus(keep))));
        run.deleted("fetch_log", empty + versioned);

        long minutes = run.jdbc().update(
                "DELETE FROM seq_clock WHERE minute < :t",
                new MapSqlParameterSource("t", Timestamp.from(run.now().minus(largest).minus(Duration.ofDays(1)))));
        run.deleted("seq_clock", minutes);
    }
}
