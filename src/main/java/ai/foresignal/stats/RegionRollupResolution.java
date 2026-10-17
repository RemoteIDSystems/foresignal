package ai.foresignal.stats;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * The region resolutions of {@code activity_rollup} (prompt 119, Intelligence-Design §4.2): 2 = region-hour,
 * 3 = region-day. Kept apart from {@link RollupResolution} so the H3 readers of prompt 038 never see region rows.
 */
public enum RegionRollupResolution {

    /** Region-hour buckets (retention {@code RetentionSettings.stats.regionHourly}, 1095 d). */
    HOUR((short) 2, Duration.ofHours(1), ChronoUnit.HOURS),

    /** Region-day buckets (UTC days, computed from raw rows; retention {@code stats.regionDaily}, 3650 d). */
    DAY((short) 3, Duration.ofDays(1), ChronoUnit.DAYS);

    private final short code;
    private final Duration width;
    private final ChronoUnit unit;

    RegionRollupResolution(short code, Duration width, ChronoUnit unit) {
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

    /** The region resolution with the same bucket width as an H3 resolution. */
    public static RegionRollupResolution of(RollupResolution resolution) {
        return resolution == RollupResolution.DAY ? DAY : HOUR;
    }

    /** The H3 resolution with the same bucket width (for {@link RollupCursor} arithmetic). */
    public RollupResolution asWidth() {
        return this == DAY ? RollupResolution.DAY : RollupResolution.HOUR;
    }
}
