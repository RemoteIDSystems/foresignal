package ai.foresignal.source.adapters.nvd;

import java.math.BigDecimal;

/**
 * The EPSS banding rule (prompt 076). Pure. Only the bands are stored, never the raw score or percentile, so a
 * daily score change inside a band is not a new version.
 *
 * <ul>
 *   <li><strong>{@code epssBand}:</strong> {@code < 0.01} very-low, {@code < 0.05} low, {@code < 0.2} medium,
 *       {@code < 0.5} high, else very-high.
 *   <li><strong>{@code percentileBand}:</strong> {@code < 0.50} {@code <p50}, {@code < 0.90} {@code p50},
 *       {@code < 0.95} {@code p90}, {@code < 0.99} {@code p95}, else {@code p99}.
 * </ul>
 * Values are compared as decimals, so the boundaries are exact ({@code 0.05} is low-not-medium only below it).
 */
public final class EpssBands {

    /** Every {@code epssBand} value, lowest first. */
    public static final java.util.List<String> EPSS_BANDS = java.util.List.of("very-low", "low", "medium", "high",
            "very-high");

    /** Every {@code percentileBand} value, lowest first. */
    public static final java.util.List<String> PERCENTILE_BANDS = java.util.List.of("<p50", "p50", "p90", "p95",
            "p99");

    private static final BigDecimal P01 = new BigDecimal("0.01");
    private static final BigDecimal P05 = new BigDecimal("0.05");
    private static final BigDecimal P20 = new BigDecimal("0.2");
    private static final BigDecimal P50 = new BigDecimal("0.5");
    private static final BigDecimal P90 = new BigDecimal("0.9");
    private static final BigDecimal P95 = new BigDecimal("0.95");
    private static final BigDecimal P99 = new BigDecimal("0.99");

    private EpssBands() {}

    /**
     * The band of an EPSS probability.
     *
     * @param epss the probability, 0–1
     */
    public static String epssBand(BigDecimal epss) {
        if (epss.compareTo(P01) < 0) {
            return "very-low";
        }
        if (epss.compareTo(P05) < 0) {
            return "low";
        }
        if (epss.compareTo(P20) < 0) {
            return "medium";
        }
        return epss.compareTo(P50) < 0 ? "high" : "very-high";
    }

    /**
     * The band of an EPSS percentile.
     *
     * @param percentile the percentile, 0–1
     */
    public static String percentileBand(BigDecimal percentile) {
        if (percentile.compareTo(P50) < 0) {
            return "<p50";
        }
        if (percentile.compareTo(P90) < 0) {
            return "p50";
        }
        if (percentile.compareTo(P95) < 0) {
            return "p90";
        }
        return percentile.compareTo(P99) < 0 ? "p95" : "p99";
    }
}
