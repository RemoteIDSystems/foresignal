package ai.foresignal.store.history;

import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The plan of one read "as known at {@code t}" on the transaction axis (Core-Data-Model §12),
 * computed by {@link AsOfResolver#plan}.
 *
 * <p>For every tier the state at {@code t} is the newest version per entity with
 * {@code seq BETWEEN lByTier[tier] AND s}, kept when it is not a tombstone and still valid at
 * {@code t}: TRACK by {@code ingested_at + validity[source] > t} (an explicit {@code validUntil} is
 * ignored on TRACK), STANDARD by its explicit {@code validUntil}, else
 * {@code ingested_at + validity[source] > t}.
 *
 * @param t the requested transaction time
 * @param s {@code S(t)}: the newest seq ingested at or before {@code t}
 * @param lByTier the lower seq bound per involved tier: TRACK {@code S(t − ttl − widening − 2 min)}
 *     (the smallest over the tier's sources), STANDARD 0
 * @param horizon the earliest supported {@code t} over the involved sources (the latest of their
 *     horizons), or null when no source is involved
 * @param approximate true when an involved source is SAMPLED or LATEST, or a range is downsampled
 * @param watermarkSeq the response's {@code watermarkSeq}; equal to {@code s} (Core-Data-Model §12
 *     step 5)
 * @param sources the involved sources after private exclusion, sorted by id, grouped per tier
 * @param validity per source, its TTL plus {@link HistoryFidelity#validityWidening}; a source
 *     without a TTL maps to null (its versions never expire by TTL)
 * @param downsampled true when an involved tier range {@code [L, S]} is downsampled
 */
public record AsOfPlan(
        Instant t,
        long s,
        Map<HistoryTier, Long> lByTier,
        Instant horizon,
        boolean approximate,
        long watermarkSeq,
        Map<HistoryTier, List<SourceDescriptor>> sources,
        Map<String, Duration> validity,
        boolean downsampled) {

    /** Copies the collections, keeping their order. */
    public AsOfPlan {
        lByTier = lByTier.isEmpty() ? Map.of() : Collections.unmodifiableMap(new EnumMap<>(lByTier));
        Map<HistoryTier, List<SourceDescriptor>> copy = new EnumMap<>(HistoryTier.class);
        sources.forEach((tier, list) -> copy.put(tier, List.copyOf(list)));
        sources = Collections.unmodifiableMap(copy);
        validity = Collections.unmodifiableMap(new LinkedHashMap<>(validity));
    }

    /** The lower seq bound of {@code tier}; 0 when the tier is not involved. */
    public long lower(HistoryTier tier) {
        return lByTier.getOrDefault(tier, 0L);
    }

    /** Every involved source, TRACK last, each tier sorted by id. */
    public List<SourceDescriptor> allSources() {
        List<SourceDescriptor> all = new ArrayList<>();
        sources.values().forEach(all::addAll);
        return List.copyOf(all);
    }

    /** The ids of {@link #allSources()}. */
    public List<String> sourceIds() {
        return allSources().stream().map(SourceDescriptor::id).toList();
    }

    /** True when no source is involved (everything requested was excluded): the answer is empty. */
    public boolean empty() {
        return sources.isEmpty();
    }

    /** The validity duration of {@code sourceId} (TTL plus widening), or null without a TTL. */
    public Duration validity(String sourceId) {
        return validity.get(sourceId);
    }

    /**
     * True when a version of {@code tier} and {@code sourceId}, ingested at {@code ingestedAt} with
     * explicit expiry {@code validUntilExplicit}, is valid at {@code at} (Core-Data-Model §12). The
     * same rule the SQL of every asOf reader applies, for the in-memory frame pass.
     */
    public boolean valid(HistoryTier tier, String sourceId, Instant ingestedAt, Instant validUntilExplicit, Instant at) {
        Duration ttl = validity(sourceId);
        if (tier == HistoryTier.STANDARD && validUntilExplicit != null) {
            return validUntilExplicit.isAfter(at);
        }
        return ttl == null || ingestedAt.plus(ttl).isAfter(at);
    }
}
