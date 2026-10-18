package ai.foresignal.media.outlet;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The vocabulary of the outlet registry and its ratings (prompt 133, {@code docs/OpenAPI.json} {@code MediaOutlet},
 * {@code MediaRating}, {@code OutletConsensus}, {@code MediaProvider}).
 */
public final class MediaVocabulary {

    /** Outlet types. */
    public static final List<String> OUTLET_TYPES = List.of("wire", "national", "regional", "local",
            "public-broadcaster", "state-affiliated", "aggregator", "blog", "official", "other");

    /** The type of an outlet nothing describes. */
    public static final String OTHER = "other";

    /** The wire type. */
    public static final String WIRE = "wire";

    /** The state-affiliated type. */
    public static final String STATE_AFFILIATED = "state-affiliated";

    /** The lean dimension: −2 (left) … +2 (right). */
    public static final String LEAN = "lean";

    /** The factuality dimension: 1 … 5. */
    public static final String FACTUALITY = "factuality";

    /** The reliability dimension. */
    public static final String RELIABILITY = "reliability";

    /** The state-affiliation dimension: 1 when a provider records the outlet as state media. */
    public static final String STATE_AFFILIATION = "state-affiliation";

    /** The ownership-type dimension (labels such as {@code public-broadcaster}, {@code state-media}). */
    public static final String OWNERSHIP_TYPE = "ownership-type";

    /** The standard dimensions. */
    public static final List<String> DIMENSIONS = List.of(LEAN, FACTUALITY, RELIABILITY, STATE_AFFILIATION,
            OWNERSHIP_TYPE);

    /** A provider-specific axis that is not left–right: {@code {provider}:{axis}}. */
    public static final Pattern PROVIDER_AXIS = Pattern.compile("^[a-z0-9-]+:[a-z0-9_-]+$");

    /** Lean buckets. */
    public static final String LEFT = "left";

    /** The center bucket. */
    public static final String CENTER = "center";

    /** The right bucket. */
    public static final String RIGHT = "right";

    /** The bucket of outlets without a lean consensus. */
    public static final String UNRATED = "unrated";

    /** Every coverage bucket. */
    public static final List<String> BUCKETS = List.of(LEFT, CENTER, RIGHT, UNRATED);

    /** Lean labels by normalized value. */
    public static final Map<Integer, String> LEAN_LABELS = Map.of(-2, "left", -1, "lean-left", 0, "center", 1,
            "lean-right", 2, "right");

    /** Egress values. */
    public static final String OPEN = "OPEN";

    /** The local-only egress. */
    public static final String LOCAL_ONLY = "LOCAL_ONLY";

    /** Providers. */
    public static final String WIKIDATA = "wikidata";

    /** The Partisan Web. */
    public static final String PARTISAN_WEB = "partisan-web";

    /** AllSides. */
    public static final String ALLSIDES = "allsides";

    /** Media Bias/Fact Check. */
    public static final String MBFC = "mbfc";

    /** Operator imports. */
    public static final String OPERATOR = "operator";

    /** Every provider, in the spec's order. */
    public static final List<String> PROVIDERS = List.of(WIKIDATA, PARTISAN_WEB, ALLSIDES, MBFC, OPERATOR);

    private MediaVocabulary() {}

    /** True for a standard dimension or a provider axis. */
    public static boolean validDimension(String dimension) {
        return dimension != null && (DIMENSIONS.contains(dimension) || PROVIDER_AXIS.matcher(dimension).matches());
    }

    /** The lean label of a normalized value, or null. */
    public static String leanLabel(Double value) {
        if (value == null || !Double.isFinite(value)) {
            return null;
        }
        return LEAN_LABELS.get((int) Math.max(-2, Math.min(2, Math.round(value))));
    }
}
