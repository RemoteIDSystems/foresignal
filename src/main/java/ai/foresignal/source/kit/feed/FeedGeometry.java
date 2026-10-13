package ai.foresignal.source.kit.feed;

import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

/**
 * The geometry of a feed item (prompt 073), in SRID 4326.
 *
 * <ul>
 *   <li><strong>GeoRSS Simple:</strong> {@code georss:point}, {@code georss:line}, {@code georss:polygon} and
 *       {@code georss:box}, coordinates in "lat lon" order;
 *   <li><strong>GeoRSS GML</strong> (inside {@code georss:where}, or directly): {@code gml:Point/gml:pos},
 *       {@code gml:LineString/gml:posList}, {@code gml:Polygon/gml:exterior/gml:LinearRing/gml:posList} (interior
 *       rings kept), lat-lon order unless {@code srsName} names CRS84;
 *   <li><strong>W3C Basic Geo:</strong> {@code geo:lat} and {@code geo:long}, directly or inside {@code geo:Point}.
 * </ul>
 * The first form found wins. A geometry outside WGS 84 ranges, with too few positions or more than
 * {@value #MAX_POSITIONS} positions is left out with a warning; the item is kept.
 */
final class FeedGeometry {

    /** GeoRSS. */
    static final String GEORSS = "http://www.georss.org/georss";

    /** GML 3 (the namespace GeoRSS GML uses). */
    static final String GML = "http://www.opengis.net/gml";

    /** W3C Basic Geo (WGS84 Geo Positioning). */
    static final String GEO = "http://www.w3.org/2003/01/geo/wgs84_pos#";

    /** The most positions read. */
    static final int MAX_POSITIONS = 10_000;

    private static final GeometryFactory FACTORY = Geo.factory();

    private FeedGeometry() {}

    /**
     * The geometry of an item element, or null.
     *
     * @param item the item or entry element
     * @param warnings receives a warning when a geometry is present but unusable
     */
    static Geometry of(XmlTree.Node item, List<String> warnings) {
        try {
            Geometry simple = simple(item);
            if (simple != null) {
                return simple;
            }
            XmlTree.Node where = item.child(GEORSS, "where");
            Geometry gml = gml(where == null ? item : where);
            if (gml != null) {
                return gml;
            }
            return basicGeo(item);
        } catch (UpstreamFormatException | IllegalArgumentException e) {
            if (warnings != null && warnings.size() < 50) {
                warnings.add("geometry left out: " + e.getMessage());
            }
            return null;
        }
    }

    private static Geometry simple(XmlTree.Node item) {
        String point = item.childText(GEORSS, "point");
        if (point != null) {
            double[] c = numbers(point);
            require(c.length == 2, "georss:point needs two numbers");
            return Geo.point(c[1], c[0]);
        }
        String line = item.childText(GEORSS, "line");
        if (line != null) {
            Coordinate[] cs = latLonPairs(numbers(line), false);
            require(cs.length >= 2, "georss:line needs two positions");
            return srid(FACTORY.createLineString(cs));
        }
        String polygon = item.childText(GEORSS, "polygon");
        if (polygon != null) {
            Coordinate[] cs = closed(latLonPairs(numbers(polygon), false));
            require(cs.length >= 4, "georss:polygon needs four positions");
            return srid(FACTORY.createPolygon(cs));
        }
        String box = item.childText(GEORSS, "box");
        if (box != null) {
            double[] c = numbers(box);
            require(c.length == 4, "georss:box needs four numbers");
            double south = c[0];
            double west = c[1];
            double north = c[2];
            double east = c[3];
            check(west, south);
            check(east, north);
            require(south <= north, "georss:box lower corner is above its upper corner");
            return srid(FACTORY.createPolygon(new Coordinate[] {new Coordinate(west, south),
                    new Coordinate(east, south), new Coordinate(east, north), new Coordinate(west, north),
                    new Coordinate(west, south)}));
        }
        return null;
    }

