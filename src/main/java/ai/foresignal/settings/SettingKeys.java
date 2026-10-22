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

    /** The collector's global pull concurrency ({@code CollectorSettings}, prompt 150). */
    public static final String COLLECTOR = "collector";

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

    /** The intelligence correlation engine's durable cursor (prompt 101). */
    public static final String INTEL_CURSOR = "intel.cursor";

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
     * The cold archive settings ({@code ArchiveSettings}, prompt 111): {@code enabled}, the source
     * allowlist, the local root, the S3 target and its (write-only) secrets.
     */
    public static final String ARCHIVE = "archive";

    /**
     * Every key above. {@link AppSettingService#invalidateCache()} announces each of them, so a
     * component holding its own copy of a setting drops it too. A new key belongs in this list.
     */
    /**
     * The CAP {@code sender} of Foresignal's own CAP messages (prompt 112), a string; default
     * {@code foresignal@{installId}}.
     */
    public static final String CAP_SENDER = "cap.sender";

    /**
     * The instance's display name (prompt 112: CAP {@code senderName} "Foresignal ({name})" and calendar
     * names), a string; default the install id.
     */
    public static final String INSTANCE_NAME = "instance.name";

    /**
     * The instance's display name in federation (prompt 113: {@code FederationInfo.instanceName}), a string;
     * default {@link #INSTANCE_NAME}'s value, else none.
     */
    public static final String FEDERATION_INSTANCE_NAME = "federation.instanceName";

    /**
     * How long pattern-of-life stays and places are kept (prompt 114), a duration string such as {@code 180d};
     * default 180 days.
     */
    public static final String POL_RETENTION = "pol.retention";

    /** The longest exercise replay window (prompt 115), a duration string such as {@code 7d}; default 7 days. */
    public static final String EXERCISE_MAX_WINDOW = "exercise.maxWindow";

    /** How long exercise alerts are kept (prompt 115), a duration string such as {@code 7d}; default 7 days. */
    public static final String EXERCISE_ALERT_RETENTION = "exercise.alertRetention";

    /** The evidence resolver's durable cursor (prompt 116; the consumer clamp of retention). */
    public static final String EVIDENCE_CURSOR = "evidence.cursor";

    /**
     * Operator extensions of the originator registry (prompt 116): an object shaped like
     * {@code intel/originators.json} ({@code originators}, {@code sources}, {@code patterns}, {@code upstreamRefs}).
     */
    public static final String INTEL_ORIGINATORS = "intel.originators";

    /**
     * The corroboration policy (prompt 154): the tunable "promote to trusted" gates — event evidence-quality
     * thresholds, situation activation, the cross-modality requirement, and automatic corroboration of lone
     * events — as the {@code IntelCorroboration} document ({@link ai.foresignal.intel.IntelCorroborationService}).
     */
    public static final String INTEL_CORROBORATION = "intel.corroboration";

    /**
     * True once the five example attention topics were created (prompt 117), so an operator who deletes them does
     * not see them come back on the next start.
     */
    public static final String ATTENTION_SEEDED = "attention.seeded";

    /** The lowest link confidence the evidence resolver links at (prompt 116), a number; default 0.5. */
    public static final String EVIDENCE_MIN_LINK_CONFIDENCE = "evidence.minLinkConfidence";

    /**
     * The detection probability an absence conclusion needs (prompt 116, {@code absenceMinDetect}), a number;
     * default 0.8.
     */
    public static final String ABSENCE_MIN_DETECT = "observability.absenceMinDetect";

    /** The situation linker's durable cursor (prompt 118; the consumer clamp of retention). */
    public static final String SITUATION_CURSOR = "situation.cursor";

    /** The linker's join threshold (prompt 118, {@code linkThreshold}), a number in [0, 1]; default 0.45. */
    public static final String SITUATION_LINK_THRESHOLD = "situation.linkThreshold";

    /**
     * How long a situation stays a linker candidate and active without new core members (prompt 118,
     * {@code dormancyWindow}), a duration string such as {@code 7d}; default 7 days.
     */
    public static final String SITUATION_DORMANCY_WINDOW = "situation.dormancyWindow";

    /** How long a dormant situation can still be reopened (prompt 118, {@code reopenWindow}); default 30 days. */
    public static final String SITUATION_REOPEN_WINDOW = "situation.reopenWindow";

    /** True to let the optional LLM brief refresh itself (prompt 118); default false. */
    public static final String SITUATION_BRIEFS_ENABLED = "situation.briefs.enabled";

    /** The most LLM briefs per UTC day across all situations (prompt 118); default 50. */
    public static final String SITUATION_BRIEFS_MAX_PER_DAY = "situation.briefs.maxPerDay";

    /** The shortest interval between two briefs of one situation (prompt 118, {@code briefMinInterval}); 30 min. */
    public static final String SITUATION_BRIEF_MIN_INTERVAL = "situation.briefMinInterval";

    /**
     * Operator rows of the situation theme table (prompt 118), an object shaped like {@code intel/themes.json}
     * ({@code eventTypes}, {@code findingTypes}, {@code tags}, {@code kinds}, {@code keywords}), merged over it.
     */
    public static final String INTEL_THEMES = "intel.themes";

    /** True to classify untyped text records with the optional LLM when no keyword rule matches (118); false. */
    public static final String INTEL_THEMES_LLM_CLASSIFY = "intel.themes.llmClassify";


    /**
     * Operator rows of the signal-family registry (prompt 119), an object shaped like {@code intel/signal-families.json}
     * ({@code families}, {@code rules}); its rules are tried before the bundled ones.
     */
    public static final String INTEL_SIGNAL_FAMILIES = "intel.signalFamilies";

    /** The region rollup job's durable cursor (prompt 119; the consumer clamp of retention). */
    public static final String STATS_REGION_CURSOR = "stats.region.cursor";

    /**
     * Per-source region level of the region rollups (prompt 119, {@code statsRegionLevel}): an object
     * {@code {sourceId: "admin1"}}; sources not named roll up by country.
     */
    public static final String STATS_REGION_LEVEL = "stats.regionLevel";

    /** The most auto-registered analysis series (prompt 119, {@code analysis.series.max}); default 2000. */
    public static final String ANALYSIS_SERIES_MAX = "analysis.series.max";

    /** True to publish {@code info} findings as {@code foresignal-analytics} records too (prompt 119); false. */
    public static final String ANALYSIS_FINDINGS_PUBLISH_INFO = "analysis.findings.publishInfo";

    /** The durable seq cursor of the live event-study scan (prompt 119; the consumer clamp of retention). */
    public static final String ANALYSIS_LIVE_CURSOR = "analysis.live.cursor";

    /** Pending pre-event re-checks of lagged weekly series (prompt 119), a list of objects. */
    public static final String ANALYSIS_LIVE_RECHECKS = "analysis.live.rechecks";

    /** The most tests one precursor mining run makes (prompt 120, {@code analysis.precursors.maxTestsPerRun}); 500. */
    public static final String ANALYSIS_PRECURSORS_MAX_TESTS = "analysis.precursors.maxTestsPerRun";

    /** The days of history precursor mining splits into discovery and validation (prompt 120); 365. */
    public static final String ANALYSIS_PRECURSORS_LOOKBACK_DAYS = "analysis.precursors.lookbackDays";

    /** The share of the range precursor mining uses for discovery, oldest first (prompt 120); 2/3. */
    public static final String ANALYSIS_PRECURSORS_DISCOVERY_FRACTION = "analysis.precursors.discoveryFraction";

    /** The durable seq cursor of the precursor live watch (prompt 120; the consumer clamp of retention). */
    public static final String PRECURSOR_CURSOR = "precursor.cursor";

    /**
     * The precursor live watch's id cursors over the finding log, alerts and anomaly observations (prompt 120), an
     * object {@code {findings, alerts, anomalies}}.
     */
    public static final String PRECURSOR_STREAM_CURSORS = "precursor.cursor.streams";

    /**
     * The Monday after the last week the own-history fold wrote to {@code reference_history} (prompt 121), an ISO
     * date; the next fold re-folds four weeks before it.
     */
    public static final String BASERATE_FOLD_WEEK = "baserate.fold.lastWeek";

    /**
     * The ensemble's extremizing state from the last refit (prompt 122): {@code a}, {@code fittedA}, {@code enabled},
     * {@code nResolutions}, {@code distinctForecasterTypes}, {@code logScoreImprovement} and {@code fittedAt}.
     */
    public static final String FORECAST_ENSEMBLE = "forecast.ensemble";

    /**
     * Operator overrides of the benchmark thresholds (prompt 123), an object shaped like
     * {@code intel/benchmark-thresholds.json} ({@code suite}, {@code components}, {@code gating}) merged over it.
     */
    public static final String BENCHMARK_THRESHOLDS = "benchmark.thresholds";

    /**
     * The series whose statistical forecasts are opened as ledger questions (prompt 126), an array of series
     * references; absent: the PortWatch Bab el-Mandeb, Hormuz and Suez transits and COT WTI managed-money net.
     */
    public static final String ANALYSIS_FORECASTS_QUESTION_SERIES = "analysis.forecasts.questionSeries";

    /** The durable seq cursor of the impact projector (prompt 127; the consumer clamp of retention). */
    public static final String IMPACT_CURSOR = "impact.cursor";

    /** Projections below this probability are dropped (prompt 127, {@code impact.minProbability}); 0.05. */
    public static final String IMPACT_MIN_PROBABILITY = "impact.minProbability";

    /**
     * Projections at or above this probability open ledger questions (prompt 127,
     * {@code impact.questionMinProbability}); 0.10.
     */
    public static final String IMPACT_QUESTION_MIN_PROBABILITY = "impact.questionMinProbability";

    /** The buffer in kilometers around an asset for published probability polygons (prompt 127); 5. */
    public static final String IMPACT_ASSET_BUFFER_KM = "impact.assetBufferKm";

    /** The durable seq cursor of the relation projector (prompt 129; the consumer clamp of retention). */
    public static final String RELATION_CURSOR = "relation.cursor";

    /** The default size cap of a new chronicle, in GB (prompt 131, {@code chronicle.defaultCapGb}); 20. */
    public static final String CHRONICLE_DEFAULT_CAP_GB = "chronicle.defaultCapGb";

    /** How many proposed chronicles the analysis cycles may activate on their own per UTC day (prompt 131); 2. */
    public static final String CHRONICLE_AUTO_ACTIVATE_PER_DAY = "chronicle.autoActivatePerDay";

    /** The size cap of a chronicle the analysis cycles activate on their own, in GB (prompt 131); 5. */
    public static final String CHRONICLE_AUTO_ACTIVATE_CAP_GB = "chronicle.autoActivateCapGb";

    /**
     * The chronicle thread's poll positions (prompt 131): the last {@code hypothesis_log} id and the last forecast
     * resolution time read for band-change and resolution items.
     */
    public static final String CHRONICLE_THREAD_CURSORS = "chronicle.threadCursors";

    /** The largest exchange package built or accepted, in MB (prompt 132, {@code exchange.maxPackageMb}); 200. */
    public static final String EXCHANGE_MAX_PACKAGE_MB = "exchange.maxPackageMb";

    /**
     * Whether a report labeled TLP GREEN may go out as a read-only share link (prompt 132,
     * {@code exchange.allowGreenLinks}); false, so only CLEAR reports get links by default.
     */
    public static final String EXCHANGE_ALLOW_GREEN_LINKS = "exchange.allowGreenLinks";

    /**
     * How a news outlet's factuality consensus maps to a 109 source reliability grade for {@code news-article} evidence
     * (prompt 133, {@code media.reliabilityMap}): a JSON object from factuality band ({@code "1"}…{@code "5"},
     * {@code "unrated"}) to grade ({@code A}…{@code F}); default 5 → A, 4 → B, 3 → C, 2 → D, 1 → E, unrated → F.
     */
    public static final String MEDIA_RELIABILITY_MAP = "media.reliabilityMap";

    /**
     * The reliability number of each Admiralty grade the event scorer takes for a news member (prompt 157,
     * {@code media.reliabilityNumbers}): a JSON object from grade ({@code "A"}…{@code "E"}) to a 0–1 number;
     * default A → 0.9, B → 0.75, C → 0.6, D → 0.45, E → 0.3 (F has none, so the scorer's default applies).
     */
    public static final String MEDIA_RELIABILITY_NUMBERS = "media.reliabilityNumbers";

    /**
     * The coverage skew and blindspot thresholds (prompt 157, {@code news.coverageThresholds}): a JSON object
     * with {@code skewShare} and {@code blindspotShare} (0–1) and {@code skewMinGroups} and
     * {@code blindspotMinRated} (integers); default skewShare 0.75, skewMinGroups 3, blindspotShare 0.75,
     * blindspotMinRated 8.
     */
    public static final String NEWS_COVERAGE_THRESHOLDS = "news.coverageThresholds";

    /**
     * The minimum estimated shared keys of a chronicle link (prompt 134, {@code network.minSharedKeys}); 5. A link
     * also needs a containment of the smaller chronicle of at least 0.05.
     */
    public static final String NETWORK_MIN_SHARED_KEYS = "network.minSharedKeys";

    /**
     * Whether federated chronicle discovery is on (prompt 134, {@code network.federated}); false. Network salts can
     * be installed and peer sketches received only while it is true.
     */
    public static final String NETWORK_FEDERATED = "network.federated";

    /**
     * The languages social posts are detected in and kept as separate volume series (prompt 135,
     * {@code social.languages}): a JSON array of BCP 47 primary subtags; default
     * {@code en, ar, he, fa, ru, uk, fr, es, de, tr, zh}.
     */
    public static final String SOCIAL_LANGUAGES = "social.languages";

    /**
     * The trailing-baseline posts per hour a region needs before social silence there means anything (prompt 135,
     * {@code social.minRegionBaseline}); 20. Below it social observability answers {@code LOW_SOCIAL_COVERAGE}.
     */
    public static final String SOCIAL_MIN_REGION_BASELINE = "social.minRegionBaseline";

    /**
     * The {@code social-velocity} finding thresholds (prompt 135, {@code social.velocity}): a JSON object
     * {@code {minRatio: 5, minUniqueAuthors: 20, dedupHours: 6}}.
     */
    public static final String SOCIAL_VELOCITY = "social.velocity";

    /**
     * Whether fingerprinted post images are kept (prompt 136, {@code social.keepMedia}); false. When true the image is
     * stored content-addressed in the 043 media store under its post's retention; otherwise only the PDQ hash stays.
     */
    public static final String SOCIAL_KEEP_MEDIA = "social.keepMedia";

    /**
     * The posts a narrative needs before the optional LLM claim extraction runs on it (prompt 136,
     * {@code social.llmClaimMinPosts}); 50. A narrative with five or more evidence groups qualifies whatever its size.
     */
    public static final String SOCIAL_LLM_CLAIM_MIN_POSTS = "social.llmClaimMinPosts";

    /** The LLM claim extractions run per UTC day (prompt 136, {@code social.llmClaimsPerDay}); 20. */
    public static final String SOCIAL_LLM_CLAIMS_PER_DAY = "social.llmClaimsPerDay";

    /**
     * Operator overrides of claim source reliability (prompt 136, {@code social.sourceReliability}): a JSON object from
     * {@code account:{platform}:{authorKey}} or {@code channel:{platform}:{channel}} to an Admiralty letter A–F.
     */
    public static final String SOCIAL_SOURCE_RELIABILITY = "social.sourceReliability";

    /**
     * The social–physical comparison settings (prompt 137, {@code social.compare}): a JSON object
     * {@code {minGroups: 3, defaultWindow: {before: "6h", after: "24h"}, familyWindows: {family: {before, after,
     * toleranceKm, publicationLag}}}} merged over {@code intel/social/social-compare.json}.
     */
    public static final String SOCIAL_COMPARE = "social.compare";

    /**
     * The attention-gap settings (prompt 137, {@code social.attentionGap}): {@code {minSeverity: "significant",
     * percentile: 10, lookbackDays: 365, windowHours: 6}}.
     */
    public static final String SOCIAL_ATTENTION_GAP = "social.attentionGap";

    /**
     * The cue settings (prompt 137, {@code social.cues}): {@code {enabled, triggers, maxPerDay: 50,
     * maxCostUsdPerCue: 1.00, platforms, xaiXSearch: {enabled: false, maxResults: 20}}}.
     */
    public static final String SOCIAL_CUES = "social.cues";

    /**
     * The track-record grading of claim sources (prompt 137, {@code social.trackRecord}): {@code {minAssessed: 10,
     * gradeEShare: 0.7, gradeDShare: 0.85}}; the overrides live in {@code social.sourceReliability}.
     */
    public static final String SOCIAL_TRACK_RECORD = "social.trackRecord";

    /**
     * The external binding router's durable cursor (prompt 138; joins the consumer clamp of retention): the last seq
     * whose EDP records the enabled bindings were applied to.
     */
    public static final String EXTERNAL_BINDING_CURSOR = "external.binding.cursor";

    /**
     * Whether automatic adapter-doctor runs may call the LLM (prompt 140, {@code doctor.autoLlm}); false by default, so
     * no LLM call happens without an explicit operator opt-in (NFR-13).
     */
    public static final String DOCTOR_AUTO_LLM = "doctor.autoLlm";

    /** The shortest interval between two automatic doctor runs of one source (prompt 140, {@code doctor.autoCooldown}); 6 h. */
    public static final String DOCTOR_AUTO_COOLDOWN = "doctor.autoCooldown";

    /**
     * Remote MCP authorization (prompt 141, {@code mcp.auth}): {@code {mode: token|oauth|both, issuer, audience, jwksUri,
     * scopeMap, scopeClaim, allowedAlgs}}; OpenAPI schema {@code McpAuthSettings}. {@code token} (the default) keeps
     * {@code /mcp} on Foresignal tokens only.
     */
    public static final String MCP_AUTH = "mcp.auth";

    /**
     * {@code ai.foresignal.plugin.PluginSettings}: whether out-of-tree WASM parser plugins may run at all (prompt 141,
     * {@code plugins}); {@code {"enabled": false}} by default.
     */
    public static final String PLUGINS = "plugins";

    /**
     * The plugins the admin enabled after reviewing their SHA-256 (prompt 141): a JSON object from plugin name to
     * {@code {sha256, enabledAt}} ({@code ai.foresignal.plugin.PluginApprovals}). A plugin runs only while its file
     * still hashes to the approved value.
     */
    public static final String PLUGINS_APPROVALS = "plugins.approvals";

    public static final List<String> ALL =
            List.of(RETENTION, CORS, TRIGGER_CURSOR, TRIGGERS, STREAM_LIMITS, SECURITY, SERVER, NOTIFICATIONS,
                    WATCHDOG, INSTALL_ID, LLM, SEARCH_INDEX, EMBEDDING_CURSOR, RATELIMITS, OUTBOUND, STATS_ROLLUP_CURSOR,
                    STATS, GLANCE, METRICS, VISIBILITY, REGION_CURSOR, REGION_TAGGING, WEATHER, ENTITY_CURSOR,
                    ENTITY, INTEL_CURSOR, ARCHIVE, CAP_SENDER, INSTANCE_NAME, FEDERATION_INSTANCE_NAME,
                    POL_RETENTION, EXERCISE_MAX_WINDOW, EXERCISE_ALERT_RETENTION, EVIDENCE_CURSOR, INTEL_ORIGINATORS,
                    EVIDENCE_MIN_LINK_CONFIDENCE, ABSENCE_MIN_DETECT, ATTENTION_SEEDED, SITUATION_CURSOR,
                    SITUATION_LINK_THRESHOLD, SITUATION_DORMANCY_WINDOW, SITUATION_REOPEN_WINDOW,
                    SITUATION_BRIEFS_ENABLED, SITUATION_BRIEFS_MAX_PER_DAY, SITUATION_BRIEF_MIN_INTERVAL, INTEL_THEMES,
                    INTEL_THEMES_LLM_CLASSIFY, INTEL_SIGNAL_FAMILIES, STATS_REGION_CURSOR, STATS_REGION_LEVEL,
                    ANALYSIS_SERIES_MAX, ANALYSIS_FINDINGS_PUBLISH_INFO, ANALYSIS_LIVE_CURSOR, ANALYSIS_LIVE_RECHECKS,
                    ANALYSIS_PRECURSORS_MAX_TESTS, ANALYSIS_PRECURSORS_LOOKBACK_DAYS,
                    ANALYSIS_PRECURSORS_DISCOVERY_FRACTION, PRECURSOR_CURSOR, PRECURSOR_STREAM_CURSORS,
                    BASERATE_FOLD_WEEK, FORECAST_ENSEMBLE, BENCHMARK_THRESHOLDS,
                    ANALYSIS_FORECASTS_QUESTION_SERIES, IMPACT_CURSOR, IMPACT_MIN_PROBABILITY,
                    IMPACT_QUESTION_MIN_PROBABILITY, IMPACT_ASSET_BUFFER_KM, RELATION_CURSOR,
                    CHRONICLE_DEFAULT_CAP_GB, CHRONICLE_AUTO_ACTIVATE_PER_DAY, CHRONICLE_AUTO_ACTIVATE_CAP_GB,
                    CHRONICLE_THREAD_CURSORS, EXCHANGE_MAX_PACKAGE_MB, EXCHANGE_ALLOW_GREEN_LINKS,
                    MEDIA_RELIABILITY_MAP, MEDIA_RELIABILITY_NUMBERS, NEWS_COVERAGE_THRESHOLDS,
                    NETWORK_MIN_SHARED_KEYS, NETWORK_FEDERATED, SOCIAL_LANGUAGES,
                    SOCIAL_MIN_REGION_BASELINE, SOCIAL_VELOCITY, SOCIAL_KEEP_MEDIA, SOCIAL_LLM_CLAIM_MIN_POSTS,
                    SOCIAL_LLM_CLAIMS_PER_DAY, SOCIAL_SOURCE_RELIABILITY, SOCIAL_COMPARE, SOCIAL_ATTENTION_GAP,
                    SOCIAL_CUES, SOCIAL_TRACK_RECORD, EXTERNAL_BINDING_CURSOR, DOCTOR_AUTO_LLM,
                    DOCTOR_AUTO_COOLDOWN, MCP_AUTH, PLUGINS, PLUGINS_APPROVALS, COLLECTOR);

    private SettingKeys() {}
}
