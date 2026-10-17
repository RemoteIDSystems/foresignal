package ai.foresignal.analytics.cooccurrence;

import ai.foresignal.analytics.finding.FindingSeverities;
import ai.foresignal.analytics.math.CountTails;
import ai.foresignal.analytics.math.MultipleTesting;
import ai.foresignal.analytics.math.RobustStats;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The co-occurrence miner (prompt 119, {@code cooccurrence} 1.0.0, Intelligence-Design §5.4): pure and deterministic.
 *
 * <ul>
 *   <li>Per region and family, the day's count gets an upper-tail p-value against its day-of-week baseline: the
 *       covered counts of the same weekday over the previous {@value #MIN_WEEKS}–{@value #MAX_WEEKS} weeks, negative
 *       binomial when overdispersed (method of moments), Poisson otherwise.
 *   <li>Every pair of families of a region with both p-values gets Fisher's combined p; Benjamini–Hochberg at
 *       {@code q} (0.05) runs across all tests of the day. A pair survives when its adjusted p is at most {@code q} and
 *       both families exceeded at p ≤ 0.05.
 *   <li>The expected joint rate assumes independence: each family's historical exceedance frequency (days with
 *       p ≤ 0.05 over the history given, normally the trailing year), multiplied; the lift is the observed joint days
 *       over the expected.
 *   <li>Severity: {@code notable}; {@code significant} when the adjusted p ≤ 0.001 and the lift ≥ 3.
 * </ul>
 */
public final class CoOccurrenceMiner {

    /** The detector name. */
    public static final String NAME = "cooccurrence";

    /** The detector version. */
    public static final String VERSION = "1.0.0";

    /** The fewest baseline weeks. */
    public static final int MIN_WEEKS = 8;

    /** The most baseline weeks. */
    public static final int MAX_WEEKS = 12;

    /** The per-family exceedance level. */
    public static final double EXCEEDANCE_P = 0.05;

    /** The lift a significant co-occurrence needs. */
    public static final double SIGNIFICANT_LIFT = 3.0;

    /**
     * One family's daily counts in a region, oldest first; the last element is the day mined.
     *
     * @param values the counts ({@code NaN} where missing)
     * @param ok true where the day was covered
     */
    public record Daily(double[] values, boolean[] ok) {}

    /**
     * One region-and-pair test.
     *
     * @param region the region
     * @param familyA the first family (alphabetical)
     * @param familyB the second family
     * @param countA the day's count of A
     * @param countB the day's count of B
     * @param expectedA A's baseline mean
     * @param expectedB B's baseline mean
     * @param pA A's exceedance p-value
     * @param pB B's exceedance p-value
     * @param fisherP Fisher's combined p
     * @param adjustedP the Benjamini–Hochberg adjusted p
     * @param jointDays observed joint exceedance days over the history (the mined day included)
     * @param expectedJointDays expected joint days under independence over the same days
     * @param historyDays the days of history judged
     * @param lift observed over expected joint days
     * @param survives true when it passes the FDR and both exceedances
     * @param severity the severity of a survivor
     */
    public record Test(String region, String familyA, String familyB, double countA, double countB, double expectedA,
            double expectedB, double pA, double pB, double fisherP, double adjustedP, int jointDays,
            double expectedJointDays, int historyDays, double lift, boolean survives, String severity) {}

    private CoOccurrenceMiner() {}

