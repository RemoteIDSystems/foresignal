package ai.foresignal.outlook.series;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import org.apache.commons.statistics.distribution.NormalDistribution;

/**
 * The quantile levels of Foresignal's statistical forecasts and the arithmetic on them (prompt 126): the seven ledger
 * levels p05…p95 plus the p01 and p99 tails used for the {@code significant} exceedance band; monotone adjustment by
 * rearrangement (sorting, which never increases the pinball loss); the pinball loss; empirical and normal quantiles.
 */
public final class QuantileSet {

    /** Every level, in order. */
    public static final double[] LEVELS = {0.01, 0.05, 0.10, 0.25, 0.50, 0.75, 0.90, 0.95, 0.99};

    /** The keys of {@link #LEVELS}. */
    public static final String[] KEYS = {"p01", "p05", "p10", "p25", "p50", "p75", "p90", "p95", "p99"};

    /** The index of p01 in {@link #LEVELS}. */
    public static final int P01 = 0;

    /** The index of p05. */
    public static final int P05 = 1;

    /** The index of p10. */
    public static final int P10 = 2;

    /** The index of the median. */
    public static final int P50 = 4;

    /** The index of p90. */
    public static final int P90 = 6;

    /** The index of p95. */
    public static final int P95 = 7;

    /** The index of p99. */
    public static final int P99 = 8;

    private static final NormalDistribution STANDARD = NormalDistribution.of(0, 1);

    private QuantileSet() {}

    /** True for a ledger level (p05…p95). */
    public static boolean ledgerLevel(int index) {
        return index >= P05 && index <= P95;
    }

    /** The quantiles in non-decreasing order (the rearrangement of Chernozhukov et al.), NaN-free. */
    public static double[] monotone(double[] q) {
        double[] out = q.clone();
        for (int i = 0; i < out.length; i++) {
            if (!Double.isFinite(out[i])) {
                out[i] = i == 0 ? 0 : out[i - 1];
            }
        }
        Arrays.sort(out);
        return out;
    }

    /** The pinball (quantile) loss of quantile {@code q} at level {@code tau} for outcome {@code y}. */
    public static double pinball(double y, double q, double tau) {
        double d = y - q;
        return d >= 0 ? tau * d : (tau - 1) * d;
    }

    /** The mean pinball loss over the seven ledger levels. */
    public static double meanPinball(double y, double[] q) {
        double sum = 0;
        int n = 0;
        for (int i = P05; i <= P95; i++) {
            sum += pinball(y, q[i], LEVELS[i]);
            n++;
        }
        return sum / n;
    }

    /** The ledger quantiles {@code p05…p95} as the ledger's {@code Quantiles} map. */
    public static Map<String, Double> ledger(double[] q) {
        Map<String, Double> out = new LinkedHashMap<>();
        for (int i = P05; i <= P95; i++) {
            out.put(KEYS[i], round(q[i]));
        }
        return out;
    }

    /** The tail quantiles {@code p01} and {@code p99}. */
    public static Map<String, Double> tails(double[] q) {
        Map<String, Double> out = new LinkedHashMap<>();
        out.put(KEYS[P01], round(q[P01]));
        out.put(KEYS[P99], round(q[P99]));
        return out;
    }

    /** All nine quantiles from a ledger map and a tails map (missing tails fall back to p05 and p95). */
    public static double[] fromMaps(Map<String, ?> ledger, Map<String, ?> tails) {
        double[] out = new double[LEVELS.length];
        for (int i = 0; i < KEYS.length; i++) {
            Object v = ledger != null && ledger.containsKey(KEYS[i]) ? ledger.get(KEYS[i])
                    : tails != null ? tails.get(KEYS[i]) : null;
            out[i] = v instanceof Number n ? n.doubleValue() : Double.NaN;
        }
        if (Double.isNaN(out[P01])) {
            out[P01] = out[P05];
        }
        if (Double.isNaN(out[P99])) {
            out[P99] = out[P95];
        }
        return out;
    }

    /** The type-7 empirical quantiles of a sample at every level. */
    public static double[] empirical(double[] sample) {
        double[] sorted = Arrays.stream(sample).filter(Double::isFinite).sorted().toArray();
        double[] out = new double[LEVELS.length];
        if (sorted.length == 0) {
            Arrays.fill(out, Double.NaN);
            return out;
        }
        for (int i = 0; i < LEVELS.length; i++) {
            out[i] = quantile7(sorted, LEVELS[i]);
        }
        return out;
    }

    /** The type-7 quantile of a sorted sample. */
    public static double quantile7(double[] sorted, double p) {
        if (sorted.length == 1) {
            return sorted[0];
        }
        double h = (sorted.length - 1) * p;
        int lo = (int) Math.floor(h);
        int hi = Math.min(sorted.length - 1, lo + 1);
        return sorted[lo] + (h - lo) * (sorted[hi] - sorted[lo]);
    }

    /** The quantiles of a normal distribution (a zero {@code sd} gives the mean at every level). */
    public static double[] normal(double mean, double sd) {
        double[] out = new double[LEVELS.length];
        for (int i = 0; i < LEVELS.length; i++) {
            out[i] = mean + (sd > 0 ? sd * STANDARD.inverseCumulativeProbability(LEVELS[i]) : 0);
        }
        return out;
    }

    /** The standard normal quantile of {@code p}. */
    public static double z(double p) {
        return STANDARD.inverseCumulativeProbability(p);
    }

    /** The standard normal CDF. */
    public static double phi(double x) {
        return STANDARD.cumulativeProbability(x);
    }

    /** Applies a monotone transform to every quantile (quantiles commute with monotone maps). */
    public static double[] map(double[] q, DoubleUnaryOperator f) {
        double[] out = new double[q.length];
        for (int i = 0; i < q.length; i++) {
            out[i] = f.applyAsDouble(q[i]);
        }
        return out;
    }

    /** Every quantile floored at {@code min}. */
    public static double[] floor(double[] q, double min) {
        return map(q, v -> Math.max(min, v));
    }

    /** Rounds to 6 significant decimals for storage. */
    public static double round(double v) {
        if (!Double.isFinite(v)) {
            return v;
        }
        return Math.round(v * 1e6) / 1e6;
    }
}
