package ai.foresignal.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.analytics.eventstudy.EventStudyEngine;
import ai.foresignal.analytics.eventstudy.LiveEventStudyService;
import ai.foresignal.analytics.math.PlaceboSampler;
import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesFrequency;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.backtest.PrecursorTester;
import ai.foresignal.baserate.BaseRateCalculator;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioMetrics;
import ai.foresignal.benchmark.BenchmarkResults.SuiteParams;
import ai.foresignal.benchmark.component.ChangePointBenchmark;
import ai.foresignal.benchmark.scenario.InMemorySeries;
import ai.foresignal.benchmark.scenario.ScenarioGenerator;
import ai.foresignal.benchmark.scenario.ScenarioType;
import ai.foresignal.precursor.PrecursorRules;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * No look-ahead in the intelligence components of prompts 116–123 (checkpoint 124, focus area 3; invariant INT-2):
 * records and series points added after a cutoff, or known only after the decision time, leave the outputs unchanged.
 *
 * <p>Covered here in the components' pure form: the event-study engine over what a live pre-event check and a batch
 * study read ({@code knownAt} after the decision ignored), precursor discovery and validation windows, base rates and
 * the change-point decisions under late revisions. The database paths (analog search, retrospective ledger
 * evaluation and the live precursor watch) are covered by {@code IntelligenceLookAheadIT}; the windowed rules of
 * prompt 100 by {@code ai.foresignal.trigger.window.LookAheadTest}.
 */
class LookAheadTest {

    static final long SEED = 1_240_002L;

    /** The engine over what the live pre-event check reads for {@code event}, as of the event. */
    static EventStudyEngine.Result liveCheck(InMemorySeries series, Instant event, List<Instant> others) {
        EventStudyEngine.Windows w = EventStudyEngine.Windows.DAILY;
        int t0 = series.indexOf(event);
        SeriesData data = series.read(t0 + LiveEventStudyService.preEventReadOffset(w), t0 + 1, event);
        SeriesData.Grid grid = data.grid();
        for (Instant known : grid.knownAt()) {
            if (known != null) {
                assertThat(known).as("a value the check reads").isBeforeOrEqualTo(event);
            }
        }
        return EventStudyEngine.run(new EventStudyEngine.Input(grid.starts(), grid.values(), SeriesFrequency.DAY,
                EventStudyEngine.Transform.of(false, null, "price"), w, List.of(new EventStudyEngine.Event(event,
                        "e")), others, 11L, PlaceboSampler.DEFAULT_COUNT, false));
    }

    @Test
    @DisplayName("event study (live and batch): revisions known after the event and later points change nothing")
    void eventStudyIgnoresLaterKnowledge() {
        ScenarioGenerator.MarketScenario sc = ScenarioGenerator.preEventMove(SEED, 0);
        InMemorySeries series = sc.series();
        Instant event = sc.trueEvents().get(3);
        List<Instant> others = sc.trueEvents().subList(0, 3);
        EventStudyEngine.Result before = liveCheck(series, event, others);
        int t0 = series.indexOf(event);
        Instant later = event.plus(Duration.ofDays(2));
        // Revise the pre-event window and the estimation window after the event, and publish later buckets.
        for (int i = t0 - 260; i < Math.min(series.size(), t0 + 40); i++) {
            series.publish(i, series.valueAsOf(i, null) * 1.25, later);
        }
        EventStudyEngine.Result after = liveCheck(series, event, others);
        assertThat(after).usingRecursiveComparison().withComparatorForType(Double::compare, Double.class).isEqualTo(before);
        // A batch study reads every bucket known by its as-of time with the same reader contract.
        SeriesData batch = series.read(0, t0 + 1, event);
        for (int i = 0; i < batch.grid().size(); i++) {
            if (batch.grid().knownAt()[i] != null) {
                assertThat(batch.grid().knownAt()[i]).isBeforeOrEqualTo(event);
            }
        }
    }

