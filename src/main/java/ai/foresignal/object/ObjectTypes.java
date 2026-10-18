package ai.foresignal.object;

import java.util.List;
import java.util.Set;

/**
 * The object and relation vocabulary of the client contract (docs/Client-Contract.md §2, §4, §17) and the versions
 * {@code GET /v1/contract} reports.
 */
public final class ObjectTypes {

    /** Contract version in force. */
    public static final String CONTRACT_VERSION = "1.5";

    /** Workspace context schema version (Client-Contract §5). */
    public static final int CONTEXT_SCHEMA_VERSION = 1;

    /** Deep-link payload version (Client-Contract §6). */
    public static final int DEEP_LINK_VERSION = 1;

    public static final String RECORD = "record";
    public static final String ENTITY = "entity";
    public static final String CANONICAL_EVENT = "canonical-event";
    public static final String INTEL_EVENT = "intel-event";
    public static final String SITUATION = "situation";
    public static final String FINDING = "finding";
    public static final String PRECURSOR = "precursor";
    public static final String BASE_RATE = "base-rate";
    public static final String HYPOTHESIS_SET = "hypothesis-set";
    public static final String HYPOTHESIS = "hypothesis";
    public static final String INDICATOR = "indicator";
    public static final String QUESTION = "question";
    public static final String FORECAST = "forecast";
    public static final String ASSESSMENT = "assessment";
    public static final String ALERT = "alert";
    public static final String INVESTIGATION = "investigation";
    public static final String PLACE = "place";
    public static final String VIEW = "view";
    public static final String WATCHLIST = "watchlist";
    public static final String SOURCE = "source";
    public static final String SENSOR = "sensor";
    public static final String WORKSPACE = "workspace";
    public static final String ANNOTATION = "annotation";
    public static final String PRINCIPAL = "principal";
    public static final String GROUP = "group";
    public static final String TAG = "tag";
    public static final String ATTACHMENT = "attachment";
    public static final String EVIDENCE_ITEM = "evidence-item";
    public static final String SAVED_SEARCH = "saved-search";
    public static final String CHRONICLE = "chronicle";
    public static final String REPORT = "report";
    public static final String REMOTE = "remote";
    public static final String NEWS_STORY = "news-story";
    public static final String OUTLET = "outlet";
    public static final String NARRATIVE = "narrative";
    public static final String CLAIM = "claim";

    /**
     * Every object type in force, in contract order: the 23 types of prompt 129, the six collaboration types of
     * prompt 130 (contract 1.1), {@code chronicle} of prompt 131 (contract 1.2), and {@code report} and
     * {@code remote} (read-only material received from another instance) of prompt 132 (contract 1.3), and
     * {@code news-story} and {@code outlet} of prompt 133 (contract 1.4), and {@code narrative} and {@code claim} of
     * prompt 136 (contract 1.5).
     */
    public static final List<String> ALL = List.of(RECORD, ENTITY, CANONICAL_EVENT, INTEL_EVENT, SITUATION, FINDING,
            PRECURSOR, BASE_RATE, HYPOTHESIS_SET, HYPOTHESIS, INDICATOR, QUESTION, FORECAST, ASSESSMENT, ALERT,
            INVESTIGATION, PLACE, VIEW, WATCHLIST, SOURCE, SENSOR, WORKSPACE, ANNOTATION, PRINCIPAL, GROUP, TAG,
            ATTACHMENT, EVIDENCE_ITEM, SAVED_SEARCH, CHRONICLE, REPORT, REMOTE, NEWS_STORY, OUTLET, NARRATIVE,
            CLAIM);

    private static final Set<String> ALL_SET = Set.copyOf(ALL);

    public static final String MEMBER_OF = "member-of";
    public static final String OBSERVED_BY = "observed-by";
    public static final String SAME_AS = "same-as";
    public static final String LOCATED_IN = "located-in";
    public static final String NEAR = "near";
    public static final String DEPENDS_ON = "depends-on";
    public static final String AFFECTS = "affects";
    public static final String DERIVED_FROM = "derived-from";
    public static final String EVIDENCE_FOR = "evidence-for";
    public static final String EVIDENCE_AGAINST = "evidence-against";
    public static final String ABOUT = "about";
    public static final String MENTIONS = "mentions";
    public static final String PRECEDES = "precedes";
    public static final String CAUSES = "causes";
    public static final String RELATED_TO = "related-to";

    /** Every relation type (Client-Contract §4), in contract order. */
    public static final List<String> RELATION_TYPES = List.of(MEMBER_OF, OBSERVED_BY, SAME_AS, LOCATED_IN, NEAR,
            DEPENDS_ON, AFFECTS, DERIVED_FROM, EVIDENCE_FOR, EVIDENCE_AGAINST, ABOUT, MENTIONS, PRECEDES, CAUSES,
            RELATED_TO);

    /** Relation labels, strongest first (the 104 vocabulary). */
    public static final List<String> LABELS = List.of("confirmed", "likely", "possible", "speculative");

    /** Client kinds for presence (Client-Contract §5). */
    public static final List<String> CLIENT_KINDS = List.of("globe", "map", "analyst", "satellite", "rf", "mobile",
            "agent", "other");

    private ObjectTypes() {}

    /** True when {@code type} is a known object type. */
    public static boolean isType(String type) {
        return type != null && ALL_SET.contains(type);
    }

    /** True when {@code type} is a known relation type. */
    public static boolean isRelationType(String type) {
        return type != null && RELATION_TYPES.contains(type);
    }

    /** The rank of a label: 0 for {@code confirmed} up to 3 for {@code speculative}; unknown labels rank weakest. */
    public static int labelRank(String label) {
        int i = label == null ? -1 : LABELS.indexOf(label);
        return i < 0 ? LABELS.size() - 1 : i;
    }

    /** The weaker of two labels. */
    public static String weaker(String a, String b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return labelRank(a) >= labelRank(b) ? a : b;
    }

    /** The label for a confidence when the producer has none: ≥ 0.9 confirmed, ≥ 0.6 likely, ≥ 0.3 possible. */
    public static String labelFor(double confidence) {
        if (confidence >= 0.9) {
            return "confirmed";
        }
        if (confidence >= 0.6) {
            return "likely";
        }
        if (confidence >= 0.3) {
            return "possible";
        }
        return "speculative";
    }
}
