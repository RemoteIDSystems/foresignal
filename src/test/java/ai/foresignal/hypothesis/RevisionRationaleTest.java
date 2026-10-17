package ai.foresignal.hypothesis;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.forecast.language.EstimativeLanguage;
import ai.foresignal.hypothesis.RevisionRationale.DriverNote;
import ai.foresignal.hypothesis.RevisionRationale.Moved;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The deterministic revision rationale (prompt 125 §4.8): it passes {@code EstimativeLanguage.lint}, a likelihood term
 * and a confidence level never share a sentence, percentages always read in their term's band, and operator text that
 * would break the lint falls back to neutral references.
 */
class RevisionRationaleTest {

    static final List<Moved> MOVES = List.of(
            new Moved("response within 72 h", 0.31, 0.49, "low", "moderate", false),
            new Moved("none in 7 days", 0.69, 0.51, "low", "low", false));

    static final List<DriverNote> DRIVERS = List.of(
            new DriverNote("NOTAMs closing southern Saudi airspace", "observed", 5.0, null, false),
            new DriverNote("naval co-occurrence", "absent", 0.7, null, false));

    @Test
    @DisplayName("the rationale states each move with its terms and numbers, the drivers, and passes the lint")
    void passesLint() {
        String text = RevisionRationale.build(MOVES, DRIVERS);
        assertThat(EstimativeLanguage.lint(text, List.of())).isEmpty();
        assertThat(text).contains("'response within 72 h' was unlikely (31%).")
                .contains("It is now roughly even chance (49%).")
                .contains("NOTAMs closing southern Saudi airspace observed (high diagnosticity)")
                .contains("naval co-occurrence absent with coverage (low diagnosticity)");
        assertThat(RevisionRationale.build(MOVES, DRIVERS)).as("deterministic").isEqualTo(text);
    }

    @Test
    @DisplayName("a likelihood term and a confidence level never share a sentence")
    void termAndConfidenceApart() {
        String text = RevisionRationale.build(MOVES, DRIVERS);
        assertThat(text).contains("Analytic confidence for 'response within 72 h' changed from low to moderate.");
        for (String sentence : text.split("(?<=[.!?])\\s+")) {
            String lower = sentence.toLowerCase(Locale.ROOT);
            boolean term = EstimativeLanguage.TERMS.stream().anyMatch(lower::contains);
            boolean confidence = lower.contains("confidence");
            assertThat(term && confidence).as(sentence).isFalse();
        }
    }

    @Test
    @DisplayName("percentages read in the band of their term")
    void percentages() {
        assertThat(RevisionRationale.percent(0.31)).isEqualTo("31%");
        assertThat(RevisionRationale.percent(0.449)).isEqualTo("44.9%");
        assertThat(RevisionRationale.percent(0.5501)).isEqualTo("55.01%");
        assertThat(RevisionRationale.percent(0.95)).isEqualTo("95%");
        for (double p = 0.001; p < 1; p += 0.0137) {
            String text = "It is now " + EstimativeLanguage.term(p) + " (" + RevisionRationale.percent(p) + ").";
            assertThat(EstimativeLanguage.clean(text, List.of())).as(text).isTrue();
        }
    }

    @Test
    @DisplayName("labels that would break the lint fall back to neutral references")
    void neutralFallback() {
        List<Moved> moves = List.of(new Moved("likely strike", 0.2, 0.3, null, null, false));
        String text = RevisionRationale.build(moves, List.of(new DriverNote("very likely closure", "observed", 2.5,
                null, false)));
        assertThat(EstimativeLanguage.lint(text, List.of())).isEmpty();
        assertThat(text).contains("Hypothesis 1 was unlikely (20%).").doesNotContain("likely strike");
    }

    @Test
    @DisplayName("suppressed and damped drivers, caps, and a revision that moves nothing are stated")
    void notes() {
        String text = RevisionRationale.build(List.of(new Moved("link", 0.1, 0.1004, null, null, true)),
                List.of(new DriverNote("echo", "observed", 2.5, HypothesisUpdater.SAME_GROUP, false),
                        new DriverNote("price spike", "observed", 5.0, null, true)));
        assertThat(text).startsWith("No hypothesis moved by half a percentage point or more.")
                .contains("echo observed (medium diagnosticity), not counted again (same-evidence-group)")
                .contains("Daily damping limited the change.")
                .contains("'link' is held at its speculative-link cap");
        assertThat(EstimativeLanguage.lint(text, List.of())).isEmpty();
    }
}
