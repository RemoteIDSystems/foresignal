package ai.foresignal.forecast.ensemble;

import ai.foresignal.forecast.Ledger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The ensemble's arithmetic (prompt 122, Intelligence-Design §8.4), pure and deterministic: track-record weights,
 * log-odds pooling, quantile averaging, LLM caps, Platt recalibration and the extremizing factor.
 *
 * <ul>
 *   <li><b>Weights</b>: {@code wᵢ ∝ exp(−η · relBrierᵢ)} (η = 2) normalized over the present members, then shrunk
 *       toward equal weights by {@code nᵢ ÷ (nᵢ + 20)}: {@code w'ᵢ = λᵢ wᵢ + (1 − λᵢ) / K}, renormalized.</li>
 *   <li><b>Binary</b>: {@code p* = σ(a · Σ wᵢ logit pᵢ)}, inputs clipped to [0.001, 0.999] before the logit, output
 *       capped to [0.01, 0.99]; {@code a = 1} unless extremizing is enabled.</li>
 *   <li><b>Multiple / ordered</b>: the same per label, then renormalized.</li>
 *   <li><b>Numeric</b>: the weighted mean of each quantile level (Vincentization), then sorted so it stays monotone.</li>
 *   <li><b>LLM caps</b>: an {@code llm} forecaster's probability is clamped to [0.03, 0.97] before recalibration.</li>
 *   <li><b>Platt</b>: {@code p' = σ(a · logit p + b)}, fitted by Newton–Raphson on the log likelihood.</li>
 * </ul>
 */
public final class EnsembleMath {

    /** The weight temperature η. */
    public static final double ETA = 2.0;

    /** The shrinkage constant: {@code n ÷ (n + 20)}. */
    public static final double SHRINK = 20.0;

    /** The lower LLM cap. */
    public static final double LLM_LOW = 0.03;

    /** The upper LLM cap. */
    public static final double LLM_HIGH = 0.97;

    /** The lower output cap. */
    public static final double OUT_LOW = 0.01;

    /** The upper output cap. */
    public static final double OUT_HIGH = 0.99;

    /** The input clip before a logit. */
    static final double EPS = 0.001;

    private EnsembleMath() {}

    /** The logistic function. */
    public static double sigmoid(double x) {
        return x >= 0 ? 1.0 / (1.0 + Math.exp(-x)) : Math.exp(x) / (1.0 + Math.exp(x));
    }

    /** The log-odds of {@code p}, clipped to [0.001, 0.999]. */
    public static double logit(double p) {
        double c = Math.max(EPS, Math.min(1 - EPS, p));
        return Math.log(c / (1 - c));
    }

    /** {@code p} clamped to [0.03, 0.97] (LLM forecasters). */
    public static double capLlm(double p) {
        return Math.max(LLM_LOW, Math.min(LLM_HIGH, p));
    }

    /** {@code p} capped to [0.01, 0.99] (ensemble output). */
    public static double capOutput(double p) {
        return Math.max(OUT_LOW, Math.min(OUT_HIGH, p));
    }

    /**
     * A member's track record.
     *
     * @param relBrier its mean Brier minus the per-question median (0 when unknown)
     * @param n the resolved questions behind it
     */
    public record Track(double relBrier, int n) {}

    /**
     * The weights of the present members, summing to 1.
     *
     * @param tracks one per member
     * @param eta the temperature
     * @param shrink the shrinkage constant
     */
    public static double[] weights(List<Track> tracks, double eta, double shrink) {
        int k = tracks.size();
        double[] out = new double[k];
        if (k == 0) {
            return out;
        }
        double[] raw = new double[k];
        double sum = 0;
        for (int i = 0; i < k; i++) {
            Track t = tracks.get(i);
            raw[i] = Math.exp(-eta * (Double.isFinite(t.relBrier()) ? t.relBrier() : 0.0));
            sum += raw[i];
        }
        double total = 0;
        for (int i = 0; i < k; i++) {
            double lambda = tracks.get(i).n() / (tracks.get(i).n() + shrink);
            out[i] = lambda * (raw[i] / sum) + (1 - lambda) / k;
            total += out[i];
        }
        for (int i = 0; i < k; i++) {
            out[i] /= total;
        }
        return out;
    }

    /** {@code Σ wᵢ logit pᵢ} (weights already sum to 1). */
    public static double aggregateLogit(double[] p, double[] w) {
        double x = 0;
        for (int i = 0; i < p.length; i++) {
            x += w[i] * logit(p[i]);
        }
        return x;
    }

    /** The pooled binary probability {@code σ(a · Σ wᵢ logit pᵢ)}, capped to [0.01, 0.99]. */
    public static double combineBinary(double[] p, double[] w, double a) {
        return capOutput(sigmoid(a * aggregateLogit(p, w)));
    }

    /**
     * The pooled distribution over {@code labels}: per label the weighted mean of log-odds, then renormalized.
     *
     * @param members one distribution per member
     * @param w the weights
     * @param labels the labels
     */
    public static Map<String, Double> combineDistribution(List<Map<String, Double>> members, double[] w,
            List<String> labels) {
        Map<String, Double> out = new LinkedHashMap<>();
        double sum = 0;
        for (String label : labels) {
            double[] p = new double[members.size()];
            for (int i = 0; i < members.size(); i++) {
                Double v = members.get(i).get(label);
                p[i] = v == null ? 0.0 : v;
            }
            double value = sigmoid(aggregateLogit(p, w));
            out.put(label, value);
            sum += value;
        }
        for (String label : labels) {
            out.put(label, out.get(label) / sum);
        }
        return out;
    }

    /**
     * The weighted quantile average over the levels every member carries, sorted to stay monotone.
     *
     * @param members one quantile map per member
     * @param w the weights
     */
    public static Map<String, Double> combineQuantiles(List<Map<String, Double>> members, double[] w) {
        List<String> levels = new ArrayList<>();
        for (String level : Ledger.QUANTILES) {
            boolean all = !members.isEmpty();
            for (Map<String, Double> m : members) {
                all &= m.get(level) != null;
            }
            if (all) {
                levels.add(level);
            }
        }
        double[] values = new double[levels.size()];
        for (int j = 0; j < levels.size(); j++) {
            double v = 0;
            for (int i = 0; i < members.size(); i++) {
                v += w[i] * members.get(i).get(levels.get(j));
            }
            values[j] = v;
        }
        Arrays.sort(values);
        Map<String, Double> out = new LinkedHashMap<>();
        for (int j = 0; j < levels.size(); j++) {
            out.put(levels.get(j), values[j]);
        }
        return out;
    }

    /** Platt recalibration {@code σ(a · logit p + b)}. */
    public static double platt(double a, double b, double p) {
        return sigmoid(a * logit(p) + b);
    }

    /**
     * Fits Platt coefficients by Newton–Raphson on the Bernoulli log likelihood of {@code o} given
     * {@code σ(a · logit p + b)}, starting from the identity ({@code a = 1, b = 0}); a tiny ridge keeps a separable
     * sample finite.
     *
     * @return {@code {a, b}}
     */
    public static double[] fitPlatt(double[] p, double[] o) {
        double a = 1.0;
        double b = 0.0;
        double ridge = 1e-6;
        for (int iter = 0; iter < 100; iter++) {
            double ga = -ridge * (a - 1);
            double gb = -ridge * b;
            double haa = -ridge;
            double hab = 0;
            double hbb = -ridge;
            for (int i = 0; i < p.length; i++) {
                double x = logit(p[i]);
                double q = sigmoid(a * x + b);
                double r = o[i] - q;
                ga += r * x;
                gb += r;
                double v = q * (1 - q);
                haa -= v * x * x;
                hab -= v * x;
                hbb -= v;
            }
            double det = haa * hbb - hab * hab;
            if (Math.abs(det) < 1e-12) {
                break;
            }
            double da = (hbb * ga - hab * gb) / det;
            double db = (haa * gb - hab * ga) / det;
            a -= da;
            b -= db;
            if (Math.abs(da) < 1e-9 && Math.abs(db) < 1e-9) {
                break;
            }
        }
        return new double[] {a, b};
    }

    /**
     * Fits the extremizing factor {@code a} of {@code σ(a · x)} by one-dimensional Newton–Raphson on the log
     * likelihood, from {@code a = 1}, kept within [0.2, 5].
     *
     * @param x the aggregate logits
     * @param o the outcomes
     */
    public static double fitExtremizing(double[] x, double[] o) {
        double a = 1.0;
        for (int iter = 0; iter < 100; iter++) {
            double g = 0;
            double h = -1e-9;
            for (int i = 0; i < x.length; i++) {
                double q = sigmoid(a * x[i]);
                g += (o[i] - q) * x[i];
                h -= q * (1 - q) * x[i] * x[i];
            }
            double step = g / h;
            a = Math.max(0.2, Math.min(5.0, a - step));
            if (Math.abs(step) < 1e-9) {
                break;
            }
        }
        return a;
    }

    /** The mean clipped log score of {@code σ(a · x)} against the outcomes. */
    public static double meanLogScore(double[] x, double[] o, double a) {
        double sum = 0;
        for (int i = 0; i < x.length; i++) {
            double p = capOutput(sigmoid(a * x[i]));
            sum += -Math.log(o[i] == 1.0 ? p : 1 - p);
        }
        return x.length == 0 ? Double.NaN : sum / x.length;
    }
}
