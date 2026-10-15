package ai.foresignal.settings;

import java.util.List;

/** Keys under which Foresignal stores its settings in {@code app_setting} (Architecture §7.1). */
public final class SettingKeys {

    /** {@link RetentionSettings}: how long records, alerts and usage are kept. */
    public static final String RETENTION = "retention";

    /** {@link CorsSettings}: which origins may read and write. */
    public static final String CORS = "cors";

    /** The durable {@code seq} cursor the trigger engine has evaluated up to. */
    public static final String TRIGGER_CURSOR = "trigger.cursor";

    /** {@link TriggerSettings}: trigger engine defaults such as the storm cap (prompt 017). */
    public static final String TRIGGERS = "triggers";

    /** Limits applied to SSE subscribers (buffer size, replay cap). */
    public static final String STREAM_LIMITS = "stream.limits";

    /** {@link SecuritySettings}: whether data reads require a token. */
    public static final String SECURITY = "security";

    /**
     * {@link ServerSettings}: the public base URL used in generated links (TileJSON from 016;
     * prompt 023 adds the rest of the setting and its endpoints).
     */
    public static final String SERVER = "server";

    /** {@link NotificationSettings}: the global notification pause (prompt 018). */
    public static final String NOTIFICATIONS = "notifications";

    /** {@link WatchdogSettings}: the off-box heartbeat and the ingest canary (prompt 019). */
    public static final String WATCHDOG = "watchdog";

    /** {@code ai.foresignal.llm.LlmSettings}: LLM roles and options (prompt 026). */
    public static final String LLM = "llm";

    /** The embedding index state: active and pending vector slots (prompt 027). */
    public static final String SEARCH_INDEX = "search.index";

    /** The embedding worker's durable cursor (prompt 027; the consumer clamp of retention). */
    public static final String EMBEDDING_CURSOR = "embedding.cursor";

    /** Per-client rate-limit classes ({@code RateLimitSettings}, prompt 035). */
    public static final String RATELIMITS = "ratelimits";

    /** Outbound URL policy overrides ({@code OutboundSettings}, prompt 035). */
    public static final String OUTBOUND = "outbound";

    /**
     * The activity rollup job's durable cursor {@code {lastHour, lastDay, seq}} (prompt 038; the consumer
     * clamp of retention).
     */
    public static final String STATS_ROLLUP_CURSOR = "stats.rollup.cursor";

    /** Stats options ({@code StatsSettings}, prompt 038): per-source {@code enabled} and {@code includePrivate}. */
    public static final String STATS = "stats";

    /** Glance options ({@code GlanceSettings}, prompt 038): {@code exposeCountsAnonymously}. */
    public static final String GLANCE = "glance";

    /** Metric projections served at {@code /v1/metrics} ({@code MetricSettings}, prompt 038). */
    public static final String METRICS = "metrics";

    /**
     * Operator visibility tightening ({@code VisibilitySettings}, prompt 039): the sources the operator
     * made private on top of their descriptors ({@code SourceSettings.private}).
     */
    public static final String VISIBILITY = "visibility";

    /** The region tag worker's durable cursor (prompt 040; the consumer clamp of retention). */
    public static final String REGION_CURSOR = "region.cursor";

    /** The entity linker's durable cursor (prompt 098; the consumer clamp of retention). */
    public static final String ENTITY_CURSOR = "entity.cursor";

    /**
     * The windowed-rule evaluator's cursor (prompt 100; the consumer clamp of retention). The
     * anomaly buckets are filled from retained history, so version retention must not outrun the
     * window state's watermark.
     */
    public static final String ANOMALY_CURSOR = "anomaly.cursor";

    /** {@code EntitySettings} (prompt 098): the callsign gap and the private-source opt-in. */
    public static final String ENTITY = "entity";

    /**
     * Region tagging modes per source ({@code RegionTaggingSettings}, prompt 040): {@code eager},
     * {@code lazy} or {@code off} ({@code SourceSettings.regionTagging}).
     */
    public static final String REGION_TAGGING = "regionTagging";

    /** This installation's generated identifier. */
    public static final String INSTALL_ID = "install.id";

    /**
     * The weather settings ({@code WeatherSettings}, prompt 056): {@code alertContext}, {@code defaultAoi} and
     * {@code aoiBufferKm}.
     */
    public static final String WEATHER = "weather";

    /**
     * Every key above. {@link AppSettingService#invalidateCache()} announces each of them, so a
     * component holding its own copy of a setting drops it too. A new key belongs in this list.
     */
    public static final List<String> ALL =
            List.of(RETENTION, CORS, TRIGGER_CURSOR, TRIGGERS, STREAM_LIMITS, SECURITY, SERVER, NOTIFICATIONS,
                    WATCHDOG, INSTALL_ID, LLM, SEARCH_INDEX, EMBEDDING_CURSOR, RATELIMITS, OUTBOUND, STATS_ROLLUP_CURSOR,
                    STATS, GLANCE, METRICS, VISIBILITY, REGION_CURSOR, REGION_TAGGING, WEATHER, ENTITY_CURSOR,
                    ENTITY);

    private SettingKeys() {}
}
