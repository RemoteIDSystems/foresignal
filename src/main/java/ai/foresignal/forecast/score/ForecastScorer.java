package ai.foresignal.forecast.score;

import ai.foresignal.forecast.Ledger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/**
 * Proper scores of single forecasts ({@code ForecastScorer} {@value #VERSION}, prompt 122, Intelligence-Design §8.3).
 * Pure functions; scores are stored with this version and recomputed only when it changes.
 *
 * <ul>
 *   <li><b>Brier</b>: binary {@code (p − o)²} (0–1); multiple {@code Σₖ (pₖ − oₖ)²} over every label (0–2).</li>
 *   <li><b>RPS</b> (ordered): {@code (1 / (K − 1)) Σₖ₌₁..K₋₁ (Fₖ − Oₖ)²} over the cumulative forecast and outcome,
 *       so it lies in 0–1.</li>
 *   <li><b>Log score</b>: {@code −ln p(outcome)} with p clipped to [0.01, 0.99], so one confident miss costs at most
 *       {@code ln 100}.</li>
 *   <li><b>CRPS</b> (numeric) from quantiles: twice the mean pinball loss over the stored levels (the quantile
 *       decomposition {@code CRPS = 2 ∫ ρ_τ dτ}, approximated on the stored levels); the pinball losses
 *       {@code ρ_τ(y − q) = max(τ (y − q), (τ − 1)(y − q))} are kept per level, and interval hits for p05–p95 and
 *       p25–p75.</li>
 *   <li><b>Time-averaged</b> ({@link #timeAveraged}): every UTC day the question is open the forecaster's standing
 *       forecast (its latest made by the end of that day, carried forward until superseded) is scored and the scores
 *       are averaged. Days before its first forecast use the {@code base-rate} forecaster's standing forecast (the Good
 *       Judgment convention for days without a forecast); days with neither are not counted.</li>
 * </ul>
 */
public final class ForecastScorer {

    /** The scorer's name. */
    public static final String NAME = "forecast-scorer";

    /** The scorer's version. */
    public static final String VERSION = "1.0.0";

    /** The lower clip of the log score. */
    public static final double LOG_CLIP_LOW = 0.01;

    /** The upper clip of the log score. */
    public static final double LOG_CLIP_HIGH = 0.99;

    private ForecastScorer() {}

    /** The binary Brier score. */
    public static double brier(double p, boolean yes) {
        double o = yes ? 1.0 : 0.0;
        return (p - o) * (p - o);
    }

    /**
     * The multi-category Brier score over {@code labels} (missing labels count as probability 0).
     *
     * @param distribution label → probability
     * @param outcome the label that happened
     * @param labels every label
     */
    public static double multiBrier(Map<String, Double> distribution, String outcome, List<String> labels) {
        double sum = 0;
        for (String label : labels) {
            double p = value(distribution, label);
            double o = label.equals(outcome) ? 1.0 : 0.0;
            sum += (p - o) * (p - o);
        }
        return sum;
    }

    /**
     * The ranked probability score of an ordered forecast, normalized by {@code K − 1}.
     *
     * @param distribution label → probability
     * @param outcome the label that happened
     * @param labels the labels in order
     */
    public static double rps(Map<String, Double> distribution, String outcome, List<String> labels) {
        int k = labels.size();
        if (k < 2) {
            return 0.0;
        }
        int outcomeIndex = labels.indexOf(outcome);
        double cumulative = 0;
        double sum = 0;
        for (int i = 0; i < k - 1; i++) {
            cumulative += value(distribution, labels.get(i));
            double observed = i >= outcomeIndex ? 1.0 : 0.0;
            sum += (cumulative - observed) * (cumulative - observed);
        }
        return sum / (k - 1);
    }

    /** The log score {@code −ln p} of the probability given to what happened, clipped to [0.01, 0.99]. */
    public static double logScore(double pOutcome) {
        double p = Math.max(LOG_CLIP_LOW, Math.min(LOG_CLIP_HIGH, pOutcome));
        return -Math.log(p);
    }

    /** The binary log score. */
    public static double logScore(double p, boolean yes) {
        return logScore(yes ? p : 1.0 - p);
    }

    /** The pinball loss of one quantile {@code q} at level {@code tau} for the outcome {@code y}. */
    public static double pinball(double tau, double q, double y) {
        double d = y - q;
        return Math.max(tau * d, (tau - 1.0) * d);
    }

    /** The pinball loss per stored level, in level order. */
    public static Map<String, Double> pinball(Map<String, Double> quantiles, double y) {
        Map<String, Double> out = new LinkedHashMap<>();
        for (String key : Ledger.QUANTILES) {
            Double q = quantiles.get(key);
            if (q != null) {
                out.put(key, pinball(Ledger.level(key), q, y));
            }
        }
        return out;
    }

