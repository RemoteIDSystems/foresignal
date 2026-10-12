package ai.foresignal.api.support;

import ai.foresignal.api.error.ValidationException;
import java.util.List;
import org.locationtech.jts.geom.Envelope;

/**
 * Parses {@code bbox=minLon,minLat,maxLon,maxLat} (WGS84, GeoJSON order).
 *
 * <p>Longitudes must lie in [-180, 180], latitudes in [-90, 90], and {@code minLat < maxLat}. When
 * {@code minLon > maxLon} the box crosses the antimeridian and is split into
 * {@code [minLon, 180]} and {@code [-180, maxLon]}. Every malformed value is a
 * {@code 400 VALIDATION_FAILED} on the field {@code bbox}. Shared by records (014), the SSE stream
 * (015) and tiles (016).
 */
public final class BboxParser {

    /** The parameter name used in error messages. */
    public static final String PARAM = "bbox";

    private BboxParser() {}

    /**
     * Parses {@code value}.
     *
     * @param value the raw parameter; null or blank means no filter
     * @return the filter, or null when {@code value} is null or blank
     * @throws ValidationException when the value is malformed or out of range
     */
    public static BboxFilter parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String[] parts = value.split(",", -1);
        if (parts.length != 4) {
            throw invalid("must be minLon,minLat,maxLon,maxLat");
        }
        double minLon = number(parts[0]);
        double minLat = number(parts[1]);
        double maxLon = number(parts[2]);
        double maxLat = number(parts[3]);
        checkLon(minLon);
        checkLon(maxLon);
        checkLat(minLat);
        checkLat(maxLat);
        if (!(minLat < maxLat)) {
            throw invalid("minLat must be less than maxLat");
        }
        if (minLon > maxLon) {
            return new BboxFilter(List.of(
                    new Envelope(minLon, 180.0, minLat, maxLat),
                    new Envelope(-180.0, maxLon, minLat, maxLat)));
        }
        return new BboxFilter(List.of(new Envelope(minLon, maxLon, minLat, maxLat)));
    }

    private static double number(String text) {
        try {
            double value = Double.parseDouble(text.strip());
            if (!Double.isFinite(value)) {
                throw invalid("coordinates must be finite numbers");
            }
            return value;
        } catch (NumberFormatException e) {
            throw invalid("'" + text.strip() + "' is not a number");
        }
    }

    private static void checkLon(double lon) {
        if (lon < -180.0 || lon > 180.0) {
            throw invalid("longitudes must lie in [-180, 180]");
        }
    }

    private static void checkLat(double lat) {
        if (lat < -90.0 || lat > 90.0) {
            throw invalid("latitudes must lie in [-90, 90]");
        }
    }

    private static ValidationException invalid(String message) {
        return ValidationException.forField(PARAM, message);
    }
}
