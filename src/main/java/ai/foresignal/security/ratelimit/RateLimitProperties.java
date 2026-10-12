package ai.foresignal.security.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Deployment switches of the per-client rate limits and the auth-failure backoff
 * ({@code foresignal.ratelimit.*}, prompt 035). The limits themselves are the operator setting
 * {@code app_setting['ratelimits']} ({@link RateLimitSettings}).
 *
 * @param enabled whether {@link ClientRateLimitFilter} and {@link ClientRateLimiter} enforce
 *     anything; the test profile turns it off so the many requests of other integration tests never
 *     meet a 429, and the rate-limit tests turn it back on
 * @param authBackoffEnabled whether {@link AuthFailureBackoff} answers 429 after repeated failures;
 *     off in the test profile for the same reason (failures are still counted in the metric)
 * @param maxTrackedClients the most token buckets kept in memory before idle ones are evicted
 * @param maxTrackedAddresses the most client IPs the auth-failure backoff tracks at once
 */
@ConfigurationProperties("foresignal.ratelimit")
public record RateLimitProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("true") boolean authBackoffEnabled,
        @DefaultValue("50000") int maxTrackedClients,
        @DefaultValue("10000") int maxTrackedAddresses) {

    /** Replaces non-positive capacities with the defaults. */
    public RateLimitProperties {
        maxTrackedClients = maxTrackedClients > 0 ? maxTrackedClients : 50_000;
        maxTrackedAddresses = maxTrackedAddresses > 0 ? maxTrackedAddresses : 10_000;
    }

    /** Everything on, default capacities (unit tests). */
    public static RateLimitProperties defaults() {
        return new RateLimitProperties(true, true, 50_000, 10_000);
    }
}
