package ai.foresignal.quality;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertView;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Implicit signals (prompt 115): detection from lifecycle events, the thresholds, exercise and suppressed alerts
 * excluded, and the documented weights of {@code signalScore}.
 */
class AlertSignalTest {

    static final Instant T = Instant.parse("2026-09-01T00:00:00Z");

    static AlertView alert(Instant ackAt, Instant resolvedAt, String resolvedBy, boolean suppressed, boolean exercise) {
        return new AlertView(1L, UUID.randomUUID(), "rule", 5L, "s", "e", "k", T, T, "m", "warning",
                resolvedAt != null ? "resolved" : ackAt != null ? "acknowledged" : "firing", 1L, ackAt,
                ackAt == null ? null : "token:a", resolvedAt, resolvedBy, null, null, null, null, null, suppressed,
                suppressed ? "alert_snoozed" : null, List.of(), null, null, null, null, false, null, null, exercise,
                exercise ? "R" : null);
    }

    @Test
    @DisplayName("ACK_FAST within 15 minutes, not after")
    void ackFast() {
        assertThat(AlertSignals.of(new AlertEvent(AlertEvent.ACKNOWLEDGED, alert(T.plus(Duration.ofMinutes(15)), null,
                null, false, false), false))).map(AlertSignals.Signal::type).contains(AlertSignalType.ACK_FAST);
        assertThat(AlertSignals.of(new AlertEvent(AlertEvent.ACKNOWLEDGED, alert(T.plus(Duration.ofMinutes(16)), null,
                null, false, false), false))).isEmpty();
    }

    @Test
    @DisplayName("SNOOZED on a snooze; FLAP on an inverse-edge resolution within 10 minutes only")
    void snoozedAndFlap() {
        assertThat(AlertSignals.of(new AlertEvent(AlertEvent.SNOOZED, alert(null, null, null, false, false), false)))
                .map(AlertSignals.Signal::type).contains(AlertSignalType.SNOOZED);
        assertThat(AlertSignals.of(new AlertEvent(AlertEvent.RESOLVED, alert(null, T.plus(Duration.ofMinutes(9)),
                "inverse_edge", false, false), false))).map(AlertSignals.Signal::type).contains(AlertSignalType.FLAP);
        assertThat(AlertSignals.of(new AlertEvent(AlertEvent.RESOLVED, alert(null, T.plus(Duration.ofMinutes(11)),
                "inverse_edge", false, false), false))).isEmpty();
        assertThat(AlertSignals.of(new AlertEvent(AlertEvent.RESOLVED, alert(null, T.plus(Duration.ofMinutes(1)),
                "token:a", false, false), false))).isEmpty();
        assertThat(AlertSignals.of(new AlertEvent(AlertEvent.UPDATED, alert(null, null, null, false, false), false)))
                .isEmpty();
    }

    @Test
    @DisplayName("exercise and suppressed alerts never produce a signal")
    void excluded() {
        assertThat(AlertSignals.of(new AlertEvent(AlertEvent.SNOOZED, alert(null, null, null, false, true), false)))
                .isEmpty();
        assertThat(AlertSignals.of(new AlertEvent(AlertEvent.SNOOZED, alert(null, null, null, true, false), true)))
                .isEmpty();
    }

    @Test
    @DisplayName("weights: ACK_FAST +0.5, SNOOZED −0.5, FLAP −0.5, UNOPENED −0.25; score per alert, null without alerts")
    void weights() {
        assertThat(AlertSignalType.weights()).containsExactly(Map.entry("ACK_FAST", 0.5), Map.entry("SNOOZED", -0.5),
                Map.entry("FLAP", -0.5), Map.entry("UNOPENED", -0.25));
        Map<String, Long> counts = Map.of("ACK_FAST", 4L, "SNOOZED", 1L, "UNOPENED", 2L);
        assertThat(AlertSignalType.score(counts, 10)).isEqualTo(0.1);
        assertThat(AlertSignalType.score(counts, 0)).isNull();
        assertThat(AlertSignalType.complete(Map.of("FLAP", 3L))).containsExactly(Map.entry("ACK_FAST", 0L),
                Map.entry("SNOOZED", 0L), Map.entry("FLAP", 3L), Map.entry("UNOPENED", 0L));
        assertThat(AlertSignalType.ACK_FAST_WITHIN).isEqualTo(Duration.ofMinutes(15));
        assertThat(AlertSignalType.FLAP_WITHIN).isEqualTo(Duration.ofMinutes(10));
        assertThat(AlertSignalType.UNOPENED_AFTER).isEqualTo(Duration.ofHours(24));
    }
}
