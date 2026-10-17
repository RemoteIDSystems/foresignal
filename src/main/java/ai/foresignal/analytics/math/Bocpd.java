package ai.foresignal.analytics.math;

import org.apache.commons.numbers.gamma.LogGamma;

/**
 * Bayesian online change-point detection (Adams and MacKay 2007; prompt 119, {@code changepoint-bocpd}) with a
 * constant hazard and run lengths truncated at {@code maxRunLength} (the mass beyond is folded into the last run
 * length). Counts use the Poisson–Gamma model (negative-binomial predictive), continuous series the Normal–Gamma model
 * (Student-t predictive). Missing values ({@code NaN}) are skipped. The output is the run-length posterior after the
 * last observation, from which {@link Result#changeWithin(int)} reads the probability of a change in the last
 * {@code k} observations.
 */
public final class Bocpd {

    /** The conjugate model. */
    public enum Model {
        /** Poisson observations with a Gamma prior on the rate. */
        POISSON_GAMMA,
        /** Normal observations with a Normal–Gamma prior on mean and precision. */
        NORMAL_GAMMA
    }

    /**
     * The detector parameters.
     *
     * @param model the model
     * @param hazard the constant hazard (default 1/250)
     * @param maxRunLength the run-length truncation (default 300)
     * @param priorLocation the prior mean (rate for Poisson–Gamma, location for Normal–Gamma)
     * @param priorScale the prior scale of the Normal–Gamma observations (ignored for Poisson–Gamma)
     * @param priorWeight the prior's weight in pseudo-observations
     */
    public record Params(Model model, double hazard, int maxRunLength, double priorLocation, double priorScale,
            double priorWeight) {

        /** Validates the hazard and truncation. */
        public Params {
            if (!(hazard > 0 && hazard < 1)) {
                throw new IllegalArgumentException("hazard must be in (0, 1)");
            }
            if (maxRunLength < 1) {
                throw new IllegalArgumentException("maxRunLength must be at least 1");
            }
            priorWeight = priorWeight > 0 ? priorWeight : 1;
            priorScale = priorScale > 0 ? priorScale : 1;
        }
    }

    /**
     * The posterior after the last observation.
     *
     * @param posterior {@code P(r = i)} for run lengths {@code 0 … n}
     * @param observations the observations used
     */
    public record Result(double[] posterior, int observations) {

        /** {@code P(r < k)}: the probability that a change happened within the last {@code k} observations. */
        public double changeWithin(int k) {
            double sum = 0;
            for (int i = 0; i < posterior.length && i < k; i++) {
                sum += posterior[i];
            }
            return Math.max(0, Math.min(1, sum));
        }

        /** The most probable run length. */
        public int mapRunLength() {
            int best = 0;
            for (int i = 1; i < posterior.length; i++) {
                if (posterior[i] > posterior[best]) {
                    best = i;
                }
            }
            return best;
        }
    }

    private Bocpd() {}

    /** Runs the detector over {@code x}. */
    public static Result run(double[] x, Params p) {
        int cap = p.maxRunLength() + 1;
        double[] logR = {0.0};
        double[][] stats = {prior(p)};
        double logH = Math.log(p.hazard());
        double log1mH = Math.log1p(-p.hazard());
        int n = 0;
        for (double value : x) {
            if (!Double.isFinite(value)) {
                continue;
            }
            double obs = p.model() == Model.POISSON_GAMMA ? Math.max(0, Math.rint(value)) : value;
            int len = logR.length;
            double[] pred = new double[len];
            for (int r = 0; r < len; r++) {
                pred[r] = logPredictive(p.model(), stats[r], obs);
            }
            int newLen = Math.min(len + 1, cap);
            double[] next = new double[newLen];
            double[][] nextStats = new double[newLen][];
            double cp = Double.NEGATIVE_INFINITY;
            for (int r = 0; r < len; r++) {
                cp = logAdd(cp, logR[r] + pred[r] + logH);
            }
            next[0] = cp;
            nextStats[0] = prior(p);
            for (int r = 0; r < len; r++) {
                double growth = logR[r] + pred[r] + log1mH;
                int target = Math.min(r + 1, newLen - 1);
                if (target == r + 1 && nextStats[target] == null) {
                    next[target] = growth;
                    nextStats[target] = update(p.model(), stats[r], obs);
                } else {
                    next[target] = logAdd(next[target], growth);
                    if (nextStats[target] == null) {
                        nextStats[target] = update(p.model(), stats[r], obs);
                    }
                }
            }
            double norm = Double.NEGATIVE_INFINITY;
            for (double v : next) {
                norm = logAdd(norm, v);
            }
            for (int r = 0; r < newLen; r++) {
                next[r] -= norm;
            }
            logR = next;
            stats = nextStats;
            n++;
        }
        double[] posterior = new double[logR.length];
        for (int r = 0; r < logR.length; r++) {
            posterior[r] = Math.exp(logR[r]);
        }
        return new Result(posterior, n);
    }

    /** The prior sufficient statistics. */
    static double[] prior(Params p) {
        if (p.model() == Model.POISSON_GAMMA) {
            double beta = p.priorWeight();
            double alpha = Math.max(1e-3, p.priorLocation()) * beta;
            return new double[] {alpha, beta};
        }
        double kappa = p.priorWeight();
        double alpha = 1.0;
        double beta = p.priorScale() * p.priorScale() * alpha;
        return new double[] {p.priorLocation(), kappa, alpha, beta};
    }

    /** The sufficient statistics after observing {@code x}. */
    static double[] update(Model model, double[] s, double x) {
        if (model == Model.POISSON_GAMMA) {
            return new double[] {s[0] + x, s[1] + 1};
        }
        double mu = s[0];
        double kappa = s[1];
        double alpha = s[2];
        double beta = s[3];
        return new double[] {(kappa * mu + x) / (kappa + 1), kappa + 1, alpha + 0.5,
            beta + kappa * (x - mu) * (x - mu) / (2 * (kappa + 1))};
    }

    /** The log posterior predictive density of {@code x}. */
    static double logPredictive(Model model, double[] s, double x) {
        if (model == Model.POISSON_GAMMA) {
            double alpha = s[0];
            double beta = s[1];
            return LogGamma.value(x + alpha) - LogGamma.value(alpha) - LogGamma.value(x + 1)
                    + alpha * Math.log(beta / (beta + 1)) - x * Math.log(beta + 1);
        }
        double mu = s[0];
        double kappa = s[1];
        double alpha = s[2];
        double beta = s[3];
        double nu = 2 * alpha;
        double scale2 = beta * (kappa + 1) / (alpha * kappa);
        double d = x - mu;
        return LogGamma.value((nu + 1) / 2) - LogGamma.value(nu / 2) - 0.5 * Math.log(nu * Math.PI * scale2)
                - (nu + 1) / 2 * Math.log1p(d * d / (nu * scale2));
    }

    static double logAdd(double a, double b) {
        if (a == Double.NEGATIVE_INFINITY) {
            return b;
        }
        if (b == Double.NEGATIVE_INFINITY) {
            return a;
        }
        double max = Math.max(a, b);
        return max + Math.log1p(Math.exp(-Math.abs(a - b)));
    }
}
