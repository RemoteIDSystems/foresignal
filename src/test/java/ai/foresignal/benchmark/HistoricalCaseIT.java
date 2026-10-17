package ai.foresignal.benchmark;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioMetrics;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.series.SeriesMeta;
import ai.foresignal.stats.RegionRollupComputer;
import ai.foresignal.stats.RegionRollupResolution;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.analytics.AnalyticsItSupport;
import ai.foresignal.testsupport.analytics.AnalyticsTestSources;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * The historical corpus (prompt 123): an operator case replays with {@code asOf} stepping a day at a time from its
 * window start, never reading a revision published later; non-replayable expectations are listed as skipped; and the
 * auto null region-weeks are sampled deterministically, leaving out region-weeks with an intel event at warning or
 * above or situation activity.
 */
@IntegrationTest
@Import(AnalyticsTestSources.class)
class HistoricalCaseIT extends AnalyticsItSupport {

    static final Instant BASE = Instant.parse("2025-01-01T00:00:00Z");

    @Autowired HistoricalSuite historical;
    @Autowired BenchmarkService service;
    @Autowired BenchmarkCaseRepository cases;
    @Autowired BenchmarkThresholds thresholds;

    @BeforeEach
    void clearCaches() {
        reader.clearCache();
    }

    static Instant day(int n) {
        return BASE.plus(Duration.ofDays(n));
    }

    @Test
    @DisplayName("an operator case replays with asOf stepping: the planted change is found, the later revision unseen")
    void operatorCaseReplays() {
        double[] v = new double[460];
        for (int i = 0; i < v.length; i++) {
            v[i] = (i < 430 ? 100 : 115) + Math.sin(i * 1.7);
        }
        seedDaily(AnalyticsTestSources.SERIES, "bench-step", BASE, v, SeriesMeta.of(SeriesMeta.PRICE,
                SeriesMeta.LEVEL, 1));
        // A revision of a window day, published long after the window: a replay as of the window must never read it.
        Instant late = day(500);
        seed(AnalyticsTestSources.SERIES, late, dailyPoint("bench-step", day(432), 1000, late));
        clock.setInstant(day(520));
        Map<String, Object> present = new LinkedHashMap<>();
        present.put("findingType", "change-point");
        present.put("present", true);
        present.put("seriesRef", "series:" + AnalyticsTestSources.SERIES + "/bench-step");
        present.put("from", day(420).toString());
        present.put("to", day(440).toString());
        Map<String, Object> other = Map.of("findingType", "co-occurrence", "present", true);
        BenchmarkCaseEntity c = service.createCase(new BenchmarkInputs.CaseInput("Planted level shift", null,
                day(425), day(445), List.of(), Map.of("findings", List.of(present, other))), "operator", "test");

        Map<String, List<ScenarioMetrics>> out = historical.run(Set.of("change-point"), JobMonitor.NONE, 0, 1);
        assertThat(out).containsOnlyKeys("change-point");
        ScenarioMetrics m = out.get("change-point").get(0);
        assertThat(m.scenario()).isEqualTo("historical");
        assertThat(m.caseId()).isEqualTo(c.getId());
        assertThat(m.metrics().get("expectations")).isEqualTo(2.0);
        assertThat(m.metrics().get("skippedExpectations")).isEqualTo(1.0);
        assertThat(m.metrics().get("decisions")).isEqualTo(21.0);
        assertThat(m.metrics().get("lookAheadViolations")).isZero();
        assertThat(m.metrics().get("missedExpectations")).isZero();
        assertThat(m.truth()).containsEntry("name", "Planted level shift");
        assertThat(m.notes()).anyMatch(n -> n.contains("not replayable"));

        // Through the thresholds, the case is a historical scenario of the change-point component.
        assertThat(thresholds.evaluate("change-point", m).checks()).extracting(BenchmarkResults.Check::metric)
                .contains("missedExpectations", "unexpectedFindings", "lookAheadViolations");
    }

