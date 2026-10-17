package ai.foresignal.cycle;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Cadence defaults, priorities and effort caps (prompt 128). */
class CycleVocabularyTest {

    @Test
    @DisplayName("cadence defaults: questions, tokens, lookback, horizon, effort and priority")
    void defaults() {
        assertThat(CycleVocabulary.defaultMaxQuestions("daily")).isEqualTo(15);
        assertThat(CycleVocabulary.defaultMaxQuestions("weekly")).isEqualTo(40);
        assertThat(CycleVocabulary.defaultMaxQuestions("monthly")).isEqualTo(60);
        assertThat(CycleVocabulary.defaultMaxTokens("daily")).isEqualTo(300_000L);
        assertThat(CycleVocabulary.defaultMaxTokens("monthly")).isEqualTo(2_000_000L);
        assertThat(CycleVocabulary.lookback("daily")).isEqualTo(Duration.ofDays(1));
        assertThat(CycleVocabulary.lookback("monthly")).isEqualTo(Duration.ofDays(30));
        assertThat(CycleVocabulary.horizon("daily")).isEqualTo(Duration.ofDays(7));
        assertThat(CycleVocabulary.horizon("weekly")).isEqualTo(Duration.ofDays(30));
        assertThat(CycleVocabulary.horizon("monthly")).isEqualTo(Duration.ofDays(90));
        assertThat(CycleVocabulary.defaultEffort("daily")).isEqualTo("medium");
        assertThat(CycleVocabulary.defaultEffort("weekly")).isEqualTo("high");
        assertThat(CycleVocabulary.priority("daily")).isEqualTo("normal");
        assertThat(CycleVocabulary.priority("monthly")).isEqualTo("high");
    }

    @Test
    @DisplayName("priority thresholds and effort caps")
    void thresholds() {
        assertThat(CycleVocabulary.belowPriority("normal", "high")).isTrue();
        assertThat(CycleVocabulary.belowPriority("high", "normal")).isFalse();
        assertThat(CycleVocabulary.belowPriority(null, null)).isFalse();
        assertThat(CycleVocabulary.effectiveEffort("high", "low")).isEqualTo("low");
        assertThat(CycleVocabulary.effectiveEffort("medium", "max")).isEqualTo("medium");
        assertThat(CycleVocabulary.effectiveEffort("high", null)).isEqualTo("high");
    }
}
