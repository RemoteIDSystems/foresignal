package ai.foresignal.cap;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

/**
 * Turns an alert's geometry into a CAP {@code <area>} (prompt 112): a polygon of {@code lat,lon} pairs (a
 * closed ring simplified to at most {@value #MAX_POINTS} points; orientation is irrelevant in CAP) for areal
 * geometry, or a circle {@code lat,lon radiusKm} around a point.
 */
public final class CapAreas {

    /** At most this many points in a CAP polygon (including the closing point). */
    public static final int MAX_POINTS = 100;

    /** The circle radius of a point record without a rule buffer. */
    public static final double DEFAULT_RADIUS_KM = 1.0;

    private CapAreas() {}

    /**
     * The area of {@code geometry}.
     *
     * @param geometry the alert's geometry (WGS 84), or null
     * @param areaDesc the area description, or null for {@code {lat},{lon}} of the geometry's center
     * @param bufferMeters the rule's geofence buffer, or null (then {@value #DEFAULT_RADIUS_KM} km)
     * @return the area, or null when there is no geometry
     */
    public static CapMessage.Area of(Geometry geometry, String areaDesc, Double bufferMeters) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        Point center = geometry.getCentroid();
        String desc = areaDesc != null && !areaDesc.isBlank() ? areaDesc
                : String.format(Locale.ROOT, "%s,%s", CapMessageWriter.coordinate(center.getY()),
                        CapMessageWriter.coordinate(center.getX()));
        if (geometry instanceof Point p) {
            double radiusKm = bufferMeters != null && bufferMeters > 0 ? bufferMeters / 1000.0 : DEFAULT_RADIUS_KM;
            return new CapMessage.Area(desc, null, new double[] {p.getY(), p.getX(), radiusKm});
        }
        Polygon polygon = largestPolygon(geometry);
        if (polygon == null) {
            Geometry hull = geometry.convexHull();
            if (hull instanceof Polygon p) {
                polygon = p;
            } else {
                // a line of two points or a degenerate geometry: a circle around its center
                double radiusKm = Math.max(DEFAULT_RADIUS_KM, halfDiagonalKm(geometry));
                return new CapMessage.Area(desc, null, new double[] {center.getY(), center.getX(), radiusKm});
            }
        }
        return new CapMessage.Area(desc, ring(polygon.getExteriorRing()), null);
    }

    /** The exterior ring as closed {@code [lat, lon]} pairs, simplified to at most {@link #MAX_POINTS}. */
    static List<double[]> ring(LineString ring) {
        Geometry simplified = ring;
        double tolerance = 0.0001;
        while (simplified.getNumPoints() > MAX_POINTS) {
            simplified = DouglasPeuckerSimplifier.simplify(ring, tolerance);
            tolerance *= 2;
        }
        Coordinate[] cs = simplified.getCoordinates();
        List<double[]> out = new ArrayList<>();
        for (Coordinate c : cs) {
            out.add(new double[] {c.y, c.x});
        }
        if (out.size() < 4) {
            // simplification collapsed the ring: fall back to the envelope
            Geometry env = ring.getEnvelope();
            out.clear();
            for (Coordinate c : env.getCoordinates()) {
                out.add(new double[] {c.y, c.x});
            }
        }
        double[] first = out.getFirst();
        double[] last = out.getLast();
        if (first[0] != last[0] || first[1] != last[1]) {
            if (out.size() >= MAX_POINTS) {
                out.removeLast();
            }
            out.add(first.clone());
        }
        return out;
    }

    private static Polygon largestPolygon(Geometry geometry) {
        Polygon best = null;
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry g = geometry.getGeometryN(i);
            Polygon candidate = g instanceof Polygon p ? p : g.getNumGeometries() > 1 ? largestPolygon(g) : null;
            if (candidate != null && (best == null || candidate.getArea() > best.getArea())) {
                best = candidate;
            }
        }
        return best;
    }

    private static double halfDiagonalKm(Geometry geometry) {
        var env = geometry.getEnvelopeInternal();
        double dLat = env.getHeight() * 111.32;
        double dLon = env.getWidth() * 111.32 * Math.cos(Math.toRadians(env.centre().y));
        return Math.sqrt(dLat * dLat + dLon * dLon) / 2;
    }
}
