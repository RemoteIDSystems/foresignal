package ai.foresignal.trigger;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Turns what a rule says about place into the MultiPolygon the engine compares against (prompt 017).
 *
 * <ul>
 *   <li>Polygons are fixed with JTS {@link GeometryFixer} and split at the antimeridian.
 *   <li>Lines are buffered server-side as {@code ST_Buffer(line::geography, :m)::geometry}, so the
 *       corridor width is in meters on the ellipsoid, then fixed and split.
 *   <li>A {@code near} point is buffered the same way by its radius.
 *   <li>The hysteresis band shrinks or grows a fence with a negative or positive geography buffer.
 * </ul>
 *
 * <p>Every result is a MultiPolygon with SRID 4326; an empty one comes back empty, and the caller
 * decides whether that is an error.
 */
@Component
public class GeofenceMaterializer {

    static final String BUFFER_SQL =
            "SELECT ST_AsBinary(ST_Buffer(ST_GeomFromWKB(?, 4326)::geography, ?)::geometry)";

    static final String BUFFER_POINT_SQL =
            "SELECT ST_AsBinary(ST_Buffer(ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography, ?)::geometry)";

    private final JdbcTemplate jdbc;

    /** Creates the materializer. */
    public GeofenceMaterializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** A polygonal fence, fixed and split. */
    public MultiPolygon polygonal(Geometry polygonal) {
        return Antimeridian.split(GeometryFixer.fix(polygonal));
    }

    /** A line buffered into a corridor {@code meters} wide on each side, fixed and split. */
    public MultiPolygon bufferLine(Geometry line, double meters) {
        return buffer(line, meters);
    }

    /** A disc of {@code meters} around {@code (lon, lat)}, fixed and split. */
    public MultiPolygon bufferPoint(double lon, double lat, double meters) {
        byte[] wkb = jdbc.queryForObject(BUFFER_POINT_SQL, byte[].class, lon, lat, meters);
        return Antimeridian.split(GeometryFixer.fix(read(wkb)));
    }

    /**
     * The fence grown ({@code meters > 0}) or shrunk ({@code meters < 0}) on the ellipsoid, fixed and
     * split; empty when a shrink consumes it.
     */
    public MultiPolygon offset(MultiPolygon fence, double meters) {
        return buffer(fence, meters);
    }

    private MultiPolygon buffer(Geometry geometry, double meters) {
        byte[] wkb = jdbc.queryForObject(BUFFER_SQL, byte[].class, new WKBWriter(2).write(geometry), meters);
        return Antimeridian.split(GeometryFixer.fix(read(wkb)));
    }

    private static Geometry read(byte[] wkb) {
        if (wkb == null) {
            return Antimeridian.FACTORY.createMultiPolygon();
        }
        try {
            Geometry geometry = new WKBReader(Antimeridian.FACTORY).read(wkb);
            geometry.setSRID(4326);
            return geometry;
        } catch (ParseException e) {
            throw new IllegalStateException("PostGIS returned unreadable WKB: " + e.getMessage(), e);
        }
    }
}
