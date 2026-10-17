package ai.foresignal.analytics.math;

import java.util.Arrays;

/**
 * Robust location and scale (prompt 119): the median, the median absolute deviation scaled to the normal standard
 * deviation ({@value #MAD_SCALE}), a floored scale, the Anscombe transform and plain moments. Non-finite values are
 * ignored everywhere.
 */
public final class RobustStats {

    /** The factor that makes the MAD a consistent estimator of the normal standard deviation. */
    public static final double MAD_SCALE = 1.4826;

    private RobustStats() {}

    /** The finite values of {@code values}, sorted. */
    static double[] finiteSorted(double[] values) {
        if (values == null) {
            return new double[0];
        }
        double[] out = Arrays.stream(values).filter(Double::isFinite).toArray();
        Arrays.sort(out);
        return out;
    }

    /** The median of the finite values, or {@code NaN} when there are none. */
    public static double median(double[] values) {
        double[] sorted = finiteSorted(values);
        return medianOfSorted(sorted);
    }

    private static double medianOfSorted(double[] sorted) {
        int n = sorted.length;
        if (n == 0) {
            return Double.NaN;
        }
        return n % 2 == 1 ? sorted[n / 2] : (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0;
    }

    /** The unscaled median absolute deviation from the median, or {@code NaN} when there are no finite values. */
    public static double mad(double[] values) {
        double[] sorted = finiteSorted(values);
        if (sorted.length == 0) {
            return Double.NaN;
        }
        double m = medianOfSorted(sorted);
        double[] deviations = new double[sorted.length];
        for (int i = 0; i < sorted.length; i++) {
            deviations[i] = Math.abs(sorted[i] - m);
        }
        Arrays.sort(deviations);
        return medianOfSorted(deviations);
    }

    /**
     * The robust scale {@code max(MAD × 1.4826, floor)}; {@code floor} also when the MAD is undefined.
     *
     * @param floor the smallest scale returned (keeps a constant series from dividing by zero)
     */
    public static double scale(double[] values, double floor) {
        double mad = mad(values);
        double scaled = Double.isFinite(mad) ? mad * MAD_SCALE : 0;
        return Math.max(scaled, floor);
    }

    /** The Anscombe variance-stabilizing transform of a count, {@code 2√(x + 3/8)}; negative input is clamped to 0. */
    public static double anscombe(double x) {
        return 2 * Math.sqrt(Math.max(0, x) + 0.375);
    }

    /** The mean of the finite values, or {@code NaN}. */
    public static double mean(double[] values) {
        double sum = 0;
        int n = 0;
        if (values != null) {
            for (double v : values) {
                if (Double.isFinite(v)) {
                    sum += v;
                    n++;
                }
            }
        }
        return n == 0 ? Double.NaN : sum / n;
    }

    /** The sample variance (n − 1) of the finite values, or {@code NaN} with fewer than two. */
    public static double variance(double[] values) {
        double mean = mean(values);
        double sum = 0;
        int n = 0;
        if (values != null) {
            for (double v : values) {
                if (Double.isFinite(v)) {
                    sum += (v - mean) * (v - mean);
                    n++;
                }
            }
        }
        return n < 2 ? Double.NaN : sum / (n - 1);
    }

    /** The sample standard deviation of the finite values, or {@code NaN} with fewer than two. */
    public static double sd(double[] values) {
        double v = variance(values);
        return Double.isFinite(v) ? Math.sqrt(v) : Double.NaN;
    }

    /** The number of finite values. */
    public static int count(double[] values) {
        int n = 0;
        if (values != null) {
            for (double v : values) {
                if (Double.isFinite(v)) {
                    n++;
                }
            }
        }
        return n;
    }

    /**
     * The {@code q} quantile of the finite values by linear interpolation between order statistics (type 7), or
     * {@code NaN} when there are none.
     */
    public static double quantile(double[] values, double q) {
        double[] sorted = finiteSorted(values);
        if (sorted.length == 0) {
            return Double.NaN;
        }
        double h = (sorted.length - 1) * Math.max(0, Math.min(1, q));
        int lo = (int) Math.floor(h);
        int hi = Math.min(sorted.length - 1, lo + 1);
        return sorted[lo] + (h - lo) * (sorted[hi] - sorted[lo]);
    }
}
