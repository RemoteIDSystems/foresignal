package ai.foresignal.stats;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

/**
 * The rollup job's durable cursor, {@code app_setting['stats.rollup.cursor']} (prompt 038).
 *
 * <p>Written in the same transaction as the rollup rows it describes, so a crash can neither lose
 * nor double-count a bucket.
 *
 * @param lastHour the start of the last hour rolled up, or null before the first
 * @param lastDay the start of the last UTC day rolled up, or null before the first
 * @param seq every seq at or below this has been rolled into both its hour and its day; retention's
 *     consumer clamp holds raw rows above it
 * @param since the first hour the job rolled up (its backfill start); buckets before it have no rows,
 *     so readers serve them from raw history
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RollupCursor(Instant lastHour, Instant lastDay, long seq, Instant since) {

    /** The cursor of the prompt's shape, without {@code since}. */
    public RollupCursor(Instant lastHour, Instant lastDay, long seq) {
        this(lastHour, lastDay, seq, null);
    }

    /**
     * The first bucket start of {@code resolution} that has rows: {@code since} (its day for days), or
     * the epoch when unknown.
     */
    public Instant start(RollupResolution resolution) {
        if (since == null) {
            return Instant.EPOCH;
        }
        return resolution.floor(since);
    }

    /** The end of the rolled-up hours (exclusive), or null before the first. */
    public Instant hoursEnd() {
        return lastHour == null ? null : lastHour.plus(RollupResolution.HOUR.width());
    }

    /** The end of the rolled-up days (exclusive), or null before the first. */
    public Instant daysEnd() {
        return lastDay == null ? null : lastDay.plus(RollupResolution.DAY.width());
    }

    /** The end (exclusive) of what {@code resolution} has rolled up, or null. */
    public Instant end(RollupResolution resolution) {
        return resolution == RollupResolution.DAY ? daysEnd() : hoursEnd();
    }
}
