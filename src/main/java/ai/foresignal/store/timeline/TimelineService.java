package ai.foresignal.store.timeline;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.history.AsOfResolver;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Service;

/**
 * Validates a timeline request, resolves its sources and chooses the bucket, then asks the
 * {@link TimelineBackend} (prompt 037).
 *
 * <ul>
 *   <li>Sources: the named ones (404 when unknown), else those declaring a named kind, else every
 *       registered source; the caller's hidden sources are removed.
 *   <li>Bucket: a duration, or {@code auto}, the smallest of {@link #AUTO_LADDER} giving at most
 *       {@value #AUTO_TARGET} buckets. More than {@value #MAX_BUCKETS} buckets is
 *       {@code 400 TIMELINE_TOO_LARGE}.
 * </ul>
 */
@Service
public class TimelineService {

    /** The most buckets. */
    public static final int MAX_BUCKETS = 1_000;

    /** How many buckets {@code bucket=auto} aims at, at most. */
    public static final int AUTO_TARGET = 200;

    /** The smallest bucket. */
    public static final Duration MIN_BUCKET = Duration.ofSeconds(1);

    /** The widths {@code bucket=auto} chooses from, smallest first. */
    public static final List<Duration> AUTO_LADDER = List.of(
            Duration.ofMinutes(1), Duration.ofMinutes(5), Duration.ofMinutes(10), Duration.ofMinutes(15),
            Duration.ofMinutes(30), Duration.ofHours(1), Duration.ofHours(2), Duration.ofHours(3), Duration.ofHours(6),
            Duration.ofHours(12), Duration.ofDays(1), Duration.ofDays(2), Duration.ofDays(7), Duration.ofDays(14),
            Duration.ofDays(30));

    private final TimelineBackend backend;
    private final SourceRegistry registry;
    private final AsOfResolver asOf;

    /** Creates the service. */
    public TimelineService(TimelineBackend backend, SourceRegistry registry, AsOfResolver asOf) {
        this.backend = backend;
        this.registry = registry;
        this.asOf = asOf;
    }

    /**
     * The timeline.
     *
     * @param sources requested source ids (may be empty)
     * @param kinds requested kinds (may be empty)
     * @param bbox spatial filter, or null
     * @param from start (required)
     * @param to end (required, after {@code from})
     * @param bucket the requested bucket, or null for {@code auto}
     * @param timeField the axis
     * @param measure {@code versions} or {@code entities}
     * @param groupBy {@code none}, {@code kind} or {@code source}
     * @param includeKeyframes count HEARTBEAT and KEYFRAME versions
     * @param includeAlerts add alert counts
     * @param excluded sources the caller may not see
     * @param restriction the caller's restriction
     */
    public Timeline timeline(List<String> sources, List<String> kinds, BboxFilter bbox, Instant from, Instant to,
            Duration bucket, TimeField timeField, String measure, String groupBy, boolean includeKeyframes,
            boolean includeAlerts, Set<String> excluded, ReadRestriction restriction) {
        if (from == null || to == null) {
            throw ValidationException.forField(from == null ? "from" : "to", "is required");
        }
        if (!from.isBefore(to)) {
            throw ValidationException.forField("to", "must be after from");
        }
        Duration width = bucket == null ? auto(from, to) : bucket;
        if (width.compareTo(MIN_BUCKET) < 0) {
            throw ValidationException.forField("bucket", "must be at least 1s");
        }
        long count = buckets(from, to, width);
        if (count > MAX_BUCKETS) {
            throw new ValidationException(ErrorCodes.TIMELINE_TOO_LARGE, "The timeline would have " + count
                    + " buckets, more than " + MAX_BUCKETS + "; use a larger bucket or a shorter range.", List.of());
        }
        List<String> involved = involved(sources, kinds, excluded);
        return backend.timeline(new TimelineQuery(involved, kinds, bbox, from, to, width, timeField, measure, groupBy,
                includeKeyframes, includeAlerts, restriction));
    }

    /** {@code ceil((to − from) / width)}. */
    static long buckets(Instant from, Instant to, Duration width) {
        long span = Duration.between(from, to).toMillis();
        long w = width.toMillis();
        return (span + w - 1) / w;
    }

    /** The smallest ladder width with at most {@value #AUTO_TARGET} buckets, else the largest. */
    public static Duration auto(Instant from, Instant to) {
        for (Duration width : AUTO_LADDER) {
            if (buckets(from, to, width) <= AUTO_TARGET) {
                return width;
            }
        }
        return AUTO_LADDER.getLast();
    }

    private List<String> involved(List<String> sources, List<String> kinds, Set<String> excluded) {
        if ((sources != null && !sources.isEmpty()) || (kinds != null && !kinds.isEmpty())) {
            return asOf.involved(sources, kinds, excluded).stream().map(SourceDescriptor::id).toList();
        }
        Set<String> all = new TreeSet<>(registry.ids());
        all.removeAll(excluded);
        return List.copyOf(all);
    }
}
