package ai.foresignal.ingest;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * What the ingest writer did with one batch (Core-Data-Model §6.2, "IngestOutcome").
 *
 * <p>Final shape, replacing prompt 006's interim record. Health stores {@code persisted} as
 * {@code last_records_changed}.
 *
 * @param seen records submitted
 * @param persisted versions written
 * @param live observations written as live-only updates, without a version
 * @param touched observations written as touches (each entity gets one touch row per batch)
 * @param touchesThrottled touches suppressed by the throttle
 * @param late observations dropped as older than the live state
 * @param skipped tombstones for absent or already tombstoned entities
 * @param rejected records the normalizer refused
 * @param clamped observations whose future {@code observed_at} was clamped beyond the skew
 *     tolerance (kept from prompt 006, for {@code UsageMeter})
 * @param fromSeq the first seq allocated, or 0 when none was
 * @param toSeq the last seq allocated, or 0 when none was
 * @param byReason versions per persist reason; always holds all nine reasons, zeros included
 * @param versionBytes the estimated stored size of the versions written (§6)
 * @param rejections each refused record's index, external id and reason
 */
public record IngestOutcome(
        int seen,
        int persisted,
        int live,
        int touched,
        int touchesThrottled,
        int late,
        int skipped,
        int rejected,
        int clamped,
        long fromSeq,
        long toSeq,
        Map<PersistReason, Integer> byReason,
        long versionBytes,
        List<Rejection> rejections) {

    public IngestOutcome {
        byReason = completeReasons(byReason);
        rejections = rejections == null ? List.of() : List.copyOf(rejections);
    }

    /** An outcome with only the seen and persisted counts, for callers that know nothing more. */
    public static IngestOutcome of(int seen, int persisted) {
        return new IngestOutcome(seen, persisted, 0, 0, 0, 0, 0, 0, 0, 0, 0, null, 0, List.of());
    }

    /** The outcome of a batch with no records. */
    public static IngestOutcome empty() {
        return of(0, 0);
    }

    /** Versions persisted for {@code reason}. */
    public int persisted(PersistReason reason) {
        return byReason.get(reason);
    }

    private static Map<PersistReason, Integer> completeReasons(Map<PersistReason, Integer> given) {
        EnumMap<PersistReason, Integer> all = new EnumMap<>(PersistReason.class);
        for (PersistReason reason : PersistReason.values()) {
            Integer count = given == null ? null : given.get(reason);
            all.put(reason, count == null ? 0 : count);
        }
        return Collections.unmodifiableMap(all);
    }
}
