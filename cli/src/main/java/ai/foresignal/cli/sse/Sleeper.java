package ai.foresignal.cli.sse;

import java.time.Duration;

/**
 * How the CLI waits (reconnect backoff, polling). Tests substitute one that records the delays and returns at once.
 */
@FunctionalInterface
public interface Sleeper {

    /** Sleeps on the current thread. */
    Sleeper SYSTEM = d -> {
        if (!d.isNegative() && !d.isZero()) {
            Thread.sleep(d.toMillis());
        }
    };

    /**
     * Waits for a duration.
     *
     * @param duration how long
     * @throws InterruptedException when interrupted
     */
    void sleep(Duration duration) throws InterruptedException;
}