    /**
     * Mines the last day of the series.
     *
     * @param counts per region, per family, the daily counts (all the same length)
     * @param q the false-discovery level (0.05)
     * @return every test of the day, survivors marked
     */
    public static List<Test> mine(Map<String, Map<String, Daily>> counts, double q) {
        record Pending(String region, String a, String b, double ca, double cb, double ea, double eb, double pa,
                double pb, double fisher, int joint, double expected, int days) {}
        List<Pending> pending = new ArrayList<>();
        for (Map.Entry<String, Map<String, Daily>> region : new TreeMap<>(counts).entrySet()) {
            Map<String, Daily> fams = new TreeMap<>(region.getValue());
            Map<String, double[]> pSeries = new TreeMap<>();
            Map<String, double[]> means = new TreeMap<>();
            for (Map.Entry<String, Daily> f : fams.entrySet()) {
                double[][] pm = pValues(f.getValue());
                pSeries.put(f.getKey(), pm[0]);
                means.put(f.getKey(), pm[1]);
            }
            List<String> names = new ArrayList<>(fams.keySet());
            for (int i = 0; i < names.size(); i++) {
                for (int j = i + 1; j < names.size(); j++) {
                    String a = names.get(i);
                    String b = names.get(j);
                    double[] pa = pSeries.get(a);
                    double[] pb = pSeries.get(b);
                    int last = pa.length - 1;
                    if (last < 0 || !Double.isFinite(pa[last]) || !Double.isFinite(pb[last])) {
                        continue;
                    }
                    int days = 0;
                    int exA = 0;
                    int exB = 0;
                    int joint = 0;
                    for (int t = 0; t <= last; t++) {
                        if (Double.isFinite(pa[t]) && Double.isFinite(pb[t])) {
                            days++;
                            boolean ea = pa[t] <= EXCEEDANCE_P;
                            boolean eb = pb[t] <= EXCEEDANCE_P;
                            exA += ea ? 1 : 0;
                            exB += eb ? 1 : 0;
                            joint += ea && eb ? 1 : 0;
                        }
                    }
                    double expected = days == 0 ? Double.NaN : ((double) exA / days) * ((double) exB / days) * days;
                    pending.add(new Pending(region.getKey(), a, b, fams.get(a).values()[last],
                            fams.get(b).values()[last], means.get(a)[last], means.get(b)[last], pa[last], pb[last],
                            CountTails.fisher(pa[last], pb[last]), joint, expected, days));
                }
            }
        }
        double[] fisher = pending.stream().mapToDouble(Pending::fisher).toArray();
        double[] adjusted = MultipleTesting.benjaminiHochberg(fisher);
        List<Test> out = new ArrayList<>();
        for (int i = 0; i < pending.size(); i++) {
            Pending pd = pending.get(i);
            double lift = pd.expected() > 0 ? pd.joint() / pd.expected() : Double.POSITIVE_INFINITY;
            boolean survives = adjusted[i] <= q && pd.pa() <= EXCEEDANCE_P && pd.pb() <= EXCEEDANCE_P;
            String severity = !survives ? FindingSeverities.INFO
                    : adjusted[i] <= FindingSeverities.SIGNIFICANT_P && lift >= SIGNIFICANT_LIFT
                            ? FindingSeverities.SIGNIFICANT : FindingSeverities.NOTABLE;
            out.add(new Test(pd.region(), pd.a(), pd.b(), pd.ca(), pd.cb(), pd.ea(), pd.eb(), pd.pa(), pd.pb(),
                    pd.fisher(), adjusted[i], pd.joint(), pd.expected(), pd.days(), lift, survives, severity));
        }
        return out;
    }

    /**
     * Each day's exceedance p-value against its day-of-week baseline, and the baseline mean ({@code NaN} where the day
     * is uncovered or the baseline has fewer than {@value #MIN_WEEKS} weeks).
     */
    static double[][] pValues(Daily d) {
        int n = d.values().length;
        double[] p = new double[n];
        double[] mean = new double[n];
        for (int t = 0; t < n; t++) {
            p[t] = Double.NaN;
            mean[t] = Double.NaN;
            if (!d.ok()[t] || !Double.isFinite(d.values()[t])) {
                continue;
            }
            List<Double> base = new ArrayList<>();
            for (int k = 1; k <= MAX_WEEKS && t - 7 * k >= 0; k++) {
                int s = t - 7 * k;
                if (d.ok()[s] && Double.isFinite(d.values()[s])) {
                    base.add(d.values()[s]);
                }
            }
            if (base.size() < MIN_WEEKS) {
                continue;
            }
            double[] b = base.stream().mapToDouble(Double::doubleValue).toArray();
            double m = RobustStats.mean(b);
            double v = RobustStats.variance(b);
            mean[t] = m;
            p[t] = CountTails.exceedanceP(d.values()[t], Math.max(m, 0.05), v);
        }
        return new double[][] {p, mean};
    }
}
