package ai.foresignal.space;

/**
 * A ground observer for pass prediction (prompt 064): WGS84 geodetic latitude and longitude in degrees and
 * the height above the ellipsoid in meters.
 *
 * @param lat latitude, −90 to 90
 * @param lon longitude, −180 to 180
 * @param altM height above the WGS84 ellipsoid, meters
 */
public record Observer(double lat, double lon, double altM) {

    /** Checks the ranges. */
    public Observer {
        if (!(lat >= -90 && lat <= 90) || !(lon >= -180 && lon <= 180) || !Double.isFinite(altM)) {
            throw new IllegalArgumentException("observer out of range: " + lat + "," + lon + "," + altM);
        }
    }
}
