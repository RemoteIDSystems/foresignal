package ai.foresignal.analytics.finding;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The caveat codes findings carry (prompt 119, Intelligence-Design §5.1): 103's codes plus
 * {@value #LAGGED_PUBLICATION}, {@value #FEW_EVENTS}, {@value #MULTIPLE_TESTS}, {@value #LOW_REPORTING_DENSITY},
 * {@value #DAILY_RESOLUTION_ONLY} and {@value #LOW_DETECTABILITY}.
 */
public final class FindingCaveats {

    /** The series is published with a delay; the pre-event window may have been incomplete at the event. */
    public static final String LAGGED_PUBLICATION = "LAGGED_PUBLICATION";

    /** Too few events or estimation points for a stable statistic. */
    public static final String FEW_EVENTS = "FEW_EVENTS";

    /** Many tests were run; the false-discovery rate is controlled, single p-values overstate. */
    public static final String MULTIPLE_TESTS = "MULTIPLE_TESTS";

    /** Thin reporting in the region. */
    public static final String LOW_REPORTING_DENSITY = "LOW_REPORTING_DENSITY";

    /** Daily or weekly market data cannot see intraday moves. */
    public static final String DAILY_RESOLUTION_ONLY = "DAILY_RESOLUTION_ONLY";

    /** The sources could not have seen an absence (116 {@code canSeeAbsence}). */
    public static final String LOW_DETECTABILITY = "LOW_DETECTABILITY";

    /** A coverage gap touched the window (103). */
    public static final String COVERAGE_GAPS = "COVERAGE_GAPS";

    /** Replays of what Foresignal stored, not ground truth (103). */
    public static final String NOT_GROUND_TRUTH = "NOT_GROUND_TRUTH";

    /** Fewer than ten samples (103). */
    public static final String SMALL_SAMPLE = "SMALL_SAMPLE";

    private FindingCaveats() {}

    /** A caveat without data. */
    public static Map<String, Object> of(String code, String text) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("code", code);
        out.put("text", text);
        return out;
    }

    /** {@value #DAILY_RESOLUTION_ONLY} with its standard text. */
    public static Map<String, Object> dailyResolutionOnly() {
        return of(DAILY_RESOLUTION_ONLY, "Foresignal's market data is daily or weekly; moves visible only in"
                + " intraday exchange data cannot be seen.");
    }

    /** {@value #NOT_GROUND_TRUTH} with its standard text. */
    public static Map<String, Object> notGroundTruth() {
        return of(NOT_GROUND_TRUTH, "Computed from what Foresignal stored, not ground truth.");
    }
}
