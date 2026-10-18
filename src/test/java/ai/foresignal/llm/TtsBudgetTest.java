package ai.foresignal.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.test.MutableClock;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link TtsBudget} (prompt 141): characters per provider per UTC day. */
class TtsBudgetTest {

    final MutableClock clock = new MutableClock(Instant.parse("2026-10-18T22:00:00Z"));
    final TtsBudget budget = new TtsBudget(clock);

    @Test
    @DisplayName("reservations count against the day; one over the allowance is refused with Retry-After to midnight")
    void daily() {
        budget.reserve("speech", 10_000, 6_000);
        budget.reserve("speech", 10_000, 4_000);
        assertThat(budget.usedToday("speech")).isEqualTo(10_000);

        assertThatThrownBy(() -> budget.reserve("speech", 10_000, 1))
                .isInstanceOfSatisfying(LlmBudgetExceededException.class,
                        e -> assertThat(e.getMessage()).contains("tts characters per day"));
        assertThat(budget.usedToday("speech")).as("a refusal reserves nothing").isEqualTo(10_000);
        assertThat(budget.usedToday("other")).isZero();
    }

    @Test
    @DisplayName("settle gives characters back; the count restarts at 00:00 UTC; reset clears it")
    void settleAndRollover() {
        budget.reserve("speech", 10_000, 8_000);
        budget.settle("speech", 3_000);
        assertThat(budget.usedToday("speech")).isEqualTo(5_000);
        budget.settle("speech", 99_000);
        assertThat(budget.usedToday("speech")).isZero();

        budget.reserve("speech", 10_000, 9_000);
        clock.advance(Duration.ofHours(2));
        assertThat(budget.usedToday("speech")).isZero();
        budget.reserve("speech", 10_000, 10_000);
        budget.settle("unknown", 5);

        budget.resetState();
        assertThat(budget.usedToday("speech")).isZero();
    }
}
