package ai.foresignal.social.narrative;

import java.time.Duration;
import java.util.List;

/**
 * The vocabulary of narratives, claims, media fingerprints, propagation and coordination (prompt 136, OpenAPI
 * {@code Narrative}, {@code Claim}, {@code MediaFingerprint}, {@code PropagationGraph}, {@code CoordinationIndicators}).
 */
public final class NarrativeVocabulary {

    /** The 116 canonical-event family of narratives. */
    public static final String FAMILY = "social-narrative";

    /** The clusterer's name. */
    public static final String CLUSTERER = "NarrativeClusterer";

    /** The clusterer's version. */
    public static final String CLUSTERER_VERSION = "1.0.0";

    /** The claim extractor's name. */
    public static final String EXTRACTOR = "ClaimExtractor";

    /** The claim extractor's version. */
    public static final String EXTRACTOR_VERSION = "1.0.0";

    /** The media fingerprinter's name. */
    public static final String FINGERPRINTER = "MediaFingerprinter";

    /** The media fingerprinter's version. */
    public static final String FINGERPRINTER_VERSION = "1.0.0";

    /** The propagation tracer's name. */
    public static final String TRACER = "PropagationTracer";

    /** The propagation tracer's version. */
    public static final String TRACER_VERSION = "1.0.0";

    /** The coordination detector's name. */
    public static final String COORDINATION = "CoordinationDetector";

    /** The coordination detector's version. */
    public static final String COORDINATION_VERSION = "1.0.0";

    /** The LLM role of claim extraction (026). */
    public static final String LLM_ROLE = "claim-extraction";

    /** The 125 evidence source of a claim. */
    public static final String EVIDENCE_SOURCE = "social-claim";

    /** Link reason of a narrative's first member. */
    public static final String SEED = "seed";

    /** Link reason: reposts a member. */
    public static final String REPOST = "repost";

    /** Link reason: quotes a member. */
    public static final String QUOTE = "quote";

    /** Link reason: similar text. */
    public static final String TEXT = "text";

    /** Link reason: shares a link with a member. */
    public static final String LINK = "link";

    /** Link reason: shares a media fingerprint with a member. */
    public static final String MEDIA = "media";

    /** Every link reason. */
    public static final List<String> LINK_REASONS = List.of(SEED, REPOST, QUOTE, TEXT, LINK, MEDIA);

    /** Status before 137 assessed anything. */
    public static final String UNASSESSED = "unassessed";

    /** Every narrative status (set by 137). */
    public static final List<String> NARRATIVE_STATUSES = List.of(UNASSESSED, "pending", "corroborated", "unobserved",
            "contradicted", "mixed", "insufficient-coverage");

    /** Every claim status (set by 137). */
    public static final List<String> CLAIM_STATUSES = List.of(UNASSESSED, "pending", "corroborated", "unobserved",
            "contradicted", "insufficient-coverage");

    /** State: young or accelerating. */
    public static final String EMERGING = "emerging";

    /** State: posting continues. */
    public static final String ACTIVE = "active";

    /** State: slowing. */
    public static final String FADING = "fading";

    /** State: no posts for {@link #DORMANT_AFTER}. */
    public static final String DORMANT = "dormant";

    /** Every state. */
    public static final List<String> STATES = List.of(EMERGING, ACTIVE, FADING, DORMANT);

    /** The narrative history actions. */
    public static final List<String> LOG_ACTIONS = List.of("created", "merged", "merged-into", "split", "split-from",
            "state-changed", "status-changed", "title-changed");

    /** How young a narrative is still emerging. */
    public static final Duration EMERGING_AGE = Duration.ofHours(6);

    /** Silence after which a narrative is fading. */
    public static final Duration FADING_AFTER = Duration.ofHours(24);

    /** Silence after which a narrative is dormant. */
    public static final Duration DORMANT_AFTER = Duration.ofHours(72);

    /** The velocity ratio at or above which a narrative is emerging whatever its age. */
    public static final double EMERGING_VELOCITY = 5;

    /** The velocity ratio below which an older narrative is fading. */
    public static final double FADING_VELOCITY = 0.5;

    /** The longest title. */
    public static final int TITLE_CHARS = 140;

    /** The default PDQ match threshold (PDQ's recommendation). */
    public static final int PDQ_MATCH_DISTANCE = 31;

    /** The least PDQ quality matched. */
    public static final int PDQ_MIN_QUALITY = 50;

    /** How much earlier a fingerprint's first sighting makes a reuse. */
    public static final Duration REUSE_AGE = Duration.ofDays(7);

    /** The finding type of reused media (119). */
    public static final String MEDIA_REUSE_FINDING = "media-reuse";

    /** The finding type of coordination indicators (119). */
    public static final String COORDINATION_FINDING = "social-coordination";

    /** The caveat stating the fingerprint store's start. */
    public static final String REFERENCE_WINDOW = "REFERENCE_WINDOW";

    /** The edge types of a propagation graph. */
    public static final List<String> EDGE_TYPES = List.of("repost", "quote", "reply", "link", "text-match",
            "media-match", "cross-platform");

    /** The node types of a propagation graph. */
    public static final List<String> NODE_TYPES = List.of("post", "evidence-group", "news-article",
            "official-record");

    /** The largest summarized graph. */
    public static final int MAX_GRAPH_NODES = 500;

    /** The coordination indicator names. */
    public static final List<String> INDICATORS = List.of("synchrony", "textDuplication", "accountAgeSkew",
            "coRepostBursts");

    /** The finding wording of coordination; never about accounts. */
    public static final String COORDINATION_STATEMENT = "coordinated-amplification indicators present";

    /** The SSE event: a narrative was created. */
    public static final String EVENT_EMERGED = "narrative.emerged";

    /** The SSE event: a narrative crossed a band. */
    public static final String EVENT_UPDATED = "narrative.updated";

    /** The SSE event: a claim was extracted. */
    public static final String EVENT_CLAIM = "claim.extracted";

    /** The SSE event: a reuse finding. */
    public static final String EVENT_MEDIA_REUSE = "media.reuse";

    /** The SSE event: a coordination finding. */
    public static final String EVENT_COORDINATION = "coordination.detected";

    /** The default {@code social.llmClaimMinPosts}. */
    public static final int DEFAULT_LLM_MIN_POSTS = 50;

    /** The evidence groups that make a narrative eligible for LLM extraction whatever its size. */
    public static final int LLM_MIN_GROUPS = 5;

    /** The default {@code social.llmClaimsPerDay}. */
    public static final int DEFAULT_LLM_PER_DAY = 20;

    /** The member texts sent to the LLM. */
    public static final int LLM_TEXTS = 20;

    private NarrativeVocabulary() {}
}
