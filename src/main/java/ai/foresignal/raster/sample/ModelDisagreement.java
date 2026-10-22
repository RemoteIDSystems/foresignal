package ai.foresignal.raster.sample;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * The model-disagreement flag (prompt 105; Weather-Imagery-Design §7.2): whether two or more forecast
 * models disagree enough about the next 24 hours that a caller should see a spread. Pure and
 * deterministic.
 *
 * <p>The fields compared are {@code gust} (max, m/s), {@code precip24h} (mm) and {@code t2mMax}
 * (max, K). A field is flagged when the spread (max − min across models) exceeds its threshold:
 * gust 8 m/s; precipitation {@code max(15 mm, 50% of the mean)}; temperature 4 K. With a previously
 * flagged field the threshold is scaled by {@link #HYSTERESIS} (0.75), so a value hovering at the
 * threshold does not churn.
 */
public final class ModelDisagreement {

    /** The gust spread threshold, m/s. */
    public static final double GUST_SPREAD_MS = 8.0;

    /** The precipitation absolute threshold, mm. */
    public static final double PRECIP_MIN_MM = 15.0;

    /** The precipitation relative threshold (share of the mean). */
    public static final double PRECIP_RELATIVE = 0.5;

    /** The temperature spread threshold, K. */
    public static final double T2M_SPREAD_K = 4.0;

    /** The hysteresis factor for a field already flagged. */
    public static final double HYSTERESIS = 0.75;

    private ModelDisagreement() {}

    /**
     * Computes the disagreement block.
     *
     * @param valuesByField field ({@code gust}, {@code precip24h}, {@code t2mMax}) to (model to value);
     *     a field with fewer than two models is ignored
     * @param previousSpread field to its previous spread, for hysteresis (empty when none)
     * @return the block {@code {fields, spread, models}}, or null when nothing disagrees
     */
    public static Map<String, Object> compute(Map<String, Map<String, Double>> valuesByField,
            Map<String, Double> previousSpread) {
        Map<String, Double> previous = previousSpread == null ? Map.of() : previousSpread;
        List<String> fields = new ArrayList<>();
        Map<String, Object> spread = new LinkedHashMap<>();
        TreeSet<String> models = new TreeSet<>();
        for (Map.Entry<String, Map<String, Double>> e : valuesByField.entrySet()) {
            String field = e.getKey();
            Map<String, Double> byModel = e.getValue();
            if (byModel == null || byModel.size() < 2) {
                continue;
            }
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            double sum = 0;
            List<String> present = new ArrayList<>();
            for (Map.Entry<String, Double> m : byModel.entrySet()) {
                Double v = m.getValue();
                if (v == null || Double.isNaN(v)) {
                    continue;
                }
                min = Math.min(min, v);
                max = Math.max(max, v);
                sum += v;
                present.add(m.getKey());
            }
            if (present.size() < 2) {
                continue;
            }
            double mean = sum / present.size();
            double s = max - min;
            double threshold = threshold(field, mean);
            boolean flagged = previous.containsKey(field) ? s >= threshold * HYSTERESIS : s >= threshold;
            if (flagged) {
                fields.add(field);
                spread.put(field, quantize(s));
                models.addAll(present);
            }
        }
        if (fields.isEmpty()) {
            return null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("fields", fields);
        out.put("spread", spread);
        out.put("models", new ArrayList<>(models));
        return out;
    }

    /** The threshold of a field given the mean. */
    public static double threshold(String field, double mean) {
        return switch (field) {
            case "gust" -> GUST_SPREAD_MS;
            case "precip24h" -> Math.max(PRECIP_MIN_MM, PRECIP_RELATIVE * Math.abs(mean));
            case "t2mMax" -> T2M_SPREAD_K;
            default -> Double.MAX_VALUE;
        };
    }

    /** The models of a flagged field, for tests. */
    static List<String> models(Map<String, Object> block) {
        Object models = block.get("models");
        return models instanceof List<?> list ? list.stream().map(Object::toString).toList() : List.of();
    }

    /** A spread rounded to 0.5. */
    static double quantize(double v) {
        return Math.round(v * 2) / 2.0;
    }
}
