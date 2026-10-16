package ai.foresignal.source.adapters.env.epauv;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** EPA UV time parsing in the place's timezone (prompt 106). */
class EpaUvTimeTest {

    @Test
    @DisplayName("parses 'Oct/16/2026 07 AM' in America/Chicago as 12:00 UTC (CDT)")
    void chicago() {
        Instant at = EpaUvAdapter.parseEpaTime("Oct/16/2026 07 AM", ZoneId.of("America/Chicago"));
        assertThat(at).isEqualTo(Instant.parse("2026-10-16T12:00:00Z"));
    }

    @Test
    @DisplayName("parses PM and UTC")
    void pm() {
        assertThat(EpaUvAdapter.parseEpaTime("Oct/16/2026 07 PM", ZoneId.of("UTC")))
                .isEqualTo(Instant.parse("2026-10-16T19:00:00Z"));
    }

    @Test
    @DisplayName("an unreadable time is null")
    void bad() {
        assertThat(EpaUvAdapter.parseEpaTime("later", ZoneId.of("UTC"))).isNull();
    }
}
