package ai.foresignal.collect;

import io.github.bucket4j.TimeMeter;
import java.time.Clock;

/**
 * Drives Bucket4j from Foresignal's injected {@link Clock}.
 *
 * <p>Bucket4j defaults to {@code System.nanoTime()}, which no test can move. Routing it through the
 * application clock is what lets a test assert that the daily bucket actually refills at UTC
 * midnight rather than waiting a day to find out.
 *
 * <p>{@link #isWallClockBased()} must be true: the daily bandwidth uses
 * {@code refillIntervallyAligned}, which needs a clock whose origin is the epoch rather than an
 * arbitrary monotonic reading.
 */
public class ClockTimeMeter implements TimeMeter {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private final Clock clock;

    public ClockTimeMeter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public long currentTimeNanos() {
        var now = clock.instant();
        return now.getEpochSecond() * NANOS_PER_SECOND + now.getNano();
    }

    @Override
    public boolean isWallClockBased() {
        return true;
    }
}
