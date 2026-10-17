package ai.foresignal.cycle;

import ai.foresignal.forecast.ForecastInputs.ForecastValue;
import ai.foresignal.forecast.Ledger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Aggregates the independent samples of the forecaster role into one forecast (prompt 128 §3): binary samples by the
 * <strong>median of their log-odds</strong>, distributions by the median log-odds of each outcome renormalized to one,
 * and quantiles by the median of each quantile (kept monotone). Probabilities are clipped to [0.001, 0.999] before the
 * log-odds so a sample of 0 or 1 cannot dominate. Pure.
 */
public final class SampleAggregator {

    /** The clip applied before log-odds. */
    static final double CLIP = 0.001;

    private SampleAggregator() {}

    /**
     * The aggregate of {@code samples}, all of one question type; null when there are none.
     *
     * @param type the question type ({@link Ledger#BINARY}, multiple, ordered or numeric)
     * @param samples the sample values
     */
    public static ForecastValue aggregate(String type, List<ForecastValue> samples) {
        List<ForecastValue> usable = new ArrayList<>();
        for (ForecastValue v : samples) {
            if (v != null) {
                usable.add(v);
            }
        }
        if (usable.isEmpty()) {
            return null;
        }
        if (Ledger.BINARY.equals(type)) {
            List<Double> logits = new ArrayList<>();
            for (ForecastValue v : usable) {
                if (v.probability() != null) {
                    logits.add(logit(v.probability()));
                }
            }
            return logits.isEmpty() ? null : ForecastValue.binary(inverse(median(logits)));
        }
        if (Ledger.NUMERIC.equals(type)) {
            TreeSet<String> keys = new TreeSet<>(Comparator.comparingDouble(Ledger::level));
            for (ForecastValue v : usable) {
                if (v.quantiles() != null) {
                    keys.addAll(v.quantiles().keySet());
                }
            }
            Map<String, Double> out = new LinkedHashMap<>();
            double floor = Double.NEGATIVE_INFINITY;
            for (String key : keys) {
                List<Double> values = new ArrayList<>();
                for (ForecastValue v : usable) {
                    if (v.quantiles() != null && v.quantiles().get(key) != null) {
                        values.add(v.quantiles().get(key));
                    }
                }
                if (values.isEmpty()) {
                    continue;
                }
                double m = Math.max(floor, median(values));
                out.put(key, m);
                floor = m;
            }
            return out.isEmpty() ? null : ForecastValue.quantiles(out);
        }
        TreeSet<String> labels = new TreeSet<>();
        List<String> order = new ArrayList<>();
        for (ForecastValue v : usable) {
            if (v.distribution() != null) {
                for (String label : v.distribution().keySet()) {
                    if (labels.add(label)) {
                        order.add(label);
                    }
                }
            }
        }
        Map<String, Double> raw = new LinkedHashMap<>();
        double sum = 0;
        for (String label : order) {
            List<Double> logits = new ArrayList<>();
            for (ForecastValue v : usable) {
                if (v.distribution() != null) {
                    Double p = v.distribution().get(label);
                    logits.add(logit(p == null ? 0 : p));
                }
            }
            double p = inverse(median(logits));
            raw.put(label, p);
            sum += p;
        }
        if (raw.isEmpty() || sum <= 0) {
            return null;
        }
        Map<String, Double> out = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : raw.entrySet()) {
            out.put(e.getKey(), e.getValue() / sum);
        }
        return ForecastValue.distribution(out);
    }

    /** The log-odds of {@code p}, clipped. */
    static double logit(double p) {
        double c = Math.max(CLIP, Math.min(1 - CLIP, p));
        return Math.log(c / (1 - c));
    }

    /** The probability of log-odds {@code x}. */
    static double inverse(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    /** The median (the mean of the two middle values for an even count). */
    static double median(List<Double> values) {
        List<Double> sorted = new ArrayList<>(values);
        sorted.sort(Double::compare);
        int n = sorted.size();
        return n % 2 == 1 ? sorted.get(n / 2) : (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2;
    }
}
