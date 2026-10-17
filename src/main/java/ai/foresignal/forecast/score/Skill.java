package ai.foresignal.forecast.score;

import ai.foresignal.analytics.math.Seeds;
import java.util.Arrays;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;

/**
 * Skill, paired comparison and the bias monitor (prompt 122, Intelligence-Design §8.3). Skill is
 * {@code 1 − S / S_ref} against a reference forecaster on the same questions; comparisons use only shared questions
 * and a paired bootstrap 95% interval with {@value #RESAMPLES} resamples and a deterministic seed (INT-1).
 */
public final class Skill {

    /** The bootstrap resamples. */
    public static final int RESAMPLES = 1_000;

    /** The interval level. */
    public static final double LEVEL = 0.95;

    /** The hedging band of the bias monitor. */
    public static final double HEDGE_LOW = 0.4;

    /** The hedging band's upper edge. */
    public static final double HEDGE_HIGH = 0.6;

    private Skill() {}

    /** {@code 1 − s / sRef}, or NaN when the reference scores zero. */
    public static double skill(double s, double sRef) {
        return sRef <= 0 ? Double.NaN : 1.0 - s / sRef;
    }

    /** The mean of the values (NaN when empty). */
    public static double mean(double[] values) {
        if (values.length == 0) {
            return Double.NaN;
        }
        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.length;
    }

    /** The seed of a comparison (stable for the same inputs). */
    public static long seed(String... parts) {
        return Seeds.of(parts);
    }

    /**
     * The paired bootstrap percentile interval of {@code mean(a − b)}: question indices are resampled with
     * replacement, so each resample keeps the pairs together.
     *
     * @return {@code {lower, upper}}, or null with fewer than two pairs
     */
    public static double[] pairedDifference(double[] a, double[] b, long seed) {
        int n = Math.min(a.length, b.length);
        if (n < 2) {
            return null;
        }
        UniformRandomProvider rng = RandomSource.XO_RO_SHI_RO_128_PP.create(seed);
        double[] stats = new double[RESAMPLES];
        for (int r = 0; r < RESAMPLES; r++) {
            double sum = 0;
            for (int k = 0; k < n; k++) {
                int i = rng.nextInt(n);
                sum += a[i] - b[i];
            }
            stats[r] = sum / n;
        }
        return percentile(stats);
    }

    /**
     * The paired bootstrap percentile interval of the skill {@code 1 − mean(s) / mean(ref)}.
     *
     * @return {@code {lower, upper}}, or null with fewer than two pairs
     */
    public static double[] pairedSkill(double[] s, double[] ref, long seed) {
        int n = Math.min(s.length, ref.length);
        if (n < 2) {
            return null;
        }
        UniformRandomProvider rng = RandomSource.XO_RO_SHI_RO_128_PP.create(seed);
        double[] stats = new double[RESAMPLES];
        int kept = 0;
        for (int r = 0; r < RESAMPLES; r++) {
            double sumS = 0;
            double sumRef = 0;
            for (int k = 0; k < n; k++) {
                int i = rng.nextInt(n);
                sumS += s[i];
                sumRef += ref[i];
            }
            if (sumRef > 0) {
                stats[kept++] = 1.0 - sumS / sumRef;
            }
        }
        return kept < 2 ? null : percentile(Arrays.copyOf(stats, kept));
    }

    private static double[] percentile(double[] stats) {
        double[] sorted = stats.clone();
        Arrays.sort(sorted);
        double alpha = (1 - LEVEL) / 2;
        return new double[] {quantile(sorted, alpha), quantile(sorted, 1 - alpha)};
    }

    /** The type-7 (linear interpolation) quantile of sorted values. */
    public static double quantile(double[] sorted, double q) {
        if (sorted.length == 0) {
            return Double.NaN;
        }
        double h = (sorted.length - 1) * q;
        int lo = (int) Math.floor(h);
        int hi = (int) Math.ceil(h);
        return sorted[lo] + (h - lo) * (sorted[hi] - sorted[lo]);
    }

    /**
     * The bias monitor of a forecaster in a category.
     *
     * @param meanForecastMinusObserved mean final forecast minus observed frequency (positive: says yes too often)
     * @param hedgedShare the share of its forecasts in [0.4, 0.6]
     * @param meanForecastOnNo the mean final forecast on questions that resolved no, or null when none did
     */
    public record Bias(double meanForecastMinusObserved, double hedgedShare, Double meanForecastOnNo) {}

    /**
     * The bias monitor.
     *
     * @param finalP the final forecasts of resolved binary questions
     * @param o their outcomes (1 or 0)
     * @param hedged how many of all its forecasts on those questions were in [0.4, 0.6]
     * @param forecasts how many forecasts it made on them
     * @return the monitor, or null without questions
     */
    public static Bias bias(double[] finalP, double[] o, int hedged, int forecasts) {
        if (finalP.length == 0) {
            return null;
        }
        double sumP = 0;
        double sumO = 0;
        double sumNo = 0;
        int nNo = 0;
        for (int i = 0; i < finalP.length; i++) {
            sumP += finalP[i];
            sumO += o[i];
            if (o[i] == 0.0) {
                sumNo += finalP[i];
                nNo++;
            }
        }
        double n = finalP.length;
        return new Bias(sumP / n - sumO / n, forecasts == 0 ? 0.0 : hedged / (double) forecasts,
                nNo == 0 ? null : sumNo / nNo);
    }

    /** True when {@code p} is a hedged forecast (in [0.4, 0.6]). */
    public static boolean hedged(double p) {
        return p >= HEDGE_LOW && p <= HEDGE_HIGH;
    }
}
