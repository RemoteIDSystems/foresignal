package ai.foresignal.trigger.window;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A learned, coverage-aware baseline for one slot (prompt 100). Built from the last
 * {@code minWeeks}–12 weeks of coverage-ok samples, excluding flagged samples, winsorized at p99.
 */
public record Baseline(double weeks, String slotMode, double mean, double variance, String model,
        double size, double probability, double slotMedianCoverage) {

    /** Builds a baseline from a slot's samples. */
    public static Baseline of(List<Integer> counts, List<Double> coverages, String slotMode, Duration bucket) {
        if (counts.isEmpty()) {
            return new Baseline(0, slotMode, 0, 0, "poisson", 0, 0, 0);
        }
        List<Integer> used = winsorize(counts);
        double mean = used.stream().mapToInt(Integer::intValue).average().orElse(0);
        double variance = 0;
        for (int value : used) {
            variance += (value - mean) * (value - mean);
        }
        variance = used.size() > 1 ? variance / (used.size() - 1) : 0;
        // A slot's samples are one per week (hour-of-week) or one per day (hour-of-day), so the span
        // of history they cover is the sample count for hour-of-week and count/7 weeks for hour-of-day.
        // (The old n x bucket / 7d divided an hour-of-week slot's handful of weekly samples by 168 and
        // could never reach minWeeks.)
        double weeks = "hour-of-day".equals(slotMode) ? used.size() / 7.0 : used.size();
        double medianCoverage = median(coverages);
        if (variance > 1.5 * mean && mean > 0) {
            double[] moments = NegBinTail.moments(mean, variance);
            if (moments != null) {
                return new Baseline(weeks, slotMode, mean, variance, "nb", moments[0], moments[1], medianCoverage);
            }
        }
        return new Baseline(weeks, slotMode, mean, variance, "poisson", 0, 0, medianCoverage);
    }

    /** The jsonb summary. */
    public Map<String, Object> toMap() {
        return Map.of("weeks", weeks, "slotMode", slotMode, "mean", mean, "variance", variance, "model", model);
    }

    /** True when at least {@code minWeeks} weeks of slot history exist. */
    public boolean ready(int minWeeks) {
        return weeks >= minWeeks;
    }

    /** Caps at p99 to stop one outlier dominating the mean and variance. */
    static List<Integer> winsorize(List<Integer> values) {
        List<Integer> sorted = new ArrayList<>(values);
        sorted.sort(Integer::compareTo);
        int index = (int) Math.ceil(0.99 * sorted.size()) - 1;
        int cap = sorted.get(Math.max(0, Math.min(sorted.size() - 1, index)));
        List<Integer> out = new ArrayList<>(values.size());
        for (int value : values) {
            out.add(Math.min(value, cap));
        }
        return out;
    }

    static double median(List<Double> values) {
        if (values.isEmpty()) {
            return 0;
        }
        List<Double> sorted = new ArrayList<>(values);
        sorted.sort(Double::compareTo);
        int middle = sorted.size() / 2;
        return sorted.size() % 2 == 0 ? (sorted.get(middle - 1) + sorted.get(middle)) / 2 : sorted.get(middle);
    }
}
