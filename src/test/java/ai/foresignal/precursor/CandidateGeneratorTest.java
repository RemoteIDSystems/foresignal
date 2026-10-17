package ai.foresignal.precursor;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Candidate generation of a mining batch (prompt 120 §2): top-K families per region, the lag grid, the cap and the
 * ranking, operator pairs always included, library patterns left out, and related series pairs.
 */
class CandidateGeneratorTest {

    static CandidateGenerator.FamilyCount family(String region, String type, long count, boolean admin1) {
        return new CandidateGenerator.FamilyCount(region, "intelEventType:" + type, Map.of("intelEventType", type),
                null, "intel events of type " + type, count, admin1);
    }

    /** Fourteen families in YE (counts 14..1), two in US, one untagged. */
    static List<CandidateGenerator.FamilyCount> families() {
        List<CandidateGenerator.FamilyCount> out = new ArrayList<>();
        for (int i = 1; i <= 14; i++) {
            out.add(family("YE", "type-" + (char) ('a' + i), i, false));
        }
        out.add(family("US", "quake", 30, true));
        out.add(family("US", "outage", 20, true));
        out.add(family("ZZ", "untagged", 100, false));
        return out;
    }

    static Candidate operator(String type) {
        return new Candidate(Candidate.EVENT, Map.of("selector", Map.of("intelEventType", type), "label", type),
                Map.of("selector", Map.of("alertRuleTag", "gnss"), "label", "gnss"), Map.of("rule", "none"),
                Duration.ZERO, Duration.ofDays(2), List.of(), "operator", "01JA8Q3V7S2K9W4T6Y1B5N0M3" + type.length(), 0,
                0, null);
    }

    static CandidateGenerator.Settings cap(int maxTests) {
        return new CandidateGenerator.Settings(CandidateGenerator.DEFAULT_TOP_K, CandidateGenerator.DEFAULT_LAG_GRID,
                maxTests);
    }

    @Test
    @DisplayName("top-K per region: 12 of 14 YE families, every ordered pair, four lags; ZZ forms no pairs")
    void topK() {
        CandidateGenerator.Plan plan = CandidateGenerator.plan(List.of(), families(), List.of(), null, Set.of(),
                cap(5000));

        int ye = 12 * 11 * 4;
        int us = 2 * 1 * 4;
        assertThat(plan.generated()).isEqualTo(ye + us);
        assertThat(plan.selected()).hasSize(ye + us);
        assertThat(plan.capped()).isZero();
        assertThat(plan.selected()).noneMatch(c -> c.antecedent().toString().contains("type-b")
                || c.antecedent().toString().contains("type-c"));
        assertThat(plan.selected()).noneMatch(c -> c.regionCodes().contains("ZZ"));
        assertThat(plan.selected()).allMatch(c -> !c.antecedent().equals(c.consequent()));
    }

    @Test
    @DisplayName("the lag grid: minLag 0 and maxLag 1, 3, 7 and 14 days per pair; sameRegion by country or admin1")
    void lagGrid() {
        CandidateGenerator.Plan plan = CandidateGenerator.plan(List.of(), families(), List.of(), null, Set.of(),
                cap(5000));

        List<Candidate> quakeToOutage = plan.selected().stream().filter(c -> c.antecedent().toString()
                .contains("quake") && c.consequent().toString().contains("outage")).toList();
        assertThat(quakeToOutage).extracting(Candidate::lagMax).containsExactlyInAnyOrder(Duration.ofDays(1),
                Duration.ofDays(3), Duration.ofDays(7), Duration.ofDays(14));
        assertThat(quakeToOutage).allMatch(c -> c.lagMin().isZero());
        assertThat(quakeToOutage).allMatch(c -> "admin1".equals(c.spatial().get("regionLevel")));
        assertThat(quakeToOutage).allMatch(c -> c.regionCodes().equals(List.of("US")));
        assertThat(((Map<?, ?>) quakeToOutage.get(0).antecedent().get("selector")).get("regionCodes"))
                .isEqualTo(List.of("US"));
        assertThat(plan.selected()).filteredOn(c -> c.regionCodes().contains("YE"))
                .allMatch(c -> "country".equals(c.spatial().get("regionLevel")));
    }

