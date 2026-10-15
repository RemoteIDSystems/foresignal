package ai.foresignal.analyst;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The deterministic change detector (prompt 102). */
class AnalystChangeDetectorTest {

    private static final Instant T = Instant.parse("2026-03-01T12:00:00Z");

    private static AnalystChangeDetector.Input input(String kind, String id, String severity, Double confidence,
            Instant at, String origin) {
        return new AnalystChangeDetector.Input(kind, id, "title", severity, confidence, at, origin, Map.of());
    }

    @Test
    @DisplayName("nothing new is a noop packet")
    void noop() {
        AnalystCursor cursor = new AnalystCursor(null, 5L, T, 9L);
        var packet = AnalystChangeDetector.detect(cursor, List.of(input("alert", "5", "warning", null, T, null),
                input("anomaly", "9", "warning", null, T, "recipe")), true);
        assertThat(packet.items()).isEmpty();
    }

    @Test
    @DisplayName("the loop guard excludes analyst-origin events")
    void loopGuard() {
        AnalystCursor cursor = AnalystCursor.empty();
        var packet = AnalystChangeDetector.detect(cursor, List.of(
                input("event", "E1", "warning", 0.5, T, "analyst"),
                input("event", "E2", "warning", 0.5, T.plusSeconds(1), "recipe")), true);
        assertThat(packet.items()).extracting(AnalystChangeDetector.Input::id).containsExactly("E2");
    }

    @Test
    @DisplayName("inputs are ranked by severity, then confidence, then recency, and capped")
    void ranking() {
        AnalystCursor cursor = AnalystCursor.empty();
        var packet = AnalystChangeDetector.detect(cursor, List.of(
                input("event", "info", "info", 0.9, T, "recipe"),
                input("event", "crit", "critical", 0.5, T, "recipe"),
                input("event", "warn", "warning", 0.8, T, "recipe")), true);
        assertThat(packet.items()).extracting(AnalystChangeDetector.Input::id)
                .containsExactly("crit", "warn", "info");
    }

    @Test
    @DisplayName("the cursor advances to the newest input")
    void cursorAdvances() {
        AnalystCursor cursor = AnalystCursor.empty();
        var packet = AnalystChangeDetector.detect(cursor, List.of(input("alert", "7", "warning", null, T, null),
                input("anomaly", "11", "warning", null, T, "recipe")), true);
        assertThat(packet.nextCursor().alertId()).isEqualTo(7);
        assertThat(packet.nextCursor().anomalyId()).isEqualTo(11);
        assertThat(packet.counts()).containsEntry("alerts", 1).containsEntry("anomalies", 1);
    }
}
