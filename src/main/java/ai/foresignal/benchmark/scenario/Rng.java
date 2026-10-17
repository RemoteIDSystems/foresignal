package ai.foresignal.benchmark.scenario;

import ai.foresignal.analytics.math.Seeds;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.PoissonSampler;
import org.apache.commons.rng.sampling.distribution.ZigguratSampler;
import org.apache.commons.rng.simple.RandomSource;

/**
 * The deterministic random stream of one scenario (prompt 123): a {@code XO_RO_SHI_RO_128_PP} generator seeded from
 * the SHA-256 of the suite seed and the scenario's name parts ({@link Seeds}), so the same seed and parts always give
 * the same draws.
 */
public final class Rng {

    private final UniformRandomProvider rng;
    private final ZigguratSampler.NormalizedGaussian gaussian;

    /** A stream seeded directly. */
    public Rng(long seed) {
        this.rng = RandomSource.XO_RO_SHI_RO_128_PP.create(seed);
        this.gaussian = ZigguratSampler.NormalizedGaussian.of(rng);
    }

    /**
     * A stream for a suite seed and the parts that name what it generates.
     *
     * @param seed the suite seed
     * @param parts the scenario name, replicate and purpose
     * @return the stream
     */
    public static Rng of(long seed, String... parts) {
        String[] all = new String[parts.length + 1];
        all[0] = Long.toString(seed);
        System.arraycopy(parts, 0, all, 1, parts.length);
        return new Rng(Seeds.of(all));
    }

    /** A uniform draw in [0, 1). */
    public double uniform() {
        return rng.nextDouble();
    }

    /** A uniform integer in [0, n). */
    public int nextInt(int n) {
        return rng.nextInt(n);
    }

    /** True with probability {@code p}. */
    public boolean bernoulli(double p) {
        return rng.nextDouble() < p;
    }

    /** A standard normal draw. */
    public double normal() {
        return gaussian.sample();
    }

    /** A Poisson draw with mean {@code mean} (0 for a mean at or below zero). */
    public int poisson(double mean) {
        if (!(mean > 0)) {
            return 0;
        }
        return PoissonSampler.of(rng, mean).sample();
    }

    /** A binomial thinning: each of {@code n} trials kept with probability {@code p}. */
    public int thin(int n, double p) {
        int kept = 0;
        for (int i = 0; i < n; i++) {
            if (rng.nextDouble() < p) {
                kept++;
            }
        }
        return kept;
    }
}
