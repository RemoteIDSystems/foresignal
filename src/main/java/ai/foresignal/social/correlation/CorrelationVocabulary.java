package ai.foresignal.social.correlation;

import java.util.List;

/**
 * The vocabulary of social–physical comparisons, attention gaps, cues and lead times (prompt 137, OpenAPI
 * {@code SocialComparison}, {@code SocialCue}, {@code LeadTimeStats}, {@code SocialSettings}).
 */
public final class CorrelationVocabulary {

    /** The comparator's name. */
    public static final String COMPARATOR = "SocialPhysicalComparator";

    /** The comparator's version. */
    public static final String COMPARATOR_VERSION = "1.0.0";

    /** The attention-gap detector's name. */
    public static final String GAP_DETECTOR = "AttentionGapDetector";

    /** The attention-gap detector's version. */
    public static final String GAP_DETECTOR_VERSION = "1.0.0";

    /** The cue service's component name (manifests). */
    public static final String CUE_COMPONENT = "SocialCueService";

    /** The cue service's version. */
    public static final String CUE_VERSION = "1.0.0";

    /** The ledger forecaster of claim reliability (122). */
    public static final String FORECASTER = "social-claim";

    /** Its version. */
    public static final String FORECASTER_VERSION = "1.0.0";

    /** The ledger question category. */
    public static final String QUESTION_CATEGORY = "social";

    /** Status: the window (plus publication lag) is open. */
    public static final String PENDING = "pending";

    /** Status: a non-social evidence group matches. */
    public static final String CORROBORATED = "corroborated";

    /** Status: nothing matched while adequate coverage could have seen it. */
    public static final String UNOBSERVED = "unobserved";

    /** Status: an agency or sensor record negates it, or its media is reused. */
    public static final String CONTRADICTED = "contradicted";

    /** Status: nothing matched and coverage could not have seen it. */
    public static final String INSUFFICIENT = "insufficient-coverage";

    /** Narrative status: its claims split across statuses. */
    public static final String MIXED = "mixed";

    /** Every comparison status. */
    public static final List<String> STATUSES = List.of(PENDING, CORROBORATED, UNOBSERVED, CONTRADICTED,
            INSUFFICIENT);

    /** The final statuses. */
    public static final List<String> FINAL_STATUSES = List.of(CORROBORATED, UNOBSERVED, CONTRADICTED, INSUFFICIENT);

    /** Contradiction: a negating agency or sensor record. */
    public static final String NEGATING_RECORD = "negating-record";

    /** Contradiction: the claim's media is a reuse finding. */
    public static final String MEDIA_REUSE = "media-reuse";

    /** Found without looking. */
    public static final String ORGANIC = "organic";

    /** Found by a cue. */
    public static final String CUE = "cue";

    /** The {@code foundBy} values. */
    public static final List<String> FOUND_BY = List.of(ORGANIC, CUE);

    /** The finding type of attention gaps (119). */
    public static final String ATTENTION_GAP_FINDING = "attention-gap";

    /** Cue status: waiting to run. */
    public static final String CUE_QUEUED = "queued";

    /** Cue status: searching. */
    public static final String CUE_RUNNING = "running";

    /** Cue status: finished. */
    public static final String CUE_DONE = "done";

    /** Cue status: every platform failed. */
    public static final String CUE_FAILED = "failed";

    /** Cue status: refused by a budget or the money cap before any call. */
    public static final String CUE_BUDGET_REFUSED = "budget-refused";

    /** Every cue status. */
    public static final List<String> CUE_STATUSES = List.of(CUE_QUEUED, CUE_RUNNING, CUE_DONE, CUE_FAILED,
            CUE_BUDGET_REFUSED);

    /** The cue trigger types. */
    public static final List<String> TRIGGER_TYPES = List.of("situation-activated", "situation-escalated", "finding",
            "anomaly", "alert", "canonical-event", "manual");

    /** Backend: the platforms' own search APIs. */
    public static final String BACKEND_PLATFORMS = "platform-apis";

    /** Backend: xAI's X search tool (026 provider {@code xai}). */
    public static final String BACKEND_XAI = "xai-x-search";

    /** SSE event: a comparison was created or revised. */
    public static final String EVENT_COMPARISON = "comparison.updated";

    /** SSE event: a cue finished, failed or was refused. */
    public static final String EVENT_CUE = "cue.completed";

    /** SSE event: an attention-gap finding. */
    public static final String EVENT_GAP = "attention.gap";

    /** Notification: a comparison became unobserved or contradicted for a fast narrative. */
    public static final String NOTIFY_DIVERGENCE = "social.divergence";

    /** Notification: an attention gap. */
    public static final String NOTIFY_GAP = "social.attention_gap";

    /** Caveat: a cue-found claim matched only the physical event that cued the search. */
    public static final String CUE_TRIGGER_MATCH = "CUE_TRIGGER_MATCH";

    /** Caveat: single comparisons are observed lead times, never predictive value. */
    public static final String OBSERVED_LEAD_TIME = "OBSERVED_LEAD_TIME";

    /** The search-capable platform adapters cues use. */
    public static final List<String> CUE_ADAPTERS = List.of("bluesky-search", "reddit", "x", "threads");

    /** The default {@code social.compare.minGroups}. */
    public static final int DEFAULT_MIN_GROUPS = 3;

    /** The default {@code social.cues.maxPerDay}. */
    public static final int DEFAULT_MAX_CUES_PER_DAY = 50;

    /** The default {@code social.cues.maxCostUsdPerCue}. */
    public static final double DEFAULT_MAX_COST_PER_CUE = 1.00;

    /** The default {@code social.attentionGap.percentile}. */
    public static final double DEFAULT_GAP_PERCENTILE = 10;

    /** The default {@code social.attentionGap.lookbackDays}. */
    public static final int DEFAULT_GAP_LOOKBACK_DAYS = 365;

    /** The default {@code social.attentionGap.windowHours}. */
    public static final int DEFAULT_GAP_WINDOW_HOURS = 6;

    /** The default {@code social.cues.xaiXSearch.maxResults}. */
    public static final int DEFAULT_XAI_MAX_RESULTS = 20;

    private CorrelationVocabulary() {}

    /** True for a final comparison status. */
    public static boolean isFinal(String status) {
        return FINAL_STATUSES.contains(status);
    }
}
