package ai.foresignal.stream.sse;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * A {@link Clock} whose {@link #millis()} comes from {@link System#nanoTime()}, for the stream
 * schedule only (live drains and heartbeats, prompt 022).
 *
 * <p>The schedule measures intervals, so it must not follow the wall clock: a backwards NTP step of
 * an hour would otherwise stop every heartbeat and live drain for that hour, and a frozen
 * application clock (the tests' {@code MutableClock}) would stop them for good, which left a stream
 * with no event without even its response headers. The instants it reports start at the wall-clock
 * time of construction and then advance monotonically; they are never used as timestamps.
 */
final class MonotonicClock extends Clock {

    private final long originNanos = System.nanoTime();
    private final long originMillis = System.currentTimeMillis();

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public long millis() {
        return originMillis + (System.nanoTime() - originNanos) / 1_000_000L;
    }

    @Override
    public Instant instant() {
        return Instant.ofEpochMilli(millis());
    }
}
