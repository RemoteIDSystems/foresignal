package ai.foresignal.store.frames;

import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.security.restrict.ReadRestriction;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * One frames request, already parsed.
 *
 * @param sources source ids (a source or kind filter is required)
 * @param kinds kinds; when sources are named they narrow the chosen versions
 * @param bbox spatial filter, or null
 * @param from the keyframe instant
 * @param to the last frame's end
 * @param step the frame step (at least 10 s)
 * @param maxFrames the most frames (at most 240)
 * @param fullGeometry true for {@code geometry=full}, false for centroids
 * @param excludedSources sources the caller may not see (the private gate)
 * @param restriction the caller's read restriction, with any {@code place=} filter folded in
 */
public record FramesQuery(
        List<String> sources,
        List<String> kinds,
        BboxFilter bbox,
        Instant from,
        Instant to,
        Duration step,
        int maxFrames,
        boolean fullGeometry,
        Set<String> excludedSources,
        ReadRestriction restriction) {

    /** Normalizes nulls. */
    public FramesQuery {
        sources = sources == null ? List.of() : List.copyOf(sources);
        kinds = kinds == null ? List.of() : List.copyOf(kinds);
        excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
        restriction = restriction == null ? ReadRestriction.NONE : restriction;
    }
}