    private static Geometry gml(XmlTree.Node parent) {
        XmlTree.Node point = parent.child(GML, "Point");
        if (point != null) {
            boolean lonLat = crs84(point);
            String pos = point.childText(GML, "pos");
            require(pos != null, "gml:Point has no gml:pos");
            double[] c = numbers(pos);
            require(c.length >= 2, "gml:pos needs two numbers");
            return lonLat ? Geo.point(c[0], c[1]) : Geo.point(c[1], c[0]);
        }
        XmlTree.Node line = parent.child(GML, "LineString");
        if (line != null) {
            String list = line.childText(GML, "posList");
            require(list != null, "gml:LineString has no gml:posList");
            Coordinate[] cs = latLonPairs(numbers(list), crs84(line));
            require(cs.length >= 2, "gml:LineString needs two positions");
            return srid(FACTORY.createLineString(cs));
        }
        XmlTree.Node polygon = parent.child(GML, "Polygon");
        if (polygon != null) {
            boolean lonLat = crs84(polygon);
            Coordinate[] shell = ring(polygon.child(GML, "exterior"), lonLat);
            require(shell != null, "gml:Polygon has no exterior ring");
            List<org.locationtech.jts.geom.LinearRing> holes = new ArrayList<>();
            for (XmlTree.Node interior : polygon.all(GML, "interior")) {
                Coordinate[] hole = ring(interior, lonLat);
                if (hole != null) {
                    holes.add(FACTORY.createLinearRing(hole));
                }
            }
            return srid(FACTORY.createPolygon(FACTORY.createLinearRing(shell),
                    holes.toArray(new org.locationtech.jts.geom.LinearRing[0])));
        }
        return null;
    }

    private static Coordinate[] ring(XmlTree.Node boundary, boolean lonLat) {
        if (boundary == null) {
            return null;
        }
        XmlTree.Node ring = boundary.child(GML, "LinearRing");
        if (ring == null) {
            return null;
        }
        String list = ring.childText(GML, "posList");
        require(list != null, "gml:LinearRing has no gml:posList");
        Coordinate[] cs = closed(latLonPairs(numbers(list), lonLat));
        require(cs.length >= 4, "gml:LinearRing needs four positions");
        return cs;
    }

    private static Geometry basicGeo(XmlTree.Node item) {
        XmlTree.Node holder = item.child(GEO, "Point") != null ? item.child(GEO, "Point") : item;
        String lat = holder.childText(GEO, "lat");
        String lon = holder.childText(GEO, "long");
        if (lat == null || lon == null) {
            return null;
        }
        try {
            return Geo.point(Double.parseDouble(lon), Double.parseDouble(lat));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("geo:lat/geo:long are not numbers");
        }
    }

    private static boolean crs84(XmlTree.Node node) {
        String srs = node.attr("srsName");
        return srs != null && srs.toUpperCase(Locale.ROOT).contains("CRS84");
    }

    private static double[] numbers(String text) {
        String[] parts = text.strip().split("[\\s,]+");
        require(parts.length <= MAX_POSITIONS * 2, "more than " + MAX_POSITIONS + " positions");
        double[] out = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                out[i] = Double.parseDouble(parts[i]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("'" + parts[i] + "' is not a number");
            }
            require(Double.isFinite(out[i]), "a coordinate is not finite");
        }
        return out;
    }

    private static Coordinate[] latLonPairs(double[] values, boolean lonLat) {
        require(values.length % 2 == 0, "an odd number of coordinates");
        Coordinate[] out = new Coordinate[values.length / 2];
        for (int i = 0; i < out.length; i++) {
            double a = values[2 * i];
            double b = values[2 * i + 1];
            double lon = lonLat ? a : b;
            double lat = lonLat ? b : a;
            check(lon, lat);
            out[i] = new Coordinate(lon, lat);
        }
        return out;
    }

    private static Coordinate[] closed(Coordinate[] cs) {
        if (cs.length == 0 || cs[0].equals2D(cs[cs.length - 1])) {
            return cs;
        }
        Coordinate[] out = java.util.Arrays.copyOf(cs, cs.length + 1);
        out[cs.length] = new Coordinate(cs[0]);
        return out;
    }

    private static void check(double lon, double lat) {
        require(lon >= -180 && lon <= 180 && lat >= -90 && lat <= 90,
                "position (" + lat + " " + lon + ") is outside WGS 84");
    }

    private static Geometry srid(Geometry g) {
        g.setSRID(Geo.SRID);
        return g;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
