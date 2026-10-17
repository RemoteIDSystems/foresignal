package ai.foresignal.trip;

import ai.foresignal.kinematics.GeoMath;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.distance.DistanceOp;

/**
 * Positions along a trip route in meters (prompt 114): where a hazard or the anchor projects onto the route and how
 * far it is from it. Each segment is measured in a local equirectangular frame at its start, so lengths agree with
 * great-circle distances for the short segments routers produce; the parts of a route split at the antimeridian
 * are measured in order.
 *
 * <p>Ordering hazards here, rather than with {@code ST_LineLocatePoint}, keeps the measure in meters and works on a
 * split (multi-part) route.
 */
public final class RouteMeasure {

    static final GeometryFactory FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private final List<Coordinate[]> parts = new ArrayList<>();
    private final List<double[]> cumulative = new ArrayList<>();
    private final Geometry route;
    private final double length;

    /** Measures {@code route}, a LineString or MultiLineString. */
    public RouteMeasure(Geometry route) {
        this.route = route;
        double total = 0;
        for (int i = 0; i < route.getNumGeometries(); i++) {
            Coordinate[] c = route.getGeometryN(i).getCoordinates();
            double[] cum = new double[c.length];
            cum[0] = total;
            for (int k = 1; k < c.length; k++) {
                total += GeoMath.distanceMeters(c[k - 1].x, c[k - 1].y, c[k].x, c[k].y);
                cum[k] = total;
            }
            parts.add(c);
            cumulative.add(cum);
        }
        this.length = total;
    }

    /** The route length in meters. */
    public double length() {
        return length;
    }

    /**
     * A position relative to the route.
     *
     * @param alongMeters the distance along the route of the projection
     * @param offsetMeters the distance from the route
     */
    public record Located(double alongMeters, double offsetMeters) {}

    /** Projects {@code (lon, lat)} onto the route. */
    public Located locate(double lon, double lat) {
        Located best = null;
        for (int p = 0; p < parts.size(); p++) {
            Coordinate[] c = parts.get(p);
            double[] cum = cumulative.get(p);
            if (c.length == 1) {
                double d = GeoMath.distanceMeters(c[0].x, c[0].y, lon, lat);
                best = better(best, new Located(cum[0], d));
                continue;
            }
            for (int k = 1; k < c.length; k++) {
                double[] ab = GeoMath.offsetMeters(c[k - 1].x, c[k - 1].y, c[k].x, c[k].y);
                double[] ap = GeoMath.offsetMeters(c[k - 1].x, c[k - 1].y, lon, lat);
                double len2 = ab[0] * ab[0] + ab[1] * ab[1];
                double t = len2 <= 0 ? 0 : Math.max(0, Math.min(1, (ap[0] * ab[0] + ap[1] * ab[1]) / len2));
                double ox = ap[0] - t * ab[0];
                double oy = ap[1] - t * ab[1];
                double along = cum[k - 1] + t * (cum[k] - cum[k - 1]);
                best = better(best, new Located(along, Math.hypot(ox, oy)));
            }
        }
        return best == null ? new Located(0, Double.MAX_VALUE) : best;
    }

    /** Projects a geometry: its point, or its nearest point to the route for a line or polygon. */
    public Located locate(Geometry geometry) {
        if (geometry instanceof Point p) {
            return locate(p.getX(), p.getY());
        }
        Coordinate[] nearest = DistanceOp.nearestPoints(route, geometry);
        Located onRoute = locate(nearest[0].x, nearest[0].y);
        double offset = GeoMath.distanceMeters(nearest[0].x, nearest[0].y, nearest[1].x, nearest[1].y);
        return new Located(onRoute.alongMeters(), offset);
    }

    private static Located better(Located a, Located b) {
        return a == null || b.offsetMeters() < a.offsetMeters() ? b : a;
    }

    /**
     * {@code line} split at the antimeridian (prompt 114: long routes): where consecutive vertices are more than
     * 180° of longitude apart the route crosses ±180°, so it is cut at the interpolated crossing and continued on
     * the other side. A route that never crosses comes back as the same single line.
     *
     * @return a LineString, or a MultiLineString when the route crosses
     */
    public static Geometry splitAntimeridian(LineString line) {
        Coordinate[] c = line.getCoordinates();
        List<List<Coordinate>> parts = new ArrayList<>();
        List<Coordinate> current = new ArrayList<>();
        current.add(new Coordinate(c[0]));
        for (int i = 1; i < c.length; i++) {
            Coordinate a = c[i - 1];
            Coordinate b = c[i];
            double dl = b.x - a.x;
            if (Math.abs(dl) > 180) {
                double edge = a.x > 0 ? 180 : -180;
                double bx = b.x + (a.x > 0 ? 360 : -360);
                double t = (edge - a.x) / (bx - a.x);
                double y = a.y + t * (b.y - a.y);
                current.add(new Coordinate(edge, y));
                parts.add(current);
                current = new ArrayList<>();
                current.add(new Coordinate(-edge, y));
            }
            current.add(new Coordinate(b));
        }
        parts.add(current);
        if (parts.size() == 1) {
            return line;
        }
        LineString[] lines = new LineString[parts.size()];
        for (int i = 0; i < parts.size(); i++) {
            lines[i] = FACTORY.createLineString(parts.get(i).toArray(new Coordinate[0]));
        }
        MultiLineString multi = FACTORY.createMultiLineString(lines);
        multi.setSRID(4326);
        return multi;
    }
}
