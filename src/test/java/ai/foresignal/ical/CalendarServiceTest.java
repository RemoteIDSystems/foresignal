package ai.foresignal.ical;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalendarServiceTest {

    @Test
    @DisplayName("briefing occurrences follow local time across a daylight-saving change")
    void occurrencesAcrossDst() {
        // America/Chicago leaves daylight time on 2026-11-01
        Instant from = Instant.parse("2026-10-30T00:00:00Z");
        Instant to = Instant.parse("2026-11-03T00:00:00Z");
        List<Instant> at = CalendarService.occurrences("0 7 * * *", "America/Chicago", from, to);
        assertThat(at).hasSize(4);
        ZoneId chicago = ZoneId.of("America/Chicago");
        assertThat(at).allSatisfy(i -> assertThat(ZonedDateTime.ofInstant(i, chicago).getHour()).isEqualTo(7));
        assertThat(at.get(0)).isEqualTo(Instant.parse("2026-10-30T12:00:00Z"));
        assertThat(at.get(3)).isEqualTo(Instant.parse("2026-11-02T13:00:00Z"));
    }

    @Test
    @DisplayName("payload times are ISO strings or epoch seconds or milliseconds")
    void instants() {
        assertThat(CalendarService.instant("2026-10-16T12:00:00Z")).isEqualTo(Instant.parse("2026-10-16T12:00:00Z"));
        assertThat(CalendarService.instant("2026-10-16T07:00:00-05:00")).isEqualTo(Instant.parse("2026-10-16T12:00:00Z"));
        assertThat(CalendarService.instant(1_760_616_000L)).isEqualTo(Instant.ofEpochSecond(1_760_616_000L));
        assertThat(CalendarService.instant(1_760_616_000_000L)).isEqualTo(Instant.ofEpochMilli(1_760_616_000_000L));
        assertThat(CalendarService.instant("soon")).isNull();
        assertThat(CalendarService.instant(null)).isNull();
    }
}
