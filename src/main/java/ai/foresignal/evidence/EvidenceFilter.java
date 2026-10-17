package ai.foresignal.evidence;

import java.util.Set;

/**
 * The filter of the SSE topic {@code evidence} (prompt 116).
 *
 * @param excludedSources sources the caller may not see; an event built only from them is withheld
 * @param family only this family, or null for all
 */
public record EvidenceFilter(Set<String> excludedSources, String family) {

    /** Copies the set. */
    public EvidenceFilter {
        excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
    }

    /** True when {@code event} passes this filter. */
    public boolean matches(EvidenceEvent event) {
        if (family != null && !family.equals(event.family())) {
            return false;
        }
        if (event.sourceIds().isEmpty()) {
            return true;
        }
        for (String source : event.sourceIds()) {
            if (!excludedSources.contains(source)) {
                return true;
            }
        }
        return false;
    }
}
