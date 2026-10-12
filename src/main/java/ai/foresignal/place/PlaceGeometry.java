package ai.foresignal.place;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.trigger.Antimeridian;
import ai.foresignal.trigger.GeofenceMaterializer;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * Reads a place's geometry and derives its {@code area} (prompt 036).
 *
 * <ul>
 *   <li>{@code point}: a GeoJSON Point; the area is {@code ST_Buffer(geom::geography, radiusKm)}.
 *   <li>{@code polygon}: a Polygon or MultiPolygon, fixed with {@link GeometryFixer}; the area is the
 *       polygon itself. A ring that jumps more than 180° of longitude is read as crossing the
 *       antimeridian.
 *   <li>{@code route}: a LineString; the area is {@code ST_Buffer(geom::geography, bufferM)}.
 * </ul>
 *
 * <p>Every area is fixed, split at the antimeridian ({@link Antimeridian}) and a MultiPolygon with SRID
 * 4326. The input may have at most {@value #MAX_VERTICES} vertices, and a derived area with more is
 * simplified until it fits.
 */
@Component
public class PlaceGeometry {

    /** The most vertices of an input geometry and of a derived area. */
    public static final int MAX_VERTICES = 10_000;

    private final GeofenceMaterializer geofences;

    /** Creates the helper. */
    public PlaceGeometry(GeofenceMaterializer geofences) {
        this.geofences = geofences;
    }

    /**
     * Reads {@code node} as the geometry of a {@code shape} place.
     *
     * @throws InvalidInputException when it is not valid GeoJSON, has too many vertices, lies outside
     *     WGS 84 or does not suit the shape
     */
    public Geometry read(String shape, JsonNode node) {
        Geometry geometry = new GeoJsonReader(MAX_VERTICES, 2).read(node);
        geometry.setSRID(4326);
        Envelope e = geometry.getEnvelopeInternal();
        if (e.getMinY() < -90 || e.getMaxY() > 90 || e.getMinX() < -540 || e.getMaxX() > 540) {
            throw new InvalidInputException("geometry: latitudes must lie within ±90 and longitudes within ±180");
        }
        boolean fits = switch (shape) {
            case "point" -> geometry instanceof Point && e.getMinX() >= -180 && e.getMaxX() <= 180;
            case "polygon" -> geometry instanceof Polygon || geometry instanceof MultiPolygon;
            case "route" -> geometry instanceof LineString;
            default -> false;
        };
        if (!fits) {
            throw new InvalidInputException("geometry: a " + shape + " place needs a "
                    + switch (shape) {
                        case "point" -> "Point within ±180/±90";
                        case "polygon" -> "Polygon or MultiPolygon";
                        default -> "LineString";
                    });
        }
        return geometry;
    }

    /**
     * The derived area.
     *
     * @param shape the place shape
     * @param geometry its geometry ({@link #read})
     * @param radiusKm the radius of a point place
     * @param bufferM the buffer of a route place
     * @throws InvalidInputException when the area is empty after fixing
     */
    public MultiPolygon area(String shape, Geometry geometry, double radiusKm, double bufferM) {
        MultiPolygon area = switch (shape) {
            case "point" -> geofences.bufferPoint(((Point) geometry).getX(), ((Point) geometry).getY(), radiusKm * 1000.0);
            case "route" -> geofences.bufferLine(geometry, bufferM);
            default -> geofences.polygonal(geometry);
        };
        area = capVertices(area);
        if (area.isEmpty()) {
            throw new InvalidInputException("geometry: the place's area is empty after fixing");
        }
        area.setSRID(4326);
        return area;
    }

    /** {@code area} grown by {@code meters} on the ellipsoid, fixed and split. */
    public MultiPolygon buffer(MultiPolygon area, double meters) {
        return geofences.offset(area, meters);
    }

    /** {@code area} simplified until it has at most {@link #MAX_VERTICES} vertices. */
    static MultiPolygon capVertices(MultiPolygon area) {
        if (area.getNumPoints() <= MAX_VERTICES) {
            return area;
        }
        Envelope e = area.getEnvelopeInternal();
        double tolerance = Math.max(e.getWidth(), e.getHeight()) / 100_000.0;
        Geometry current = area;
        for (int i = 0; i < 40 && current.getNumPoints() > MAX_VERTICES; i++) {
            current = DouglasPeuckerSimplifier.simplify(area, tolerance);
            tolerance *= 2;
        }
        return Antimeridian.toMulti(GeometryFixer.fix(current));
    }
}
