package ai.foresignal.precursor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.ToLongBiFunction;

/**
 * Generates the candidates of one mining batch (prompt 120 §2). Pure and deterministic.
 *
 * <ul>
 *   <li><b>Operator pairs</b> ({@code precursor_candidate} rows) are always included and are never capped.
 *   <li><b>Automatic event pairs:</b> per region, the top {@code K} (12) event families by occurrence count over the
 *       discovery window; every ordered pair of two different families, with {@code minLag} 0 and each
 *       {@code maxLag} of the grid ({@code 1 d, 3 d, 7 d, 14 d}); spatial rule {@code sameRegion} by country, or by
 *       first-level region when both families carry admin1 regions. Untagged occurrences ({@code ZZ}) form no pairs.
 *   <li><b>Automatic series pairs:</b> ordered pairs of enabled analysis series that share a region, a family (the
 *       theme of a series) or a topic, with lags {@code 1…L} buckets ({@code L} = 14 for daily, 8 for weekly series);
 *       a pair of a daily and a weekly series is tested weekly.
 *   <li><b>Cap:</b> automatic candidates are ranked by the antecedent's occurrence count (or usable points), then the
 *       pair's data overlap, then their key, and cut at {@code maxTestsPerRun} minus the operator pairs; the number cut
 *       is reported as {@code capped}.
 * </ul>
 *
 * <p>Patterns already in the library as {@code validated}, {@code stale} or {@code retired} are left out (they are
 * managed by the re-test).
 */
public final class CandidateGenerator {

    /** The default top-K families per region. */
    public static final int DEFAULT_TOP_K = 12;

    /** The default lag grid of automatic event pairs. */
    public static final List<Duration> DEFAULT_LAG_GRID = List.of(Duration.ofDays(1), Duration.ofDays(3),
            Duration.ofDays(7), Duration.ofDays(14));

    /** The default cap. */
    public static final int DEFAULT_MAX_TESTS = 500;

    /** The largest lag of daily series pairs, in days. */
    public static final int DAILY_MAX_LAG = 14;

    /** The largest lag of weekly series pairs, in weeks. */
    public static final int WEEKLY_MAX_LAG = 8;

    private CandidateGenerator() {}

    /**
     * An event family with its occurrence count in one region over the discovery window.
     *
     * @param region the country (ISO 3166-1 alpha-2)
     * @param familyKey a stable key of the family ({@code intelEventType:maritime-disruption}, …)
     * @param selector the family's {@code EventSelector} (without regions)
     * @param signalFamily the 119 signal family it belongs to, or null
     * @param label a display label
     * @param count the occurrences
     * @param admin1 true when the family's occurrences carry first-level regions
     */
    public record FamilyCount(String region, String familyKey, Map<String, Object> selector, String signalFamily,
            String label, long count, boolean admin1) {}

    /**
     * An enabled analysis series.
     *
     * @param ref the series reference
     * @param frequency {@code day} or {@code week}
     * @param region its 040 region, or null
     * @param family its signal family, or null
     * @param topic its attention topic, or null
     * @param points its usable points in the range
     * @param label a display label
     */
    public record SeriesInfo(String ref, String frequency, String region, String family, String topic, long points,
            String label) {}

    /**
     * Generation settings.
     *
     * @param topK families per region
     * @param lagGrid the {@code maxLag} values of automatic event pairs
     * @param maxTests the cap
     */
    public record Settings(int topK, List<Duration> lagGrid, int maxTests) {

        /** The defaults. */
        public static Settings defaults() {
            return new Settings(DEFAULT_TOP_K, DEFAULT_LAG_GRID, DEFAULT_MAX_TESTS);
        }
    }

    /**
     * The batch plan.
     *
     * @param selected the candidates to test: operator pairs first, then automatic ones by rank
     * @param generated every candidate generated (operator and automatic, before the cap)
     * @param capped the automatic candidates cut by the cap
     * @param operator the operator pairs included
     */
    public record Plan(List<Candidate> selected, int generated, int capped, int operator) {}

    /**
     * Plans a batch.
     *
     * @param operator the operator candidates
     * @param families the event families per region
     * @param series the enabled analysis series
     * @param overlap the data overlap of a series pair (buckets where both have a value)
     * @param excludedKeys keys of library patterns not to generate again
     * @param settings the settings
     * @return the plan
     */
    public static Plan plan(List<Candidate> operator, List<FamilyCount> families, List<SeriesInfo> series,
            ToLongBiFunction<SeriesInfo, SeriesInfo> overlap, Set<String> excludedKeys, Settings settings) {
        Set<String> excluded = excludedKeys == null ? Set.of() : excludedKeys;
        Map<String, Candidate> chosenOperator = new LinkedHashMap<>();
        for (Candidate c : operator == null ? List.<Candidate>of() : operator) {
            if (!excluded.contains(c.key())) {
                chosenOperator.putIfAbsent(c.key(), c);
            }
        }
        Map<String, Candidate> auto = new LinkedHashMap<>();
        for (Candidate c : eventPairs(families, settings)) {
            if (!excluded.contains(c.key()) && !chosenOperator.containsKey(c.key())) {
                auto.putIfAbsent(c.key(), c);
            }
        }
        for (Candidate c : seriesPairs(series, overlap)) {
            if (!excluded.contains(c.key()) && !chosenOperator.containsKey(c.key())) {
                auto.putIfAbsent(c.key(), c);
            }
        }
        List<Candidate> ranked = new ArrayList<>(auto.values());
        ranked.sort(Comparator.comparingLong(Candidate::nAntecedent).reversed()
                .thenComparing(Comparator.comparingLong(Candidate::overlap).reversed())
                .thenComparing(Candidate::key));
        int room = Math.max(0, settings.maxTests() - chosenOperator.size());
        List<Candidate> selected = new ArrayList<>(chosenOperator.values());
        selected.addAll(ranked.subList(0, Math.min(room, ranked.size())));
        int capped = Math.max(0, ranked.size() - room);
        return new Plan(List.copyOf(selected), chosenOperator.size() + ranked.size(), capped, chosenOperator.size());
    }

