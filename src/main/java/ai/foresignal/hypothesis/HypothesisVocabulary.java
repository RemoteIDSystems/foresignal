package ai.foresignal.hypothesis;

import java.util.List;
import java.util.Map;

/**
 * The vocabularies of rolling hypotheses (prompt 125), as {@code docs/OpenAPI.json} enumerates them. The indicator
 * state and evidence source lists are kept as plain lists so prompts 136–137 can extend them ({@code reported},
 * {@code social-claim}) without touching the updater.
 */
public final class HypothesisVocabulary {

    /** Exclusive set: probabilities sum to 1 with exactly one {@code other} hypothesis. */
    public static final String EXCLUSIVE = "exclusive";

    /** Independent set: each hypothesis is judged on its own. */
    public static final String INDEPENDENT = "independent";

    /** The set kinds. */
    public static final List<String> KINDS = List.of(EXCLUSIVE, INDEPENDENT);

    /** The review cadences. */
    public static final List<String> CADENCES = List.of("daily", "weekly", "monthly");

    /** An active set. */
    public static final String SET_ACTIVE = "active";

    /** A resolved set. */
    public static final String SET_RESOLVED = "resolved";

    /** An archived set. */
    public static final String SET_ARCHIVED = "archived";

    /** The set statuses. */
    public static final List<String> SET_STATUSES = List.of(SET_ACTIVE, SET_RESOLVED, SET_ARCHIVED);

    /** The origins. */
    public static final List<String> ORIGINS = List.of("operator", "agent", "analyst");

    /** Low confidence. */
    public static final String LOW = "low";

    /** Moderate confidence. */
    public static final String MODERATE = "moderate";

    /** High confidence. */
    public static final String HIGH = "high";

    /** The confidence levels, lowest first. */
    public static final List<String> CONFIDENCE = List.of(LOW, MODERATE, HIGH);

    /** An outcome hypothesis. */
    public static final String OUTCOME = "outcome";

    /** An explanation hypothesis. */
    public static final String EXPLANATION = "explanation";

    /** A speculative-link hypothesis (needs linked findings, a coincidence baseline and two alternatives). */
    public static final String SPECULATIVE_LINK = "speculative-link";

    /** The "other / none of these" hypothesis of an exclusive set. */
    public static final String OTHER = "other";

    /** The hypothesis types. */
    public static final List<String> HYPOTHESIS_TYPES = List.of(OUTCOME, EXPLANATION, SPECULATIVE_LINK, OTHER);

    /** The hypothesis statuses. */
    public static final List<String> HYPOTHESIS_STATUSES = List.of("open", "confirmed", "refuted", "expired");

    /** The prior-basis types. */
    public static final List<String> PRIOR_BASES = List.of("base-rate", "analogs", "operator");

    /** The revision causes. */
    public static final List<String> CAUSES = List.of("indicator-observed", "indicator-absent", "evidence-added",
            "evidence-removed", "base-rate-refresh", "review", "operator", "created");

    /** The indicator signal types. */
    public static final List<String> SIGNAL_TYPES = List.of("rule", "precursor", "series", "intelEvent", "situation",
            "question", "manual");

    /** The signal types that own a 017 watch rule. */
    public static final List<String> WATCHED_SIGNALS = List.of("rule", "series", "intelEvent", "situation");

    /** A pending indicator. */
    public static final String PENDING = "pending";

    /** An indicator whose first-stage threshold was met. */
    public static final String EMERGING = "emerging";

    /** An observed indicator. */
    public static final String OBSERVED = "observed";

    /** An indicator that did not appear by {@code expected_by} with coverage. */
    public static final String ABSENT = "absent";

    /** An indicator that did not appear by {@code expected_by} without coverage (no update). */
    public static final String UNKNOWN_COVERAGE = "unknown-coverage";

    /** A retired indicator. */
    public static final String RETIRED = "retired";

    /** A social claim (prompts 136–137: reported, distinct from observed physical evidence). */
    public static final String REPORTED = "reported";

    /** The indicator states (extensible). */
    public static final List<String> INDICATOR_STATES = List.of(PENDING, EMERGING, OBSERVED, ABSENT, UNKNOWN_COVERAGE,
            RETIRED, REPORTED);

    /** The credibility and relevance grades. */
    public static final List<String> GRADES = List.of("H", "M", "L");

    /** The ACH weight of a grade (L 0.5, M 1, H 2). */
    public static final Map<String, Double> GRADE_WEIGHT = Map.of("L", 0.5, "M", 1.0, "H", 2.0);

    /** The ACH ratings. */
    public static final List<String> RATINGS = List.of("CC", "C", "N", "NA", "I", "II");

    /**
     * The evidence sources that can be added through the API (extensible: 130 adds {@code evidence-item}, a 130
     * evidence item the caller can see; 136 adds {@code social-claim}).
     */
    public static final List<String> INPUT_EVIDENCE_SOURCES = List.of("record", "finding", "intel_event", "alert",
            "manual", "evidence-item", "social-claim");

    /** Every evidence source a row may carry. */
    public static final List<String> EVIDENCE_SOURCES = List.of("indicator", "record", "finding", "intel_event",
            "alert", "manual", "evidence-item", "social-claim");

    /** The key-assumption statuses. */
    public static final List<String> ASSUMPTION_STATUSES = List.of("solid", "caveated", "unsupported", "broken");

    /** The LR classes. */
    public static final List<String> LR_CLASSES = List.of("high", "medium", "low", "neutral");

    /** The LR directions. */
    public static final List<String> LR_DIRECTIONS = List.of("supports", "weakens");

    private HypothesisVocabulary() {}

    /** The confidence one step lower ({@code low} stays {@code low}). */
    public static String lower(String level) {
        int i = CONFIDENCE.indexOf(level);
        return i <= 0 ? LOW : CONFIDENCE.get(i - 1);
    }
}
