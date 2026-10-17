package ai.foresignal.forecast;

import java.util.List;

/** The vocabularies of the forecast ledger (prompt 122), as the spec enumerates them. */
public final class Ledger {

    /** A yes/no question. */
    public static final String BINARY = "binary";

    /** One of several unordered outcomes. */
    public static final String MULTIPLE = "multiple";

    /** One of several ordered outcomes (scored with RPS). */
    public static final String ORDERED = "ordered";

    /** A number (forecast as quantiles, scored with CRPS). */
    public static final String NUMERIC = "numeric";

    /** The question types. */
    public static final List<String> TYPES = List.of(BINARY, MULTIPLE, ORDERED, NUMERIC);

    /** The resolver types. */
    public static final List<String> RESOLVER_TYPES = List.of("event-exists", "count-threshold", "series-value",
            "alert-fired", "situation-status", "hypothesis-outcome", "manual");

    /** The question origins. */
    public static final List<String> ORIGINS = List.of("operator", "agent", "system", "analyst");

    /** The forecaster types. */
    public static final List<String> FORECASTER_TYPES = List.of("base-rate", "climatology", "persistence",
            "statistical", "precursor", "analog", "impact", "indicator-bayes", "llm", "operator", "external",
            "ensemble");

    /** The quantile levels of a numeric forecast, in order. */
    public static final List<String> QUANTILES = List.of("p05", "p10", "p25", "p50", "p75", "p90", "p95");

    /** The quantile levels a numeric forecast must carry. */
    public static final List<String> REQUIRED_QUANTILES = List.of("p05", "p25", "p50", "p75", "p95");

    /** The annulment codes. */
    public static final List<String> ANNUL_CODES = List.of("LOW_DETECTABILITY", "COVERAGE_GAP", "OPERATOR");

    /** The binary outcome labels. */
    public static final List<String> YES_NO = List.of("yes", "no");

    private Ledger() {}

    /** The probability level of a quantile key ({@code p05} → 0.05). */
    public static double level(String quantile) {
        return Integer.parseInt(quantile.substring(1)) / 100.0;
    }
}