    @Test
    @DisplayName("precursor discovery and validation windows: occurrences after the window's end change nothing")
    void precursorWindowsIgnoreLaterOccurrences() {
        ScenarioGenerator.EventStreamScenario sc = ScenarioGenerator.eventStreams(ScenarioType.TRUE_PRECURSOR, SEED, 0);
        Instant from = sc.rangeEnd().minus(Duration.ofDays(365));
        Instant split = from.plus(Duration.ofDays(243));
        List<PrecursorTester.Occurrence> antecedents = new ArrayList<>();
        List<PrecursorTester.Occurrence> consequents = new ArrayList<>();
        for (PrecursorTester.Occurrence o : sc.antecedents()) {
            if (o.time().isBefore(split)) {
                antecedents.add(o);
            }
        }
        for (PrecursorTester.Occurrence o : sc.consequents()) {
            if (o.time().isBefore(split)) {
                consequents.add(o);
            }
        }
        PrecursorTester.Result discovery = PrecursorTester.test(request(antecedents, consequents, from, split),
                PrecursorRules.MINING_SHIFTS);
        // Everything of the validation window (and beyond) is added to the inputs; the discovery range ends at split.
        PrecursorTester.Result withLater = PrecursorTester.test(request(sc.antecedents(), sc.consequents(), from,
                split), PrecursorRules.MINING_SHIFTS);
        assertThat(withLater).usingRecursiveComparison().withComparatorForType(Double::compare, Double.class).isEqualTo(discovery);

        PrecursorTester.Result validation = PrecursorTester.test(request(sc.antecedents(), sc.consequents(), split,
                sc.rangeEnd()), PrecursorRules.MINING_SHIFTS);
        List<PrecursorTester.Occurrence> beyond = new ArrayList<>(sc.consequents());
        for (int d = 1; d <= 30; d++) {
            beyond.add(occurrence(sc.rangeEnd().plus(Duration.ofDays(d))));
        }
        PrecursorTester.Result validationWithLater = PrecursorTester.test(request(sc.antecedents(), beyond, split,
                sc.rangeEnd()), PrecursorRules.MINING_SHIFTS);
        assertThat(validationWithLater).usingRecursiveComparison().withComparatorForType(Double::compare, Double.class).isEqualTo(validation);
    }

    private static PrecursorTester.Request request(List<PrecursorTester.Occurrence> a,
            List<PrecursorTester.Occurrence> c, Instant from, Instant to) {
        return new PrecursorTester.Request(a, c, Duration.ZERO, Duration.ofDays(3), PrecursorTester.Spatial.none(),
                Duration.ofDays(3), from, to, 0.0, "look-ahead");
    }

    private static PrecursorTester.Occurrence occurrence(Instant t) {
        return new PrecursorTester.Occurrence(t, null, null, null, null, Set.of());
    }

    @Test
    @DisplayName("base rates: counts of the current week and later weeks change nothing")
    void baseRateIgnoresLaterWeeks() {
        LocalDate first = LocalDate.parse("2023-01-02");
        LocalDate end = first.plusWeeks(104);
        Map<LocalDate, Long> weeks = new TreeMap<>();
        for (int w = 0; w < 104; w++) {
            weeks.put(first.plusWeeks(w), (long) (w % 5 == 0 ? 2 : w % 3 == 0 ? 1 : 0));
        }
        BaseRateCalculator.Result before = rate(weeks, first, end);
        Map<LocalDate, Long> withLater = new TreeMap<>(weeks);
        for (int w = 104; w < 130; w++) {
            withLater.put(first.plusWeeks(w), 9L);
        }
        BaseRateCalculator.Result after = rate(withLater, first, end);
        assertThat(after).usingRecursiveComparison().withComparatorForType(Double::compare, Double.class).isEqualTo(before);
        assertThat(before.nWindows()).isPositive();
    }

    private static BaseRateCalculator.Result rate(Map<LocalDate, Long> weeks, LocalDate first, LocalDate end) {
        BaseRateCalculator.Coverage coverage = BaseRateCalculator.Coverage.global(List.<LocalDate[]>of(
                new LocalDate[] {first, end.plusWeeks(40)}));
        return BaseRateCalculator.compute(new BaseRateCalculator.Inputs(List.of("XR"),
                new BaseRateCalculator.Counts(Map.of("XR", Map.of("src", weeks))), Map.of("src", coverage), null, 4,
                end, null, null, 0));
    }

    @Test
    @DisplayName("change points: decisions under late revisions never use a value published after the decision time")
    void changePointsIgnoreLateRevisions() {
        ScenarioMetrics m = new ChangePointBenchmark().run(ScenarioType.LATE_REVISION, new SuiteParams(SEED, 3, 20),
                JobMonitor.NONE);
        assertThat(m.metrics().get("lookAheadViolations")).isZero();
    }
}
