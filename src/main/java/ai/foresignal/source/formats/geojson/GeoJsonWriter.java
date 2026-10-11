package ai.foresignal.source.formats.geojson;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * Writes a JTS geometry as a GeoJSON geometry object (RFC 7946), as plain maps and lists that
 * Jackson serializes directly.
 *
 * <p>The counterpart of {@link GeoJsonReader}, and for the same reason Foresignal's own: JTS's
 * {@code GeoJsonWriter} lives in {@code jts-io-common}, which the build deliberately does not
 * depend on (Conventions, "GeoJSON reading"). Coordinates are written as {@code [lon, lat]}, plus
 * the elevation when it is set; empty geometries are written with empty coordinate arrays.
 */
public final class GeoJsonWriter {

    /** Creates a writer; it holds no state. */
    public GeoJsonWriter() {}

    /** The GeoJSON object for {@code geometry}, or null for a null geometry. */
    public Map<String, Object> write(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        switch (geometry) {
            case Point point -> {
                out.put("type", "Point");
                out.put("coordinates", point.isEmpty() ? List.of() : position(point.getCoordinate()));
            }
            case LineString line -> {
                out.put("type", "LineString");
                out.put("coordinates", positions(line.getCoordinates()));
            }
            case Polygon polygon -> {
                out.put("type", "Polygon");
                out.put("coordinates", rings(polygon));
            }
            case MultiPoint multi -> {
                out.put("type", "MultiPoint");
                List<Object> points = new ArrayList<>();
                for (int i = 0; i < multi.getNumGeometries(); i++) {
                    Point point = (Point) multi.getGeometryN(i);
                    if (!point.isEmpty()) {
                        points.add(position(point.getCoordinate()));
                    }
                }
                out.put("coordinates", points);
            }
            case MultiLineString multi -> {
                out.put("type", "MultiLineString");
                List<Object> lines = new ArrayList<>();
                for (int i = 0; i < multi.getNumGeometries(); i++) {
                    lines.add(positions(multi.getGeometryN(i).getCoordinates()));
                }
                out.put("coordinates", lines);
            }
            case MultiPolygon multi -> {
                out.put("type", "MultiPolygon");
                List<Object> polygons = new ArrayList<>();
                for (int i = 0; i < multi.getNumGeometries(); i++) {
                    polygons.add(rings((Polygon) multi.getGeometryN(i)));
                }
                out.put("coordinates", polygons);
            }
            case GeometryCollection collection -> {
                out.put("type", "GeometryCollection");
                List<Object> members = new ArrayList<>();
                for (int i = 0; i < collection.getNumGeometries(); i++) {
                    members.add(write(collection.getGeometryN(i)));
                }
                out.put("geometries", members);
            }
            default -> throw new IllegalArgumentException(
                    "Cannot write a " + geometry.getGeometryType() + " as GeoJSON");
        }
        return out;
    }

    private static List<Object> rings(Polygon polygon) {
        List<Object> rings = new ArrayList<>();
        if (polygon.isEmpty()) {
            return rings;
        }
        rings.add(positions(polygon.getExteriorRing().getCoordinates()));
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            rings.add(positions(polygon.getInteriorRingN(i).getCoordinates()));
        }
        return rings;
    }

    private static List<Object> positions(Coordinate[] coordinates) {
        List<Object> list = new ArrayList<>(coordinates.length);
        for (Coordinate coordinate : coordinates) {
            list.add(position(coordinate));
        }
        return list;
    }

    private static List<Double> position(Coordinate c) {
        return Double.isNaN(c.getZ()) ? List.of(c.getX(), c.getY()) : List.of(c.getX(), c.getY(), c.getZ());
    }
}
