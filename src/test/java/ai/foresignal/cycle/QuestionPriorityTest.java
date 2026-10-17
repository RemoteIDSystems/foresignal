package ai.foresignal.cycle;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Question choice by value of information (prompt 128 §3): stakes × uncertainty, then due date, then recency. */
class QuestionPriorityTest {

    static final Instant T = Instant.parse("2026-10-17T00:00:00Z");

    @Test
    @DisplayName("value is stakes times 4p(1-p), 1 without an ensemble")
    void value() {
        assertThat(new QuestionPriority.Candidate("q", "critical", 0.5, T, null).value()).isEqualTo(3.0);
        assertThat(new QuestionPriority.Candidate("q", "warning", null, T, null).value()).isEqualTo(2.0);
        assertThat(new QuestionPriority.Candidate("q", null, 0.9, T, null).value()).isCloseTo(0.36,
                org.assertj.core.api.Assertions.within(1e-9));
    }

    @Test
    @DisplayName("ties go to the earlier due date, then to the least recently forecast")
    void order() {
        List<QuestionPriority.Candidate> chosen = QuestionPriority.choose(List.of(
                new QuestionPriority.Candidate("late", "info", 0.5, T.plusSeconds(100), null),
                new QuestionPriority.Candidate("recent", "info", 0.5, T, T.plusSeconds(50)),
                new QuestionPriority.Candidate("never", "info", 0.5, T, null),
                new QuestionPriority.Candidate("critical", "critical", 0.95, T.plusSeconds(999), null),
                new QuestionPriority.Candidate("certain", "info", 0.99, T, null)), 4);
        assertThat(chosen).extracting(QuestionPriority.Candidate::questionId)
                .containsExactly("never", "recent", "late", "critical");
        assertThat(QuestionPriority.choose(List.of(), 3)).isEmpty();
        assertThat(QuestionPriority.choose(chosen, 0)).isEmpty();
    }
}
