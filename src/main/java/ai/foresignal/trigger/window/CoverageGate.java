package ai.foresignal.trigger.window;

import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.SourceHealthView;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.store.CoverageSample;
import ai.foresignal.store.SourceHealthEntity;
import ai.foresignal.store.SourceHealthRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * The coverage gate for absence and downward-anomaly evaluation (prompt 100). It returns {@code ok}
 * only when every involved source is enabled and was covered for the whole window; otherwise
 * {@code unknown} with a reason. Absence of evidence is not evidence of absence.
 */
@Service
public class CoverageGate {

    /** The reason prefix the health coverage rule uses. */
    public static final String COVERAGE_REASON = "coverage dropped";

    /** The maximum FAILED share a stream source may have in the window. */
    public static final double STREAM_FAILURE_MAX = 0.10;

    private final SourceRegistry registry;
    private final HealthService health;
    private final SourceHealthRepository healthRows;
    private final JdbcTemplate jdbc;

    /** Creates the gate. */
    public CoverageGate(SourceRegistry registry, HealthService health, SourceHealthRepository healthRows,
            JdbcTemplate jdbc) {
        this.registry = registry;
        this.health = health;
        this.healthRows = healthRows;
        this.jdbc = jdbc;
    }

    /**
     * The gate result.
     *
     * @param ok true when every source is covered
     * @param reason why not, when not ok
     */
    public record Gate(boolean ok, String reason) {

        /** The covered result. */
        public static Gate covered() {
            return new Gate(true, null);
        }

        /** An uncovered result. */
        public static Gate unknown(String reason) {
            return new Gate(false, reason);
        }
    }

    /**
     * Evaluates the gate over {@code [from, to)}.
     *
     * @param sources the input sources
     * @param from the window start
     * @param to the window end
     * @param window the window length (for the staleness bound)
     * @param coverageMin the minimum OK/NOT_MODIFIED share
     */
    public Gate evaluate(Iterable<String> sources, Instant from, Instant to, Duration window, double coverageMin) {
        for (String sourceId : sources) {
            Gate gate = source(sourceId, from, to, window, coverageMin);
            if (!gate.ok()) {
                return gate;
            }
        }
        return Gate.covered();
    }

    private Gate source(String sourceId, Instant from, Instant to, Duration window, double coverageMin) {
        Boolean enabled = enabled(sourceId);
        HealthStatus status;
        String reason;
        Instant lastDataAt;
        try {
            SourceHealthView view = health.health(sourceId);
            status = view == null || view.status() == null ? HealthStatus.IDLE : view.status();
            reason = view == null ? null : view.statusReason();
            lastDataAt = view == null ? null : view.lastDataAt();
        } catch (RuntimeException e) {
            status = HealthStatus.IDLE;
            reason = null;
            lastDataAt = null;
        }
        Duration interval = interval(sourceId);
        boolean stream = stream(sourceId);
        long[] counts = enabled != null && enabled ? fetchCounts(sourceId, from, to) : new long[] {0, 0, 0};
        boolean coverageDrop = enabled != null && enabled && coverageDropped(sourceId);
        return decide(sourceId, enabled, status, reason, lastDataAt, to, window, interval, counts[0], counts[1],
                counts[2], stream, coverageDrop, coverageMin);
    }

    /**
     * The pure gate decision, so the health × coverage × window table is directly testable.
     *
     * @param enabled whether the source is installed and enabled; null when not installed
     * @param lastDataAt the last success that returned records, or null
     * @param to the window end
     * @param window the window length
     * @param interval the source's poll interval
     * @param total fetch rows in the window
     * @param ok OK or NOT_MODIFIED fetch rows in the window
     * @param failed FAILED fetch rows in the window
     * @param stream true for a streaming source
     * @param coverageDrop true when the newest coverage is at or below half the ring median
     * @param coverageMin the minimum OK/NOT_MODIFIED share
     */
    public static Gate decide(String sourceId, Boolean enabled, HealthStatus status, String reason, Instant lastDataAt,
            Instant to, Duration window, Duration interval, long total, long ok, long failed, boolean stream,
            boolean coverageDrop, double coverageMin) {
        if (enabled == null) {
            return Gate.unknown("source " + sourceId + " is not installed");
        }
        if (!enabled) {
            return Gate.unknown("source " + sourceId + " is disabled");
        }
        if (status == HealthStatus.FAILING) {
            return Gate.unknown("source " + sourceId + " is failing");
        }
        if (status == HealthStatus.DEGRADED && reason != null
                && (reason.startsWith(COVERAGE_REASON) || reason.toLowerCase().contains("format"))) {
            return Gate.unknown("source " + sourceId + " is degraded: " + reason);
        }
        Duration allowed = window.compareTo(interval.multipliedBy(2)) >= 0 ? window : interval.multipliedBy(2);
        if (lastDataAt == null || lastDataAt.isBefore(to.minus(allowed))) {
            return Gate.unknown("source " + sourceId + " has no data within " + allowed);
        }
        if (total == 0) {
            return Gate.unknown("source " + sourceId + " had no fetches in the window");
        }
        if (stream) {
            if ((double) failed / total > STREAM_FAILURE_MAX) {
                return Gate.unknown("source " + sourceId + " disconnected for more than 10% of the window");
            }
        } else if ((double) ok / total < coverageMin) {
            return Gate.unknown("source " + sourceId + " fetch coverage " + (ok * 100 / total) + "% below "
                    + Math.round(coverageMin * 100) + "%");
        }
        if (coverageDrop) {
            return Gate.unknown("source " + sourceId + " coverage dropped");
        }
        return Gate.covered();
    }

