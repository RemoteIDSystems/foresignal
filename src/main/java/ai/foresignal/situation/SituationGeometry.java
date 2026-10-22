package ai.foresignal.situation;

import ai.foresignal.source.spi.Geo;
import java.util.Collection;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;

/**
 * Situation geometry (prompt 118): the convex hull of the member geometries buffered by {@value #BUFFER_KM} km and
 * simplified to at most {@value #MAX_VERTICES} vertices, its area, and distances to it.
 *
 * <p>Work is done in a local equirectangular projection in kilometers around the hull's center ({@code x =
 * lon·cos(lat₀)·111.32}, {@code y = lat·111.32}), so the buffer is 25 km in every direction at any latitude. The
 * result is deterministic for the same member geometries. Geometries that cross the antimeridian are not split.
 */
public final class SituationGeometry {

    /** The buffer around the hull, km. */
    public static final double BUFFER_KM = 25.0;

    /** The most vertices the stored geometry keeps. */
    public static final int MAX_VERTICES = 500;

    /** Kilometers per degree of latitude. */
    static final double KM_PER_DEGREE = 111.32;

    private SituationGeometry() {}

    /**
     * The situation geometry of {@code members} (null entries skipped), or null when none has a geometry.
     */
    public static Geometry of(Collection<Geometry> members) {
        List<Geometry> present = members.stream().filter(g -> g != null && !g.isEmpty()).toList();
        if (present.isEmpty()) {
            return null;
        }
        GeometryCollection collection = Geo.factory().createGeometryCollection(present.toArray(Geometry[]::new));
        Geometry hull = collection.convexHull();
        double lat0 = hull.getCentroid().getY();
        Geometry projected = project(hull, lat0);
        Geometry buffered = projected.buffer(BUFFER_KM, 8);
        double tolerance = 0.5;
        Geometry simplified = buffered;
        while (simplified.getNumPoints() > MAX_VERTICES) {
            simplified = TopologyPreservingSimplifier.simplify(buffered, tolerance);
            tolerance *= 2;
        }
        Geometry out = unproject(simplified, lat0);
        out.setSRID(Geo.SRID);
        return out;
    }

    /** The area of {@code geometry} in km², or null for null. */
    public static Double areaKm2(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        double lat0 = geometry.getCentroid().getY();
        return round(project(geometry, lat0).getArea(), 1);
    }

    /** The centroid of {@code geometry} (SRID 4326), or null. */
    public static Point centroid(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        Point centroid = geometry.getCentroid();
        centroid.setSRID(Geo.SRID);
        return centroid;
    }

    /**
     * The distance in km between {@code a} and {@code b} (0 when they intersect), or null when either is null.
     * Measured in the local projection around {@code a}'s centroid.
     */
    public static Double distanceKm(Geometry a, Geometry b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return null;
        }
        if (a.intersects(b)) {
            return 0.0;
        }
        double lat0 = a.getCentroid().getY();
        return round(DistanceOp.distance(project(a, lat0), project(b, lat0)), 3);
    }

    /** The bounding box {@code [minLon, minLat, maxLon, maxLat]}, or null. */
    public static List<Double> bbox(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        var envelope = geometry.getEnvelopeInternal();
        return List.of(envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());
    }

    private static Geometry project(Geometry geometry, double lat0) {
        double cos = Math.cos(Math.toRadians(lat0));
        Geometry copy = geometry.copy();
        copy.apply((CoordinateFilter) c -> {
            c.x = c.x * cos * KM_PER_DEGREE;
            c.y = c.y * KM_PER_DEGREE;
        });
        copy.geometryChanged();
        return copy;
    }

    private static Geometry unproject(Geometry geometry, double lat0) {
        double cos = Math.max(1e-6, Math.cos(Math.toRadians(lat0)));
        Geometry copy = geometry.copy();
        copy.apply((CoordinateFilter) c -> {
            c.x = round(c.x / (cos * KM_PER_DEGREE), 6);
            c.y = round(c.y / KM_PER_DEGREE, 6);
        });
        copy.geometryChanged();
        return copy;
    }

    /** {@code value} rounded to {@code places} decimals. */
    static double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    /** The representative point of a geometry (for region lookups), or null. */
    public static Coordinate representative(Geometry geometry) {
        return geometry == null || geometry.isEmpty() ? null : geometry.getInteriorPoint().getCoordinate();
    }
}