    /** The CRPS from quantiles: twice the mean pinball loss over the stored levels. */
    public static double crps(Map<String, Double> quantiles, double y) {
        Map<String, Double> losses = pinball(quantiles, y);
        if (losses.isEmpty()) {
            return Double.NaN;
        }
        double sum = 0;
        for (double loss : losses.values()) {
            sum += loss;
        }
        return 2.0 * sum / losses.size();
    }

    /**
     * Whether the outcome {@code y} fell at or below each stored quantile ({@code y ≤ q_τ}), per level, in level
     * order (numeric calibration, prompt 152). The empirical mean of each level's indicator should equal its nominal
     * level τ when the forecaster's quantiles are reliable; the indicators also place {@code y} in a PIT bin.
     */
    public static Map<String, Boolean> quantileCoverage(Map<String, Double> quantiles, double y) {
        Map<String, Boolean> out = new LinkedHashMap<>();
        for (String key : Ledger.QUANTILES) {
            Double q = quantiles.get(key);
            if (q != null) {
                out.put(key, y <= q);
            }
        }
        return out;
    }

    /** Whether {@code y} fell inside p05–p95 and p25–p75 (inclusive). */
    public static Map<String, Boolean> intervalHits(Map<String, Double> quantiles, double y) {
        Map<String, Boolean> out = new LinkedHashMap<>();
        out.put("p05p95", within(quantiles.get("p05"), quantiles.get("p95"), y));
        out.put("p25p75", within(quantiles.get("p25"), quantiles.get("p75"), y));
        return out;
    }

    private static Boolean within(Double low, Double high, double y) {
        if (low == null || high == null) {
            return null;
        }
        return y >= low && y <= high;
    }

    /**
     * A forecast standing from a time.
     *
     * @param at when it was made
     * @param value the forecast
     * @param <T> the forecast's form
     */
    public record Timed<T>(Instant at, T value) {}

    /**
     * A time-averaged score.
     *
     * @param score the mean daily score, or NaN without scored days
     * @param nDays the days scored
     * @param filledDays the days scored with the fill (base-rate) forecast
     */
    public record TimeAveraged(double score, int nDays, int filledDays) {}

    /**
     * The time-averaged score of a forecaster's standing forecasts over the UTC days of {@code [from, to)}: each day
     * is scored at its end (or at {@code to} on the last day) with the newest own forecast made by then, else the
     * newest fill forecast, else not at all.
     *
     * @param own the forecaster's forecasts
     * @param fill the fill forecasts (the {@code base-rate} forecaster's), may be empty
     * @param from when the question opened
     * @param to when it stopped being open (closed or resolved)
     * @param score the score of one forecast against the outcome
     * @param <T> the forecast's form
     */
    public static <T> TimeAveraged timeAveraged(List<Timed<T>> own, List<Timed<T>> fill, Instant from, Instant to,
            ToDoubleFunction<T> score) {
        if (from == null || to == null || !from.isBefore(to)) {
            return new TimeAveraged(Double.NaN, 0, 0);
        }
        List<Timed<T>> mine = sorted(own);
        List<Timed<T>> base = sorted(fill);
        LocalDate first = LocalDate.ofInstant(from, ZoneOffset.UTC);
        LocalDate last = LocalDate.ofInstant(to.minusNanos(1), ZoneOffset.UTC);
        double sum = 0;
        int days = 0;
        int filled = 0;
        for (LocalDate day = first; !day.isAfter(last); day = day.plusDays(1)) {
            Instant end = day.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant at = end.isAfter(to) ? to : end;
            T standing = standing(mine, at);
            boolean usedFill = false;
            if (standing == null) {
                standing = standing(base, at);
                usedFill = standing != null;
            }
            if (standing == null) {
                continue;
            }
            sum += score.applyAsDouble(standing);
            days++;
            if (usedFill) {
                filled++;
            }
        }
        return new TimeAveraged(days == 0 ? Double.NaN : sum / days, days, filled);
    }

    /** The newest value made at or before {@code at}, or null. */
    public static <T> T standing(List<Timed<T>> sortedForecasts, Instant at) {
        T out = null;
        for (Timed<T> t : sortedForecasts) {
            if (t.at().isAfter(at)) {
                break;
            }
            out = t.value();
        }
        return out;
    }

    private static <T> List<Timed<T>> sorted(List<Timed<T>> in) {
        List<Timed<T>> out = new ArrayList<>(in == null ? List.of() : in);
        out.sort(Comparator.comparing(Timed::at));
        return out;
    }

    private static double value(Map<String, Double> distribution, String label) {
        Double v = distribution == null ? null : distribution.get(label);
        return v == null ? 0.0 : v;
    }
}
