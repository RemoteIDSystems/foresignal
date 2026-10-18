package ai.foresignal.inbound.edp.binding;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The vocabulary of external data bindings (prompt 138, docs/External-Data-Protocol.md §5): targets, the item types
 * each target accepts, binding states, question-link orientations and the router's limits.
 */
public final class BindingVocabulary {

    /** Observations become analysis series. */
    public static final String SERIES = "series";

    /** Events join a 116 canonical-event family. */
    public static final String CANONICAL_FAMILY = "canonical-family";

    /** {@code social-post} documents join the 135–137 social pipeline. */
    public static final String SOCIAL = "social";

    /** {@code article} documents join 133 news stories. */
    public static final String NEWS = "news";

    /** Items become evidence rows of a 125 hypothesis set. */
    public static final String EVIDENCE = "evidence";

    /** Items mark a 125 indicator observed or reported. */
    public static final String INDICATOR = "indicator";

    /** Forecast items become external forecasts on 122 ledger questions. */
    public static final String FORECAST = "forecast";

    /** The latest value or document becomes a 036 place attribute. */
    public static final String PLACE = "place";

    /** Items link to a 118 situation or are offered to its linker. */
    public static final String SITUATION = "situation";

    /** A 131 chronicle captures the stream ({@code external-stream} layer). */
    public static final String CHRONICLE = "chronicle";

    /** Every target, in the spec's order. */
    public static final List<String> TARGETS = List.of(SERIES, CANONICAL_FAMILY, SOCIAL, NEWS, EVIDENCE, INDICATOR,
            FORECAST, PLACE, SITUATION, CHRONICLE);

    /** The binding is applied. */
    public static final String ACTIVE = "active";

    /** The binding is switched off (by the operator, or by a breaking manifest update). */
    public static final String DISABLED = "disabled";

    /** The binding is applying stored records. */
    public static final String BACKFILLING = "backfilling";

    /** The binding cannot be applied (its target is gone). */
    public static final String ERROR = "error";

    /** Every binding state. */
    public static final List<String> STATES = List.of(ACTIVE, DISABLED, BACKFILLING, ERROR);

    /** A question link keeps the provider's probability. */
    public static final String SAME = "same";

    /** A question link inverts the provider's probability ({@code 1 − p}). */
    public static final String INVERSE = "inverse";

    /** The orientations. */
    public static final List<String> ORIENTATIONS = List.of(SAME, INVERSE);

    /** The most stored items one binding backfills. */
    public static final int BACKFILL_CAP = 100_000;

    /** The items a preview evaluates. */
    public static final int PREVIEW_ITEMS = 100;

    /** The smallest probability move that records a new external forecast. */
    public static final double MIN_PROBABILITY_CHANGE = 0.02;

    /** Resolved linked questions an external forecaster needs before it joins the ensemble. */
    public static final int ENSEMBLE_MIN_RESOLVED = 30;

    /** The prefix of external forecaster ids. */
    public static final String FORECASTER_PREFIX = "edp:";

    /** The item types each target accepts. */
    public static final Map<String, Set<String>> ITEM_TYPES;

    static {
        Map<String, Set<String>> m = new LinkedHashMap<>();
        m.put(SERIES, Set.of("observation"));
        m.put(CANONICAL_FAMILY, Set.of("event"));
        m.put(SOCIAL, Set.of("document"));
        m.put(NEWS, Set.of("document"));
        m.put(EVIDENCE, Set.of("feature", "event", "observation", "document"));
        m.put(INDICATOR, Set.of("feature", "event", "observation", "document"));
        m.put(FORECAST, Set.of("forecast"));
        m.put(PLACE, Set.of("feature", "event", "observation", "document"));
        m.put(SITUATION, Set.of("feature", "event", "document"));
        m.put(CHRONICLE, Set.of("feature", "event", "observation", "document", "forecast"));
        ITEM_TYPES = java.util.Collections.unmodifiableMap(m);
    }

    /** The document type a target needs ({@code social-post} or {@code article}), or null. */
    public static String documentType(String target) {
        return switch (target) {
            case SOCIAL -> "social-post";
            case NEWS -> "article";
            default -> null;
        };
    }

    /** The external forecaster of a forecast stream: {@code edp:{sourceId}/{streamId}}. */
    public static String forecasterId(String sourceId, String streamId) {
        return FORECASTER_PREFIX + sourceId + "/" + streamId;
    }

    /** The requirement of {@code target} in words, for {@code BINDING_TARGET_INVALID}. */
    public static String requirement(String target) {
        Set<String> types = ITEM_TYPES.getOrDefault(target, Set.of());
        String doc = documentType(target);
        String list = String.join(", ", new java.util.TreeSet<>(types));
        return doc == null ? "a stream of " + list + " items"
                : "a stream of document items with documentType " + doc + " (or none declared)";
    }

    private BindingVocabulary() {}
}
