package ai.foresignal.exercise;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Emits replayed matches in order with the pacing of an {@link ExerciseSpeed} (prompt 115). Cancellation is
 * checked before every match, so a cancel takes effect between matches, never in the middle of one.
 */
public final class ReplayPacer {

    /** Sleeps; the production implementation is {@link Thread#sleep(Duration)}, tests advance a clock. */
    @FunctionalInterface
    public interface Sleeper {

        /** Sleeps {@code duration} (never called with zero). */
        void sleep(Duration duration) throws InterruptedException;
    }

    /** The real sleeper. */
    public static final Sleeper THREAD_SLEEP = Thread::sleep;

    /**
     * One item to emit.
     *
     * @param ingestedAt when the item's version was ingested, or null when unknown
     * @param item the item
     * @param <T> the item type
     */
    public record Paced<T>(Instant ingestedAt, T item) {}

    /**
     * What a pacing pass did.
     *
     * @param emitted items handed to the consumer
     * @param slept the total time slept
     * @param canceled true when the pass stopped on a cancellation
     */
    public record Outcome(int emitted, Duration slept, boolean canceled) {}

    private final ExerciseSpeed speed;
    private final Sleeper sleeper;

    /** Creates a pacer. */
    public ReplayPacer(ExerciseSpeed speed, Sleeper sleeper) {
        this.speed = Objects.requireNonNull(speed, "speed");
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
    }

    /**
     * Emits {@code items} in order, sleeping between them as the speed says and checking {@code canceled}
     * before each item. An interrupted sleep counts as a cancellation.
     *
     * @param items the items, in seq order
     * @param canceled true once the run should stop
     * @param consumer receives each item
     * @param <T> the item type
     */
    public <T> Outcome run(List<Paced<T>> items, BooleanSupplier canceled, Consumer<T> consumer) {
        Instant previous = null;
        Duration slept = Duration.ZERO;
        int emitted = 0;
        for (Paced<T> paced : items) {
            if (canceled.getAsBoolean()) {
                return new Outcome(emitted, slept, true);
            }
            Duration pause = speed.pause(previous, paced.ingestedAt());
            if (!pause.isZero()) {
                try {
                    sleeper.sleep(pause);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return new Outcome(emitted, slept, true);
                }
                slept = slept.plus(pause);
                if (canceled.getAsBoolean()) {
                    return new Outcome(emitted, slept, true);
                }
            }
            consumer.accept(paced.item());
            emitted++;
            if (paced.ingestedAt() != null) {
                previous = paced.ingestedAt();
            }
        }
        return new Outcome(emitted, slept, false);
    }
}
