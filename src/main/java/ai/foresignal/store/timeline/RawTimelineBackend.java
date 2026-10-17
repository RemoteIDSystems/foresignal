package ai.foresignal.store.timeline;

import ai.foresignal.api.support.TimeField;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.retention.RetentionState;
import ai.foresignal.security.restrict.RestrictionClamp;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.history.AsOfResolver;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.SeqClockQuery;
import ai.foresignal.store.query.SqlParams;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.stereotype.Component;

/**
 * The timeline over raw {@code record_version} rows (prompt 037).
 *
 * <p>{@code date_bin(:bucket, <axis>, :from)} grouped per bucket (and per kind or source with
 * {@code GROUPING SETS}, so a bucket's total of distinct entities stays exact), served by the
 * {@code (source_id, observed_at)} index on the observed axis. {@code timeField=ingested} is
 * translated to seq bounds through {@code seq_clock} and binned by {@code ingested_at}. HEARTBEAT
 * and KEYFRAME versions are left out unless {@code includeKeyframes}, so the density reflects real
 * change. Alert counts per severity come from {@code alert.fired_at}. Each request runs with a
 * {@value #TIMEOUT_SECONDS} s statement timeout ({@code 503 QUERY_TIMEOUT}).
 */
@Component
public class RawTimelineBackend implements TimelineBackend {

    /** The statement timeout in seconds. */
    public static final int TIMEOUT_SECONDS = 10;

    /** The statement timeout. */
    public static final Duration TIMEOUT = Duration.ofSeconds(TIMEOUT_SECONDS);

    /** The severities counted by the alert overlay. */
    public static final List<String> SEVERITIES = List.of("info", "warning", "critical");

    private final ReadQueryRunner runner;
    private final SeqClockQuery seqClock;
    private final SourceRegistry registry;
    private final RetentionState retention;
    private final AsOfResolver asOf;
    private final Clock clock;

    /** Creates the backend. */
    public RawTimelineBackend(ReadQueryRunner runner, SeqClockQuery seqClock, SourceRegistry registry,
            RetentionState retention, AsOfResolver asOf, Clock clock) {
        this.runner = runner;
        this.seqClock = seqClock;
        this.registry = registry;
        this.retention = retention;
        this.asOf = asOf;
        this.clock = clock;
    }

    @Override
    public Timeline timeline(TimelineQuery q) {
        Instant now = clock.instant();
        Instant retainedFrom = null;
        boolean approximate = false;
        Set<HistoryTier> tiers = EnumSet.noneOf(HistoryTier.class);
        for (String id : q.sources()) {
            Duration window = retention.window(id);
            if (window != null) {
                Instant start = now.minus(window);
                if (retainedFrom == null || start.isAfter(retainedFrom)) {
                    retainedFrom = start;
                }
            }
            approximate |= asOf.sampled(id);
            tiers.add(registry.descriptor(id).historyTier());
        }
        long count = q.bucketCount();
        Map<Instant, Long> totals = new HashMap<>();
        Map<Instant, Map<String, Long>> groups = new HashMap<>();
        Map<Instant, long[]> alerts = new HashMap<>();
        boolean downsampled = runner.read(TIMEOUT, jdbc -> {
            long lo = seqClock.lastSeqBefore(jdbc, q.from());
            long hi = seqClock.lastSeqBefore(jdbc, q.to());
            if (!q.sources().isEmpty()) {
                SqlParams p = new SqlParams();
                jdbc.query(countSql(q, lo, hi, p), p.source(), rs -> {
                    Instant t = rs.getObject("t", OffsetDateTime.class).toInstant();
                    long n = rs.getLong("n");
                    if (rs.getInt("grp") == 1) {
                        totals.put(t, n);
                    } else {
                        String g = rs.getString("g");
                        groups.computeIfAbsent(t, k -> new TreeMap<>()).put(g == null ? "" : g, n);
                    }
                });
                if (q.includeAlerts()) {
                    SqlParams ap = new SqlParams();
                    jdbc.query(alertSql(q, ap), ap.source(), rs -> {
                        Instant t = rs.getObject("t", OffsetDateTime.class).toInstant();
                        int index = SEVERITIES.indexOf(rs.getString("severity"));
                        if (index >= 0) {
                            alerts.computeIfAbsent(t, k -> new long[3])[index] += rs.getLong("n");
                        }
                    });
                }
            }
            boolean any = false;
            for (HistoryTier tier : tiers) {
                any |= asOf.fidelity().downsampled(tier, lo, hi);
            }
            return any;
        });
        List<Timeline.Bucket> buckets = new ArrayList<>((int) count);
        List<Timeline.AlertBucket> alertBuckets = q.includeAlerts() ? new ArrayList<>((int) count) : null;
        long max = 0;
        for (long i = 0; i < count; i++) {
            Instant t = q.bucketStart(i);
            long total = totals.getOrDefault(t, 0L);
            max = Math.max(max, total);
            Map<String, Long> counts = "none".equals(q.groupBy()) ? Map.of() : groups.getOrDefault(t, Map.of());
            buckets.add(new Timeline.Bucket(t, total, counts));
            if (alertBuckets != null) {
                long[] a = alerts.getOrDefault(t, new long[3]);
                alertBuckets.add(new Timeline.AlertBucket(t, a[0], a[1], a[2]));
            }
        }
        return new Timeline(q.from(), q.to(), q.bucket(), q.timeField().wireValue(), q.measure(), q.groupBy(),
                List.copyOf(buckets), alertBuckets == null ? null : List.copyOf(alertBuckets), max,
                approximate || downsampled, retainedFrom, "raw");
    }

