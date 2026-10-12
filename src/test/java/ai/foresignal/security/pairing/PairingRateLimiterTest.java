package ai.foresignal.security.pairing;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.MutableClock;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The pairing claim limits (prompt 023). */
class PairingRateLimiterTest {

    private final MutableClock clock = new MutableClock();
    private final PairingRateLimiter limiter = new PairingRateLimiter(clock);

    @Test
    @DisplayName("five per minute per IP; refills after a minute")
    void perIp() {
        for (int i = 0; i < 5; i++) {
            assertThat(limiter.attempt("10.0.0.1").allowed()).isTrue();
        }
        PairingRateLimiter.Decision refused = limiter.attempt("10.0.0.1");
        assertThat(refused.allowed()).isFalse();
        assertThat(refused.retryAfterSeconds()).isBetween(1L, 60L);
        assertThat(limiter.attempt("10.0.0.2").allowed()).isTrue();
        clock.advance(Duration.ofMinutes(1));
        assertThat(limiter.attempt("10.0.0.1").allowed()).isTrue();
    }

    @Test
    @DisplayName("fifty per hour across IPs; reset clears")
    void global() {
        for (int i = 0; i < 50; i++) {
            assertThat(limiter.attempt("10.1.0." + i).allowed()).isTrue();
        }
        assertThat(limiter.attempt("10.2.0.1").allowed()).isFalse();
        limiter.reset();
        assertThat(limiter.attempt("10.2.0.1").allowed()).isTrue();
        assertThat(limiter.attempt(null).allowed()).isTrue();
    }
}
