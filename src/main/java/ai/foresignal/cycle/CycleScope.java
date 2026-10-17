package ai.foresignal.cycle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What a cycle covers ({@code CycleScope}, prompt 128): regions (ISO 3166-1 alpha-2 or 3166-2), themes, situations,
 * hypothesis sets (a list, or {@code cadence-match} for every set whose cadence matches the cycle's), sources and
 * question categories. Empty dimensions admit everything.
 *
 * <p>Filters apply where an item has a value for the dimension (an item without regions passes the region filter).
 * An on-demand situation assessment uses a <em>focused</em> scope: an item must then belong to one of the listed
 * situations or hypothesis sets.
 *
 * @param regionCodes region codes
 * @param themes situation themes
 * @param situationIds situation ids
 * @param hypothesisSetIds hypothesis set ids
 * @param cadenceMatch true for {@code cadence-match}
 * @param sourceIds source ids
 * @param categories forecast question categories
 * @param focused true when items must belong to the listed situations or sets
 */
public record CycleScope(List<String> regionCodes, List<String> themes, List<String> situationIds,
        List<String> hypothesisSetIds, boolean cadenceMatch, List<String> sourceIds, List<String> categories,
        boolean focused) {

    /** The wire value of {@code cadence-match}. */
    public static final String CADENCE_MATCH = "cadence-match";

    /** An empty scope (everything). */
    public static final CycleScope ALL = new CycleScope(List.of(), List.of(), List.of(), List.of(), false, List.of(),
            List.of(), false);

    /** Copies the lists. */
    public CycleScope {
        regionCodes = copy(regionCodes);
        themes = copy(themes);
        situationIds = copy(situationIds);
        hypothesisSetIds = copy(hypothesisSetIds);
        sourceIds = copy(sourceIds);
        categories = copy(categories);
    }

    /** The scope stored in a cycle row (unknown members ignored). */
    public static CycleScope of(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return ALL;
        }
        Object sets = map.get("hypothesisSetIds");
        boolean match = CADENCE_MATCH.equals(sets);
        return new CycleScope(strings(map.get("regionCodes")), strings(map.get("themes")),
                strings(map.get("situationIds")), match ? List.of() : strings(sets), match,
                strings(map.get("sourceIds")), strings(map.get("categories")), false);
    }

    /** A focused scope of one situation and its sets (on-demand assessments). */
    public static CycleScope focusedOn(String situationId, Collection<String> setIds) {
        return new CycleScope(List.of(), List.of(), List.of(situationId), new ArrayList<>(setIds), false, List.of(),
                List.of(), true);
    }

    /** The stored form ({@code focused} is never stored). */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("regionCodes", regionCodes);
        out.put("themes", themes);
        out.put("situationIds", situationIds);
        out.put("hypothesisSetIds", cadenceMatch ? CADENCE_MATCH : hypothesisSetIds);
        out.put("sourceIds", sourceIds);
        out.put("categories", categories);
        return out;
    }

    /**
     * True when an item passes the scope.
     *
     * @param regions the item's regions (empty: not located)
     * @param theme the item's theme, or null
     * @param situationId the item's situation, or null
     * @param setId the item's hypothesis set, or null
     * @param setCadence the cadence of the item's set, or null
     * @param sources the item's sources (empty: none)
     * @param category the item's question category, or null
     * @param cycleCadence the cycle's cadence (for {@code cadence-match})
     */
    public boolean admits(Collection<String> regions, String theme, String situationId, String setId,
            String setCadence, Collection<String> sources, String category, String cycleCadence) {
        if (focused) {
            boolean inSituation = situationId != null && situationIds.contains(situationId);
            boolean inSet = setId != null && hypothesisSetIds.contains(setId);
            return inSituation || inSet;
        }
        if (!regionCodes.isEmpty() && regions != null && !regions.isEmpty() && !regionsOverlap(regions)) {
            return false;
        }
        if (!themes.isEmpty() && theme != null && !themes.contains(theme)) {
            return false;
        }
        if (!situationIds.isEmpty() && situationId != null && !situationIds.contains(situationId)) {
            return false;
        }
        if (setId != null) {
            if (cadenceMatch && setCadence != null && cycleCadence != null && !cycleCadence.equals(setCadence)) {
                return false;
            }
            if (!hypothesisSetIds.isEmpty() && !hypothesisSetIds.contains(setId)) {
                return false;
            }
        }
        if (!sourceIds.isEmpty() && sources != null && !sources.isEmpty()
                && sources.stream().noneMatch(sourceIds::contains)) {
            return false;
        }
        return categories.isEmpty() || category == null || categories.contains(category);
    }

    /** True when a region of the item equals, contains or lies in a scope region ({@code US} and {@code US-TX}). */
    boolean regionsOverlap(Collection<String> regions) {
        for (String region : regions) {
            if (region == null) {
                continue;
            }
            for (String scope : regionCodes) {
                if (region.equals(scope) || region.startsWith(scope + "-") || scope.startsWith(region + "-")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<String> copy(List<String> in) {
        return in == null ? List.of() : List.copyOf(in);
    }

    /** The strings of a JSON list (anything else: empty). */
    static List<String> strings(Object raw) {
        List<String> out = new ArrayList<>();
        if (raw instanceof Collection<?> list) {
            for (Object item : list) {
                if (item != null && !String.valueOf(item).isBlank()) {
                    out.add(String.valueOf(item).strip());
                }
            }
        }
        return out;
    }
}
