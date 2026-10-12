package ai.foresignal.api.support;

import ai.foresignal.api.error.ValidationException;

/**
 * Parses {@code near=lon,lat} (GeoJSON order, like {@code bbox}) and {@code radiusKm}.
 *
 * <p>{@code radiusKm} is required with {@code near} and must lie in (0, 2000]; {@code radiusKm}
 * without {@code near} is rejected too. Errors are {@code 400 VALIDATION_FAILED}.
 */
public final class NearParser {

    /** The largest radius accepted. */
    public static final double MAX_RADIUS_KM = 2000.0;

    private NearParser() {}

    /**
     * Parses the pair.
     *
     * @param near the raw {@code near}, or null
     * @param radiusKm the raw {@code radiusKm}, or null
     * @return the filter, or null when both are absent
     */
    public static NearFilter parse(String near, String radiusKm) {
        boolean hasNear = near != null && !near.isBlank();
        boolean hasRadius = radiusKm != null && !radiusKm.isBlank();
        if (!hasNear && !hasRadius) {
            return null;
        }
        if (!hasNear) {
            throw ValidationException.forField("near", "radiusKm needs near=lon,lat");
        }
        if (!hasRadius) {
            throw ValidationException.forField("radiusKm", "radiusKm is required with near");
        }
        String[] parts = near.split(",", -1);
        if (parts.length != 2) {
            throw ValidationException.forField("near", "must be lon,lat");
        }
        double lon = number("near", parts[0]);
        double lat = number("near", parts[1]);
        if (lon < -180.0 || lon > 180.0) {
            throw ValidationException.forField("near", "longitude must lie in [-180, 180]");
        }
        if (lat < -90.0 || lat > 90.0) {
            throw ValidationException.forField("near", "latitude must lie in [-90, 90]");
        }
        double radius = number("radiusKm", radiusKm);
        if (!(radius > 0.0) || radius > MAX_RADIUS_KM) {
            throw ValidationException.forField("radiusKm", "must be greater than 0 and at most 2000");
        }
        return new NearFilter(lon, lat, radius);
    }

    private static double number(String field, String text) {
        try {
            double value = Double.parseDouble(text.strip());
            if (!Double.isFinite(value)) {
                throw ValidationException.forField(field, "must be a finite number");
            }
            return value;
        } catch (NumberFormatException e) {
            throw ValidationException.forField(field, "'" + text.strip() + "' is not a number");
        }
    }
}
