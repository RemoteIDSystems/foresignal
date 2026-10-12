package ai.foresignal.briefing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * What a briefing covers (OpenAPI {@code BriefingScope}, prompt 029): sources, kinds, a bbox, trigger
 * rules for the alert section, and whether source health is included. Empty lists mean "all".
 *
 * <p>The spec's later members ({@code placeIds}, {@code viewId} from 036, {@code country},
 * {@code admin1}, {@code county} from 040, {@code places}, {@code weatherProducts} from 056) are not
 * part of this record yet; {@link BriefingValidator} refuses them when non-empty, and
 * {@link ScopeResolver} is the hook those prompts extend.
 *
 * @param sources source ids
 * @param kinds kinds
 * @param bbox {@code minLon,minLat,maxLon,maxLat}, or null
 * @param ruleIds trigger rules whose alerts the briefing reports; empty for all rules in scope
 * @param includeHealth whether the source health section is included (default true)
 */
public record BriefingScope(List<String> sources, List<String> kinds, String bbox, List<UUID> ruleIds,
        boolean includeHealth) {

    /** Copies the lists. */
    public BriefingScope {
        sources = sources == null ? List.of() : List.copyOf(sources);
        kinds = kinds == null ? List.of() : List.copyOf(kinds);
        ruleIds = ruleIds == null ? List.of() : List.copyOf(ruleIds);
        bbox = bbox == null || bbox.isBlank() ? null : bbox.strip();
    }

    /** Everything, with health. */
    public static BriefingScope all() {
        return new BriefingScope(List.of(), List.of(), null, List.of(), true);
    }

    /** The stored and returned form. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sources", sources);
        out.put("kinds", kinds);
        out.put("bbox", bbox);
        out.put("ruleIds", ruleIds.stream().map(UUID::toString).toList());
        out.put("includeHealth", includeHealth);
        return out;
    }

    /** The scope of a stored document (written by {@link #toMap()}). */
    public static BriefingScope fromMap(Map<String, Object> map) {
        if (map == null) {
            return all();
        }
        List<UUID> rules = new ArrayList<>();
        for (String id : strings(map.get("ruleIds"))) {
            try {
                rules.add(UUID.fromString(id));
            } catch (IllegalArgumentException e) {
                // a stored id is always a UUID; anything else is skipped
            }
        }
        Object health = map.get("includeHealth");
        Object bbox = map.get("bbox");
        return new BriefingScope(strings(map.get("sources")), strings(map.get("kinds")),
                bbox == null ? null : bbox.toString(), rules, !(health instanceof Boolean b) || b);
    }

    private static List<String> strings(Object value) {
        List<String> out = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item != null) {
                    out.add(item.toString());
                }
            }
        }
        return out;
    }
}
