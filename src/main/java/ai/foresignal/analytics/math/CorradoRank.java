package ai.foresignal.analytics.math;

import java.util.Arrays;
import java.util.Comparator;
import org.apache.commons.statistics.distribution.NormalDistribution;

/**
 * The Corrado (1989) nonparametric rank test of event-window abnormal values, with Cowan's (1992) extension to
 * multi-bucket windows (prompt 119).
 *
 * <p>Each event contributes its abnormal values over the combined estimation and event windows on a common offset
 * axis ({@code NaN} where missing). Within each event the values are ranked (ties take their average rank) and turned
 * into centered uniform ranks {@code U = rank/(n + 1) − ½}; {@code K̄ₜ} is their cross-event mean at offset {@code t},
 * {@code S = √(mean K̄ₜ²)} over every offset, and the statistic of a window of {@code L} offsets is
 * {@code Σ K̄ₜ / (√L · S)}, approximately standard normal under the null. The p-value is two-sided.
 */
public final class CorradoRank {

    private static final NormalDistribution STANDARD = NormalDistribution.of(0, 1);

    /**
     * The test result.
     *
     * @param statistic the standardized statistic, or {@code NaN} when it cannot be computed
     * @param p the two-sided p-value, or {@code NaN}
     */
    public record Result(double statistic, double p) {}

    private CorradoRank() {}

    /**
     * Runs the test.
     *
     * @param values per event, the abnormal values on the common offset axis
     * @param windowFrom the first index of the test window on that axis
     * @param windowTo the last index of the test window (inclusive)
     */
    public static Result test(double[][] values, int windowFrom, int windowTo) {
        if (values == null || values.length == 0) {
            return new Result(Double.NaN, Double.NaN);
        }
        int width = values[0].length;
        double[] sum = new double[width];
        int[] count = new int[width];
        for (double[] event : values) {
            double[] u = centeredRanks(event);
            for (int t = 0; t < width && t < u.length; t++) {
                if (Double.isFinite(u[t])) {
                    sum[t] += u[t];
                    count[t]++;
                }
            }
        }
        double ss = 0;
        int used = 0;
        double[] kbar = new double[width];
        for (int t = 0; t < width; t++) {
            if (count[t] > 0) {
                kbar[t] = sum[t] / count[t];
                ss += kbar[t] * kbar[t];
                used++;
            } else {
                kbar[t] = Double.NaN;
            }
        }
        if (used == 0) {
            return new Result(Double.NaN, Double.NaN);
        }
        double s = Math.sqrt(ss / used);
        double windowSum = 0;
        int windowLength = 0;
        for (int t = Math.max(0, windowFrom); t <= Math.min(width - 1, windowTo); t++) {
            if (Double.isFinite(kbar[t])) {
                windowSum += kbar[t];
                windowLength++;
            }
        }
        if (windowLength == 0 || !(s > 0)) {
            return new Result(Double.NaN, Double.NaN);
        }
        double z = windowSum / (Math.sqrt(windowLength) * s);
        return new Result(z, 2 * STANDARD.survivalProbability(Math.abs(z)));
    }

    /** {@code rank/(n + 1) − ½} of the finite values, ties averaged; {@code NaN} elsewhere. */
    static double[] centeredRanks(double[] values) {
        int n = values.length;
        double[] out = new double[n];
        Arrays.fill(out, Double.NaN);
        Integer[] index = Arrays.stream(range(n)).filter(i -> Double.isFinite(values[i])).boxed()
                .toArray(Integer[]::new);
        Arrays.sort(index, Comparator.comparingDouble(i -> values[i]));
        int m = index.length;
        int i = 0;
        while (i < m) {
            int j = i;
            while (j + 1 < m && values[index[j + 1]] == values[index[i]]) {
                j++;
            }
            double rank = (i + j) / 2.0 + 1;
            for (int k = i; k <= j; k++) {
                out[index[k]] = rank / (m + 1) - 0.5;
            }
            i = j + 1;
        }
        return out;
    }

    private static int[] range(int n) {
        int[] out = new int[n];
        for (int i = 0; i < n; i++) {
            out[i] = i;
        }
        return out;
    }
}
