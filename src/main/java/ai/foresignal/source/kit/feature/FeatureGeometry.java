package ai.foresignal.source.kit.feature;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.UpstreamFormatException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import tools.jackson.databind.JsonNode;

/**
 * GeoJSON geometry of a kit feature as JTS (prompt 073), kept under the Core-Data-Model §3 vertex limit.
 *
 * <p>A geometry with more than {@value #MAX_VERTICES} vertices is simplified with
 * {@link TopologyPreservingSimplifier} at doubling tolerances, starting at {@value #START_TOLERANCE_DEG}°, until it
 * fits; the record then carries {@code geometrySimplified: true} and {@code simplifyToleranceDeg}. Topology-preserving
 * simplification keeps a valid polygon valid (no self-intersections, holes stay inside).
 *
 * <p>The geometry is read with a raised vertex cap ({@value #READ_MAX_VERTICES}) because {@code Geo.fromGeoJson}
 * enforces the 10,000-vertex store limit while reading, which would reject a large perimeter before it could be
 * simplified; the result carries SRID 4326 like everything {@link Geo} produces.
 */
public final class FeatureGeometry {

    /** The vertex count a stored geometry may have after simplification. */
    public static final int MAX_VERTICES = 9_500;

    /** The first simplification tolerance, degrees. */
    public static final double START_TOLERANCE_DEG = 0.0005;

    /** The largest geometry read before simplification. */
    public static final int READ_MAX_VERTICES = 500_000;

    /** The payload key set when the geometry was simplified. */
    public static final String SIMPLIFIED_KEY = "geometrySimplified";

    /** The payload key holding the tolerance used, degrees. */
    public static final String TOLERANCE_KEY = "simplifyToleranceDeg";

    /** At most this many doublings are tried (0.0005° · 2^24 is far beyond any extent). */
    static final int MAX_STEPS = 24;

    private static final GeoJsonReader READER = new GeoJsonReader(READ_MAX_VERTICES,
            GeoJsonReader.DEFAULT_MAX_COLLECTION_DEPTH);

    /**
     * A converted geometry.
     *
     * @param geometry the JTS geometry, SRID 4326, or null when the feature had none
     * @param simplified true when it was simplified to fit
     * @param toleranceDeg the tolerance used, or null
     */
    public record Result(Geometry geometry, boolean simplified, Double toleranceDeg) {

        /** No geometry. */
        public static final Result NONE = new Result(null, false, null);
    }

    private FeatureGeometry() {}

    /**
     * Converts a GeoJSON geometry node.
     *
     * @param node the {@code geometry} member, or null
     * @return the geometry; {@link Result#NONE} for a null or missing geometry
     * @throws UpstreamFormatException when the geometry is malformed or has no coordinates
     */
    public static Result fromGeoJson(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return Result.NONE;
        }
        Geometry geometry;
        try {
            geometry = READER.read(node);
        } catch (InvalidInputException e) {
            throw new UpstreamFormatException("Could not parse feature geometry: " + e.getMessage(), "", e);
        }
        if (geometry == null || geometry.isEmpty()) {
            return Result.NONE;
        }
        geometry.setSRID(Geo.SRID);
        return fit(geometry);
    }

    /**
     * {@code geometry} simplified until it has at most {@value #MAX_VERTICES} vertices.
     *
     * @param geometry a geometry in SRID 4326
     */
    public static Result fit(Geometry geometry) {
        if (geometry.getNumPoints() <= MAX_VERTICES) {
            if (!geometry.isValid()) {
                Geometry fixed = GeometryFixer.fix(geometry);
                fixed.setSRID(Geo.SRID);
                return new Result(fixed, false, null);
            }
            return new Result(geometry, false, null);
        }
        double tolerance = START_TOLERANCE_DEG;
        for (int step = 0; step < MAX_STEPS; step++) {
            Geometry simplified = TopologyPreservingSimplifier.simplify(geometry, tolerance);
            if (!simplified.isValid()) {
                // Topology-preserving simplification of a self-touching or overlapping source can
                // still round into an invalid ring; repair it rather than store an invalid geometry.
                simplified = GeometryFixer.fix(simplified);
            }
            if (simplified.isValid() && simplified.getNumPoints() <= MAX_VERTICES) {
                simplified.setSRID(Geo.SRID);
                return new Result(simplified, true, tolerance);
            }
            tolerance *= 2;
        }
        throw new UpstreamFormatException("Feature geometry has " + geometry.getNumPoints()
                + " vertices and could not be simplified below " + MAX_VERTICES, "");
    }
}
