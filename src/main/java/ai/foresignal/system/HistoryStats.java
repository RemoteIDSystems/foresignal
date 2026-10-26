package ai.foresignal.system;

import ai.foresignal.retention.DownsampleRanges;
import ai.foresignal.retention.RetentionResolver;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.history.PartitionManager;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * History storage per tier (Core-Data-Model §15, {@code /v1/system.history}), from the leaves of
 * {@code pg_partition_tree('record_version')}: partitions, rows (Σ {@code reltuples}), bytes
 * (Σ {@code pg_total_relation_size}), seq headroom and the tier's window; plus the versions written
 * per minute over the last hour ({@code source_usage}) and the projected bytes at the current rate.
 *
 * <p>Catalog queries are cheap but not free, so a snapshot is cached for {@value #CACHE_SECONDS}
 * seconds; the operator gauges and {@code /v1/system} share it.
 */
@Component
public class HistoryStats {

    /** How long a snapshot is reused. */
    static final long CACHE_SECONDS = 30;

    /** Above this many versions per minute {@code HISTORY_RATE_HIGH} is raised (Core §15). */
    public static final double HIGH_RATE = 20_000;

    static final String LEAVES = """
            SELECT t.parentrelid::regclass::text AS parent, greatest(c.reltuples, 0)::bigint AS rows,
                   pg_total_relation_size(t.relid) AS bytes
              FROM pg_partition_tree('record_version') t
              JOIN pg_class c ON c.oid = t.relid
             WHERE t.isleaf
            """;

    static final String RATE = "SELECT source_id, coalesce(sum(versions), 0) FROM source_usage WHERE minute >= ? GROUP BY source_id";

    /**
     * One tier.
     *
     * @param tier the tier
     * @param partitions attached leaves
     * @param rows estimated rows
     * @param bytes bytes including indexes and TOAST
     * @param headroomSeq seq values covered ahead of the allocator, or null before the first read
     * @param windowDays the tier's retention window in days
     * @param versionsPerMinute versions written per minute over the last hour
     * @param downsampledLeaves leaves thinned by history downsampling (prompt 044)
     * @param downsampledRows rows those leaves kept
     */
    public record Tier(HistoryTier tier, int partitions, long rows, long bytes, Long headroomSeq, double windowDays,
            double versionsPerMinute, int downsampledLeaves, long downsampledRows) {}

    /**
     * A snapshot.
     *
     * @param tiers TRACK and STANDARD
     * @param versionsPerMinute both tiers
     * @param projectedBytes Σ over tiers of bytes per row × versions per minute × window minutes
     * @param at when it was taken
     */
    public record Snapshot(List<Tier> tiers, double versionsPerMinute, long projectedBytes, Instant at) {

        /** The tier's entry, or null. */
        public Tier tier(HistoryTier tier) {
            return tiers.stream().filter(t -> t.tier() == tier).findFirst().orElse(null);
        }
    }

    private final JdbcTemplate jdbc;
    private final PartitionManager partitions;
    private final RetentionResolver resolver;
    private final SourceRegistry registry;
    private final DownsampleRanges downsampled;
    private final Clock clock;
    private volatile Snapshot cached;

    /** Creates the component. */
    public HistoryStats(JdbcTemplate jdbc, PartitionManager partitions, RetentionResolver resolver,
            SourceRegistry registry, DownsampleRanges downsampled, Clock clock) {
        this.jdbc = jdbc;
        this.partitions = partitions;
        this.resolver = resolver;
        this.registry = registry;
        this.downsampled = downsampled;
        this.clock = clock;
    }

    /** A snapshot no older than {@value #CACHE_SECONDS} seconds. */
    public Snapshot snapshot() {
        Snapshot current = cached;
        Instant now = clock.instant();
        if (current != null && !current.at().isAfter(now)
                && Duration.between(current.at(), now).getSeconds() < CACHE_SECONDS) {
            return current;
        }
        Snapshot fresh = compute(now);
        cached = fresh;
        return fresh;
    }

    /** Drops the cached snapshot (tests, and after a retention run). */
    public void invalidate() {
        cached = null;
    }

    Snapshot compute(Instant now) {
        Map<HistoryTier, long[]> sums = new EnumMap<>(HistoryTier.class);
        for (HistoryTier tier : HistoryTier.values()) {
            sums.put(tier, new long[3]);
        }
        String trackParent = PartitionManager.parentOf(HistoryTier.TRACK);
        jdbc.query(LEAVES, rs -> {
            HistoryTier tier = trackParent.equals(rs.getString(1)) ? HistoryTier.TRACK : HistoryTier.STANDARD;
            long[] s = sums.get(tier);
            s[0]++;
            s[1] += rs.getLong(2);
            s[2] += rs.getLong(3);
        });
        Map<HistoryTier, Double> rates = new EnumMap<>(HistoryTier.class);
        rates.put(HistoryTier.TRACK, 0.0);
        rates.put(HistoryTier.STANDARD, 0.0);
        jdbc.query(RATE, rs -> {
            String source = rs.getString(1);
            HistoryTier tier = registry.find(source).map(r -> r.descriptor().historyTier()).orElse(HistoryTier.STANDARD);
            rates.merge(tier, rs.getLong(2) / 60.0, Double::sum);
        }, Timestamp.from(now.minus(Duration.ofHours(1))));

        RetentionSettings settings = resolver.settings();
        Duration standardWindow = settings.capped(settings.defaultRetention());
        for (Map<String, Duration> windows : resolver.standardWindows().values()) {
            for (Duration window : windows.values()) {
                standardWindow = window.compareTo(standardWindow) > 0 ? window : standardWindow;
            }
        }
        List<Tier> tiers = new ArrayList<>();
        double totalRate = 0;
        double projected = 0;
        for (HistoryTier tier : List.of(HistoryTier.TRACK, HistoryTier.STANDARD)) {
            long[] s = sums.get(tier);
            double headroom = partitions.headroom(tier);
            Duration window = tier == HistoryTier.TRACK ? settings.effectiveTrackWindow() : standardWindow;
            double rate = rates.get(tier);
            DownsampleRanges.Summary thinned = downsampled.summary(tier);
            tiers.add(new Tier(tier, (int) s[0], s[1], s[2], Double.isNaN(headroom) ? null : (long) headroom,
                    window.toMinutes() / 1440.0, rate, thinned.leaves(), thinned.rows()));
            totalRate += rate;
            if (s[1] > 0) {
                projected += (double) s[2] / s[1] * rate * window.toMinutes();
            }
        }
        return new Snapshot(List.copyOf(tiers), totalRate, (long) projected, now);
    }
}
