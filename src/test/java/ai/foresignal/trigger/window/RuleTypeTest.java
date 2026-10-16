package ai.foresignal.trigger.window;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Which rule types the per-record engine (017) and its persist gate may compile (prompt 100). */
class RuleTypeTest {

    @Test
    @DisplayName("only record (or a blank type) belongs to the per-record engine; windowed and unknown types never do")
    void recordEngine() {
        assertThat(RuleType.recordEngine(null)).isTrue();
        assertThat(RuleType.recordEngine("")).isTrue();
        assertThat(RuleType.recordEngine("record")).isTrue();
        assertThat(RuleType.recordEngine("RECORD")).isTrue();
        assertThat(RuleType.recordEngine("count")).isFalse();
        assertThat(RuleType.recordEngine("absence")).isFalse();
        assertThat(RuleType.recordEngine("anomaly")).isFalse();
        assertThat(RuleType.recordEngine("mystery")).isFalse();
    }
}
