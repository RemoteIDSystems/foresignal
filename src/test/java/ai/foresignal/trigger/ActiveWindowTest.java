package ai.foresignal.trigger;

import static ai.foresignal.trigger.TestRules.rule;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.MutableClock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link ActiveWindow} and the active period of {@link RuleSpec}, across both DST transitions (prompt 017). */
class ActiveWindowTest {

    static final ZoneId CHICAGO = ZoneId.of("America/Chicago");

    static ActiveWindow nights() {
        return new ActiveWindow(CHICAGO, List.of(new ActiveWindow.Window(Set.of(DayOfWeek.SATURDAY), LocalTime.of(22, 0),
                LocalTime.of(7, 0))));
    }

    static ActiveWindow mornings() {
        return new ActiveWindow(CHICAGO, List.of(new ActiveWindow.Window(Set.of(DayOfWeek.SUNDAY), LocalTime.of(1, 0),
                LocalTime.of(4, 0))));
    }

    @Test
    @DisplayName("a window spanning midnight covers the late evening and the next early morning")
    void midnight() {
        ActiveWindow window = nights();
        assertThat(window.isActive(Instant.parse("2026-06-07T03:30:00Z"))).as("Sat 22:30 CDT").isTrue();
        assertThat(window.isActive(Instant.parse("2026-06-07T11:59:00Z"))).as("Sun 06:59 CDT").isTrue();
        assertThat(window.isActive(Instant.parse("2026-06-07T12:00:00Z"))).as("Sun 07:00 CDT").isFalse();
        assertThat(window.isActive(Instant.parse("2026-06-06T02:30:00Z"))).as("Fri 21:30 CDT").isFalse();
    }

    @Test
    @DisplayName("spring forward: 02:00-03:00 local does not exist, the window still covers wall-clock time")
    void springForward() {
        MutableClock clock = new MutableClock(Instant.parse("2026-03-08T07:30:00Z")); // Sun 01:30 CST
        ActiveWindow window = mornings();
        assertThat(window.isActive(clock.instant())).isTrue();
        clock.advance(Duration.ofMinutes(45)); // 08:15Z = 03:15 CDT (the clock jumped)
        assertThat(window.isActive(clock.instant())).isTrue();
        clock.advance(Duration.ofMinutes(50)); // 09:05Z = 04:05 CDT
        assertThat(window.isActive(clock.instant())).isFalse();
    }

    @Test
    @DisplayName("fall back: 01:00-02:00 local happens twice and is active both times")
    void fallBack() {
        MutableClock clock = new MutableClock(Instant.parse("2026-11-01T06:30:00Z")); // Sun 01:30 CDT
        ActiveWindow window = mornings();
        assertThat(window.isActive(clock.instant())).isTrue();
        clock.advance(Duration.ofHours(1)); // 07:30Z = 01:30 CST, the second time
        assertThat(window.isActive(clock.instant())).isTrue();
        clock.advance(Duration.ofHours(3)); // 10:30Z = 04:30 CST
        assertThat(window.isActive(clock.instant())).isFalse();
    }

    @Test
    @DisplayName("round trip through the stored form")
    void storage() {
        ActiveWindow window = nights();
        assertThat(ActiveWindow.fromMap(window.toMap())).isEqualTo(window);
        assertThat(window.toMap()).containsEntry("timezone", "America/Chicago");
        assertThat(ActiveWindow.fromMap(null)).isNull();
    }

    @Test
    @DisplayName("activeFrom is inclusive, activeUntil exclusive, and the window applies on top")
    void period() {
        TestRules builder = rule().sources("s");
        builder.activeFrom = Instant.parse("2026-06-01T00:00:00Z");
        builder.activeUntil = Instant.parse("2026-07-01T00:00:00Z");
        RuleSpec spec = builder.build();
        assertThat(spec.isActiveAt(Instant.parse("2026-05-31T23:59:59Z"))).isFalse();
        assertThat(spec.isActiveAt(Instant.parse("2026-06-01T00:00:00Z"))).isTrue();
        assertThat(spec.isActiveAt(Instant.parse("2026-07-01T00:00:00Z"))).isFalse();
        builder.window = nights();
        assertThat(builder.build().isActiveAt(Instant.parse("2026-06-07T03:30:00Z"))).isTrue();
        assertThat(builder.build().isActiveAt(Instant.parse("2026-06-07T15:00:00Z"))).isFalse();
    }
}
