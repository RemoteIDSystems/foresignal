package ai.foresignal.backtest;

import ai.foresignal.ingest.SourceIngestProfiles;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.store.query.ReadQueryRunner;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * The stored facts the backtest caveats are built from (prompt 103 §2): coverage gaps from
 * {@code fetch_log}, SAMPLED or LATEST ingest policies, and entity-link changes in the range. Each
 * query runs in its own read-only transaction with the backtest statement timeout.
 */
@Component
public class BacktestCoverage {

    /** The statement timeout of every backtest query. */
    public static final Duration STATEMENT_TIMEOUT = Duration.ofSeconds(60);

    /** The most {@code fetch_log} rows read per source; beyond it the gap analysis uses the first rows. */
    public static final int MAX_RUNS_PER_SOURCE = 1_000_000;

    private final ReadQueryRunner reads;
    private final SourceIngestProfiles profiles;

    /** Creates the component. */
    public BacktestCoverage(ReadQueryRunner reads, SourceIngestProfiles profiles) {
        this.reads = reads;
        this.profiles = profiles;
    }

    /**
     * The largest fraction of {@code [from, to]} that coverage gaps cover for any one of
     * {@code sources}; 0 for an empty set.
     */
    public double gapFraction(Set<String> sources, Instant from, Instant to) {
        double worst = 0;
        for (String source : sources) {
            List<CoverageGaps.Run> runs = reads.read(STATEMENT_TIMEOUT, t -> t.getJdbcTemplate().query(
                    "SELECT started_at, status FROM fetch_log WHERE source_id = ? AND started_at >= ?"
                            + " AND started_at <= ? ORDER BY started_at LIMIT " + MAX_RUNS_PER_SOURCE,
                    (rs, n) -> new CoverageGaps.Run(rs.getTimestamp(1).toInstant(), rs.getString(2)),
                    source, Timestamp.from(from), Timestamp.from(to)));
            worst = Math.max(worst, CoverageGaps.fraction(CoverageGaps.gaps(runs, from, to), from, to));
        }
        return worst;
    }

    /** True when any of {@code sources} stores only some observations (SAMPLED or LATEST). */
    public boolean sampled(Set<String> sources) {
        for (String source : sources) {
            try {
                IngestPolicy.PolicyMode mode = profiles.get(source).policy().mode();
                if (mode == IngestPolicy.PolicyMode.SAMPLED || mode == IngestPolicy.PolicyMode.LATEST) {
                    return true;
                }
            } catch (RuntimeException e) {
                // An unregistered source has no policy to report.
            }
        }
        return false;
    }

    /**
     * True when an entity link (prompt 098) of {@code sources} (any source when empty) started or
     * ended inside {@code [from, to]}.
     */
    public boolean entityResolutionChanged(Set<String> sources, Instant from, Instant to) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT EXISTS (SELECT 1 FROM entity_link WHERE"
                + " ((valid_from >= ? AND valid_from <= ?) OR (valid_to >= ? AND valid_to <= ?))");
        params.add(Timestamp.from(from));
        params.add(Timestamp.from(to));
        params.add(Timestamp.from(from));
        params.add(Timestamp.from(to));
        if (!sources.isEmpty()) {
            sql.append(" AND source_id IN (").append(String.join(",", sources.stream().map(s -> "?").toList()))
                    .append(')');
            params.addAll(sources);
        }
        sql.append(')');
        Boolean changed = reads.read(STATEMENT_TIMEOUT,
                t -> t.getJdbcTemplate().queryForObject(sql.toString(), Boolean.class, params.toArray()));
        return Boolean.TRUE.equals(changed);
    }
}
