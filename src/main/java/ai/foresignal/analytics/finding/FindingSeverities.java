package ai.foresignal.analytics.finding;

import java.util.List;

/**
 * Finding severities (prompt 119): {@code significant} when the detector's p-value (or placebo p) is at most
 * {@value #SIGNIFICANT_P} and the effect passes its threshold, {@code notable} at most {@value #NOTABLE_P}, otherwise
 * {@code info} (stored, published as a record only with {@code analysis.findings.publishInfo}).
 */
public final class FindingSeverities {

    /** Informational. */
    public static final String INFO = "info";

    /** Notable. */
    public static final String NOTABLE = "notable";

    /** Significant. */
    public static final String SIGNIFICANT = "significant";

    /** Every severity, lowest first. */
    public static final List<String> ALL = List.of(INFO, NOTABLE, SIGNIFICANT);

    /** The p-value of a significant finding. */
    public static final double SIGNIFICANT_P = 0.001;

    /** The p-value of a notable finding. */
    public static final double NOTABLE_P = 0.01;

    private FindingSeverities() {}

    /** The severity of a p-value when the effect passes ({@code effectPasses}); {@code info} otherwise. */
    public static String of(double p, boolean effectPasses) {
        if (!effectPasses || Double.isNaN(p)) {
            return INFO;
        }
        if (p <= SIGNIFICANT_P) {
            return SIGNIFICANT;
        }
        return p <= NOTABLE_P ? NOTABLE : INFO;
    }

    /** The rank of a severity (0 info, 1 notable, 2 significant; −1 unknown). */
    public static int rank(String severity) {
        return ALL.indexOf(severity);
    }
}
