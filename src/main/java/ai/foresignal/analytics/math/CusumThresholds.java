package ai.foresignal.analytics.math;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.PoissonSampler;
import org.apache.commons.rng.sampling.distribution.SharedStateDiscreteSampler;
import org.apache.commons.rng.sampling.distribution.ZigguratSampler;
import org.apache.commons.rng.simple.RandomSource;
import org.apache.commons.statistics.distribution.ContinuousDistribution;
import org.apache.commons.statistics.distribution.GammaDistribution;

/**
 * Chooses the CUSUM alarm threshold {@code h} by simulation for a target in-control average run length (prompt 119;
 * default 365 buckets).
 *
 * <p>{@value #RUNS} in-control paths of {@code 5 × target} buckets are drawn from the model at the series' typical
 * in-control mean with a generator seeded by the parameter key, so the same parameters always give the same {@code h}
 * (INT-1). Each path is reduced to its record highs, so the run length of any {@code h} is read off without
 * re-simulating, and the smallest {@code h} whose mean run length reaches the target is found by bisection. Paths
 * that never alarm count their full length (a slight underestimate of the run length, so {@code h} errs high and
 * false alarms low). Results are cached per parameter set and rounded mean.
 */
public final class CusumThresholds {

    /** The simulated paths per threshold. */
    public static final int RUNS = 200;

    /** The bisection steps. */
    static final int STEPS = 40;

    private final Map<String, Double> cache = new ConcurrentHashMap<>();

    /** Creates an empty cache. */
    public CusumThresholds() {}

    /**
     * The threshold of {@code params} at the typical in-control mean {@code mu0} for {@code targetArl}.
     *
     * @param mu0 the typical in-control mean (ignored by the continuous form, which simulates standard residuals)
     * @param targetArl the target in-control average run length in buckets
     */
    public double threshold(Cusum.Params params, double mu0, int targetArl) {
        double typical = params.model() == Cusum.Model.GAUSSIAN ? 0 : Cusum.round2(Math.max(mu0, 0.01));
        String key = params.key() + "|" + typical + "|" + targetArl;
        return cache.computeIfAbsent(key, k -> simulate(params, typical, targetArl, Seeds.of("cusum-arl", k)));
    }

    /** The number of cached thresholds (tests). */
    public int size() {
        return cache.size();
    }

    /** Simulates and bisects; pure for given arguments. */
    static double simulate(Cusum.Params params, double mu0, int targetArl, long seed) {
        UniformRandomProvider rng = RandomSource.XO_RO_SHI_RO_128_PP.create(seed);
        int length = Math.max(10, 5 * targetArl);
        List<double[][]> records = new ArrayList<>(RUNS);
        double highest = 0;
        Draw draw = drawer(params, mu0, rng);
        for (int run = 0; run < RUNS; run++) {
            List<double[]> highs = new ArrayList<>();
            double s = 0;
            double best = 0;
            for (int t = 0; t < length; t++) {
                double x = draw.next();
                s = Math.max(0, s + Cusum.increment(params, x, params.model() == Cusum.Model.GAUSSIAN ? 0 : mu0, 1));
                if (s > best) {
                    best = s;
                    highs.add(new double[] {t + 1, s});
                }
            }
            highest = Math.max(highest, best);
            records.add(highs.toArray(new double[0][]));
        }
        double lo = 0;
        double hi = highest + 1e-9;
        if (arl(records, hi, length) < targetArl) {
            return hi;
        }
        for (int i = 0; i < STEPS; i++) {
            double mid = (lo + hi) / 2;
            if (arl(records, mid, length) >= targetArl) {
                hi = mid;
            } else {
                lo = mid;
            }
        }
        return hi;
    }

    /** The mean run length of threshold {@code h} over the recorded paths. */
    static double arl(List<double[][]> records, double h, int length) {
        double sum = 0;
        for (double[][] highs : records) {
            double rl = length;
            for (double[] high : highs) {
                if (high[1] >= h) {
                    rl = high[0];
                    break;
                }
            }
            sum += rl;
        }
        return sum / records.size();
    }

    /** One in-control draw. */
    private interface Draw {
        double next();
    }

    private static Draw drawer(Cusum.Params params, double mu0, UniformRandomProvider rng) {
        switch (params.model()) {
            case GAUSSIAN -> {
                ZigguratSampler.NormalizedGaussian gaussian = ZigguratSampler.NormalizedGaussian.of(rng);
                return gaussian::sample;
            }
            case NEG_BIN -> {
                double size = params.negBinSize();
                ContinuousDistribution.Sampler gamma = GammaDistribution.of(size, mu0 / size).createSampler(rng);
                return () -> {
                    double lambda = gamma.sample();
                    return lambda <= 0 ? 0 : PoissonSampler.of(rng, lambda).sample();
                };
            }
            default -> {
                SharedStateDiscreteSampler poisson = PoissonSampler.of(rng, mu0);
                return poisson::sample;
            }
        }
    }
}
