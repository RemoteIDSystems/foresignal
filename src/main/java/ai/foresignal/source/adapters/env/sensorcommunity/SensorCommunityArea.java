package ai.foresignal.source.adapters.env.sensorcommunity;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@code area} parameter of {@code sensor-community} (prompt 078): one filter of Sensor.Community's
 * {@code /airrohr/v1/filter/{query}} API (the "EN-APIs" wiki page of opendata-stuttgart/meta, read 2026-10-13).
 *
 * <ul>
 *   <li>{@code box=lat1,lon1,lat2,lon2}: corners in either order, each side at most {@value #MAX_BOX_DEGREES}°;
 *   <li>{@code area=lat,lon,radiusKm}: radius above 0 and at most {@value #MAX_RADIUS_KM} km;
 *   <li>{@code country=XX}: one ISO 3166-1 alpha-2 code.
 * </ul>
 * Pure; {@link #check} returns the problem or null, {@link #path} the normalized filter path.
 */
public final class SensorCommunityArea {

    /** The widest box side, degrees. */
    public static final double MAX_BOX_DEGREES = 10;

    /** The largest radius, km. */
    public static final double MAX_RADIUS_KM = 500;

    private static final String NUMBER = "\\s*(-?\\d+(?:\\.\\d+)?)\\s*";

    private static final Pattern BOX = Pattern.compile("(?i)box\\s*=" + NUMBER + "," + NUMBER + "," + NUMBER + ","
            + NUMBER);

    private static final Pattern AREA = Pattern.compile("(?i)area\\s*=" + NUMBER + "," + NUMBER + "," + NUMBER);

    private static final Pattern COUNTRY = Pattern.compile("(?i)country\\s*=\\s*([A-Za-z]{2})\\s*");

    private SensorCommunityArea() {}

    /**
     * What is wrong with {@code value}, or null when it is a valid filter.
     *
     * @param value the parameter value
     */
    public static String check(String value) {
        if (value == null || value.isBlank()) {
            return "is required: box=lat1,lon1,lat2,lon2, area=lat,lon,radiusKm or country=XX";
        }
        String v = value.strip();
        Matcher m = BOX.matcher(v);
        if (m.matches()) {
            double lat1 = Double.parseDouble(m.group(1));
            double lon1 = Double.parseDouble(m.group(2));
            double lat2 = Double.parseDouble(m.group(3));
            double lon2 = Double.parseDouble(m.group(4));
            if (!lat(lat1) || !lat(lat2) || !lon(lon1) || !lon(lon2)) {
                return "box corners must be latitudes -90..90 and longitudes -180..180";
            }
            if (Math.abs(lat2 - lat1) > MAX_BOX_DEGREES || Math.abs(lon2 - lon1) > MAX_BOX_DEGREES) {
                return "the box may span at most " + (int) MAX_BOX_DEGREES + "° of latitude and of longitude";
            }
            if (lat1 == lat2 || lon1 == lon2) {
                return "the box must have an area";
            }
            return null;
        }
        m = AREA.matcher(v);
        if (m.matches()) {
            double lat = Double.parseDouble(m.group(1));
            double lon = Double.parseDouble(m.group(2));
            double radius = Double.parseDouble(m.group(3));
            if (!lat(lat) || !lon(lon)) {
                return "the center must be a latitude -90..90 and a longitude -180..180";
            }
            if (radius <= 0 || radius > MAX_RADIUS_KM) {
                return "the radius must be above 0 and at most " + (int) MAX_RADIUS_KM + " km";
            }
            return null;
        }
        if (COUNTRY.matcher(v).matches()) {
            return null;
        }
        return "must be box=lat1,lon1,lat2,lon2, area=lat,lon,radiusKm or country=XX, but was '" + v + "'";
    }

    /**
     * The filter path segment of a valid value: spaces removed, the filter name and country code normalized.
     *
     * @throws IllegalArgumentException when {@link #check} finds a problem
     */
    public static String path(String value) {
        String problem = check(value);
        if (problem != null) {
            throw new IllegalArgumentException("area " + problem);
        }
        String v = value.strip().replaceAll("\\s+", "");
        int eq = v.indexOf('=');
        String name = v.substring(0, eq).toLowerCase(Locale.ROOT);
        String rest = v.substring(eq + 1);
        return name + "=" + (name.equals("country") ? rest.toUpperCase(Locale.ROOT) : rest);
    }

    private static boolean lat(double v) {
        return v >= -90 && v <= 90;
    }

    private static boolean lon(double v) {
        return v >= -180 && v <= 180;
    }
}
