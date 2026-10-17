package ai.foresignal.benchmark.component;

import ai.foresignal.analytics.eventstudy.EventStudyEngine;
import ai.foresignal.analytics.eventstudy.LiveEventStudyService;
import ai.foresignal.analytics.math.PlaceboSampler;
import ai.foresignal.analytics.math.Seeds;
import ai.foresignal.analytics.registry.WatchPair;
import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesFrequency;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioMetrics;
import ai.foresignal.benchmark.BenchmarkResults.SuiteParams;
import ai.foresignal.benchmark.BenchmarkScorer;
import ai.foresignal.benchmark.scenario.InMemorySeries;
import ai.foresignal.benchmark.scenario.ScenarioGenerator;
import ai.foresignal.benchmark.scenario.ScenarioType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Benchmarks the 119 event-study engine's live pre-event check ({@code event-study}) on {@code pre-event-move}: for
 * every planted and placebo event the price series is read as of the event time, the engine runs with the daily
 * windows, the event's other qualifying times excluded from placebo dates and 500 placebo dates, and a
 * {@code pre-event-signal} is found as {@link LiveEventStudyService} finds one: placebo p at most
 * {@value LiveEventStudyService#PLACEBO_P} and {@code |CAR| ≥ 2}.
 *
 * <p>Metrics: recall on planted events, the false-signal rate on placebo events, and the mean placebo p of each kind
 * (planted events should be near 0, placebo events near 0.5), for the live check as it runs and ({@code engine*}) for
 * the same engine and rule over all history known by the event time.
 */
@Component
public class EventStudyBenchmark implements ComponentBenchmark {

    /** The component name. */
    public static final String NAME = "event-study";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String version() {
        return EventStudyEngine.NAME + " " + EventStudyEngine.VERSION;
    }

    @Override
    public List<ScenarioType> scenarios() {
        return List.of(ScenarioType.PRE_EVENT_MOVE);
    }

    @Override
    public ScenarioMetrics run(ScenarioType scenario, SuiteParams params, JobMonitor monitor) {
        int replicates = params.replicates();
        Tally live = new Tally();
        Tally engine = new Tally();
        Map<String, Object> truth = null;
        for (int r = 0; r < replicates; r++) {
            monitor.checkpoint((double) r / replicates);
            ScenarioGenerator.MarketScenario sc = ScenarioGenerator.preEventMove(params.seed(), r);
            if (truth == null) {
                truth = new LinkedHashMap<>(sc.truth());
            }
            List<Instant> all = new ArrayList<>(sc.trueEvents());
            all.addAll(sc.placeboEvents());
            for (Instant event : sc.trueEvents()) {
                live.add(true, check(sc.series(), event, all, r, true));
                engine.add(true, check(sc.series(), event, all, r, false));
            }
            for (Instant event : sc.placeboEvents()) {
                live.add(false, check(sc.series(), event, all, r, true));
                engine.add(false, check(sc.series(), event, all, r, false));
            }
        }
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("recall", BenchmarkScorer.recall(live.trueFound, live.trueTotal));
        m.put("placeboFalseSignalRate", BenchmarkScorer.rate(live.placeboFound, live.placeboTotal));
        m.put("meanPlaceboPOnTrue", mean(live.trueP));
        m.put("meanPlaceboPOnPlacebo", mean(live.placeboP));
        m.put("engineRecall", BenchmarkScorer.recall(engine.trueFound, engine.trueTotal));
        m.put("enginePlaceboFalseSignalRate", BenchmarkScorer.rate(engine.placeboFound, engine.placeboTotal));
        m.put("engineMeanPlaceboPOnTrue", mean(engine.trueP));
        m.put("engineMeanPlaceboPOnPlacebo", mean(engine.placeboP));
        m.put("plantedEvents", (double) live.trueTotal);
        m.put("placeboEvents", (double) live.placeboTotal);
        if (truth != null) {
            truth.put("replicates", replicates);
        }
        List<String> notes = new ArrayList<>();
        notes.add("recall, placeboFalseSignalRate and meanPlaceboP*: the live pre-event check exactly as"
                + " LiveEventStudyService runs it (252 days read as of the event time).");
        notes.add("engine*: the same engine and decision rule over all history known by the event time, as an"
                + " on-demand event study reads it.");
        return new ScenarioMetrics(scenario.wire(), null, params.seed(), m, truth, notes);
    }

    /** Counts of one variant. */
    private static final class Tally {
        int trueTotal;
        int trueFound;
        int placeboTotal;
        int placeboFound;
        final List<Double> trueP = new ArrayList<>();
        final List<Double> placeboP = new ArrayList<>();

        void add(boolean planted, Signal s) {
            if (planted) {
                trueTotal++;
                trueFound += s.found() ? 1 : 0;
                trueP.add(s.placeboP());
            } else {
                placeboTotal++;
                placeboFound += s.found() ? 1 : 0;
                placeboP.add(s.placeboP());
            }
        }
    }

    /**
     * One event's outcome.
     *
     * @param found true when a pre-event signal would be written
     * @param placeboP the placebo p of the pre-event window ({@code NaN} when excluded)
     */
    record Signal(boolean found, double placeboP) {}

    /**
     * The pre-event check of one event.
     *
     * @param liveRange true to read what the live check reads ({@code estFrom − 2} days before the event), false to
     *     read all history known by the event time
     */
    static Signal check(InMemorySeries series, Instant event, List<Instant> allEvents, int replicate,
            boolean liveRange) {
        EventStudyEngine.Windows w = EventStudyEngine.Windows.of(SeriesFrequency.DAY);
        int t0 = series.indexOf(event);
        SeriesData data = series.read(liveRange ? t0 + w.estFrom() - 2 : 0, t0 + 1, event);
        SeriesData.Grid grid = data.grid();
        List<Instant> others = new ArrayList<>();
        for (Instant e : allEvents) {
            if (e.isBefore(event)) {
                others.add(e);
            }
        }
        EventStudyEngine.Transform transform = EventStudyEngine.Transform.of(false, null, "price");
        EventStudyEngine.Result result = EventStudyEngine.run(new EventStudyEngine.Input(grid.starts(),
                grid.values(), SeriesFrequency.DAY, transform, w, List.of(new EventStudyEngine.Event(event,
                        "bench-event:" + replicate + ":" + event)), others,
                Seeds.of("pre-event", series.ref().format(), event.toString()), PlaceboSampler.DEFAULT_COUNT, false));
        EventStudyEngine.EventOutcome outcome = result.events().get(0);
        if (outcome.excluded() != null) {
            return new Signal(false, Double.NaN);
        }
        double p = result.pre().placeboP();
        boolean found = p <= LiveEventStudyService.PLACEBO_P
                && Math.abs(outcome.carPre()) >= WatchPair.DEFAULT_MIN_EFFECT;
        return new Signal(found, p);
    }

    private static double mean(List<Double> values) {
        double sum = 0;
        int n = 0;
        for (double v : values) {
            if (Double.isFinite(v)) {
                sum += v;
                n++;
            }
        }
        return n == 0 ? Double.NaN : sum / n;
    }
}
