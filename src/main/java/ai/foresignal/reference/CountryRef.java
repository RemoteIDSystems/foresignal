package ai.foresignal.reference;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One row of the bundled country table (OpenAPI {@code CountryRef}, prompt 040).
 *
 * @param iso2 the ISO 3166-1 alpha-2 code (Natural Earth {@code ISO_A2_EH}; never {@code -99})
 * @param iso3 the ISO 3166-1 alpha-3 code ({@code ISO_A3_EH})
 * @param isoNumeric the ISO 3166-1 numeric code, or null
 * @param fips the FIPS 10-4 code ({@code FIPS_10}, else GeoNames), or null
 * @param name the short name
 * @param nameLong the long name, or null
 * @param continent the continent, or null
 * @param labelLon the label point's longitude (Natural Earth {@code LABEL_X}; always inside the country)
 * @param labelLat the label point's latitude ({@code LABEL_Y})
 * @param aliases other names: the Natural Earth names, the GeoNames {@code countryInfo} name and common
 *     variants
 */
public record CountryRef(String iso2, String iso3, String isoNumeric, String fips, String name, String nameLong,
        String continent, double labelLon, double labelLat, List<String> aliases) {

    /** Copies the aliases. */
    public CountryRef {
        aliases = aliases == null ? List.of() : List.copyOf(aliases);
    }

    /** The {@code CountryRef} JSON object. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("iso2", iso2);
        out.put("iso3", iso3);
        out.put("isoNumeric", isoNumeric);
        out.put("fips", fips);
        out.put("name", name);
        out.put("nameLong", nameLong);
        out.put("continent", continent);
        out.put("labelLon", labelLon);
        out.put("labelLat", labelLat);
        out.put("aliases", aliases);
        return out;
    }
}
