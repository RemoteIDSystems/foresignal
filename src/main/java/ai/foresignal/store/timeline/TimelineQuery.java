package ai.foresignal.store.timeline;

import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.security.restrict.ReadRestriction;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * One timeline request after validation: the sources are resolved and the bucket chosen.
 *
 * @param sources the involved source ids (visible to the caller), sorted
 * @param kinds the kind filter, empty for all
 * @param bbox spatial filter, or null
 * @param from start (inclusive; the bucket origin)
 * @param to end (exclusive)
 * @param bucket the bucket width
 * @param timeField the time axis
 * @param measure {@code versions} or {@code entities}
 * @param groupBy {@code none}, {@code kind} or {@code source}
 * @param includeKeyframes count HEARTBEAT and KEYFRAME versions too
 * @param includeAlerts add alert counts per severity per bucket
 * @param restriction the caller's read restriction, with any {@code place=} filter folded in
 */
public record TimelineQuery(
        List<String> sources,
        List<String> kinds,
        BboxFilter bbox,
        Instant from,
        Instant to,
        Duration bucket,
        TimeField timeField,
        String measure,
        String groupBy,
        boolean includeKeyframes,
        boolean includeAlerts,
        ReadRestriction restriction) {

    /** Normalizes nulls. */
    public TimelineQuery {
        sources = sources == null ? List.of() : List.copyOf(sources);
        kinds = kinds == null ? List.of() : List.copyOf(kinds);
        restriction = restriction == null ? ReadRestriction.NONE : restriction;
    }

    /** The number of buckets: {@code ceil((to − from) / bucket)}. */
    public long bucketCount() {
        long span = Duration.between(from, to).toMillis();
        long width = bucket.toMillis();
        return (span + width - 1) / width;
    }

    /** The start of bucket {@code i}. */
    public Instant bucketStart(long i) {
        return from.plus(bucket.multipliedBy(i));
    }
}