    /** The automatic event pairs: top-K families per region, every ordered pair, every lag of the grid. */
    static List<Candidate> eventPairs(List<FamilyCount> families, Settings settings) {
        Map<String, List<FamilyCount>> byRegion = new TreeMap<>();
        for (FamilyCount f : families == null ? List.<FamilyCount>of() : families) {
            if (f.region() == null || f.region().length() != 2 || "ZZ".equals(f.region()) || f.count() <= 0) {
                continue;
            }
            byRegion.computeIfAbsent(f.region(), k -> new ArrayList<>()).add(f);
        }
        List<Candidate> out = new ArrayList<>();
        for (Map.Entry<String, List<FamilyCount>> entry : byRegion.entrySet()) {
            List<FamilyCount> top = new ArrayList<>(entry.getValue());
            top.sort(Comparator.comparingLong(FamilyCount::count).reversed().thenComparing(FamilyCount::familyKey));
            top = top.subList(0, Math.min(settings.topK(), top.size()));
            for (FamilyCount a : top) {
                for (FamilyCount b : top) {
                    if (a.familyKey().equals(b.familyKey())) {
                        continue;
                    }
                    for (Duration maxLag : settings.lagGrid()) {
                        out.add(eventCandidate(entry.getKey(), a, b, maxLag));
                    }
                }
            }
        }
        return out;
    }

    static Candidate eventCandidate(String region, FamilyCount a, FamilyCount b, Duration maxLag) {
        Map<String, Object> spatial = new LinkedHashMap<>();
        spatial.put("rule", "sameRegion");
        spatial.put("regionLevel", a.admin1() && b.admin1() ? "admin1" : "country");
        long overlap = Math.max(a.count(), b.count()) == 0 ? 0
                : Math.round(1000.0 * Math.min(a.count(), b.count()) / Math.max(a.count(), b.count()));
        return new Candidate(Candidate.EVENT, eventSide(a, region), eventSide(b, region), spatial, Duration.ZERO, maxLag,
                List.of(region), "auto", null, a.count(), overlap, null);
    }

    static Map<String, Object> eventSide(FamilyCount family, String region) {
        Map<String, Object> selector = new LinkedHashMap<>(family.selector());
        selector.put("regionCodes", List.of(region));
        Map<String, Object> side = new LinkedHashMap<>();
        side.put("selector", selector);
        side.put("family", family.signalFamily() != null ? family.signalFamily() : family.familyKey());
        side.put("label", family.label() + " in " + region);
        return side;
    }

    /** The automatic series pairs: ordered pairs sharing a region, family or topic. */
    static List<Candidate> seriesPairs(List<SeriesInfo> series, ToLongBiFunction<SeriesInfo, SeriesInfo> overlap) {
        List<SeriesInfo> sorted = new ArrayList<>(series == null ? List.of() : series);
        sorted.sort(Comparator.comparing(SeriesInfo::ref));
        List<Candidate> out = new ArrayList<>();
        for (SeriesInfo a : sorted) {
            for (SeriesInfo b : sorted) {
                if (a.ref().equals(b.ref()) || !related(a, b)) {
                    continue;
                }
                out.add(seriesCandidate(a, b, overlap == null ? Math.min(a.points(), b.points())
                        : overlap.applyAsLong(a, b)));
            }
        }
        return out;
    }

    /**
     * True when two series share a region (other than {@code ZZ}), a family or a topic. Social series (prompt 137,
     * family {@code social}) are antecedents only, paired with the non-social, non-attention series of their region.
     */
    static boolean related(SeriesInfo a, SeriesInfo b) {
        if ("social".equals(b.family())) {
            return false;
        }
        if ("social".equals(a.family())) {
            return a.region() != null && !"ZZ".equals(a.region()) && a.region().equals(b.region())
                    && !"attention".equals(b.family());
        }
        boolean region = a.region() != null && !"ZZ".equals(a.region()) && a.region().equals(b.region());
        boolean family = a.family() != null && a.family().equals(b.family());
        boolean topic = a.topic() != null && a.topic().equals(b.topic());
        return region || family || topic;
    }

    /** The series candidate of {@code a} leading {@code b}. */
    static Candidate seriesCandidate(SeriesInfo a, SeriesInfo b, long overlap) {
        String frequency = "week".equals(a.frequency()) || "week".equals(b.frequency()) ? "week" : "day";
        Duration width = "week".equals(frequency) ? Duration.ofDays(7) : Duration.ofDays(1);
        int lags = "week".equals(frequency) ? WEEKLY_MAX_LAG : DAILY_MAX_LAG;
        Set<String> regions = new LinkedHashSet<>();
        for (String r : new String[] {a.region(), b.region()}) {
            if (r != null && !"ZZ".equals(r)) {
                regions.add(r);
            }
        }
        return new Candidate(Candidate.SERIES, seriesSide(a), seriesSide(b), Map.of("rule", "none"), width,
                width.multipliedBy(lags), List.copyOf(regions), "auto", null, a.points(), overlap, frequency);
    }

    static Map<String, Object> seriesSide(SeriesInfo s) {
        Map<String, Object> side = new LinkedHashMap<>();
        side.put("seriesRef", s.ref());
        side.put("family", s.family());
        side.put("label", Objects.requireNonNullElse(s.label(), s.ref()));
        side.put("threshold", PrecursorRules.LIVE_THRESHOLD);
        return side;
    }
}
