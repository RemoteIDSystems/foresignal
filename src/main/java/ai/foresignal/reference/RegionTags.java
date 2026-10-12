package ai.foresignal.reference;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The region context of a point or record (OpenAPI {@code RegionTags}, prompt 040). Stored beside the
 * record in {@code record_region}, never in its payload, so it is outside every content hash.
 *
 * @param iso2 the country (ISO 3166-1 alpha-2), or null at sea or when unknown
 * @param admin1 the first-level region (ISO 3166-2, e.g. {@code US-TX}; the Natural Earth
 *     {@code adm1_code} where ISO has none), or null
 * @param admin2 the US county (FIPS5), or null
 * @param sea the sea area name (set only when {@code iso2} is null), or null
 * @param near the nearest gazetteer place, or null
 * @param countryName the country's name, or null
 * @param admin1Name the first-level region's name, or null
 * @param countyName the county's name, or null
 */
public record RegionTags(String iso2, String admin1, String admin2, String sea, RegionNear near, String countryName,
        String admin1Name, String countyName) {

    /** No tags at all. */
    public static final RegionTags EMPTY = new RegionTags(null, null, null, null, null, null, null, null);

    /** True when nothing is known. */
    public boolean isEmpty() {
        return iso2 == null && admin1 == null && admin2 == null && sea == null && near == null;
    }

    /** The {@code near} text in kilometres, or null. */
    public String nearText() {
        return near == null ? null : near.text();
    }

    /**
     * The {@code RegionTags} JSON object; {@code attribution} carries the GeoNames credit whenever
     * {@code near} is present (prompt 040 licensing).
     */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("iso2", iso2);
        out.put("admin1", admin1);
        out.put("admin2", admin2);
        out.put("sea", sea);
        out.put("near", near == null ? null : near.toMap());
        out.put("countryName", countryName);
        out.put("admin1Name", admin1Name);
        out.put("countyName", countyName);
        out.put("attribution", near == null ? null : RegionAttribution.GEONAMES);
        return out;
    }

    /**
     * Tags read back from their JSON object ({@link #toMap()}), as stored in {@code alert.region}; null
     * for null.
     */
    public static RegionTags fromMap(Map<?, ?> map) {
        if (map == null) {
            return null;
        }
        RegionNear near = null;
        if (map.get("near") instanceof Map<?, ?> n && n.get("name") != null) {
            String name = String.valueOf(n.get("name"));
            String text = string(n.get("text"));
            String suffix = null;
            String marker = name + ", ";
            if (text != null && text.contains(marker)) {
                suffix = text.substring(text.lastIndexOf(marker) + marker.length());
            }
            near = new RegionNear(number(n.get("geonameid")) == null ? 0 : number(n.get("geonameid")).longValue(),
                    name, string(n.get("iso2")), string(n.get("admin1")),
                    number(n.get("distanceKm")) == null ? 0 : number(n.get("distanceKm")).doubleValue(),
                    number(n.get("bearingDeg")) == null ? 0 : number(n.get("bearingDeg")).doubleValue(),
                    number(n.get("population")) == null ? null : number(n.get("population")).longValue(), suffix);
        }
        return new RegionTags(string(map.get("iso2")), string(map.get("admin1")), string(map.get("admin2")),
                string(map.get("sea")), near, string(map.get("countryName")), string(map.get("admin1Name")),
                string(map.get("countyName")));
    }

    private static final tools.jackson.databind.json.JsonMapper JSON =
            tools.jackson.databind.json.JsonMapper.builder().build();

    /** Tags read back from JSON text (as stored in {@code alert.region}); null for null, blank or unreadable. */
    public static RegionTags fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return fromMap(JSON.readValue(json, Map.class));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Number number(Object value) {
        if (value instanceof Number n) {
            return n;
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
