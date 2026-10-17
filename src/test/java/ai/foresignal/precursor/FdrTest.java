package ai.foresignal.precursor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * False-discovery control of a precursor batch (prompt 120, FDR-1): Benjamini–Hochberg and Benjamini–Yekutieli adjust
 * known p-vectors to their reference values (R {@code p.adjust(p, "BH")} and {@code p.adjust(p, "BY")}), survivors
 * are those at or below q, and the method follows the overlap flag.
 */
class FdrTest {

    @Test
    @DisplayName("BH adjusts a known p-vector to the reference values, in input order")
    void benjaminiHochberg() {
        Fdr.Adjusted a = Fdr.adjust(new double[] {0.01, 0.04, 0.03, 0.005}, false, 0.05);

        assertThat(a.method()).isEqualTo(Fdr.BH);
        assertThat(a.adjusted()).containsExactly(new double[] {0.02, 0.04, 0.04, 0.02}, within(1e-12));
        assertThat(a.rejected()).containsExactly(true, true, true, true);
        assertThat(a.survivors()).isEqualTo(4);
        assertThat(a.raw()).containsExactly(0.01, 0.04, 0.03, 0.005);
    }

    @Test
    @DisplayName("BY multiplies the BH values by the harmonic sum and rejects fewer")
    void benjaminiYekutieli() {
        Fdr.Adjusted a = Fdr.adjust(new double[] {0.01, 0.04, 0.03, 0.005}, true, 0.05);
        double c = 1 + 1 / 2.0 + 1 / 3.0 + 1 / 4.0;

        assertThat(a.method()).isEqualTo(Fdr.BY);
        assertThat(a.adjusted()).containsExactly(new double[] {0.02 * c, 0.04 * c, 0.04 * c, 0.02 * c}, within(1e-12));
        assertThat(a.rejected()).containsExactly(true, false, false, true);
    }

    @Test
    @DisplayName("the R reference vector 0.01..0.05: BH all 0.05, BY all 0.05 × 2.2833")
    void referenceVector() {
        double[] p = {0.01, 0.02, 0.03, 0.04, 0.05};
        assertThat(Fdr.adjust(p, false, Fdr.DEFAULT_Q).adjusted()).containsOnly(new double[] {0.05}, within(1e-12));
        assertThat(Fdr.adjust(p, true, Fdr.DEFAULT_Q).adjusted()).containsOnly(new double[] {0.1141666666666667},
                within(1e-12));
        assertThat(Fdr.adjust(p, true, Fdr.DEFAULT_Q).survivors()).isZero();
        assertThat(Fdr.adjust(p, false, Fdr.DEFAULT_Q).survivors()).isEqualTo(5);
    }

    @Test
    @DisplayName("an empty batch adjusts to nothing; adjusted values never exceed 1")
    void edges() {
        assertThat(Fdr.adjust(new double[0], false, 0.1).adjusted()).isEmpty();
        assertThat(Fdr.adjust(null, false, 0.1).survivors()).isZero();
        assertThat(java.util.Arrays.stream(Fdr.adjust(new double[] {0.9, 0.95, 1.0}, true, 0.1).adjusted()).max()
                .orElse(0)).isLessThanOrEqualTo(1.0);
    }
}
