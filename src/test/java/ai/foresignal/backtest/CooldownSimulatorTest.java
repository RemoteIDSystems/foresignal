package ai.foresignal.backtest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The cooldown simulator (prompt 103). */
class CooldownSimulatorTest {

    private static final Instant T = Instant.parse("2026-01-01T00:00:00Z");

    private static List<CooldownSimulator.Match> matches(String entity, int count, Duration step) {
        List<CooldownSimulator.Match> out = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            out.add(new CooldownSimulator.Match(i + 1, entity, T.plus(step.multipliedBy(i))));
        }
        return out;
    }

    @Test
    @DisplayName("cooldown on observed_at suppresses matches inside the pause")
    void cooldown() {
        var result = CooldownSimulator.run(matches("a", 4, Duration.ofMinutes(1)), Duration.ofMinutes(5), false, null,
                null);
        assertThat(result.fired()).hasSize(1);
        assertThat(result.cooldownSuppressed()).isEqualTo(3);
    }

    @Test
    @DisplayName("rule scope shares the cooldown across entities")
    void ruleScope() {
        List<CooldownSimulator.Match> m = new ArrayList<>(matches("a", 1, Duration.ofMinutes(1)));
        m.add(new CooldownSimulator.Match(2, "b", T.plus(Duration.ofMinutes(1))));
        var result = CooldownSimulator.run(m, Duration.ofMinutes(5), true, null, null);
        assertThat(result.fired()).hasSize(1);
        assertThat(result.cooldownSuppressed()).isEqualTo(1);
    }

    @Test
    @DisplayName("the hourly cap suppresses beyond the cap")
    void cap() {
        var result = CooldownSimulator.run(matches("a", 5, Duration.ofMinutes(1)), Duration.ZERO, false, 2, null);
        assertThat(result.fired()).hasSize(2);
        assertThat(result.capSuppressed()).isEqualTo(3);
    }

    @Test
    @DisplayName("the active window suppresses outside it")
    void window() {
        var result = CooldownSimulator.run(matches("a", 3, Duration.ofMinutes(1)), Duration.ZERO, false, null,
                at -> at.isBefore(T.plus(Duration.ofMinutes(1))));
        assertThat(result.fired()).hasSize(1);
        assertThat(result.windowSuppressed()).isEqualTo(2);
    }
}
