package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.intel.EventScorer.MemberView;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The confidence scorer (prompt 101). */
class EventScorerTest {

    private static MemberView member(String family, String source, double weight, boolean healthy) {
        return new MemberView(family, weight, source, healthy, null);
    }

    @Test
    @DisplayName("one family is a low score; two families a moderate one; three from two sources is strong and high")
    void families() {
        EventScorer.Score one = EventScorer.score(List.of(member("ais-behaviour", "a", 0.6, true)), 0);
        assertThat(one.confidence()).isCloseTo(0.36, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(one.label()).isEqualTo("low");
        assertThat(one.evidenceQuality()).isEqualTo("weak");

        EventScorer.Score two = EventScorer.score(List.of(member("ais-behaviour", "a", 0.6, true),
                member("nav-warning", "b", 0.6, true)), 0);
        assertThat(two.confidence()).isCloseTo(0.5904, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(two.label()).isEqualTo("moderate");
        assertThat(two.evidenceQuality()).isEqualTo("moderate");

        EventScorer.Score strong = EventScorer.score(List.of(member("ais-behaviour", "a", 0.5, true),
                member("nav-warning", "b", 0.5, true), member("gnss-interference", "b", 0.5, true)), 0);
        assertThat(strong.evidenceQuality()).isEqualTo("strong");
        assertThat(strong.label()).isEqualTo("high");
    }

    @Test
    @DisplayName("the best member per family is used, and a contradiction multiplies by 0.75")
    void bestPerFamilyAndContradiction() {
        EventScorer.Score base = EventScorer.score(List.of(member("ais-behaviour", "a", 0.6, true),
                member("ais-behaviour", "a2", 0.3, true)), 0);
        assertThat(base.confidence()).isCloseTo(0.36, org.assertj.core.data.Offset.offset(1e-9));
        EventScorer.Score contradicted = EventScorer.score(List.of(member("ais-behaviour", "a", 0.6, true)), 1);
        assertThat(contradicted.confidence()).isCloseTo(0.36 * 0.75, org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    @DisplayName("the score is capped, and members only from unhealthy sources are insufficient")
    void capAndInsufficient() {
        EventScorer.Score capped = EventScorer.score(List.of(member("a", "s1", 1, true), member("b", "s2", 1, true),
                member("c", "s3", 1, true), member("d", "s4", 1, true)), 0);
        assertThat(capped.confidence()).isLessThanOrEqualTo(EventScorer.CAP);
        EventScorer.Score unhealthy = EventScorer.score(List.of(member("ais-behaviour", "a", 0.6, false)), 0);
        assertThat(unhealthy.evidenceQuality()).isEqualTo("insufficient");
    }

    @Test
    @DisplayName("labels follow the thresholds")
    void labels() {
        assertThat(EventScorer.label(0.39)).isEqualTo("low");
        assertThat(EventScorer.label(0.4)).isEqualTo("moderate");
        assertThat(EventScorer.label(0.69)).isEqualTo("moderate");
        assertThat(EventScorer.label(0.7)).isEqualTo("high");
    }
}
