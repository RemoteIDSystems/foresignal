package ai.foresignal.stats;

import ai.foresignal.api.support.TimeField;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.store.query.SqlParams;
import ai.foresignal.store.timeline.RawTimelineBackend;
import ai.foresignal.store.timeline.Timeline;
import ai.foresignal.store.timeline.TimelineBackend;
import ai.foresignal.store.timeline.TimelineQuery;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * The timeline backend of {@code /v1/timeline} (prompt 037) with the activity rollup behind it (prompt
 * 038): {@code timeField=ingested} buckets that start before {@code retainedFrom}, where raw history is
 * already incomplete, are served from {@code activity_rollup}; everything else, and the alert overlay,
 * comes from {@link RawTimelineBackend}.
 *
 * <p>The rollup is used only when it can answer exactly as the raw backend would: the ingested axis,
 * keyframes excluded, no filter needing the payload, every involved source rolled up, buckets that are
 * whole multiples of an hour starting on an hour ({@code measure=versions}), or exactly an hour or a UTC
 * day ({@code measure=entities}, which is not additive). The document says {@code backend: rollup} when
 * every bucket came from the rollup and {@code mixed} when some did.
 */
@Component
@Primary
public class RollupTimelineBackend implements TimelineBackend {

    private final RawTimelineBackend raw;
    private final RollupJob rollup;
    private final StatsSettingsService statsSettings;
    private final ReadQueryRunner runner;
    private final AppSettingService settings;
    private final Clock clock;

    /** Creates the backend. */
    public RollupTimelineBackend(RawTimelineBackend raw, RollupJob rollup, StatsSettingsService statsSettings,
            ReadQueryRunner runner, AppSettingService settings, Clock clock) {
        this.raw = raw;
        this.rollup = rollup;
        this.statsSettings = statsSettings;
        this.runner = runner;
        this.settings = settings;
        this.clock = clock;
    }

    @Override
    public Timeline timeline(TimelineQuery q) {
        Timeline base = raw.timeline(q);
        Instant retainedFrom = base.retainedFrom();
        RollupResolution resolution = resolution(q);
        RollupCursor cursor = rollup.cursor();
        if (resolution == null || cursor == null || retainedFrom == null || !q.from().isBefore(retainedFrom)
                || q.sources().isEmpty() || !Set.copyOf(statsSettings.rolledUpSources()).containsAll(q.sources())) {
            return base;
        }
        StatsMeasure measure = "entities".equals(q.measure()) ? StatsMeasure.ENTITIES : StatsMeasure.VERSIONS;
        StatsQuery sq = new StatsQuery(q.sources(), q.kinds(), q.bbox(), q.restriction(), q.from(), q.to(), resolution,
                measure, q.groupBy(), TimeField.INGESTED);
        if (!sq.rollupUsable()) {
            return base;
        }
        Instant now = clock.instant();
        Instant start = later(cursor.start(resolution), resolution.ceil(now.minus(window(resolution))));
        Instant end = cursor.end(resolution);
        // The replaced buckets: wholly inside the rollup's range and starting before retainedFrom.
        List<Integer> replaced = new ArrayList<>();
        Instant replaceFrom = null;
        Instant replaceTo = null;
        for (int i = 0; i < base.buckets().size(); i++) {
            Instant t = base.buckets().get(i).t();
            Instant bucketEnd = t.plus(q.bucket());
            if (!t.isBefore(start) && !bucketEnd.isAfter(end) && t.isBefore(retainedFrom)) {
                replaced.add(i);
                replaceFrom = replaceFrom == null ? t : replaceFrom;
                replaceTo = bucketEnd;
            }
        }
        if (replaced.isEmpty()) {
            return base;
        }
        Instant from = replaceFrom;
        Instant to = replaceTo;
        Map<Instant, Map<String, Long>> values = new HashMap<>();
        runner.read(RawTimelineBackend.TIMEOUT, jdbc -> {
            SqlParams p = new SqlParams();
            List<String> where = StatsService.rollupWhere(sq, q.sources(), resolution, from, to, p);
            String g = switch (q.groupBy()) {
                case "kind" -> "r.kind";
                case "source" -> "r.source_id";
                default -> "CAST(NULL AS text)";
            };
            String sql = "SELECT date_bin(CAST(" + p.add(q.bucket().toMillis() + " milliseconds") + " AS interval),"
                    + " r.bucket_start, " + p.instant(q.from()) + ") AS t, " + g + " AS g, sum(r." + measure.column()
                    + ") AS n FROM activity_rollup r WHERE " + String.join(" AND ", where) + " GROUP BY 1, 2";
            jdbc.query(sql, p.source(), rs -> {
                Instant t = rs.getObject("t", OffsetDateTime.class).toInstant();
                String key = rs.getString("g");
                values.computeIfAbsent(t, k -> new TreeMap<>()).merge(key == null ? "" : key, rs.getLong("n"),
                        Long::sum);
            });
            return null;
        });
        List<Timeline.Bucket> buckets = new ArrayList<>(base.buckets());
        for (int i : replaced) {
            Instant t = buckets.get(i).t();
            Map<String, Long> groups = values.getOrDefault(t, Map.of());
            long total = groups.values().stream().mapToLong(Long::longValue).sum();
            Map<String, Long> counts = "none".equals(q.groupBy()) ? Map.of() : new TreeMap<>(groups);
            buckets.set(i, new Timeline.Bucket(t, total, counts));
        }
        long max = buckets.stream().mapToLong(Timeline.Bucket::total).max().orElse(0);
        String backend = replaced.size() == buckets.size() ? "rollup" : "mixed";
        return new Timeline(base.from(), base.to(), base.bucket(), base.timeField(), base.measure(), base.groupBy(),
                List.copyOf(buckets), base.alerts(), max, base.approximate(), retainedFrom, backend);
    }

    /**
     * The rollup resolution that serves {@code q}'s buckets exactly, or null: hourly rows for
     * {@code versions} buckets that are whole hours starting on an hour; for {@code entities}, hourly
     * rows only for 1 h buckets and daily rows only for 1 d buckets starting on a UTC midnight.
     */
    static RollupResolution resolution(TimelineQuery q) {
        if (q.timeField() != TimeField.INGESTED || q.includeKeyframes()) {
            return null;
        }
        Duration width = q.bucket();
        boolean hourAligned = RollupResolution.HOUR.floor(q.from()).equals(q.from());
        boolean dayAligned = RollupResolution.DAY.floor(q.from()).equals(q.from());
        if ("entities".equals(q.measure())) {
            if (width.equals(Duration.ofHours(1)) && hourAligned) {
                return RollupResolution.HOUR;
            }
            if (width.equals(Duration.ofDays(1)) && dayAligned) {
                return RollupResolution.DAY;
            }
            return null;
        }
        if (!hourAligned || width.toMillis() % Duration.ofHours(1).toMillis() != 0) {
            return null;
        }
        return RollupResolution.HOUR;
    }

    private Duration window(RollupResolution resolution) {
        RetentionSettings.Stats stats = settings.getOrDefault(SettingKeys.RETENTION, RetentionSettings.class,
                RetentionSettings::defaults).stats();
        return resolution == RollupResolution.DAY ? stats.daily() : stats.hourly();
    }

    private static Instant later(Instant a, Instant b) {
        return a.isAfter(b) ? a : b;
    }
}
