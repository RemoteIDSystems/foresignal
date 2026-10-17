package ai.foresignal.analytics.changepoint;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.analytics.math.CusumThresholds;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The change-point detector (prompt 119): a planted step is found with the right delay, a stationary weekly-seasonal
 * series raises nothing (fixed seed), a coverage gap makes a candidate unknown, BOCPD confirmation is reported, and
 * the result is deterministic.
 */
class ChangePointDetectorTest {

    private static final Instant T0 = Instant.parse("2025-01-06T00:00:00Z");

    private final CusumThresholds thresholds = new CusumThresholds();

    private static Instant[] days(int n) {
        Instant[] out = new Instant[n];
        for (int i = 0; i < n; i++) {
            out[i] = T0.plus(Duration.ofDays(i));
        }
        return out;
    }

    /** Daily Poisson counts with a weekday pattern; level multiplied by {@code factor} from {@code stepAt}. */
    private static double[] counts(long seed, int n, int stepAt, double factor) {
        Random random = new Random(seed);
        double[] x = new double[n];
        for (int t = 0; t < n; t++) {
            double base = (t % 7 >= 5 ? 6 : 12);
            x[t] = poisson(random, t >= stepAt ? base * factor : base);
        }
        return x;
    }

    @Test
    @DisplayName("a planted step (×2.5) is confirmed with the change near the step and a short delay")
    void plantedStep() {
        int n = 160;
        double[] x = counts(11, n, 150, 2.5);
        ChangePointDetector.Result r = ChangePointDetector.detect(days(n), x, new boolean[n], true,
                ChangePointDetector.Params.defaults(7, 10), thresholds);
        assertThat(r.state()).isEqualTo(ChangePointDetector.State.CONFIRMED);
        assertThat(r.direction()).isEqualTo(1);
        assertThat(r.changeIndex()).isBetween(147, 152);
        assertThat(r.detectionDelay()).isBetween(0, 5);
        assertThat(r.effect()).isGreaterThan(0.8);
        assertThat(r.levelAfter()).isGreaterThan(r.levelBefore());
        assertThat(r.bocpdPosterior()).as("BOCPD confirms the change").isGreaterThan(0.8);
        assertThat(r.changeAt()).isEqualTo(T0.plus(Duration.ofDays(r.changeIndex())));
        assertThat(r.historyBuckets()).isPositive();
    }

    @Test
    @DisplayName("a planted fall is reported as a downward candidate")
    void plantedFall() {
        int n = 160;
        double[] x = counts(5, n, 150, 0.2);
        ChangePointDetector.Result r = ChangePointDetector.detect(days(n), x, new boolean[n], true,
                ChangePointDetector.Params.defaults(7, 10), thresholds);
        assertThat(r.state()).isEqualTo(ChangePointDetector.State.CONFIRMED);
        assertThat(r.downward()).isTrue();
    }

    @Test
    @DisplayName("a stationary seasonal series raises no recent alarm (fixed seed)")
    void stationary() {
        int n = 400;
        double[] x = counts(23, n, Integer.MAX_VALUE, 1);
        ChangePointDetector.Result r = ChangePointDetector.detect(days(n), x, new boolean[n], true,
                ChangePointDetector.Params.defaults(7, 7), thresholds);
        assertThat(r.state()).isEqualTo(ChangePointDetector.State.NONE);
    }

    @Test
    @DisplayName("a coverage gap after the change makes the candidate unknown, never a drop")
    void coverageGap() {
        int n = 160;
        double[] x = counts(11, n, 150, 2.5);
        boolean[] gap = new boolean[n];
        gap[157] = true;
        x[157] = Double.NaN;
        ChangePointDetector.Result r = ChangePointDetector.detect(days(n), x, gap, true,
                ChangePointDetector.Params.defaults(7, 10), thresholds);
        assertThat(r.state()).isEqualTo(ChangePointDetector.State.UNKNOWN);
    }

    @Test
    @DisplayName("a step too recent to have persisted for three buckets is not reported yet")
    void notYetPersisted() {
        int n = 152;
        double[] x = counts(11, n, 150, 4);
        ChangePointDetector.Result r = ChangePointDetector.detect(days(n), x, new boolean[n], true,
                ChangePointDetector.Params.defaults(7, 10), thresholds);
        assertThat(r.state()).isNotEqualTo(ChangePointDetector.State.CONFIRMED);
    }

    @Test
    @DisplayName("a continuous level shift is found in standard deviations; identical input gives identical output")
    void continuousAndDeterministic() {
        int n = 200;
        Random random = new Random(9);
        double[] x = new double[n];
        for (int t = 0; t < n; t++) {
            x[t] = 80 + (t >= 190 ? 6 : 0) + random.nextGaussian();
        }
        ChangePointDetector.Params p = ChangePointDetector.Params.defaults(7, 10);
        ChangePointDetector.Result r = ChangePointDetector.detect(days(n), x, new boolean[n], false, p, thresholds);
        assertThat(r.state()).isEqualTo(ChangePointDetector.State.CONFIRMED);
        assertThat(r.model()).isEqualTo("GAUSSIAN");
        assertThat(r.effect()).isGreaterThan(2);
        ChangePointDetector.Result again = ChangePointDetector.detect(days(n), x.clone(), new boolean[n], false, p,
                new CusumThresholds());
        assertThat(again).isEqualTo(r);
    }

    @Test
    @DisplayName("the count baseline uses only same-slot values before t")
    void baseline() {
        double[] x = new double[30];
        for (int t = 0; t < 30; t++) {
            x[t] = t % 7 == 0 ? 100 : 1;
        }
        double[] mu0 = new double[30];
        ChangePointDetector.countBaseline(x, new boolean[30], 7, mu0);
        assertThat(mu0[27]).as("too few same-slot values").isNaN();
        assertThat(mu0[28]).as("index 28 has four Mondays before it").isEqualTo(100.0);
        assertThat(ChangePointService.params(ai.foresignal.analytics.series.SeriesFrequency.WEEK).period())
                .isEqualTo(1);
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
