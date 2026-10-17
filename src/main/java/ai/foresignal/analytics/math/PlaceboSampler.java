package ai.foresignal.analytics.math;

import java.util.Arrays;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;

/**
 * The deterministic placebo date sampler (prompt 119): draws pseudo-event positions without replacement from the
 * eligible positions (periods without qualifying events, with full windows) using a generator seeded from the study's
 * definition and range, so a study always uses the same placebo dates (INT-1).
 */
public final class PlaceboSampler {

    /** The default number of placebo dates. */
    public static final int DEFAULT_COUNT = 500;

    private PlaceboSampler() {}

    /**
     * Samples up to {@code n} of the eligible positions.
     *
     * @param eligible the eligible positions (any order; duplicates are kept once)
     * @param n the sample size
     * @param seed the seed
     * @return the chosen positions, ascending; every eligible position when there are at most {@code n}
     */
    public static int[] sample(int[] eligible, int n, long seed) {
        int[] pool = Arrays.stream(eligible == null ? new int[0] : eligible).distinct().sorted().toArray();
        if (pool.length <= n) {
            return pool;
        }
        UniformRandomProvider rng = RandomSource.XO_RO_SHI_RO_128_PP.create(seed);
        for (int i = 0; i < n; i++) {
            int j = i + rng.nextInt(pool.length - i);
            int tmp = pool[i];
            pool[i] = pool[j];
            pool[j] = tmp;
        }
        int[] out = Arrays.copyOf(pool, n);
        Arrays.sort(out);
        return out;
    }

    /**
     * Draws {@code sets} means of {@code size} values sampled with replacement from {@code values}, seeded: the
     * placebo distribution of a cross-event average when only single-event placebo values were computed.
     */
    public static double[] resampledMeans(double[] values, int size, int sets, long seed) {
        if (values == null || values.length == 0 || size <= 0) {
            return new double[0];
        }
        UniformRandomProvider rng = RandomSource.XO_RO_SHI_RO_128_PP.create(seed);
        double[] out = new double[sets];
        for (int s = 0; s < sets; s++) {
            double sum = 0;
            for (int i = 0; i < size; i++) {
                sum += values[rng.nextInt(values.length)];
            }
            out[s] = sum / size;
        }
        return out;
    }

    /** The share of {@code placebo} values whose absolute value is at least {@code |observed|} (with a +1 correction). */
    public static double share(double[] placebo, double observed) {
        if (placebo == null || placebo.length == 0 || !Double.isFinite(observed)) {
            return Double.NaN;
        }
        int extreme = 0;
        for (double v : placebo) {
            if (Math.abs(v) >= Math.abs(observed)) {
                extreme++;
            }
        }
        return (extreme + 1.0) / (placebo.length + 1.0);
    }
}
