package ai.foresignal.trigger.window;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.HealthStatus;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The coverage-gate decision table (prompt 100). */
class CoverageGateTest {

    private static final Instant TO = Instant.parse("2026-10-15T12:00:00Z");
    private static final Duration WINDOW = Duration.ofHours(2);
    private static final Duration INTERVAL = Duration.ofMinutes(10);
    private static final Instant FRESH = TO.minus(Duration.ofMinutes(5));

    private static CoverageGate.Gate decide(Boolean enabled, HealthStatus status, String reason, Instant lastDataAt,
            long total, long ok, long failed, boolean stream, boolean coverageDrop) {
        return CoverageGate.decide("src", enabled, status, reason, lastDataAt, TO, WINDOW, INTERVAL, total, ok, failed,
                stream, coverageDrop, 0.8);
    }

    @Test
    @DisplayName("a healthy, covered source passes")
    void covered() {
        assertThat(decide(true, HealthStatus.HEALTHY, null, FRESH, 12, 12, 0, false, false).ok()).isTrue();
    }

    @Test
    @DisplayName("an uninstalled or disabled source is unknown")
    void notRunning() {
        assertThat(decide(null, HealthStatus.HEALTHY, null, FRESH, 0, 0, 0, false, false).reason()).contains("not installed");
        assertThat(decide(false, HealthStatus.HEALTHY, null, FRESH, 0, 0, 0, false, false).reason()).contains("disabled");
    }

    @Test
    @DisplayName("a failing or coverage-degraded source is unknown")
    void unhealthy() {
        assertThat(decide(true, HealthStatus.FAILING, null, FRESH, 12, 12, 0, false, false).reason()).contains("failing");
        assertThat(decide(true, HealthStatus.DEGRADED, "coverage dropped: receivers 1 against a median of 8", FRESH,
                12, 12, 0, false, false).ok()).isFalse();
        assertThat(decide(true, HealthStatus.DEGRADED, "some unrelated warning", FRESH, 12, 12, 0, false, false).ok())
                .isTrue();
    }

    @Test
    @DisplayName("stale data beyond max(window, 2xinterval) is unknown")
    void stale() {
        assertThat(decide(true, HealthStatus.HEALTHY, null, TO.minus(Duration.ofHours(3)), 12, 12, 0, false, false).ok())
                .isFalse();
    }

    @Test
    @DisplayName("a low OK/NOT_MODIFIED share, no fetches, a stream disconnect or a coverage drop is unknown")
    void coverage() {
        assertThat(decide(true, HealthStatus.HEALTHY, null, FRESH, 10, 5, 5, false, false).ok()).isFalse();
        assertThat(decide(true, HealthStatus.HEALTHY, null, FRESH, 0, 0, 0, false, false).ok()).isFalse();
        assertThat(decide(true, HealthStatus.HEALTHY, null, FRESH, 10, 8, 2, true, false).ok()).isFalse();
        assertThat(decide(true, HealthStatus.HEALTHY, null, FRESH, 10, 10, 0, false, true).ok()).isFalse();
        assertThat(decide(true, HealthStatus.HEALTHY, null, FRESH, 10, 9, 1, true, false).ok()).isTrue();
    }
}
