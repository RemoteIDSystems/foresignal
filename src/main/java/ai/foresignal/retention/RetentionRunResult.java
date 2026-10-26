package ai.foresignal.retention;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * The {@code RetentionRunResult} schema: what one run deleted (prompt 019).
 *
 * @param startedAt when the run started
 * @param durationMs how long it took
 * @param versionsDeleted STANDARD versions deleted (dropped TRACK leaves are counted in
 *     {@code partitionsDropped}, not here)
 * @param headsDeleted alias of {@code headsPurged}
 * @param alertsDeleted resolved or suppressed alerts deleted
 * @param usageRowsDeleted {@code source_usage} minutes deleted
 * @param partitionsDropped TRACK and STANDARD leaves dropped
 * @param headsPurged heads (with their live rows) purged by the writer
 * @param fetchLogDeleted {@code fetch_log} rows deleted
 * @param seqClockDeleted {@code seq_clock} minutes deleted
 * @param notificationsDeleted {@code notification} rows deleted
 * @param skippedSources sources skipped after a failed delete (no HeadPurge)
 * @param consumerClamp the consumer clamp of this run
 * @param perSource per source: versions deleted and the published floor
 * @param capWindowDays the cap-history window this run applied, in days (prompt R2-015), or null when history is not
 *     capped by age
 * @param dbCap what the database-size cap did this run (prompt R2-015), or null when it is off
 */
public record RetentionRunResult(
        Instant startedAt,
        long durationMs,
        long versionsDeleted,
        long headsDeleted,
        long alertsDeleted,
        long usageRowsDeleted,
        int partitionsDropped,
        long headsPurged,
        long fetchLogDeleted,
        long seqClockDeleted,
        long notificationsDeleted,
        List<String> skippedSources,
        ConsumerClamp consumerClamp,
        Map<String, PerSource> perSource,
        Integer capWindowDays,
        RetentionDbCap.Outcome dbCap) {

    public RetentionRunResult {
        skippedSources = skippedSources == null ? List.of() : List.copyOf(skippedSources);
        perSource = perSource == null ? Map.of() : java.util.Collections.unmodifiableMap(new java.util.TreeMap<>(perSource));
    }

    /** The shape before prompt R2-015: no cap history. */
    public RetentionRunResult(Instant startedAt, long durationMs, long versionsDeleted, long headsDeleted,
            long alertsDeleted, long usageRowsDeleted, int partitionsDropped, long headsPurged, long fetchLogDeleted,
            long seqClockDeleted, long notificationsDeleted, List<String> skippedSources, ConsumerClamp consumerClamp,
            Map<String, PerSource> perSource) {
        this(startedAt, durationMs, versionsDeleted, headsDeleted, alertsDeleted, usageRowsDeleted, partitionsDropped,
                headsPurged, fetchLogDeleted, seqClockDeleted, notificationsDeleted, skippedSources, consumerClamp,
                perSource, null, null);
    }

    /**
     * The consumer clamp.
     *
     * @param active true when a consumer held some cutoff back
     * @param consumer the consumer that held it back furthest, or null
     * @param cursorSeq that consumer's cursor, or null
     */
    public record ConsumerClamp(boolean active, String consumer, Long cursorSeq) {}

    /**
     * One source's figures.
     *
     * @param versionsDeleted versions deleted this run
     * @param floorSeq the floor published after the run, or null when retention has none for it
     */
    public record PerSource(long versionsDeleted, Long floorSeq) {}
}
