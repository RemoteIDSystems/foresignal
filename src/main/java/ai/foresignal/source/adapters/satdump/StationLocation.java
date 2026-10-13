package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.spi.PlacePoints;
import java.util.regex.Pattern;

/**
 * A station's {@code location} parameter (prompt 049): {@code lat,lon[,altM]} in WGS84 degrees and
 * meters, or the id of a place (prompt 036), resolved through {@link PlacePoints} whenever it is used,
 * so moving the place moves the station.
 *
 * @param lat the latitude, or null for a place
 * @param lon the longitude, or null for a place
 * @param altitudeM the altitude in meters, or null
 * @param placeId the place id, or null for coordinates
 */
public record StationLocation(Double lat, Double lon, Double altitudeM, String placeId) {

    private static final Pattern PLACE_ID = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    /**
     * Parses the parameter.
     *
     * @throws IllegalArgumentException when it is neither coordinates in range nor a place id
     */
    public static StationLocation parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("must be lat,lon[,altM] or a place id");
        }
        String trimmed = text.trim();
        if (trimmed.contains(",")) {
            String[] parts = trimmed.split(",");
            if (parts.length < 2 || parts.length > 3) {
                throw new IllegalArgumentException("must be lat,lon or lat,lon,altM, but had " + parts.length + " values");
            }
            double lat = number(parts[0]);
            double lon = number(parts[1]);
            Double alt = parts.length == 3 ? number(parts[2]) : null;
            if (lat < -90 || lat > 90) {
                throw new IllegalArgumentException("latitude " + lat + " is outside -90..90");
            }
            if (lon < -180 || lon > 180) {
                throw new IllegalArgumentException("longitude " + lon + " is outside -180..180");
            }
            return new StationLocation(lat, lon, alt, null);
        }
        if (!PLACE_ID.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("'" + trimmed + "' is neither lat,lon[,altM] nor a place id");
        }
        return new StationLocation(null, null, null, trimmed);
    }

    /**
     * The station point as {@code {lon, lat}}, or null when the place is unknown or places are not
     * available.
     */
    public double[] point(PlacePoints places) {
        if (placeId == null) {
            return new double[] {lon, lat};
        }
        if (places == null) {
            return null;
        }
        return places.point(placeId).orElse(null);
    }

    private static double number(String text) {
        try {
            double v = Double.parseDouble(text.trim());
            if (!Double.isFinite(v)) {
                throw new IllegalArgumentException("'" + text.trim() + "' is not a finite number");
            }
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + text.trim() + "' is not a number");
        }
    }
}
