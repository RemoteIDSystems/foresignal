package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** ULIDs for principals (prompt 023). */
class UlidsTest {

    @Test
    @DisplayName("26 Crockford characters that sort by time")
    void shapeAndOrder() {
        String early = Ulids.next(Instant.parse("2026-01-01T00:00:00Z"));
        String late = Ulids.next(Instant.parse("2026-01-01T00:00:01Z"));

        assertThat(early).matches("^[0-9A-HJKMNP-TV-Z]{26}$");
        assertThat(Ulids.isUlid(early)).isTrue();
        assertThat(early.substring(0, 10)).isLessThan(late.substring(0, 10));
        assertThat(Ulids.isUlid("owner")).isFalse();
        assertThat(Ulids.isUlid("01JA8Q3V7S2K9W4T6Y1B5N0M3U")).isFalse();
        assertThat(Ulids.isUlid(null)).isFalse();
    }
}
