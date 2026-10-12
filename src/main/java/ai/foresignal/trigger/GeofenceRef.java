package ai.foresignal.trigger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A region preset used as a rule geofence (OpenAPI {@code GeofenceRef}, prompt 040): the union of the
 * named countries ({@code ref_country}), first-level regions ({@code ref_admin1}) and US counties
 * ({@code ref_admin2}). {@link RuleSqlBuilder} renders the union as a subquery and
 * {@link TriggerPersistGate} loads the same union through {@link RegionFences}, so the SQL and the gate
 * test the same geometry with no tag lag.
 *
 * @param country ISO2 codes
 * @param admin1 ISO 3166-2 codes
 * @param county FIPS5 codes
 */
public record GeofenceRef(List<String> country, List<String> admin1, List<String> county) {

    /** Copies the lists. */
    public GeofenceRef {
        country = country == null ? List.of() : List.copyOf(country);
        admin1 = admin1 == null ? List.of() : List.copyOf(admin1);
        county = county == null ? List.of() : List.copyOf(county);
    }

    /** True when it names nothing. */
    public boolean isEmpty() {
        return country.isEmpty() && admin1.isEmpty() && county.isEmpty();
    }

    /** A stable text form, for caches. */
    public String canonical() {
        return String.join(",", country.stream().sorted().toList()) + "|" + String.join(",",
                admin1.stream().sorted().toList()) + "|" + String.join(",", county.stream().sorted().toList());
    }

    /** The stored and returned form (only the non-empty members). */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        if (!country.isEmpty()) {
            out.put("country", country);
        }
        if (!admin1.isEmpty()) {
            out.put("admin1", admin1);
        }
        if (!county.isEmpty()) {
            out.put("county", county);
        }
        return out;
    }

    /** The preset of a stored map, or null for null or an empty map. */
    public static GeofenceRef fromMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        GeofenceRef ref = new GeofenceRef(strings(map.get("country")), strings(map.get("admin1")),
                strings(map.get("county")));
        return ref.isEmpty() ? null : ref;
    }

    /**
     * The union of the preset's geometries as a scalar SQL expression (a MultiPolygon, SRID 4326):
     * {@code (SELECT ST_Multi(ST_CollectionExtract(ST_Union(g), 3)) FROM (… UNION ALL …) u)}.
     *
     * @param textArray binds a list of codes and returns its {@code text[]} placeholder
     */
    public String unionSql(Function<List<String>, String> textArray) {
        List<String> parts = new ArrayList<>();
        if (!country.isEmpty()) {
            parts.add("SELECT geom AS g FROM ref_country WHERE iso2 = ANY(" + textArray.apply(country) + ")");
        }
        if (!admin1.isEmpty()) {
            parts.add("SELECT geom AS g FROM ref_admin1 WHERE code = ANY(" + textArray.apply(admin1) + ")");
        }
        if (!county.isEmpty()) {
            parts.add("SELECT geom AS g FROM ref_admin2 WHERE fips5 = ANY(" + textArray.apply(county) + ")");
        }
        if (parts.isEmpty()) {
            return "NULL::geometry";
        }
        return "(SELECT ST_Multi(ST_CollectionExtract(ST_Union(u.g), 3)) FROM (" + String.join(" UNION ALL ", parts)
                + ") u)";
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
