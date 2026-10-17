package ai.foresignal.forecast.score;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link Calibration} (prompt 122): pool-adjacent-violators on a known sequence, the CORP decomposition summing to the
 * score, the Murphy identity with its within-bin terms, the 10-bin table and sharpness.
 */
class CorpReliabilityTest {

    @Test
    @DisplayName("PAV pools adjacent violators into the non-decreasing least-squares fit")
    void pavKnownSequence() {
        // Outcomes in forecast order 1, 0, 1, 1, 0 (one forecast each): {1, 0} pool to 0.5; {1, 1, 0}: the
        // last violator pools with the pair before it → 2/3 for the last three.
        List<double[]> blocks = new ArrayList<>();
        double[] outcomes = {1, 0, 1, 1, 0};
        for (int i = 0; i < outcomes.length; i++) {
            blocks.add(new double[] {outcomes[i], 1, i, i, i / 10.0});
        }
        double[] fit = Calibration.pav(blocks);
        assertThat(fit[0]).isCloseTo(0.5, within(1e-12));
        assertThat(fit[1]).isCloseTo(0.5, within(1e-12));
        assertThat(fit[2]).isCloseTo(2.0 / 3, within(1e-12));
        assertThat(fit[3]).isCloseTo(2.0 / 3, within(1e-12));
        assertThat(fit[4]).isCloseTo(2.0 / 3, within(1e-12));
        for (int i = 1; i < fit.length; i++) {
            assertThat(fit[i]).isGreaterThanOrEqualTo(fit[i - 1]);
        }
    }

    @Test
    @DisplayName("CORP: equal forecasts form one block, and MCB − DSC + UNC equals the score")
    void corpDecomposition() {
        double[] p = {0.1, 0.1, 0.4, 0.4, 0.6, 0.8, 0.9, 0.9};
        double[] o = {0, 1, 0, 0, 1, 0, 1, 1};
        Calibration.Corp c = Calibration.corp(p, o);
        assertThat(c.curve()).extracting(Calibration.Point::forecast).containsExactly(0.1, 0.4, 0.6, 0.8, 0.9);
        assertThat(c.miscalibration() - c.discrimination() + c.uncertainty()).isCloseTo(c.score(), within(1e-12));
        assertThat(c.miscalibration()).isGreaterThanOrEqualTo(-1e-12);
        assertThat(c.discrimination()).isGreaterThanOrEqualTo(-1e-12);
        assertThat(c.uncertainty()).isCloseTo(0.5 * 0.5, within(1e-12));
        // The tied 0.1 forecasts share one calibrated value.
        assertThat(c.calibrated()[0]).isEqualTo(c.calibrated()[1]);
    }

    @Test
    @DisplayName("CORP decomposition sums to the score on random samples")
    void corpRandom() {
        Random r = new Random(42);
        for (int trial = 0; trial < 20; trial++) {
            int n = 50 + r.nextInt(200);
            double[] p = new double[n];
            double[] o = new double[n];
            for (int i = 0; i < n; i++) {
                p[i] = Math.round(r.nextDouble() * 20) / 20.0;
                o[i] = r.nextDouble() < p[i] ? 1 : 0;
            }
            Calibration.Corp c = Calibration.corp(p, o);
            assertThat(c.miscalibration() - c.discrimination() + c.uncertainty()).isCloseTo(c.score(),
                    within(1e-12));
        }
    }

    @Test
    @DisplayName("Murphy: BS = REL − RES + UNC + WBV − 2 WBC exactly")
    void murphy() {
        double[] p = {0.05, 0.12, 0.33, 0.38, 0.55, 0.71, 0.74, 0.96, 0.99, 0.5};
        double[] o = {0, 0, 1, 0, 1, 1, 0, 1, 1, 0};
        Calibration.Murphy m = Calibration.murphy(p, o);
        double rebuilt = m.reliability() - m.resolution() + m.uncertainty() + m.withinBinVariance()
                - 2 * m.withinBinCovariance();
        assertThat(rebuilt).isCloseTo(m.brier(), within(1e-12));
        assertThat(Calibration.murphy(new double[0], new double[0])).isNull();
    }

    @Test
    @DisplayName("ten bins with counts, empty bins without means; sharpness is the forecast variance")
    void binsAndSharpness() {
        double[] p = {0.05, 0.15, 0.15, 0.95, 1.0};
        double[] o = {0, 1, 0, 1, 1};
        List<Calibration.Bin> bins = Calibration.bins(p, o);
        assertThat(bins).hasSize(10);
        assertThat(bins.get(1).n()).isEqualTo(2);
        assertThat(bins.get(1).observedFrequency()).isCloseTo(0.5, within(1e-12));
        assertThat(bins.get(5).meanForecast()).isNull();
        assertThat(bins.get(9).n()).isEqualTo(2);
        assertThat(Calibration.sharpness(new double[] {0.5, 0.5})).isZero();
        assertThat(Calibration.sharpness(new double[] {0.0, 1.0})).isCloseTo(0.25, within(1e-12));
    }
}
