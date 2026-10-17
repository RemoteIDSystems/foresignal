package ai.foresignal.forecast.language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * {@link EstimativeLanguage} (prompt 122, LED-3): every band boundary with its inclusive/exclusive rule, the mirror
 * symmetry of the scale, band changes, and the three lint rules with offsets.
 */
class EstimativeLanguageTest {

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({
        "0.0, almost no chance", "0.01, almost no chance", "0.0499, almost no chance",
        "0.05, very unlikely", "0.1999, very unlikely",
        "0.20, unlikely", "0.4499, unlikely",
        "0.45, roughly even chance", "0.5, roughly even chance", "0.55, roughly even chance",
        "0.5501, likely", "0.80, likely",
        "0.8001, very likely", "0.95, very likely",
        "0.9501, almost certain", "0.99, almost certain", "1.0, almost certain"
    })
    @DisplayName("every band boundary: below one half closed below and open above, above one half the reverse")
    void bands(double p, String term) {
        assertThat(EstimativeLanguage.term(p)).isEqualTo(term);
    }

    @Test
    @DisplayName("the scale is symmetric: term(p) and term(1 − p) are mirror terms")
    void symmetric() {
        for (int i = 0; i <= 1000; i++) {
            double p = i / 1000.0;
            int a = EstimativeLanguage.band(p).index();
            int b = EstimativeLanguage.band(1 - p).index();
            assertThat(a + b).as("p = " + p).isEqualTo(6);
        }
    }

    @Test
    @DisplayName("band changes, and non-probabilities refused")
    void crossesBand() {
        assertThat(EstimativeLanguage.crossesBand(0.54, 0.56)).isTrue();
        assertThat(EstimativeLanguage.crossesBand(0.56, 0.79)).isFalse();
        assertThat(EstimativeLanguage.crossesBand(0.199, 0.2)).isTrue();
        assertThat(EstimativeLanguage.termOrNull(null)).isNull();
        assertThatThrownBy(() -> EstimativeLanguage.term(1.2)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EstimativeLanguage.term(Double.NaN)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("a term that disagrees with its claim's number is reported with its offsets")
    void termMismatchWithClaim() {
        String text = "A strike is likely within the week.";
        List<EstimativeLanguage.Violation> v = EstimativeLanguage.lint(text, List.of(
                new EstimativeLanguage.Claim(0.30, 0, text.length())));
        assertThat(v).hasSize(1);
        assertThat(v.get(0).code()).isEqualTo(EstimativeLanguage.TERM_MISMATCH);
        assertThat(text.substring(v.get(0).start(), v.get(0).end())).isEqualTo("likely");
        assertThat(EstimativeLanguage.lint(text, List.of(new EstimativeLanguage.Claim(0.7, 0, text.length()))))
                .isEmpty();
    }

    @Test
    @DisplayName("a percentage in the same sentence is the term's number")
    void termMismatchWithPercentage() {
        assertThat(EstimativeLanguage.lint("Escalation is very likely (60%).", List.of()))
                .extracting(EstimativeLanguage.Violation::code).containsExactly(EstimativeLanguage.TERM_MISMATCH);
        assertThat(EstimativeLanguage.lint("Escalation is very likely (85%).", List.of())).isEmpty();
        // A number in another sentence does not count.
        assertThat(EstimativeLanguage.lint("Escalation is very likely. Turnout was 60%.", List.of())).isEmpty();
    }

    @Test
    @DisplayName("longest terms win: 'very unlikely' is never also 'unlikely' or 'likely'")
    void longestFirst() {
        String text = "It is very unlikely (10%).";
        assertThat(EstimativeLanguage.lint(text, List.of())).isEmpty();
        assertThat(EstimativeLanguage.lint("It is highly improbable.", List.of())).isEmpty();
    }

    @Test
    @DisplayName("second-row terms mixed with first-row terms are reported at each second-row term")
    void mixedRows() {
        String text = "A ceasefire is unlikely. A new offensive is probable.";
        List<EstimativeLanguage.Violation> v = EstimativeLanguage.lint(text, List.of());
        assertThat(v).extracting(EstimativeLanguage.Violation::code).containsExactly(EstimativeLanguage.MIXED_ROWS);
        assertThat(v.get(0).term()).isEqualTo("probable");
        assertThat(v.get(0).message()).contains("likely");
        // The second row alone is not a mix.
        assertThat(EstimativeLanguage.lint("A new offensive is probable.", List.of())).isEmpty();
    }

    @Test
    @DisplayName("a likelihood term and a confidence level in one sentence are refused; in separate sentences allowed")
    void termWithConfidence() {
        assertThat(EstimativeLanguage.lint("We assess with high confidence that a strike is likely.", List.of()))
                .extracting(EstimativeLanguage.Violation::code)
                .containsExactly(EstimativeLanguage.TERM_WITH_CONFIDENCE);
        assertThat(EstimativeLanguage.lint("A strike is likely. We have moderate confidence in the sourcing.",
                List.of())).isEmpty();
        assertThat(EstimativeLanguage.lint("A strike is likely\nLow-confidence sourcing.", List.of())).isEmpty();
    }

    @Test
    @DisplayName("clean text, empty text and null are clean")
    void clean() {
        assertThat(EstimativeLanguage.clean("Shipping volumes fell 12% week on week.", List.of())).isTrue();
        assertThat(EstimativeLanguage.clean("", List.of())).isTrue();
        assertThat(EstimativeLanguage.clean(null, null)).isTrue();
    }
}
