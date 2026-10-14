package ai.foresignal.source.spi;

import java.time.Duration;
import java.util.Objects;
import java.util.function.DoubleSupplier;

/**
 * A streaming source's own reconnect ladder (prompt 082), used by the streaming supervisor instead of its global
 * ladder when the descriptor sets one: exponential from {@code initial}, capped at {@code max}, with a symmetric
 * random jitter.
 *
 * <p>The delay of attempt {@code n} (0-based) is {@code min(max, initial × 2^n) × (1 + jitter × u)} with {@code u}
 * uniform in [-1, 1], so it always lies in {@code [base × (1 − jitter), base × (1 + jitter)]}. APRS-IS asks clients to
 * back off with jitter so a server restart does not bring every client back in the same second.
 *
 * @param initial the first delay; positive
 * @param max the largest base delay; at least {@code initial}
 * @param jitter the relative jitter, 0 (none) to 0.5
 */
public record ReconnectPolicy(Duration initial, Duration max, double jitter) {

    /** Checks the bounds. */
    public ReconnectPolicy {
        Objects.requireNonNull(initial, "initial");
        Objects.requireNonNull(max, "max");
        if (initial.isNegative() || initial.isZero()) {
            throw new IllegalArgumentException("initial must be positive, but was " + initial);
        }
        if (max.compareTo(initial) < 0) {
            throw new IllegalArgumentException("max " + max + " is below initial " + initial);
        }
        if (!(jitter >= 0.0 && jitter <= 0.5)) {
            throw new IllegalArgumentException("jitter must be 0 to 0.5, but was " + jitter);
        }
    }

    /** An exponential ladder without jitter. */
    public static ReconnectPolicy exponential(Duration initial, Duration max) {
        return new ReconnectPolicy(initial, max, 0.0);
    }

    /** The delay before attempt {@code attempt} without jitter: {@code min(max, initial × 2^attempt)}. */
    public Duration base(int attempt) {
        int n = Math.max(0, Math.min(attempt, 40));
        long millis = initial.toMillis();
        long cap = max.toMillis();
        for (int i = 0; i < n && millis < cap; i++) {
            millis *= 2;
        }
        return Duration.ofMillis(Math.min(millis, cap));
    }

    /**
     * The delay before attempt {@code attempt}, jittered.
     *
     * @param attempt reconnects since the ladder last reset, from 0
     * @param uniform a source of values in [0, 1)
     */
    public Duration delay(int attempt, DoubleSupplier uniform) {
        long base = base(attempt).toMillis();
        if (jitter == 0.0) {
            return Duration.ofMillis(base);
        }
        double u = 2.0 * uniform.getAsDouble() - 1.0;
        long jittered = Math.round(base * (1.0 + jitter * Math.max(-1.0, Math.min(1.0, u))));
        return Duration.ofMillis(Math.max(1, jittered));
    }
}
