package ai.foresignal.social.correlation;

import ai.foresignal.precursor.PrecursorSettings;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.springframework.stereotype.Component;

/**
 * Lead-time statistics (prompt 137, {@code GET /v1/social/lead-times}, OpenAPI {@code LeadTimeStats}): corroborated
 * comparisons grouped by claim type, platform, region and {@code foundBy}, with the median and interquartile lead of
 * social first observation over physical, news and official first observation, and counts.
 *
 * <p>The training cut-off splits all corroborated comparisons by evaluation time at the 120 discovery fraction
 * ({@code analysis.precursors.discoveryFraction}, 2/3): comparisons evaluated after it are out of sample, and only they
 * support a "leads by" statement (SOC-5). A comparison counts toward each platform of its narrative.
 */
@Component
public class LeadTimeStatistics {

    /** The comparisons read. */
    static final int LIMIT = 50_000;

    /** The out-of-sample comparisons a group needs before assessments state its lead. */
    public static final int MIN_OUT_OF_SAMPLE = 5;

    private final ComparisonStore store;
    private final AppSettingService settings;
    private final Clock clock;

    /** Creates the component. */
    public LeadTimeStatistics(ComparisonStore store, AppSettingService settings, Clock clock) {
        this.store = store;
        this.settings = settings;
        this.clock = clock;
    }

    /**
     * The filters.
     *
     * @param claimType a claim type, or null
     * @param platform a network, or null
     * @param region a 040 region (its admin-1 regions too), or null
     * @param foundBy {@code organic} or {@code cue}, or null
     * @param since the earliest evaluation, or null
     */
    public record Filters(String claimType, String platform, String region, String foundBy, Instant since) {}

    /**
     * One corroborated comparison as the statistics see it.
     *
     * @param claimType the claim type
     * @param platforms the networks
     * @param regionCode the region, or null
     * @param foundBy the discovery path
     * @param evaluatedAt the evaluation time
     * @param overPhysical the lead over physical first observation (s), or null
     * @param overNews the lead over news (s), or null
     * @param overOfficial the lead over official first observation (s), or null
     */
    public record Point(String claimType, List<String> platforms, String regionCode, String foundBy,
            Instant evaluatedAt, Double overPhysical, Double overNews, Double overOfficial) {}

    /** The {@code LeadTimeSummary} of values (seconds). Pure. */
    public static Map<String, Object> summary(List<Double> values) {
        List<Double> v = values.stream().filter(Objects::nonNull).sorted().toList();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("n", v.size());
        out.put("median", v.isEmpty() ? null : quantile(v, 0.5));
        out.put("p25", v.isEmpty() ? null : quantile(v, 0.25));
        out.put("p75", v.isEmpty() ? null : quantile(v, 0.75));
        return out;
    }

    /** The linear-interpolation quantile of sorted values. */
    static double quantile(List<Double> sorted, double q) {
        if (sorted.size() == 1) {
            return sorted.get(0);
        }
        double pos = q * (sorted.size() - 1);
        int lo = (int) Math.floor(pos);
        int hi = (int) Math.ceil(pos);
        return sorted.get(lo) + (sorted.get(hi) - sorted.get(lo)) * (pos - lo);
    }

    /** The training cut-off of points: the evaluation time at the discovery fraction (EPOCH without points). Pure. */
    public static Instant cutoff(List<Point> points, double fraction) {
        if (points.isEmpty()) {
            return Instant.EPOCH;
        }
        List<Instant> times = points.stream().map(Point::evaluatedAt).sorted().toList();
        int index = (int) Math.floor(fraction * (times.size() - 1));
        return times.get(Math.max(0, Math.min(times.size() - 1, index)));
    }

