package ai.foresignal.news;

import ai.foresignal.media.outlet.MediaVocabulary;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * {@code CoverageAnalyzer} {@value #VERSION} (prompt 133): how a story, topic, situation or a hypothesis's news support
 * is covered. Pure; it describes reporting, never truth.
 *
 * <ul>
 *   <li>article and evidence-group counts by lean bucket ({@code left}, {@code center}, {@code right},
 *       {@code unrated}), evidence groups by country, by outlet type and by factuality band (1–5, {@code unrated});
 *   <li>{@code coverageBalance}: each bucket's share of the evidence groups;
 *   <li>{@code stateAffiliatedShare}: the share of evidence groups from state-affiliated outlets;
 *   <li>{@code blindspot}: {@code left} or {@code right} when at least {@value #BLINDSPOT_SHARE} of the <em>rated</em>
 *       evidence groups come from that side and at least {@value #BLINDSPOT_MIN_RATED} are rated (the value names the
 *       side the coverage comes from: a {@code right} blindspot is a story the left barely covers);
 *   <li>{@code skewed} ({@code SKEWED_COVERAGE}): at least {@value #SKEW_SHARE} of the rated groups from one bucket,
 *       or of all groups from state-affiliated outlets, with at least {@value #SKEW_MIN_GROUPS} groups.
 * </ul>
 * Evidence groups are counted, never articles, wherever a share or threshold is computed: 30 reprints of one wire story
 * are one group.
 */
public final class CoverageAnalyzer {

    /** The analyzer name. */
    public static final String NAME = "CoverageAnalyzer";

    /** The analyzer version. */
    public static final String VERSION = "1.0.0";

    /** The rated share from one side that makes a blindspot. */
    public static final double BLINDSPOT_SHARE = 0.75;

    /** The rated evidence groups a blindspot needs. */
    public static final int BLINDSPOT_MIN_RATED = 8;

    /** The share from one bucket (or from state-affiliated outlets) that makes coverage skewed. */
    public static final double SKEW_SHARE = 0.75;

    /** The evidence groups a skew judgment needs. */
    public static final int SKEW_MIN_GROUPS = 3;

    /** The blindspot caveat. */
    public static final String COVERAGE_BLINDSPOT = "COVERAGE_BLINDSPOT";

    /** The skew caveat. */
    public static final String SKEWED_COVERAGE = "SKEWED_COVERAGE";

    /** The caveat of coverage mostly from unrated outlets. */
    public static final String MOSTLY_UNRATED = "MOSTLY_UNRATED_OUTLETS";

    private CoverageAnalyzer() {}

    /**
     * One evidence group as the analyzer sees it.
     *
     * @param bucket the lean bucket of its outlet ({@code unrated} without a lean consensus)
     * @param country the outlet's country, or null
     * @param outletType the outlet's type
     * @param factualityBand {@code 1}…{@code 5} or {@code unrated}
     * @param stateAffiliated true for a state-affiliated outlet
     * @param articles the group's articles
     */
    public record GroupInfo(String bucket, String country, String outletType, String factualityBand,
            boolean stateAffiliated, int articles) {}

    /**
     * The analysis.
     *
     * @param stats the {@code CoverageStats} map
     * @param blindspot {@code left}, {@code right} or null
     * @param skewed the skew judgment
     * @param nRated the rated evidence groups
     * @param stateAffiliatedShare the state-affiliated share
     * @param groups the evidence groups
     */
    public record Result(Map<String, Object> stats, String blindspot, boolean skewed, int nRated,
            double stateAffiliatedShare, int groups) {}

    /** The analyzer reference stored with every output. */
    public static Map<String, Object> detector() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("blindspotShare", BLINDSPOT_SHARE);
        params.put("blindspotMinRated", BLINDSPOT_MIN_RATED);
        params.put("skewShare", SKEW_SHARE);
        params.put("skewMinGroups", SKEW_MIN_GROUPS);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("name", NAME);
        out.put("version", VERSION);
        out.put("params", params);
        return out;
    }

    /**
     * Analyzes coverage.
     *
     * @param scope {@code story}, {@code topic}, {@code situation} or {@code hypothesis}
     * @param groups the evidence groups
     * @param corroborated whether non-news evidence corroborates it
     * @param corroboratedShare the corroborated share of news members (situations), or null
     * @param computedAt the decision time
     */
    public static Result analyze(String scope, List<GroupInfo> groups, boolean corroborated, Double corroboratedShare,
            Instant computedAt) {
        Map<String, Integer> articles = buckets();
        Map<String, Integer> byBucket = buckets();
        Map<String, Integer> byCountry = new TreeMap<>();
        Map<String, Integer> byType = new TreeMap<>();
        Map<String, Integer> byFactuality = new TreeMap<>();
        int state = 0;
        for (GroupInfo g : groups) {
            String bucket = MediaVocabulary.BUCKETS.contains(g.bucket()) ? g.bucket() : MediaVocabulary.UNRATED;
            articles.merge(bucket, Math.max(1, g.articles()), Integer::sum);
            byBucket.merge(bucket, 1, Integer::sum);
            byCountry.merge(g.country() == null ? "unknown" : g.country(), 1, Integer::sum);
            byType.merge(g.outletType() == null ? MediaVocabulary.OTHER : g.outletType(), 1, Integer::sum);
            byFactuality.merge(g.factualityBand() == null ? MediaVocabulary.UNRATED : g.factualityBand(), 1,
                    Integer::sum);
            if (g.stateAffiliated()) {
                state++;
            }
        }
        int total = groups.size();
        int rated = byBucket.get(MediaVocabulary.LEFT) + byBucket.get(MediaVocabulary.CENTER)
                + byBucket.get(MediaVocabulary.RIGHT);
        Map<String, Object> balance = new LinkedHashMap<>();
        for (String b : MediaVocabulary.BUCKETS) {
            balance.put(b, total == 0 ? 0.0 : round(byBucket.get(b) / (double) total));
        }
        double stateShare = total == 0 ? 0.0 : round(state / (double) total);
        String blindspot = null;
        if (rated >= BLINDSPOT_MIN_RATED) {
            if (byBucket.get(MediaVocabulary.LEFT) / (double) rated >= BLINDSPOT_SHARE) {
                blindspot = MediaVocabulary.LEFT;
            } else if (byBucket.get(MediaVocabulary.RIGHT) / (double) rated >= BLINDSPOT_SHARE) {
                blindspot = MediaVocabulary.RIGHT;
            }
        }
        String skewBucket = null;
        if (rated >= SKEW_MIN_GROUPS) {
            for (String b : List.of(MediaVocabulary.LEFT, MediaVocabulary.CENTER, MediaVocabulary.RIGHT)) {
                if (byBucket.get(b) / (double) rated >= SKEW_SHARE) {
                    skewBucket = b;
                }
            }
        }
        boolean stateSkew = total >= SKEW_MIN_GROUPS && stateShare >= SKEW_SHARE;
        boolean skewed = skewBucket != null || stateSkew;
        List<Map<String, Object>> caveats = new ArrayList<>();
        if (blindspot != null) {
            caveats.add(caveat(COVERAGE_BLINDSPOT, "At least 75% of the rated outlets covering this come from the "
                    + blindspot + " (" + rated + " rated evidence groups). Coverage describes reporting, not truth.",
                    Map.of("side", blindspot, "nRated", rated)));
        }
        if (skewed) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("bucket", skewBucket);
            data.put("stateAffiliatedShare", stateShare);
            caveats.add(caveat(SKEWED_COVERAGE, stateSkew && skewBucket == null
                    ? "At least 75% of this coverage comes from state-affiliated outlets."
                    : "At least 75% of the rated coverage comes from the " + skewBucket + " bucket.", data));
        }
        if (total > 0 && byBucket.get(MediaVocabulary.UNRATED) / (double) total > 0.5) {
            caveats.add(caveat(MOSTLY_UNRATED, "Most of this coverage comes from outlets no declared provider rates.",
                    Map.of("unratedShare", balance.get(MediaVocabulary.UNRATED))));
        }
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("scope", scope);
        stats.put("articles", new LinkedHashMap<>(articles));
        stats.put("evidenceGroups", new LinkedHashMap<>(byBucket));
        stats.put("byCountry", byCountry);
        stats.put("byOutletType", byType);
        stats.put("byFactuality", byFactuality);
        stats.put("coverageBalance", balance);
        stats.put("stateAffiliatedShare", stateShare);
        stats.put("nRated", rated);
        stats.put("blindspot", blindspot);
        stats.put("corroborated", corroborated);
        stats.put("corroboratedShare", corroboratedShare);
        stats.put("skewed", skewed);
        stats.put("caveats", caveats);
        stats.put("analyzer", detector());
        stats.put("manifestId", null);
        stats.put("computedAt", computedAt == null ? null : computedAt.toString());
        return new Result(stats, blindspot, skewed, rated, stateShare, total);
    }

    /** The factuality band of a factuality consensus median: its rounding to 1…5, or {@code unrated}. */
    public static String factualityBand(Double median) {
        if (median == null || !Double.isFinite(median)) {
            return MediaVocabulary.UNRATED;
        }
        return Long.toString(Math.max(1, Math.min(5, Math.round(median))));
    }

    private static Map<String, Integer> buckets() {
        Map<String, Integer> out = new LinkedHashMap<>();
        for (String b : MediaVocabulary.BUCKETS) {
            out.put(b, 0);
        }
        return out;
    }

    private static Map<String, Object> caveat(String code, String text, Map<String, Object> data) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("code", code);
        out.put("text", text);
        out.put("data", data);
        return out;
    }

    private static double round(double v) {
        return Math.round(v * 10_000) / 10_000.0;
    }
}
