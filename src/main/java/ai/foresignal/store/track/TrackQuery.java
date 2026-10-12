package ai.foresignal.store.track;

import ai.foresignal.security.restrict.ReadRestriction;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * One track request, already parsed.
 *
 * @param sourceId the source
 * @param externalId the entity
 * @param from window start (inclusive, on {@code observed_at})
 * @param to window end (inclusive)
 * @param toleranceMeters the simplification tolerance, or null for {@code auto}
 * @param maxPoints the point cap (2–10,000)
 * @param gap the time gap that starts a new segment, or null for {@code max(ttl, 10 min)}
 * @param includeLive append the current live position when it is newer than the last version
 * @param fields at most four numeric payload fields to return in parallel arrays
 * @param excludedSources sources the caller may not see (a track of one is 404)
 * @param restriction the caller's read restriction (prompts 035, 036)
 */
public record TrackQuery(
        String sourceId,
        String externalId,
        Instant from,
        Instant to,
        Double toleranceMeters,
        int maxPoints,
        Duration gap,
        boolean includeLive,
        List<String> fields,
        Set<String> excludedSources,
        ReadRestriction restriction) {

    /** Normalizes nulls. */
    public TrackQuery {
        fields = fields == null ? List.of() : List.copyOf(fields);
        excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
        restriction = restriction == null ? ReadRestriction.NONE : restriction;
    }
}
