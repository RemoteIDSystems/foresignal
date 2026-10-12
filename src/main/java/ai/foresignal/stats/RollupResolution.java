package ai.foresignal.stats;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * The resolutions of {@code activity_rollup} (prompt 038). Prompt 119 adds region resolutions 2
 * (region-hour) and 3 (region-day) with a nullable {@code region_code}; nothing here assumes only two.
 */
public enum RollupResolution {

    /** Hourly buckets. */
    HOUR((short) 0, Duration.ofHours(1), ChronoUnit.HOURS),

    /** Daily buckets (UTC days), computed from raw rows: distinct entities are not additive. */
    DAY((short) 1, Duration.ofDays(1), ChronoUnit.DAYS);

    private final short code;
    private final Duration width;
    private final ChronoUnit unit;

    RollupResolution(short code, Duration width, ChronoUnit unit) {
        this.code = code;
        this.width = width;
        this.unit = unit;
    }

    /** The {@code resolution} column value. */
    public short code() {
        return code;
    }

    /** The bucket width. */
    public Duration width() {
        return width;
    }

    /** The bucket containing {@code t}. */
    public Instant floor(Instant t) {
        return t.truncatedTo(unit);
    }

    /** The first bucket start at or after {@code t}. */
    public Instant ceil(Instant t) {
        Instant f = floor(t);
        return f.equals(t) ? f : f.plus(width);
    }
}
