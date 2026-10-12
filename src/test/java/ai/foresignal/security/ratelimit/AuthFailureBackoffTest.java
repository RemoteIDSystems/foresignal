package ai.foresignal.security.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.MutableClock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link AuthFailureBackoff} (prompt 035): threshold, doubling, cap, success reset, metrics, bounds. */
class AuthFailureBackoffTest {

    static final String IP = "203.0.113.7";

    MutableClock clock;
    SimpleMeterRegistry meters;
    AuthFailureBackoff backoff;

    @BeforeEach
    void setUp() {
        clock = new MutableClock();
        meters = new SimpleMeterRegistry();
        backoff = new AuthFailureBackoff(clock, RateLimitProperties.defaults(), meters);
    }

    /** Reports {@code n} failures from {@code ip}, one second apart, and returns the answers. */
    List<Optional<Duration>> fail(String ip, int n) {
        List<Optional<Duration>> answers = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            answers.add(backoff.recordFailure(ip));
            clock.advanceSeconds(1);
        }
        return answers;
    }

    @Test
    @DisplayName("the first five failures within 60 s get the normal refusal; the sixth gets 429 after 1 s")
    void threshold() {
        List<Optional<Duration>> answers = fail(IP, 6);

        assertThat(answers.subList(0, 5)).allMatch(Optional::isEmpty);
        assertThat(answers.get(5)).contains(Duration.ofSeconds(1));
    }

    @Test
    @DisplayName("each further failure doubles Retry-After: 1, 2, 4 … 256, then the 300 s cap")
    void doublingAndCap() {
        fail(IP, 5);
        List<Long> waits = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            waits.add(backoff.recordFailure(IP).orElseThrow().getSeconds());
        }

        assertThat(waits).containsExactly(1L, 2L, 4L, 8L, 16L, 32L, 64L, 128L, 256L, 300L, 300L, 300L);
    }

    @Test
    @DisplayName("failures spread wider than the 60 s window never start the backoff")
    void windowSlides() {
        for (int i = 0; i < 20; i++) {
            assertThat(backoff.recordFailure(IP)).isEmpty();
            clock.advanceSeconds(13);
        }
    }

    @Test
    @DisplayName("a caller honoring Retry-After still climbs to the cap (the backoff outlives the window)")
    void honoringRetryAfterStillClimbs() {
        fail(IP, 5);
        long last = 0;
        for (int i = 0; i < 10; i++) {
            Duration wait = backoff.recordFailure(IP).orElseThrow();
            last = wait.getSeconds();
            clock.advance(wait);
        }
        assertThat(last).isEqualTo(300);
    }

    @Test
    @DisplayName("fifteen quiet minutes forget the address")
    void forgottenAfterQuiet() {
        fail(IP, 7);
        clock.advance(AuthFailureBackoff.FORGET_AFTER);

        assertThat(fail(IP, 5)).allMatch(Optional::isEmpty);
    }

    @Test
    @DisplayName("a success resets the counter and the backoff level")
    void successResets() {
        fail(IP, 8);
        backoff.recordSuccess(IP);

        assertThat(fail(IP, 5)).allMatch(Optional::isEmpty);
        assertThat(backoff.recordFailure(IP)).contains(Duration.ofSeconds(1));
    }

    @Test
    @DisplayName("a valid credential is never blocked: success is reported, never refused, even mid-backoff")
    void validCredentialNeverBlocked() {
        fail(IP, 9);
        assertThat(backoff.activeCount()).isEqualTo(1);

        // The caller evaluated a correct credential: the API has no way to refuse it.
        backoff.recordSuccess(IP);

        assertThat(backoff.activeCount()).isZero();
        assertThat(backoff.trackedAddresses()).isZero();
    }

    @Test
    @DisplayName("addresses are independent")
    void perAddress() {
        fail(IP, 6);

        assertThat(backoff.recordFailure("198.51.100.1")).isEmpty();
    }

    @Test
    @DisplayName("foresignal.auth.failures counts every failure; the gauge counts addresses still in backoff")
    void metrics() {
        fail(IP, 6);
        backoff.recordFailure(IP); // 2 s

        assertThat(meters.get(AuthFailureBackoff.FAILURES_METER).counter().count()).isEqualTo(7.0);
        assertThat(meters.get(AuthFailureBackoff.ACTIVE_METER).gauge().value()).isEqualTo(1.0);
        clock.advanceSeconds(3);
        assertThat(meters.get(AuthFailureBackoff.ACTIVE_METER).gauge().value()).isZero();
    }

    @Test
    @DisplayName("disabled: failures are counted but never answered with 429")
    void disabled() {
        AuthFailureBackoff off = new AuthFailureBackoff(clock,
                new RateLimitProperties(true, false, 50_000, 10_000), meters);
        for (int i = 0; i < 10; i++) {
            assertThat(off.recordFailure(IP)).isEmpty();
        }
        assertThat(meters.get(AuthFailureBackoff.FAILURES_METER).counter().count()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("memory is bounded: past the cap, addresses not in backoff are dropped first")
    void bounded() {
        AuthFailureBackoff small = new AuthFailureBackoff(clock, new RateLimitProperties(true, true, 10, 3),
                new SimpleMeterRegistry());
        for (int i = 0; i < 6; i++) {
            small.recordFailure(IP);
        }
        small.recordFailure("10.0.0.1");
        small.recordFailure("10.0.0.2");
        small.recordFailure("10.0.0.3");

        assertThat(small.trackedAddresses()).isLessThanOrEqualTo(3);
        // The address in backoff survived the eviction.
        assertThat(small.recordFailure(IP)).isPresent();
    }

    @Test
    @DisplayName("Retry-After seconds round up and are at least 1")
    void seconds() {
        assertThat(AuthFailureBackoff.seconds(Duration.ofMillis(1))).isEqualTo(1);
        assertThat(AuthFailureBackoff.seconds(Duration.ofMillis(1500))).isEqualTo(2);
        assertThat(AuthFailureBackoff.seconds(Duration.ZERO)).isEqualTo(1);
        assertThat(AuthFailureBackoff.delay(40)).isEqualTo(AuthFailureBackoff.CAP);
    }

    @Test
    @DisplayName("a missing address is tracked as one 'unknown' client")
    void unknownAddress() {
        for (int i = 0; i < 5; i++) {
            backoff.recordFailure(null);
        }
        assertThat(backoff.recordFailure("")).isPresent();
    }
}
