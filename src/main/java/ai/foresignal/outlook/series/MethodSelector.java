package ai.foresignal.outlook.series;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Chooses a forecasting method per series by rolling-origin backtest (prompt 126, Intelligence-Design §9.1). Pure.
 *
 * <p>Origins: the last {@value #ORIGINS}, one {@code spacing} apart (a week), the newest placed so that its longest
 * horizon still has an observed target. At each origin every applicable method sees only the buckets before the
 * origin whose value was known by the origin ({@code knownAt}, INT-2), forecasts every horizon, and is scored by the
 * mean pinball loss over the seven ledger quantiles; the interval coverage of p05–p95 is reported too. The method
 * with the lowest mean pinball loss at the selection horizon (7 days) wins; ties go to the earlier method in the
 * candidate list, which starts with the {@code seasonal-naive} benchmark. A method that cannot run at
 * {@value #MIN_ORIGINS} or more origins is not eligible.
 */
public final class MethodSelector {

    /** The backtest origins. */
    public static final int ORIGINS = 8;

    /** The fewest origins a method must be scored at. */
    public static final int MIN_ORIGINS = 6;

    /** The reported horizons, days. */
    public static final int[] HORIZONS = {1, 7, 28};

    /** The selection horizon, days. */
    public static final int SELECTION_HORIZON = 7;

    /** Every method, the benchmark first. */
    public static final List<ForecastMethod> METHODS = List.of(new SeasonalNaive(), new EtsAnn(), new EtsAaa(),
            new NbGlmMethod(), new DampedTrend());

    private MethodSelector() {}

    /**
     * The score of one method at one horizon.
     *
     * @param method the method
     * @param horizon the horizon, days
     * @param meanPinballLoss the mean over origins of the mean pinball loss
     * @param intervalCoverageP05P95 the share of targets inside p05–p95, or null without targets
     * @param n the origins scored
     */
    public record Score(String method, int horizon, double meanPinballLoss, Double intervalCoverageP05P95, int n) {

        /** The {@code SeriesForecastModel.backtest.results} item. */
        public Map<String, Object> view() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("method", method);
            out.put("horizon", horizon);
            out.put("meanPinballLoss", QuantileSet.round(meanPinballLoss));
            out.put("intervalCoverageP05P95", intervalCoverageP05P95 == null ? null
                    : QuantileSet.round(intervalCoverageP05P95));
            out.put("n", n);
            return out;
        }
    }

    /**
     * A selection.
     *
     * @param method the chosen method, or null when no method could be scored
     * @param scores every score
     * @param origins the origins used
     * @param spacing the origin spacing ({@code P7D})
     */
    public record Selection(String method, List<Score> scores, List<Instant> origins, String spacing) {

        /** The {@code SeriesForecastModel.backtest} object. */
        public Map<String, Object> backtest() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("origins", origins.size());
            out.put("originTimes", origins.stream().map(Instant::toString).toList());
            out.put("spacing", spacing);
            out.put("selectionHorizon", SELECTION_HORIZON);
            out.put("results", scores.stream().map(Score::view).toList());
            return out;
        }
    }

    /**
     * Steps per horizon for a bucket length in days ({@code ⌈h / days⌉}, at least 1).
     *
     * @param bucketDays 1 for daily data, 7 for weekly
     */
    public static int[] steps(int bucketDays) {
        int[] out = new int[HORIZONS.length];
        for (int i = 0; i < HORIZONS.length; i++) {
            out[i] = Math.max(1, (HORIZONS[i] + bucketDays - 1) / bucketDays);
        }
        return out;
    }

    /**
     * Runs the backtest.
     *
     * @param starts the bucket starts
     * @param values the values (NaN where missing)
     * @param knownAt when each value became known (null where missing)
     * @param decision the selection time: nothing known after it is read
     * @param counts true for a count series
     * @param season the season length in buckets
     * @param bucketDays the bucket length in days (1 or 7)
     */
    public static Selection select(Instant[] starts, double[] values, Instant[] knownAt, Instant decision,
            boolean counts, int season, int bucketDays) {
        double[] known = new double[values.length];
        int last = -1;
        for (int i = 0; i < values.length; i++) {
            boolean ok = Double.isFinite(values[i]) && (knownAt == null || knownAt[i] == null
                    || !knownAt[i].isAfter(decision)) && (starts == null || starts[i].isBefore(decision));
            known[i] = ok ? values[i] : Double.NaN;
            if (ok) {
                last = i;
            }
        }
        int[] steps = steps(bucketDays);
        int maxStep = Arrays.stream(steps).max().orElse(1);
        int spacing = Math.max(1, 7 / bucketDays);
        List<Integer> origins = new ArrayList<>();
        int newest = last + 1 - maxStep;
        for (int k = 0; k < ORIGINS; k++) {
            int o = newest - k * spacing;
            if (o > 0) {
                origins.add(0, o);
            }
        }
        List<Score> scores = new ArrayList<>();
        Map<String, double[]> selectionLoss = new LinkedHashMap<>();
        for (ForecastMethod method : METHODS) {
            if (!method.supports(counts) || (method instanceof EtsAaa && season < 2)) {
                continue;
            }
            double[][] loss = new double[steps.length][origins.size()];
            int[][] inside = new int[steps.length][origins.size()];
            boolean[][] scored = new boolean[steps.length][origins.size()];
            int ran = 0;
            for (int oi = 0; oi < origins.size(); oi++) {
                int o = origins.get(oi);
                double[] history = SeriesPrep.fill(Arrays.copyOfRange(known, 0, o), season);
                if (history.length < method.minHistory(season)) {
                    continue;
                }
                ForecastMethod.MethodForecast f;
                try {
                    f = method.forecast(history, season, counts, distinct(steps));
                } catch (RuntimeException e) {
                    continue;
                }
                ran++;
                for (int hi = 0; hi < steps.length; hi++) {
                    int target = o - 1 + steps[hi];
                    if (target >= known.length || !Double.isFinite(known[target])) {
                        continue;
                    }
                    double[] q = f.quantiles().get(steps[hi]);
                    loss[hi][oi] = QuantileSet.meanPinball(known[target], q);
                    inside[hi][oi] = known[target] >= q[QuantileSet.P05] && known[target] <= q[QuantileSet.P95]
                            ? 1 : 0;
                    scored[hi][oi] = true;
                }
            }
            if (ran < Math.min(MIN_ORIGINS, Math.max(1, origins.size()))) {
                continue;
            }
            for (int hi = 0; hi < steps.length; hi++) {
                double sum = 0;
                int n = 0;
                int in = 0;
                for (int oi = 0; oi < origins.size(); oi++) {
                    if (scored[hi][oi]) {
                        sum += loss[hi][oi];
                        in += inside[hi][oi];
                        n++;
                    }
                }
                if (n == 0) {
                    continue;
                }
                scores.add(new Score(method.id(), HORIZONS[hi], sum / n, (double) in / n, n));
                if (HORIZONS[hi] == SELECTION_HORIZON) {
                    selectionLoss.put(method.id(), new double[] {sum / n});
                }
            }
        }
        String best = null;
        double bestLoss = Double.POSITIVE_INFINITY;
        for (Map.Entry<String, double[]> e : selectionLoss.entrySet()) {
            if (e.getValue()[0] < bestLoss - 1e-12) {
                bestLoss = e.getValue()[0];
                best = e.getKey();
            }
        }
        List<Instant> originTimes = new ArrayList<>();
        if (starts != null) {
            for (int o : origins) {
                originTimes.add(starts[o]);
            }
        }
        return new Selection(best, scores, originTimes, bucketDays == 7 ? "P1W" : "P7D");
    }

    /** The method with {@code id}. */
    public static ForecastMethod method(String id) {
        for (ForecastMethod m : METHODS) {
            if (m.id().equals(id)) {
                return m;
            }
        }
        throw new IllegalArgumentException("unknown method " + id);
    }

    /** The distinct values of {@code steps}, in order. */
    public static int[] distinct(int[] steps) {
        return Arrays.stream(steps).distinct().toArray();
    }
}
