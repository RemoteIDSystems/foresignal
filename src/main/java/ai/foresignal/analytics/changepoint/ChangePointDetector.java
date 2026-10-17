package ai.foresignal.analytics.changepoint;

import ai.foresignal.analytics.math.Bocpd;
import ai.foresignal.analytics.math.Cusum;
import ai.foresignal.analytics.math.CusumThresholds;
import ai.foresignal.analytics.math.RobustStats;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The change-point detector (prompt 119, Intelligence-Design §5.2): pure, deterministic for identical input (INT-1).
 *
 * <p><b>Baseline.</b> For counts, the in-control mean {@code μ₀ₜ} is the mean of the last {@value #BASELINE_SLOTS}
 * covered values of the same slot (same weekday for daily series, same hour of week for hourly, the previous weeks for
 * weekly), using only buckets before {@code t}; it needs at least {@value #MIN_BASELINE} of them. Overdispersed counts
 * (mean squared Pearson residual above {@value #OVERDISPERSION}) use the negative-binomial CUSUM with the size the
 * residuals imply. Continuous series use the median and floored MAD scale of the previous {@value #CONTINUOUS_WINDOW}
 * covered buckets.
 *
 * <p><b>Detection.</b> Upward and downward CUSUMs with {@code minRelShift} (0.5) and an alarm threshold simulated for a
 * target in-control run length of {@code targetArl} (365) buckets. The most recent alarm in the last
 * {@code recentBuckets} becomes a candidate; it is confirmed when the level from the estimated change through the
 * alarm is shifted by at least half of {@code minRelShift} (half a standard deviation for continuous series) and at
 * least {@code minPersistence} (3) covered buckets after the alarm, and at least two thirds of them, each show that
 * shift; until then it is not reported. A coverage gap between the change and the end makes it
 * {@code unknown}, never a drop.
 *
 * <p><b>Confirmation and base rate.</b> BOCPD (hazard 1/250, run lengths ≤ 300) gives the posterior probability of a
 * change since the estimated change; ≥ 0.8 makes the finding significant. The base rate is the number of alarms the
 * same settings raise over the series' own history before the change, per year.
 */
public final class ChangePointDetector {

    /** The CUSUM detector name. */
    public static final String CUSUM = "changepoint-cusum";

    /** The BOCPD detector name. */
    public static final String BOCPD = "changepoint-bocpd";

    /** The detectors' version. */
    public static final String VERSION = "1.0.0";

    /** Same-slot values in a count baseline. */
    public static final int BASELINE_SLOTS = 8;

    /** The fewest same-slot values a count baseline needs. */
    public static final int MIN_BASELINE = 4;

    /** The trailing window of the continuous baseline, buckets. */
    public static final int CONTINUOUS_WINDOW = 60;

    /** The mean squared Pearson residual above which counts are treated as negative binomial. */
    public static final double OVERDISPERSION = 1.5;

    /**
     * The detector parameters.
     *
     * @param minRelShift the relative shift of the count CUSUM (0.5)
     * @param gaussianShift the shift of the continuous CUSUM in standard deviations (1.0)
     * @param targetArl the in-control average run length (365 buckets)
     * @param minPersistence the covered buckets a change must persist (3)
     * @param hazard the BOCPD hazard (1/250)
     * @param truncation the BOCPD run-length truncation (300)
     * @param confirmPosterior the BOCPD posterior that makes a finding significant (0.8)
     * @param period the slot period of the count baseline in buckets (7 daily, 168 hourly, 1 weekly)
     * @param recentBuckets how far back from the end an alarm may be to be reported
     */
    public record Params(double minRelShift, double gaussianShift, int targetArl, int minPersistence, double hazard,
            int truncation, double confirmPosterior, int period, int recentBuckets) {

        /** The documented defaults for a slot period. */
        public static Params defaults(int period, int recentBuckets) {
            return new Params(0.5, 1.0, 365, 3, 1.0 / 250, 300, 0.8, period, recentBuckets);
        }

        /** The parameters as stored with a finding. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("minRelShift", minRelShift);
            out.put("gaussianShift", gaussianShift);
            out.put("targetArl", targetArl);
            out.put("minPersistence", minPersistence);
            out.put("hazard", hazard);
            out.put("truncation", truncation);
            out.put("confirmPosterior", confirmPosterior);
            out.put("period", period);
            out.put("recentBuckets", recentBuckets);
            return out;
        }
    }

    /** The outcome state. */
    public enum State {
        /** No candidate. */
        NONE,
        /** A candidate that persisted. */
        CONFIRMED,
        /** A candidate that a coverage gap cut, or one that did not persist long enough to judge. */
        UNKNOWN
    }

    /**
     * The result.
     *
     * @param state the state
     * @param direction {@code +1} up, {@code −1} down, 0 without a candidate
     * @param changeIndex the bucket of the estimated change, or −1
     * @param alarmIndex the alarm bucket, or −1
     * @param changeAt the start of the change bucket, or null
     * @param alarmAt the start of the alarm bucket, or null
     * @param levelBefore the level before (mean of the baseline values; median for continuous series)
     * @param levelAfter the level from the change through the alarm (at least {@code minPersistence} buckets)
     * @param effect the relative change (counts) or the shift in standard deviations (continuous)
     * @param bocpdPosterior the BOCPD probability of a change since {@code changeAt}
     * @param threshold the CUSUM threshold used
     * @param model {@code POISSON}, {@code NEG_BIN} or {@code GAUSSIAN}
     * @param historicalAlarms the alarms before the change over the series' history
     * @param historyBuckets the covered buckets of that history
     */
    public record Result(State state, int direction, int changeIndex, int alarmIndex, Instant changeAt,
            Instant alarmAt, double levelBefore, double levelAfter, double effect, double bocpdPosterior,
            double threshold, String model, int historicalAlarms, int historyBuckets) {

        /** The detection delay in buckets, or −1. */
        public int detectionDelay() {
            return alarmIndex < 0 ? -1 : alarmIndex - changeIndex;
        }

        /** Alarms per year of history at {@code perYear} buckets a year. */
        public double alarmsPerYear(double perYear) {
            return historyBuckets <= 0 ? Double.NaN : historicalAlarms * perYear / historyBuckets;
        }

        /** True for a downward change. */
        public boolean downward() {
            return direction < 0;
        }

        static Result none(String model, double threshold) {
            return new Result(State.NONE, 0, -1, -1, null, null, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                    threshold, model, 0, 0);
        }
    }

    private ChangePointDetector() {}

    /**
     * Runs the detector.
     *
     * @param starts the bucket starts
     * @param x the values ({@code NaN} where missing)
     * @param gap true where missing or uncovered
     * @param counts true for counts, false for continuous values
     * @param p the parameters
     * @param thresholds the threshold cache
     */
    public static Result detect(Instant[] starts, double[] x, boolean[] gap, boolean counts, Params p,
            CusumThresholds thresholds) {
        int n = x.length;
        double[] mu0 = new double[n];
        double[] sigma = new double[n];
        if (counts) {
            countBaseline(x, gap, p.period(), mu0);
        } else {
            continuousBaseline(x, gap, mu0, sigma);
        }
        Cusum.Model model = counts ? Cusum.Model.POISSON : Cusum.Model.GAUSSIAN;
        double size = Double.NaN;
        if (counts) {
            size = negBinSize(x, gap, mu0);
            if (Double.isFinite(size)) {
                model = Cusum.Model.NEG_BIN;
            }
        }
        double typical = RobustStats.median(mu0);
        Cusum.Params up = new Cusum.Params(model, 1, p.minRelShift(), p.gaussianShift(),
                Double.isFinite(size) ? size : 1);
        Cusum.Params down = new Cusum.Params(model, -1, p.minRelShift(), p.gaussianShift(),
                Double.isFinite(size) ? size : 1);
        double hUp = thresholds.threshold(up, Double.isFinite(typical) ? typical : 1, p.targetArl());
        double hDown = thresholds.threshold(down, Double.isFinite(typical) ? typical : 1, p.targetArl());
        Cusum.Run runUp = Cusum.run(x, mu0, counts ? null : sigma, gap, up, hUp);
        Cusum.Run runDown = Cusum.run(x, mu0, counts ? null : sigma, gap, down, hDown);
        Cusum.Alarm latestUp = latest(runUp.alarms());
        Cusum.Alarm latestDown = latest(runDown.alarms());
        Cusum.Alarm alarm;
        int direction;
        if (latestUp == null && latestDown == null) {
            alarm = null;
            direction = 0;
        } else if (latestDown == null || (latestUp != null && latestUp.alarmIndex() >= latestDown.alarmIndex())) {
            alarm = latestUp;
            direction = 1;
        } else {
            alarm = latestDown;
            direction = -1;
        }
        int recentFrom = Math.max(0, n - p.recentBuckets());
        if (alarm == null || alarm.alarmIndex() < recentFrom) {
            if (canceledRecently(runUp, recentFrom) || canceledRecently(runDown, recentFrom)) {
                return new Result(State.UNKNOWN, 0, -1, -1, null, null, Double.NaN, Double.NaN, Double.NaN,
                        Double.NaN, Math.max(hUp, hDown), model.name(), 0, 0);
            }
            return Result.none(model.name(), Math.max(hUp, hDown));
        }
        // Recency and direction are judged on the latest alarm; the change is where its shift began.
        alarm = chainStart(direction > 0 ? runUp.alarms() : runDown.alarms(), alarm);
        int change = alarm.changeIndex();
        boolean gapAfter = false;
        for (int t = change; t < n; t++) {
            if (gap[t] || !Double.isFinite(x[t])) {
                gapAfter = true;
                break;
            }
        }
        double before = levelBefore(x, gap, mu0, change, counts);
        // The level after is measured over the change through the alarm (at least minPersistence buckets), so it
        // stays the same on later runs once that span is complete.
        int spanEnd = Math.max(alarm.alarmIndex(), change + p.minPersistence() - 1);
        double after = RobustStats.mean(java.util.Arrays.copyOfRange(x, change, Math.min(n, spanEnd + 1)));
        double effect;
        if (counts) {
            effect = before > 0 ? (after - before) / before : Double.NaN;
        } else {
            double scale = sigma[Math.min(change, n - 1)];
            effect = scale > 0 && Double.isFinite(scale) ? (after - before) / scale : Double.NaN;
        }
        int covered = 0;
        for (int t = change; t < n && t <= spanEnd; t++) {
            if (!gap[t] && Double.isFinite(x[t])) {
                covered++;
            }
        }
        double needed = counts ? p.minRelShift() / 2 : 0.5;
        // Persistence (checkpoint 124): at least minPersistence covered buckets after the alarm, and at least two
        // thirds of them, must each show the shift, not only the average from the change through the alarm. Averaging the span let a two-bucket burst plus one normal bucket pass as a
        // level shift (the 123 benchmark's spike scenario confirmed a change point in every replicate).
        // Each bucket is judged against its own in-control baseline (same slot for counts), so a seasonal high is not
        // mistaken for the shift; the level before the change stands in where no baseline exists.
        int shifted = 0;
        int judged = 0;
        for (int t = alarm.alarmIndex() + 1; t < n; t++) {
            if (gap[t] || !Double.isFinite(x[t])) {
                continue;
            }
            judged++;
            double base = Double.isFinite(mu0[t]) ? mu0[t] : before;
            double scale = counts ? base : sigma[t];
            if (scale > 0 && Double.isFinite(scale) && direction * (x[t] - base) / scale >= needed) {
                shifted++;
            }
        }
        boolean persisted = covered >= p.minPersistence() && shifted >= p.minPersistence()
                && 3 * shifted >= 2 * judged && Double.isFinite(effect) && direction * effect >= needed;
        int historical = 0;
        for (Cusum.Alarm a : runUp.alarms()) {
            if (a.alarmIndex() < change) {
                historical++;
            }
        }
        for (Cusum.Alarm a : runDown.alarms()) {
            if (a.alarmIndex() < change) {
                historical++;
            }
        }
        int history = 0;
        for (int t = 0; t < change; t++) {
            if (!gap[t] && Double.isFinite(x[t]) && Double.isFinite(mu0[t])) {
                history++;
            }
        }
        if (!gapAfter && !persisted) {
            // Not yet persisted for minPersistence covered buckets, or it did not hold: no change point (yet).
            return Result.none(model.name(), direction > 0 ? hUp : hDown);
        }
        State state = gapAfter ? State.UNKNOWN : State.CONFIRMED;
        double posterior = bocpd(x, gap, counts, typical, sigma, change, p);
        return new Result(state, direction, change, alarm.alarmIndex(), starts[change], starts[alarm.alarmIndex()],
                before, after, effect, posterior, direction > 0 ? hUp : hDown, model.name(), historical, history);
    }

    private static Cusum.Alarm latest(List<Cusum.Alarm> alarms) {
        return alarms.isEmpty() ? null : alarms.get(alarms.size() - 1);
    }

    /**
     * The first alarm of the shift {@code alarm} belongs to. After an alarm the CUSUM restarts from zero, so a
     * sustained shift re-alarms every few buckets; an alarm whose estimated change is the bucket right after the
     * previous alarm (the statistic never returned to zero in between) continues that shift. Reporting the chain's
     * first alarm gives the change and delay of the shift rather than of its latest re-alarm.
     */
    private static Cusum.Alarm chainStart(List<Cusum.Alarm> alarms, Cusum.Alarm alarm) {
        int i = alarms.indexOf(alarm);
        while (i > 0 && alarms.get(i).changeIndex() == alarms.get(i - 1).alarmIndex() + 1) {
            i--;
        }
        return alarms.get(i);
    }

    private static boolean canceledRecently(Cusum.Run run, int from) {
        for (int t : run.canceled()) {
            if (t >= from) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@code μ₀ₜ}: the mean of the last same-slot covered values before {@code t} (also the count baseline that prompt
     * 120 pre-whitens series pairs with).
     *
     * @param x the values
     * @param gap true where a value is missing or uncovered
     * @param period the slot period in buckets (7 for daily series, 1 otherwise)
     * @param mu0 receives the baseline per bucket ({@code NaN} with fewer than {@value #MIN_BASELINE} slots)
     */
    public static void countBaseline(double[] x, boolean[] gap, int period, double[] mu0) {
        int step = Math.max(1, period);
        for (int t = 0; t < x.length; t++) {
            double sum = 0;
            int k = 0;
            for (int s = t - step; s >= 0 && k < BASELINE_SLOTS; s -= step) {
                if (!gap[s] && Double.isFinite(x[s])) {
                    sum += x[s];
                    k++;
                }
            }
            mu0[t] = k >= MIN_BASELINE ? Math.max(sum / k, 0.05) : Double.NaN;
        }
    }

    /** The trailing median and floored robust scale of the previous {@value #CONTINUOUS_WINDOW} covered values. */
    static void continuousBaseline(double[] x, boolean[] gap, double[] mu0, double[] sigma) {
        List<Double> window = new ArrayList<>();
        for (int t = 0; t < x.length; t++) {
            if (window.size() >= Math.max(10, CONTINUOUS_WINDOW / 3)) {
                double[] w = window.stream().mapToDouble(Double::doubleValue).toArray();
                double median = RobustStats.median(w);
                mu0[t] = median;
                double sdFloor = Math.max(1e-9, 1e-3 * Math.abs(median));
                double sd = RobustStats.sd(w);
                sigma[t] = RobustStats.scale(w, Math.max(sdFloor, Double.isFinite(sd) ? sd * 0.1 : 0));
            } else {
                mu0[t] = Double.NaN;
                sigma[t] = Double.NaN;
            }
            if (!gap[t] && Double.isFinite(x[t])) {
                window.add(x[t]);
                if (window.size() > CONTINUOUS_WINDOW) {
                    window.remove(0);
                }
            }
        }
    }

    /** The negative-binomial size the residuals imply, or {@code NaN} when the counts are not overdispersed. */
    static double negBinSize(double[] x, boolean[] gap, double[] mu0) {
        double sum = 0;
        double meanMu = 0;
        int k = 0;
        for (int t = 0; t < x.length; t++) {
            if (!gap[t] && Double.isFinite(x[t]) && Double.isFinite(mu0[t]) && mu0[t] > 0) {
                sum += (x[t] - mu0[t]) * (x[t] - mu0[t]) / mu0[t];
                meanMu += mu0[t];
                k++;
            }
        }
        if (k < 10) {
            return Double.NaN;
        }
        double phi = sum / k;
        if (phi <= OVERDISPERSION) {
            return Double.NaN;
        }
        return Math.max(0.5, (meanMu / k) / (phi - 1));
    }

    private static double levelBefore(double[] x, boolean[] gap, double[] mu0, int change, boolean counts) {
        if (counts) {
            return Double.isFinite(mu0[change]) ? mu0[change] : Double.NaN;
        }
        List<Double> w = new ArrayList<>();
        for (int t = change - 1; t >= 0 && w.size() < CONTINUOUS_WINDOW; t--) {
            if (!gap[t] && Double.isFinite(x[t])) {
                w.add(x[t]);
            }
        }
        return RobustStats.median(w.stream().mapToDouble(Double::doubleValue).toArray());
    }

    private static double bocpd(double[] x, boolean[] gap, boolean counts, double typical, double[] sigma, int change,
            Params p) {
        List<Double> used = new ArrayList<>();
        int sinceChange = 0;
        for (int t = 0; t < x.length; t++) {
            if (!gap[t] && Double.isFinite(x[t])) {
                used.add(x[t]);
                if (t >= change) {
                    sinceChange++;
                }
            }
        }
        if (used.isEmpty()) {
            return Double.NaN;
        }
        double[] values = used.stream().mapToDouble(Double::doubleValue).toArray();
        double location = counts ? (Double.isFinite(typical) ? typical : RobustStats.median(values))
                : RobustStats.median(values);
        double scale = counts ? 1 : RobustStats.scale(values, 1e-9 + 1e-3 * Math.abs(location));
        Bocpd.Params params = new Bocpd.Params(counts ? Bocpd.Model.POISSON_GAMMA : Bocpd.Model.NORMAL_GAMMA,
                p.hazard(), p.truncation(), location, scale, 1);
        return Bocpd.run(values, params).changeWithin(sinceChange + 1);
    }
}
