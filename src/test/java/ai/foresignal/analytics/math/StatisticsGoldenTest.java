package ai.foresignal.analytics.math;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Golden values of the discovery statistics (prompt 119). Reference values were computed independently from the
 * closed forms (negative-binomial and Poisson sums of the probability mass function, the even-degree chi-squared
 * survival function, hand-worked Benjamini–Hochberg and Benjamini–Yekutieli steps).
 */
class StatisticsGoldenTest {

    @Test
    @DisplayName("median, MAD × 1.4826 with a floor, quantiles and the Anscombe transform")
    void robust() {
        double[] values = {1, 2, 3, 4, 100, Double.NaN};
        assertThat(RobustStats.median(values)).isEqualTo(3.0);
        assertThat(RobustStats.mad(values)).isEqualTo(1.0);
        assertThat(RobustStats.scale(values, 0.1)).isCloseTo(1.4826, offset(1e-12));
        assertThat(RobustStats.scale(new double[] {5, 5, 5}, 0.25)).as("a constant series uses the floor")
                .isEqualTo(0.25);
        assertThat(RobustStats.median(new double[] {4, 1, 3, 2})).isEqualTo(2.5);
        assertThat(RobustStats.median(new double[0])).isNaN();
        assertThat(RobustStats.quantile(new double[] {1, 2, 3, 4, 5}, 0.25)).isEqualTo(2.0);
        assertThat(RobustStats.anscombe(0)).isCloseTo(1.224744871391589, offset(1e-12));
        assertThat(RobustStats.anscombe(10)).isCloseTo(6.442049363362563, offset(1e-12));
        assertThat(RobustStats.anscombe(-3)).as("negative input is clamped").isCloseTo(1.224744871391589,
                offset(1e-12));
        assertThat(RobustStats.sd(new double[] {2, 4, 4, 4, 5, 5, 7, 9})).isCloseTo(2.138089935299395, offset(1e-12));
    }

    @Test
    @DisplayName("negative-binomial (method of moments) and Poisson tails")
    void tails() {
        CountTails.NegBin nb = CountTails.negBinMoments(5, 15);
        assertThat(nb.size()).isCloseTo(2.5, offset(1e-12));
        assertThat(nb.probability()).isCloseTo(1.0 / 3, offset(1e-12));
        assertThat(nb.mean()).isCloseTo(5, offset(1e-12));
        assertThat(CountTails.negBinAtLeast(12, nb)).isCloseTo(0.06704175644861288, offset(1e-10));
        assertThat(CountTails.negBinAtMost(3, nb)).isCloseTo(0.42053908496528275, offset(1e-10));
        assertThat(CountTails.negBinMoments(5, 4)).as("not overdispersed").isNull();
        assertThat(CountTails.poissonAtLeast(10, 3)).isCloseTo(0.0011024881301152645, offset(1e-12));
        assertThat(CountTails.poissonAtMost(1, 3)).isCloseTo(0.1991482734714558, offset(1e-12));
        assertThat(CountTails.poissonAtLeast(0, 3)).isEqualTo(1.0);
        assertThat(CountTails.exceedanceP(12, 5, 15)).as("overdispersed: negative binomial")
                .isCloseTo(0.06704175644861288, offset(1e-10));
        assertThat(CountTails.exceedanceP(10, 3, 2)).as("not overdispersed: Poisson")
                .isCloseTo(0.0011024881301152645, offset(1e-12));
    }

    @Test
    @DisplayName("Fisher's combination against the closed-form chi-squared survival function")
    void fisher() {
        assertThat(CountTails.fisher(0.01, 0.02)).isCloseTo(0.001903438638283249, offset(1e-12));
        assertThat(CountTails.fisher(0.2, 0.5, 0.05)).isCloseTo(0.10167200412440151, offset(1e-12));
        assertThat(CountTails.fisher()).isEqualTo(1.0);
        assertThat(CountTails.fisher(0.0, 0.5)).as("p = 0 is bounded").isBetween(0.0, 1e-200);
    }

