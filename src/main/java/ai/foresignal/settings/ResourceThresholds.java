package ai.foresignal.settings;

import java.util.List;

/**
 * The host-resource and operational alert thresholds (prompt R2-014), stored under {@link SettingKeys#THRESHOLDS};
 * OpenAPI schema {@code ResourceThresholds}. A breach shows as a {@code /v1/system} warning and fires one alert, and
 * one recovery notice when it ends, through the channel system.
 *
 * @param disk free-space thresholds for the instance's data volumes; on by default (15 % warn, 5 % critical)
 * @param memory free-memory thresholds; off by default
 * @param alertChannels the channel ids an alert goes to; empty means the {@code system} subscribers and the personal
 *     channels of every active user who holds {@code audit.read}
 * @param sourceStorm when enough sources are failing at once to raise {@code system.source_storm}
 * @param collectorBacklog when collection is behind for long enough to raise {@code system.collector_backlog}
 */
public record ResourceThresholds(Threshold disk, Threshold memory, List<String> alertChannels,
        SourceStorm sourceStorm, CollectorBacklog collectorBacklog) {

    /** The default disk warning: below 15 % free. */
    public static final double DEFAULT_DISK_WARN_PERCENT = 15;

    /** The default disk critical level: below 5 % free. */
    public static final double DEFAULT_DISK_CRITICAL_PERCENT = 5;

    /** The default number of failing sources that makes a storm. */
    public static final int DEFAULT_STORM_SOURCES = 5;

    /** The default trigger lag, in sequence numbers, that counts as a backlog (the catch-up threshold). */
    public static final long DEFAULT_BACKLOG_LAG_SEQ = 10_000;

    /** The default time a backlog must last before it is raised. */
    public static final int DEFAULT_BACKLOG_SUSTAINED_SECONDS = 300;

    public ResourceThresholds {
        disk = disk == null ? Threshold.OFF : disk;
        memory = memory == null ? Threshold.OFF : memory;
        alertChannels = alertChannels == null ? List.of() : List.copyOf(alertChannels);
        sourceStorm = sourceStorm == null ? new SourceStorm(null) : sourceStorm;
        collectorBacklog = collectorBacklog == null
                ? new CollectorBacklog(null, null, DEFAULT_BACKLOG_SUSTAINED_SECONDS) : collectorBacklog;
    }

    /** Disk at 15 % / 5 % free, memory off, a storm at 5 failing sources, a backlog of 10,000 seq for 5 minutes. */
    public static ResourceThresholds defaults() {
        return new ResourceThresholds(new Threshold(DEFAULT_DISK_WARN_PERCENT, null, DEFAULT_DISK_CRITICAL_PERCENT, null),
                Threshold.OFF, List.of(), new SourceStorm(DEFAULT_STORM_SOURCES),
                new CollectorBacklog(DEFAULT_BACKLOG_LAG_SEQ, null, DEFAULT_BACKLOG_SUSTAINED_SECONDS));
    }

    /**
     * Warn and critical levels for one resource; a level is reached when the free share drops below its percent
     * <em>or</em> the free amount drops below its bytes. Null members are off.
     *
     * @param warnFreePercent warn below this percent free
     * @param warnFreeBytes warn below this many bytes free
     * @param criticalFreePercent critical below this percent free
     * @param criticalFreeBytes critical below this many bytes free
     */
    public record Threshold(Double warnFreePercent, Long warnFreeBytes, Double criticalFreePercent,
            Long criticalFreeBytes) {

        /** Every level off. */
        public static final Threshold OFF = new Threshold(null, null, null, null);

        /** True when no level is set. */
        public boolean off() {
            return warnFreePercent == null && warnFreeBytes == null && criticalFreePercent == null
                    && criticalFreeBytes == null;
        }

        /**
         * The level a reading is at.
         *
         * @param freeBytes free bytes, or null when unknown
         * @param freePercent free percent, or null when unknown
         * @return {@code ok}, {@code warn} or {@code critical}
         */
        public String level(Long freeBytes, Double freePercent) {
            if (below(freeBytes, criticalFreeBytes) || below(freePercent, criticalFreePercent)) {
                return "critical";
            }
            if (below(freeBytes, warnFreeBytes) || below(freePercent, warnFreePercent)) {
                return "warn";
            }
            return "ok";
        }

        private static boolean below(Number value, Number floor) {
            return value != null && floor != null && value.doubleValue() < floor.doubleValue();
        }
    }

    /**
     * The source-failure storm threshold.
     *
     * @param failedSources raise when at least this many sources are failing at once; null is off
     */
    public record SourceStorm(Integer failedSources) {}

    /**
     * The collector backlog threshold: a backlog is the trigger lag or the number of fetches waiting for a pull
     * permit staying above its bound for {@code sustainedSeconds}.
     *
     * @param triggerLagSeq the trigger lag bound, in sequence numbers; null is off
     * @param waitingPulls the waiting-fetch bound; null is off
     * @param sustainedSeconds how long the bound must be exceeded
     */
    public record CollectorBacklog(Long triggerLagSeq, Integer waitingPulls, Integer sustainedSeconds) {

        public CollectorBacklog {
            sustainedSeconds = sustainedSeconds == null ? DEFAULT_BACKLOG_SUSTAINED_SECONDS : sustainedSeconds;
        }
    }
}
