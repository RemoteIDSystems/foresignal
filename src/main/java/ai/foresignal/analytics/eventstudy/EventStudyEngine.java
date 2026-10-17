package ai.foresignal.analytics.eventstudy;

import ai.foresignal.analytics.math.CorradoRank;
import ai.foresignal.analytics.math.PlaceboSampler;
import ai.foresignal.analytics.math.RobustStats;
import ai.foresignal.analytics.series.SeriesFrequency;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.statistics.distribution.NormalDistribution;

/**
 * The event-study engine (prompt 119, {@code event-study} 1.0.0, Intelligence-Design §5.3): pure and deterministic for
 * identical input and seed (INT-1).
 *
 * <ul>
 *   <li><b>Transform</b> by series family: prices to log returns; volume, open interest and attention to
 *       {@code log(1 + V)}; net positions (and other levels) to first differences; counts to the Anscombe transform.
 *       Abnormal values are deviations from the estimation-window mean in its standard deviations (a constant-mean
 *       model), so every statistic is standardized.
 *   <li><b>Windows</b> in buckets of the series' frequency: daily estimation {@code [−250, −11]} (at least 60 points),
 *       pre-event {@code [−5, −1]}, event {@code [0, +1]}; weekly {@code [−52, −3]} (at least 26), {@code [−2, −1]},
 *       {@code [0, +1]}. An event whose pre-event window has a missing or uncovered point is excluded.
 *   <li><b>CAR</b> per event and window (in standard deviations); events whose windows overlap are clustered (their
 *       CARs averaged) before the cross-event average (<b>CAAR</b>).
 *   <li><b>Tests:</b> the standardized test {@code Σ CARᵢ/√L / √N}, the Corrado rank test, and a placebo test: the
 *       same statistic at up to 500 deterministic pseudo-event positions from periods without qualifying events (the
 *       coincidence baseline), its distribution summarized.
 * </ul>
 */
public final class EventStudyEngine {

    /** The detector name. */
    public static final String NAME = "event-study";

    /** The detector version. */
    public static final String VERSION = "1.0.0";

    private static final NormalDistribution STANDARD = NormalDistribution.of(0, 1);

    /** The transforms. */
    public enum Transform {
        /** Log returns (prices). */
        LOG_RETURN("log-return"),
        /** {@code log(1 + V)} standardized (volume, open interest, attention). */
        LOG_LEVEL("log-level-standardised"),
        /** First differences standardized (net positions, levels). */
        DIFFERENCE("weekly-difference-standardised"),
        /** Anscombe-transformed counts. */
        ANSCOMBE("anscombe-residual");

        private final String wire;

        Transform(String wire) {
            this.wire = wire;
        }

        /** The wire value. */
        public String wire() {
            return wire;
        }

        /**
         * The transform of a series: counts are Anscombe; then by the 117 transform hint, else by family.
         *
         * @param counts true for count series
         * @param hint the 117 transform hint ({@code log-return}, {@code log-level}, {@code difference},
         *     {@code level}), or null
         * @param family the 117 family, or null
         */
        public static Transform of(boolean counts, String hint, String family) {
            if (counts) {
                return ANSCOMBE;
            }
            if (hint != null) {
                switch (hint) {
                    case "log-return" -> {
                        return LOG_RETURN;
                    }
                    case "log-level" -> {
                        return LOG_LEVEL;
                    }
                    default -> {
                        return DIFFERENCE;
                    }
                }
            }
            if ("price".equals(family)) {
                return LOG_RETURN;
            }
            if ("volume".equals(family) || "open-interest".equals(family) || "attention".equals(family)) {
                return LOG_LEVEL;
            }
            return DIFFERENCE;
        }
    }

    /**
     * Windows in buckets (inclusive offsets).
     *
     * @param estFrom estimation start
     * @param estTo estimation end
     * @param preFrom pre-event start
     * @param preTo pre-event end
     * @param evFrom event start
     * @param evTo event end
     * @param minEstimation the fewest estimation points
     */
    public record Windows(int estFrom, int estTo, int preFrom, int preTo, int evFrom, int evTo, int minEstimation) {

        /** Daily windows. */
        public static final Windows DAILY = new Windows(-250, -11, -5, -1, 0, 1, 60);

        /** Weekly windows. */
        public static final Windows WEEKLY = new Windows(-52, -3, -2, -1, 0, 1, 26);

        /** The windows of a frequency (weekly for weeks, daily otherwise). */
        public static Windows of(SeriesFrequency frequency) {
            return frequency == SeriesFrequency.WEEK ? WEEKLY : DAILY;
        }

        /** The span of the test windows, buckets. */
        int span() {
            return evTo - preFrom;
        }

