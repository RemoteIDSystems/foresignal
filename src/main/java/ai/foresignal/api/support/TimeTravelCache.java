package ai.foresignal.api.support;

import java.time.Duration;
import java.time.Instant;

/**
 * The caching rule of time-travel responses (prompt 037): asOf tiles, and tracks keyed on
 * {@code to}.
 *
 * <p>A past answer is immutable only when all of these hold:
 *
 * <ul>
 *   <li>{@code t ≤ now − 2 min}, so the minute bucket of {@code seq_clock} is closed;
 *   <li>history at {@code t} is settled: {@code t < HistoryFidelity.finalBefore(tier)} or the range
 *       is already downsampled (prompt 044);
 *   <li>{@code t} is at least 1 h after the retention horizon.
 * </ul>
 *
 * <p>Then the response is {@code {public|private}, max-age=31536000, immutable} with a strong ETag;
 * otherwise {@code {public|private}, max-age=300} with an ETag. {@code public} only when
 * {@code openReads} covers the path and no private source is involved. Chrome ignores
 * {@code immutable} but honors the year-long {@code max-age}.
 */
public final class TimeTravelCache {

    /** {@code max-age} of an immutable answer: one year. */
    public static final Duration IMMUTABLE_MAX_AGE = Duration.ofSeconds(31_536_000);

    /** {@code max-age} of an answer that may still change. */
    public static final Duration MUTABLE_MAX_AGE = Duration.ofSeconds(300);

    /** How long after {@code t} its {@code seq_clock} minute is certainly closed. */
    public static final Duration CLOSED_AFTER = Duration.ofMinutes(2);

    /** How far past the retention horizon an answer must be to count as stable. */
    public static final Duration HORIZON_GRACE = Duration.ofHours(1);

    private TimeTravelCache() {}

    /**
     * True when the answer for {@code t} can never change again.
     *
     * @param t the requested instant ({@code asOf}, or a track's {@code to})
     * @param now the current time
     * @param horizon the retention horizon of the involved sources, or null when none
     * @param settled true when no involved tier at {@code t} is pending downsampling
     */
    public static boolean immutable(Instant t, Instant now, Instant horizon, boolean settled) {
        return !t.isAfter(now.minus(CLOSED_AFTER))
                && settled
                && (horizon == null || !t.isBefore(horizon.plus(HORIZON_GRACE)));
    }

    /** The {@code Cache-Control} value for an immutable or a mutable answer. */
    public static String cacheControl(boolean immutable, boolean publicCache) {
        String visibility = publicCache ? "public" : "private";
        return immutable
                ? visibility + ", max-age=" + IMMUTABLE_MAX_AGE.toSeconds() + ", immutable"
                : visibility + ", max-age=" + MUTABLE_MAX_AGE.toSeconds();
    }
}
