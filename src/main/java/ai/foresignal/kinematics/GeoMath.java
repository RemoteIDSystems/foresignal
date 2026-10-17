package ai.foresignal.kinematics;

/**
 * Spherical helpers shared by the kinematics, trip and pattern-of-life code (prompt 114): great-circle
 * distance, initial bearing, and the local equirectangular frame CPA works in.
 */
public final class GeoMath {

    /** The mean Earth radius in meters (IUGG). */
    public static final double EARTH_RADIUS_M = 6_371_008.8;

    /** Meters per degree of latitude in the local equirectangular frame. */
    public static final double METERS_PER_DEGREE_LAT = 110_574.0;

    /** Meters per degree of longitude at the equator in the local equirectangular frame. */
    public static final double METERS_PER_DEGREE_LON = 111_320.0;

    private GeoMath() {}

    /**
     * The great-circle (haversine) distance between two positions.
     *
     * @return meters
     */
    public static double distanceMeters(double lon1, double lat1, double lon2, double lat2) {
        double p1 = Math.toRadians(lat1);
        double p2 = Math.toRadians(lat2);
        double dp = p2 - p1;
        double dl = Math.toRadians(normalizeLon(lon2 - lon1));
        double a = Math.sin(dp / 2) * Math.sin(dp / 2) + Math.cos(p1) * Math.cos(p2) * Math.sin(dl / 2) * Math.sin(dl / 2);
        return 2 * EARTH_RADIUS_M * Math.asin(Math.min(1, Math.sqrt(a)));
    }

    /**
     * The initial great-circle bearing from the first position to the second.
     *
     * @return degrees clockwise from true north, in {@code [0, 360)}
     */
    public static double bearingDegrees(double lon1, double lat1, double lon2, double lat2) {
        double p1 = Math.toRadians(lat1);
        double p2 = Math.toRadians(lat2);
        double dl = Math.toRadians(normalizeLon(lon2 - lon1));
        double y = Math.sin(dl) * Math.cos(p2);
        double x = Math.cos(p1) * Math.sin(p2) - Math.sin(p1) * Math.cos(p2) * Math.cos(dl);
        return normalizeDegrees(Math.toDegrees(Math.atan2(y, x)));
    }

    /** {@code degrees} folded into {@code [0, 360)}. */
    public static double normalizeDegrees(double degrees) {
        double d = degrees % 360.0;
        return d < 0 ? d + 360.0 : d;
    }

    /** A longitude difference folded into {@code [-180, 180]}, so a pair across the antimeridian is near. */
    public static double normalizeLon(double deltaLon) {
        double d = deltaLon % 360.0;
        if (d > 180) {
            d -= 360;
        } else if (d < -180) {
            d += 360;
        }
        return d;
    }

    /** The absolute difference of two angles in degrees, in {@code [0, 180]}. */
    public static double angleDifference(double a, double b) {
        double d = Math.abs(normalizeDegrees(a) - normalizeDegrees(b));
        return d > 180 ? 360 - d : d;
    }

    /**
     * The offset of {@code (lon2, lat2)} from {@code (lon1, lat1)} in a local equirectangular frame
     * centered on their midpoint latitude (099's {@code derived-cpa} frame).
     *
     * @return {@code {east, north}} in meters
     */
    public static double[] offsetMeters(double lon1, double lat1, double lon2, double lat2) {
        double mid = Math.toRadians((lat1 + lat2) / 2);
        double east = normalizeLon(lon2 - lon1) * METERS_PER_DEGREE_LON * Math.cos(mid);
        double north = (lat2 - lat1) * METERS_PER_DEGREE_LAT;
        return new double[] {east, north};
    }

    /**
     * A velocity vector from a speed and a course.
     *
     * @param speed the speed in any unit
     * @param courseDegrees the course, degrees clockwise from true north
     * @return {@code {east, north}} in the speed's unit
     */
    public static double[] velocity(double speed, double courseDegrees) {
        double c = Math.toRadians(courseDegrees);
        return new double[] {speed * Math.sin(c), speed * Math.cos(c)};
    }
}
