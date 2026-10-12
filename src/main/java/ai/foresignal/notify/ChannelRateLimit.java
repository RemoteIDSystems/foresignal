package ai.foresignal.notify;

import java.time.Duration;

/**
 * A destination's rate limit: at most {@code capacity} requests per {@code period}, per channel
 * (Architecture §17).
 *
 * @param capacity requests per period
 * @param period the refill period
 */
public record ChannelRateLimit(int capacity, Duration period) {

    /** Validates. */
    public ChannelRateLimit {
        if (capacity < 1 || period == null || period.isNegative() || period.isZero()) {
            throw new IllegalArgumentException("a rate limit needs a positive capacity and period");
        }
    }

    /** {@code n} per second. */
    public static ChannelRateLimit perSecond(int n) {
        return new ChannelRateLimit(n, Duration.ofSeconds(1));
    }

    /** {@code n} per minute. */
    public static ChannelRateLimit perMinute(int n) {
        return new ChannelRateLimit(n, Duration.ofMinutes(1));
    }
}