        /** The {@code windows} JSON. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("estimation", List.of(estFrom, estTo));
            out.put("preEvent", List.of(preFrom, preTo));
            out.put("event", List.of(evFrom, evTo));
            return out;
        }
    }

    /**
     * One event.
     *
     * @param at the event time
     * @param ref an object reference of the event, or null
     */
    public record Event(Instant at, String ref) {}

    /**
     * The input of a study.
     *
     * @param starts the series' bucket starts
     * @param values the raw values ({@code NaN} where missing or uncovered)
     * @param frequency the frequency
     * @param transform the transform
     * @param windows the windows
     * @param events the events
     * @param otherEvents qualifying event times that are not studied but must not host a placebo date
     * @param seed the placebo seed
     * @param placeboCount the number of placebo dates (500)
     * @param needEventWindow false for live pre-event checks (the event window may not exist yet)
     */
    public record Input(Instant[] starts, double[] values, SeriesFrequency frequency, Transform transform,
            Windows windows, List<Event> events, List<Instant> otherEvents, long seed, int placeboCount,
            boolean needEventWindow) {}

    /**
     * One event's outcome.
     *
     * @param at the event time
     * @param ref the event reference, or null
     * @param index the event bucket, or −1
     * @param clusterId the cluster (0-based), or −1 when excluded
     * @param excluded why it was excluded, or null
     * @param estimationPoints the estimation points used
     * @param abnormal standardized abnormal values at offsets {@code preFrom … evTo} ({@code NaN} where missing)
     * @param carPre the pre-event CAR in standard deviations, or {@code NaN}
     * @param carEvent the event-window CAR, or {@code NaN}
     */
    public record EventOutcome(Instant at, String ref, int index, int clusterId, String excluded, int estimationPoints,
            double[] abnormal, double carPre, double carEvent) {}

