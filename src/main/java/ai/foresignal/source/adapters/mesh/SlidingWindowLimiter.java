package ai.foresignal.source.adapters.mesh;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A sliding-window counter (prompt 083): at most {@code limit} events in any {@code window}. Used for the public
 * broker's inbound cap (100 messages per minute, checked before parsing), the private broker's safety cap (1,000 per
 * minute) and the public broker's 12 connects per hour. Thread-safe.
 */
public final class SlidingWindowLimiter {

    private final int limit;
    private final Duration window;
    private final Deque<Instant> events = new ArrayDeque<>();

    /**
     * Creates a limiter.
     *
     * @param limit the most events in one window; positive
     * @param window the window; positive
     */
    public SlidingWindowLimiter(int limit, Duration window) {
        if (limit < 1 || window == null || window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("a limiter needs a positive limit and window");
        }
        this.limit = limit;
        this.window = window;
    }

    /** Records an event at {@code now} and returns true, or returns false (recording nothing) when the window is full. */
    public synchronized boolean tryAcquire(Instant now) {
        Instant cutoff = now.minus(window);
        while (!events.isEmpty() && !events.peekFirst().isAfter(cutoff)) {
            events.pollFirst();
        }
        if (events.size() >= limit) {
            return false;
        }
        events.addLast(now);
        return true;
    }

    /** The limit. */
    public int limit() {
        return limit;
    }

    /** The window. */
    public Duration window() {
        return window;
    }
}
