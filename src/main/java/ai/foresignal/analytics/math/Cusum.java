package ai.foresignal.analytics.math;

import java.util.ArrayList;
import java.util.List;

/**
 * CUSUM change detection (prompt 119, Intelligence-Design §5.2), {@code changepoint-cusum}.
 *
 * <p>Counts use the Poisson form {@code S = max(0, S + x − k)} with the reference value
 * {@code k = (μ₁ − μ₀) / ln(μ₁/μ₀)} for a shift to {@code μ₁ = μ₀ (1 ± minRelShift)} against a time-varying in-control
 * mean {@code μ₀}. It is accumulated in log-likelihood-ratio units, {@code x ln(μ₁/μ₀) − (μ₁ − μ₀)}, which is the same
 * statistic scaled by the constant {@code |ln(1 ± minRelShift)|}, so one alarm threshold serves every {@code μ₀}.
 * Overdispersed counts use the negative-binomial log-likelihood ratio with a fixed size. Continuous series use the
 * standardized form {@code S = max(0, S ± z − δ/2)} on robust residuals {@code z = (x − μ₀)/σ}.
 *
 * <p>A coverage gap while a candidate is accumulating ({@code S > 0}) cancels it ({@link Run#canceled}); after an
 * alarm the statistic restarts from zero.
 */
public final class Cusum {

    /** The count or continuous model. */
    public enum Model {
        /** Poisson counts. */
        POISSON,
        /** Overdispersed counts with a fixed negative-binomial size. */
        NEG_BIN,
        /** Standardized continuous residuals. */
        GAUSSIAN
    }

    /**
     * The detector parameters.
     *
     * @param model the model
     * @param direction {@code +1} for upward shifts, {@code −1} for downward
     * @param minRelShift the relative shift of the count forms (default 0.5)
     * @param gaussianShift the shift of the standardized form in standard deviations (default 1)
     * @param negBinSize the negative-binomial size of {@link Model#NEG_BIN}
     */
    public record Params(Model model, int direction, double minRelShift, double gaussianShift, double negBinSize) {

        /** Validates the direction and shifts. */
        public Params {
            if (direction != 1 && direction != -1) {
                throw new IllegalArgumentException("direction must be +1 or -1");
            }
            if (!(minRelShift > 0) || (direction < 0 && minRelShift >= 1)) {
                throw new IllegalArgumentException("minRelShift must be in (0, 1) downward and > 0 upward");
            }
            if (!(gaussianShift > 0)) {
                throw new IllegalArgumentException("gaussianShift must be positive");
            }
        }

        /** The ratio {@code μ₁/μ₀}. */
        public double ratio() {
            return 1 + direction * minRelShift;
        }

        /** A stable key of the parameters, for threshold caches. */
        public String key() {
            return model + "|" + direction + "|" + minRelShift + "|" + gaussianShift + "|"
                    + (model == Model.NEG_BIN ? round2(negBinSize) : 0);
        }
    }

    /**
     * One alarm.
     *
     * @param alarmIndex the bucket at which {@code S} reached {@code h}
     * @param changeIndex the estimated change: the bucket after the last {@code S = 0} before the alarm
     */
    public record Alarm(int alarmIndex, int changeIndex) {

        /** The detection delay in buckets. */
        public int delay() {
            return alarmIndex - changeIndex;
        }
    }

    /**
     * The result of a pass.
     *
     * @param alarms the alarms in time order
     * @param canceled the buckets at which a gap canceled an accumulating candidate
     * @param finalS the statistic after the last bucket
     * @param openChangeIndex the start of the candidate accumulating at the end, or −1 when {@code S = 0}
     */
    public record Run(List<Alarm> alarms, List<Integer> canceled, double finalS, int openChangeIndex) {}

    private Cusum() {}

    /** The Poisson reference value {@code k = (μ₁ − μ₀) / ln(μ₁/μ₀)}. */
    public static double referenceValue(double mu0, double mu1) {
        if (mu0 <= 0 || mu1 <= 0 || mu0 == mu1) {
            return mu0;
        }
        return (mu1 - mu0) / Math.log(mu1 / mu0);
    }

    /**
     * The log-likelihood-ratio increment of one observation.
     *
     * @param x the observation
     * @param mu0 the in-control mean (count forms) or location (continuous form)
     * @param sigma the scale of the continuous form (ignored by the count forms)
     */
    public static double increment(Params p, double x, double mu0, double sigma) {
        switch (p.model()) {
            case GAUSSIAN -> {
                double z = (x - mu0) / sigma;
                return p.direction() * z - p.gaussianShift() / 2;
            }
            case NEG_BIN -> {
                double m0 = Math.max(mu0, 1e-9);
                double m1 = m0 * p.ratio();
                double s = p.negBinSize();
                return x * Math.log(m1 * (s + m0) / (m0 * (s + m1))) + s * Math.log((s + m0) / (s + m1));
            }
            default -> {
                double m0 = Math.max(mu0, 1e-9);
                double r = p.ratio();
                return x * Math.log(r) - m0 * (r - 1);
            }
        }
    }

    /**
     * Runs the CUSUM over a series.
     *
     * @param x the observations ({@code NaN} where missing)
     * @param mu0 the in-control mean or location per bucket ({@code NaN} while the baseline is learning)
     * @param sigma the continuous scale per bucket (may be null for the count forms)
     * @param gap true where coverage failed or the point is missing
     * @param h the alarm threshold, in log-likelihood-ratio units
     */
    public static Run run(double[] x, double[] mu0, double[] sigma, boolean[] gap, Params p, double h) {
        List<Alarm> alarms = new ArrayList<>();
        List<Integer> canceled = new ArrayList<>();
        double s = 0;
        int lastZero = -1;
        for (int t = 0; t < x.length; t++) {
            boolean missing = (gap != null && gap[t]) || !Double.isFinite(x[t]) || !Double.isFinite(mu0[t])
                    || (p.model() == Model.GAUSSIAN && (sigma == null || !(sigma[t] > 0)));
            if (missing) {
                if (s > 0) {
                    canceled.add(t);
                }
                s = 0;
                lastZero = t;
                continue;
            }
            s = Math.max(0, s + increment(p, x[t], mu0[t], sigma == null ? 1 : sigma[t]));
            if (s == 0) {
                lastZero = t;
            } else if (s >= h) {
                alarms.add(new Alarm(t, lastZero + 1));
                s = 0;
                lastZero = t;
            }
        }
        return new Run(List.copyOf(alarms), List.copyOf(canceled), s, s > 0 ? lastZero + 1 : -1);
    }

    static double round2(double value) {
        if (!(value > 0) || !Double.isFinite(value)) {
            return value;
        }
        double magnitude = Math.pow(10, Math.floor(Math.log10(value)) - 1);
        return Math.round(value / magnitude) * magnitude;
    }
}
