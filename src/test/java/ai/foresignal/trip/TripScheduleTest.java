package ai.foresignal.trip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** Trip schedules and the static trip input checks (prompt 114). */
class TripScheduleTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static TripSchedule parse(String json) {
        return TripSchedule.parse(JSON.readTree(json));
    }

    @Test
    @DisplayName("a one-off trip has one occurrence and none after it")
    void once() {
        TripSchedule s = parse("{\"type\":\"once\",\"departAt\":\"2026-10-16T08:00:00Z\"}");
        assertThat(s.recurring()).isFalse();
        assertThat(s.next(Instant.parse("2026-10-16T07:00:00Z"))).isEqualTo(Instant.parse("2026-10-16T08:00:00Z"));
        assertThat(s.next(Instant.parse("2026-10-16T09:00:00Z"))).isNull();
        assertThat(s.occurrences(Instant.parse("2026-10-16T00:00:00Z"), Instant.parse("2026-10-17T00:00:00Z")))
                .hasSize(1);
        assertThat(s.toMap()).containsEntry("departAt", "2026-10-16T08:00:00Z");
    }

    @Test
    @DisplayName("a recurring commute runs only on its days, at its local time")
    void recurring() {
        TripSchedule s = parse("{\"type\":\"recurring\",\"days\":[\"MO\",\"FR\"],\"departTime\":\"08:15\","
                + "\"timezone\":\"Europe/Amsterdam\"}");
        // 2026-10-16 is a Friday; Amsterdam is UTC+2 until 25 October.
        assertThat(s.next(Instant.parse("2026-10-16T00:00:00Z"))).isEqualTo(Instant.parse("2026-10-16T06:15:00Z"));
        assertThat(s.next(Instant.parse("2026-10-16T07:00:00Z"))).isEqualTo(Instant.parse("2026-10-19T06:15:00Z"));
        // After the change to UTC+1 the same local time is an hour later in UTC.
        assertThat(s.next(Instant.parse("2026-10-27T00:00:00Z"))).isEqualTo(Instant.parse("2026-10-30T07:15:00Z"));
    }

    @Test
    @DisplayName("invalid schedules are refused")
    void invalid() {
        for (String json : new String[] {"{}", "{\"type\":\"daily\"}", "{\"type\":\"once\"}",
                "{\"type\":\"once\",\"departAt\":\"tomorrow\"}", "{\"type\":\"recurring\",\"days\":[]}",
                "{\"type\":\"recurring\",\"days\":[\"XX\"],\"departTime\":\"08:00\",\"timezone\":\"UTC\"}",
                "{\"type\":\"recurring\",\"days\":[\"MO\"],\"departTime\":\"8am\",\"timezone\":\"UTC\"}",
                "{\"type\":\"recurring\",\"days\":[\"MO\"],\"departTime\":\"08:00\",\"timezone\":\"Mars/Base\"}"}) {
            assertThatThrownBy(() -> parse(json)).as(json).isInstanceOf(ValidationException.class);
        }
        assertThatThrownBy(() -> TripSchedule.parse(null)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("the static input checks accept a valid trip and refuse bad fields")
    void staticChecks() {
        String ok = "{\"name\":\"x\",\"route\":{\"type\":\"LineString\",\"coordinates\":[[0,0],[1,1]]},"
                + "\"schedule\":{\"type\":\"once\",\"departAt\":\"2026-10-16T08:00:00Z\"},\"expectedDuration\":\"1h\"}";
        TripService.validateInput(JSON.readTree(ok));
        assertThatThrownBy(() -> TripService.validateInput(JSON.readTree(ok.replace("\"1h\"", "\"8d\""))))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> TripService.validateInput(JSON.readTree(ok.replace("\"expectedDuration\"",
                "\"bufferMeters\":0,\"expectedDuration\"")))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> TripService.validateInput(JSON.readTree(ok.replace("\"expectedDuration\"",
                "\"minSeverity\":\"huge\",\"expectedDuration\"")))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> TripService.validateInput(JSON.readTree(ok.replace("\"expectedDuration\"",
                "\"channelIds\":[\"nope\"],\"expectedDuration\"")))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> TripService.validateInput(JSON.readTree(ok.replace("\"expectedDuration\"",
                "\"anchor\":\"bad\",\"expectedDuration\"")))).isInstanceOf(ValidationException.class);
        assertThat(TripWatch.tag("01JA8Q3V7S2K9W4T6Y1B5N0M3C")).isEqualTo("trip-01ja8q3v7s2k9w4t6y1b5n0m3c")
                .matches("^[a-z0-9][a-z0-9_-]{0,31}$");
    }
}
