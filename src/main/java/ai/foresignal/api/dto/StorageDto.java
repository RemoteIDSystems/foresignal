package ai.foresignal.api.dto;

/**
 * The {@code SourceStorage} schema: what a source costs in storage (Core-Data-Model §15).
 *
 * @param observationsPerMinute observations per minute over the last hour
 * @param versionsPerMinute persisted versions per minute over the last hour
 * @param persistRatio versions divided by observations
 * @param triggerEdgeShare share of versions persisted as TRIGGER_EDGE over the last hour
 * @param bytesPerDay version bytes per day, seven-day average
 * @param projectedBytes {@code bytesPerDay} times the retention window
 * @param lateLastHour LATE observations in the last hour
 * @param clampedLastHour future-clamped observations in the last hour
 */
public record StorageDto(
        double observationsPerMinute,
        double versionsPerMinute,
        double persistRatio,
        double triggerEdgeShare,
        long bytesPerDay,
        long projectedBytes,
        long lateLastHour,
        long clampedLastHour) {}
