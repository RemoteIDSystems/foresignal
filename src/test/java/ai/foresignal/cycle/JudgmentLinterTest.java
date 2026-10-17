package ai.foresignal.cycle;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The wording rules for generated judgments (prompt 128): lint, one regeneration, deterministic downgrade, drop. */
class JudgmentLinterTest {

    @Test
    @DisplayName("clean text passes untouched without a regeneration")
    void clean() {
        AtomicInteger calls = new AtomicInteger();
        JudgmentLinter.Result r = JudgmentLinter.check("A response within 72 hours is likely.", 0.62, m -> {
            calls.incrementAndGet();
            return null;
        });
        assertThat(r.text()).isEqualTo("A response within 72 hours is likely.");
        assertThat(r.regenerated()).isFalse();
        assertThat(r.downgraded()).isFalse();
        assertThat(calls).hasValue(0);
    }

    @Test
    @DisplayName("failing text is regenerated once and the clean regeneration is kept")
    void regenerated() {
        AtomicInteger calls = new AtomicInteger();
        JudgmentLinter.Result r = JudgmentLinter.check("A response is very likely with high confidence.", 0.62,
                messages -> {
                    calls.incrementAndGet();
                    assertThat(messages).isNotEmpty();
                    return "A response is likely.";
                });
        assertThat(calls).hasValue(1);
        assertThat(r.regenerated()).isTrue();
        assertThat(r.text()).isEqualTo("A response is likely.");
        assertThat(r.warnings()).anyMatch(w -> w.contains("regenerated once"));
    }

    @Test
    @DisplayName("text still failing after the regeneration is downgraded: terms follow the number, confidence goes")
    void downgraded() {
        JudgmentLinter.Result r = JudgmentLinter.check("A response is very likely with high confidence.", 0.62,
                messages -> "A response is almost certain with moderate confidence.");
        assertThat(r.downgraded()).isTrue();
        assertThat(r.text()).isEqualTo("A response is likely.");
        assertThat(JudgmentLinter.violations(r.text(), 0.62)).isEmpty();
        JudgmentLinter.Result mixed = JudgmentLinter.check("Strikes are likely; talks are improbable.", null, null);
        assertThat(mixed.downgraded()).isTrue();
        assertThat(mixed.text()).isEqualTo("Strikes are likely; talks are unlikely.");
        JudgmentLinter.Result percent = JudgmentLinter.check("A strike is unlikely (70%).", null, null);
        assertThat(percent.text()).isEqualTo("A strike is likely (70%).");
    }

    @Test
    @DisplayName("empty text is dropped; a failing regeneration falls back to the original")
    void dropped() {
        assertThat(JudgmentLinter.check(" ", 0.5, null).dropped()).isTrue();
        JudgmentLinter.Result r = JudgmentLinter.check("A response is very likely.", 0.62, m -> {
            throw new IllegalStateException("boom");
        });
        assertThat(r.downgraded()).isTrue();
        assertThat(r.warnings()).anyMatch(w -> w.contains("regeneration failed"));
        assertThat(JudgmentLinter.downgrade("We assess with high confidence that ports stay shut.", null))
                .isEqualTo("We assess that ports stay shut.");
        assertThat(JudgmentLinter.violations(null, null)).isEqualTo(List.of());
    }
}
