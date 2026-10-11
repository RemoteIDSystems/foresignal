package ai.foresignal.collect;

/**
 * What a source costs in storage (OpenAPI {@code SourceStorage}, Core-Data-Model §15).
 *
 * @param observationsPerMinute observations received per minute over the last hour
 * @param versionsPerMinute versions persisted per minute over the last hour
 * @param persistRatio versions / observations over the last hour, 0..1
 * @param triggerEdgeShare share of the last hour's versions persisted as {@code TRIGGER_EDGE}, 0..1
 * @param bytesPerDay version bytes per day, averaged over up to 7 days
 * @param projectedBytes {@code bytesPerDay ×} the retention window in days
 * @param lateLastHour observations dropped as late in the last hour
 * @param clampedLastHour observations whose time was clamped in the last hour
 */
public record SourceStorage(
        double observationsPerMinute,
        double versionsPerMinute,
        double persistRatio,
        double triggerEdgeShare,
        long bytesPerDay,
        long projectedBytes,
        long lateLastHour,
        long clampedLastHour) {

    /** A source that has stored nothing. */
    public static final SourceStorage ZERO = new SourceStorage(0, 0, 0, 0, 0, 0, 0, 0);
}
