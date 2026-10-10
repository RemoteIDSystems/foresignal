package ai.elaro.source.spi;

import java.util.Map;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Geometry helpers for adapters.
 *
 * <p>Everything produced here carries SRID 4326. Adapters never set the SRID themselves, so a feed
 * that happens to publish another projection cannot leak it into the store, where every spatial
 * query and tile assumes 4326.
 */
public final class Geo {

    /** WGS 84. */
    public static final int SRID = 4326;

    private static final GeometryFactory FACTORY = new GeometryFactory(new PrecisionModel(), SRID);

    /** Used only to re-serialize a {@code Map} form of GeoJSON for the JTS reader. */
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private Geo() {}

    /** The shared factory, already configured with SRID 4326. */
    public static GeometryFactory factory() {
        return FACTORY;
    }

    /**
     * A point.
     *
     * @throws UpstreamFormatException if the coordinates are outside the valid WGS 84 range, which
     *     usually means an upstream swapped latitude and longitude
     */
    public static Point point(double longitude, double latitude) {
        if (longitude < -180.0 || longitude > 180.0 || Double.isNaN(longitude)) {
            throw new UpstreamFormatException(
                    "Longitude " + longitude + " is outside -180..180", String.valueOf(longitude));
        }
        if (latitude < -90.0 || latitude > 90.0 || Double.isNaN(latitude)) {
            throw new UpstreamFormatException(
                    "Latitude " + latitude + " is outside -90..90", String.valueOf(latitude));
        }
        return FACTORY.createPoint(new Coordinate(longitude, latitude));
    }

    /**
     * Parses a GeoJSON geometry object.
     *
     * <p>Expects the geometry itself ({@code {"type":"Point",...}}), not a Feature.
     *
     * @throws UpstreamFormatException if it cannot be parsed
     */
    public static Geometry fromGeoJson(String geoJson) {
        if (geoJson == null || geoJson.isBlank()) {
            throw new UpstreamFormatException("GeoJSON geometry is empty", geoJson);
        }
        try {
            Geometry geometry = new GeoJsonReader(FACTORY).read(geoJson);
            if (geometry == null) {
                throw new UpstreamFormatException("GeoJSON parsed to no geometry", geoJson);
            }
            if (geometry.isEmpty()) {
                // A geometry object with no coordinates reads as POINT EMPTY rather than failing.
                // Storing that would be worse than rejecting it: an empty geometry is a missing
                // position, and it behaves surprisingly in spatial predicates and tiles.
                throw new UpstreamFormatException(
                        "GeoJSON geometry has no coordinates", geoJson);
            }
            geometry.setSRID(SRID);
            return geometry;
        } catch (ParseException | RuntimeException e) {
            if (e instanceof UpstreamFormatException already) {
                throw already;
            }
            throw new UpstreamFormatException(
                    "Could not parse GeoJSON geometry: " + e.getMessage(), geoJson, e);
        }
    }

    /** Parses a GeoJSON geometry already decoded into a map. */
    public static Geometry fromGeoJson(Map<String, Object> geoJson) {
        if (geoJson == null || geoJson.isEmpty()) {
            throw new UpstreamFormatException("GeoJSON geometry is empty", String.valueOf(geoJson));
        }
        try {
            return fromGeoJson(JSON.writeValueAsString(geoJson));
        } catch (JacksonException e) {
            throw new UpstreamFormatException(
                    "Could not re-serialize GeoJSON geometry: " + e.getOriginalMessage(),
                    String.valueOf(geoJson),
                    e);
        }
    }

    /** True for an exact (0,0) point, which is almost always a missing position (§3). */
    public static boolean isNullIsland(Geometry geometry) {
        if (geometry == null || !(geometry instanceof Point point) || point.isEmpty()) {
            return false;
        }
        return point.getX() == 0.0 && point.getY() == 0.0;
    }
}
