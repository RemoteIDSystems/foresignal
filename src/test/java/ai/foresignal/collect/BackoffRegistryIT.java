package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The durability half of backoff: a restart in the middle of one must not release the brake.
 *
 * <p>{@link BackoffRegistryTest} covers the arithmetic with no database. What can only be asserted
 * here is that the deadline reaches {@code source_health.backoff_until} and is read back by a fresh
 * registry — which is the whole point of persisting it, since the process that was asked to slow
 * down is exactly the one that might be restarted.
 */
@IntegrationTest
class BackoffRegistryIT {

    private static final String SOURCE = "backoff-test";
    private static final Instant START = Instant.parse("2026-03-01T12:00:00Z");

    @Autowired private JdbcTemplate jdbc;

    private MutableClock clock;
    private BackoffRegistry registry;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(START);
        registry = new BackoffRegistry(jdbc, clock);
    }

    private Instant storedDeadline() {
        Timestamp stored =
                jdbc.query(
                        "select backoff_until from source_health where source_id = ?",
                        rs -> rs.next() ? rs.getTimestamp(1) : null,
                        SOURCE);
        return stored == null ? null : stored.toInstant();
    }

    private long healthRows() {
        return jdbc.queryForObject(
                "select count(*) from source_health where source_id = ?", Long.class, SOURCE);
    }

    @Test
    @DisplayName("a throttle creates the health row and stores the deadline")
    void throttleCreatesTheRow() {
        assertThat(healthRows()).isZero();

        Instant deadline = registry.onThrottled(SOURCE, Optional.of(Duration.ofMinutes(10)));

        assertThat(deadline).isEqualTo(START.plus(Duration.ofMinutes(10)));
        assertThat(healthRows()).isEqualTo(1);
        assertThat(storedDeadline()).isEqualTo(deadline.truncatedTo(ChronoUnit.MILLIS));
    }

    @Test
    @DisplayName("a repeated short Retry-After escalates: 30 s, 30 s, 60 s, 120 s ... up to an hour, until a success")
    void retryAfterEscalates() {
        Duration asked = Duration.ofSeconds(30);
        assertThat(registry.onThrottled(SOURCE, Optional.of(asked))).isEqualTo(START.plusSeconds(30));
        assertThat(registry.onThrottled(SOURCE, Optional.of(asked))).isEqualTo(START.plusSeconds(30));
        assertThat(registry.onThrottled(SOURCE, Optional.of(asked))).isEqualTo(START.plusSeconds(60));
        assertThat(registry.onThrottled(SOURCE, Optional.of(asked))).isEqualTo(START.plusSeconds(120));
        for (int i = 0; i < 20; i++) {
            registry.onThrottled(SOURCE, Optional.of(asked));
        }
        assertThat(registry.onThrottled(SOURCE, Optional.of(asked))).isEqualTo(START.plus(Duration.ofHours(1)));

        registry.onSuccess(SOURCE);
        assertThat(registry.onThrottled(SOURCE, Optional.of(asked))).as("a success resets the escalation")
                .isEqualTo(START.plusSeconds(30));
    }

    @Test
    @DisplayName("a long Retry-After is honored as asked even when it exceeds the escalation step")
    void longRetryAfterWins() {
        registry.onThrottled(SOURCE, Optional.empty());
        assertThat(registry.onThrottled(SOURCE, Optional.of(Duration.ofMinutes(20))))
                .isEqualTo(START.plus(Duration.ofMinutes(20)));
    }

    @Test
    @DisplayName("a second throttle updates the existing row rather than inserting another")
    void secondThrottleUpdatesInPlace() {
        registry.onThrottled(SOURCE, Optional.empty());
        Instant second = registry.onThrottled(SOURCE, Optional.empty());

        assertThat(healthRows()).isEqualTo(1);
        // 30s then 60s: the exponent survives because the registry holds the count in memory.
        assertThat(second).isEqualTo(START.plus(Duration.ofSeconds(60)));
        assertThat(storedDeadline()).isEqualTo(second.truncatedTo(ChronoUnit.MILLIS));
    }

    @Test
    @DisplayName("a restart during a backoff still sees the source held back")
    void restartKeepsTheBrakeOn() {
        registry.onThrottled(SOURCE, Optional.of(Duration.ofMinutes(30)));

        // A new registry over the same database is what a restart looks like.
        BackoffRegistry restarted = new BackoffRegistry(jdbc, clock);
        assertThat(restarted.isInBackoff(SOURCE)).isTrue();
        assertThat(restarted.backoffUntil(SOURCE)).isEqualTo(START.plus(Duration.ofMinutes(30)));
    }

    @Test
    @DisplayName("the deadline is read from the database when the cache is cold")
    void readsThroughToTheDatabase() {
        registry.onThrottled(SOURCE, Optional.of(Duration.ofMinutes(5)));
        registry.clearCache();

        assertThat(registry.isInBackoff(SOURCE)).isTrue();
        assertThat(registry.backoffUntil(SOURCE)).isEqualTo(START.plus(Duration.ofMinutes(5)));
    }

    @Test
    @DisplayName("a persisted deadline that has passed does not hold the source back")
    void expiredPersistedDeadlineIsIgnored() {
        registry.onThrottled(SOURCE, Optional.of(Duration.ofMinutes(5)));
        registry.clearCache();
        clock.advance(Duration.ofMinutes(6));

        assertThat(registry.isInBackoff(SOURCE)).isFalse();
        assertThat(registry.backoffUntil(SOURCE)).isNull();
        // Still on the row: prompt 006 reads the history, and clearing it here would lose it.
        assertThat(storedDeadline()).isNotNull();
    }

    @Test
    @DisplayName("a success clears the stored deadline as well as the cached one")
    void successClearsThePersistedDeadline() {
        registry.onThrottled(SOURCE, Optional.of(Duration.ofMinutes(20)));
        assertThat(storedDeadline()).isNotNull();

        registry.onSuccess(SOURCE);

        assertThat(storedDeadline()).isNull();
        assertThat(registry.isInBackoff(SOURCE)).isFalse();
        assertThat(registry.consecutiveThrottles(SOURCE)).isZero();

        // And a restart agrees, rather than reviving the backoff.
        assertThat(new BackoffRegistry(jdbc, clock).isInBackoff(SOURCE)).isFalse();
    }

    @Test
    @DisplayName("a success resets the exponent, so the next throttle starts at 30 seconds again")
    void successResetsTheExponent() {
        registry.onThrottled(SOURCE, Optional.empty());
        registry.onThrottled(SOURCE, Optional.empty());
        registry.onSuccess(SOURCE);

        assertThat(registry.onThrottled(SOURCE, Optional.empty()))
                .isEqualTo(START.plus(BackoffRegistry.BASE_BACKOFF));
    }

    @Test
    @DisplayName("a success for a source that was never throttled writes nothing")
    void successOnAHealthySourceDoesNotTouchTheTable() {
        registry.onSuccess(SOURCE);
        assertThat(healthRows()).isZero();
    }

    @Test
    @DisplayName("a source with no health row is not in backoff")
    void unknownSourceIsNotInBackoff() {
        assertThat(registry.isInBackoff("never-throttled")).isFalse();
        assertThat(registry.backoffUntil("never-throttled")).isNull();
    }

    @Test
    @DisplayName("an existing health row keeps its other columns when a backoff is written")
    void otherHealthColumnsSurvive() {
        jdbc.update(
                """
                insert into source_health (source_id, consecutive_failures, recent_record_counts,
                                           adapter_state, backoff_until)
                values (?, 4, '[1,2,3]'::jsonb, '{"cursor":"abc"}'::jsonb, null)
                """,
                SOURCE);

        registry.onThrottled(SOURCE, Optional.of(Duration.ofMinutes(10)));

        assertThat(storedDeadline()).isEqualTo(START.plus(Duration.ofMinutes(10)));
        assertThat(jdbc.queryForObject(
                        "select consecutive_failures from source_health where source_id = ?",
                        Integer.class,
                        SOURCE))
                .isEqualTo(4);
        assertThat(jdbc.queryForObject(
                        "select adapter_state::text from source_health where source_id = ?",
                        String.class,
                        SOURCE))
                .contains("abc");
    }

    @Test
    @DisplayName("an HTTP-date Retry-After is honored against the injected clock")
    void httpDateRetryAfterIsPersisted() {
        Optional<Duration> parsed = registry.parseRetryAfter("Sun, 01 Mar 2026 12:20:00 GMT");
        assertThat(parsed).contains(Duration.ofMinutes(20));

        Instant deadline = registry.onThrottled(SOURCE, parsed);

        assertThat(deadline).isEqualTo(START.plus(Duration.ofMinutes(20)));
        assertThat(storedDeadline()).isEqualTo(deadline.truncatedTo(ChronoUnit.MILLIS));
    }
}
