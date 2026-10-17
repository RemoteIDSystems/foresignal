package ai.foresignal.pol;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.settings.Durations;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The stay and clustering parameters of a subject (prompt 114, OpenAPI {@code PolSubjectInput.params}).
 *
 * @param stayRadiusM the stay radius, meters (default 100)
 * @param minDwell the shortest stay (default 10 min)
 * @param maxGap the gap between fixes that ends a stay (default 30 min)
 * @param eps the DBSCAN radius, meters (default 150)
 * @param minPts the DBSCAN minimum stays (default 3)
 * @param lookback the nightly recompute window over stored stays (default 28 d)
 */
public record PolParams(double stayRadiusM, Duration minDwell, Duration maxGap, double eps, int minPts,
        Duration lookback) {

    /** The defaults. */
    public static final PolParams DEFAULTS =
            new PolParams(100, Duration.ofMinutes(10), Duration.ofMinutes(30), 150, 3, Duration.ofDays(28));

    /**
     * Reads stored or requested parameters, defaults filled in.
     *
     * @throws ValidationException when a value is out of range
     */
    public static PolParams of(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return DEFAULTS;
        }
        double radius = number(params, "stayRadiusM", DEFAULTS.stayRadiusM(), 1);
        Duration minDwell = duration(params, "minDwell", DEFAULTS.minDwell());
        Duration maxGap = duration(params, "maxGap", DEFAULTS.maxGap());
        double eps = number(params, "eps", DEFAULTS.eps(), 1);
        double minPts = number(params, "minPts", DEFAULTS.minPts(), 1);
        if (minPts != Math.rint(minPts)) {
            throw ValidationException.forField("params.minPts", "must be an integer ≥ 1");
        }
        Duration lookback = duration(params, "lookback", DEFAULTS.lookback());
        for (String key : params.keySet()) {
            if (!DEFAULTS.toMap().containsKey(key)) {
                throw ValidationException.forField("params." + key, "is not a pattern-of-life parameter");
            }
        }
        return new PolParams(radius, minDwell, maxGap, eps, (int) minPts, lookback);
    }

    /** The parameters as stored and served. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("stayRadiusM", stayRadiusM);
        out.put("minDwell", Durations.format(minDwell));
        out.put("maxGap", Durations.format(maxGap));
        out.put("eps", eps);
        out.put("minPts", minPts);
        out.put("lookback", Durations.format(lookback));
        return out;
    }

    private static double number(Map<String, Object> params, String key, double def, double min) {
        Object value = params.get(key);
        if (value == null) {
            return def;
        }
        if (!(value instanceof Number n) || !Double.isFinite(n.doubleValue()) || n.doubleValue() < min) {
            throw ValidationException.forField("params." + key, "must be a number ≥ " + (long) min);
        }
        return n.doubleValue();
    }

    private static Duration duration(Map<String, Object> params, String key, Duration def) {
        Object value = params.get(key);
        if (value == null) {
            return def;
        }
        try {
            Duration d = Durations.parse(String.valueOf(value));
            if (d.isNegative() || d.isZero()) {
                throw new IllegalArgumentException("not positive");
            }
            return d;
        } catch (RuntimeException e) {
            throw ValidationException.forField("params." + key, "must be a positive duration such as 10m");
        }
    }
}
