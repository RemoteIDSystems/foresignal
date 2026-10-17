package ai.foresignal.impact.hazard;

import ai.foresignal.baserate.BaseRateCalculator;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Calibrates a river gauge's category forecast by its own history (prompt 127 §1, {@code river-flood}). Pure.
 *
 * <p>A <b>sample</b> is the first stored observation of a UTC day whose basis category reached the flood-relevant
 * category: the forecast category ({@value #FORECAST}) or, without a forecast, the current category under persistence
 * ({@value #PERSISTENCE}). Its outcome is a <b>hit</b> when the observed category reached the same category after the
 * sample and by the end of its horizon (the forecast's valid time, else 24 hours) plus {@link #GRACE}. Samples whose
 * horizon has not passed are left out. The calibrated probability is {@code hits / n} with a 95% Jeffreys interval
 * (121 {@link BaseRateCalculator#jeffreys}); fewer than {@value #MIN_SAMPLES} samples is no calibration, and a gauge
 * without one yields no probability (a deterministic category is not a probability).
 */
public final class GaugeCalibration {

    /** The forecast basis. */
    public static final String FORECAST = "forecast";

    /** The persistence basis (current category). */
    public static final String PERSISTENCE = "persistence";

    /** The fewest samples a calibration needs. */
    public static final int MIN_SAMPLES = 10;

    /** The persistence horizon, and the horizon of a forecast without a valid time. */
    public static final Duration HORIZON = Duration.ofHours(24);

    /** The grace after a horizon within which the observed category still counts. */
    public static final Duration GRACE = Duration.ofHours(6);

    /** NWPS flood categories in rank order; other values (not defined, out of service, …) have no rank. */
    public static final List<String> CATEGORIES = List.of("no_flooding", "action", "minor", "moderate", "major");

    private GaugeCalibration() {}

    /**
     * One stored gauge observation.
     *
     * @param at the observation time
     * @param floodCategory the observed category
     * @param forecastCategory the forecast category, or null
     * @param forecastValidTime the forecast's valid time, or null
     */
    public record Observation(Instant at, String floodCategory, String forecastCategory, Instant forecastValidTime) {}

    /**
     * A calibration.
     *
     * @param basis {@value #FORECAST} or {@value #PERSISTENCE}
     * @param minCategory the flood-relevant category
     * @param n the samples
     * @param hits the samples that verified
     * @param p {@code hits / n}, or null without samples
     * @param lower the interval's lower bound, or null
     * @param upper the interval's upper bound, or null
     */
    public record Result(String basis, String minCategory, int n, int hits, Double p, Double lower, Double upper) {

        /** True when there are enough samples to use. */
        public boolean sufficient() {
            return n >= MIN_SAMPLES && p != null;
        }

        /** The result as stored with a forecast. */
        public Map<String, Object> view() {
            Map<String, Object> out = new java.util.LinkedHashMap<>();
            out.put("basis", basis);
            out.put("minCategory", minCategory);
            out.put("n", n);
            out.put("hits", hits);
            out.put("p", p);
            out.put("lower", lower);
            out.put("upper", upper);
            out.put("minSamples", MIN_SAMPLES);
            return out;
        }
    }

    /** The rank of a category, or -1 when it has none. */
    public static int rank(String category) {
        return category == null ? -1 : CATEGORIES.indexOf(category.toLowerCase(Locale.ROOT));
    }

    /**
     * Calibrates the basis over the observations.
     *
     * @param observations the gauge's stored observations (any order)
     * @param basis {@value #FORECAST} or {@value #PERSISTENCE}
     * @param minCategory the flood-relevant category
     * @param now the decision time; only samples whose horizon (plus grace) has passed count
     */
    public static Result calibrate(List<Observation> observations, String basis, String minCategory, Instant now) {
        int min = rank(minCategory);
        if (min < 0) {
            throw new IllegalArgumentException("not a flood category: " + minCategory);
        }
        List<Observation> sorted = new ArrayList<>(observations);
        sorted.removeIf(o -> o == null || o.at() == null);
        sorted.sort(Comparator.comparing(Observation::at));
        Set<LocalDate> days = new HashSet<>();
        int n = 0;
        int hits = 0;
        for (int i = 0; i < sorted.size(); i++) {
            Observation o = sorted.get(i);
            String category = FORECAST.equals(basis) ? o.forecastCategory() : o.floodCategory();
            if (rank(category) < min) {
                continue;
            }
            Instant end = FORECAST.equals(basis) && o.forecastValidTime() != null && o.forecastValidTime()
                    .isAfter(o.at()) ? o.forecastValidTime() : o.at().plus(HORIZON);
            Instant until = end.plus(GRACE);
            if (until.isAfter(now)) {
                continue;
            }
            if (!days.add(o.at().atZone(ZoneOffset.UTC).toLocalDate())) {
                continue;
            }
            n++;
            for (int j = i + 1; j < sorted.size() && !sorted.get(j).at().isAfter(until); j++) {
                if (rank(sorted.get(j).floodCategory()) >= min) {
                    hits++;
                    break;
                }
            }
        }
        if (n == 0) {
            return new Result(basis, minCategory, 0, 0, null, null, null);
        }
        double[] interval = BaseRateCalculator.jeffreys(hits, n);
        return new Result(basis, minCategory, n, hits, (double) hits / n, interval[0], interval[1]);
    }
}
