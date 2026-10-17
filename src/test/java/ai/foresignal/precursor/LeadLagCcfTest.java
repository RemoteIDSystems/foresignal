package ai.foresignal.precursor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The series lead–lag test {@code leadlag-ccf} 1.0.0 (prompt 120): a planted lagged dependence is found at the right lag
 * and sign; independent AR(1) series are not significant (fixed seed); the reverse direction flags co-movement; the
 * block length rule; insufficient overlap; determinism (INT-1).
 */
class LeadLagCcfTest {

    static double[] noise(int n, long seed) {
        Random r = new Random(seed);
        double[] out = new double[n];
        for (int i = 0; i < n; i++) {
            out[i] = r.nextGaussian();
        }
        return out;
    }

    static double[] ar1(int n, double phi, long seed) {
        double[] e = noise(n, seed);
        double[] out = new double[n];
        for (int i = 0; i < n; i++) {
            out[i] = (i == 0 ? 0 : phi * out[i - 1]) + e[i];
        }
        return out;
    }

    static double[] lagged(double[] lead, int lag, double weight, long seed) {
        double[] e = noise(lead.length, seed);
        double[] out = new double[lead.length];
        for (int i = 0; i < lead.length; i++) {
            out[i] = (i >= lag ? weight * lead[i - lag] : 0) + 0.6 * e[i];
        }
        return out;
    }

    @Test
    @DisplayName("a planted positive dependence at lag 3 is found at lag 3, positive, significant, not co-movement")
    void plantedPositive() {
        double[] a = noise(300, 1);
        double[] b = lagged(a, 3, 0.8, 2);

        LeadLagCcf.Result r = LeadLagCcf.test(a, b, 14, 42);

        assertThat(r.insufficient()).isFalse();
        assertThat(r.forward().bestLag()).isEqualTo(3);
        assertThat(r.sign()).isEqualTo("positive");
        assertThat(r.forward().correlation()).isGreaterThan(0.6);
        assertThat(r.forward().pValue()).isLessThanOrEqualTo(0.01);
        assertThat(r.coMovement()).isFalse();
        assertThat(r.overlap()).isEqualTo(300);
    }

    @Test
    @DisplayName("a planted negative dependence at lag 2 reports the negative sign")
    void plantedNegative() {
        double[] a = noise(300, 3);
        double[] b = lagged(a, 2, -0.8, 4);

        LeadLagCcf.Result r = LeadLagCcf.test(a, b, 14, 7);

        assertThat(r.forward().bestLag()).isEqualTo(2);
        assertThat(r.sign()).isEqualTo("negative");
        assertThat(r.forward().pValue()).isLessThanOrEqualTo(0.01);
    }

    @Test
    @DisplayName("independent AR(1) series are not significant (fixed seed)")
    void independent() {
        double[] a = ar1(300, 0.5, 11);
        double[] b = ar1(300, 0.5, 12);

        LeadLagCcf.Result r = LeadLagCcf.test(a, b, 14, 99);

        assertThat(r.forward().pValue()).isGreaterThan(0.01);
    }

    @Test
    @DisplayName("when the consequent leads more strongly, the result is flagged co-movement, not precedence")
    void reverseFlag() {
        int n = 400;
        double[] ea = noise(n, 21);
        double[] eb = noise(n, 22);
        double[] a = new double[n];
        double[] b = new double[n];
        for (int t = 0; t < n; t++) {
            a[t] = (t >= 2 ? 0.7 * b[t - 2] : 0) + ea[t];
            b[t] = (t >= 2 ? 0.4 * a[t - 2] : 0) + eb[t];
        }

        LeadLagCcf.Result r = LeadLagCcf.test(a, b, 14, 5);

        assertThat(r.reverse().strong()).isTrue();
        assertThat(r.reverse().statistic()).isGreaterThanOrEqualTo(r.forward().statistic());
        assertThat(r.coMovement()).isTrue();
    }

    @Test
    @DisplayName("block length: at least 7, longer for a persistent series, at most a quarter of it")
    void blockLength() {
        assertThat(LeadLagCcf.blockLength(noise(500, 31))).isEqualTo(LeadLagCcf.MIN_BLOCK);
        int persistent = LeadLagCcf.blockLength(ar1(1000, 0.95, 32));
        assertThat(persistent).isBetween(15, 250);
        assertThat(LeadLagCcf.blockLength(ar1(40, 0.99, 33))).isBetween(LeadLagCcf.MIN_BLOCK, 10);
    }

    @Test
    @DisplayName("too little overlap is insufficient; gaps are skipped pairwise")
    void insufficient() {
        LeadLagCcf.Result r = LeadLagCcf.test(noise(20, 1), noise(20, 2), 5, 1);
        assertThat(r.insufficient()).isTrue();
        assertThat(r.forward().pValue()).isEqualTo(1.0);
        assertThat(r.sign()).isNull();

        double[] a = noise(200, 5);
        double[] b = lagged(a, 1, 0.9, 6);
        for (int i = 0; i < 200; i += 3) {
            a[i] = Double.NaN;
        }
        LeadLagCcf.Result gappy = LeadLagCcf.test(a, b, 7, 3);
        assertThat(gappy.overlap()).isLessThan(200);
        assertThat(gappy.forward().bestLag()).isEqualTo(1);
    }

    @Test
    @DisplayName("identical input and seed give identical output")
    void deterministic() {
        double[] a = noise(250, 8);
        double[] b = lagged(a, 4, 0.3, 9);
        assertThat(LeadLagCcf.test(a, b, 14, 77)).isEqualTo(LeadLagCcf.test(a, b, 14, 77));
        assertThat(LeadLagCcf.correlation(a, a, 0)).isEqualTo(1.0, org.assertj.core.api.Assertions.within(1e-12));
    }
}
