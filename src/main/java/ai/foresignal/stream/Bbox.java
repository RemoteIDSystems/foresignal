package ai.foresignal.stream;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

/**
 * An axis-aligned bounding box in WGS 84 degrees, as carried on bus events.
 *
 * <p>A plain value rather than a JTS {@link Envelope}, so subscribers (SSE, triggers) can filter
 * and serialize it without a geometry library in the way.
 *
 * @param minX west longitude
 * @param minY south latitude
 * @param maxX east longitude
 * @param maxY north latitude
 */
public record Bbox(double minX, double minY, double maxX, double maxY) {

    /** The box of {@code envelope}, or null for a null or empty envelope. */
    public static Bbox of(Envelope envelope) {
        if (envelope == null || envelope.isNull()) {
            return null;
        }
        return new Bbox(envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());
    }

    /** The box of {@code geometry}, or null for a null or empty geometry. */
    public static Bbox of(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        return of(geometry.getEnvelopeInternal());
    }

    /** True when this box and {@code other} share at least one point. */
    public boolean intersects(Bbox other) {
        return other != null
                && minX <= other.maxX
                && other.minX <= maxX
                && minY <= other.maxY
                && other.minY <= maxY;
    }
}
