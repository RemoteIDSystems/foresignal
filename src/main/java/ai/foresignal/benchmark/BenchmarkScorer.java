package ai.foresignal.benchmark;

import ai.foresignal.analytics.math.RobustStats;
import ai.foresignal.forecast.score.Calibration;
import ai.foresignal.forecast.score.ForecastScorer;
import ai.foresignal.forecast.score.Skill;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The benchmark metrics {@value #NAME} {@value #VERSION} (prompt 123): pure functions over what a component reported
 * and what was planted. Every metric is defined here once, so component benchmarks only count.
 *
 * <ul>
 *   <li><b>False-discovery rate</b> {@code V / max(R, 1)}: false discoveries over discoveries (0 without
 *       discoveries).</li>
 *   <li><b>Recall</b> found ÷ planted, <b>precision</b> true discoveries ÷ discoveries ({@code NaN} with nothing to
 *       divide by).</li>
 *   <li><b>Detection delay</b>: median and 90th percentile (type 7) of the delays of detected signals.</li>
 *   <li><b>Correct {@code unknown} share</b> under outages: 1 − wrong "absent" or downward conclusions ÷
 *       opportunities.</li>
 *   <li><b>Duplicate inflation</b>: the increase in independent evidence groups from echoes, plus one when
 *       confidence rose and one when evidence quality rose; it must be 0.</li>
 *   <li><b>Pairwise linking</b> precision and recall of an assignment against planted groups, and spurious merges
 *       (assigned clusters that mix planted groups).</li>
 *   <li><b>Forecasts</b>: mean Brier and log score ({@link ForecastScorer}), the largest reliability deviation over
 *       populated 10-bin cells and the CORP miscalibration ({@link Calibration}), and skill {@code 1 − S/S_ref}
 *       ({@link Skill}).</li>
 * </ul>
 */
public final class BenchmarkScorer {

    /** The scorer name. */
    public static final String NAME = "BenchmarkScorer";

    /** The scorer version. */
    public static final String VERSION = "1.0.0";

    private static final List<String> QUALITY = List.of("insufficient", "weak", "moderate", "strong");

    private BenchmarkScorer() {}

    /** {@code V / max(R, 1)}. */
    public static double fdr(long falseDiscoveries, long discoveries) {
        return discoveries <= 0 ? 0.0 : (double) falseDiscoveries / discoveries;
    }

    /** {@code found / planted}, {@code NaN} without planted signals. */
    public static double recall(long found, long planted) {
        return planted <= 0 ? Double.NaN : (double) found / planted;
    }

    /** {@code truePositives / discoveries}, {@code NaN} without discoveries. */
    public static double precision(long truePositives, long discoveries) {
        return discoveries <= 0 ? Double.NaN : (double) truePositives / discoveries;
    }

    /** {@code events / opportunities}, {@code NaN} without opportunities. */
    public static double rate(long events, long opportunities) {
        return opportunities <= 0 ? Double.NaN : (double) events / opportunities;
    }

    /** The median delay, {@code NaN} without delays. */
    public static double medianDelay(List<Double> delays) {
        return RobustStats.median(toArray(delays));
    }

    /** The 90th-percentile delay (type 7), {@code NaN} without delays. */
    public static double p90Delay(List<Double> delays) {
        return RobustStats.quantile(toArray(delays), 0.9);
    }

    /** {@code 1 − wrong / opportunities}, {@code NaN} without opportunities. */
    public static double correctUnknownShare(long wrongConclusions, long opportunities) {
        return opportunities <= 0 ? Double.NaN : 1.0 - (double) wrongConclusions / opportunities;
    }

    /**
     * The duplicate inflation of one event: the increase in independent groups, plus 1 when the confidence rose
     * (beyond 1e-9) and 1 when the evidence quality rose.
     */
    public static double duplicateInflation(int groupsBefore, int groupsAfter, double confidenceBefore,
            double confidenceAfter, String qualityBefore, String qualityAfter) {
        double out = Math.max(0, groupsAfter - groupsBefore);
        if (confidenceAfter > confidenceBefore + 1e-9) {
            out += 1;
        }
        if (QUALITY.indexOf(qualityAfter) > QUALITY.indexOf(qualityBefore)) {
            out += 1;
        }
        return out;
    }

    /**
     * Pairwise counts of an assignment against planted groups.
     *
     * @param truePairs pairs in the same assigned cluster and the same planted group
     * @param falsePairs pairs in the same assigned cluster but different planted groups
     * @param missedPairs pairs in the same planted group but not in the same assigned cluster
     */
    public record PairCounts(long truePairs, long falsePairs, long missedPairs) {

        /** Pairwise precision ({@code NaN} without assigned pairs). */
        public double precision() {
            return BenchmarkScorer.precision(truePairs, truePairs + falsePairs);
        }

        /** Pairwise recall ({@code NaN} without planted pairs). */
        public double recall() {
            return BenchmarkScorer.recall(truePairs, truePairs + missedPairs);
        }

        /** These counts plus {@code other}. */
        public PairCounts plus(PairCounts other) {
            return new PairCounts(truePairs + other.truePairs, falsePairs + other.falsePairs,
                    missedPairs + other.missedPairs);
        }
    }

    /**
     * Counts pairs; an unassigned item (null cluster) pairs with nothing.
     *
     * @param truth the planted group of each item
     * @param assigned the assigned cluster of each item, or null, aligned
     */
    public static PairCounts pairs(List<String> truth, List<String> assigned) {
        long tp = 0;
        long fp = 0;
        long fn = 0;
        for (int i = 0; i < truth.size(); i++) {
            for (int j = i + 1; j < truth.size(); j++) {
                boolean sameTruth = truth.get(i).equals(truth.get(j));
                boolean sameCluster = assigned.get(i) != null && assigned.get(i).equals(assigned.get(j));
                if (sameCluster && sameTruth) {
                    tp++;
                } else if (sameCluster) {
                    fp++;
                } else if (sameTruth) {
                    fn++;
                }
            }
        }
        return new PairCounts(tp, fp, fn);
    }

    /** The assigned clusters that hold items of more than one planted group. */
    public static int spuriousMerges(List<String> truth, List<String> assigned) {
        Map<String, Set<String>> groups = new HashMap<>();
        for (int i = 0; i < truth.size(); i++) {
            if (assigned.get(i) != null) {
                groups.computeIfAbsent(assigned.get(i), k -> new HashSet<>()).add(truth.get(i));
            }
        }
        int out = 0;
        for (Set<String> g : groups.values()) {
            if (g.size() > 1) {
                out++;
            }
        }
        return out;
    }

    /** The mean Brier score of binary forecasts, {@code NaN} without forecasts. */
    public static double brier(double[] p, boolean[] outcome) {
        if (p.length == 0) {
            return Double.NaN;
        }
        double sum = 0;
        for (int i = 0; i < p.length; i++) {
            sum += ForecastScorer.brier(p[i], outcome[i]);
        }
        return sum / p.length;
    }

    /** The mean log score (clipped as {@link ForecastScorer} clips), {@code NaN} without forecasts. */
    public static double logScore(double[] p, boolean[] outcome) {
        if (p.length == 0) {
            return Double.NaN;
        }
        double sum = 0;
        for (int i = 0; i < p.length; i++) {
            sum += ForecastScorer.logScore(p[i], outcome[i]);
        }
        return sum / p.length;
    }

    /**
     * The largest {@code |mean forecast − observed frequency|} over the 10-bin cells holding at least
     * {@code minCount} forecasts, {@code NaN} when no cell is populated.
     */
    public static double reliabilityMaxDeviation(double[] p, boolean[] outcome, int minCount) {
        double max = Double.NaN;
        for (Calibration.Bin bin : Calibration.bins(p, toDoubles(outcome))) {
            if (bin.n() >= minCount && bin.meanForecast() != null && bin.observedFrequency() != null) {
                double d = Math.abs(bin.meanForecast() - bin.observedFrequency());
                max = Double.isNaN(max) ? d : Math.max(max, d);
            }
        }
        return max;
    }

    /** The number of populated 10-bin cells. */
    public static int populatedBins(double[] p, boolean[] outcome, int minCount) {
        int out = 0;
        for (Calibration.Bin bin : Calibration.bins(p, toDoubles(outcome))) {
            if (bin.n() >= minCount) {
                out++;
            }
        }
        return out;
    }

    /** The CORP miscalibration (MCB), {@code NaN} without forecasts. */
    public static double corpMiscalibration(double[] p, boolean[] outcome) {
        Calibration.Corp corp = Calibration.corp(p, toDoubles(outcome));
        return corp == null ? Double.NaN : corp.miscalibration();
    }

    /** Skill {@code 1 − S / S_ref} ({@code NaN} when either is not finite or the reference is 0). */
    public static double skill(double score, double reference) {
        if (!Double.isFinite(score) || !Double.isFinite(reference)) {
            return Double.NaN;
        }
        return Skill.skill(score, reference);
    }

    /** The outcomes as 0/1. */
    public static double[] toDoubles(boolean[] outcome) {
        double[] out = new double[outcome.length];
        for (int i = 0; i < outcome.length; i++) {
            out[i] = outcome[i] ? 1 : 0;
        }
        return out;
    }

    private static double[] toArray(List<Double> values) {
        return values.stream().filter(Objects::nonNull).mapToDouble(Double::doubleValue).toArray();
    }
}
