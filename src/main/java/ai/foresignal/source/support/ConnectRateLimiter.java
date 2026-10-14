package ai.foresignal.source.support;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * At most {@code limit} connects in any {@code window} (prompt 085): the hourly connect caps of the streaming sources
 * (12 an hour for {@code ais-kystverket} and {@code ais-digitraffic}), which the per-minute and per-day budget cannot
 * express. A sliding window over the attempts' times; time comes from the caller. Thread-safe.
 */
public final class ConnectRateLimiter {

    private final int limit;
    private final Duration window;
    private final Deque<Instant> attempts = new ArrayDeque<>();

    /**
     * Creates a limiter.
     *
     * @param limit the most attempts in one window; positive
     * @param window the window; positive
     */
    public ConnectRateLimiter(int limit, Duration window) {
        if (limit < 1 || window == null || window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("a limiter needs a positive limit and window");
        }
        this.limit = limit;
        this.window = window;
    }

    /** Records an attempt at {@code now} and returns true, or returns false (recording nothing) when the window is full. */
    public synchronized boolean tryAcquire(Instant now) {
        Instant cutoff = now.minus(window);
        while (!attempts.isEmpty() && !attempts.peekFirst().isAfter(cutoff)) {
            attempts.pollFirst();
        }
        if (attempts.size() >= limit) {
            return false;
        }
        attempts.addLast(now);
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
