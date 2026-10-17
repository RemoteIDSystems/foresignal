package ai.foresignal.precursor;

import java.util.Random;

/**
 * The series lead–lag test {@value #NAME} {@value #VERSION} (prompt 120): does series A tend to move before series B?
 *
 * <p>Both inputs are pre-whitened residuals on one bucket grid ({@code NaN} where a value is missing or uncovered; see
 * {@link Prewhitening}). The statistic is the largest absolute cross-correlation over lags {@code 1…L} with the
 * antecedent leading ({@code corr(a[t], b[t + lag])}); the best lag and the sign of its correlation are reported. The
 * null distribution comes from {@value #SURROGATES} circular block-shift surrogates of the antecedent: the whole
 * antecedent is shifted circularly by an offset of at least one block, so its autocorrelation is kept and only its
 * alignment with the consequent is broken. The block length is {@code max(7, first lag at which the antecedent's
 * residual autocorrelation falls below 0.2)}. The p-value is {@code (1 + #surrogates ≥ observed) / (SURROGATES + 1)}.
 *
 * <p>The reverse direction (consequent leading) is tested the same way; when it is as strong as the forward direction
 * the result is flagged "co-movement, not precedence". Deterministic: the surrogates are drawn from a generator seeded
 * by the caller (INT-1).
 */
public final class LeadLagCcf {

    /** The test name. */
    public static final String NAME = "leadlag-ccf";

    /** The test version. */
    public static final String VERSION = "1.0.0";

    /** The number of surrogates. */
    public static final int SURROGATES = 500;

    /** The shortest block length. */
    public static final int MIN_BLOCK = 7;

    /** The autocorrelation below which the block length is reached. */
    public static final double ACF_CUTOFF = 0.2;

    /** The fewest overlapping points a test needs. */
    public static final int MIN_OVERLAP = 30;

    /** The fewest pairs a lag's correlation needs. */
    public static final int MIN_PAIRS = 10;

    /** The p-value at or below which a direction counts as strong. */
    public static final double STRONG_P = 0.05;

    private LeadLagCcf() {}

    /**
     * The statistics of one direction.
     *
     * @param bestLag the lag (in buckets) with the largest absolute correlation, or 0 when none could be computed
     * @param correlation the correlation at the best lag ({@code NaN} when none)
     * @param statistic the absolute correlation at the best lag
     * @param pValue the surrogate p-value
     */
    public record Direction(int bestLag, double correlation, double statistic, double pValue) {

        /** True when this direction is strong on its own. */
        public boolean strong() {
            return Double.isFinite(statistic) && pValue <= STRONG_P;
        }
    }

    /**
     * The result.
     *
     * @param overlap the buckets where both series have a value
     * @param maxLag the largest lag tested
     * @param blockLength the surrogate block length
     * @param forward the antecedent-leading direction
     * @param reverse the consequent-leading direction
     * @param coMovement true when the reverse is as strong as the forward direction ("co-movement, not precedence")
     * @param insufficient true when there is too little overlap
     */
    public record Result(int overlap, int maxLag, int blockLength, Direction forward, Direction reverse,
            boolean coMovement, boolean insufficient) {

        /** The sign of the forward correlation: {@code positive}, {@code negative} or null. */
        public String sign() {
            if (!Double.isFinite(forward.correlation()) || forward.correlation() == 0) {
                return null;
            }
            return forward.correlation() > 0 ? "positive" : "negative";
        }
    }

    /**
     * Tests {@code antecedent} leading {@code consequent} over lags {@code 1…maxLag}.
     *
     * @param antecedent the antecedent residuals
     * @param consequent the consequent residuals, on the same grid
     * @param maxLag the largest lag in buckets (at least 1)
     * @param seed the surrogate seed
     * @return the result
     */
    public static Result test(double[] antecedent, double[] consequent, int maxLag, long seed) {
        int n = Math.min(antecedent.length, consequent.length);
        int lags = Math.max(1, Math.min(maxLag, n / 3));
        int overlap = 0;
        for (int i = 0; i < n; i++) {
            if (Double.isFinite(antecedent[i]) && Double.isFinite(consequent[i])) {
                overlap++;
            }
        }
        int block = blockLength(antecedent);
        if (overlap < MIN_OVERLAP || n < 2 * block + 2) {
            Direction none = new Direction(0, Double.NaN, Double.NaN, 1.0);
            return new Result(overlap, lags, block, none, none, false, true);
        }
        Direction forward = direction(antecedent, consequent, lags, block, seed);
        Direction reverse = direction(consequent, antecedent, lags, blockLength(consequent), seed ^ 0x5DEECE66DL);
        boolean coMovement = reverse.strong() && reverse.statistic() >= forward.statistic();
        return new Result(overlap, lags, block, forward, reverse, coMovement, false);
    }

