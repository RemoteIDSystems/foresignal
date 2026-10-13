package ai.foresignal.feed;

import java.util.Locale;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * GeoRSS-Simple for Atom entries (prompt 058; namespace {@value #NAMESPACE}): a point is
 * {@code georss:point "lat lon"}; a single polygon (a Polygon, or a MultiPolygon of one part) whose
 * outer ring has at most {@value #MAX_POLYGON_VERTICES} vertices is {@code georss:polygon} with its outer
 * ring (closed, so its first point repeats) as {@code "lat lon lat lon …"}; anything else is the centroid as a point.
 *
 * @param element {@code point} or {@code polygon}
 * @param value the element text
 */
public record GeoRss(String element, String value) {

    /** The GeoRSS namespace. */
    public static final String NAMESPACE = "http://www.georss.org/georss";

    /** The most outer-ring vertices written as a polygon. */
    public static final int MAX_POLYGON_VERTICES = 500;

    /** The GeoRSS form of {@code geometry}, or null for a null or empty geometry. */
    public static GeoRss of(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        if (geometry instanceof Point p) {
            return point(p.getCoordinate());
        }
        Polygon polygon = null;
        if (geometry instanceof Polygon single) {
            polygon = single;
        } else if (geometry instanceof MultiPolygon multi && multi.getNumGeometries() == 1) {
            polygon = (Polygon) multi.getGeometryN(0);
        }
        if (polygon != null && polygon.getExteriorRing().getNumPoints() - 1 <= MAX_POLYGON_VERTICES) {
            StringBuilder out = new StringBuilder();
            for (Coordinate c : polygon.getExteriorRing().getCoordinates()) {
                if (!out.isEmpty()) {
                    out.append(' ');
                }
                out.append(number(c.y)).append(' ').append(number(c.x));
            }
            return new GeoRss("polygon", out.toString());
        }
        Point centroid = geometry.getCentroid();
        return centroid == null || centroid.isEmpty() ? null : point(centroid.getCoordinate());
    }

    private static GeoRss point(Coordinate c) {
        return new GeoRss("point", number(c.y) + " " + number(c.x));
    }

    /** A coordinate with at most seven decimals and no exponent. */
    static String number(double value) {
        String text = String.format(Locale.ROOT, "%.7f", value);
        text = text.contains(".") ? text.replaceAll("0+$", "").replaceAll("\\.$", "") : text;
        return "-0".equals(text) ? "0" : text;
    }
}
