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
     * @param filter the places' and view's request filter (prompt 036), ANDed into the run's
     *     restriction, or null
     * @param placeNames the names of the scope's places, for the digest heading (never coordinates)
     */
    record Resolved(List<String> sources, List<String> kinds, BboxFilter bbox, Set<UUID> ruleIds,
            boolean includeHealth, ai.foresignal.security.restrict.RecordFilter filter, List<String> placeNames) {

        /** Filters without places or a view. */
        public Resolved(List<String> sources, List<String> kinds, BboxFilter bbox, Set<UUID> ruleIds,
                boolean includeHealth) {
            this(sources, kinds, bbox, ruleIds, includeHealth, null, List.of());
        }

        /** Copies the collections. */
        public Resolved {
            sources = sources == null ? List.of() : List.copyOf(sources);
            kinds = kinds == null ? List.of() : List.copyOf(kinds);
            ruleIds = ruleIds == null ? Set.of() : Set.copyOf(ruleIds);
            placeNames = placeNames == null ? List.of() : List.copyOf(placeNames);
        }

        /** {@code restriction} narrowed by {@link #filter}. */
        public ai.foresignal.security.restrict.ReadRestriction narrow(
                ai.foresignal.security.restrict.ReadRestriction restriction) {
            return restriction.withFilter(filter);
        }
    }
}
