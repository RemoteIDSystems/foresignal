package ai.foresignal.exercise;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

/**
 * The pacing of an exercise replay (prompt 115): {@code realtime}, {@code x10}, {@code x60} or {@code max}.
 *
 * <p>Between two consecutive matches the replay sleeps the difference of their {@code ingested_at} divided by
 * the factor, capped at 60 s ÷ factor for any single sleep; {@code max} never sleeps.
 */
public enum ExerciseSpeed {
    /** The original pace. */
    REALTIME("realtime", 1),
    /** Ten times faster. */
    X10("x10", 10),
    /** Sixty times faster. */
    X60("x60", 60),
    /** As fast as the pipeline allows: no sleep. */
    MAX("max", 0);

    /** The wire values, in declaration order. */
    public static final List<String> WIRE_VALUES = List.of("realtime", "x10", "x60", "max");

    /** The longest single sleep at real time. */
    public static final Duration SINGLE_SLEEP_CAP = Duration.ofSeconds(60);

    private final String wire;
    private final int factor;

    ExerciseSpeed(String wire, int factor) {
        this.wire = wire;
        this.factor = factor;
    }

    /** The wire value. */
    public String wire() {
        return wire;
    }

    /** The speed-up factor; 0 for {@link #MAX}. */
    public int factor() {
        return factor;
    }

    /** The longest single sleep at this speed: 60 s ÷ factor; zero for {@link #MAX}. */
    public Duration cap() {
        return factor == 0 ? Duration.ZERO : SINGLE_SLEEP_CAP.dividedBy(factor);
    }

    /**
     * The sleep before a match ingested at {@code next} when the previous one was ingested at {@code previous}:
     * the scaled difference, never negative and never above {@link #cap()}; zero for the first match (null
     * {@code previous}), an unknown time, or {@link #MAX}.
     */
    public Duration pause(Instant previous, Instant next) {
        if (factor == 0 || previous == null || next == null || !next.isAfter(previous)) {
            return Duration.ZERO;
        }
        Duration scaled = Duration.between(previous, next).dividedBy(factor);
        Duration cap = cap();
        return scaled.compareTo(cap) > 0 ? cap : scaled;
    }

    /**
     * Parses a wire value (case-insensitive).
     *
     * @throws IllegalArgumentException for anything else
     */
    public static ExerciseSpeed fromWire(String value) {
        String lower = value == null ? "" : value.toLowerCase(Locale.ROOT);
        for (ExerciseSpeed speed : values()) {
            if (speed.wire.equals(lower)) {
                return speed;
            }
        }
        throw new IllegalArgumentException("must be one of realtime, x10, x60, max");
    }
}
