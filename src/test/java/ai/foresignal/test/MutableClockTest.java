package ai.foresignal.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MutableClockTest {

    private static final Instant START = Instant.parse("2026-03-01T12:00:00Z");

    @Test
    @DisplayName("defaults to a fixed instant in UTC")
    void defaultsToFixedUtcInstant() {
        var clock = new MutableClock();

        assertThat(clock.instant()).isEqualTo(MutableClock.DEFAULT_INSTANT);
        assertThat(clock.getZone()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    @DisplayName("does not move on its own")
    void doesNotMoveOnItsOwn() {
        var clock = new MutableClock(START);

        assertThat(clock.instant()).isEqualTo(START);
        assertThat(clock.instant()).isEqualTo(START);
    }

    @Test
    void advancesForward() {
        var clock = new MutableClock(START);

        clock.advance(Duration.ofMinutes(90));

        assertThat(clock.instant()).isEqualTo(START.plus(Duration.ofMinutes(90)));
    }

    @Test
    @DisplayName("a negative duration moves the clock backwards")
    void advancesBackwards() {
        var clock = new MutableClock(START);

        clock.advance(Duration.ofSeconds(-30));

        assertThat(clock.instant()).isEqualTo(START.minusSeconds(30));
    }

    @Test
    void advancesSeconds() {
        var clock = new MutableClock(START);

        clock.advanceSeconds(45);

        assertThat(clock.instant()).isEqualTo(START.plusSeconds(45));
    }

    @Test
    void setsAbsoluteInstant() {
        var clock = new MutableClock(START);

        clock.setInstant(MutableClock.DEFAULT_INSTANT);

        assertThat(clock.instant()).isEqualTo(MutableClock.DEFAULT_INSTANT);
    }

    @Test
    void resetsToDefaultInstant() {
        var clock = new MutableClock(START);
        clock.advance(Duration.ofDays(5));

        clock.reset();

        assertThat(clock.instant()).isEqualTo(MutableClock.DEFAULT_INSTANT);
    }

    @Test
    @DisplayName("withZone returns a clock in the requested zone at the same instant")
    void withZoneReturnsRezonedClock() {
        var clock = new MutableClock(START);

        Clock paris = clock.withZone(ZoneId.of("Europe/Paris"));

        assertThat(paris).isNotSameAs(clock);
        assertThat(paris.getZone()).isEqualTo(ZoneId.of("Europe/Paris"));
        assertThat(paris.instant()).isEqualTo(START);
    }

    @Test
    @DisplayName("withZone returns the same instance for the zone it already has")
    void withZoneReturnsSameInstanceForSameZone() {
        var clock = new MutableClock(START);

        assertThat(clock.withZone(ZoneOffset.UTC)).isSameAs(clock);
    }

    @Test
    @DisplayName("the test configuration supplies a mutable clock")
    void testConfigSuppliesMutableClock() {
        MutableClock clock = new MutableClock.TestConfig().mutableClock();

        assertThat(clock.instant()).isEqualTo(MutableClock.DEFAULT_INSTANT);
    }

    @Test
    @DisplayName("millis() agrees with instant()")
    void millisAgreesWithInstant() {
        var clock = new MutableClock(START);

        assertThat(clock.millis()).isEqualTo(START.toEpochMilli());
    }
}
