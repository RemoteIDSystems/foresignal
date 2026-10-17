package ai.foresignal.kinematics;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The kinematic state of a record (prompt 114, OpenAPI {@code KinematicState}): position,
 * {@code observedAt}, speed and course (from the payload, or derived from the last two persisted
 * positions when absent), and altitude when present.
 *
 * @param sourceId the source
 * @param externalId the entity
 * @param kind the kind, or null
 * @param lon the longitude
 * @param lat the latitude
 * @param observedAt when the position was observed
 * @param speedMps the speed in m/s, or null when unknown
 * @param courseDeg the course in degrees from true north, or null when unknown
 * @param altitudeM the altitude in meters, or null
 * @param derived true when speed and course were derived from persisted positions
 * @param stale true when older than 2 × the source TTL
 */
public record KinematicState(String sourceId, String externalId, String kind, double lon, double lat,
        Instant observedAt, Double speedMps, Double courseDeg, Double altitudeM, boolean derived, boolean stale) {

    /** A stationary state at a fixed point (a place or a coordinate target). */
    public static KinematicState fixed(double lon, double lat, Instant at) {
        return new KinematicState(null, null, null, lon, lat, at, 0.0, 0.0, null, false, false);
    }

    /** True when both speed and course are known. */
    public boolean hasVelocity() {
        return speedMps != null && courseDeg != null;
    }

    /** The velocity {@code {east, north}} in m/s; zero without one. */
    public double[] velocity() {
        return hasVelocity() ? GeoMath.velocity(speedMps, courseDeg) : new double[] {0, 0};
    }

    /** The age of the state at {@code now} (zero when observed in the future). */
    public Duration age(Instant now) {
        Duration age = Duration.between(observedAt, now);
        return age.isNegative() ? Duration.ZERO : age;
    }

    /**
     * The position projected to {@code t} at constant velocity, in the local frame around this state.
     *
     * @return {@code {lon, lat}}
     */
    public double[] projectedTo(Instant t) {
        if (!hasVelocity()) {
            return new double[] {lon, lat};
        }
        double seconds = Duration.between(observedAt, t).toMillis() / 1000.0;
        double[] v = velocity();
        double dLat = v[1] * seconds / GeoMath.METERS_PER_DEGREE_LAT;
        double cos = Math.max(1e-6, Math.cos(Math.toRadians(lat)));
        double dLon = v[0] * seconds / (GeoMath.METERS_PER_DEGREE_LON * cos);
        double newLat = Math.max(-90, Math.min(90, lat + dLat));
        double newLon = lon + dLon;
        newLon = ((newLon + 540) % 360) - 180;
        return new double[] {newLon, newLat};
    }

    /** This state marked stale or not. */
    public KinematicState withStale(boolean value) {
        return new KinematicState(sourceId, externalId, kind, lon, lat, observedAt, speedMps, courseDeg, altitudeM,
                derived, value);
    }

    /** The OpenAPI {@code KinematicState} object. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sourceId", sourceId);
        out.put("externalId", externalId);
        out.put("kind", kind);
        out.put("position", Map.of("type", "Point", "coordinates", List.of(lon, lat)));
        out.put("observedAt", observedAt);
        out.put("speedMps", speedMps == null ? null : round(speedMps, 2));
        out.put("courseDeg", courseDeg == null ? null : round(courseDeg, 1));
        out.put("altitudeM", altitudeM == null ? null : round(altitudeM, 0));
        out.put("derived", derived);
        out.put("stale", stale);
        return out;
    }

    static double round(double value, int decimals) {
        double f = Math.pow(10, decimals);
        return Math.round(value * f) / f;
    }
}
