package ai.foresignal.baserate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reporting density (prompt 121): how much a country is reported on, relative to countries of similar population.
 * A country's density is its mean weekly count of text-reported events divided by the median of the countries in its
 * population quintile (countries without a population form their own group); the median is floored at
 * {@value #MEDIAN_FLOOR} events a week so a quiet group does not divide by zero. Deciles rank every country by density
 * (1 = least reported); decile 1 carries {@code LOW_REPORTING_DENSITY}.
 */
public final class ReportingDensity {

    /** The smallest median used as a divisor. */
    public static final double MEDIAN_FLOOR = 0.1;

    /** The decile that is flagged. */
    public static final int LOW_DECILE = 1;

    private ReportingDensity() {}

    /**
     * One country's inputs.
     *
     * @param weeklyEvents mean weekly text-reported events
     * @param population the population, or null
     */
    public record Input(double weeklyEvents, Long population) {}

    /**
     * One country's result.
     *
     * @param density the density
     * @param decile the decile, 1–10
     */
    public record Profile(double density, int decile) {}

    /** Computes every country's density and decile. */
    public static Map<String, Profile> compute(Map<String, Input> inputs) {
        Map<String, Profile> out = new TreeMap<>();
        if (inputs.isEmpty()) {
            return out;
        }
        List<String> withPopulation = new ArrayList<>();
        List<String> without = new ArrayList<>();
        inputs.forEach((code, in) -> (in.population() != null && in.population() > 0 ? withPopulation : without)
                .add(code));
        withPopulation.sort(Comparator.comparingLong((String c) -> inputs.get(c).population()).thenComparing(c -> c));
        Map<String, Integer> group = new LinkedHashMap<>();
        for (int i = 0; i < withPopulation.size(); i++) {
            group.put(withPopulation.get(i), Math.min(4, i * 5 / withPopulation.size()));
        }
        for (String c : without) {
            group.put(c, -1);
        }
        Map<Integer, List<Double>> values = new TreeMap<>();
        group.forEach((c, g) -> values.computeIfAbsent(g, k -> new ArrayList<>()).add(inputs.get(c).weeklyEvents()));
        Map<Integer, Double> medians = new TreeMap<>();
        values.forEach((g, v) -> medians.put(g, Math.max(MEDIAN_FLOOR, median(v))));
        Map<String, Double> density = new TreeMap<>();
        group.forEach((c, g) -> density.put(c, inputs.get(c).weeklyEvents() / medians.get(g)));
        List<String> ranked = new ArrayList<>(density.keySet());
        ranked.sort(Comparator.comparingDouble((String c) -> density.get(c)).thenComparing(c -> c));
        int n = ranked.size();
        for (int i = 0; i < n; i++) {
            String c = ranked.get(i);
            out.put(c, new Profile(density.get(c), decile(i, n)));
        }
        return out;
    }

    /** The decile (1–10) of rank {@code i} (0-based, ascending) among {@code n}. */
    static int decile(int i, int n) {
        return Math.min(10, 1 + (int) Math.floor(10.0 * i / n));
    }

    /** The median of a list (0 when empty). */
    static double median(List<Double> values) {
        if (values.isEmpty()) {
            return 0;
        }
        List<Double> sorted = new ArrayList<>(values);
        sorted.sort(null);
        int m = sorted.size() / 2;
        return sorted.size() % 2 == 1 ? sorted.get(m) : (sorted.get(m - 1) + sorted.get(m)) / 2;
    }
}
