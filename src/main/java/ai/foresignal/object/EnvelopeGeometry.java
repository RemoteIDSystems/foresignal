package ai.foresignal.object;

import ai.foresignal.source.formats.geojson.GeoJsonWriter;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;

/**
 * Envelope geometry (docs/Client-Contract.md §3): a GeoJSON geometry simplified to at most {@value #MAX_VERTICES}
 * vertices with JTS's topology-preserving simplifier (doubling the tolerance until it fits), or its centroid when it
 * cannot be simplified enough, plus its bbox.
 */
public final class EnvelopeGeometry {

    /** Most vertices in an envelope geometry. */
    public static final int MAX_VERTICES = 500;

    private static final GeoJsonWriter WRITER = new GeoJsonWriter();

    private EnvelopeGeometry() {}

    /**
     * A shape for an envelope.
     *
     * @param geoJson the GeoJSON geometry
     * @param bbox {@code [minLon, minLat, maxLon, maxLat]}
     */
    public record Shape(Map<String, Object> geoJson, List<Double> bbox) {}

    /** The envelope shape of {@code geometry}, or null when it is null or empty. */
    public static Shape of(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        Envelope e = geometry.getEnvelopeInternal();
        List<Double> bbox = List.of(e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY());
        return new Shape(WRITER.write(simplify(geometry)), bbox);
    }

    /** {@code geometry} with at most {@value #MAX_VERTICES} vertices (its centroid when simplifying fails). */
    public static Geometry simplify(Geometry geometry) {
        if (geometry.getNumPoints() <= MAX_VERTICES) {
            return geometry;
        }
        Envelope e = geometry.getEnvelopeInternal();
        double tolerance = Math.max(Math.max(e.getWidth(), e.getHeight()) / 2000.0, 1e-6);
        Geometry current = geometry;
        for (int i = 0; i < 20 && current.getNumPoints() > MAX_VERTICES; i++) {
            current = TopologyPreservingSimplifier.simplify(geometry, tolerance);
            tolerance *= 2;
        }
        return current.getNumPoints() <= MAX_VERTICES ? current : geometry.getCentroid();
    }
}
