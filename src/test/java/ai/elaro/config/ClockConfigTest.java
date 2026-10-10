package ai.elaro.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class ClockConfigTest {

    @Test
    void clockIsUtc() {
        Clock clock = new ClockConfig().clock();

        assertThat(clock.getZone()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    void clockReportsCurrentTime() {
        Clock clock = new ClockConfig().clock();
        Instant before = Instant.now();

        Instant reading = clock.instant();

        assertThat(reading)
                .isBetween(before.minus(Duration.ofMinutes(1)), before.plus(Duration.ofMinutes(1)));
    }
}
