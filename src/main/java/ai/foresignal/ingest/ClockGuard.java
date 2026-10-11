package ai.foresignal.ingest;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * The writer's clock rules (Core-Data-Model §6.1, invariant I3). Pure.
 *
 * <ul>
 *   <li><strong>Behind:</strong> when {@code now < lastIngestedAt − tolerance}, the writer holds
 *       every batch rather than write an {@code ingested_at} far in the past of what is stored.
 *   <li><strong>Jump:</strong> per batch, the wall-clock delta since the previous batch is compared
 *       with the monotonic ({@code System.nanoTime}) delta; a divergence above the tolerance is a
 *       clock jump, counted and warned about but never blocking.
 *   <li><strong>Clamp:</strong> {@code ingestedAt = max(now, lastIngestedAt)}, so a small backward
 *       step is absorbed and {@code ingested_at} never decreases with seq.
 * </ul>
 *
 * @param tolerance the allowed skew
 */
public record ClockGuard(Duration tolerance) {

    /** The direction of a detected clock jump. */
    public enum Direction {
        /** The wall clock moved further than monotonic time. */
        FORWARD,
        /** The wall clock moved less than monotonic time, or backwards. */
        BACKWARD;

        /** The metric tag value. */
        public String tag() {
            return name().toLowerCase();
        }
    }

    /** True when {@code now} is more than the tolerance behind {@code lastIngestedAt}. */
    public boolean isBehind(Instant now, Instant lastIngestedAt) {
        return lastIngestedAt != null && now.isBefore(lastIngestedAt.minus(tolerance));
    }

    /**
     * The jump between two batches, if wall and monotonic time diverge by more than the tolerance.
     *
     * @param wallDelta wall-clock time between the batches
     * @param monotonicNanos {@code System.nanoTime} difference between the batches
     */
    public Optional<Direction> jump(Duration wallDelta, long monotonicNanos) {
        Duration divergence = wallDelta.minus(Duration.ofNanos(monotonicNanos));
        if (divergence.abs().compareTo(tolerance) <= 0) {
            return Optional.empty();
        }
        return Optional.of(divergence.isNegative() ? Direction.BACKWARD : Direction.FORWARD);
    }

    /** {@code max(now, lastIngestedAt)}. */
    public static Instant ingestedAt(Instant now, Instant lastIngestedAt) {
        return lastIngestedAt != null && now.isBefore(lastIngestedAt) ? lastIngestedAt : now;
    }
}
