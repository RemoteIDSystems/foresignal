package ai.foresignal.security.visibility;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * {@code app_setting['visibility']} (prompt 039): the sources the operator made private through
 * {@code updateSource} ({@code SourceSettings.private}). A source whose descriptor is private is private
 * whatever this says; the operator can only tighten.
 *
 * @param privateSources the operator-private source ids
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VisibilitySettings(Set<String> privateSources) {

    /** Copies the set, sorted. */
    public VisibilitySettings {
        privateSources = privateSources == null ? Set.of()
                : java.util.Collections.unmodifiableSet(new TreeSet<>(privateSources));
    }

    /** Nothing tightened. */
    public static VisibilitySettings defaults() {
        return new VisibilitySettings(null);
    }

    /** True when the operator made {@code sourceId} private. */
    public boolean isPrivate(String sourceId) {
        return privateSources.contains(sourceId);
    }

    /** This value with {@code sourceId} made private ({@code true}) or no longer tightened ({@code false}). */
    public VisibilitySettings with(String sourceId, boolean makePrivate) {
        Set<String> next = new TreeSet<>(privateSources);
        if (makePrivate) {
            next.add(sourceId);
        } else {
            next.remove(sourceId);
        }
        return new VisibilitySettings(next);
    }

    /** This value keeping only {@code known} ids. */
    public VisibilitySettings retain(Collection<String> known) {
        Set<String> next = new TreeSet<>(privateSources);
        next.retainAll(known);
        return new VisibilitySettings(next);
    }
}