    void regionDays(String region, Instant from, Instant to) {
        for (Instant t = from; t.isBefore(to); t = t.plus(Duration.ofDays(1))) {
            jdbc.update("INSERT INTO activity_rollup (resolution, bucket_start, source_id, kind, cell, region_code,"
                    + " versions, entities, firsts, tombstones, edges, changes) VALUES (?, ?, '*', 'bench', ?, ?, 3, 3,"
                    + " 0, 0, 0, 0)", RegionRollupResolution.DAY.code(), Timestamp.from(t),
                    RegionRollupComputer.regionCell(region), region);
        }
    }

    @Test
    @DisplayName("auto null cases: twenty region-weeks drawn deterministically, excluding intel events and situations")
    void nullCases() {
        Instant now = day(120);
        clock.setInstant(now);
        for (String region : List.of("XB", "XC", "XD")) {
            regionDays(region, day(10), now);
        }
        LocalDate week = LocalDate.ofInstant(day(40), ZoneOffset.UTC).with(TemporalAdjusters.previousOrSame(
                DayOfWeek.MONDAY));
        Instant monday = week.atStartOfDay(ZoneOffset.UTC).toInstant();
        jdbc.update("INSERT INTO intelligence_event (id, type, title, severity, status, start_time, region_tags, origin,"
                + " version, created_at, updated_at) VALUES (?, 'bench', 'Warning event', 'warning', 'active', ?,"
                + " CAST(? AS jsonb), 'recipe', 1, ?, ?)", Ulids.next(now), Timestamp.from(monday.plus(Duration
                .ofDays(2))), "[\"XC\"]", Timestamp.from(now), Timestamp.from(now));
        jdbc.update("INSERT INTO situation (id, title, theme, status, severity, region_codes, started_at,"
                + " last_activity_at, summary, origin, version, created_at, updated_at) VALUES (?, 'Bench situation',"
                + " 'conflict', 'active', 'warning', CAST(? AS text[]), ?, ?, '', 'linker', 1, ?, ?)", Ulids.next(now),
                "{XD}", Timestamp.from(monday.plus(Duration.ofDays(1))), Timestamp.from(monday.plus(Duration
                        .ofDays(2))), Timestamp.from(now), Timestamp.from(now));

        int added = historical.sampleNullCases(now);
        assertThat(added).isEqualTo(HistoricalSuite.NULL_CASES_PER_MONTH);
        List<String> first = autoNames();
        assertThat(first).doesNotContain(HistoricalSuite.nullCaseName("XC", week.toString()),
                HistoricalSuite.nullCaseName("XD", week.toString()));
        for (BenchmarkCaseEntity c : cases.findBySource("auto")) {
            assertThat(c.getRegionCodes()).hasSize(1);
            assertThat(Duration.between(c.getWindowFrom(), c.getWindowTo())).isEqualTo(Duration.ofDays(7));
            assertThat(c.getWindowFrom().atZone(ZoneOffset.UTC).getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
            assertThat(String.valueOf(c.getExpectations().get("findings"))).contains("present=false");
            assertThat(c.getCreatedBy()).isEqualTo("scheduler");
        }

        cases.deleteAll();
        assertThat(historical.sampleNullCases(now)).isEqualTo(HistoricalSuite.NULL_CASES_PER_MONTH);
        assertThat(autoNames()).as("the same month draws the same region-weeks").isEqualTo(first);

        Instant to = LocalDate.ofInstant(now, ZoneOffset.UTC).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay(ZoneOffset.UTC).toInstant();
        int candidates = historical.nullCandidates(to.minus(Duration.ofDays(364)), to).size();
        int more = historical.sampleNullCases(now);
        assertThat(more).isEqualTo(Math.min(HistoricalSuite.NULL_CASES_PER_MONTH,
                Math.max(0, candidates - HistoricalSuite.NULL_CASES_PER_MONTH)));
        assertThat(cases.findBySource("auto")).hasSize(HistoricalSuite.NULL_CASES_PER_MONTH + more);

        Map<String, List<ScenarioMetrics>> replay = historical.run(Set.of("change-point"), JobMonitor.NONE, 0, 1);
        assertThat(replay.get("change-point")).hasSize(HistoricalSuite.NULL_CASES_PER_MONTH + more);
        assertThat(replay.get("change-point")).allMatch(m -> m.metrics().get("unexpectedFindings") == 0.0);
    }

    List<String> autoNames() {
        return cases.findBySource("auto").stream().map(BenchmarkCaseEntity::getName).sorted().toList();
    }
}
