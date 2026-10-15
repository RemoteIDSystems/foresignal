package ai.foresignal.derive;

import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.PositionSource;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * One observation a {@link DerivedGenerator} reasons over (prompt 099): the merged view of a
 * version's payload (from the {@code ChangeBus}) and the freshest live state (from the
 * {@code LiveBus}, {@code record_live}) for one entity of one source.
 *
 * <p>An observation is read-only: the feed hands out copies of the current state, never history
 * (invariant HIST-2). {@code payload} is the current payload as every current read builds it
 * ({@code jsonb_strip_nulls(head.payload || live.kinematic)}).
 *
 * @param sourceId the source
 * @param externalId the entity
 * @param kind the record kind
 * @param observedAt the latest accepted observation time
 * @param geometry the live geometry, or null
 * @param payload the current payload
 * @param positionSource how the position was obtained
 */
public record Observation(
        String sourceId,
        String externalId,
        String kind,
        Instant observedAt,
        Geometry geometry,
        Map<String, Object> payload,
        PositionSource positionSource) {

    /** Copies the payload. */
    public Observation {
        payload = payload == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(payload));
        positionSource = positionSource == null ? PositionSource.UNKNOWN : positionSource;
    }

    /** True when the observation carries a usable point. */
    public boolean located() {
        return longitude() != null && latitude() != null;
    }

    /** The longitude of the geometry's representative point, or null when there is none. */
    public Double longitude() {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        Point point = geometry.getCentroid();
        if (point == null || point.isEmpty()) {
            return null;
        }
        return point.getX();
    }

    /** The latitude of the geometry's representative point, or null when there is none. */
    public Double latitude() {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        Point point = geometry.getCentroid();
        if (point == null || point.isEmpty()) {
            return null;
        }
        return point.getY();
    }

    /** A payload value as a double, or null when absent or not numeric. */
    public Double number(String key) {
        Object value = payload.get(key);
        if (value instanceof Number number) {
            double d = number.doubleValue();
            return Double.isFinite(d) ? d : null;
        }
        return null;
    }

    /** A payload value as a string, or null when absent. */
    public String text(String key) {
        Object value = payload.get(key);
        return value == null ? null : String.valueOf(value);
    }

    /** The distance in km from this observation to a point, or NaN when it has no position. */
    public double distanceKm(double lon, double lat) {
        Double myLon = longitude();
        Double myLat = latitude();
        if (myLon == null || myLat == null) {
            return Double.NaN;
        }
        return Geo.haversineKm(myLat, myLon, lat, lon);
    }
}
