package ai.foresignal.store.timeline;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * An event-density histogram (the {@code Timeline} schema).
 *
 * @param from start
 * @param to end
 * @param bucket the bucket width
 * @param timeField the time axis
 * @param measure {@code versions} or {@code entities}
 * @param groupBy {@code none}, {@code kind} or {@code source}
 * @param buckets every bucket in range, empty ones included, in time order
 * @param alerts alert counts parallel to {@code buckets}, or null without {@code includeAlerts}
 * @param maxBucket the largest bucket total
 * @param approximate a sampled source or a downsampled range is involved
 * @param retainedFrom the oldest instant every involved source still retains, or null
 * @param backend {@code raw}, {@code rollup} or {@code mixed} (prompt 038)
 */
public record Timeline(Instant from, Instant to, Duration bucket, String timeField, String measure, String groupBy,
        List<Bucket> buckets, List<AlertBucket> alerts, long maxBucket, boolean approximate, Instant retainedFrom,
        String backend) {

    /**
     * One bucket.
     *
     * @param t the bucket start
     * @param total versions or distinct entities in the bucket
     * @param counts per kind or source when grouped, else empty
     */
    public record Bucket(Instant t, long total, Map<String, Long> counts) {}

    /**
     * Alert counts of one bucket by severity.
     *
     * @param t the bucket start
     * @param info {@code info} alerts fired in the bucket
     * @param warning {@code warning} alerts
     * @param critical {@code critical} alerts
     */
    public record AlertBucket(Instant t, long info, long warning, long critical) {}
}
