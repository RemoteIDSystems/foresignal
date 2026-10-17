package ai.foresignal.hypothesis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The deterministic Bayesian updater of hypothesis sets ({@value #NAME} {@value #VERSION}, prompt 125 §4,
 * Intelligence-Design §6.4). A pure function of the set's current probabilities, the new drivers and what was already
 * applied today; it never reads the clock or the database.
 *
 * <ol>
 *   <li><b>Log-odds.</b> Each driver adds {@code ln(LR)} per hypothesis it carries a likelihood for. An independent
 *       set works on each hypothesis's logit; an exclusive set on log-probabilities, renormalized by softmax, so
 *       evidence that is neutral to every hypothesis changes nothing and the {@code other} hypothesis (neutral to
 *       most indicators) takes the residual mass.</li>
 *   <li><b>Correlated evidence.</b> An evidence driver whose evidence key (its 116 evidence group) was already applied
 *       is suppressed ({@code same-evidence-group}). Within one signal family only the largest |ln LR| per hypothesis
 *       per UTC day counts: a weaker one is suppressed ({@code same-family-day}), a stronger one applies only its
 *       excess over the one already counted.</li>
 *   <li><b>Damping.</b> The net evidence change per hypothesis per UTC day stays within ±{@value #DAILY_LIMIT}
 *       logits; the excess is cut proportionally from that revision's evidence drivers ({@code damped}).</li>
 *   <li><b>Bounds.</b> While the set is active probabilities stay in [{@value #P_MIN}, {@value #P_MAX}]; a
 *       speculative link never exceeds its cap unless an operator override is logged ({@code capped}). In an exclusive
 *       set the bounds are applied by a box-constrained renormalization, so the probabilities still sum to 1.</li>
 * </ol>
 *
 * Base-rate refreshes, reviews and overrides are non-evidence drivers: they are neither suppressed nor damped.
 */
public final class HypothesisUpdater {

    /** The updater name (also the ledger forecaster id). */
    public static final String NAME = "indicator-bayes";

    /** The updater version. */
    public static final String VERSION = "1.0.0";

    /** The version label stored on revisions. */
    public static final String LABEL = NAME + " " + VERSION;

    /** The largest net evidence change per hypothesis per UTC day, in logits. */
    public static final double DAILY_LIMIT = 1.5;

    /** The lowest probability while active. */
    public static final double P_MIN = 0.02;

    /** The highest probability while active. */
    public static final double P_MAX = 0.98;

    /** Same evidence group already applied. */
    public static final String SAME_GROUP = "same-evidence-group";

    /** A stronger driver of the same family already counted today. */
    public static final String SAME_FAMILY = "same-family-day";

    /** Cut by the daily damping. */
    public static final String DAMPED = "damped";

    /** Held at a speculative-link cap. */
    public static final String CAPPED = "capped";

    private HypothesisUpdater() {}

    /**
     * A hypothesis as the updater sees it.
     *
     * @param id the hypothesis id
     * @param label its label
     * @param probability its current probability
     * @param cap its speculative-link cap, or null
     * @param capOverridden true when an operator override lifts the cap
     */
    public record Hyp(String id, String label, double probability, Double cap, boolean capOverridden) {}

    /**
     * One driver of a revision.
     *
     * @param kind {@code indicator}, {@code evidence}, {@code base-rate}, {@code review} or {@code operator}
     * @param id the indicator, evidence or base-rate id, or null
     * @param ref the object reference, or null
     * @param evidenceKey the evidence key (116 group) of an evidence driver, or null
     * @param signalFamily the signal family of an evidence driver, or null
     * @param lr the effective LR per hypothesis id (hypotheses without an entry are neutral)
     * @param pDetect the detection probability applied to an absence, or null
     * @param evidence true when the driver is evidence (suppression and damping apply)
     */
    public record Driver(String kind, String id, String ref, String evidenceKey, String signalFamily,
            Map<String, Double> lr, Double pDetect, boolean evidence) {

        /** Copies the LR map. */
        public Driver {
            lr = lr == null ? Map.of() : Map.copyOf(lr);
        }
    }

    /**
     * What earlier revisions of the set already applied.
     *
     * @param evidenceKeys the evidence keys already applied (ever)
     * @param familyDay the |ln LR| counted today per {@code family|hypothesisId} (signed)
     * @param dayNet the net evidence change applied today per hypothesis id
     */
    public record History(Set<String> evidenceKeys, Map<String, Double> familyDay, Map<String, Double> dayNet) {

        /** Nothing applied yet. */
        public static final History EMPTY = new History(Set.of(), Map.of(), Map.of());

        /** Copies the members. */
        public History {
            evidenceKeys = evidenceKeys == null ? Set.of() : Set.copyOf(evidenceKeys);
            familyDay = familyDay == null ? Map.of() : Map.copyOf(familyDay);
            dayNet = dayNet == null ? Map.of() : Map.copyOf(dayNet);
        }
    }

    /**
     * One driver's contribution to one hypothesis (the {@code RevisionDriver} schema).
     *
     * @param kind the driver kind
     * @param id the driver id, or null
     * @param ref the object reference, or null
     * @param hypothesisId the hypothesis
     * @param signalFamily the signal family, or null
     * @param evidenceKey the evidence key, or null
     * @param lr the LR, or null
     * @param lnLr {@code ln(LR)}, or null
     * @param pDetect the detection probability, or null
     * @param applied the log-odds change applied after suppression and damping
     * @param suppressed true when nothing was applied
     * @param suppressionReason why it was suppressed, damped or capped, or null
     * @param evidence true when it came from an evidence driver (suppression and damping apply)
     */
    public record Contribution(String kind, String id, String ref, String hypothesisId, String signalFamily,
            String evidenceKey, Double lr, Double lnLr, Double pDetect, double applied, boolean suppressed,
            String suppressionReason, boolean evidence) {

        /** The {@code RevisionDriver} map (plus the evidence key, kept for later revisions). */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("kind", kind);
            out.put("id", id);
            out.put("ref", ref);
            out.put("hypothesisId", hypothesisId);
            out.put("signalFamily", signalFamily);
            out.put("lr", lr);
            out.put("lnLr", lnLr);
            out.put("pDetect", pDetect);
            out.put("applied", applied);
            out.put("suppressed", suppressed);
            out.put("suppressionReason", suppressionReason);
            out.put("evidenceKey", evidenceKey);
            out.put("evidence", evidence);
            return out;
        }

        Contribution withApplied(double value, String reason) {
            return new Contribution(kind, id, ref, hypothesisId, signalFamily, evidenceKey, lr, lnLr, pDetect, value,
                    suppressed, reason, evidence);
        }
    }

    /**
     * The result of an update.
     *
     * @param before the probabilities before, by hypothesis id in set order
     * @param after the probabilities after
     * @param contributions every contribution, suppressed ones included
     * @param capped the hypotheses held at their cap
     */
    public record Result(Map<String, Double> before, Map<String, Double> after, List<Contribution> contributions,
            Set<String> capped) {

        /** True when some probability moved by more than 1e-9. */
        public boolean moved() {
            for (Map.Entry<String, Double> e : before.entrySet()) {
                if (Math.abs(after.get(e.getKey()) - e.getValue()) > 1e-9) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Updates a set.
     *
     * @param kind {@code exclusive} or {@code independent}
     * @param active true while the set is active (the [0.02, 0.98] clamp applies)
     * @param hyps the hypotheses in set order
     * @param drivers the new drivers
     * @param history what earlier revisions applied
     */
    public static Result update(String kind, boolean active, List<Hyp> hyps, List<Driver> drivers, History history) {
        History h = history == null ? History.EMPTY : history;
        List<Contribution> contributions = new ArrayList<>();
        Set<String> keysSeen = new HashSet<>(h.evidenceKeys());
        Map<String, Double> familyBest = new HashMap<>(h.familyDay());
        for (Driver d : drivers) {
            boolean duplicate = d.evidence() && d.evidenceKey() != null && keysSeen.contains(d.evidenceKey());
            for (Hyp hyp : hyps) {
                Double lr = d.lr().get(hyp.id());
                if (lr == null) {
                    continue;
                }
                double ln = Math.log(lr);
                if (duplicate) {
                    contributions.add(new Contribution(d.kind(), d.id(), d.ref(), hyp.id(), d.signalFamily(),
                            d.evidenceKey(), lr, ln, d.pDetect(), 0.0, true, SAME_GROUP, d.evidence()));
                    continue;
                }
                double applied = ln;
                if (d.evidence() && d.signalFamily() != null) {
                    String key = d.signalFamily() + "|" + hyp.id();
                    Double previous = familyBest.get(key);
                    if (previous != null && Math.abs(ln) <= Math.abs(previous)) {
                        contributions.add(new Contribution(d.kind(), d.id(), d.ref(), hyp.id(), d.signalFamily(),
                                d.evidenceKey(), lr, ln, d.pDetect(), 0.0, true, SAME_FAMILY, d.evidence()));
                        continue;
                    }
                    applied = ln - (previous == null ? 0.0 : previous);
                    familyBest.put(key, ln);
                }
                contributions.add(new Contribution(d.kind(), d.id(), d.ref(), hyp.id(), d.signalFamily(),
                        d.evidenceKey(), lr, ln, d.pDetect(), applied, false, null, d.evidence()));
            }
            if (d.evidence() && d.evidenceKey() != null && !duplicate) {
                keysSeen.add(d.evidenceKey());
            }
        }

        // Damping: the net evidence change per hypothesis per UTC day stays within ±DAILY_LIMIT.
        Map<String, Double> add = new LinkedHashMap<>();
        for (Hyp hyp : hyps) {
            double evidence = 0;
            double other = 0;
            for (Contribution c : contributions) {
                if (!c.hypothesisId().equals(hyp.id()) || c.suppressed()) {
                    continue;
                }
                if (c.evidence()) {
                    evidence += c.applied();
                } else {
                    other += c.applied();
                }
            }
            double net = h.dayNet().getOrDefault(hyp.id(), 0.0);
            double hi = Math.max(0.0, DAILY_LIMIT - net);
            double lo = Math.min(0.0, -DAILY_LIMIT - net);
            double clipped = Math.max(lo, Math.min(hi, evidence));
            if (Math.abs(clipped - evidence) > 1e-12 && Math.abs(evidence) > 1e-12) {
                double factor = clipped / evidence;
                for (int i = 0; i < contributions.size(); i++) {
                    Contribution c = contributions.get(i);
                    if (c.hypothesisId().equals(hyp.id()) && !c.suppressed() && c.evidence()
                            && Math.abs(c.applied()) > 0) {
                        contributions.set(i, c.withApplied(c.applied() * factor, DAMPED));
                    }
                }
            }
            add.put(hyp.id(), clipped + other);
        }

        Map<String, Double> before = new LinkedHashMap<>();
        for (Hyp hyp : hyps) {
            before.put(hyp.id(), hyp.probability());
        }
        double[] raw = new double[hyps.size()];
        boolean exclusive = HypothesisVocabulary.EXCLUSIVE.equals(kind);
        if (exclusive) {
            double[] scores = new double[hyps.size()];
            double max = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < hyps.size(); i++) {
                scores[i] = Math.log(Math.max(1e-12, hyps.get(i).probability())) + add.get(hyps.get(i).id());
                max = Math.max(max, scores[i]);
            }
            double sum = 0;
            for (int i = 0; i < scores.length; i++) {
                raw[i] = Math.exp(scores[i] - max);
                sum += raw[i];
            }
            for (int i = 0; i < raw.length; i++) {
                raw[i] = raw[i] / sum;
            }
        } else {
            for (int i = 0; i < hyps.size(); i++) {
                double p = Math.max(1e-12, Math.min(1 - 1e-12, hyps.get(i).probability()));
                raw[i] = sigmoid(logit(p) + add.get(hyps.get(i).id()));
            }
        }
        Bounded bounded = bound(exclusive, active, hyps, raw);
        Map<String, Double> after = new LinkedHashMap<>();
        for (int i = 0; i < hyps.size(); i++) {
            after.put(hyps.get(i).id(), bounded.values()[i]);
        }
        if (!bounded.capped().isEmpty()) {
            for (int i = 0; i < contributions.size(); i++) {
                Contribution c = contributions.get(i);
                if (bounded.capped().contains(c.hypothesisId()) && !c.suppressed() && c.applied() > 0) {
                    contributions.set(i, c.withApplied(c.applied(), CAPPED));
                }
            }
        }
        return new Result(before, after, List.copyOf(contributions), bounded.capped());
    }

    /**
     * Sets probabilities directly (a review or operator override): the named hypotheses take their values, and in an
     * exclusive set the others share the remaining mass in proportion to their current probabilities. Bounds apply;
     * a speculative-link cap is exceeded only with {@code exceedCap}.
     *
     * @param kind the set kind
     * @param active whether the set is active
     * @param hyps the hypotheses in set order
     * @param values the new probabilities by hypothesis id
     * @param exceedCap true when the operator lifts speculative-link caps (logged by the caller)
     * @param driverKind {@code review} or {@code operator}
     */
    public static Result override(String kind, boolean active, List<Hyp> hyps, Map<String, Double> values,
            boolean exceedCap, String driverKind) {
        boolean exclusive = HypothesisVocabulary.EXCLUSIVE.equals(kind);
        double[] raw = new double[hyps.size()];
        double named = 0;
        double restCurrent = 0;
        for (Hyp hyp : hyps) {
            if (values.containsKey(hyp.id())) {
                named += values.get(hyp.id());
            } else {
                restCurrent += hyp.probability();
            }
        }
        for (int i = 0; i < hyps.size(); i++) {
            Hyp hyp = hyps.get(i);
            if (values.containsKey(hyp.id())) {
                raw[i] = values.get(hyp.id());
            } else if (exclusive) {
                double rest = Math.max(0, 1 - named);
                raw[i] = restCurrent > 0 ? hyp.probability() * rest / restCurrent : rest / Math.max(1,
                        hyps.size() - values.size());
            } else {
                raw[i] = hyp.probability();
            }
        }
        if (exclusive) {
            double sum = 0;
            for (double v : raw) {
                sum += v;
            }
            if (sum > 0) {
                for (int i = 0; i < raw.length; i++) {
                    raw[i] /= sum;
                }
            }
        }
        List<Hyp> effective = new ArrayList<>();
        for (Hyp hyp : hyps) {
            effective.add(exceedCap ? new Hyp(hyp.id(), hyp.label(), hyp.probability(), hyp.cap(), true) : hyp);
        }
        Bounded bounded = bound(exclusive, active, effective, raw);
        Map<String, Double> before = new LinkedHashMap<>();
        Map<String, Double> after = new LinkedHashMap<>();
        List<Contribution> contributions = new ArrayList<>();
        for (int i = 0; i < hyps.size(); i++) {
            Hyp hyp = hyps.get(i);
            before.put(hyp.id(), hyp.probability());
            after.put(hyp.id(), bounded.values()[i]);
            if (values.containsKey(hyp.id())) {
                double change = exclusive ? Math.log(Math.max(1e-12, bounded.values()[i]))
                        - Math.log(Math.max(1e-12, hyp.probability()))
                        : logit(clampOpen(bounded.values()[i])) - logit(clampOpen(hyp.probability()));
                contributions.add(new Contribution(driverKind, null, null, hyp.id(), null, null, null, null, null,
                        change, false, bounded.capped().contains(hyp.id()) ? CAPPED : null, false));
            }
        }
        return new Result(before, after, List.copyOf(contributions), bounded.capped());
    }

    /**
     * The speculative-link cap (prompt 125 §4.6): {@code min(0.98, posterior)} where the posterior applies
     * {@code LR = (1 − placeboRate) ÷ placeboRate}, bounded to [1, 20], to the prior.
     *
     * @param prior the hypothesis prior
     * @param placeboRate the coincidence baseline's placebo rate
     */
    public static double cap(double prior, double placeboRate) {
        double rate = Math.max(1e-9, Math.min(1, placeboRate));
        double lr = Math.max(1.0, Math.min(20.0, (1 - rate) / rate));
        double posterior = prior * lr / (prior * lr + (1 - prior));
        return Math.min(P_MAX, posterior);
    }

    /** {@code ln(p / (1 − p))}. */
    public static double logit(double p) {
        return Math.log(p / (1 - p));
    }

    /** {@code 1 / (1 + e^−x)}. */
    public static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private static double clampOpen(double p) {
        return Math.max(1e-9, Math.min(1 - 1e-9, p));
    }

    /** Bounded probabilities and the hypotheses held at their cap. */
    private record Bounded(double[] values, Set<String> capped) {}

    private static Bounded bound(boolean exclusive, boolean active, List<Hyp> hyps, double[] raw) {
        int n = hyps.size();
        double[] lo = new double[n];
        double[] hi = new double[n];
        for (int i = 0; i < n; i++) {
            Hyp hyp = hyps.get(i);
            lo[i] = active ? P_MIN : 0.0;
            hi[i] = active ? P_MAX : 1.0;
            if (hyp.cap() != null && !hyp.capOverridden()) {
                hi[i] = Math.min(hi[i], hyp.cap());
            }
            if (lo[i] > hi[i]) {
                lo[i] = hi[i];
            }
        }
        double[] out = exclusive ? boxSimplex(raw, lo, hi) : boxClamp(raw, lo, hi);
        Set<String> capped = new LinkedHashSet<>();
        for (int i = 0; i < n; i++) {
            Hyp hyp = hyps.get(i);
            if (hyp.cap() != null && !hyp.capOverridden() && raw[i] > hyp.cap() + 1e-12
                    && Math.abs(out[i] - hi[i]) < 1e-12) {
                capped.add(hyp.id());
            }
        }
        return new Bounded(out, capped);
    }

    private static double[] boxClamp(double[] raw, double[] lo, double[] hi) {
        double[] out = new double[raw.length];
        for (int i = 0; i < raw.length; i++) {
            out[i] = Math.max(lo[i], Math.min(hi[i], raw[i]));
        }
        return out;
    }

    /**
     * Projects a distribution onto {@code lo ≤ p ≤ hi} with {@code Σ p = 1}: the free values share the remaining mass
     * in proportion to their raw values; values below their lower bound are fixed there first, then values above their
     * upper bound, one direction per round, until nothing is violated.
     */
    static double[] boxSimplex(double[] raw, double[] lo, double[] hi) {
        int n = raw.length;
        double[] out = raw.clone();
        boolean[] fixed = new boolean[n];
        for (int round = 0; round <= 2 * n + 1; round++) {
            double fixedMass = 0;
            double freeRaw = 0;
            int free = 0;
            for (int i = 0; i < n; i++) {
                if (fixed[i]) {
                    fixedMass += out[i];
                } else {
                    freeRaw += raw[i];
                    free++;
                }
            }
            if (free == 0) {
                break;
            }
            double mass = 1 - fixedMass;
            for (int i = 0; i < n; i++) {
                if (!fixed[i]) {
                    out[i] = freeRaw > 0 ? raw[i] * mass / freeRaw : mass / free;
                }
            }
            boolean violated = false;
            for (int i = 0; i < n; i++) {
                if (!fixed[i] && out[i] < lo[i] - 1e-15) {
                    out[i] = lo[i];
                    fixed[i] = true;
                    violated = true;
                }
            }
            if (!violated) {
                for (int i = 0; i < n; i++) {
                    if (!fixed[i] && out[i] > hi[i] + 1e-15) {
                        out[i] = hi[i];
                        fixed[i] = true;
                        violated = true;
                    }
                }
            }
            if (!violated) {
                break;
            }
        }
        return out;
    }
}
