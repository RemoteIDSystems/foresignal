package ai.foresignal.trigger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The small value types of the alert model: enums, {@link AlertQuery}, {@link AlertEvent}, {@link AutoResolve}. */
class AlertModelTest {

    static final UUID RULE = UUID.randomUUID();
    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");

    static AlertView alert(String state, String severity, boolean suppressed, String feedback) {
        return new AlertView(1, RULE, "r", 10, "s", "e", "k", T, T, "m", severity, state, 5, null, null, null, null, null,
                feedback, null, null, null, suppressed, suppressed ? "rule_snoozed" : null, List.of("t1"), null);
    }

    @Test
    @DisplayName("severity, state, feedback and scope wire values")
    void enums() {
        assertThat(Severity.fromWire("critical")).isEqualTo(Severity.CRITICAL);
        assertThat(Severity.WARNING.atOrAbove()).containsExactly("warning", "critical");
        assertThatThrownBy(() -> Severity.fromWire("high")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Severity.fromWire(null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(AlertState.RESOLVED.wire()).isEqualTo("resolved");
        assertThat(Feedback.fromWire("false_positive")).isEqualTo(Feedback.FALSE_POSITIVE);
        assertThatThrownBy(() -> Feedback.fromWire("meh")).isInstanceOf(IllegalArgumentException.class);
        assertThat(CooldownScope.fromWire("rule").wire()).isEqualTo("rule");
        assertThatThrownBy(() -> CooldownScope.fromWire("x")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("AlertQuery.matches agrees with every filter")
    void query() {
        AlertView a = alert("firing", "warning", false, null);
        assertThat(AlertQuery.all().matches(a)).isTrue();
        assertThat(AlertQuery.all().matches(alert("firing", "warning", true, null))).as("suppressed hidden").isFalse();
        assertThat(new AlertQuery(Set.of(UUID.randomUUID()), null, null, null, null, null, null, null, null, false, null).matches(a)).isFalse();
        assertThat(new AlertQuery(null, List.of("other"), null, null, null, null, null, null, null, false, null).matches(a)).isFalse();
        assertThat(new AlertQuery(null, null, List.of("k"), null, null, null, null, null, null, false, null).matches(a)).isTrue();
        assertThat(new AlertQuery(null, null, null, T.plusSeconds(1), null, null, null, null, null, false, null).matches(a)).isFalse();
        assertThat(new AlertQuery(null, null, null, null, true, null, null, null, null, false, null).matches(a)).isFalse();
        assertThat(new AlertQuery(null, null, null, null, null, List.of("resolved"), null, null, null, false, null).matches(a)).isFalse();
        assertThat(new AlertQuery(null, null, null, null, null, null, List.of("noise"), null, null, false, null).matches(a)).isFalse();
        assertThat(new AlertQuery(null, null, null, null, null, null, null, Severity.CRITICAL, null, false, null).matches(a)).isFalse();
        assertThat(new AlertQuery(null, null, null, null, null, null, null, Severity.INFO, "t1", false, null).matches(a)).isTrue();
        assertThat(new AlertQuery(null, null, null, null, null, null, null, null, null, false, Set.of("s")).matches(a)).isFalse();
    }

    @Test
    @DisplayName("events map to the three SSE names and carry the suppressed flag")
    void events() {
        AlertView suppressed = alert("firing", "info", true, null);
        assertThat(AlertEvent.of(AlertEvent.FIRED, suppressed).suppressed()).isTrue();
        assertThat(AlertEvent.of(AlertEvent.FIRED, suppressed).sseEvent()).isEqualTo("alert");
        assertThat(AlertEvent.of(AlertEvent.RESOLVED, suppressed).sseEvent()).isEqualTo("alert.resolved");
        assertThat(AlertEvent.of(AlertEvent.ACKNOWLEDGED, suppressed).sseEvent()).isEqualTo("alert.updated");
        assertThat(alert("resolved", "info", false, null).open()).isFalse();
    }

    @Test
    @DisplayName("AutoResolve round-trips through storage and the API form")
    void autoResolve() {
        AutoResolve stale = new AutoResolve(true, false, true, Duration.ofHours(2));
        assertThat(AutoResolve.fromStorage(stale.toStorage())).isEqualTo(stale);
        assertThat(stale.toApi()).containsEntry("staleAfter", "2h").containsEntry("onTombstone", false);
        assertThat(AutoResolve.fromStorage(null)).isNull();
        assertThat(AutoResolve.ALL.needsEdgePass()).isTrue();
        assertThat(new AutoResolve(false, false, false, null).needsPeriodicPass()).isFalse();
    }
}
