package ai.foresignal.media.outlet;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The consensus of an outlet's ratings on one dimension (prompt 133): the median of the providers' normalized values,
 * the number of providers, the disagreement ({@code max − min}) and, for lean, the bucket: {@code |median| < 0.75} is
 * {@code center}, below is {@code left}, above is {@code right} (so −0.75 is left and +0.75 is right). Providers whose
 * rating has no normalized value (a label only) count in {@code nProviders} and in the breakdown but not in the median.
 * Disagreement is shown, never hidden: the breakdown always lists every provider. Pure.
 */
public final class Consensus {

    /** Half the width of the center bucket on the −2…+2 lean scale. */
    public static final double CENTER_HALF_WIDTH = 0.75;

    private Consensus() {}

    /**
     * One provider's rating as the consensus sees it.
     *
     * @param provider the provider
     * @param valueNorm the normalized value, or null
     * @param label the provider's label, or null
     * @param ratedAt the provider's rating date, or null
     * @param license the rating's license
     * @param egress {@code OPEN} or {@code LOCAL_ONLY}
     */
    public record ProviderValue(String provider, Double valueNorm, String label, Instant ratedAt, String license,
            String egress) {

        /** The breakdown entry ({@code OutletConsensus.providers[]}). */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("provider", provider);
            out.put("valueNorm", valueNorm);
            out.put("label", label);
            out.put("ratedAt", ratedAt == null ? null : ratedAt.toString());
            out.put("licence", license);
            return out;
        }
    }

    /**
     * A consensus.
     *
     * @param dimension the dimension
     * @param median the median of the normalized values, or null when none has one
     * @param nProviders the providers with a rating on this dimension
     * @param disagreement {@code max − min} of the normalized values, or null
     * @param bucket the lean bucket, or null (other dimensions, or no median)
     * @param providers every provider's value, in provider order
     */
    public record Result(String dimension, Double median, int nProviders, Double disagreement, String bucket,
            List<ProviderValue> providers) {

        /** Copies the list. */
        public Result {
            providers = providers == null ? List.of() : List.copyOf(providers);
        }
    }

    /** The consensus of {@code values} on {@code dimension}. */
    public static Result compute(String dimension, List<ProviderValue> values) {
        List<ProviderValue> sorted = new ArrayList<>(values == null ? List.of() : values);
        sorted.sort(Comparator.comparingInt((ProviderValue v) -> order(v.provider())).thenComparing(
                ProviderValue::provider));
        List<Double> numbers = new ArrayList<>();
        for (ProviderValue v : sorted) {
            if (v.valueNorm() != null && Double.isFinite(v.valueNorm())) {
                numbers.add(v.valueNorm());
            }
        }
        Double median = median(numbers);
        Double disagreement = numbers.isEmpty() ? null
                : round(numbers.stream().mapToDouble(Double::doubleValue).max().orElse(0)
                        - numbers.stream().mapToDouble(Double::doubleValue).min().orElse(0));
        String bucket = MediaVocabulary.LEAN.equals(dimension) ? bucket(median) : null;
        return new Result(dimension, median, sorted.size(), disagreement, bucket, sorted);
    }

    /** The lean bucket of a median: {@code center} when {@code |median| < 0.75}; null for no median. */
    public static String bucket(Double median) {
        if (median == null || !Double.isFinite(median)) {
            return null;
        }
        if (Math.abs(median) < CENTER_HALF_WIDTH) {
            return MediaVocabulary.CENTER;
        }
        return median < 0 ? MediaVocabulary.LEFT : MediaVocabulary.RIGHT;
    }

    /** The median, or null for no values. */
    static Double median(List<Double> values) {
        if (values.isEmpty()) {
            return null;
        }
        List<Double> s = new ArrayList<>(values);
        s.sort(Double::compare);
        int n = s.size();
        double m = n % 2 == 1 ? s.get(n / 2) : (s.get(n / 2 - 1) + s.get(n / 2)) / 2.0;
        return round(m);
    }

    private static int order(String provider) {
        int i = MediaVocabulary.PROVIDERS.indexOf(provider);
        return i < 0 ? MediaVocabulary.PROVIDERS.size() : i;
    }

    private static double round(double v) {
        return Math.round(v * 10_000) / 10_000.0;
    }
}
