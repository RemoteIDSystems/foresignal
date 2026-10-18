package ai.foresignal.cli.sse;

import java.time.Duration;
import java.util.random.RandomGenerator;

/**
 * Jittered exponential reconnect backoff: 1 s, 2 s, 4 s … capped at 60 s, each delay multiplied by a random factor
 * in [0.8, 1.2) (and still capped), so that many clients dropped together do not reconnect together. A server
 * {@code retry:} or {@code Retry-After} acts as a floor.
 */
public final class Backoff {

    /** The first delay. */
    public static final Duration INITIAL = Duration.ofSeconds(1);

    /** The largest delay. */
    public static final Duration MAX = Duration.ofSeconds(60);

    private final Duration initial;
    private final Duration max;
    private final RandomGenerator random;
    private int attempts;

    /**
     * Creates the default 1 s → 60 s backoff.
     *
     * @param random the jitter source
     */
    public Backoff(RandomGenerator random) {
        this(INITIAL, MAX, random);
    }

    /**
     * Creates a backoff.
     *
     * @param initial the first delay
     * @param max     the cap
     * @param random  the jitter source
     */
    public Backoff(Duration initial, Duration max, RandomGenerator random) {
        this.initial = initial;
        this.max = max;
        this.random = random;
    }

    /**
     * The next delay; each call doubles the base.
     *
     * @param floor a server-requested minimum ({@code retry:} or {@code Retry-After}), or null
     * @return the delay to wait before reconnecting
     */
    public Duration next(Duration floor) {
        long base = initial.toMillis() << Math.min(attempts, 20);
        base = Math.min(base, max.toMillis());
        attempts++;
        double factor = 0.8 + 0.4 * random.nextDouble();
        long delay = Math.min(max.toMillis(), Math.round(base * factor));
        if (floor != null && floor.toMillis() > delay) {
            delay = floor.toMillis();
        }
        return Duration.ofMillis(delay);
    }

    /** Starts again from the first delay (after a connection that delivered events). */
    public void reset() {
        attempts = 0;
    }

    /**
     * How many delays were handed out since the last reset.
     *
     * @return the count of consecutive failed attempts
     */
    public int attempts() {
        return attempts;
    }
}
