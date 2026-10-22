package ai.foresignal.social.narrative;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The deterministic, LLM-free framing score (prompt 153): neutral prose scores 0, hype scores high, and it is pure. */
class FramingScoreTest {

    @Test
    @DisplayName("plainly stated prose is neutral with score 0")
    void neutralProse() {
        FramingScore.Framing f = FramingScore.score("A blast was reported near the port at dawn.");
        assertThat(f.score()).isZero();
        assertThat(f.label()).isEqualTo("neutral");
    }

    @Test
    @DisplayName("outrage and partisan hype is inflammatory")
    void inflammatory() {
        FramingScore.Framing f = FramingScore.score("OUTRAGE! Shocking disaster, absolute carnage!!");
        assertThat(f.score()).isGreaterThanOrEqualTo(FramingScore.INFLAMMATORY);
        assertThat(f.label()).isEqualTo("inflammatory");
        assertThat(f.markers()).containsEntry("inflammatory", 4);
    }

    @Test
    @DisplayName("a lone intensifier amid plain prose is charged, not inflammatory")
    void charged() {
        FramingScore.Framing f = FramingScore.score("The incredible report said a vehicle stopped on the main road.");
        assertThat(f.label()).isEqualTo("charged");
        assertThat(f.score()).isGreaterThanOrEqualTo(FramingScore.CHARGED).isLessThan(FramingScore.INFLAMMATORY);
    }

    @Test
    @DisplayName("the same text always scores the same")
    void deterministic() {
        String text = "SCANDAL! The corrupt regime slammed critics in an unbelievable meltdown.";
        assertThat(FramingScore.score(text)).isEqualTo(FramingScore.score(text));
    }

    @Test
    @DisplayName("empty or null text is neutral")
    void empty() {
        assertThat(FramingScore.score(null).label()).isEqualTo("neutral");
        assertThat(FramingScore.score("   ").score()).isZero();
    }
}
