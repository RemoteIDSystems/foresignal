package ai.foresignal.outlook.index;

import ai.foresignal.analytics.math.NbGlm;
import ai.foresignal.outlook.series.QuantileSet;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Fits an index's weights to outcomes (prompt 126, Intelligence-Design §9.3). Pure.
 *
 * <p>Rows are (region, week) pairs: the lagged normalized components (÷ 100) and the target count of the next four
 * weeks. The model is an L2-regularized negative-binomial regression {@code log μ = β₀ + Σ βₖ·xₖ} ({@link NbGlm}),
 * every component coefficient kept non-negative (components are already direction-adjusted, so a declared direction
 * means a non-negative effect). The weeks are cut into {@value #FOLDS} consecutive blocks: {@code λ} is chosen from
 * {@link #LAMBDAS} by forward-chaining validation (train on the blocks before, validate on the next, minimizing the
 * mean NB negative log-likelihood), and the last block is held out for the out-of-sample skill of the implied count
 * forecast against the seasonal-naive outlook (the same four weeks a year earlier, NB with moment dispersion): mean
 * pinball loss over the seven ledger quantiles and mean negative log score, with {@code skill = 1 − model/baseline}.
 * The proposed weights are the final coefficients (fitted on every row) renormalized to sum to 1; each component also
 * reports its Wald standard error and two-sided p-value.
 */
public final class IndexFitter {

    /** The L2 penalties tried. */
    public static final double[] LAMBDAS = {0, 0.01, 0.1, 1, 10, 100};

    /** The time blocks. */
    public static final int FOLDS = 5;

    /** The fewest rows a fit needs. */
    public static final int MIN_ROWS = 30;

    /** The fewest distinct weeks a fit needs. */
    public static final int MIN_WEEKS = 10;

    /** The significance level of the per-component test. */
    public static final double ALPHA = 0.05;

    private IndexFitter() {}

    /**
     * One observation.
     *
     * @param region the region
     * @param week the Monday the components were read
     * @param x the normalized components ÷ 100, in key order
     * @param y the target count of the next four weeks
     * @param baseline the seasonal-naive count (the same four weeks a year earlier), or null
     */
    public record Row(String region, LocalDate week, double[] x, double y, Double baseline) {}

    /**
     * One component's fit.
     *
     * @param key the key
     * @param coefficient the coefficient
     * @param standardError its standard error, or null
     * @param pValue its two-sided p-value, or null
     * @param significant true when {@code p < 0.05}
     * @param currentWeight the weight in force
     * @param proposedWeight the proposed weight
     */
    public record ComponentFit(String key, double coefficient, Double standardError, Double pValue,
            boolean significant, double currentWeight, double proposedWeight) {

        /** The {@code IndexFitResult.components} item. */
        public Map<String, Object> view() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("key", key);
            out.put("coefficient", QuantileSet.round(coefficient));
            out.put("standardError", standardError == null ? null : QuantileSet.round(standardError));
            out.put("pValue", pValue == null ? null : QuantileSet.round(pValue));
            out.put("significant", significant);
            out.put("currentWeight", QuantileSet.round(currentWeight));
            out.put("proposedWeight", QuantileSet.round(proposedWeight));
            return out;
        }
    }

    /**
     * A fit.
     *
     * @param lambda the chosen penalty
     * @param regions the regions in the data
     * @param origins the weeks in the data
     * @param proposed the proposed weights, or null when no component has a positive coefficient
     * @param components the per-component fits
     * @param skill the out-of-sample skill ({@code IndexSkill})
     */
    public record Result(double lambda, int regions, int origins, Map<String, Double> proposed,
            List<ComponentFit> components, Map<String, Object> skill) {}

    /**
     * Fits the weights.
     *
     * @param rows the observations
     * @param keys the component keys, in the order of {@code Row.x}
     * @param current the weights in force
     * @param evaluatedAt the evaluation time
     * @throws IllegalArgumentException with fewer than {@value #MIN_ROWS} rows or {@value #MIN_WEEKS} weeks
     */
    public static Result fit(List<Row> rows, List<String> keys, Map<String, Double> current, Instant evaluatedAt) {
        TreeSet<LocalDate> weeks = new TreeSet<>();
        TreeSet<String> regions = new TreeSet<>();
        for (Row r : rows) {
            weeks.add(r.week());
            regions.add(r.region());
        }
        if (rows.size() < MIN_ROWS || weeks.size() < MIN_WEEKS) {
            throw new IllegalArgumentException("an index fit needs at least " + MIN_ROWS + " rows over " + MIN_WEEKS
                    + " weeks; there are " + rows.size() + " rows over " + weeks.size() + " weeks");
        }
        List<LocalDate> ordered = new ArrayList<>(weeks);
        LocalDate[] cuts = new LocalDate[FOLDS];
        for (int f = 0; f < FOLDS; f++) {
            cuts[f] = ordered.get(Math.min(ordered.size() - 1, (int) Math.floor((double) f * ordered.size() / FOLDS)));
        }
        boolean[] nonNegative = new boolean[keys.size() + 1];
        for (int j = 1; j <= keys.size(); j++) {
            nonNegative[j] = true;
        }
        double bestLambda = LAMBDAS[0];
        double bestLoss = Double.POSITIVE_INFINITY;
        for (double lambda : LAMBDAS) {
            double loss = 0;
            int n = 0;
            for (int f = 1; f < FOLDS - 1; f++) {
                List<Row> train = block(rows, null, cuts[f]);
                List<Row> valid = block(rows, cuts[f], cuts[f + 1]);
                if (train.size() < keys.size() + 2 || valid.isEmpty()) {
                    continue;
                }
                NbGlm.Fit fit = fitRows(train, lambda, nonNegative);
                for (Row r : valid) {
                    loss -= NbGlm.logPmf(r.y(), fit.mean(design(r)), fit.theta());
                    n++;
                }
            }
            double mean = n == 0 ? Double.POSITIVE_INFINITY : loss / n;
            if (mean < bestLoss - 1e-12) {
                bestLoss = mean;
                bestLambda = lambda;
            }
        }
        List<Row> trainAll = block(rows, null, cuts[FOLDS - 1]);
        List<Row> holdout = block(rows, cuts[FOLDS - 1], null);
        Map<String, Object> skill = skill(trainAll.size() >= keys.size() + 2 ? fitRows(trainAll, bestLambda,
                nonNegative) : null, trainAll, holdout, evaluatedAt);
        NbGlm.Fit finalFit = fitRows(rows, bestLambda, nonNegative);
        double sum = 0;
        for (int j = 1; j <= keys.size(); j++) {
            sum += Math.max(0, finalFit.beta()[j]);
        }
        Map<String, Double> proposed = null;
        if (sum > 0) {
            proposed = new LinkedHashMap<>();
            for (int j = 1; j <= keys.size(); j++) {
                proposed.put(keys.get(j - 1), QuantileSet.round(Math.max(0, finalFit.beta()[j]) / sum));
            }
        }
        List<ComponentFit> components = new ArrayList<>();
        for (int j = 1; j <= keys.size(); j++) {
            double b = finalFit.beta()[j];
            double se = finalFit.standardError(j);
            Double p = Double.isFinite(se) && se > 0 ? 2 * (1 - QuantileSet.phi(Math.abs(b / se))) : null;
            components.add(new ComponentFit(keys.get(j - 1), b, Double.isFinite(se) && se > 0 ? se : null, p,
                    p != null && p < ALPHA && b > 0, current.getOrDefault(keys.get(j - 1), 0.0),
                    proposed == null ? 0 : proposed.get(keys.get(j - 1))));
        }
        return new Result(bestLambda, regions.size(), weeks.size(), proposed, components, skill);
    }

    /** The rows with {@code from ≤ week < to} (null bounds open). */
    static List<Row> block(List<Row> rows, LocalDate from, LocalDate to) {
        List<Row> out = new ArrayList<>();
        for (Row r : rows) {
            if ((from == null || !r.week().isBefore(from)) && (to == null || r.week().isBefore(to))) {
                out.add(r);
            }
        }
        return out;
    }

    static double[] design(Row r) {
        double[] x = new double[r.x().length + 1];
        x[0] = 1;
        System.arraycopy(r.x(), 0, x, 1, r.x().length);
        return x;
    }

    static NbGlm.Fit fitRows(List<Row> rows, double lambda, boolean[] nonNegative) {
        double[][] x = new double[rows.size()][];
        double[] y = new double[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            x[i] = design(rows.get(i));
            y[i] = rows.get(i).y();
        }
        return NbGlm.fit(x, y, lambda, nonNegative, true);
    }

    /** The out-of-sample skill on the holdout rows that have a seasonal-naive baseline. */
    static Map<String, Object> skill(NbGlm.Fit fit, List<Row> train, List<Row> holdout, Instant evaluatedAt) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("evaluatedAt", evaluatedAt.toString());
        out.put("baseline", "seasonal-naive outlook");
        List<Row> scored = holdout.stream().filter(r -> r.baseline() != null).toList();
        out.put("n", fit == null ? 0 : scored.size());
        if (fit == null || scored.isEmpty()) {
            out.put("pinballLoss", null);
            out.put("logScore", null);
            out.put("baselinePinballLoss", null);
            out.put("baselineLogScore", null);
            out.put("skillPinball", null);
            out.put("skillLog", null);
            return out;
        }
        double baseTheta = baselineTheta(train);
        double pin = 0;
        double log = 0;
        double basePin = 0;
        double baseLog = 0;
        for (Row r : scored) {
            double mu = fit.mean(design(r));
            pin += QuantileSet.meanPinball(r.y(), quantiles(mu, fit.theta()));
            log -= NbGlm.logPmf(r.y(), mu, fit.theta());
            double bmu = Math.max(0.5, r.baseline());
            basePin += QuantileSet.meanPinball(r.y(), quantiles(bmu, baseTheta));
            baseLog -= NbGlm.logPmf(r.y(), bmu, baseTheta);
        }
        int n = scored.size();
        out.put("pinballLoss", QuantileSet.round(pin / n));
        out.put("logScore", QuantileSet.round(log / n));
        out.put("baselinePinballLoss", QuantileSet.round(basePin / n));
        out.put("baselineLogScore", QuantileSet.round(baseLog / n));
        out.put("skillPinball", basePin > 0 ? QuantileSet.round(1 - pin / basePin) : null);
        out.put("skillLog", baseLog > 0 ? QuantileSet.round(1 - log / baseLog) : null);
        return out;
    }

    /** The nine NB quantiles of a count with mean {@code mu} and size {@code theta}. */
    static double[] quantiles(double mu, double theta) {
        double[] q = new double[QuantileSet.LEVELS.length];
        for (int i = 0; i < q.length; i++) {
            q[i] = NbGlm.quantile(QuantileSet.LEVELS[i], mu, theta);
        }
        return q;
    }

    /** The moment dispersion of the seasonal-naive baseline on the training rows. */
    static double baselineTheta(List<Row> train) {
        List<Double> y = new ArrayList<>();
        List<Double> mu = new ArrayList<>();
        for (Row r : train) {
            if (r.baseline() != null) {
                y.add(r.y());
                mu.add(Math.max(0.5, r.baseline()));
            }
        }
        if (y.size() < 3) {
            return NbGlm.THETA_MAX;
        }
        return NbGlm.momentTheta(y.stream().mapToDouble(Double::doubleValue).toArray(),
                mu.stream().mapToDouble(Double::doubleValue).toArray(), 0);
    }
}
