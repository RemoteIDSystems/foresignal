package ai.foresignal.source.formats.cap;

import ai.foresignal.source.spi.UpstreamFormatException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Turns CAP area shapes into JTS geometry in SRID 4326 (prompt 067).
 *
 * <ul>
 *   <li>A {@code polygon} is space-separated {@code lat,lon} pairs; they are swapped to lon/lat, an open
 *       ring is closed, and the polygons of an info block become one MultiPolygon.
 *   <li>A {@code circle} {@code "lat,lon radiusKm"} becomes a {@value #CIRCLE_SEGMENTS}-segment polygon
 *       of points at that great-circle distance from the center (a spherical earth of mean radius
 *       {@value #EARTH_RADIUS_KM} km); radius 0 is a Point.
 *   <li>An area with only geocodes has no geometry (null); the geocodes stay in the payload and prompt
 *       089 resolves boundaries.
 * </ul>
 *
 * <p>When an info block has both polygons and zero-radius circles, the polygons win. The result is not
 * repaired here: the normalizer runs every record geometry through JTS {@code GeometryFixer} and flags a
 * repair as {@code GEOMETRY_FIXED}, which a repair here would hide.
 */
public final class CapGeometry {

    /** Segments of a circle approximated as a polygon. */
    public static final int CIRCLE_SEGMENTS = 64;

    /** The mean earth radius used for circles, in kilometers. */
    public static final double EARTH_RADIUS_KM = 6371.0088;

    /** The geometry factory used for every result (SRID 4326). */
    public static final GeometryFactory FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private CapGeometry() {}

    /**
     * The geometry of every area of {@code info}: a MultiPolygon of the polygons and positive-radius
     * circles; else a Point (one zero-radius circle) or MultiPoint (several); else null.
     *
     * @throws UpstreamFormatException when a polygon or circle does not parse
     */
    public static Geometry of(CapAlert.Info info) {
        return info == null ? null : of(info.areas());
    }

    /**
     * The geometry of {@code areas}, as {@link #of(CapAlert.Info)}.
     *
     * @throws UpstreamFormatException when a polygon or circle does not parse
     */
    public static Geometry of(Collection<CapAlert.Area> areas) {
        List<Polygon> polygons = new ArrayList<>();
        List<Point> points = new ArrayList<>();
        for (CapAlert.Area area : areas) {
            for (String polygon : area.polygons()) {
                polygons.add(polygon(polygon));
            }
            for (String circle : area.circles()) {
                Geometry shape = circle(circle);
                if (shape instanceof Polygon p) {
                    polygons.add(p);
                } else {
                    points.add((Point) shape);
                }
            }
        }
        Geometry out;
        if (!polygons.isEmpty()) {
            out = FACTORY.createMultiPolygon(polygons.toArray(new Polygon[0]));
        } else if (points.size() == 1) {
            out = points.get(0);
        } else if (!points.isEmpty()) {
            out = FACTORY.createMultiPoint(points.toArray(new Point[0]));
        } else {
            return null;
        }
        out.setSRID(FACTORY.getSRID());
        return out;
    }

    /**
     * One CAP polygon as a lon/lat polygon; an open ring is closed.
     *
     * @throws UpstreamFormatException when a pair does not parse, is out of range, or the ring has fewer
     *     than four points
     */
    public static Polygon polygon(String text) {
        String[] pairs = text == null ? new String[0] : text.strip().split("\\s+");
        List<Coordinate> coordinates = new ArrayList<>();
        for (String pair : pairs) {
            if (!pair.isEmpty()) {
                double[] latLon = latLon(pair);
                coordinates.add(new Coordinate(latLon[1], latLon[0]));
            }
        }
        if (!coordinates.isEmpty() && !coordinates.get(0).equals2D(coordinates.get(coordinates.size() - 1))) {
            coordinates.add(new Coordinate(coordinates.get(0)));
        }
        if (coordinates.size() < 4) {
            throw new UpstreamFormatException("A CAP polygon needs at least three distinct points", text);
        }
        try {
            LinearRing ring = FACTORY.createLinearRing(coordinates.toArray(new Coordinate[0]));
            return FACTORY.createPolygon(ring);
        } catch (IllegalArgumentException e) {
            throw new UpstreamFormatException("The CAP polygon is malformed: " + e.getMessage(), text, e);
        }
    }

    /**
     * One CAP circle: a {@value #CIRCLE_SEGMENTS}-segment polygon, or a Point when the radius is 0.
     *
     * @throws UpstreamFormatException when it does not parse or the radius is negative
     */
    public static Geometry circle(String text) {
        String[] parts = text == null ? new String[0] : text.strip().split("\\s+");
        if (parts.length != 2) {
            throw new UpstreamFormatException("A CAP circle is \"lat,lon radiusKm\"", text);
        }
        double[] center = latLon(parts[0]);
        double radiusKm;
        try {
            radiusKm = Double.parseDouble(parts[1]);
        } catch (NumberFormatException e) {
            throw new UpstreamFormatException("The CAP circle radius is not a number", text, e);
        }
        if (!Double.isFinite(radiusKm) || radiusKm < 0) {
            throw new UpstreamFormatException("The CAP circle radius must be a non-negative number", text);
        }
        if (radiusKm == 0) {
            Point point = FACTORY.createPoint(new Coordinate(center[1], center[0]));
            point.setSRID(FACTORY.getSRID());
            return point;
        }
        double lat1 = Math.toRadians(center[0]);
        double lon1 = Math.toRadians(center[1]);
        double angular = radiusKm / EARTH_RADIUS_KM;
        Coordinate[] ring = new Coordinate[CIRCLE_SEGMENTS + 1];
        for (int i = 0; i < CIRCLE_SEGMENTS; i++) {
            double bearing = 2 * Math.PI * i / CIRCLE_SEGMENTS;
            double lat2 = Math.asin(Math.sin(lat1) * Math.cos(angular)
                    + Math.cos(lat1) * Math.sin(angular) * Math.cos(bearing));
            double lon2 = lon1 + Math.atan2(Math.sin(bearing) * Math.sin(angular) * Math.cos(lat1),
                    Math.cos(angular) - Math.sin(lat1) * Math.sin(lat2));
            double lonDeg = ((Math.toDegrees(lon2) + 540) % 360) - 180;
            ring[i] = new Coordinate(lonDeg, Math.toDegrees(lat2));
        }
        ring[CIRCLE_SEGMENTS] = new Coordinate(ring[0]);
        Polygon polygon = FACTORY.createPolygon(ring);
        polygon.setSRID(FACTORY.getSRID());
        return polygon;
    }

    /**
     * A {@code "lat,lon"} pair, range-checked.
     *
     * @return {@code {lat, lon}}
     * @throws UpstreamFormatException when it does not parse or is out of range
     */
    public static double[] latLon(String pair) {
        String[] parts = pair == null ? new String[0] : pair.strip().split(",");
        if (parts.length != 2) {
            throw new UpstreamFormatException("'" + pair + "' is not a lat,lon pair", pair);
        }
        try {
            double lat = Double.parseDouble(parts[0].strip());
            double lon = Double.parseDouble(parts[1].strip());
            if (!Double.isFinite(lat) || !Double.isFinite(lon) || lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                throw new UpstreamFormatException("'" + pair + "' is out of range", pair);
            }
            return new double[] {lat, lon};
        } catch (NumberFormatException e) {
            throw new UpstreamFormatException("'" + pair + "' is not a lat,lon pair", pair, e);
        }
    }
}
