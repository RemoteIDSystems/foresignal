package ai.foresignal.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.trigger.TriggerEngine;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The catch-up rule: lag over 10,000 seq, or the queue above 80% for over a minute (prompt 019). */
class CatchUpMonitorTest {

    static final Instant T = Instant.parse("2026-06-01T00:00:00Z");

    final CatchUpMonitor monitor = new CatchUpMonitor(mock(ChangeBus.class), mock(TriggerEngine.class),
            mock(IngestQueue.class), Clock.systemUTC());
    final CatchUpHealthIndicator indicator = new CatchUpHealthIndicator(monitor);

    @Test
    @DisplayName("a lag above 10,000 seq is catching up at once")
    void lag() {
        assertThat(monitor.evaluate(T, 10_000, 0, 64).catchingUp()).isFalse();
        assertThat(monitor.evaluate(T, 10_001, 0, 64).catchingUp()).isTrue();
        assertThat(indicator.health().getStatus().getCode()).isEqualTo("OUT_OF_SERVICE");
        assertThat(monitor.evaluate(T, 0, 0, 64).catchingUp()).isFalse();
        assertThat(indicator.health().getStatus().getCode()).isEqualTo("UP");
    }

    @Test
    @DisplayName("a high queue counts only after a minute, and resets when it drains")
    void queue() {
        assertThat(monitor.evaluate(T, 0, 60, 64).catchingUp()).isFalse();
        assertThat(monitor.evaluate(T.plus(Duration.ofSeconds(60)), 0, 60, 64).catchingUp()).isFalse();
        assertThat(monitor.evaluate(T.plus(Duration.ofSeconds(61)), 0, 60, 64).catchingUp()).isTrue();
        assertThat(monitor.evaluate(T.plus(Duration.ofSeconds(62)), 0, 10, 64).catchingUp()).isFalse();
        assertThat(monitor.evaluate(T.plus(Duration.ofSeconds(63)), 0, 60, 64).catchingUp()).isFalse();
    }
}
