package ai.foresignal.precursor;

import ai.foresignal.analytics.changepoint.ChangePointDetector;
import ai.foresignal.analytics.math.RobustStats;

/**
 * Pre-whitening for the lead–lag test (prompt 120): each series becomes residuals against its own baseline, so that
 * shared trends and seasons do not pass for precedence.
 *
 * <ul>
 *   <li><b>Counts:</b> the 119 count baseline (the mean of the last 8 same-slot covered values before {@code t}); the
 *       residual is the difference of Anscombe transforms {@code A(x) − A(μ₀)}, which stabilizes the Poisson variance.
 *   <li><b>Other series:</b> the series' own seasonal median and MAD: the residual is {@code (x − median of the slot) /
 *       (MAD of the slot)}, with slots of {@code period} buckets (7 for daily series, 1 otherwise).
 * </ul>
 *
 * <p>Pure: callers pass one window at a time, so validation data never shapes discovery residuals.
 */
public final class Prewhitening {

    /** The smallest robust scale used to standardize. */
    static final double SCALE_FLOOR = 1e-9;

    private Prewhitening() {}

    /**
     * The residuals of one series window.
     *
     * @param values the values ({@code NaN} where missing or uncovered)
     * @param counts true for a count series
     * @param period the slot period in buckets
     * @return the residuals, {@code NaN} where the value or its baseline is missing
     */
    public static double[] residuals(double[] values, boolean counts, int period) {
        int n = values.length;
        double[] out = new double[n];
        int step = Math.max(1, period);
        if (counts) {
            boolean[] gap = new boolean[n];
            for (int i = 0; i < n; i++) {
                gap[i] = !Double.isFinite(values[i]);
            }
            double[] mu0 = new double[n];
            ChangePointDetector.countBaseline(values, gap, step, mu0);
            for (int i = 0; i < n; i++) {
                out[i] = gap[i] || !Double.isFinite(mu0[i]) ? Double.NaN
                        : RobustStats.anscombe(values[i]) - RobustStats.anscombe(mu0[i]);
            }
            return out;
        }
        double[] median = new double[step];
        double[] scale = new double[step];
        for (int slot = 0; slot < step; slot++) {
            int size = 0;
            for (int i = slot; i < n; i += step) {
                size++;
            }
            double[] slotValues = new double[size];
            int k = 0;
            for (int i = slot; i < n; i += step) {
                slotValues[k++] = values[i];
            }
            median[slot] = RobustStats.median(slotValues);
            scale[slot] = RobustStats.scale(slotValues, SCALE_FLOOR);
        }
        for (int i = 0; i < n; i++) {
            int slot = i % step;
            out[i] = Double.isFinite(values[i]) && Double.isFinite(median[slot]) && Double.isFinite(scale[slot])
                    ? (values[i] - median[slot]) / scale[slot] : Double.NaN;
        }
        return out;
    }

    /**
     * Standardized residuals: each residual divided by the robust scale (MAD × 1.4826) of all residuals, so that a
     * threshold such as 2.0 means "two robust standard deviations".
     *
     * @param residuals the residuals
     * @return the standardized residuals
     */
    public static double[] standardize(double[] residuals) {
        double center = RobustStats.median(residuals);
        double scale = RobustStats.scale(residuals, SCALE_FLOOR);
        double[] out = new double[residuals.length];
        for (int i = 0; i < residuals.length; i++) {
            out[i] = Double.isFinite(residuals[i]) && Double.isFinite(center) && Double.isFinite(scale)
                    ? (residuals[i] - center) / scale : Double.NaN;
        }
        return out;
    }
}
