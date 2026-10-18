package ai.foresignal.social;

import ai.foresignal.analytics.math.RobustStats;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@code SocialBaseline} 1.0.0 (prompt 135): the seasonal baseline of an hourly social volume series, a pure function
 * of stored counts.
 *
 * <p>For hour {@code h}, the same hour of the week in each of the trailing {@value #WEEKS} weeks
 * ({@code h − k·7d}, {@code k = 1…8}) is a sample. With platform-language denominators (every post seen on complete
 * streams), each sample is a <strong>rate</strong> {@code value_k / denominator_k}, the baseline is
 * {@code median(rate) × denominator_h} and the MAD is scaled the same way, so platform growth, an outage or a
 * platform-wide drop changes the expectation instead of reading as an attention change. Without denominators (a
 * search platform) the samples are the raw values.
 *
 * <ul>
 *   <li>A sample counts only when its hour was collected (its denominator is positive, or the caller marks it
 *       collected); at least {@value #MIN_WEEKS} samples are needed, else there is no baseline.
 *   <li>{@code velocityRatio = observed / max(baseline, 1)} (the baseline is floored at one post, so a first mention
 *       is not an infinite ratio).
 *   <li>{@code zRobust = (observed − baseline) / max(1.4826 × MAD, √max(baseline, 1))} (a Poisson floor keeps a
 *       constant series from dividing by zero).
 *   <li>Series from sampled paths are never baselined ({@link #eligible}; SOC-3).
 * </ul>
 */
public final class SocialBaseline {

    /** The detector name. */
    public static final String NAME = "SocialBaseline";

    /** The detector version. */
    public static final String VERSION = "1.0.0";

    /** The trailing weeks. */
    public static final int WEEKS = 8;

    /** The samples a baseline needs. */
    public static final int MIN_WEEKS = 4;

    private static final Duration WEEK = Duration.ofDays(7);

    /**
     * One baselined hour.
     *
     * @param hour the hour start
     * @param observed the observed value
     * @param baseline the baseline, or null when there are too few samples
     * @param mad the (denominator-scaled, unscaled-by-1.4826) MAD, or null
     * @param velocityRatio {@code observed / max(baseline, 1)}, or null
     * @param zRobust the robust z, or null
     * @param samples the samples used
     * @param scaled true when denominators scaled the samples
     */
    public record Point(Instant hour, double observed, Double baseline, Double mad, Double velocityRatio,
            Double zRobust, int samples, boolean scaled) {}

    private SocialBaseline() {}

    /** The detector reference ({@code DetectorRef}). */
    public static Map<String, Object> detector() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("name", NAME);
        out.put("version", VERSION);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("weeks", WEEKS);
        params.put("minWeeks", MIN_WEEKS);
        params.put("statistic", "hour-of-week median and MAD, denominator-scaled");
        out.put("params", params);
        return out;
    }

    /** True when a collection type may be baselined (complete and paged only). */
    public static boolean eligible(String collectionType) {
        return SocialVocabulary.feedsBaselines(collectionType);
    }

    /**
     * Baselines one hour.
     *
     * @param hour the hour start
     * @param values hour start to observed value (missing hours are zero when collected)
     * @param denominators hour start to the platform-language denominator; empty for a platform without them
     * @param collected hours known to be collected when there are no denominators, or null to treat every hour as
     *     collected
     * @return the point
     */
    public static Point at(Instant hour, Map<Instant, ? extends Number> values, Map<Instant, Long> denominators,
            Set<Instant> collected) {
        double observed = value(values, hour);
        boolean scaled = hasDenominators(denominators);
        Long dh = scaled ? denominators.get(hour) : null;
        if (scaled && (dh == null || dh <= 0)) {
            return new Point(hour, observed, null, null, null, null, 0, true);
        }
        List<Double> samples = new ArrayList<>();
        for (int k = 1; k <= WEEKS; k++) {
            Instant hk = hour.minus(WEEK.multipliedBy(k));
            if (scaled) {
                Long dk = denominators.get(hk);
                if (dk == null || dk <= 0) {
                    continue;
                }
                samples.add(value(values, hk) / dk);
            } else {
                if (collected != null && !collected.contains(hk)) {
                    continue;
                }
                samples.add(value(values, hk));
            }
        }
        if (samples.size() < MIN_WEEKS) {
            return new Point(hour, observed, null, null, null, null, samples.size(), scaled);
        }
        double[] s = samples.stream().mapToDouble(Double::doubleValue).toArray();
        double factor = scaled ? dh : 1.0;
        double baseline = RobustStats.median(s) * factor;
        double mad = RobustStats.mad(s) * factor;
        double sigma = Math.max(RobustStats.MAD_SCALE * mad, Math.sqrt(Math.max(baseline, 1.0)));
        double ratio = observed / Math.max(baseline, 1.0);
        double z = (observed - baseline) / sigma;
        return new Point(hour, observed, baseline, mad, ratio, z, samples.size(), scaled);
    }

    /** True when a denominator map holds at least one positive hour (a stream that only counts its filter has none). */
    public static boolean hasDenominators(Map<Instant, Long> denominators) {
        if (denominators == null) {
            return false;
        }
        for (Long d : denominators.values()) {
            if (d != null && d > 0) {
                return true;
            }
        }
        return false;
    }

    private static double value(Map<Instant, ? extends Number> values, Instant hour) {
        Number n = values == null ? null : values.get(hour);
        return n == null ? 0 : n.doubleValue();
    }
}
