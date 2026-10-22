package ai.foresignal.forecast.baseline;

import ai.foresignal.forecast.Ledger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The crossing probability of a threshold question from a predictive quantile distribution (prompt 156): given a
 * numeric forecaster's quantiles and a resolver's {@code op} and {@code threshold}, {@code P(value op threshold)}.
 * Pure.
 *
 * <p>The quantiles are read as points on the predictive CDF (level → value). The CDF at the threshold is found by
 * linear interpolation between the two bracketing quantiles (and linear extrapolation of the nearest segment
 * beyond the outermost quantiles), clamped to {@code [0, 1]}. {@code P(value > t)} and {@code P(value >= t)} are
 * {@code 1 − CDF(t)}; {@code P(value < t)} and {@code P(value <= t)} are {@code CDF(t)}. The result is clamped away
 * from certainty to {@code [MIN, MAX]}. An equality threshold has probability zero under a continuous distribution
 * and is not answered.
 */
public final class ThresholdCrossing {

    /** The smallest crossing probability returned (a predictive distribution is never certain). */
    public static final double MIN = 0.01;

    /** The largest crossing probability returned. */
    public static final double MAX = 0.99;

    private ThresholdCrossing() {}

    /**
     * {@code P(value op threshold)} from a quantile map, or {@code NaN} when it cannot be computed (fewer than two
     * distinct quantile points, or an {@code eq} op).
     *
     * @param quantiles level keys ({@code p05}…{@code p95}, optionally {@code p01}/{@code p99}) to values
     * @param op the resolver op ({@code gt}, {@code gte}, {@code lt}, {@code lte}, {@code eq})
     * @param threshold the resolver threshold
     */
    public static double probability(Map<String, Double> quantiles, String op, double threshold) {
        if ("eq".equals(op)) {
            return Double.NaN;
        }
        double cdf = cdf(quantiles, threshold);
        if (Double.isNaN(cdf)) {
            return Double.NaN;
        }
        double p = switch (op) {
            case "gt", "gte" -> 1 - cdf;
            case "lt", "lte" -> cdf;
            default -> Double.NaN;
        };
        if (Double.isNaN(p)) {
            return Double.NaN;
        }
        return Math.max(MIN, Math.min(MAX, p));
    }

    /**
     * {@code P(value <= x)} interpolated from the quantile map, or {@code NaN} when there are fewer than two
     * distinct points.
     */
    public static double cdf(Map<String, Double> quantiles, double x) {
        List<double[]> points = points(quantiles);
        if (points.size() < 2) {
            return Double.NaN;
        }
        for (int i = 0; i < points.size(); i++) {
            double[] p = points.get(i);
            if (x == p[1]) {
                return p[0];
            }
            if (x < p[1]) {
                if (i == 0) {
                    return extrapolate(points.get(0), points.get(1), x);
                }
                return interpolate(points.get(i - 1), points.get(i), x);
            }
        }
        // Beyond the highest quantile: extrapolate the last segment.
        return extrapolate(points.get(points.size() - 2), points.get(points.size() - 1), x);
    }

    /** The quantile points as {@code [level, value]} ordered by level, values clamped non-decreasing. */
    private static List<double[]> points(Map<String, Double> quantiles) {
        List<double[]> points = new ArrayList<>();
        if (quantiles == null) {
            return points;
        }
        for (String key : List.of("p01", "p05", "p10", "p25", "p50", "p75", "p90", "p95", "p99")) {
            Double value = quantiles.get(key);
            if (value != null && Double.isFinite(value)) {
                points.add(new double[] {Ledger.level(key), value});
            }
        }
        // The CDF must be non-decreasing in value; clamp a crossing quantile up to its predecessor.
        for (int i = 1; i < points.size(); i++) {
            if (points.get(i)[1] < points.get(i - 1)[1]) {
                points.get(i)[1] = points.get(i - 1)[1];
            }
        }
        // Drop points that now share a value with their predecessor, keeping the lower level.
        List<double[]> distinct = new ArrayList<>();
        for (double[] p : points) {
            if (distinct.isEmpty() || p[1] > distinct.get(distinct.size() - 1)[1]) {
                distinct.add(p);
            }
        }
        return distinct;
    }

    /** The level at {@code x} between two points, by linear interpolation. */
    private static double interpolate(double[] lo, double[] hi, double x) {
        double slope = (hi[0] - lo[0]) / (hi[1] - lo[1]);
        return clampLevel(lo[0] + slope * (x - lo[1]));
    }

    /** The level at {@code x} beyond the points, by linear extrapolation of the segment, clamped to [0, 1]. */
    private static double extrapolate(double[] a, double[] b, double x) {
        double slope = (b[0] - a[0]) / (b[1] - a[1]);
        return clampLevel(a[0] + slope * (x - a[1]));
    }

    private static double clampLevel(double level) {
        return Math.max(0, Math.min(1, level));
    }
}
