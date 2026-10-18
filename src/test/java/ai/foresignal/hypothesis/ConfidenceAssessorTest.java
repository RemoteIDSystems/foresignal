package ai.foresignal.hypothesis;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.hypothesis.ConfidenceAssessor.Assessment;
import ai.foresignal.hypothesis.ConfidenceAssessor.Inputs;
import ai.foresignal.hypothesis.ConfidenceAssessor.Observed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code ConfidenceAssessor} 1.2.0 (prompt 125 §4.7, prompts 133 and 134): each rule of high, moderate and low, and the one-step downgrade
 * of a broken linchpin.
 */
class ConfidenceAssessorTest {

    static Assessment assess(List<Observed> observed, boolean linchpin, boolean lowDensity) {
        return ConfidenceAssessor.assess(new Inputs(observed, linchpin, lowDensity, null, null, null));
    }

    static Observed obs(String family, String credibility, String grade) {
        return new Observed("i-" + family + credibility, family, credibility, grade);
    }

    @Test
    @DisplayName("low without a credible observed indicator")
    void low() {
        assertThat(assess(List.of(), false, false).level()).isEqualTo("low");
        assertThat(assess(List.of(obs("airspace", "L", "A")), false, false).level()).isEqualTo("low");
    }

    @Test
    @DisplayName("moderate with at least one observed indicator of credibility M or H")
    void moderate() {
        assertThat(assess(List.of(obs("airspace", "M", null)), false, false).level()).isEqualTo("moderate");
        assertThat(assess(List.of(obs("airspace", "H", "F")), false, false).level()).isEqualTo("moderate");
    }

    @Test
    @DisplayName("high needs two credible indicators from two families, sources A/B (or ungraded) and enough reporting")
    void high() {
        Assessment high = assess(List.of(obs("airspace", "H", "A"), obs("naval", "M", "B")), false, false);
        assertThat(high.level()).isEqualTo("high");
        assertThat(high.basis()).containsEntry("observedIndicators", 2).containsEntry("families", 2)
                .containsEntry("reportingDensity", "ok").containsEntry("brokenLinchpin", false)
                .containsEntry("sourceReliability", "B");
        assertThat(assess(List.of(obs("airspace", "H", null), obs("naval", "H", null)), false, false).level())
                .as("ungraded sources count as default").isEqualTo("high");
        assertThat(assess(List.of(obs("airspace", "H", "A"), obs("airspace", "H", "A")), false, false).level())
                .as("one family").isEqualTo("moderate");
        assertThat(assess(List.of(obs("airspace", "H", "A"), obs("naval", "H", "C")), false, false).level())
                .as("a source below B").isEqualTo("moderate");
        assertThat(assess(List.of(obs("airspace", "H", "A"), obs("naval", "L", "A")), false, false).level())
                .as("one credible").isEqualTo("moderate");
        Assessment thin = assess(List.of(obs("airspace", "H", "A"), obs("naval", "H", "A")), false, true);
        assertThat(thin.level()).as("low reporting density").isEqualTo("moderate");
        assertThat(thin.basis()).containsEntry("reportingDensity", ConfidenceAssessor.LOW_REPORTING_DENSITY);
    }

    @Test
    @DisplayName("a broken linchpin lowers confidence one step")
    void linchpin() {
        Assessment fromHigh = assess(List.of(obs("airspace", "H", "A"), obs("naval", "H", "A")), true, false);
        assertThat(fromHigh.level()).isEqualTo("moderate");
        assertThat(fromHigh.basis()).containsEntry("brokenLinchpin", true);
        assertThat(String.valueOf(fromHigh.basis().get("rule"))).contains("broken linchpin");
        assertThat(assess(List.of(obs("airspace", "M", null)), true, false).level()).isEqualTo("low");
        assertThat(assess(List.of(), true, false).level()).isEqualTo("low");
        assertThat(HypothesisVocabulary.lower("high")).isEqualTo("moderate");
        assertThat(HypothesisVocabulary.lower("low")).isEqualTo("low");
    }

    @Test
    @DisplayName("133: skewed news support without non-news corroboration lowers confidence one step; nothing else does")
    void skewedNewsSupport() {
        List<Observed> two = List.of(obs("airspace", "H", "A"), obs("naval", "M", "B"));
        Assessment high = ConfidenceAssessor.assess(new Inputs(two, false, false, null, null, null, false));
        assertThat(high.level()).isEqualTo("high");
        assertThat(high.basis()).containsEntry("newsCoverage", "ok");
        Assessment lowered = ConfidenceAssessor.assess(new Inputs(two, false, false, null, null, null, true));
        assertThat(lowered.level()).isEqualTo("moderate");
        assertThat(lowered.basis()).containsEntry("newsCoverage", ConfidenceAssessor.SKEWED_COVERAGE);
        assertThat(lowered.basis().get("rule").toString()).contains("skewed");
        Assessment both = ConfidenceAssessor.assess(new Inputs(two, true, false, null, null, null, true));
        assertThat(both.level()).as("linchpin and skew: two steps").isEqualTo("low");
        assertThat(ConfidenceAssessor.VERSION).isEqualTo("1.2.0");
    }
}
