package ai.foresignal.source.adapters.gdelt;

import java.time.Duration;
import java.util.function.LongSupplier;

/**
 * A minimum spacing between the starts of two requests of one source (prompt 117: the GDELT DOC API allows one
 * request every 5 seconds). {@link #await} sleeps until the spacing since the previous start has passed, then marks
 * a new start. Thread-safe; time is a monotonic nanosecond clock (replaceable in tests, with the sleeper).
 */
public final class RequestSpacing {

    /** Sleeps for a number of nanoseconds; replaceable in tests. */
    @FunctionalInterface
    public interface Sleeper {

        /**
         * Sleeps.
         *
         * @param nanos how long, at least 1
         * @throws InterruptedException when the thread is interrupted
         */
        void sleep(long nanos) throws InterruptedException;
    }

    private final long spacingNanos;
    private final LongSupplier nanoTime;
    private final Sleeper sleeper;
    private long lastStart;
    private boolean started;

    /**
     * Creates a spacing.
     *
     * @param spacing the minimum time between two request starts
     * @param nanoTime the monotonic clock
     * @param sleeper how to wait
     */
    public RequestSpacing(Duration spacing, LongSupplier nanoTime, Sleeper sleeper) {
        this.spacingNanos = spacing.toNanos();
        this.nanoTime = nanoTime;
        this.sleeper = sleeper;
    }

    /** A spacing on the system's monotonic clock. */
    public static RequestSpacing system(Duration spacing) {
        return new RequestSpacing(spacing, System::nanoTime, nanos -> Thread.sleep(nanos / 1_000_000L,
                (int) (nanos % 1_000_000L)));
    }

    /**
     * Waits until a request may start, then marks it started.
     *
     * @throws InterruptedException when interrupted while waiting
     */
    public synchronized void await() throws InterruptedException {
        if (started) {
            long wait = lastStart + spacingNanos - nanoTime.getAsLong();
            if (wait > 0) {
                sleeper.sleep(wait);
            }
        }
        lastStart = nanoTime.getAsLong();
        started = true;
    }
}
