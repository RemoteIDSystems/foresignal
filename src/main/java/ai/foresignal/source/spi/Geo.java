package ai.foresignal.source.spi;

import java.util.Map;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
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

    /** Used only to convert a {@code Map} form of GeoJSON into a tree for the reader. */
    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** Foresignal's own Jackson 3 reader; {@code jts-io-common} is not used (Conventions). */
    private static final GeoJsonReader READER = new GeoJsonReader();

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
     * <p>Expects a geometry ({@code {"type":"Point",...}}); a Feature is unwrapped to its geometry
     * and a FeatureCollection becomes a GeometryCollection. Reads through
     * {@link GeoJsonReader}, which enforces the Core-Data-Model §3 vertex and finiteness limits.
     *
     * @throws UpstreamFormatException if it cannot be parsed or has no coordinates
     */
    public static Geometry fromGeoJson(String geoJson) {
        if (geoJson == null || geoJson.isBlank()) {
            throw new UpstreamFormatException("GeoJSON geometry is empty", geoJson);
        }
        try {
            return checked(READER.read(geoJson), geoJson);
        } catch (InvalidInputException e) {
            throw new UpstreamFormatException(
                    "Could not parse GeoJSON geometry: " + e.getMessage(), geoJson, e);
        }
    }

    /** Parses a GeoJSON geometry already decoded into a map. */
    public static Geometry fromGeoJson(Map<String, Object> geoJson) {
        if (geoJson == null || geoJson.isEmpty()) {
            throw new UpstreamFormatException("GeoJSON geometry is empty", String.valueOf(geoJson));
        }
        JsonNode tree;
        try {
            tree = JSON.valueToTree(geoJson);
        } catch (JacksonException e) {
            throw new UpstreamFormatException(
                    "Could not convert GeoJSON geometry: " + e.getOriginalMessage(),
                    String.valueOf(geoJson),
                    e);
        }
        try {
            return checked(READER.read(tree), String.valueOf(geoJson));
        } catch (InvalidInputException e) {
            throw new UpstreamFormatException(
                    "Could not parse GeoJSON geometry: " + e.getMessage(), String.valueOf(geoJson), e);
        }
    }

    private static Geometry checked(Geometry geometry, String content) {
        if (geometry == null || geometry.isEmpty()) {
            // An empty geometry is a missing position, and it behaves surprisingly in spatial
            // predicates and tiles, so it is rejected rather than stored.
            throw new UpstreamFormatException("GeoJSON geometry has no coordinates", content);
        }
        geometry.setSRID(SRID);
        return geometry;
    }

    /** The mean Earth radius used by {@link #haversineKm}, km (IUGG). */
    public static final double EARTH_RADIUS_KM = 6371.0088;

    /**
     * The great-circle distance between two WGS84 positions on the mean sphere, km (prompt 048: the GLM
     * flash store and the lightning clustering).
     */
    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double s = Math.sin(dLat / 2);
        double t = Math.sin(dLon / 2);
        double a = s * s + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * t * t;
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.min(1, Math.sqrt(a)));
    }

    /** True for an exact (0,0) point, which is almost always a missing position (§3). */
    public static boolean isNullIsland(Geometry geometry) {
        if (geometry == null || !(geometry instanceof Point point) || point.isEmpty()) {
            return false;
        }
        return point.getX() == 0.0 && point.getY() == 0.0;
    }
}
