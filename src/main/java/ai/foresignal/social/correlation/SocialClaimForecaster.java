package ai.foresignal.social.correlation;

import ai.foresignal.baserate.BaseRateCalculator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

/**
 * The {@code social-claim} forecaster 1.0.0 (prompt 137, a 122 ledger forecaster): the probability that a claim gets
 * independent confirmation, from the base rate of corroboration among final comparisons of the same claim type,
 * platform mix, region and evidence-group band, under a Jeffreys Beta(½, ½) prior. When a cell has fewer than
 * {@value #MIN_CELL} decided comparisons it backs off: type, platform mix and region; type and region; type; all
 * claims. {@code insufficient-coverage} comparisons are annulled questions and do not count. Pure.
 */
public final class SocialClaimForecaster {

    /** The decided comparisons a cell needs before it is used. */
    public static final int MIN_CELL = 5;

    private SocialClaimForecaster() {}

    /**
     * One decided past comparison.
     *
     * @param claimType the claim type
     * @param platforms the platform mix
     * @param regionCode the region, or null
     * @param evidenceGroups the claim's evidence groups
     * @param corroborated true when corroborated (false for unobserved or contradicted)
     */
    public record Outcome(String claimType, List<String> platforms, String regionCode, int evidenceGroups,
            boolean corroborated) {}

    /**
     * A forecast.
     *
     * @param probability the posterior mean
     * @param lower the Jeffreys 95% lower bound
     * @param upper the Jeffreys 95% upper bound
     * @param corroborated corroborated outcomes in the cell used
     * @param n decided outcomes in the cell used
     * @param level the cell used: {@code type+platforms+region+band}, {@code type+platforms+region},
     *     {@code type+region}, {@code type} or {@code all}
     */
    public record Estimate(double probability, double lower, double upper, long corroborated, long n, String level) {

        /** The inputs member of the recorded forecast. */
        public Map<String, Object> toMap() {
            return Map.of("probability", probability, "lower", lower, "upper", upper, "corroborated", corroborated,
                    "n", n, "level", level, "prior", "Jeffreys Beta(0.5, 0.5)");
        }
    }

    /** The evidence-group band: 1, 2–3, 4–7, 8+. */
    public static int band(int groups) {
        if (groups <= 1) {
            return 1;
        }
        if (groups <= 3) {
            return 2;
        }
        return groups <= 7 ? 4 : 8;
    }

    /** The platform mix key (sorted, distinct). */
    public static String mix(List<String> platforms) {
        return String.join("+", new TreeSet<>(platforms == null ? List.of() : platforms));
    }

    /** The forecast for a claim of {@code type} seen on {@code platforms} in {@code region} with {@code groups}. */
    public static Estimate estimate(List<Outcome> history, String type, List<String> platforms, String region,
            int groups) {
        String mix = mix(platforms);
        int band = band(groups);
        String[] levels = {"type+platforms+region+band", "type+platforms+region", "type+region", "type", "all"};
        for (int level = 0; level < levels.length; level++) {
            long n = 0;
            long x = 0;
            for (Outcome o : history) {
                if (!matches(o, level, type, mix, region, band)) {
                    continue;
                }
                n++;
                if (o.corroborated()) {
                    x++;
                }
            }
            if (n >= MIN_CELL || level == levels.length - 1) {
                double p = (x + 0.5) / (n + 1.0);
                double[] interval = BaseRateCalculator.jeffreys(x, n);
                return new Estimate(p, interval[0], interval[1], x, n, levels[level]);
            }
        }
        throw new IllegalStateException("unreachable");
    }

    private static boolean matches(Outcome o, int level, String type, String mix, String region, int band) {
        return switch (level) {
            case 0 -> Objects.equals(o.claimType(), type) && mix(o.platforms()).equals(mix)
                    && Objects.equals(o.regionCode(), region) && band(o.evidenceGroups()) == band;
            case 1 -> Objects.equals(o.claimType(), type) && mix(o.platforms()).equals(mix)
                    && Objects.equals(o.regionCode(), region);
            case 2 -> Objects.equals(o.claimType(), type) && Objects.equals(o.regionCode(), region);
            case 3 -> Objects.equals(o.claimType(), type);
            default -> true;
        };
    }
}
