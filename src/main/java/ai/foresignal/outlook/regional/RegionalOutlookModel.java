package ai.foresignal.outlook.regional;

import ai.foresignal.analytics.math.NbGlm;
import ai.foresignal.analytics.math.NbSampling;
import ai.foresignal.outlook.series.QuantileSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The regional outlook model (prompt 126, {@code regional-outlook} 1.0.0, Intelligence-Design §9.2), in the style of
 * ACLED CAST. Pure and seeded.
 *
 * <p>Per region and family, a negative-binomial GLM on weekly counts {@code y(t)} with
 * <pre>
 *   log μ(t) = β₀ + β₁·log1p y(t−1) + β₂·log1p y(t−2) + β₃·log1p y(t−4) + β₄·log1p(mean y(t−13…t−1))
 *            + β₅·sin(2π·doy/365.25) + β₆·cos(2π·doy/365.25)
 *            [+ β₇·Σ_neighbors log1p y_n(t−1)] [+ β₈·index(t−1)/100]
 * </pre>
 * fitted by IRLS ({@link NbGlm}). The region effect is approximated by partial pooling: the intercept is shrunk
 * toward the mean intercept of the region's class by empirical Bayes ({@link #shrink}). Regions with fewer than
 * {@value #MIN_WEEKS} weeks, no events, or a failed fit use the fallback: an intercept-only NB with the mean and
 * moment dispersion of the last 52 weeks.
 *
 * <p>Forecasts: {@value #DRAWS} coefficient draws from {@code N(β̂, Cov)} × {@value #PATHS_PER_DRAW} simulated paths
 * forward from the end of the history (neighbors' counts and the index held at their last values), summed over each
 * 4-week window; the window quantiles are the empirical quantiles of those sums.
 *
 * <p>Evaluation ({@link #evaluate}): rolling origins over the last {@value #EVAL_WEEKS} weeks, each refitting on the
 * weeks before it and forecasting the next 4-week window, against the seasonal-naive window (the same four weeks a
 * year earlier, with the spread of past year-on-year differences) and the climatology of the same window in past
 * years (±2 weeks).
 */
public final class RegionalOutlookModel {

    /** The model name. */
    public static final String NAME = "regional-outlook";

    /** The model version. */
    public static final String VERSION = "1.0.0";

    /** Windows published. */
    public static final int WINDOWS = 6;

    /** Weeks per window. */
    public static final int WINDOW_WEEKS = 4;

    /** Weeks of history a regression needs (else the fallback). */
    public static final int MIN_WEEKS = 52;

    /** The evaluation span, weeks. */
    public static final int EVAL_WEEKS = 26;

    /** Coefficient draws per forecast. */
    public static final int DRAWS = 100;

    /** Simulated paths per draw. */
    public static final int PATHS_PER_DRAW = 5;

    /** The longest lag of the design. */
    static final int MAX_LAG = 13;

    private RegionalOutlookModel() {}

    /**
     * One region's inputs.
     *
     * @param counts weekly counts
     * @param firstWeek the Monday of {@code counts[0]}
     * @param neighbors {@code Σ log1p} of the neighbors' counts per week (same length; NaN unknown), or null
     * @param index the region's index value per week (0–100; NaN unknown), or null
     */
    public record Input(long[] counts, LocalDate firstWeek, double[] neighbors, double[] index) {

        /** The number of weeks. */
        public int size() {
            return counts.length;
        }

        /** True when the neighbor term is usable. */
        public boolean withNeighbors() {
            return finiteAny(neighbors);
        }

        /** True when the index term is usable. */
        public boolean withIndex() {
            return finiteAny(index);
        }
    }

    /**
     * A fit.
     *
     * @param beta the coefficients (empty for the fallback)
     * @param covariance their covariance
     * @param theta the NB size
     * @param fallback true for the intercept-only fallback
     * @param fallbackMean the weekly mean of the fallback
     * @param withNeighbors true with the neighbor term
     * @param withIndex true with the index term
     * @param n the rows fitted
     * @param interceptVariance the intercept's variance (for pooling)
     */
    public record Fit(double[] beta, double[][] covariance, double theta, boolean fallback, double fallbackMean,
            boolean withNeighbors, boolean withIndex, int n, double interceptVariance) {

        /** The coefficients and settings as a map. */
        public Map<String, Object> view() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("model", fallback ? "fallback-mean" : "nb-glm");
            List<Double> b = new ArrayList<>();
            for (double v : beta) {
                b.add(QuantileSet.round(v));
            }
            out.put("coefficients", b);
            out.put("terms", terms(withNeighbors, withIndex));
            out.put("theta", QuantileSet.round(theta));
            out.put("fallbackMean", fallback ? QuantileSet.round(fallbackMean) : null);
            out.put("rows", n);
            return out;
        }

        /** This fit with its intercept replaced. */
        public Fit withIntercept(double intercept) {
            if (fallback || beta.length == 0) {
                return this;
            }
            double[] b = beta.clone();
            b[0] = intercept;
            return new Fit(b, covariance, theta, false, fallbackMean, withNeighbors, withIndex, n, interceptVariance);
        }
    }

    /** The names of the design's terms. */
    public static List<String> terms(boolean withNeighbors, boolean withIndex) {
        List<String> out = new ArrayList<>(List.of("intercept", "lag1", "lag2", "lag4", "ma13", "sinAnnual",
                "cosAnnual"));
        if (withNeighbors) {
            out.add("neighbors");
        }
        if (withIndex) {
            out.add("index");
        }
        return out;
    }

    /**
     * The design row of week {@code t}.
     *
     * @param y the counts (history and simulated weeks)
     * @param t the week (≥ {@value #MAX_LAG})
     * @param week the Monday of week {@code t}
     * @param neighborsLag the neighbor term at {@code t − 1}
     * @param indexLag the index at {@code t − 1}
     */
    public static double[] row(double[] y, int t, LocalDate week, double neighborsLag, double indexLag,
            boolean withNeighbors, boolean withIndex) {
        double[] r = new double[7 + (withNeighbors ? 1 : 0) + (withIndex ? 1 : 0)];
        r[0] = 1;
        r[1] = Math.log1p(y[t - 1]);
        r[2] = Math.log1p(y[t - 2]);
        r[3] = Math.log1p(y[t - 4]);
        double ma = 0;
        for (int k = 1; k <= MAX_LAG; k++) {
            ma += y[t - k];
        }
        r[4] = Math.log1p(ma / MAX_LAG);
        double a = 2 * Math.PI * week.getDayOfYear() / 365.25;
        r[5] = Math.sin(a);
        r[6] = Math.cos(a);
        int i = 7;
        if (withNeighbors) {
            r[i++] = Double.isFinite(neighborsLag) ? neighborsLag : 0;
        }
        if (withIndex) {
            r[i] = Double.isFinite(indexLag) ? indexLag / 100 : 0;
        }
        return r;
    }

    /** Fits the model on weeks {@code [0, end)} of {@code in}. */
    public static Fit fit(Input in, int end) {
        double[] y = asDouble(in.counts(), end);
        boolean nb = in.withNeighbors();
        boolean idx = in.withIndex();
        int rows = end - MAX_LAG;
        double total = 0;
        for (int t = 0; t < end; t++) {
            total += y[t];
        }
        if (end < MIN_WEEKS || rows < 26 || total <= 0) {
            return fallback(y, end, nb, idx);
        }
        double[][] x = new double[rows][];
        double[] target = new double[rows];
        double[] nbFilled = carry(in.neighbors(), end);
        double[] idxFilled = carry(in.index(), end);
        for (int t = MAX_LAG; t < end; t++) {
            x[t - MAX_LAG] = row(y, t, in.firstWeek().plusWeeks(t), nbFilled == null ? Double.NaN : nbFilled[t - 1],
                    idxFilled == null ? Double.NaN : idxFilled[t - 1], nb, idx);
            target[t - MAX_LAG] = y[t];
        }
        try {
            NbGlm.Fit f = NbGlm.fit(x, target);
            boolean finite = Arrays.stream(f.beta()).allMatch(Double::isFinite);
            if (!finite) {
                return fallback(y, end, nb, idx);
            }
            return new Fit(f.beta(), f.covariance(), f.theta(), false, Double.NaN, nb, idx, rows,
                    f.covariance()[0][0]);
        } catch (RuntimeException e) {
            return fallback(y, end, nb, idx);
        }
    }

    /** The intercept-only fallback over the last 52 weeks before {@code end}. */
    static Fit fallback(double[] y, int end, boolean nb, boolean idx) {
        int from = Math.max(0, end - 52);
        int n = end - from;
        double mean = 0;
        for (int t = from; t < end; t++) {
            mean += y[t];
        }
        mean = n == 0 ? 0 : mean / n;
        double var = 0;
        for (int t = from; t < end; t++) {
            var += (y[t] - mean) * (y[t] - mean);
        }
        var = n > 1 ? var / (n - 1) : 0;
        double theta = var > mean && mean > 0 ? Math.max(NbGlm.THETA_MIN, mean * mean / (var - mean))
                : NbGlm.THETA_MAX;
        return new Fit(new double[0], new double[0][0], theta, true, mean, nb, idx, n, Double.NaN);
    }

    /**
     * Empirical Bayes shrinkage of each region's intercept toward its class mean: with {@code v_r} the intercept's
     * variance and {@code τ²} the between-region variance of the class ({@code max(0, Var(β₀) − mean v)}), the pooled
     * intercept is {@code (τ²·β₀ + v·m) / (τ² + v)}. Classes with one region and fallbacks are left unchanged.
     *
     * @param fits the fits by region
     * @param classes the class of each region
     * @return the pooled fits by region
     */
    public static Map<String, Fit> shrink(Map<String, Fit> fits, Map<String, String> classes) {
        Map<String, List<String>> members = new LinkedHashMap<>();
        for (Map.Entry<String, Fit> e : fits.entrySet()) {
            if (!e.getValue().fallback() && Double.isFinite(e.getValue().interceptVariance())) {
                members.computeIfAbsent(classes.getOrDefault(e.getKey(), "unknown"), k -> new ArrayList<>())
                        .add(e.getKey());
            }
        }
        Map<String, Fit> out = new LinkedHashMap<>(fits);
        for (List<String> group : members.values()) {
            if (group.size() < 2) {
                continue;
            }
            double m = 0;
            double v = 0;
            for (String r : group) {
                m += fits.get(r).beta()[0];
                v += Math.max(0, fits.get(r).interceptVariance());
            }
            m /= group.size();
            v /= group.size();
            double var = 0;
            for (String r : group) {
                double d = fits.get(r).beta()[0] - m;
                var += d * d;
            }
            var /= group.size() - 1;
            double tau2 = Math.max(0, var - v);
            for (String r : group) {
                Fit f = fits.get(r);
                double vr = Math.max(1e-12, f.interceptVariance());
                double pooled = (tau2 * f.beta()[0] + vr * m) / (tau2 + vr);
                out.put(r, f.withIntercept(pooled));
            }
        }
        return out;
    }

    /**
     * Simulates the window sums: {@code windows} windows of {@value #WINDOW_WEEKS} weeks, the first starting
     * {@code offset} weeks after the end of the history ({@code end}).
     *
     * @return per window the nine quantiles of {@link QuantileSet#LEVELS} (monotone)
     */
    public static double[][] windows(Fit fit, Input in, int end, int offset, int windows, long seed, int draws) {
        int horizon = offset + windows * WINDOW_WEEKS;
        double[][] sums = simulate(fit, in, end, horizon, seed, draws);
        double[][] out = new double[windows][];
        int paths = sums.length;
        for (int w = 0; w < windows; w++) {
            double[] s = new double[paths];
            for (int p = 0; p < paths; p++) {
                double total = 0;
                for (int k = 0; k < WINDOW_WEEKS; k++) {
                    total += sums[p][offset + w * WINDOW_WEEKS + k];
                }
                s[p] = total;
            }
            out[w] = QuantileSet.monotone(QuantileSet.empirical(s));
        }
        return out;
    }

    /** Simulated weekly counts, {@code paths × horizon}, after week {@code end − 1}. */
    static double[][] simulate(Fit fit, Input in, int end, int horizon, long seed, int draws) {
        NbSampling sampler = new NbSampling(seed);
        int paths = draws * PATHS_PER_DRAW;
        double[][] out = new double[paths][horizon];
        if (fit.fallback()) {
            for (int p = 0; p < paths; p++) {
                for (int k = 0; k < horizon; k++) {
                    out[p][k] = sampler.negBin(fit.fallbackMean(), fit.theta());
                }
            }
            return out;
        }
        double[] history = asDouble(in.counts(), end);
        double[] nbFilled = carry(in.neighbors(), end);
        double[] idxFilled = carry(in.index(), end);
        double nbLast = nbFilled == null || end == 0 ? Double.NaN : nbFilled[end - 1];
        double idxLast = idxFilled == null || end == 0 ? Double.NaN : idxFilled[end - 1];
        double[][] chol = NbGlm.cholesky(fit.covariance());
        double[] y = Arrays.copyOf(history, end + horizon);
        int p = 0;
        for (int d = 0; d < draws; d++) {
            double[] beta = sampler.multivariateNormal(fit.beta(), chol);
            for (int k = 0; k < PATHS_PER_DRAW; k++) {
                for (int s = 0; s < horizon; s++) {
                    int t = end + s;
                    double[] r = row(y, t, in.firstWeek().plusWeeks(t), nbLast, idxLast, fit.withNeighbors(),
                            fit.withIndex());
                    double eta = 0;
                    for (int j = 0; j < beta.length; j++) {
                        eta += beta[j] * r[j];
                    }
                    double mu = Math.exp(Math.max(-30, Math.min(30, eta)));
                    y[t] = sampler.negBin(mu, fit.theta());
                    out[p][s] = y[t];
                }
                p++;
            }
        }
        return out;
    }

    /**
     * A rolling-origin evaluation.
     *
     * @param n the origins scored
     * @param pinballModel the mean pinball loss of the model's 4-week window
     * @param pinballSeasonalNaive that of the seasonal-naive window
     * @param pinballClimatology that of the climatology window
     * @param coverageP10P90 the share of actuals inside the model's p10–p90
     */
    public record Evaluation(int n, double pinballModel, double pinballSeasonalNaive, double pinballClimatology,
            double coverageP10P90) {

        /** {@code 1 − model/seasonal-naive}, or null. */
        public Double skillVsSeasonalNaive() {
            return n == 0 || !(pinballSeasonalNaive > 0) || !Double.isFinite(pinballSeasonalNaive) ? null
                    : 1 - pinballModel / pinballSeasonalNaive;
        }

        /** {@code 1 − model/climatology}, or null. */
        public Double skillVsClimatology() {
            return n == 0 || !(pinballClimatology > 0) || !Double.isFinite(pinballClimatology) ? null
                    : 1 - pinballModel / pinballClimatology;
        }

        /** The stored map. */
        public Map<String, Object> view() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("evaluatedWeeks", EVAL_WEEKS);
            out.put("n", n);
            out.put("meanPinballLoss", n == 0 ? null : QuantileSet.round(pinballModel));
            out.put("seasonalNaivePinballLoss", n == 0 || !Double.isFinite(pinballSeasonalNaive) ? null
                    : QuantileSet.round(pinballSeasonalNaive));
            out.put("climatologyPinballLoss", n == 0 || !Double.isFinite(pinballClimatology) ? null
                    : QuantileSet.round(pinballClimatology));
            Double sn = skillVsSeasonalNaive();
            Double cl = skillVsClimatology();
            out.put("skillVsSeasonalNaive", sn == null ? null : QuantileSet.round(sn));
            out.put("skillVsClimatology", cl == null ? null : QuantileSet.round(cl));
            out.put("intervalCoverageP10P90", n == 0 ? null : QuantileSet.round(coverageP10P90));
            return out;
        }
    }

    /** Rolling-origin evaluation of the next 4-week window over the last {@value #EVAL_WEEKS} weeks. */
    public static Evaluation evaluate(Input in, long seed, int draws) {
        int n = in.size();
        double[] y = asDouble(in.counts(), n);
        double model = 0;
        double sn = 0;
        double cl = 0;
        int snN = 0;
        int clN = 0;
        int inside = 0;
        int scored = 0;
        for (int o = Math.max(MIN_WEEKS, n - EVAL_WEEKS); o + WINDOW_WEEKS <= n; o++) {
            Fit f = fit(in, o);
            double[] q = windows(f, in, o, 0, 1, seed + o, draws)[0];
            double actual = 0;
            for (int k = 0; k < WINDOW_WEEKS; k++) {
                actual += y[o + k];
            }
            model += QuantileSet.meanPinball(actual, q);
            inside += actual >= q[QuantileSet.P10] && actual <= q[QuantileSet.P90] ? 1 : 0;
            scored++;
            double[] naive = seasonalNaive(y, o);
            if (naive != null) {
                sn += QuantileSet.meanPinball(actual, naive);
                snN++;
            }
            double[] clim = climatology(y, o);
            if (clim != null) {
                cl += QuantileSet.meanPinball(actual, clim);
                clN++;
            }
        }
        if (scored == 0) {
            return new Evaluation(0, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        }
        return new Evaluation(scored, model / scored, snN == 0 ? Double.NaN : sn / snN,
                clN == 0 ? Double.NaN : cl / clN, (double) inside / scored);
    }

    /**
     * The seasonal-naive quantiles of the 4-week window starting at {@code o}: the same window 52 weeks earlier
     * plus the empirical spread of past year-on-year differences of 4-week sums, floored at zero; null without two
     * years of history.
     */
    public static double[] seasonalNaive(double[] y, int o) {
        if (o < 52 + WINDOW_WEEKS + 8) {
            return null;
        }
        double base = sum(y, o - 52, WINDOW_WEEKS);
        List<Double> diffs = new ArrayList<>();
        for (int s = 52; s + WINDOW_WEEKS <= o; s++) {
            diffs.add(sum(y, s, WINDOW_WEEKS) - sum(y, s - 52, WINDOW_WEEKS));
        }
        if (diffs.size() < 8) {
            return null;
        }
        double[] d = diffs.stream().mapToDouble(Double::doubleValue).toArray();
        double[] q = QuantileSet.empirical(d);
        return QuantileSet.monotone(QuantileSet.floor(QuantileSet.map(q, v -> base + v), 0));
    }

    /**
     * The climatology quantiles of the 4-week window starting at {@code o}: the same window in past years shifted by
     * up to 2 weeks; null with fewer than 5 such windows.
     */
    public static double[] climatology(double[] y, int o) {
        List<Double> sample = new ArrayList<>();
        for (int k = 1; o - 52 * k - 2 >= 0; k++) {
            for (int d = -2; d <= 2; d++) {
                int s = o - 52 * k + d;
                if (s >= 0 && s + WINDOW_WEEKS <= o) {
                    sample.add(sum(y, s, WINDOW_WEEKS));
                }
            }
        }
        if (sample.size() < 5) {
            return null;
        }
        return QuantileSet.monotone(QuantileSet.empirical(sample.stream().mapToDouble(Double::doubleValue)
                .toArray()));
    }

    private static double sum(double[] y, int from, int len) {
        double s = 0;
        for (int i = from; i < from + len; i++) {
            s += y[i];
        }
        return s;
    }

    static double[] asDouble(long[] counts, int end) {
        double[] y = new double[end];
        for (int i = 0; i < end; i++) {
            y[i] = Math.max(0, counts[i]);
        }
        return y;
    }

    /** The values up to {@code end} with gaps carried forward (leading gaps 0), or null. */
    static double[] carry(double[] values, int end) {
        if (!finiteAny(values)) {
            return null;
        }
        double[] out = new double[end];
        double last = 0;
        for (int i = 0; i < end; i++) {
            double v = i < values.length ? values[i] : Double.NaN;
            if (Double.isFinite(v)) {
                last = v;
            }
            out[i] = last;
        }
        return out;
    }

    private static boolean finiteAny(double[] values) {
        if (values == null) {
            return false;
        }
        for (double v : values) {
            if (Double.isFinite(v)) {
                return true;
            }
        }
        return false;
    }
}
