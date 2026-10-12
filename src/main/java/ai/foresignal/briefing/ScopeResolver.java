package ai.foresignal.briefing;

import ai.foresignal.api.support.BboxFilter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Folds a {@link BriefingScope} into the plain filters the digest statistics take (prompt 029).
 *
 * <p>This is the hook later prompts extend: 036 resolves {@code placeIds} (place areas) into bbox and
 * area filters, 040 the region filters. Until then {@link DefaultScopeResolver} resolves the members
 * this prompt defines.
 */
public interface ScopeResolver {

    /**
     * The filters of {@code scope}.
     *
     * @throws ai.foresignal.api.error.ValidationException when the scope cannot be resolved
     */
    Resolved resolve(BriefingScope scope);

    /**
     * Resolved filters; empty collections mean "all".
     *
     * @param sources source ids
     * @param kinds kinds
     * @param bbox the area, or null
     * @param ruleIds rules whose alerts are reported
     * @param includeHealth whether the health section is included
     */
    record Resolved(List<String> sources, List<String> kinds, BboxFilter bbox, Set<UUID> ruleIds,
            boolean includeHealth) {

        /** Copies the collections. */
        public Resolved {
            sources = sources == null ? List.of() : List.copyOf(sources);
            kinds = kinds == null ? List.of() : List.copyOf(kinds);
            ruleIds = ruleIds == null ? Set.of() : Set.copyOf(ruleIds);
        }
    }
}
