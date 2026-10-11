package ai.foresignal.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.util.Map;

/**
 * How long Foresignal keeps what it collects (Architecture §11).
 *
 * <p>Durations are read and written in shorthand, so this serializes to
 * {@code {"default":"7d","alerts":"90d","usage":"30d","sources":{"usgs-earthquakes":"3650d"}}},
 * matching the {@code RetentionSettings} schema in the OpenAPI document.
 *
 * @param defaultRetention window applied to a source with no override
 * @param alerts how long fired alerts are kept
 * @param usage how long per-minute usage counters are kept
 * @param sources per-source overrides, keyed by source id
 */
public record RetentionSettings(
        @JsonProperty("default") Duration defaultRetention,
        Duration alerts,
        Duration usage,
        Map<String, Duration> sources) {

    public static final Duration DEFAULT_RETENTION = Duration.ofDays(7);
    public static final Duration DEFAULT_ALERTS = Duration.ofDays(90);
    public static final Duration DEFAULT_USAGE = Duration.ofDays(30);

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
    }

    /** The shipped defaults: 7d records, 90d alerts, 30d usage, no per-source overrides. */
    public static RetentionSettings defaults() {
        return new RetentionSettings(DEFAULT_RETENTION, DEFAULT_ALERTS, DEFAULT_USAGE, Map.of());
    }

    /** The retention window for {@code sourceId}: its override if set, otherwise the default. */
    public Duration forSource(String sourceId) {
        return sources.getOrDefault(sourceId, defaultRetention);
    }
}
