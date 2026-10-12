package ai.foresignal.reference;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The nearest gazetteer place of a point (OpenAPI {@code RegionNear}, prompt 040). GeoNames data,
 * CC BY 4.0: every response that carries a {@code near} also carries {@link RegionAttribution#GEONAMES}.
 *
 * @param geonameid the GeoNames id
 * @param name the place name
 * @param iso2 the place's country, or null
 * @param admin1 the place's first-level region as ISO 3166-2 when known (else the GeoNames code), or null
 * @param distanceKm the distance from the place to the point, in kilometres
 * @param bearingDeg the bearing from the place to the point, degrees clockwise from north
 * @param population the place's population, or null
 * @param suffix the text after the name in {@link #text}: the US state code, else the country name; or
 *     null
 */
public record RegionNear(long geonameid, String name, String iso2, String admin1, double distanceKm,
        double bearingDeg, Long population, String suffix) {

    /** Kilometres to statute miles. */
    public static final double MILES_PER_KM = 0.621371192;

    /** The 16 compass points, starting at north. */
    public static final List<String> COMPASS16 = List.of("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S",
            "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW");

    /** The 16-point compass name of {@code bearingDeg}. */
    public static String compass16(double bearingDeg) {
        double normalized = ((bearingDeg % 360) + 360) % 360;
        int index = (int) Math.floor((normalized + 11.25) / 22.5) % 16;
        return COMPASS16.get(index);
    }

    /** {@link #compass16(double)} of this place's bearing. */
    public String compass() {
        return compass16(bearingDeg);
    }

    /** The distance in statute miles. */
    public double distanceMi() {
        return distanceKm * MILES_PER_KM;
    }

    /** {@code "12 km NNE of Austin, TX"}, in kilometres. */
    public String text() {
        return text(false);
    }

    /**
     * {@code "12 km NNE of Austin, TX"} ({@code "7 mi NNE of Austin, TX"} when {@code imperial});
     * {@code "in Austin, TX"} within one unit of the place.
     */
    public String text(boolean imperial) {
        double distance = imperial ? distanceMi() : distanceKm;
        String place = suffix == null || suffix.isBlank() ? name : name + ", " + suffix;
        if (distance < 1) {
            return "in " + place;
        }
        return formatDistance(distance) + (imperial ? " mi " : " km ") + compass() + " of " + place;
    }

    /** A distance rounded to a whole number. */
    static String formatDistance(double value) {
        return String.format(Locale.ROOT, "%d", Math.round(value));
    }

    /** The {@code RegionNear} JSON object. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("geonameid", geonameid);
        out.put("name", name);
        out.put("iso2", iso2);
        out.put("admin1", admin1);
        out.put("distanceKm", Math.round(distanceKm * 10) / 10.0);
        out.put("bearingDeg", Math.round(bearingDeg * 10) / 10.0);
        out.put("compass16", compass());
        out.put("population", population);
        out.put("text", text());
        return out;
    }
}