    /** The bucket counts: one total row per bucket ({@code grp = 1}) and, when grouped, one per group. */
    static String countSql(TimelineQuery q, long lo, long hi, SqlParams p) {
        String axis = switch (q.timeField()) {
            case OBSERVED -> "v.observed_at";
            case EFFECTIVE -> "COALESCE(v.event_time, v.observed_at)";
            case EVENT -> "v.event_time";
            case INGESTED -> "v.ingested_at";
        };
        List<String> where = new ArrayList<>();
        where.add("v.source_id = ANY(" + p.textArray(q.sources()) + ")");
        if (q.timeField() == TimeField.INGESTED) {
            where.add("v.seq > " + p.bigint(lo) + " AND v.seq <= " + p.bigint(hi));
        } else {
            where.add(axis + " >= " + p.instant(q.from()) + " AND " + axis + " < " + p.instant(q.to()));
        }
        if (!q.kinds().isEmpty()) {
            where.add("v.kind = ANY(" + p.textArray(q.kinds()) + ")");
        }
        if (!q.includeKeyframes()) {
            where.add("v.persist_reason NOT IN (" + PersistReason.HEARTBEAT.code() + ", " + PersistReason.KEYFRAME.code()
                    + ")");
        }
        if (q.bbox() != null) {
            where.add(RecordQueryService.bboxClause("v.geom", q.bbox(), p));
        }
        RestrictionClamp.sqlPredicate("v.source_id", "v.kind", "v.geom", "v.payload", q.restriction(), p)
                .ifPresent(where::add);
        String group = switch (q.groupBy()) {
            case "kind" -> "v.kind";
            case "source" -> "v.source_id";
            default -> "CAST(NULL AS text)";
        };
        String measure = "entities".equals(q.measure())
                ? "count(DISTINCT x.source_id || chr(31) || x.external_id)"
                : "count(*)";
        String inner = "SELECT date_bin(CAST(" + p.add(q.bucket().toMillis() + " milliseconds") + " AS interval), "
                + axis + ", " + p.instant(q.from()) + ") AS t, " + group + " AS g, v.source_id, v.external_id"
                + " FROM record_version v WHERE " + String.join(" AND ", where);
        if ("none".equals(q.groupBy())) {
            return "SELECT x.t, CAST(NULL AS text) AS g, " + measure + " AS n, 1 AS grp FROM (" + inner + ") x"
                    + " GROUP BY x.t";
        }
        return "SELECT x.t, x.g, " + measure + " AS n, GROUPING(x.g) AS grp FROM (" + inner + ") x"
                + " GROUP BY GROUPING SETS ((x.t, x.g), (x.t))";
    }

    /** Alerts per bucket and severity, under the same source, kind, bbox and restriction filters. */
    static String alertSql(TimelineQuery q, SqlParams p) {
        List<String> where = new ArrayList<>();
        where.add("a.fired_at >= " + p.instant(q.from()) + " AND a.fired_at < " + p.instant(q.to()));
        where.add("a.source_id = ANY(" + p.textArray(q.sources()) + ")");
        // Exercise alerts (prompt 115) are not part of the timeline.
        where.add("NOT a.exercise");
        if (!q.kinds().isEmpty()) {
            where.add("a.kind = ANY(" + p.textArray(q.kinds()) + ")");
        }
        if (q.bbox() != null) {
            where.add(RecordQueryService.bboxClause(RestrictionClamp.ALERT_GEOMETRY_SQL, q.bbox(), p));
        }
        RestrictionClamp.sqlPredicate("a.source_id", "a.kind", RestrictionClamp.ALERT_GEOMETRY_SQL,
                "(a.record_snapshot -> 'payload')", q.restriction(), p).ifPresent(where::add);
        return "SELECT date_bin(CAST(" + p.add(q.bucket().toMillis() + " milliseconds") + " AS interval), a.fired_at, "
                + p.instant(q.from()) + ") AS t, a.severity, count(*) AS n FROM alert a WHERE "
                + String.join(" AND ", where) + " GROUP BY 1, 2";
    }
}
