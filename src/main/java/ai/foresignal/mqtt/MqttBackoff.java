package ai.foresignal.mqtt;

import java.time.Duration;

/**
 * The reconnect schedule (prompt 060): exponential from 1 s, doubling per failed attempt, capped at
 * 120 s, with ±20 % jitter so many clients do not reconnect in step after a broker restart.
 */
public final class MqttBackoff {

    /** The first delay. */
    public static final Duration INITIAL = Duration.ofSeconds(1);

    /** The longest delay. */
    public static final Duration MAX = Duration.ofSeconds(120);

    /** The jitter, as a fraction of the delay either way. */
    public static final double JITTER = 0.2;

    private MqttBackoff() {}

    /** The delay before attempt {@code attempt} (1 for the first reconnect) without jitter. */
    public static Duration base(int attempt) {
        int exponent = Math.max(0, Math.min(attempt - 1, 16));
        long millis = INITIAL.toMillis() << exponent;
        return Duration.ofMillis(Math.min(millis, MAX.toMillis()));
    }

    /**
     * The delay before attempt {@code attempt} with jitter.
     *
     * @param random a uniform value in [0, 1)
     * @return {@link #base} × (0.8 … 1.2), never above {@link #MAX}
     */
    public static Duration delay(int attempt, double random) {
        double factor = 1 - JITTER + 2 * JITTER * Math.max(0, Math.min(random, 1));
        long millis = Math.round(base(attempt).toMillis() * factor);
        return Duration.ofMillis(Math.min(millis, MAX.toMillis()));
    }
}
