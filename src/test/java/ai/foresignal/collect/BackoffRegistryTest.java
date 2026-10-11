package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** The delay arithmetic and Retry-After parsing, without a database. */
class BackoffRegistryTest {

    private static final Instant NOW = Instant.parse("2026-03-01T12:00:00Z");

    private final BackoffRegistry registry =
            new BackoffRegistry(null, Clock.fixed(NOW, ZoneOffset.UTC));

    @ParameterizedTest(name = "throttle #{0} waits {1}s")
    @CsvSource({"1, 30", "2, 60", "3, 120", "4, 240", "5, 480", "6, 960", "7, 1920"})
    @DisplayName("exponential backoff doubles from 30 seconds")
    void exponentialDoubles(int attempt, long expectedSeconds) {
        assertThat(BackoffRegistry.exponential(attempt)).isEqualTo(Duration.ofSeconds(expectedSeconds));
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 9, 20, 100, Integer.MAX_VALUE})
    @DisplayName("exponential backoff is capped at an hour, so an outage settles into hourly probes")
    void exponentialIsCapped(int attempt) {
        assertThat(BackoffRegistry.exponential(attempt)).isEqualTo(BackoffRegistry.MAX_BACKOFF);
    }

    @Test
    @DisplayName("a zero or negative attempt is treated as the first")
    void attemptFloorIsOne() {
        assertThat(BackoffRegistry.exponential(0)).isEqualTo(BackoffRegistry.BASE_BACKOFF);
        assertThat(BackoffRegistry.exponential(-5)).isEqualTo(BackoffRegistry.BASE_BACKOFF);
    }

    @ParameterizedTest(name = "Retry-After {0}s becomes {1}s")
    @CsvSource({"0, 5", "1, 5", "4, 5", "5, 5", "90, 90", "3600, 3600", "7200, 3600"})
    @DisplayName("an upstream's request is clamped to [5s, 1h]")
    void clampsRetryAfter(long given, long expected) {
        assertThat(BackoffRegistry.clampRetryAfter(Duration.ofSeconds(given)))
                .isEqualTo(Duration.ofSeconds(expected));
    }

    @Test
    @DisplayName("a negative or missing Retry-After falls back to the floor")
    void clampHandlesOddValues() {
        assertThat(BackoffRegistry.clampRetryAfter(Duration.ofSeconds(-9)))
                .isEqualTo(BackoffRegistry.MIN_RETRY_AFTER);
        assertThat(BackoffRegistry.clampRetryAfter(null)).isEqualTo(BackoffRegistry.MIN_RETRY_AFTER);
    }

    @Test
    @DisplayName("Retry-After in delta-seconds is parsed")
    void parsesDeltaSeconds() {
        assertThat(registry.parseRetryAfter("120")).contains(Duration.ofSeconds(120));
        assertThat(registry.parseRetryAfter("  120  ")).contains(Duration.ofSeconds(120));
    }

    @Test
    @DisplayName("Retry-After as an HTTP-date is parsed relative to now")
    void parsesHttpDate() {
        assertThat(registry.parseRetryAfter("Sun, 01 Mar 2026 12:02:00 GMT"))
                .contains(Duration.ofMinutes(2));
    }

    @Test
    @DisplayName("an HTTP-date already in the past becomes zero, not a negative delay")
    void pastHttpDateIsZero() {
        assertThat(registry.parseRetryAfter("Sun, 01 Mar 2026 11:00:00 GMT")).contains(Duration.ZERO);
    }

    @Test
    void negativeDeltaSecondsIsZero() {
        assertThat(registry.parseRetryAfter("-5")).contains(Duration.ZERO);
    }

    @ParameterizedTest
    @ValueSource(strings = {"soon", "", "   ", "not-a-date", "12x"})
    @DisplayName("an unparseable Retry-After falls back to exponential backoff")
    void unparseableFallsBack(String value) {
        assertThat(registry.parseRetryAfter(value)).isEmpty();
    }

    @Test
    void nullRetryAfterIsEmpty() {
        assertThat(registry.parseRetryAfter(null)).isEmpty();
    }

    @Test
    @DisplayName("the clamp is applied to what the upstream asked for, not to the raw header")
    void clampAppliesToParsedValue() {
        Optional<Duration> parsed = registry.parseRetryAfter("1");

        assertThat(parsed).contains(Duration.ofSeconds(1));
        assertThat(BackoffRegistry.clampRetryAfter(parsed.orElseThrow()))
                .isEqualTo(BackoffRegistry.MIN_RETRY_AFTER);
    }
}
