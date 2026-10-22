package ai.foresignal.inbound.edp.binding;

import ai.foresignal.forecast.Ledger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Maps a decile forecast (a provider like TimesFM emits p10…p90) onto the ledger's required quantiles
 * (p05, p25, p50, p75, p95; prompt 155), so a decile provider is accepted without loosening
 * {@link Ledger#REQUIRED_QUANTILES}. Pure.
 *
 * <p>The mapping is piecewise-linear over the provided (cumulative probability → value) points, ordered by
 * level: a target level between two provided deciles is linearly interpolated; a target below the lowest or
 * above the highest provided decile (p05 below p10, p95 above p90) is linearly extrapolated from the two
 * nearest points; the result is clamped to be non-decreasing. A set that already carries every required
 * quantile is returned unchanged, and a set with fewer than two points cannot be mapped and is returned as is
 * (the ledger then rejects it).
 */
public final class DecileQuantiles {

    private DecileQuantiles() {}

    /** The levels produced, a superset of {@link Ledger#REQUIRED_QUANTILES} within {@link Ledger#QUANTILES}. */
    static final List<String> OUTPUT = List.of("p05", "p10", "p25", "p50", "p75", "p90", "p95");

    /**
     * {@code quantiles} mapped onto the required ledger quantiles when it is a decile set missing some of them;
     * {@code quantiles} unchanged when it already carries every required quantile or cannot be mapped.
     */
    public static Map<String, Double> toLedger(Map<String, Double> quantiles) {
        if (quantiles == null || quantiles.isEmpty()) {
            return quantiles;
        }
        if (hasAllRequired(quantiles)) {
            return quantiles;
        }
        TreeMap<Double, Double> points = points(quantiles);
        if (points.size() < 2) {
            return quantiles;
        }
        Map<String, Double> out = new LinkedHashMap<>();
        double last = Double.NEGATIVE_INFINITY;
        for (String key : OUTPUT) {
            double value = interpolate(points, Ledger.level(key));
            value = Math.max(value, last);
            out.put(key, value);
            last = value;
        }
        return out;
    }

    /** True when every required quantile is already present. */
    static boolean hasAllRequired(Map<String, Double> quantiles) {
        for (String key : Ledger.REQUIRED_QUANTILES) {
            if (quantiles.get(key) == null) {
                return false;
            }
        }
        return true;
    }

    /** The provided quantiles as sorted (level → value) points. */
    private static TreeMap<Double, Double> points(Map<String, Double> quantiles) {
        TreeMap<Double, Double> points = new TreeMap<>();
        for (Map.Entry<String, Double> e : quantiles.entrySet()) {
            if (e.getValue() == null || !e.getKey().matches("^p[0-9]{2}$")) {
                continue;
            }
            points.put(Ledger.level(e.getKey()), e.getValue());
        }
        return points;
    }

    /** The value at cumulative probability {@code level}, interpolating within and extrapolating beyond. */
    private static double interpolate(TreeMap<Double, Double> points, double level) {
        Map.Entry<Double, Double> floor = points.floorEntry(level);
        Map.Entry<Double, Double> ceil = points.ceilingEntry(level);
        if (floor != null && ceil != null) {
            if (floor.getKey().equals(ceil.getKey())) {
                return floor.getValue();
            }
            return line(floor, ceil, level);
        }
        List<Map.Entry<Double, Double>> sorted = new ArrayList<>(points.entrySet());
        if (floor == null) {
            // Below the lowest point: extrapolate from the first two.
            return line(sorted.get(0), sorted.get(1), level);
        }
        // Above the highest point: extrapolate from the last two.
        return line(sorted.get(sorted.size() - 2), sorted.get(sorted.size() - 1), level);
    }

    private static double line(Map.Entry<Double, Double> a, Map.Entry<Double, Double> b, double x) {
        double slope = (b.getValue() - a.getValue()) / (b.getKey() - a.getKey());
        return a.getValue() + slope * (x - a.getKey());
    }
}
