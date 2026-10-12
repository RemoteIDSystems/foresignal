package ai.foresignal.notify;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.settings.NotificationSettings;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link QuietHours} and {@link Deferral} (prompt 018). */
class QuietHoursTest {

    static final ZoneId CHICAGO = ZoneId.of("America/Chicago");
    final QuietHours nights = new QuietHours(LocalTime.of(22, 0), LocalTime.of(7, 0), Set.of(), "critical");

    @Test
    @DisplayName("an overnight window ends at the next end time in the channel zone")
    void overnight() {
        // 2026-05-01 23:30 CDT = 04:30Z on 05-02
        assertThat(nights.windowEnd(Instant.parse("2026-05-02T04:30:00Z"), CHICAGO))
                .contains(Instant.parse("2026-05-02T12:00:00Z"));
        // 06:59 CDT still inside the window that began the evening before
        assertThat(nights.windowEnd(Instant.parse("2026-05-02T11:59:00Z"), CHICAGO)).isPresent();
        // 07:00 CDT is outside
        assertThat(nights.windowEnd(Instant.parse("2026-05-02T12:00:00Z"), CHICAGO)).isEmpty();
        assertThat(nights.windowEnd(Instant.parse("2026-05-02T18:00:00Z"), CHICAGO)).isEmpty();
    }

    @Test
    @DisplayName("days restrict the days a window starts on")
    void days() {
        QuietHours weekdays = new QuietHours(LocalTime.of(22, 0), LocalTime.of(7, 0), Set.of(DayOfWeek.FRIDAY), null);
        // Friday 2026-05-01 23:00 CDT: inside
        assertThat(weekdays.windowEnd(Instant.parse("2026-05-02T04:00:00Z"), CHICAGO)).isPresent();
        // Saturday 23:00 CDT: no window starts on Saturday
        assertThat(weekdays.windowEnd(Instant.parse("2026-05-03T04:00:00Z"), CHICAGO)).isEmpty();
    }

    @Test
    @DisplayName("a same-day window; the bypass severity; the stored form round trips")
    void sameDayBypassAndMap() {
        QuietHours lunch = new QuietHours(LocalTime.of(12, 0), LocalTime.of(13, 0), Set.of(), "warning");
        assertThat(lunch.windowEnd(Instant.parse("2026-05-01T17:30:00Z"), CHICAGO)).contains(Instant.parse("2026-05-01T18:00:00Z"));
        assertThat(lunch.bypasses("warning")).isTrue();
        assertThat(lunch.bypasses("info")).isFalse();
        assertThat(new QuietHours(LocalTime.MIDNIGHT, LocalTime.NOON, Set.of(), null).bypasses("critical")).isFalse();
        assertThat(QuietHours.fromMap(lunch.toMap())).isEqualTo(lunch);
        assertThat(QuietHours.fromMap(null)).isNull();
    }

    @Test
    @DisplayName("deferral: the pause wins over quiet hours; each has its bypass")
    void deferral() {
        Instant inside = Instant.parse("2026-05-02T04:30:00Z");
        NotificationSettings pause = new NotificationSettings(Instant.parse("2026-05-03T00:00:00Z"), "critical");
        assertThat(Deferral.check("warning", nights, CHICAGO, pause, inside)).hasValueSatisfying(d -> {
            assertThat(d.reason()).isEqualTo("paused");
            assertThat(d.until()).isEqualTo(Instant.parse("2026-05-03T00:00:00Z"));
        });
        assertThat(Deferral.check("warning", nights, CHICAGO, NotificationSettings.defaults(), inside))
                .hasValueSatisfying(d -> assertThat(d.reason()).isEqualTo("quiet_hours"));
        assertThat(Deferral.check("critical", nights, CHICAGO, pause, inside)).isEmpty();
        assertThat(Deferral.check("info", null, CHICAGO, new NotificationSettings(Instant.parse("2026-05-01T00:00:00Z"), null),
                inside)).isEmpty();
    }
}
