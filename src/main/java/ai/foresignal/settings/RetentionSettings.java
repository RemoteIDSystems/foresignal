package ai.foresignal.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.util.Map;

/**
 * How long Foresignal keeps what it collects (Architecture §11).
 *
 * <p>Durations are read and written in shorthand, so this serializes to
 * {@code {"default":"7d","alerts":"90d","usage":"30d","sources":{"usgs-earthquakes":"3650d"},
 * "trackWindow":"3d"}},
 * matching the {@code RetentionSettings} schema in the OpenAPI document.
 *
 * @param defaultRetention window applied to a source with no override
 * @param alerts how long fired alerts are kept
 * @param usage how long per-minute usage counters are kept
 * @param sources per-source overrides, keyed by source id
 * @param trackWindow the single retention window of the TRACK tier (Core-Data-Model §0, §13);
 *     prompt 019 extends retention settings further
 */
public record RetentionSettings(
        @JsonProperty("default") Duration defaultRetention,
        Duration alerts,
        Duration usage,
        Map<String, Duration> sources,
        Duration trackWindow) {

    public static final Duration DEFAULT_RETENTION = Duration.ofDays(7);
    public static final Duration DEFAULT_ALERTS = Duration.ofDays(90);
    public static final Duration DEFAULT_USAGE = Duration.ofDays(30);
    public static final Duration DEFAULT_TRACK_WINDOW = Duration.ofDays(3);

    public RetentionSettings {
        if (defaultRetention == null) {
            defaultRetention = DEFAULT_RETENTION;
        }
        if (alerts == null) {
            alerts = DEFAULT_ALERTS;
        }
        if (usage == null) {
            usage = DEFAULT_USAGE;
        }
        sources = sources == null ? Map.of() : Map.copyOf(sources);
        if (trackWindow == null) {
            trackWindow = DEFAULT_TRACK_WINDOW;
        }
    }

    /**
     * The shipped defaults: 7d records, 90d alerts, 30d usage, no per-source overrides and a 3d
     * TRACK window.
     */
    public static RetentionSettings defaults() {
        return new RetentionSettings(
                DEFAULT_RETENTION, DEFAULT_ALERTS, DEFAULT_USAGE, Map.of(), DEFAULT_TRACK_WINDOW);
    }

    /** The retention window for {@code sourceId}: its override if set, otherwise the default. */
    public Duration forSource(String sourceId) {
        return sources.getOrDefault(sourceId, defaultRetention);
    }
}
