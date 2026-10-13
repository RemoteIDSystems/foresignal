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
 * <p>Prompt 036 adds {@code placeIds} (the places' areas bound the scope and the digest heading names
 * them) and {@code viewId} (a saved view's predicate). Prompt 040 adds {@code country}, {@code admin1}
 * and {@code county} (region filters, matched through {@code record_region} and the alert region
 * snapshot). Prompt 056 adds {@code places} (places of the "Weather at places" section; empty for none) and
 * {@code weatherProducts} (the products shown there; empty for the products backing {@code place-weather}).
 *
 * @param sources source ids
 * @param kinds kinds
 * @param bbox {@code minLon,minLat,maxLon,maxLat}, or null
 * @param ruleIds trigger rules whose alerts the briefing reports; empty for all rules in scope
 * @param includeHealth whether the source health section is included (default true)
 * @param placeIds places whose areas bound the scope (prompt 036); empty for none
 * @param viewId a saved view id or slug (prompt 036), or null
 * @param country ISO2 codes (prompt 040); empty for any
 * @param admin1 ISO 3166-2 codes (prompt 040); empty for any
 * @param county US county FIPS5 codes (prompt 040); empty for any
 * @param places places of the "Weather at places" section (prompt 056); empty for none
 * @param weatherProducts products of that section (prompt 056); empty for the default set
 */
public record BriefingScope(List<String> sources, List<String> kinds, String bbox, List<UUID> ruleIds,
        boolean includeHealth, List<String> placeIds, String viewId, List<String> country, List<String> admin1,
        List<String> county, List<String> places, List<String> weatherProducts) {

    /** A scope without the weather section (the shape before prompt 056). */
    public BriefingScope(List<String> sources, List<String> kinds, String bbox, List<UUID> ruleIds,
            boolean includeHealth, List<String> placeIds, String viewId, List<String> country, List<String> admin1,
            List<String> county) {
        this(sources, kinds, bbox, ruleIds, includeHealth, placeIds, viewId, country, admin1, county, List.of(),
                List.of());
    }

    /** A scope without region filters (the shape before prompt 040). */
    public BriefingScope(List<String> sources, List<String> kinds, String bbox, List<UUID> ruleIds,
            boolean includeHealth, List<String> placeIds, String viewId) {
        this(sources, kinds, bbox, ruleIds, includeHealth, placeIds, viewId, List.of(), List.of(), List.of());
    }

    /** A scope without places or a view. */
    public BriefingScope(List<String> sources, List<String> kinds, String bbox, List<UUID> ruleIds,
            boolean includeHealth) {
        this(sources, kinds, bbox, ruleIds, includeHealth, List.of(), null);
    }

    /** Copies the lists. */
    public BriefingScope {
        sources = sources == null ? List.of() : List.copyOf(sources);
        kinds = kinds == null ? List.of() : List.copyOf(kinds);
        ruleIds = ruleIds == null ? List.of() : List.copyOf(ruleIds);
        bbox = bbox == null || bbox.isBlank() ? null : bbox.strip();
        placeIds = placeIds == null ? List.of() : List.copyOf(placeIds);
        viewId = viewId == null || viewId.isBlank() ? null : viewId.strip();
        country = country == null ? List.of() : List.copyOf(country);
        admin1 = admin1 == null ? List.of() : List.copyOf(admin1);
        county = county == null ? List.of() : List.copyOf(county);
        places = places == null ? List.of() : List.copyOf(places);
        weatherProducts = weatherProducts == null ? List.of() : List.copyOf(weatherProducts);
    }

    /** The scope's region filters (prompt 040), or null when it has none. */
    public ai.foresignal.reference.RegionFilter regionFilter() {
        return ai.foresignal.reference.RegionFilter.of(country, admin1, county);
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
        out.put("placeIds", placeIds);
        out.put("viewId", viewId);
        out.put("country", country);
        out.put("admin1", admin1);
        out.put("county", county);
        out.put("places", places);
        out.put("weatherProducts", weatherProducts);
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
        Object view = map.get("viewId");
        return new BriefingScope(strings(map.get("sources")), strings(map.get("kinds")),
                bbox == null ? null : bbox.toString(), rules, !(health instanceof Boolean b) || b,
                strings(map.get("placeIds")), view == null ? null : view.toString(), strings(map.get("country")),
                strings(map.get("admin1")), strings(map.get("county")), strings(map.get("places")),
                strings(map.get("weatherProducts")));
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
