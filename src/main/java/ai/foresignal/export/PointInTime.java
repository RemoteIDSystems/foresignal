package ai.foresignal.export;

import ai.foresignal.source.spi.HistoryTier;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The state "as known at seq {@code s}" an asOf export or an {@code atSeq} snapshot reads (prompt 111,
 * Core-Data-Model §12): per tier the newest version per entity with {@code seq BETWEEN L AND s},
 * kept when it is not a tombstone and still valid at the transaction time {@code t}.
 *
 * @param s the seq of the state
 * @param t the transaction time of {@code s} (its {@code ingested_at})
 * @param lower per tier, the lower seq bound {@code L} (TRACK {@code S(t − ttl − 2 min)}, STANDARD 0)
 * @param validity per source, its TTL (widened over downsampled ranges), or null without one
 * @param held per source read partly from {@code record_version_held}, the ids of the covering holds
 * @param approximate true when a source is SAMPLED or LATEST, or a range is downsampled
 */
public record PointInTime(long s, Instant t, Map<HistoryTier, Long> lower, Map<String, Duration> validity,
        Map<String, List<String>> held, boolean approximate) {

    /** Copies the maps. */
    public PointInTime {
        lower = lower.isEmpty() ? Map.of() : Collections.unmodifiableMap(new EnumMap<>(lower));
        validity = Collections.unmodifiableMap(new LinkedHashMap<>(validity));
        held = Collections.unmodifiableMap(new LinkedHashMap<>(held));
    }

    /** The lower seq bound of {@code tier} (0 when not set). */
    public long lower(HistoryTier tier) {
        return lower.getOrDefault(tier, 0L);
    }

    /** The hold ids whose copies are read for {@code sourceId} (empty when none). */
    public List<String> holdsFor(String sourceId) {
        return held.getOrDefault(sourceId, List.of());
    }

    /**
     * Whether a version is valid at {@code t}: on STANDARD its explicit {@code validUntil} when set,
     * else (and always on TRACK) {@code ingested_at + ttl > t}; no TTL means valid.
     */
    public boolean valid(HistoryTier tier, String sourceId, Instant ingestedAt, Instant validUntilExplicit) {
        if (tier == HistoryTier.STANDARD && validUntilExplicit != null) {
            return validUntilExplicit.isAfter(t);
        }
        Duration ttl = validity.get(sourceId);
        return ttl == null || ingestedAt == null || ingestedAt.plus(ttl).isAfter(t);
    }
}