    /**
     * Cross-event statistics of one window.
     *
     * @param from the first offset
     * @param to the last offset
     * @param caar the CAAR in standard deviations
     * @param z the standardized statistic
     * @param p its two-sided p-value
     * @param corradoStat the Corrado statistic
     * @param corradoP its p-value
     * @param placeboP the share of placebo values at least as extreme
     * @param placebo the placebo values of the statistic
     */
    public record WindowStats(int from, int to, double caar, double z, double p, double corradoStat, double corradoP,
            double placeboP, double[] placebo) {

        /** The {@code EventStudyWindowStats} JSON. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("window", List.of(from, to));
            out.put("caar", finite(caar));
            out.put("standardisedZ", finite(z));
            out.put("standardisedP", finite(p));
            out.put("corradoStat", finite(corradoStat));
            out.put("corradoP", finite(corradoP));
            out.put("placeboP", finite(placeboP));
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("count", placebo.length);
            summary.put("mean", finite(RobustStats.mean(placebo)));
            summary.put("sd", finite(RobustStats.sd(placebo)));
            summary.put("p05", finite(RobustStats.quantile(placebo, 0.05)));
            summary.put("p50", finite(RobustStats.quantile(placebo, 0.5)));
            summary.put("p95", finite(RobustStats.quantile(placebo, 0.95)));
            out.put("placebo", summary);
            return out;
        }
    }

    /**
     * The result.
     *
     * @param events every event's outcome
     * @param clusters the number of clusters
     * @param pre the pre-event window statistics
     * @param event the event window statistics
     * @param placeboDates the placebo positions used
     */
    public record Result(List<EventOutcome> events, int clusters, WindowStats pre, WindowStats event,
            int placeboDates) {

        /** The events that were used. */
        public int used() {
            return (int) events.stream().filter(e -> e.excluded() == null).count();
        }

        /** The events that were excluded. */
        public int excluded() {
            return events.size() - used();
        }
    }

    private EventStudyEngine() {}

    /** Runs a study. */
    public static Result run(Input in) {
        double[] x = transform(in.values(), in.transform());
        Windows w = in.windows();
        int n = x.length;
        List<EventOutcome> outcomes = new ArrayList<>();
        for (Event e : in.events()) {
            int index = indexOf(in.starts(), in.frequency(), e.at());
            outcomes.add(evaluate(x, index, w, e, in.needEventWindow()));
        }
        outcomes.sort(Comparator.comparing(EventOutcome::at));
        // Cluster overlapping events.
        List<List<Integer>> clusters = new ArrayList<>();
        int anchor = Integer.MIN_VALUE;
        List<EventOutcome> clustered = new ArrayList<>();
        for (EventOutcome o : outcomes) {
            if (o.excluded() != null) {
                clustered.add(o);
                continue;
            }
            if (clusters.isEmpty() || o.index() - anchor > w.span()) {
                clusters.add(new ArrayList<>());
                anchor = o.index();
            }
            int id = clusters.size() - 1;
            clusters.get(id).add(clustered.size());
            clustered.add(new EventOutcome(o.at(), o.ref(), o.index(), id, null, o.estimationPoints(), o.abnormal(),
                    o.carPre(), o.carEvent()));
        }
        int nc = clusters.size();
        double[] carPre = new double[nc];
        double[] carEv = new double[nc];
        double[][] rankRows = new double[nc][];
        for (int c = 0; c < nc; c++) {
            List<Integer> members = clusters.get(c);
            double sp = 0;
            double se = 0;
            int ke = 0;
            for (int m : members) {
                sp += clustered.get(m).carPre();
                if (Double.isFinite(clustered.get(m).carEvent())) {
                    se += clustered.get(m).carEvent();
                    ke++;
                }
            }
            carPre[c] = sp / members.size();
            carEv[c] = ke == 0 ? Double.NaN : se / ke;
            rankRows[c] = rankRow(x, clustered.get(members.get(0)).index(), w);
        }
        // Placebo positions: full windows, away from every qualifying event.
        List<Integer> excludedIndexes = new ArrayList<>();
        for (EventOutcome o : outcomes) {
            if (o.index() >= 0) {
                excludedIndexes.add(o.index());
            }
        }
        for (Instant t : in.otherEvents() == null ? List.<Instant>of() : in.otherEvents()) {
            int i = indexOf(in.starts(), in.frequency(), t);
            if (i >= 0) {
                excludedIndexes.add(i);
            }
        }
        int radius = w.span() + 10;
        List<Integer> eligible = new ArrayList<>();
        int last = in.needEventWindow() ? w.evTo() : w.preTo();
        for (int p = -w.estFrom(); p + last < n; p++) {
            boolean near = false;
            for (int e : excludedIndexes) {
                if (Math.abs(p - e) <= radius) {
                    near = true;
                    break;
                }
            }
            if (!near) {
                eligible.add(p);
            }
        }
        int[] chosen = PlaceboSampler.sample(eligible.stream().mapToInt(Integer::intValue).toArray(),
                in.placeboCount(), in.seed());
        List<Double> placeboPre = new ArrayList<>();
        List<Double> placeboEv = new ArrayList<>();
        for (int p : chosen) {
            EventOutcome o = evaluate(x, p, w, new Event(in.starts()[p], null), in.needEventWindow());
            if (o.excluded() == null) {
                placeboPre.add(o.carPre());
                if (Double.isFinite(o.carEvent())) {
                    placeboEv.add(o.carEvent());
                }
            }
        }
        WindowStats pre = stats(carPre, w.preFrom(), w.preTo(), rankRows, w, placeboPre, nc, in.seed());
        WindowStats ev = stats(carEv, w.evFrom(), w.evTo(), rankRows, w, placeboEv, nc, in.seed() + 1);
        return new Result(List.copyOf(clustered), nc, pre, ev, chosen.length);
    }

    /** The transformed series. */
    static double[] transform(double[] v, Transform t) {
        int n = v.length;
        double[] x = new double[n];
        Arrays.fill(x, Double.NaN);
        for (int i = 0; i < n; i++) {
            switch (t) {
                case LOG_RETURN -> {
                    if (i > 0 && v[i] > 0 && v[i - 1] > 0 && Double.isFinite(v[i]) && Double.isFinite(v[i - 1])) {
                        x[i] = Math.log(v[i] / v[i - 1]);
                    }
                }
                case LOG_LEVEL -> {
                    if (Double.isFinite(v[i]) && v[i] > -1) {
                        x[i] = Math.log1p(v[i]);
                    }
                }
                case DIFFERENCE -> {
                    if (i > 0 && Double.isFinite(v[i]) && Double.isFinite(v[i - 1])) {
                        x[i] = v[i] - v[i - 1];
                    }
                }
                case ANSCOMBE -> {
                    if (Double.isFinite(v[i])) {
                        x[i] = RobustStats.anscombe(v[i]);
                    }
                }
            }
        }
        return x;
    }

    /** The bucket containing {@code at}, or −1 when outside the series. */
    static int indexOf(Instant[] starts, SeriesFrequency frequency, Instant at) {
        if (starts.length == 0 || at == null) {
            return -1;
        }
        Instant bucket = frequency.regular() ? frequency.floor(at) : at;
        int i = Arrays.binarySearch(starts, bucket);
        if (i >= 0) {
            return i;
        }
        int insertion = -i - 1;
        return frequency.regular() ? -1 : Math.max(0, insertion - 1);
    }

    private static EventOutcome evaluate(double[] x, int index, Windows w, Event e, boolean needEventWindow) {
        int n = x.length;
        int width = w.evTo() - w.preFrom() + 1;
        double[] abnormal = new double[width];
        Arrays.fill(abnormal, Double.NaN);
        if (index < 0) {
            return new EventOutcome(e.at(), e.ref(), index, -1, "outside the series", 0, abnormal, Double.NaN,
                    Double.NaN);
        }
        if (index + w.estFrom() < 0) {
            return new EventOutcome(e.at(), e.ref(), index, -1, "FEW_EVENTS: the estimation window starts before the"
                    + " series", 0, abnormal, Double.NaN, Double.NaN);
        }
        int lastNeeded = needEventWindow ? w.evTo() : w.preTo();
        if (index + lastNeeded >= n) {
            return new EventOutcome(e.at(), e.ref(), index, -1, "the window runs past the series", 0, abnormal,
                    Double.NaN, Double.NaN);
        }
        double[] est = Arrays.copyOfRange(x, index + w.estFrom(), index + w.estTo() + 1);
        int points = RobustStats.count(est);
        if (points < w.minEstimation()) {
            return new EventOutcome(e.at(), e.ref(), index, -1, "FEW_EVENTS: " + points + " estimation points, fewer"
                    + " than " + w.minEstimation(), points, abnormal, Double.NaN, Double.NaN);
        }
        double mean = RobustStats.mean(est);
        double sd = RobustStats.sd(est);
        if (!(sd > 0)) {
            return new EventOutcome(e.at(), e.ref(), index, -1, "the estimation window does not vary", points,
                    abnormal, Double.NaN, Double.NaN);
        }
        for (int k = 0; k < width; k++) {
            int t = index + w.preFrom() + k;
            if (t >= 0 && t < n && Double.isFinite(x[t])) {
                abnormal[k] = (x[t] - mean) / sd;
            }
        }
        double carPre = 0;
        for (int off = w.preFrom(); off <= w.preTo(); off++) {
            double a = abnormal[off - w.preFrom()];
            if (!Double.isFinite(a)) {
                return new EventOutcome(e.at(), e.ref(), index, -1, "coverage gap in the pre-event window", points,
                        abnormal, Double.NaN, Double.NaN);
            }
            carPre += a;
        }
        double carEvent = 0;
        boolean eventOk = true;
        for (int off = w.evFrom(); off <= w.evTo(); off++) {
            double a = abnormal[off - w.preFrom()];
            if (!Double.isFinite(a)) {
                eventOk = false;
                break;
            }
            carEvent += a;
        }
        if (needEventWindow && !eventOk) {
            return new EventOutcome(e.at(), e.ref(), index, -1, "coverage gap in the event window", points, abnormal,
                    carPre, Double.NaN);
        }
        return new EventOutcome(e.at(), e.ref(), index, 0, null, points, abnormal, carPre,
                eventOk ? carEvent : Double.NaN);
    }

    /** One cluster's standardized abnormal values over estimation through event window, for the rank test. */
    private static double[] rankRow(double[] x, int index, Windows w) {
        int width = w.evTo() - w.estFrom() + 1;
        double[] row = new double[width];
        Arrays.fill(row, Double.NaN);
        for (int k = 0; k < width; k++) {
            int t = index + w.estFrom() + k;
            if (t >= 0 && t < x.length && Double.isFinite(x[t])) {
                row[k] = x[t];
            }
        }
        return row;
    }

    private static WindowStats stats(double[] car, int from, int to, double[][] rankRows, Windows w,
            List<Double> placebo, int clusters, long seed) {
        int length = to - from + 1;
        double sum = 0;
        double zsum = 0;
        int k = 0;
        for (double c : car) {
            if (Double.isFinite(c)) {
                sum += c;
                zsum += c / Math.sqrt(length);
                k++;
            }
        }
        double caar = k == 0 ? Double.NaN : sum / k;
        double z = k == 0 ? Double.NaN : zsum / Math.sqrt(k);
        double p = Double.isFinite(z) ? 2 * STANDARD.survivalProbability(Math.abs(z)) : Double.NaN;
        CorradoRank.Result corrado = rankRows.length == 0 ? new CorradoRank.Result(Double.NaN, Double.NaN)
                : CorradoRank.test(rankRows, from - w.estFrom(), to - w.estFrom());
        double[] single = placebo.stream().mapToDouble(Double::doubleValue).toArray();
        double[] distribution = k <= 1 ? single : PlaceboSampler.resampledMeans(single, k, single.length, seed);
        double placeboP = PlaceboSampler.share(distribution, caar);
        return new WindowStats(from, to, caar, z, p, corrado.statistic(), corrado.p(), placeboP, distribution);
    }

    static Double finite(double value) {
        return Double.isFinite(value) ? value : null;
    }
}
