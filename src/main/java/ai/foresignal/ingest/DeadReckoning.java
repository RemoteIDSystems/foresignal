package ai.foresignal.ingest;

import ai.foresignal.source.spi.MotionSpec;
import java.time.Duration;
import org.locationtech.jts.geom.Coordinate;

/**
 * Deviation of a candidate position from where the persisted state predicted it would be
 * (Core-Data-Model §5.2, row 10).
 *
 * <p>With motion: dead reckoning from the persisted centroid along the persisted course at the
 * persisted speed (converted to m/s through {@link MotionSpec.SpeedUnit}) for {@code Δt}, as a
 * great-circle destination; the deviation is the haversine distance from that prediction to the
 * candidate centroid. Without usable motion: the plain haversine distance between the two
 * centroids. Zero when either side has no position.
 *
 * <p>Coordinates are JTS {@link Coordinate}s with x = longitude and y = latitude in degrees. Pure
 * and thread-safe.
 */
public final class DeadReckoning {

    /** The IUGG mean Earth radius, in meters. */
    public static final double EARTH_RADIUS_METERS = 6_371_008.8;

    private DeadReckoning() {}

    /**
     * The deviation in meters.
     *
     * @param persisted the persisted centroid, or null
     * @param speed the persisted speed in {@code unit}, or null when unknown
     * @param unit the speed's unit; knots when null
     * @param courseDegrees the persisted course, degrees clockwise from true north, or null
     * @param elapsed {@code Δt}; negative values count as zero
     * @param candidate the candidate centroid, or null
     */
    public static double deviationMeters(
            Coordinate persisted,
            Double speed,
            MotionSpec.SpeedUnit unit,
            Double courseDegrees,
            Duration elapsed,
            Coordinate candidate) {
        if (persisted == null || candidate == null) {
            return 0.0;
        }
        Coordinate predicted = persisted;
        if (speed != null
                && courseDegrees != null
                && Double.isFinite(speed)
                && Double.isFinite(courseDegrees)
                && elapsed != null) {
            MotionSpec.SpeedUnit effectiveUnit = unit == null ? MotionSpec.SpeedUnit.KNOTS : unit;
            double seconds = Math.max(0.0, elapsed.toNanos() / 1e9);
            double distance = effectiveUnit.toMetersPerSecond(speed) * seconds;
            predicted = destination(persisted, courseDegrees, distance);
        }
        return haversineMeters(predicted, candidate);
    }

    /**
     * The great-circle destination from {@code start} after {@code distanceMeters} on initial
     * bearing {@code bearingDegrees}. The result's longitude is normalized to [-180, 180), so a
     * track across the antimeridian stays in range.
     */
    public static Coordinate destination(Coordinate start, double bearingDegrees, double distanceMeters) {
        double lat1 = Math.toRadians(start.getY());
        double lon1 = Math.toRadians(start.getX());
        double bearing = Math.toRadians(bearingDegrees);
        double angular = distanceMeters / EARTH_RADIUS_METERS;

        double sinLat2 =
                Math.sin(lat1) * Math.cos(angular) + Math.cos(lat1) * Math.sin(angular) * Math.cos(bearing);
        double lat2 = Math.asin(Math.max(-1.0, Math.min(1.0, sinLat2)));
        double lon2 =
                lon1
                        + Math.atan2(
                                Math.sin(bearing) * Math.sin(angular) * Math.cos(lat1),
                                Math.cos(angular) - Math.sin(lat1) * Math.sin(lat2));
        return new Coordinate(normalizeLongitude(Math.toDegrees(lon2)), Math.toDegrees(lat2));
    }

    /** The haversine great-circle distance between two positions, in meters. */
    public static double haversineMeters(Coordinate a, Coordinate b) {
        double lat1 = Math.toRadians(a.getY());
        double lat2 = Math.toRadians(b.getY());
        double dLat = lat2 - lat1;
        double dLon = Math.toRadians(b.getX() - a.getX());
        double h =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_METERS * Math.asin(Math.min(1.0, Math.sqrt(h)));
    }

    /** {@code degrees} wrapped into [-180, 180). */
    static double normalizeLongitude(double degrees) {
        double wrapped = ((degrees + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
        return wrapped;
    }
}
