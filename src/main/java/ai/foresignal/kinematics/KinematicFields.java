package ai.foresignal.kinematics;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reads speed, course and altitude from a record payload under the field names the adapters use
 * (prompt 114; Adapter Guide §5 and §15): aircraft {@code gs} (knots), {@code track} (degrees),
 * {@code alt_geom}/{@code alt_baro} (meters); vessels {@code sog} (knots) and {@code cog} (degrees);
 * gpsd {@code speed} (m/s) and {@code track}; location devices {@code speedMs}, {@code courseDeg} and
 * {@code altitudeM}. The first present field of each list wins.
 */
public final class KinematicFields {

    /** Knots to meters per second. */
    public static final double KNOT_TO_MPS = 0.514444;

    /** Speed fields and their factor to m/s, in preference order. */
    static final List<Map.Entry<String, Double>> SPEED = List.of(
            Map.entry("speedMs", 1.0),
            Map.entry("speed", 1.0),
            Map.entry("gs", KNOT_TO_MPS),
            Map.entry("sog", KNOT_TO_MPS));

    /** Course fields in degrees from true north, in preference order. */
    static final List<String> COURSE = List.of("courseDeg", "track", "cog");

    /** Altitude fields in meters, in preference order. */
    static final List<String> ALTITUDE = List.of("altitudeM", "alt_geom", "alt_baro", "altHAE");

    /** Every payload path this class reads (the persist gate loads them for {@code approach} rules). */
    public static final Set<String> PATHS = Set.of("speedMs", "speed", "gs", "sog", "courseDeg", "track", "cog",
            "altitudeM", "alt_geom", "alt_baro", "altHAE");

    private KinematicFields() {}

    /** The speed in m/s, or null when no speed field holds a finite, non-negative number. */
    public static Double speedMps(Map<String, ?> payload) {
        if (payload == null) {
            return null;
        }
        for (Map.Entry<String, Double> field : SPEED) {
            Double value = number(payload.get(field.getKey()));
            if (value != null && value >= 0) {
                return value * field.getValue();
            }
        }
        return null;
    }

    /** The course in degrees {@code [0, 360)}, or null. */
    public static Double courseDeg(Map<String, ?> payload) {
        if (payload == null) {
            return null;
        }
        for (String field : COURSE) {
            Double value = number(payload.get(field));
            if (value != null && value >= 0 && value <= 360) {
                return GeoMath.normalizeDegrees(value);
            }
        }
        return null;
    }

    /** The altitude in meters, or null. */
    public static Double altitudeM(Map<String, ?> payload) {
        if (payload == null) {
            return null;
        }
        for (String field : ALTITUDE) {
            Double value = number(payload.get(field));
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * The same reads over the persist gate's path map ({@code path → value}), where a missing path maps
     * to null.
     *
     * @return {@code {speedMps, courseDeg, altitudeM}}, each possibly null
     */
    public static Double[] fromPathValues(Map<String, Object> values) {
        return new Double[] {speedMps(values), courseDeg(values), altitudeM(values)};
    }

    /** A JSON number as a finite double, or null. */
    static Double number(Object value) {
        if (value instanceof Number n) {
            double d = n.doubleValue();
            return Double.isFinite(d) ? d : null;
        }
        return null;
    }
}
