package ai.foresignal.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.util.Map;
import java.util.TreeMap;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

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
 * @param stats how long activity rollups are kept (prompt 038): hourly rows 35 d, daily rows 1825 d
 * @param trackRawWindow TRACK history older than this is downsampled (prompt 044; default 24 h). The
 *     API's {@code null} disables downsampling; it is held as {@link Duration#ZERO} here so that "disabled"
 *     survives the round trip through {@code app_setting}, where an absent member means "default"
 *     ({@link RawWindowDeserializer}). Use {@link #downsamplingEnabled()} rather than testing the value.
 * @param trackDownsampleInterval one sample per entity per this interval is kept in a downsampled leaf
 *     (prompt 044; default 5 min); a TRACK source may override it ({@code SourceSettings.downsampleInterval})
 * @param entityLinks how long closed entity links are kept (prompt 098; default 365 d)
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
        Duration inbox,
        Stats stats,
        @JsonSerialize(using = RetentionSettings.RawWindowSerializer.class)
        @JsonDeserialize(using = RetentionSettings.RawWindowDeserializer.class)
        Duration trackRawWindow,
        Duration trackDownsampleInterval,
        Duration entityLinks) {

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
    public static final Duration DEFAULT_TRACK_RAW_WINDOW = Duration.ofHours(24);
    public static final Duration DEFAULT_TRACK_DOWNSAMPLE_INTERVAL = Duration.ofMinutes(5);
    public static final Duration DEFAULT_ENTITY_LINKS = Duration.ofDays(365);

    /** The {@code trackRawWindow} value that means "downsampling disabled" (the API's {@code null}). */
    public static final Duration DOWNSAMPLING_DISABLED = Duration.ZERO;

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
        stats = stats == null ? Stats.defaults() : stats;
        trackRawWindow = trackRawWindow == null ? DEFAULT_TRACK_RAW_WINDOW : trackRawWindow;
        trackDownsampleInterval =
                trackDownsampleInterval == null ? DEFAULT_TRACK_DOWNSAMPLE_INTERVAL : trackDownsampleInterval;
        entityLinks = entityLinks == null ? DEFAULT_ENTITY_LINKS : entityLinks;
    }

    /**
     * The 038 shape: every member except {@code trackRawWindow} and {@code trackDownsampleInterval},
     * which take their defaults.
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
     * @param inbox see the canonical constructor
     * @param stats see the canonical constructor
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
            Duration audit,
            Duration inbox,
            Stats stats) {
        this(defaultRetention, alerts, usage, sources, trackWindow, provenance, notifications, briefings, ask, kinds,
                audit, inbox, stats, null, null, null);
    }

    /** True unless {@code trackRawWindow} was set to {@code null} (prompt 044). */
    @JsonIgnore
    public boolean downsamplingEnabled() {
        return trackRawWindow != null && !trackRawWindow.isZero();
    }

    /**
     * Retention of the activity rollups (prompt 038, {@code RetentionSettings.stats}). Prompt 119 adds
     * the region resolutions ({@code regionHourly}, {@code regionDaily}); until then those members are
     * accepted by the API and not stored.
     *
     * @param hourly how long hourly rows (resolution 0) are kept, by {@code bucket_start}
     * @param daily how long daily rows (resolution 1) are kept
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Stats(Duration hourly, Duration daily, Duration regionHourly, Duration regionDaily) {

        public static final Duration DEFAULT_HOURLY = Duration.ofDays(35);
        public static final Duration DEFAULT_DAILY = Duration.ofDays(1825);

        /** The default window of region-hour rollups (prompt 119). */
        public static final Duration DEFAULT_REGION_HOURLY = Duration.ofDays(1095);

        /** The default window of region-day rollups (prompt 119). */
        public static final Duration DEFAULT_REGION_DAILY = Duration.ofDays(3650);

        /** Fills missing windows with their defaults. */
        public Stats {
            hourly = hourly == null ? DEFAULT_HOURLY : hourly;
            daily = daily == null ? DEFAULT_DAILY : daily;
            regionHourly = regionHourly == null ? DEFAULT_REGION_HOURLY : regionHourly;
            regionDaily = regionDaily == null ? DEFAULT_REGION_DAILY : regionDaily;
        }

        /** The prompt 038 shape: the H3 windows, region windows at their defaults. */
        public Stats(Duration hourly, Duration daily) {
            this(hourly, daily, null, null);
        }

        /** The shipped defaults. */
        public static Stats defaults() {
            return new Stats(null, null, null, null);
        }
    }

    /**
     * The 033 shape: every member except {@code stats}, which takes its default.
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
     * @param inbox see the canonical constructor
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
            Duration audit,
            Duration inbox) {
        this(defaultRetention, alerts, usage, sources, trackWindow, provenance, notifications, briefings, ask, kinds,
                audit, inbox, null);
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
                audit, null, null);
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
                null, null, null);
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
        return new RetentionSettings(
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
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
                notifications, briefings, ask, kinds, audit, inbox, stats, trackRawWindow, trackDownsampleInterval,
                entityLinks);
    }

    /**
     * Writes {@code trackRawWindow}: {@code null} when downsampling is disabled, else shorthand.
     */
    public static final class RawWindowSerializer extends ValueSerializer<Duration> {

        @Override
        public void serialize(Duration value, JsonGenerator gen, SerializationContext ctx) {
            if (value == null || value.isZero()) {
                gen.writeNull();
            } else {
                gen.writeString(Durations.format(value));
            }
        }
    }

    /**
     * Reads {@code trackRawWindow}: an explicit {@code null} is "disabled"
     * ({@link #DOWNSAMPLING_DISABLED}); an absent member stays null, so the canonical constructor
     * fills the 24 h default (settings stored before prompt 044).
     */
    public static final class RawWindowDeserializer extends ValueDeserializer<Duration> {

        @Override
        public Duration deserialize(JsonParser parser, DeserializationContext ctx) {
            if (parser.currentToken() == JsonToken.VALUE_NUMBER_INT) {
                return Duration.ofSeconds(Math.max(0, parser.getLongValue()));
            }
            return Durations.parse(parser.getString());
        }

        /** An explicit JSON {@code null}: downsampling disabled. */
        @Override
        public Duration getNullValue(DeserializationContext ctx) {
            return DOWNSAMPLING_DISABLED;
        }

        /** An absent member: null, which the record turns into the default. */
        @Override
        public Duration getAbsentValue(DeserializationContext ctx) {
            return null;
        }
    }
}
