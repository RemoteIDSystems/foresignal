package ai.foresignal.analytics.math;

import java.util.Arrays;
import java.util.Comparator;

/**
 * False-discovery-rate control (prompt 119): Benjamini–Hochberg step-up adjusted p-values, and the
 * Benjamini–Yekutieli variant that stays valid under arbitrary dependence (the BH values times {@code Σ 1/i}).
 * A hypothesis is rejected at level {@code q} when its adjusted p-value is at most {@code q}.
 */
public final class MultipleTesting {

    private MultipleTesting() {}

    /** Benjamini–Hochberg adjusted p-values, in input order. */
    public static double[] benjaminiHochberg(double[] pValues) {
        return adjust(pValues, 1.0);
    }

    /** Benjamini–Yekutieli adjusted p-values, in input order. */
    public static double[] benjaminiYekutieli(double[] pValues) {
        int m = pValues == null ? 0 : pValues.length;
        double c = 0;
        for (int i = 1; i <= m; i++) {
            c += 1.0 / i;
        }
        return adjust(pValues, Math.max(1, c));
    }

    /** True for each hypothesis whose adjusted p-value is at most {@code q}. */
    public static boolean[] rejected(double[] adjusted, double q) {
        boolean[] out = new boolean[adjusted.length];
        for (int i = 0; i < adjusted.length; i++) {
            out[i] = adjusted[i] <= q;
        }
        return out;
    }

    private static double[] adjust(double[] pValues, double factor) {
        if (pValues == null || pValues.length == 0) {
            return new double[0];
        }
        int m = pValues.length;
        Integer[] order = new Integer[m];
        for (int i = 0; i < m; i++) {
            order[i] = i;
        }
        Arrays.sort(order, Comparator.comparingDouble((Integer i) -> sanitize(pValues[i])).thenComparingInt(i -> i));
        double[] out = new double[m];
        double running = 1;
        for (int rank = m; rank >= 1; rank--) {
            int index = order[rank - 1];
            double value = sanitize(pValues[index]) * m * factor / rank;
            running = Math.min(running, value);
            out[index] = Math.min(1, running);
        }
        return out;
    }

    private static double sanitize(double p) {
        return Double.isNaN(p) ? 1 : Math.max(0, Math.min(1, p));
    }
}
