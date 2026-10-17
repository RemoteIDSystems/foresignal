package ai.foresignal.backtest;

/**
 * The Wilson score interval (prompt 103). Since prompt 119 the implementation lives in
 * {@link ai.foresignal.analytics.math.Wilson}; this class delegates so existing callers keep working.
 */
public final class Wilson {

    /** The 95% z score. */
    public static final double Z95 = ai.foresignal.analytics.math.Wilson.Z95;

    private Wilson() {}

    /**
     * The 95% Wilson interval of {@code successes / total}.
     *
     * @param successes the successes
     * @param total the trials (0 returns [0, 1])
     */
    public static double[] interval(long successes, long total) {
        return ai.foresignal.analytics.math.Wilson.interval(successes, total);
    }
}
