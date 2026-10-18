package ai.foresignal.inbound.edp;

import java.util.Set;

/**
 * The filter of the SSE topic {@code external} (prompt 138).
 *
 * @param sourceIds only these providers, or empty for all
 */
public record ExternalTopicFilter(Set<String> sourceIds) {

    /** Copies the set. */
    public ExternalTopicFilter {
        sourceIds = sourceIds == null ? Set.of() : Set.copyOf(sourceIds);
    }

    /** True when {@code event} passes this filter. */
    public boolean matches(ExternalEvent event) {
        return sourceIds.isEmpty() || sourceIds.contains(event.sourceId());
    }
}