    /** The statistics of points under filters. Pure. */
    public static Map<String, Object> compute(List<Point> points, Filters f, double fraction, Instant now) {
        Instant cutoff = cutoff(points, fraction);
        Map<String, List<Point>> groups = new TreeMap<>();
        for (Point p : points) {
            if (f.claimType() != null && !f.claimType().equals(p.claimType())) {
                continue;
            }
            if (f.foundBy() != null && !f.foundBy().equals(p.foundBy())) {
                continue;
            }
            if (f.region() != null && (p.regionCode() == null || !(p.regionCode().equals(f.region())
                    || p.regionCode().startsWith(f.region() + "-")))) {
                continue;
            }
            if (f.since() != null && p.evaluatedAt().isBefore(f.since())) {
                continue;
            }
            for (String platform : p.platforms().isEmpty() ? List.of("unknown") : p.platforms()) {
                if (f.platform() != null && !f.platform().equals(platform)) {
                    continue;
                }
                String key = p.claimType() + "\u0000" + platform + "\u0000" + (p.regionCode() == null ? ""
                        : p.regionCode()) + "\u0000" + p.foundBy();
                groups.computeIfAbsent(key, k -> new ArrayList<>()).add(p);
            }
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (Map.Entry<String, List<Point>> e : groups.entrySet()) {
            String[] k = e.getKey().split("\u0000", -1);
            List<Point> ps = e.getValue();
            List<Point> oos = ps.stream().filter(p -> p.evaluatedAt().isAfter(cutoff)).toList();
            Map<String, Object> g = new LinkedHashMap<>();
            g.put("claimType", k[0]);
            g.put("platform", k[1]);
            g.put("regionCode", k[2].isEmpty() ? null : k[2]);
            g.put("foundBy", k[3]);
            g.put("comparisons", ps.size());
            g.put("outOfSample", oos.size());
            g.put("overPhysical", summary(ps.stream().map(Point::overPhysical).toList()));
            g.put("overNews", summary(ps.stream().map(Point::overNews).toList()));
            g.put("overOfficial", summary(ps.stream().map(Point::overOfficial).toList()));
            g.put("outOfSampleOverPhysical", summary(oos.stream().map(Point::overPhysical).toList()));
            items.add(g);
        }
        items.sort(Comparator.comparing((Map<String, Object> g) -> -((Number) g.get("comparisons")).intValue()));
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("claimType", f.claimType());
        filters.put("platform", f.platform());
        filters.put("region", f.region());
        filters.put("foundBy", f.foundBy());
        filters.put("since", f.since() == null ? null : f.since().toString());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("generatedAt", now.toString());
        out.put("trainingCutoff", cutoff.toString());
        out.put("filters", filters);
        out.put("groups", items);
        out.put("caveats", List.of(Map.of("code", "OUT_OF_SAMPLE_ONLY", "text", "Only comparisons evaluated after the"
                + " training cut-off (outOfSampleOverPhysical) support a statement that a kind of social signal leads"
                + " the physical world; the other summaries describe the training period. Cue-found narratives were"
                + " found by looking and are kept apart (foundBy).")));
        return out;
    }

    /** The corroborated comparisons as points. */
    public List<Point> points() {
        List<Point> out = new ArrayList<>();
        for (ComparisonStore.Comparison c : store.finalSince(Instant.EPOCH, LIMIT)) {
            if (!CorrelationVocabulary.CORROBORATED.equals(c.status()) || c.leadTimes() == null) {
                continue;
            }
            out.add(new Point(c.claimType(), c.platforms(), c.regionCode(), c.foundBy(), c.evaluatedAt(),
                    number(c.leadTimes().get("leadOverPhysicalSeconds")), number(c.leadTimes().get(
                            "leadOverNewsSeconds")), number(c.leadTimes().get("leadOverOfficialSeconds"))));
        }
        return out;
    }

    /** The discovery fraction in force. */
    public double fraction() {
        Number n = settings.get(SettingKeys.ANALYSIS_PRECURSORS_DISCOVERY_FRACTION, Number.class).orElse(null);
        double f = n == null ? PrecursorSettings.DEFAULT_DISCOVERY_FRACTION : n.doubleValue();
        return Math.max(0.5, Math.min(0.9, f));
    }

    /** The {@code LeadTimeStats} under filters. */
    public Map<String, Object> stats(Filters f) {
        return compute(points(), f, fraction(), clock.instant());
    }

    /**
     * The notable out-of-sample lead times of assessments ({@code SocialVersusObserved.leadTimes}): groups with at
     * least {@value #MIN_OUT_OF_SAMPLE} out-of-sample comparisons.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> notable(String regionPrefix) {
        Map<String, Object> s = stats(new Filters(null, null, regionPrefix, null, null));
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> g : (List<Map<String, Object>>) s.get("groups")) {
            if (((Number) g.get("outOfSample")).intValue() < MIN_OUT_OF_SAMPLE) {
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("claimType", g.get("claimType"));
            m.put("platform", g.get("platform"));
            m.put("regionCode", g.get("regionCode"));
            m.put("overPhysical", g.get("outOfSampleOverPhysical"));
            out.add(m);
        }
        return out;
    }

    private static Double number(Object v) {
        return v instanceof Number n ? n.doubleValue() : null;
    }
}
