package ai.foresignal.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.util.Map;
import java.util.TreeMap;

/**
 * How long Foresignal keeps what it collects (Core-Data-Model §13, prompt 019), stored under
 * {@link SettingKeys#RETENTION}.
 *
 * <p>Durations are read and written in shorthand, so this serializes to
 * {@code {"default":"7d","alerts":"90d","usage":"30d","sources":{},"trackWindow":"3d",...}},
 * matching the {@code RetentionSettings} schema in the OpenAPI document.
 *
 * <p>{@code sources} is a <em>view</em> over {@code source_settings.retention}: the retention API
 * writes per-source overrides there and reads them back from there, so one store holds them. The
 * copy in {@code app_setting} is kept empty by {@code RetentionSettingsService}; a value stored
 * before prompt 019 is still read by {@link #forSource(String)}.
 *
 * @param defaultRetention window of a STANDARD source with no other window (step 5 of the
 *     resolution order)
 * @param alerts how long resolved and suppressed alerts are kept (by {@code fired_at})
 * @param usage how long per-minute usage counters are kept
 * @param sources per-source overrides, keyed by source id (view over {@code source_settings})
 * @param trackWindow the single retention window of the TRACK tier (Core-Data-Model §0, §13)
 * @param provenance how long {@code fetch_log} rows that wrote versions are kept
 * @param notifications how long {@code notification} rows are kept
 * @param briefings how long briefing runs are kept (prompt 029 adds its step)
 * @param ask how long Ask threads are kept (prompt 028 adds its step)
 * @param kinds per-kind windows of STANDARD sources, first in the resolution order
 * @param audit how long {@code audit_event} rows are kept (prompt 023)
 * @param inbox how long notifications stay in the inbox (prompt 033; default 30 d), separate from
 *     {@code notifications}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RetentionSettings(
        @JsonProperty("default") Duration defaultRetention,
        Duration alerts,
        Duration usage,
        Map<String, Duration> sources,
        Duration trackWindow,
        Duration provenance,
        Duration notifications,
        Duration briefings,
        Duration ask,
        Map<String, Duration> kinds,
        Duration audit,
        Duration inbox) {

    public static final Duration DEFAULT_RETENTION = Duration.ofDays(7);
    public static final Duration DEFAULT_ALERTS = Duration.ofDays(90);
    public static final Duration DEFAULT_USAGE = Duration.ofDays(30);
    public static final Duration DEFAULT_TRACK_WINDOW = Duration.ofDays(3);
    public static final Duration DEFAULT_PROVENANCE = Duration.ofDays(30);
    public static final Duration DEFAULT_NOTIFICATIONS = Duration.ofDays(90);
    public static final Duration DEFAULT_BRIEFINGS = Duration.ofDays(180);
    public static final Duration DEFAULT_ASK = Duration.ofDays(90);
    public static final Duration DEFAULT_AUDIT = Duration.ofDays(90);
    public static final Duration DEFAULT_INBOX = Duration.ofDays(30);

    /** Fills every missing window with its default and copies the maps (sorted, for stable output). */
    public RetentionSettings {
        defaultRetention = defaultRetention == null ? DEFAULT_RETENTION : defaultRetention;
        alerts = alerts == null ? DEFAULT_ALERTS : alerts;
        usage = usage == null ? DEFAULT_USAGE : usage;
        sources = sources == null ? Map.of() : java.util.Collections.unmodifiableMap(new TreeMap<>(sources));
        trackWindow = trackWindow == null ? DEFAULT_TRACK_WINDOW : trackWindow;
        provenance = provenance == null ? DEFAULT_PROVENANCE : provenance;
        notifications = notifications == null ? DEFAULT_NOTIFICATIONS : notifications;
        briefings = briefings == null ? DEFAULT_BRIEFINGS : briefings;
        ask = ask == null ? DEFAULT_ASK : ask;
        kinds = kinds == null ? Map.of() : java.util.Collections.unmodifiableMap(new TreeMap<>(kinds));
        audit = audit == null ? DEFAULT_AUDIT : audit;
        inbox = inbox == null ? DEFAULT_INBOX : inbox;
    }

    /**
     * The 023 shape: every member except {@code inbox}, which takes its default.
     *
     * @param defaultRetention see the canonical constructor
     * @param alerts see the canonical constructor
     * @param usage see the canonical constructor
     * @param sources see the canonical constructor
     * @param trackWindow see the canonical constructor
     * @param provenance see the canonical constructor
     * @param notifications see the canonical constructor
     * @param briefings see the canonical constructor
     * @param ask see the canonical constructor
     * @param kinds see the canonical constructor
     * @param audit see the canonical constructor
     */
    public RetentionSettings(
            Duration defaultRetention,
            Duration alerts,
            Duration usage,
            Map<String, Duration> sources,
            Duration trackWindow,
            Duration provenance,
            Duration notifications,
            Duration briefings,
            Duration ask,
            Map<String, Duration> kinds,
            Duration audit) {
        this(defaultRetention, alerts, usage, sources, trackWindow, provenance, notifications, briefings, ask, kinds,
                audit, null);
    }

    /**
     * The 019 shape: every member except {@code audit}, which takes its default.
     *
     * @param defaultRetention see the canonical constructor
     * @param alerts see the canonical constructor
     * @param usage see the canonical constructor
     * @param sources see the canonical constructor
     * @param trackWindow see the canonical constructor
     * @param provenance see the canonical constructor
     * @param notifications see the canonical constructor
     * @param briefings see the canonical constructor
     * @param ask see the canonical constructor
     * @param kinds see the canonical constructor
     */
    public RetentionSettings(
            Duration defaultRetention,
            Duration alerts,
            Duration usage,
            Map<String, Duration> sources,
            Duration trackWindow,
            Duration provenance,
            Duration notifications,
            Duration briefings,
            Duration ask,
            Map<String, Duration> kinds) {
        this(defaultRetention, alerts, usage, sources, trackWindow, provenance, notifications, briefings, ask, kinds,
                null, null);
    }

    /**
     * The pre-019 shape: the five original members, every later window at its default.
     *
     * @param defaultRetention see the canonical constructor
     * @param alerts see the canonical constructor
     * @param usage see the canonical constructor
     * @param sources see the canonical constructor
     * @param trackWindow see the canonical constructor
     */
    public RetentionSettings(
            Duration defaultRetention,
            Duration alerts,
            Duration usage,
            Map<String, Duration> sources,
            Duration trackWindow) {
        this(defaultRetention, alerts, usage, sources, trackWindow, null, null, null, null, null);
    }

    /** The shipped defaults: every window at its default, no per-source or per-kind overrides. */
    public static RetentionSettings defaults() {
        return new RetentionSettings(null, null, null, null, null, null, null, null, null, null, null, null);
    }

    /**
     * The window of {@code sourceId} from this value alone: its entry in {@link #sources()}, else
     * {@link #defaultRetention()}. The full resolution order (kinds, descriptor defaults) is
     * {@code RetentionResolver}'s.
     */
    public Duration forSource(String sourceId) {
        return sources.getOrDefault(sourceId, defaultRetention);
    }

    /** This value with {@code sources} replaced. */
    public RetentionSettings withSources(Map<String, Duration> newSources) {
        return new RetentionSettings(defaultRetention, alerts, usage, newSources, trackWindow, provenance,
                notifications, briefings, ask, kinds, audit, inbox);
    }
}
