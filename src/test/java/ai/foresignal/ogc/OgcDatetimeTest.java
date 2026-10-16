package ai.foresignal.ogc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OgcDatetimeTest {

    @Test
    @DisplayName("an instant, closed and half-bounded intervals, and dates as whole UTC days")
    void forms() {
        Instant t = Instant.parse("2026-10-16T12:00:00Z");
        assertThat(OgcDatetime.parse("2026-10-16T12:00:00Z")).isEqualTo(new OgcDatetime(t, t));
        assertThat(OgcDatetime.parse("2026-10-16T12:00:00Z").isInstant()).isTrue();
        assertThat(OgcDatetime.parse("2026-10-16T07:00:00-05:00/2026-10-17T00:00:00Z"))
                .isEqualTo(new OgcDatetime(t, Instant.parse("2026-10-17T00:00:00Z")));
        assertThat(OgcDatetime.parse("../2026-10-16T12:00:00Z")).isEqualTo(new OgcDatetime(null, t));
        assertThat(OgcDatetime.parse("2026-10-16T12:00:00Z/..")).isEqualTo(new OgcDatetime(t, null));
        assertThat(OgcDatetime.parse("2026-10-16T12:00:00Z/")).isEqualTo(new OgcDatetime(t, null));
        OgcDatetime day = OgcDatetime.parse("2026-10-16");
        assertThat(day.from()).isEqualTo(Instant.parse("2026-10-16T00:00:00Z"));
        assertThat(day.to()).isBefore(Instant.parse("2026-10-17T00:00:00Z")).isAfter(Instant.parse("2026-10-16T23:59:59Z"));
        assertThat(OgcDatetime.parse(null)).isNull();
        assertThat(OgcDatetime.parse(" ")).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"yesterday", "../..", "a/b/c", "2026-10-17T00:00:00Z/2026-10-16T00:00:00Z", "2026-13-01"})
    @DisplayName("malformed values are 400")
    void invalid(String raw) {
        assertThatThrownBy(() -> OgcDatetime.parse(raw)).isInstanceOf(ValidationException.class);
    }
}