    @Test
    @DisplayName("Benjamini–Hochberg and Benjamini–Yekutieli adjusted p-values, in input order")
    void falseDiscovery() {
        double[] p = {0.01, 0.04, 0.03, 0.005};
        assertThat(MultipleTesting.benjaminiHochberg(p)).containsExactly(new double[] {0.02, 0.04, 0.04, 0.02},
                offset(1e-12));
        assertThat(MultipleTesting.benjaminiYekutieli(p)).containsExactly(new double[] {0.041666666666666664,
            0.08333333333333333, 0.08333333333333333, 0.041666666666666664}, offset(1e-12));
        assertThat(MultipleTesting.rejected(MultipleTesting.benjaminiHochberg(p), 0.03))
                .containsExactly(true, false, false, true);
        assertThat(MultipleTesting.benjaminiHochberg(new double[] {0.9, 0.95})).containsExactly(
                new double[] {0.95, 0.95}, offset(1e-12));
        assertThat(MultipleTesting.benjaminiHochberg(new double[0])).isEmpty();
    }

    @Test
    @DisplayName("the Corrado rank test finds a planted event-window shift and stays quiet on noise")
    void corrado() {
        Random random = new Random(119);
        double[][] shifted = new double[30][];
        double[][] noise = new double[30][];
        for (int e = 0; e < 30; e++) {
            shifted[e] = new double[260];
            noise[e] = new double[260];
            for (int t = 0; t < 260; t++) {
                shifted[e][t] = random.nextGaussian();
                noise[e][t] = random.nextGaussian();
            }
            shifted[e][250] += 3;
            shifted[e][251] += 3;
        }
        CorradoRank.Result planted = CorradoRank.test(shifted, 250, 251);
        assertThat(planted.statistic()).isGreaterThan(4);
        assertThat(planted.p()).isLessThan(0.001);
        CorradoRank.Result quiet = CorradoRank.test(noise, 250, 251);
        assertThat(quiet.p()).isGreaterThan(0.01);
        assertThat(CorradoRank.centeredRanks(new double[] {3, 1, 1, Double.NaN})).containsExactly(
                new double[] {0.25, -0.125, -0.125, Double.NaN}, offset(1e-12));
        assertThat(CorradoRank.test(new double[0][], 0, 0).p()).isNaN();
    }

