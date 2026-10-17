package ai.foresignal.outlook.index;

import ai.foresignal.outlook.series.QuantileSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * The arithmetic of a regional index (prompt 126, {@code regional-index} 1.0.0, Intelligence-Design §9.3). Pure.
 *
 * <p>Each component's raw window value is normalized into [0, 100] against the pooled history of the region's class
 * ({@link #normalize}): {@code pctrank} (the default) is the mid-rank percentile; {@code zscore} maps
 * {@code Φ((x − mean)/sd)}; {@code log1p} and {@code level} rescale {@code log1p(x)} or {@code x} between the pool's
 * minimum and maximum. A component with {@code direction −1} is reversed ({@code 100 − n}); a component
 * {@code floor} is a lower bound on its normalized value. The index is the weighted sum of the components present,
 * the weights renormalized over them, so the contributions always sum to the value; floor rules then raise the value
 * to their minimum when their condition holds. Indices are composites, never probabilities.
 */
public final class IndexComputer {

    private IndexComputer() {}

    /**
     * A component.
     *
     * @param key the key
     * @param ref the series reference template ({@code {region}} is replaced)
     * @param transform {@code log1p}, {@code zscore}, {@code pctrank} or {@code level}
     * @param windowDays 7, 28 or 90
     * @param direction +1 or −1
     * @param floor the lower bound of the normalized value, or null
     */
    public record Component(String key, String ref, String transform, int windowDays, int direction, Double floor) {

        /** The reference for a region. */
        public String refFor(String region) {
            return ref.replace("{region}", region);
        }
    }

    /**
     * A floor rule.
     *
     * @param id the id
     * @param description the description
     * @param condition the condition ({@code {type: component, key, gte}} or {@code {type: reference, family,
     *     source?, minEvents?, minFatalities?, withinDays}})
     * @param minValue the value the index is raised to
     */
    public record Floor(String id, String description, Map<String, Object> condition, double minValue) {}

    /**
     * One component's part of a value.
     *
     * @param key the key
     * @param raw the raw window value, or null when missing
     * @param normalized the normalized value (0–100), or NaN when missing
     * @param weight the weight used (renormalized over the components present)
     * @param contribution {@code weight × normalized}
     */
    public record Part(String key, Double raw, double normalized, double weight, double contribution) {

        /**
         * The {@code RegionalIndexValue.components} item; its normalized value goes out under the spec's
         * {@code normalised} member, a published wire name that is kept as it is.
         */
        public Map<String, Object> view() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("key", key);
            out.put("raw", raw == null ? null : QuantileSet.round(raw));
            out.put("normalised", Double.isNaN(normalized) ? 0.0 : QuantileSet.round(normalized));
            out.put("weight", QuantileSet.round(weight));
            out.put("contribution", QuantileSet.round(contribution));
            out.put("missing", Double.isNaN(normalized));
            return out;
        }
    }

    /**
     * A computed value.
     *
     * @param value the index value (0–100) after floors, or NaN when no component is present
     * @param beforeFloors the weighted sum (the contributions' total)
     * @param parts the decomposition
     * @param floorsApplied the ids of the floor rules that raised the value
     */
    public record Result(double value, double beforeFloors, List<Part> parts, List<String> floorsApplied) {

        /** The decomposition as maps. */
        public List<Map<String, Object>> partsView() {
            return parts.stream().map(Part::view).toList();
        }
    }

    /**
     * Normalizes a raw value against a pool of the class's history.
     *
     * @param raw the value
     * @param pool the pool (any order; non-finite values ignored)
     * @param transform the transform
     * @return the normalized value in [0, 100], or NaN for an empty pool or a missing value
     */
    public static double normalize(double raw, double[] pool, String transform) {
        if (!Double.isFinite(raw)) {
            return Double.NaN;
        }
        double[] p = Arrays.stream(pool == null ? new double[0] : pool).filter(Double::isFinite).toArray();
        if (p.length == 0) {
            return Double.NaN;
        }
        return switch (transform == null ? "pctrank" : transform) {
            case "zscore" -> {
                double mean = Arrays.stream(p).average().orElse(0);
                double ss = 0;
                for (double v : p) {
                    ss += (v - mean) * (v - mean);
                }
                double sd = p.length > 1 ? Math.sqrt(ss / (p.length - 1)) : 0;
                yield sd > 0 ? 100 * QuantileSet.phi((raw - mean) / sd) : 50;
            }
            case "log1p" -> minMax(Math.log1p(Math.max(0, raw)), Arrays.stream(p).map(v -> Math.log1p(Math.max(0,
                    v))).toArray());
            case "level" -> minMax(raw, p);
            default -> {
                int below = 0;
                int equal = 0;
                for (double v : p) {
                    if (v < raw) {
                        below++;
                    } else if (v == raw) {
                        equal++;
                    }
                }
                yield 100.0 * (below + 0.5 * equal) / p.length;
            }
        };
    }

    private static double minMax(double x, double[] p) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (double v : p) {
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        if (!(max > min)) {
            return 50;
        }
        return Math.max(0, Math.min(100, 100 * (x - min) / (max - min)));
    }

    /**
     * Computes a value.
     *
     * @param components the components
     * @param weights the weights by key
     * @param raw the raw window value of each component (missing or NaN when absent)
     * @param pools the class pool of each component
     * @param floors the floor rules
     * @param referenceCondition evaluates a {@code reference} floor condition (the service reads reference history)
     */
    public static Result compute(List<Component> components, Map<String, Double> weights, Map<String, Double> raw,
            Map<String, double[]> pools, List<Floor> floors, Predicate<Map<String, Object>> referenceCondition) {
        Map<String, Double> normalized = new LinkedHashMap<>();
        double presentWeight = 0;
        for (Component c : components) {
            Double r = raw.get(c.key());
            double n = normalize(r == null ? Double.NaN : r, pools.get(c.key()), c.transform());
            if (!Double.isNaN(n)) {
                if (c.direction() < 0) {
                    n = 100 - n;
                }
                if (c.floor() != null) {
                    n = Math.max(n, c.floor());
                }
                presentWeight += weights.getOrDefault(c.key(), 0.0);
            }
            normalized.put(c.key(), n);
        }
        List<Part> parts = new ArrayList<>();
        double total = 0;
        for (Component c : components) {
            double n = normalized.get(c.key());
            double w = Double.isNaN(n) || !(presentWeight > 0) ? 0 : weights.getOrDefault(c.key(), 0.0) / presentWeight;
            double contribution = Double.isNaN(n) ? 0 : w * n;
            total += contribution;
            Double r = raw.get(c.key());
            parts.add(new Part(c.key(), r == null || !Double.isFinite(r) ? null : r, n, w, contribution));
        }
        if (!(presentWeight > 0)) {
            return new Result(Double.NaN, Double.NaN, parts, List.of());
        }
        double value = total;
        List<String> applied = new ArrayList<>();
        for (Floor f : floors) {
            if (value < f.minValue() && holds(f.condition(), normalized, referenceCondition)) {
                value = f.minValue();
                applied.add(f.id());
            }
        }
        return new Result(Math.max(0, Math.min(100, value)), total, parts, applied);
    }

    /** Whether a floor condition holds. */
    static boolean holds(Map<String, Object> condition, Map<String, Double> normalized,
            Predicate<Map<String, Object>> referenceCondition) {
        if (condition == null) {
            return false;
        }
        Object type = condition.get("type");
        if ("component".equals(type)) {
            Double n = normalized.get(String.valueOf(condition.get("key")));
            Object gte = condition.get("gte");
            return n != null && !Double.isNaN(n) && gte instanceof Number g && n >= g.doubleValue();
        }
        if ("reference".equals(type)) {
            return referenceCondition != null && referenceCondition.test(condition);
        }
        return false;
    }

    /** The change from a previous value, or null without one. */
    public static Double delta(double value, Double previous) {
        return previous == null || !Double.isFinite(previous) ? null : QuantileSet.round(value - previous);
    }
}
