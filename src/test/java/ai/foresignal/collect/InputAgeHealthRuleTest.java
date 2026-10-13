package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.HealthStatus;
import ai.foresignal.testsupport.weather.WeatherAdapterHarness;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * "Newest file age" health (prompt 048): degraded once the newest processed input is older than its
 * gap (GLM 2 minutes, FDC 15), healthy before, no verdict without both values; and the
 * {@code recordInput} helper that writes them.
 */
class InputAgeHealthRuleTest {

    static final Instant NOW = Instant.parse("2026-10-12T22:00:00Z");

    @Test
    @DisplayName("older than the gap is degraded, with the age and the limit in the reason")
    void degraded() {
        HealthVerdict v = InputAgeHealthRule.verdict("2026-10-12T21:57:40Z", "PT2M", NOW).orElseThrow();
        assertThat(v.status()).isEqualTo(HealthStatus.DEGRADED);
        assertThat(v.reason()).isEqualTo("newest input file is 140s old (limit 120s)");
        assertThat(v.newestFrameAt()).isEqualTo(Instant.parse("2026-10-12T21:57:40Z"));
    }

    @Test
    @DisplayName("within the gap is healthy; FDC's 15 minutes")
    void healthy() {
        assertThat(InputAgeHealthRule.verdict("2026-10-12T21:59:00Z", "PT2M", NOW).orElseThrow().status())
                .isEqualTo(HealthStatus.HEALTHY);
        assertThat(InputAgeHealthRule.verdict("2026-10-12T21:46:00Z", "PT15M", NOW).orElseThrow().status())
                .isEqualTo(HealthStatus.HEALTHY);
        assertThat(InputAgeHealthRule.verdict("2026-10-12T21:44:00Z", "PT15M", NOW).orElseThrow().status())
                .isEqualTo(HealthStatus.DEGRADED);
    }

    @Test
    @DisplayName("no verdict without both values, or with unreadable ones")
    void noVerdict() {
        assertThat(InputAgeHealthRule.verdict(null, "PT2M", NOW)).isEmpty();
        assertThat(InputAgeHealthRule.verdict("2026-10-12T21:59:00Z", null, NOW)).isEmpty();
        assertThat(InputAgeHealthRule.verdict("yesterday", "PT2M", NOW)).isEmpty();
        assertThat(InputAgeHealthRule.verdict("2026-10-12T21:59:00Z", "2 minutes", NOW)).isEmpty();
    }

    @Test
    @DisplayName("recordInput writes the instant and the gap, only on change")
    void recordInput() {
        WeatherAdapterHarness.MapState state = new WeatherAdapterHarness.MapState();
        state.recordInput(Instant.parse("2026-10-12T21:57:40Z"), Duration.ofMinutes(2));
        assertThat(state.asMap()).containsEntry("health.newestInputAt", "2026-10-12T21:57:40Z")
                .containsEntry("health.maxInputGap", "PT2M");
        state.recordInput(null, null);
        assertThat(state.asMap()).hasSize(2);
    }
}
