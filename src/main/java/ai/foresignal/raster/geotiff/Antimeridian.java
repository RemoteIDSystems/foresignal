package ai.foresignal.raster.geotiff;

import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Unwrapping and splitting at the antimeridian (prompt 054): footprints and swaths are built with
 * continuous ("unwrapped") longitudes, which may run past ±180°, and are then cut into the
 * −180…180° windows and shifted back, so a Bering Sea pass becomes a MultiPolygon with one part on
 * each side of 180°.
 */
public final class Antimeridian {

    /** The factory of every geometry built here: SRID 4326. */
    public static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);

    private Antimeridian() {}

    /**
     * Makes a ring's longitudes continuous: each longitude is moved by a multiple of 360° so it lies
     * within 180° of the previous one. The coordinates are changed in place.
     */
    public static void unwrap(Coordinate[] ring) {
        for (int i = 1; i < ring.length; i++) {
            double previous = ring[i - 1].x;
            double x = ring[i].x;
            while (x - previous > 180) {
                x -= 360;
            }
            while (x - previous < -180) {
                x += 360;
            }
            ring[i].x = x;
        }
    }

    /**
     * Splits an unwrapped polygonal geometry at ±180°.
     *
     * @param unwrapped polygons whose longitudes may lie anywhere in −540…540°
     * @return the parts inside −180…180°, SRID 4326; empty when nothing is left
     */
    public static MultiPolygon split(Geometry unwrapped) {
        List<Polygon> parts = new ArrayList<>();
        for (int shift = -2; shift <= 2; shift++) {
            double offset = shift * 360.0;
            Polygon window = box(-180 + offset, -90, 180 + offset, 90);
            Geometry piece = unwrapped.intersection(window);
            for (int i = 0; i < piece.getNumGeometries(); i++) {
                if (piece.getGeometryN(i) instanceof Polygon polygon && !polygon.isEmpty() && polygon.getArea() > 0) {
                    Polygon moved = (Polygon) GEOMETRY.createGeometry(polygon);
                    double dx = -offset;
                    moved.apply((CoordinateFilter) c -> c.x = round(c.x + dx));
                    moved.geometryChanged();
                    parts.add(moved);
                }
            }
        }
        return GEOMETRY.createMultiPolygon(parts.toArray(Polygon[]::new));
    }

    /** The axis-aligned box as a polygon, SRID 4326. */
    public static Polygon box(double west, double south, double east, double north) {
        return GEOMETRY.createPolygon(new Coordinate[] {new Coordinate(west, south), new Coordinate(east, south),
            new Coordinate(east, north), new Coordinate(west, north), new Coordinate(west, south)});
    }

    /**
     * The WGS84 bounding box {@code [west, south, east, north]} of a split geometry, with
     * {@code west > east} when it crosses ±180° (as {@code GridDefinition} accepts).
     */
    public static double[] bounds(MultiPolygon split) {
        double south = 90;
        double north = -90;
        double minWest = 180;
        double maxEast = -180;
        boolean touchesEast = false;
        boolean touchesWest = false;
        double westOfEastPart = 180;
        double eastOfWestPart = -180;
        for (int i = 0; i < split.getNumGeometries(); i++) {
            Geometry g = split.getGeometryN(i);
            var env = g.getEnvelopeInternal();
            south = Math.min(south, env.getMinY());
            north = Math.max(north, env.getMaxY());
            minWest = Math.min(minWest, env.getMinX());
            maxEast = Math.max(maxEast, env.getMaxX());
            if (env.getMaxX() >= 179.999999) {
                touchesEast = true;
                westOfEastPart = Math.min(westOfEastPart, env.getMinX());
            }
            if (env.getMinX() <= -179.999999) {
                touchesWest = true;
                eastOfWestPart = Math.max(eastOfWestPart, env.getMaxX());
            }
        }
        if (touchesEast && touchesWest && split.getNumGeometries() > 1 && westOfEastPart > eastOfWestPart) {
            return new double[] {westOfEastPart, south, eastOfWestPart, north};
        }
        return new double[] {minWest, south, maxEast, north};
    }

    private static double round(double v) {
        return Math.round(v * 1e7) / 1e7;
    }
}
