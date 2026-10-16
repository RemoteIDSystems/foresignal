package ai.foresignal.trigger.window;

import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.SourceHealthView;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The coverage gate for absence and downward-anomaly evaluation (prompt 100). It returns {@code ok}
 * only when every involved source is enabled and was covered for the whole window; otherwise
 * {@code unknown} with a reason. Absence of evidence is not evidence of absence.
 *
 * <p><strong>Involved sources.</strong> The rule's sources; for a rule that names only kinds, every
 * source whose descriptor declares one of those kinds (enabled ones for a live window, ones that were
 * fetching around the bucket for a past one). A rule with no involved source is never covered.
 *
 * <p><strong>Two modes.</strong> {@link #evaluate} judges the current window (ending now) from the
 * current settings and health plus {@code fetch_log}. {@link #evaluateAsOf} judges a past bucket from
 * time-stamped history only (invariant INT-2, no look-ahead): {@code fetch_log} rows started before
 * the bucket end stand in for health, freshness and coverage as they were then; current health,
 * settings and the {@code source_health} rings are never read for a past bucket.
 *
 * <p><strong>Freshness.</strong> "Last data" counts the last successful contact, a 304 included: a
 * source that answers "not modified" for the whole window still covers it (prompt 100's "a 304
 * streak with steady coverage still counts as covered"), while a source with no successful contact is
 * stale.
 */
@Service
public class CoverageGate {

    /** The reason prefix the health coverage rule uses. */
    public static final String COVERAGE_REASON = "coverage dropped";

    /** The maximum FAILED share a stream source may have in the window. */
    public static final double STREAM_FAILURE_MAX = 0.10;

    /** How many consecutive failed fetches before the bucket end read as "failing" for a past bucket. */
    public static final int FAILING_STREAK = 3;

    /** How far back a past bucket's kind-only rule looks for sources that were fetching. */
    public static final Duration ACTIVE_LOOKBACK = Duration.ofDays(84);

    private final SourceRegistry registry;
    private final HealthService health;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the gate. */
    public CoverageGate(SourceRegistry registry, HealthService health, JdbcTemplate jdbc, JsonMapper json) {
        this.registry = registry;
        this.health = health;
        this.jdbc = jdbc;
        this.json = json;
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
     * Evaluates the gate over the current window {@code [from, to)} ending now, from current settings
     * and health plus {@code fetch_log}.
     *
     * @param sources the rule's sources (may be empty)
     * @param kinds the rule's kinds, resolved to sources when {@code sources} is empty
     * @param from the window start
     * @param to the window end (now)
     * @param window the window length (for the staleness bound)
     * @param coverageMin the minimum OK/NOT_MODIFIED share
     * @return ok, or unknown with the first reason found
     */
    public Gate evaluate(Set<String> sources, Set<String> kinds, Instant from, Instant to, Duration window,
            double coverageMin) {
        Set<String> involved = involvedSources(sources, kinds, from, to, false);
        if (involved.isEmpty()) {
            return Gate.unknown(noSourcesReason(sources, kinds));
        }
        for (String sourceId : involved) {
            Gate gate = liveSource(sourceId, from, to, window, coverageMin);
            if (!gate.ok()) {
                return gate;
            }
        }
        return Gate.covered();
    }

    /**
     * Evaluates the gate over a past bucket {@code [from, to)} from history only: nothing that was
     * recorded after {@code to} is read, and current health and settings are ignored.
     *
     * @param sources the rule's sources (may be empty)
     * @param kinds the rule's kinds, resolved to sources when {@code sources} is empty
     * @param from the bucket start
     * @param to the bucket end (the decision time)
     * @param window the bucket length (for the staleness bound)
     * @param coverageMin the minimum OK/NOT_MODIFIED share
     * @return ok, or unknown with the first reason found
     */
    public Gate evaluateAsOf(Set<String> sources, Set<String> kinds, Instant from, Instant to, Duration window,
            double coverageMin) {
        Set<String> involved = involvedSources(sources, kinds, from, to, true);
        if (involved.isEmpty()) {
            return Gate.unknown(noSourcesReason(sources, kinds));
        }
        for (String sourceId : involved) {
            Gate gate = historicalSource(sourceId, from, to, window, coverageMin);
            if (!gate.ok()) {
                return gate;
            }
        }
        return Gate.covered();
    }

    private static String noSourcesReason(Set<String> sources, Set<String> kinds) {
        if ((sources == null || sources.isEmpty()) && (kinds == null || kinds.isEmpty())) {
            return "the rule names no sources or kinds, so no coverage can be checked";
        }
        if (sources == null || sources.isEmpty()) {
            return "no enabled source declares the rule's kinds " + new TreeSet<>(kinds);
        }
        return "the rule's sources are unknown";
    }

    /**
     * The sources whose coverage decides the rule: its own, or for a kind-only rule every source that
     * declares one of the kinds and was enabled (live) or fetching around the bucket (historical).
     *
     * @param sources the rule's sources
     * @param kinds the rule's kinds
     * @param from the window start
     * @param to the window end
     * @param historical true for a past bucket
     * @return the involved source ids, sorted
     */
    public Set<String> involvedSources(Set<String> sources, Set<String> kinds, Instant from, Instant to,
            boolean historical) {
        Set<String> out = new TreeSet<>();
        if (sources != null && !sources.isEmpty()) {
            out.addAll(sources);
            return out;
        }
        if (kinds == null || kinds.isEmpty()) {
            return out;
        }
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (descriptor.kinds().stream().noneMatch(kinds::contains)) {
                continue;
            }
            boolean active = historical
                    ? fetchedBetween(descriptor.id(), to.minus(ACTIVE_LOOKBACK), to)
                    : Boolean.TRUE.equals(enabled(descriptor.id()));
            if (active) {
                out.add(descriptor.id());
            }
        }
        return out;
    }

    private Gate liveSource(String sourceId, Instant from, Instant to, Duration window, double coverageMin) {
        Boolean enabled = enabled(sourceId);
        HealthStatus status;
        String reason;
        Instant fresh;
        try {
            SourceHealthView view = health.health(sourceId);
            status = view == null || view.status() == null ? HealthStatus.IDLE : view.status();
            reason = view == null ? null : view.statusReason();
            fresh = latest(view == null ? null : view.lastDataAt(), view == null ? null : view.lastSuccessAt());
        } catch (RuntimeException e) {
            status = HealthStatus.IDLE;
            reason = null;
            fresh = null;
        }
        boolean running = enabled != null && enabled;
        long[] counts = running ? fetchCounts(sourceId, from, to) : new long[] {0, 0, 0};
        if (running) {
            fresh = latest(fresh, lastContact(sourceId, to));
        }
        boolean coverageDrop = running && coverageDroppedAsOf(sourceId, to);
        return decide(sourceId, enabled, status, reason, fresh, to, window, interval(sourceId), counts[0], counts[1],
                counts[2], stream(sourceId), coverageDrop, coverageMin);
    }

    private Gate historicalSource(String sourceId, Instant from, Instant to, Duration window, double coverageMin) {
        if (!registry.contains(sourceId)) {
            return Gate.unknown("source " + sourceId + " is not installed");
        }
        Instant firstFetch = firstFetch(sourceId);
        if (firstFetch == null || firstFetch.isAfter(from)) {
            return Gate.unknown("source " + sourceId + " was not running for the whole bucket");
        }
        long[] counts = fetchCounts(sourceId, from, to);
        HealthStatus status = failingAsOf(sourceId, to) ? HealthStatus.FAILING : HealthStatus.HEALTHY;
        String reason = formatFailureIn(sourceId, from, to) ? "format errors in the bucket" : null;
        if (reason != null) {
            status = HealthStatus.DEGRADED;
        }
        return decide(sourceId, true, status, reason, lastContact(sourceId, to), to, window, interval(sourceId),
                counts[0], counts[1], counts[2], stream(sourceId), coverageDroppedAsOf(sourceId, to), coverageMin);
    }

    /**
     * The pure gate decision, so the health × coverage × window table is directly testable.
     *
     * @param enabled whether the source is installed and enabled; null when not installed
     * @param lastDataAt the last successful contact (data or not-modified) before {@code to}, or null
     * @param to the window end
     * @param window the window length
     * @param interval the source's poll interval
     * @param total fetch rows in the window
     * @param ok OK or NOT_MODIFIED fetch rows in the window
     * @param failed FAILED fetch rows in the window
     * @param stream true for a streaming source
     * @param coverageDrop true when the newest coverage is at or below half the median of the last 20
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
                && (reason.startsWith(COVERAGE_REASON) || reason.toLowerCase(Locale.ROOT).contains("format"))) {
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

    /**
     * The per-bucket coverage ratio for the anomaly baseline (prompt 100 §4), from history up to the
     * bucket end: the minimum over the involved sources of the OK (or, for a stream, non-failed) fetch
     * share in the bucket times {@code min(1, receivers ÷ median receivers of the previous 20)} when
     * {@code fetch_log} reports coverage. No involved source, or one with no fetches, yields 0.
     */
    public double coverageRatioAsOf(Set<String> sources, Set<String> kinds, Instant from, Instant to) {
        Set<String> involved = involvedSources(sources, kinds, from, to, true);
        if (involved.isEmpty()) {
            return 0.0;
        }
        double min = 1.0;
        for (String sourceId : involved) {
            long[] counts = fetchCounts(sourceId, from, to);
            long total = counts[0];
            double share = total == 0 ? 0.0
                    : stream(sourceId) ? 1.0 - (double) counts[2] / total : (double) counts[1] / total;
            min = Math.min(min, Math.max(0.0, Math.min(1.0, share * receiversFactorAsOf(sourceId, to))));
        }
        return min;
    }

    /**
     * The {@code fetch_log} id range of each involved source within {@code [from, to)}, for evidence.
     *
     * @return source id → {@code {firstId, lastId, rows}}, sources without rows left out
     */
    public Map<String, long[]> fetchIdRanges(Set<String> sources, Set<String> kinds, Instant from, Instant to,
            boolean historical) {
        Map<String, long[]> out = new LinkedHashMap<>();
        for (String sourceId : involvedSources(sources, kinds, from, to, historical)) {
            jdbc.query("SELECT min(id), max(id), count(*) FROM fetch_log WHERE source_id = ? AND started_at >= ?"
                            + " AND started_at < ?",
                    rs -> {
                        if (rs.getLong(3) > 0) {
                            out.put(sourceId, new long[] {rs.getLong(1), rs.getLong(2), rs.getLong(3)});
                        }
                    },
                    sourceId, Timestamp.from(from), Timestamp.from(to));
        }
        return out;
    }

    /**
     * When the source first fetched (its first {@code fetch_log} row), or null when it never has. Used
     * for the absence warm-up: a source must have existed for one full window.
     */
    public Instant firstFetch(String sourceId) {
        List<Timestamp> rows = jdbc.queryForList("SELECT min(started_at) FROM fetch_log WHERE source_id = ?",
                Timestamp.class, sourceId);
        return rows.isEmpty() || rows.get(0) == null ? null : rows.get(0).toInstant();
    }

    // ------------------------------------------------------------------ history reads (≤ to)

    private Boolean enabled(String sourceId) {
        List<Boolean> rows = jdbc.query("SELECT enabled FROM source_settings WHERE source_id = ?",
                (rs, n) -> rs.getBoolean(1), sourceId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private boolean fetchedBetween(String sourceId, Instant from, Instant to) {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM (SELECT 1 FROM fetch_log WHERE source_id = ?"
                + " AND started_at >= ? AND started_at < ? LIMIT 1) x", Integer.class, sourceId,
                Timestamp.from(from), Timestamp.from(to));
        return n != null && n > 0;
    }

    /** {@code [total, ok, failed]} fetch_log rows in the window. */
    private long[] fetchCounts(String sourceId, Instant from, Instant to) {
        return jdbc.queryForObject(
                "SELECT count(*) FILTER (WHERE status IN ('OK','NOT_MODIFIED')),"
                        + " count(*) FILTER (WHERE status = 'FAILED'), count(*)"
                        + " FROM fetch_log WHERE source_id = ? AND started_at >= ? AND started_at < ?"
                        + " AND status NOT IN ('REFUSED','DISCARDED')",
                (rs, n) -> new long[] {rs.getLong(3), rs.getLong(1), rs.getLong(2)},
                sourceId, Timestamp.from(from), Timestamp.from(to));
    }

    /** The last OK or NOT_MODIFIED fetch started before {@code to}, or null. */
    private Instant lastContact(String sourceId, Instant to) {
        List<Timestamp> rows = jdbc.queryForList("SELECT max(started_at) FROM fetch_log WHERE source_id = ?"
                + " AND started_at < ? AND status IN ('OK','NOT_MODIFIED')", Timestamp.class, sourceId,
                Timestamp.from(to));
        return rows.isEmpty() || rows.get(0) == null ? null : rows.get(0).toInstant();
    }

    /** True when the last {@value #FAILING_STREAK} attempted fetches before {@code to} all failed. */
    private boolean failingAsOf(String sourceId, Instant to) {
        List<String> statuses = jdbc.queryForList("SELECT status FROM fetch_log WHERE source_id = ? AND started_at < ?"
                + " AND status NOT IN ('REFUSED','DISCARDED') ORDER BY started_at DESC, id DESC LIMIT ?",
                String.class, sourceId, Timestamp.from(to), FAILING_STREAK);
        return statuses.size() == FAILING_STREAK && statuses.stream().allMatch("FAILED"::equals);
    }

    /** True when a fetch in the window failed on the upstream's format (the 006 format rule). */
    private boolean formatFailureIn(String sourceId, Instant from, Instant to) {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM fetch_log WHERE source_id = ? AND started_at >= ?"
                + " AND started_at < ? AND status = 'FAILED' AND error ILIKE '%format%'", Integer.class, sourceId,
                Timestamp.from(from), Timestamp.from(to));
        return n != null && n > 0;
    }

    /**
     * The reported coverage of the last 21 fetches before {@code to}, newest first: {@code {receivers,
     * expected}} per row, null where not reported.
     */
    private List<Integer[]> coverageHistory(String sourceId, Instant to) {
        List<String> rows = jdbc.queryForList("SELECT coverage::text FROM fetch_log WHERE source_id = ?"
                + " AND started_at < ? AND coverage IS NOT NULL ORDER BY started_at DESC, id DESC LIMIT 21",
                String.class, sourceId, Timestamp.from(to));
        List<Integer[]> out = new ArrayList<>();
        for (String row : rows) {
            try {
                JsonNode node = json.readTree(row);
                out.add(new Integer[] {intOrNull(node, "receivers"), intOrNull(node, "expected")});
            } catch (RuntimeException e) {
                // An unreadable coverage row says nothing.
            }
        }
        return out;
    }

    private static Integer intOrNull(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() || !value.isNumber() ? null : value.asInt();
    }

    /**
     * True when the newest reported receivers or expected count before {@code to} is at least 50%
     * below the median of the previous 20 (the 006 rule, read from {@code fetch_log}).
     */
    boolean coverageDroppedAsOf(String sourceId, Instant to) {
        List<Integer[]> history = coverageHistory(sourceId, to);
        if (history.size() < 3) {
            return false;
        }
        for (int field = 0; field < 2; field++) {
            Integer newest = history.get(0)[field];
            Integer median = median(history, field);
            if (newest != null && median != null && median > 0 && newest <= median / 2.0) {
                return true;
            }
        }
        return false;
    }

    /** {@code min(1, newest receivers ÷ median receivers of the previous 20)} before {@code to}, or 1. */
    private double receiversFactorAsOf(String sourceId, Instant to) {
        List<Integer[]> history = coverageHistory(sourceId, to);
        if (history.isEmpty() || history.get(0)[0] == null) {
            return 1.0;
        }
        Integer median = median(history, 0);
        if (median == null || median <= 0) {
            return 1.0;
        }
        return Math.min(1.0, (double) history.get(0)[0] / median);
    }

    /** The median of {@code field} over the rows after the newest, or null when none reports it. */
    private static Integer median(List<Integer[]> history, int field) {
        List<Integer> earlier = new ArrayList<>();
        for (int i = 1; i < history.size(); i++) {
            Integer value = history.get(i)[field];
            if (value != null) {
                earlier.add(value);
            }
        }
        if (earlier.isEmpty()) {
            return null;
        }
        earlier.sort(Integer::compareTo);
        return earlier.get(earlier.size() / 2);
    }

    private static Instant latest(Instant a, Instant b) {
        if (a == null) {
            return b;
        }
        return b == null || a.isAfter(b) ? a : b;
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
}
