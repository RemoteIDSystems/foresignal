package ai.foresignal.hypothesis;

import java.util.List;
import java.util.Map;

/**
 * Expected information gain of pending indicators ({@code InformationValue} {@value #VERSION}, prompt 125 §6a,
 * Intelligence-Design §6.6): {@code EIG = H(P) − Σ_o P(o) · H(P | o)} over the outcomes observed and absent, with
 * {@code H} the Shannon entropy in nats.
 *
 * <p>{@code q_h}, the probability that the signal appears when {@code h} is true, comes from the indicator's LR and the
 * base rate {@code s} of the signal (its probability when the hypothesis says nothing about it): the odds of the
 * signal are multiplied by the LR, {@code q_h = s·LR / (s·LR + 1 − s)}. The signal is seen only when it occurs and is
 * detected, so {@code P(observed) = Σ_h P(h) · q_h · p_detect}. An exclusive set uses its full distribution; an
 * independent set sums the binary gains of its hypotheses (each against its complement at {@code q = s}). Pure.
 */
public final class InformationValue {

    /** The version. */
    public static final String VERSION = "1.0.0";

    /** The version label. */
    public static final String LABEL = "InformationValue " + VERSION;

    /** The signal base rate used when the indicator states none. */
    public static final double DEFAULT_SIGNAL_BASE_RATE = 0.5;

    private InformationValue() {}

    /**
     * The gain of one indicator.
     *
     * @param eig the expected information gain at the actual {@code p_detect}
     * @param potentialEig the gain at {@code p_detect = 1}
     * @param pObserved the probability of observing it at the actual {@code p_detect}
     */
    public record Gain(double eig, double potentialEig, double pObserved) {}

    /** Shannon entropy in nats (zero terms skipped). */
    public static double entropy(double[] p) {
        double h = 0;
        for (double v : p) {
            if (v > 0) {
                h -= v * Math.log(v);
            }
        }
        return h;
    }

    /** The binary entropy of {@code p} in nats. */
    public static double binaryEntropy(double p) {
        return entropy(new double[] {p, 1 - p});
    }

    /** {@code q = s·LR / (s·LR + 1 − s)}. */
    public static double q(double signalBaseRate, double lr) {
        double s = Math.max(1e-9, Math.min(1 - 1e-9, signalBaseRate));
        return s * lr / (s * lr + 1 - s);
    }

    /**
     * The gain of an indicator.
     *
     * @param exclusive true for an exclusive set
     * @param ids the hypothesis ids in set order
     * @param probabilities the current probability per id
     * @param lr the indicator's effective LR per id (missing means neutral)
     * @param signalBaseRate the signal's base rate {@code s}
     * @param pDetect the detection probability (null counts as 1)
     */
    public static Gain gain(boolean exclusive, List<String> ids, Map<String, Double> probabilities,
            Map<String, Double> lr, double signalBaseRate, Double pDetect) {
        double pd = pDetect == null ? 1.0 : Math.max(0, Math.min(1, pDetect));
        double[] p = new double[ids.size()];
        double[] q = new double[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            p[i] = probabilities.getOrDefault(ids.get(i), 0.0);
            q[i] = q(signalBaseRate, lr.getOrDefault(ids.get(i), 1.0));
        }
        double base = q(signalBaseRate, 1.0);
        double eig = exclusive ? exclusiveGain(p, q, pd) : independentGain(p, q, base, pd);
        double potential = exclusive ? exclusiveGain(p, q, 1.0) : independentGain(p, q, base, 1.0);
        double pObs = 0;
        if (exclusive) {
            for (int i = 0; i < p.length; i++) {
                pObs += p[i] * q[i] * pd;
            }
        } else {
            for (int i = 0; i < p.length; i++) {
                pObs = Math.max(pObs, (p[i] * q[i] + (1 - p[i]) * base) * pd);
            }
        }
        return new Gain(Math.max(0, eig), Math.max(0, potential), pObs);
    }

    static double exclusiveGain(double[] p, double[] q, double pd) {
        double sum = 0;
        for (double v : p) {
            sum += v;
        }
        double[] prior = new double[p.length];
        for (int i = 0; i < p.length; i++) {
            prior[i] = sum > 0 ? p[i] / sum : 0;
        }
        double pObs = 0;
        for (int i = 0; i < p.length; i++) {
            pObs += prior[i] * q[i] * pd;
        }
        double[] obs = new double[p.length];
        double[] abs = new double[p.length];
        for (int i = 0; i < p.length; i++) {
            obs[i] = pObs > 0 ? prior[i] * q[i] * pd / pObs : 0;
            abs[i] = pObs < 1 ? prior[i] * (1 - q[i] * pd) / (1 - pObs) : 0;
        }
        return entropy(prior) - pObs * entropy(obs) - (1 - pObs) * entropy(abs);
    }

    static double independentGain(double[] p, double[] q, double base, double pd) {
        double total = 0;
        for (int i = 0; i < p.length; i++) {
            double pObs = (p[i] * q[i] + (1 - p[i]) * base) * pd;
            double postObs = pObs > 0 ? p[i] * q[i] * pd / pObs : p[i];
            double postAbs = pObs < 1 ? p[i] * (1 - q[i] * pd) / (1 - pObs) : p[i];
            total += binaryEntropy(p[i]) - pObs * binaryEntropy(postObs) - (1 - pObs) * binaryEntropy(postAbs);
        }
        return total;
    }
}
