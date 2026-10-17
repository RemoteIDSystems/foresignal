package ai.foresignal.outlook.regional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.analytics.math.NbSampling;
import ai.foresignal.baserate.reference.ReferenceCounts;
import ai.foresignal.outlook.series.QuantileSet;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The regional outlook model (prompt 126): fit, pooling, window quantiles, fallback, evaluation and eligibility. */
class RegionalOutlookModelTest {

    static final LocalDate FIRST = LocalDate.of(2022, 1, 3);

    /** Weekly counts with an annual cycle around {@code level}. */
    static long[] weekly(int n, double level, long seed) {
        NbSampling sampler = new NbSampling(seed);
        long[] y = new long[n];
        for (int t = 0; t < n; t++) {
            double mu = level * Math.exp(0.4 * Math.sin(2 * Math.PI * FIRST.plusWeeks(t).getDayOfYear() / 365.25));
            y[t] = sampler.negBin(mu, 6);
        }
        return y;
    }

    @Test
    @DisplayName("six monotone 4-week windows whose medians track four weeks of the level; deterministic per seed")
    void windows() {
        long[] y = weekly(200, 10, 1);
        RegionalOutlookModel.Input in = new RegionalOutlookModel.Input(y, FIRST, null, null);
        RegionalOutlookModel.Fit fit = RegionalOutlookModel.fit(in, in.size());
        assertThat(fit.fallback()).isFalse();
        double[][] q = RegionalOutlookModel.windows(fit, in, in.size(), 1, RegionalOutlookModel.WINDOWS, 42, 60);
        double[][] again = RegionalOutlookModel.windows(fit, in, in.size(), 1, RegionalOutlookModel.WINDOWS, 42, 60);
        assertThat(q).hasNumberOfRows(RegionalOutlookModel.WINDOWS);
        for (int w = 0; w < q.length; w++) {
            for (int i = 1; i < q[w].length; i++) {
                assertThat(q[w][i]).isGreaterThanOrEqualTo(q[w][i - 1]);
            }
            assertThat(q[w][QuantileSet.P50]).isBetween(15.0, 80.0);
            assertThat(q[w]).containsExactly(again[w]);
        }
        assertThat(fit.view()).containsEntry("model", "nb-glm");
        assertThat(RegionalOutlookModel.terms(false, false)).hasSize(7);
    }

    @Test
    @DisplayName("too little history uses the fallback mean with its NB dispersion")
    void fallback() {
        long[] y = weekly(30, 4, 2);
        RegionalOutlookModel.Input in = new RegionalOutlookModel.Input(y, FIRST, null, null);
        RegionalOutlookModel.Fit fit = RegionalOutlookModel.fit(in, in.size());
        assertThat(fit.fallback()).isTrue();
        double mean = java.util.Arrays.stream(y).average().orElse(0);
        assertThat(fit.fallbackMean()).isCloseTo(mean, within(1e-9));
        double[][] q = RegionalOutlookModel.windows(fit, in, in.size(), 1, 2, 3, 60);
        assertThat(q[0][QuantileSet.P50]).isCloseTo(4 * mean, within(4 * mean));
    }

    @Test
    @DisplayName("empirical Bayes pooling moves each intercept toward its class mean, never past it")
    void pooling() {
        Map<String, RegionalOutlookModel.Fit> fits = new LinkedHashMap<>();
        Map<String, String> classes = new LinkedHashMap<>();
        double[][] cov = new double[7][7];
        cov[0][0] = 0.25;
        for (int i = 0; i < 4; i++) {
            double[] beta = new double[7];
            beta[0] = i;
            fits.put("R" + i, new RegionalOutlookModel.Fit(beta, cov, 5, false, Double.NaN, false, false, 100, 0.25));
            classes.put("R" + i, "continent:AF");
        }
        Map<String, RegionalOutlookModel.Fit> pooled = RegionalOutlookModel.shrink(fits, classes);
        double m = 1.5;
        for (int i = 0; i < 4; i++) {
            double before = fits.get("R" + i).beta()[0];
            double after = pooled.get("R" + i).beta()[0];
            assertThat(Math.abs(after - m)).isLessThanOrEqualTo(Math.abs(before - m) + 1e-12);
            assertThat((after - m) * (before - m)).isGreaterThanOrEqualTo(0);
        }
        // A class of one is left alone.
        Map<String, RegionalOutlookModel.Fit> single = RegionalOutlookModel.shrink(Map.of("R0", fits.get("R0")),
                Map.of("R0", "continent:EU"));
        assertThat(single.get("R0").beta()[0]).isEqualTo(0.0);
    }

    @Test
    @DisplayName("the rolling-origin evaluation scores the model against seasonal naive and climatology")
    void evaluation() {
        long[] y = weekly(220, 12, 4);
        RegionalOutlookModel.Evaluation e = RegionalOutlookModel.evaluate(new RegionalOutlookModel.Input(y, FIRST,
                null, null), 7, 20);
        assertThat(e.n()).isPositive();
        assertThat(e.pinballModel()).isPositive();
        assertThat(e.view()).containsKeys("skillVsSeasonalNaive", "skillVsClimatology", "intervalCoverageP10P90");
        double[] yd = new double[200];
        java.util.Arrays.fill(yd, 3);
        assertThat(RegionalOutlookModel.seasonalNaive(yd, 150)).isNotNull();
        assertThat(RegionalOutlookModel.seasonalNaive(yd, 150)[QuantileSet.P50]).isEqualTo(12.0);
        assertThat(RegionalOutlookModel.climatology(yd, 150)[QuantileSet.P50]).isEqualTo(12.0);
        assertThat(RegionalOutlookModel.seasonalNaive(yd, 20)).isNull();
    }

    @Test
    @DisplayName("eligibility: countries need 52 weeks and an event in two years; admin-1 needs 52 weeks of 5+")
    void eligibility() {
        long[] busy = new long[60];
        java.util.Arrays.fill(busy, 6);
        long[] quiet = new long[300];
        assertThat(RegionalOutlookService.eligible(new ReferenceCounts.Weekly("f", "YE", "ucdp", FIRST, busy)))
                .isTrue();
        assertThat(RegionalOutlookService.eligible(new ReferenceCounts.Weekly("f", "YE", "ucdp", FIRST, quiet)))
                .isFalse();
        assertThat(RegionalOutlookService.eligible(new ReferenceCounts.Weekly("f", "YE-SA", "ucdp", FIRST, busy)))
                .isTrue();
        long[] thin = new long[200];
        java.util.Arrays.fill(thin, 4);
        assertThat(RegionalOutlookService.eligible(new ReferenceCounts.Weekly("f", "YE-SA", "ucdp", FIRST, thin)))
                .isFalse();
        assertThat(RegionalOutlookService.eligible(new ReferenceCounts.Weekly("f", "ZZ", "ucdp", FIRST, busy)))
                .isFalse();
    }

    @Test
    @DisplayName("a stored fit round-trips through its map")
    void fitMap() {
        long[] y = weekly(120, 8, 5);
        RegionalOutlookModel.Fit fit = RegionalOutlookModel.fit(new RegionalOutlookModel.Input(y, FIRST, null, null),
                y.length);
        RegionalOutlookModel.Fit back = RegionalOutlookService.fromMap(RegionalOutlookService.fitMap(fit));
        assertThat(back.beta()).hasSize(fit.beta().length);
        assertThat(back.theta()).isCloseTo(fit.theta(), within(1e-5));
        assertThat(back.covariance()[0][0]).isCloseTo(fit.covariance()[0][0], within(1e-12));
    }
}
