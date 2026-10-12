package ai.foresignal.trigger;

import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.geom.util.PolygonExtracter;

/**
 * Splits polygons at the antimeridian, so every stored fence lies within longitudes −180…180 and
 * {@code ST_Intersects} on plain geometry gives the right answer (prompt 017).
 *
 * <p>A ring that jumps by more than 180° of longitude between two vertices is read as crossing the
 * antimeridian (the usual GeoJSON convention before RFC 7946's split rule), and so is any part that
 * extends beyond ±180, as a geography buffer near the antimeridian can. Such a polygon is unwrapped
 * to continuous longitudes, cut into the bands −540…−180, −180…180 and 180…540, and each band is
 * shifted back into range.
 */
public final class Antimeridian {

    /** WGS 84 geometries. */
    static final GeometryFactory FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private Antimeridian() {}

    /**
     * {@code polygonal} split at the antimeridian, fixed, as a MultiPolygon with SRID 4326.
     *
     * @return the split geometry; empty when nothing polygonal remains
     */
    public static MultiPolygon split(Geometry polygonal) {
        List<Polygon> parts = new ArrayList<>();
        for (Object item : PolygonExtracter.getPolygons(polygonal)) {
            Polygon polygon = (Polygon) item;
            if (polygon.isEmpty()) {
                continue;
            }
            Polygon continuous = crossesByJump(polygon) ? unwrap(polygon) : polygon;
            Envelope env = continuous.getEnvelopeInternal();
            if (env.getMinX() >= -180 && env.getMaxX() <= 180) {
                parts.add(continuous);
                continue;
            }
            addBand(parts, continuous, -540, -180, 360);
            addBand(parts, continuous, -180, 180, 0);
            addBand(parts, continuous, 180, 540, -360);
        }
        return toMulti(GeometryFixer.fix(FACTORY.createMultiPolygon(parts.toArray(Polygon[]::new))));
    }

    /** Every polygon of {@code geometry} as one MultiPolygon with SRID 4326 (empty when none). */
    public static MultiPolygon toMulti(Geometry geometry) {
        List<Polygon> polygons = new ArrayList<>();
        for (Object item : PolygonExtracter.getPolygons(geometry)) {
            Polygon polygon = (Polygon) item;
            if (!polygon.isEmpty()) {
                polygons.add(polygon);
            }
        }
        MultiPolygon multi = FACTORY.createMultiPolygon(polygons.toArray(Polygon[]::new));
        multi.setSRID(4326);
        return multi;
    }

    private static void addBand(List<Polygon> parts, Polygon polygon, double minX, double maxX, double shift) {
        Geometry band = FACTORY.toGeometry(new Envelope(minX, maxX, -90, 90));
        Geometry clipped = polygon.intersection(band);
        if (clipped.isEmpty()) {
            return;
        }
        Geometry moved = shift == 0 ? clipped : AffineTransformation.translationInstance(shift, 0).transform(clipped);
        for (Object item : PolygonExtracter.getPolygons(moved)) {
            Polygon p = (Polygon) item;
            if (!p.isEmpty() && p.getArea() > 0) {
                parts.add(p);
            }
        }
    }

    /** True when an exterior-ring edge jumps by more than 180° of longitude. */
    static boolean crossesByJump(Polygon polygon) {
        Coordinate[] ring = polygon.getExteriorRing().getCoordinates();
        for (int i = 1; i < ring.length; i++) {
            if (Math.abs(ring[i].x - ring[i - 1].x) > 180) {
                return true;
            }
        }
        return false;
    }

    /** The polygon with negative longitudes moved east by 360°, so a crossing ring is continuous. */
    private static Polygon unwrap(Polygon polygon) {
        LinearRing shell = shiftRing(polygon.getExteriorRing().getCoordinates());
        LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
        for (int i = 0; i < holes.length; i++) {
            holes[i] = shiftRing(polygon.getInteriorRingN(i).getCoordinates());
        }
        return FACTORY.createPolygon(shell, holes);
    }

    private static LinearRing shiftRing(Coordinate[] coordinates) {
        Coordinate[] shifted = new Coordinate[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            Coordinate c = coordinates[i];
            shifted[i] = new Coordinate(c.x < 0 ? c.x + 360 : c.x, c.y);
        }
        return FACTORY.createLinearRing(shifted);
    }
}
