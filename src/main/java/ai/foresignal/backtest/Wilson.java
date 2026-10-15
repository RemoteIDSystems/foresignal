package ai.foresignal.backtest;

/**
 * The Wilson score interval (prompt 103), the confidence interval for a binomial proportion that
 * behaves at small samples. Descriptive only; never a calibrated probability.
 */
public final class Wilson {

    /** The 95% z score. */
    public static final double Z95 = 1.959963984540054;

    private Wilson() {}

    /**
     * The 95% Wilson interval of {@code successes / total}.
     *
     * @param successes the successes
     * @param total the trials (0 returns [0, 1])
     */
    public static double[] interval(long successes, long total) {
        if (total <= 0) {
            return new double[] {0, 1};
        }
        double p = (double) successes / total;
        double z2 = Z95 * Z95;
        double denom = 1 + z2 / total;
        double centre = (p + z2 / (2 * total)) / denom;
        double margin = Z95 * Math.sqrt((p * (1 - p) + z2 / (4 * total)) / total) / denom;
        return new double[] {Math.max(0, centre - margin), Math.min(1, centre + margin)};
    }
}