    /** One direction: the observed statistic and its surrogate p-value. */
    static Direction direction(double[] lead, double[] follow, int maxLag, int block, long seed) {
        int n = Math.min(lead.length, follow.length);
        double[] observed = best(lead, follow, maxLag);
        if (!Double.isFinite(observed[1])) {
            return new Direction(0, Double.NaN, Double.NaN, 1.0);
        }
        double stat = Math.abs(observed[1]);
        Random random = new Random(seed);
        int span = Math.max(1, n - 2 * block);
        int atLeast = 0;
        double[] shifted = new double[n];
        for (int s = 0; s < SURROGATES; s++) {
            int offset = block + random.nextInt(span);
            for (int i = 0; i < n; i++) {
                shifted[(i + offset) % n] = lead[i];
            }
            double[] surrogate = best(shifted, follow, maxLag);
            if (Double.isFinite(surrogate[1]) && Math.abs(surrogate[1]) >= stat) {
                atLeast++;
            }
        }
        return new Direction((int) observed[0], observed[1], stat, (1.0 + atLeast) / (SURROGATES + 1.0));
    }

    /** {@code [bestLag, correlation]}: the lag in {@code 1…maxLag} with the largest absolute correlation. */
    static double[] best(double[] lead, double[] follow, int maxLag) {
        int bestLag = 0;
        double bestCorrelation = Double.NaN;
        for (int lag = 1; lag <= maxLag; lag++) {
            double r = correlation(lead, follow, lag);
            if (Double.isFinite(r) && (!Double.isFinite(bestCorrelation) || Math.abs(r) > Math.abs(bestCorrelation))) {
                bestLag = lag;
                bestCorrelation = r;
            }
        }
        return new double[] {bestLag, bestCorrelation};
    }

    /** The Pearson correlation of {@code lead[t]} and {@code follow[t + lag]} over pairs where both are finite. */
    public static double correlation(double[] lead, double[] follow, int lag) {
        int n = Math.min(lead.length, follow.length);
        double sx = 0;
        double sy = 0;
        double sxx = 0;
        double syy = 0;
        double sxy = 0;
        int k = 0;
        for (int t = 0; t + lag < n; t++) {
            double x = lead[t];
            double y = follow[t + lag];
            if (Double.isFinite(x) && Double.isFinite(y)) {
                sx += x;
                sy += y;
                sxx += x * x;
                syy += y * y;
                sxy += x * y;
                k++;
            }
        }
        if (k < MIN_PAIRS) {
            return Double.NaN;
        }
        double cov = sxy - sx * sy / k;
        double vx = sxx - sx * sx / k;
        double vy = syy - sy * sy / k;
        if (vx <= 1e-12 || vy <= 1e-12) {
            return Double.NaN;
        }
        return cov / Math.sqrt(vx * vy);
    }

    /**
     * The surrogate block length: {@code max(7, first lag at which the autocorrelation falls below 0.2)}, at most a
     * quarter of the series.
     *
     * @param residuals the residuals ({@code NaN} where missing)
     * @return the block length in buckets
     */
    public static int blockLength(double[] residuals) {
        int n = residuals.length;
        int cap = Math.max(MIN_BLOCK, n / 4);
        int first = cap;
        for (int lag = 1; lag < cap; lag++) {
            double r = correlation(residuals, residuals, lag);
            if (!Double.isFinite(r) || r < ACF_CUTOFF) {
                first = lag;
                break;
            }
        }
        return Math.max(MIN_BLOCK, Math.min(first, cap));
    }
}
