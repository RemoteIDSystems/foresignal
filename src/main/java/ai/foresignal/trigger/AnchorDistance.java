package ai.foresignal.trigger;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * Java distances between a moving anchor and a target, for the persist gate and the reverse
 * evaluation (prompt 036).
 *
 * <p>The SQL uses {@code ST_DWithin} on geography (the WGS 84 spheroid), which Java does not
 * reproduce exactly. This class therefore answers with a margin: a target whose estimated distance
 * lies within the band around the radius is {@link Evaluation#AMBIGUOUS}, so the gate persists it
 * whenever its geometry changed. Points use the haversine distance (within 0.5% of the spheroid);
 * other geometries use the nearest vertex or edge in a local equirectangular projection (a wider
 * band). With radius 0 the test is "the anchor lies inside the target", which JTS answers exactly,
 * as {@code ST_Intersects} does.
 */
public final class AnchorDistance {

    /** The mean Earth radius in meters. */
    static final double EARTH_RADIUS_M = 6_371_008.8;

    private AnchorDistance() {}

    /**
     * Whether {@code target} is near the anchor at {@code anchor}: {@code MATCH}, {@code NO_MATCH}, or
     * {@code AMBIGUOUS} within the uncertainty band; {@code NO_MATCH} for a missing target geometry.
     */
    public static Evaluation near(NearAnchor rule, Point anchor, Geometry target) {
        if (target == null || target.isEmpty() || anchor == null) {
            return Evaluation.NO_MATCH;
        }
        if (rule.inside()) {
            return Evaluation.of(target.intersects(anchor));
        }
        double radius = rule.radiusMeters();
        double distance = distanceMeters(anchor, target);
        double band = target instanceof Point ? radius * 0.005 + 10 : radius * 0.03 + 100;
        if (Math.abs(distance - radius) <= band) {
            return Evaluation.AMBIGUOUS;
        }
        return Evaluation.of(distance < radius);
    }

    /** The approximate distance in meters from {@code anchor} to the nearest point of {@code target}. */
    public static double distanceMeters(Point anchor, Geometry target) {
        if (target instanceof Point p) {
            return haversine(anchor.getY(), anchor.getX(), p.getY(), p.getX());
        }
        if (target.getDimension() == 2 && target.intersects(anchor)) {
            return 0;
        }
        double lat0 = Math.toRadians(anchor.getY());
        double kx = Math.cos(lat0) * Math.PI / 180 * EARTH_RADIUS_M;
        double ky = Math.PI / 180 * EARTH_RADIUS_M;
        Coordinate[] coordinates = target.getCoordinates();
        double best = Double.MAX_VALUE;
        for (int i = 0; i < coordinates.length; i++) {
            double ax = wrap(coordinates[i].x - anchor.getX()) * kx;
            double ay = (coordinates[i].y - anchor.getY()) * ky;
            best = Math.min(best, Math.hypot(ax, ay));
            if (i + 1 < coordinates.length) {
                double bx = wrap(coordinates[i + 1].x - anchor.getX()) * kx;
                double by = (coordinates[i + 1].y - anchor.getY()) * ky;
                best = Math.min(best, segment(ax, ay, bx, by));
            }
        }
        return best;
    }

    /** The great-circle distance in meters. */
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_M * Math.asin(Math.min(1.0, Math.sqrt(a)));
    }

    private static double wrap(double degrees) {
        double d = degrees;
        while (d > 180) {
            d -= 360;
        }
        while (d < -180) {
            d += 360;
        }
        return d;
    }

    /** Distance from the origin to segment {@code a–b} in the plane. */
    private static double segment(double ax, double ay, double bx, double by) {
        double dx = bx - ax;
        double dy = by - ay;
        double length = dx * dx + dy * dy;
        if (length == 0) {
            return Math.hypot(ax, ay);
        }
        double t = Math.max(0, Math.min(1, -(ax * dx + ay * dy) / length));
        return Math.hypot(ax + t * dx, ay + t * dy);
    }
}
