package ai.foresignal.precursor;

import ai.foresignal.analytics.math.MultipleTesting;

/**
 * False-discovery control of one precursor batch (prompt 120, FDR-1): Benjamini–Hochberg at {@code q} across the
 * batch, or Benjamini–Yekutieli (valid under arbitrary dependence) when the batch contains overlapping series pairs,
 * that is, when one series appears in more than one pair. Raw and adjusted p-values are both kept.
 */
public final class Fdr {

    /** The default false-discovery level. */
    public static final double DEFAULT_Q = 0.10;

    /** Benjamini–Hochberg. */
    public static final String BH = "BH";

    /** Benjamini–Yekutieli. */
    public static final String BY = "BY";

    private Fdr() {}

    /**
     * The adjusted batch.
     *
     * @param method {@value #BH} or {@value #BY}
     * @param q the level
     * @param raw the raw p-values, in input order
     * @param adjusted the adjusted p-values, in input order
     * @param rejected true where the adjusted p-value is at most {@code q} (a survivor)
     */
    public record Adjusted(String method, double q, double[] raw, double[] adjusted, boolean[] rejected) {

        /** The number of survivors. */
        public int survivors() {
            int n = 0;
            for (boolean r : rejected) {
                if (r) {
                    n++;
                }
            }
            return n;
        }
    }

    /**
     * Adjusts a batch.
     *
     * @param pValues the raw p-values
     * @param overlapping true when the batch contains overlapping series pairs (Benjamini–Yekutieli)
     * @param q the level
     * @return the adjusted batch
     */
    public static Adjusted adjust(double[] pValues, boolean overlapping, double q) {
        double[] raw = pValues == null ? new double[0] : pValues.clone();
        double[] adjusted = overlapping ? MultipleTesting.benjaminiYekutieli(raw) : MultipleTesting.benjaminiHochberg(raw);
        return new Adjusted(overlapping ? BY : BH, q, raw, adjusted, MultipleTesting.rejected(adjusted, q));
    }
}
