package ai.foresignal.security.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.test.MutableClock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/** {@link ClientRateLimiter} (prompt 035): buckets, Retry-After, overrides, stream slots, bounds, metrics. */
class ClientRateLimiterTest {

    MutableClock clock;
    SimpleMeterRegistry meters;
    RateLimitSettingsService settings;
    ClientRateLimiter limiter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock();
        meters = new SimpleMeterRegistry();
        settings = mock(RateLimitSettingsService.class);
        when(settings.get()).thenReturn(new RateLimitSettings(3, 0, null, 2, 0, 0, null));
        limiter = new ClientRateLimiter(settings, RateLimitProperties.defaults(), clock, meters);
    }

    @Test
    @DisplayName("n per minute: n requests pass, the next is refused with Retry-After 60/n s, and refills on the clock")
    void perMinute() {
        for (int i = 0; i < 3; i++) {
            assertThat(limiter.tryConsume("ip:1", RateClass.READ).allowed()).isTrue();
        }
        ClientRateLimiter.Decision refused = limiter.tryConsume("ip:1", RateClass.READ);

        assertThat(refused.allowed()).isFalse();
        assertThat(refused.rateClass()).isEqualTo(RateClass.READ);
        assertThat(refused.retryAfterSeconds()).isEqualTo(20);
        clock.advanceSeconds(19);
        assertThat(limiter.tryConsume("ip:1", RateClass.READ).allowed()).isFalse();
        clock.advanceSeconds(1);
        assertThat(limiter.tryConsume("ip:1", RateClass.READ).allowed()).isTrue();
    }

    @Test
    @DisplayName("clients and classes have separate buckets")
    void separateBuckets() {
        for (int i = 0; i < 3; i++) {
            limiter.tryConsume("ip:1", RateClass.READ);
        }
        assertThat(limiter.tryConsume("ip:2", RateClass.READ).allowed()).isTrue();
        assertThat(limiter.tryConsume("ip:1", RateClass.EXPENSIVE).allowed()).isTrue();
    }

    @Test
    @DisplayName("an explicit limit (a token's maxRequestsPerMinute) replaces the class limit")
    void explicitLimit() {
        assertThat(limiter.tryConsume("token:a", RateClass.READ, 1).allowed()).isTrue();
        ClientRateLimiter.Decision refused = limiter.tryConsume("token:a", RateClass.READ, 1);

        assertThat(refused.allowed()).isFalse();
        assertThat(refused.retryAfterSeconds()).isEqualTo(60);
        assertThat(limiter.tryConsume("token:a", RateClass.READ, 0).allowed()).isTrue();
    }

    @Test
    @DisplayName("a changed setting applies at once: the bucket is keyed by its limit")
    void settingChange() {
        for (int i = 0; i < 3; i++) {
            limiter.tryConsume("ip:1", RateClass.READ);
        }
        when(settings.get()).thenReturn(new RateLimitSettings(10, 0, null, 0, 0, 0, null));

        assertThat(limiter.tryConsume("ip:1", RateClass.READ).allowed()).isTrue();
    }

    @Test
    @DisplayName("METRICS without metricsPerMinute is never limited")
    void metricsUnlimitedByDefault() {
        for (int i = 0; i < 100; i++) {
            assertThat(limiter.tryConsume("ip:1", RateClass.METRICS).allowed()).isTrue();
        }
        assertThat(limiter.trackedBuckets()).isZero();
    }

    @Test
    @DisplayName("concurrent stream slots: a client gets n, the next is refused, a release frees one exactly once")
    void streamSlots() {
        ClientRateLimiter.StreamSlot a = limiter.tryOpenStream("ip:1", 2);
        ClientRateLimiter.StreamSlot b = limiter.tryOpenStream("ip:1", 2);

        assertThat(a).isNotNull();
        assertThat(b).isNotNull();
        assertThat(limiter.tryOpenStream("ip:1", 2)).isNull();
        assertThat(limiter.tryOpenStream("ip:2", 2)).isNotNull();
        a.release();
        a.release();
        assertThat(limiter.openStreams("ip:1")).isEqualTo(1);
        assertThat(limiter.tryOpenStream("ip:1", 2)).isNotNull();
        assertThat(limiter.concurrentStreamsRefused().retryAfterSeconds()).isPositive();
    }

    @Test
    @DisplayName("slots at zero are removed, so the map holds only clients with open streams")
    void slotsRemovedAtZero() {
        ClientRateLimiter.StreamSlot slot = limiter.tryOpenStream("ip:1", 1);
        slot.release();

        assertThat(limiter.openStreams("ip:1")).isZero();
    }

    @Test
    @DisplayName("refusals increment foresignal.ratelimit.rejected with the class tag")
    void rejectedMetric() {
        for (int i = 0; i < 5; i++) {
            limiter.tryConsume("ip:1", RateClass.EXPENSIVE);
        }
        limiter.tryOpenStream("ip:1", 1);
        limiter.tryOpenStream("ip:1", 1);

        assertThat(meters.get(ClientRateLimiter.REJECTED_METER).tag("class", "expensive").counter().count())
                .isEqualTo(3.0);
        assertThat(meters.get(ClientRateLimiter.REJECTED_METER).tag("class", "stream").counter().count())
                .isEqualTo(1.0);
        assertThat(meters.get(ClientRateLimiter.REJECTED_METER).tag("class", "read").counter().count()).isZero();
    }

    @Test
    @DisplayName("disabled: everything is allowed and nothing is tracked")
    void disabled() {
        ClientRateLimiter off = new ClientRateLimiter(settings, new RateLimitProperties(false, true, 10, 10), clock,
                new SimpleMeterRegistry());
        for (int i = 0; i < 10; i++) {
            assertThat(off.tryConsume("ip:1", RateClass.READ).allowed()).isTrue();
            assertThat(off.tryOpenStream("ip:1", 1)).isNotNull();
        }
        assertThat(off.trackedBuckets()).isZero();
        assertThat(off.enabled()).isFalse();
    }

    @Test
    @DisplayName("idle buckets (full again after a minute) are swept; past the cap the map never grows")
    void bounded() {
        ClientRateLimiter small = new ClientRateLimiter(settings, new RateLimitProperties(true, true, 5, 10), clock,
                new SimpleMeterRegistry());
        for (int i = 0; i < 5; i++) {
            small.tryConsume("ip:" + i, RateClass.READ);
        }
        assertThat(small.trackedBuckets()).isEqualTo(5);
        clock.advance(ClientRateLimiter.IDLE);
        small.tryConsume("ip:new", RateClass.READ);
        assertThat(small.trackedBuckets()).isEqualTo(1);

        for (int i = 0; i < 20; i++) {
            small.tryConsume("ip:busy" + i, RateClass.READ);
        }
        assertThat(small.trackedBuckets()).isLessThanOrEqualTo(5);
    }

    @Test
    @DisplayName("reset forgets buckets and slots")
    void reset() {
        limiter.tryConsume("ip:1", RateClass.READ);
        limiter.tryOpenStream("ip:1", 3);
        limiter.reset();

        assertThat(limiter.trackedBuckets()).isZero();
        assertThat(limiter.openStreams("ip:1")).isZero();
    }

    @Test
    @DisplayName("client keys: principal, else accepted token, else ip:<remote address>")
    void clientKeys() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("198.51.100.4");

        assertThat(ClientRateLimiter.clientKey(request)).isEqualTo("ip:198.51.100.4");
        assertThat(ClientRateLimiter.ipKey(null)).isEqualTo("ip:unknown");
        assertThat(ClientRateLimiter.ipKey(" ")).isEqualTo("ip:unknown");
    }

    @Test
    @DisplayName("seconds round up and are at least 1")
    void seconds() {
        assertThat(ClientRateLimiter.seconds(1)).isEqualTo(1);
        assertThat(ClientRateLimiter.seconds(0)).isEqualTo(1);
        assertThat(ClientRateLimiter.seconds(2_000_000_001L)).isEqualTo(3);
    }
}