    @Test
    @DisplayName("BOCPD puts its posterior on a planted change and none on a stationary series")
    void bocpd() {
        Random random = new Random(7);
        double[] changed = new double[130];
        double[] steady = new double[130];
        for (int t = 0; t < 130; t++) {
            changed[t] = poisson(random, t < 110 ? 5 : 15);
            steady[t] = poisson(random, 5);
        }
        Bocpd.Params params = new Bocpd.Params(Bocpd.Model.POISSON_GAMMA, 1.0 / 250, 300, 5, 1, 1);
        Bocpd.Result planted = Bocpd.run(changed, params);
        assertThat(planted.changeWithin(25)).isGreaterThan(0.9);
        assertThat(planted.mapRunLength()).isBetween(15, 25);
        Bocpd.Result quiet = Bocpd.run(steady, params);
        assertThat(quiet.changeWithin(20)).isLessThan(0.2);
        assertThat(quiet.observations()).isEqualTo(130);
        double sum = 0;
        for (double v : quiet.posterior()) {
            sum += v;
        }
        assertThat(sum).as("the posterior is normalized").isCloseTo(1.0, offset(1e-9));

        double[] levels = new double[120];
        for (int t = 0; t < 120; t++) {
            levels[t] = (t < 100 ? 0 : 4) + random.nextGaussian();
        }
        Bocpd.Result gaussian = Bocpd.run(levels, new Bocpd.Params(Bocpd.Model.NORMAL_GAMMA, 1.0 / 250, 300, 0, 1, 1));
        assertThat(gaussian.changeWithin(25)).isGreaterThan(0.9);
        assertThatThrownBy(() -> new Bocpd.Params(Bocpd.Model.POISSON_GAMMA, 0, 300, 1, 1, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("CUSUM: the reference value, a planted step alarms, and the simulated threshold is deterministic")
    void cusum() {
        assertThat(Cusum.referenceValue(10, 15)).isCloseTo(5 / Math.log(1.5), offset(1e-12));
        Cusum.Params up = new Cusum.Params(Cusum.Model.POISSON, 1, 0.5, 1.0, 1);
        CusumThresholds a = new CusumThresholds();
        CusumThresholds b = new CusumThresholds();
        double h = a.threshold(up, 10, 365);
        assertThat(h).isPositive().isEqualTo(b.threshold(up, 10, 365));
        assertThat(a.threshold(up, 10.04, 365)).as("rounded means share a cached threshold").isEqualTo(h);
        assertThat(a.size()).isEqualTo(1);
        assertThat(CusumThresholds.simulate(up, 10, 365, 42)).isEqualTo(CusumThresholds.simulate(up, 10, 365, 42));
        assertThat(a.threshold(up, 10, 50)).as("a shorter run length needs a lower threshold").isLessThan(h);

        double[] x = new double[200];
        double[] mu0 = new double[200];
        java.util.Arrays.fill(mu0, 10);
        Random random = new Random(3);
        for (int t = 0; t < 200; t++) {
            x[t] = poisson(random, t < 150 ? 10 : 20);
        }
        Cusum.Run run = Cusum.run(x, mu0, null, new boolean[200], up, h);
        assertThat(run.alarms()).isNotEmpty();
        Cusum.Alarm first = run.alarms().stream().filter(al -> al.alarmIndex() >= 150).findFirst().orElseThrow();
        assertThat(first.alarmIndex()).isBetween(150, 165);
        assertThat(first.changeIndex()).isBetween(140, 155);
        assertThat(first.delay()).isNotNegative();

        boolean[] gap = new boolean[200];
        gap[160] = true;
        Cusum.Run cut = Cusum.run(x, mu0, null, gap, up, Double.MAX_VALUE);
        assertThat(cut.canceled()).as("a gap cancels an accumulating candidate").contains(160);
        assertThatThrownBy(() -> new Cusum.Params(Cusum.Model.POISSON, 0, 0.5, 1, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("the placebo sampler is deterministic per seed and the resampled means and shares behave")
    void placebo() {
        int[] eligible = new int[1000];
        for (int i = 0; i < 1000; i++) {
            eligible[i] = i;
        }
        int[] first = PlaceboSampler.sample(eligible, 500, Seeds.of("study", "range"));
        assertThat(first).hasSize(500).isSorted().doesNotHaveDuplicates()
                .isEqualTo(PlaceboSampler.sample(eligible, 500, Seeds.of("study", "range")));
        assertThat(PlaceboSampler.sample(eligible, 500, Seeds.of("other"))).isNotEqualTo(first);
        assertThat(PlaceboSampler.sample(new int[] {3, 1, 3}, 500, 1)).containsExactly(1, 3);
        assertThat(PlaceboSampler.share(new double[] {0.5, -2, 3}, 2.5)).isCloseTo(0.5, offset(1e-12));
        assertThat(PlaceboSampler.resampledMeans(new double[] {1, 1, 1}, 4, 3, 9)).containsExactly(1.0, 1.0, 1.0);
        assertThat(Seeds.hex("a")).hasSize(16);
        assertThat(Seeds.of("a", "b")).isNotEqualTo(Seeds.of("ab"));
    }

    @Test
    @DisplayName("the Wilson interval lives in analytics.math and the 103 class delegates to it")
    void wilson() {
        assertThat(Wilson.interval(5, 10)).containsExactly(ai.foresignal.backtest.Wilson.interval(5, 10));
        assertThat(Wilson.interval(0, 0)).containsExactly(0.0, 1.0);
        assertThat(Wilson.interval(5, 10)[0]).isCloseTo(0.236593090512564, offset(1e-9));
    }

    private static int poisson(Random random, double lambda) {
        double l = Math.exp(-lambda);
        int k = 0;
        double p = 1;
        do {
            k++;
            p *= random.nextDouble();
        } while (p > l);
        return k - 1;
    }
}
