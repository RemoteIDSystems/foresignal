package ai.foresignal.source.adapters.derived;

import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.support.ReferenceSupport;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/** Geometry helpers for the derived layers (prompt 099). */
public final class GeoBuffers {

    private GeoBuffers() {}

    /** A geodesic circle polygon around a point, SRID 4326. */
    public static Polygon circle(double lon, double lat, double radiusKm) {
        return ReferenceSupport.geodesicCircle(lon, lat, radiusKm, 32);
    }

    /** The distance in km from a geometry's representative point to a position, or NaN. */
    public static double distanceKm(Geometry geometry, double lon, double lat) {
        if (geometry == null || geometry.isEmpty()) {
            return Double.NaN;
        }
        Point point = geometry.getCentroid();
        return Geo.haversineKm(point.getY(), point.getX(), lat, lon);
    }

    /** True when a geometry's representative point is within {@code radiusKm} of a position. */
    public static boolean withinKm(Geometry geometry, double lon, double lat, double radiusKm) {
        double distance = distanceKm(geometry, lon, lat);
        return !Double.isNaN(distance) && distance <= radiusKm;
    }
}