    private Boolean enabled(String sourceId) {
        List<Boolean> rows = jdbc.query("SELECT enabled FROM source_settings WHERE source_id = ?",
                (rs, n) -> rs.getBoolean(1), sourceId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private Duration interval(String sourceId) {
        try {
            return Optional.ofNullable(registry.get(sourceId).descriptor().defaultInterval())
                    .orElse(Duration.ofHours(1));
        } catch (RuntimeException e) {
            return Duration.ofHours(1);
        }
    }

    private boolean stream(String sourceId) {
        try {
            return registry.get(sourceId).descriptor().mode() == Mode.STREAMING;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** {@code [total, ok, failed]} fetch_log rows in the window. */
    private long[] fetchCounts(String sourceId, Instant from, Instant to) {
        return jdbc.queryForObject(
                "SELECT count(*) FILTER (WHERE status IN ('OK','NOT_MODIFIED')),"
                        + " count(*) FILTER (WHERE status = 'FAILED'), count(*)"
                        + " FROM fetch_log WHERE source_id = ? AND started_at >= ? AND started_at < ?",
                (rs, n) -> new long[] {rs.getLong(3), rs.getLong(1), rs.getLong(2)},
                sourceId, java.sql.Timestamp.from(from), java.sql.Timestamp.from(to));
    }

    /** True when the newest coverage is at or below half the median of the earlier ring entries. */
    private boolean coverageDropped(String sourceId) {
        SourceHealthEntity row = healthRows.findById(sourceId).orElse(null);
        if (row == null) {
            return false;
        }
        List<CoverageSample> ring = row.getRecentCoverage();
        if (ring.size() < 3) {
            return false;
        }
        CoverageSample newest = ring.get(ring.size() - 1);
        List<Integer> earlier = receivers(ring, ring.size() - 1);
        if (earlier.isEmpty() || newest.receivers() == null) {
            return false;
        }
        earlier.sort(Integer::compareTo);
        int median = earlier.get(earlier.size() / 2);
        return median > 0 && newest.receivers() <= median / 2;
    }

    /**
     * The per-bucket coverage ratio for the anomaly baseline (prompt 100 §4): the minimum over the
     * involved sources of the OK (or, for a stream, non-failed) fetch share multiplied by
     * {@code min(1, receivers ÷ median receivers of the last 20)} when coverage is reported. A
     * disabled or not-installed source yields 0.
     */
    public double coverageRatio(Iterable<String> sources, Instant from, Instant to) {
        double min = 1.0;
        boolean any = false;
        for (String sourceId : sources) {
            any = true;
            Boolean enabled = enabled(sourceId);
            if (enabled == null || !enabled) {
                return 0.0;
            }
            long[] counts = fetchCounts(sourceId, from, to);
            long total = counts[0];
            long ok = counts[1];
            long failed = counts[2];
            double share = total == 0 ? 0.0
                    : stream(sourceId) ? 1.0 - (double) failed / total : (double) ok / total;
            min = Math.min(min, Math.max(0.0, Math.min(1.0, share * receiversFactor(sourceId))));
        }
        return any ? min : 0.0;
    }

    /** {@code min(1, newest receivers ÷ median receivers of the earlier ring)}, or 1 when unreported. */
    private double receiversFactor(String sourceId) {
        SourceHealthEntity row = healthRows.findById(sourceId).orElse(null);
        if (row == null) {
            return 1.0;
        }
        List<CoverageSample> ring = row.getRecentCoverage();
        if (ring.isEmpty()) {
            return 1.0;
        }
        CoverageSample newest = ring.get(ring.size() - 1);
        if (newest.receivers() == null) {
            return 1.0;
        }
        List<Integer> earlier = receivers(ring, ring.size() - 1);
        if (earlier.isEmpty()) {
            return 1.0;
        }
        earlier.sort(Integer::compareTo);
        int median = earlier.get(earlier.size() / 2);
        if (median <= 0) {
            return 1.0;
        }
        return Math.min(1.0, (double) newest.receivers() / median);
    }

    /** The reported receiver counts of the ring entries before index {@code upTo}. */
    private static List<Integer> receivers(List<CoverageSample> ring, int upTo) {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < Math.min(upTo, ring.size()); i++) {
            Integer receivers = ring.get(i).receivers();
            if (receivers != null) {
                out.add(receivers);
            }
        }
        return out;
    }
}