    @Test
    @DisplayName("the cap keeps operator pairs first, then the busiest antecedents, and reports what it cut")
    void capAndRanking() {
        List<Candidate> operator = List.of(operator("one"), operator("three"));
        CandidateGenerator.Plan plan = CandidateGenerator.plan(operator, families(), List.of(), null, Set.of(),
                cap(10));

        assertThat(plan.selected()).hasSize(10);
        assertThat(plan.operator()).isEqualTo(2);
        assertThat(plan.selected().subList(0, 2)).allMatch(c -> "operator".equals(c.origin()));
        assertThat(plan.generated()).isEqualTo(2 + 12 * 11 * 4 + 2 * 1 * 4);
        assertThat(plan.capped()).isEqualTo(plan.generated() - 10);
        assertThat(plan.selected().subList(2, 6)).allMatch(c -> c.nAntecedent() == 30);
        assertThat(plan.selected().subList(6, 10)).allMatch(c -> c.nAntecedent() == 20);
        List<Long> ranks = plan.selected().subList(2, 10).stream().map(Candidate::nAntecedent).toList();
        assertThat(ranks).isSortedAccordingTo((a, b) -> Long.compare(b, a));
    }

    @Test
    @DisplayName("operator pairs are always included, even beyond the cap")
    void operatorAlwaysIncluded() {
        List<Candidate> operator = List.of(operator("one"), operator("three"), operator("seven"));
        CandidateGenerator.Plan plan = CandidateGenerator.plan(operator, families(), List.of(), null, Set.of(), cap(1));

        assertThat(plan.selected()).hasSize(3).allMatch(c -> "operator".equals(c.origin()));
        assertThat(plan.capped()).isEqualTo(12 * 11 * 4 + 2 * 1 * 4);
    }

    @Test
    @DisplayName("patterns already validated, stale or retired are not generated again")
    void excluded() {
        Candidate op = operator("one");
        CandidateGenerator.Plan all = CandidateGenerator.plan(List.of(op), families(), List.of(), null, Set.of(),
                cap(5000));
        Candidate firstAuto = all.selected().get(1);

        CandidateGenerator.Plan plan = CandidateGenerator.plan(List.of(op), families(), List.of(), null,
                Set.of(op.key(), firstAuto.key()), cap(5000));

        assertThat(plan.selected()).extracting(Candidate::key).doesNotContain(op.key(), firstAuto.key());
        assertThat(plan.generated()).isEqualTo(all.generated() - 2);
    }

    @Test
    @DisplayName("series pairs: related by region, family or topic, both directions; lags 1..14 days or 1..8 weeks")
    void seriesPairs() {
        List<CandidateGenerator.SeriesInfo> series = List.of(
                new CandidateGenerator.SeriesInfo("series:a/oil", "day", "YE", "markets", null, 300, "oil"),
                new CandidateGenerator.SeriesInfo("series:a/views", "day", "YE", "attention", null, 200, "views"),
                new CandidateGenerator.SeriesInfo("series:a/quakes", "day", "JP", "seismic", null, 100, "quakes"),
                new CandidateGenerator.SeriesInfo("series:a/cot", "week", null, "markets", null, 50, "cot"));

        CandidateGenerator.Plan plan = CandidateGenerator.plan(List.of(), List.of(), series, (x, y) -> 42, Set.of(),
                cap(5000));

        assertThat(plan.selected()).hasSize(4);
        assertThat(plan.selected()).noneMatch(c -> c.antecedent().containsValue("series:a/quakes")
                || c.consequent().containsValue("series:a/quakes"));
        Candidate oilViews = plan.selected().stream().filter(c -> "series:a/oil".equals(c.antecedent()
                .get("seriesRef")) && "series:a/views".equals(c.consequent().get("seriesRef"))).findFirst()
                .orElseThrow();
        assertThat(oilViews.lagMin()).isEqualTo(Duration.ofDays(1));
        assertThat(oilViews.lagMax()).isEqualTo(Duration.ofDays(14));
        assertThat(oilViews.frequency()).isEqualTo("day");
        assertThat(oilViews.overlap()).isEqualTo(42);
        assertThat(oilViews.antecedent()).containsEntry("threshold", PrecursorRules.LIVE_THRESHOLD);
        Candidate oilCot = plan.selected().stream().filter(c -> "series:a/cot".equals(c.consequent()
                .get("seriesRef"))).findFirst().orElseThrow();
        assertThat(oilCot.frequency()).isEqualTo("week");
        assertThat(oilCot.lagMax()).isEqualTo(Duration.ofDays(7 * 8));
        assertThat(plan.selected().get(0).nAntecedent()).isEqualTo(300);
    }

    @Test
    @DisplayName("the plan is deterministic")
    void deterministic() {
        CandidateGenerator.Plan a = CandidateGenerator.plan(List.of(operator("x")), families(), List.of(), null,
                Set.of(), cap(50));
        CandidateGenerator.Plan b = CandidateGenerator.plan(List.of(operator("x")), families(), List.of(), null,
                Set.of(), cap(50));
        assertThat(a.selected()).extracting(Candidate::key).containsExactlyElementsOf(
                b.selected().stream().map(Candidate::key).toList());
    }
}
