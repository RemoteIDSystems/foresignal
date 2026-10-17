package ai.foresignal.benchmark.component;

import ai.foresignal.analytics.math.Seeds;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.backtest.PrecursorTester;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioMetrics;
import ai.foresignal.benchmark.BenchmarkResults.SuiteParams;
import ai.foresignal.benchmark.BenchmarkScorer;
import ai.foresignal.benchmark.scenario.ScenarioGenerator;
import ai.foresignal.benchmark.scenario.ScenarioType;
import ai.foresignal.precursor.CandidateGenerator;
import ai.foresignal.precursor.Fdr;
import ai.foresignal.precursor.LeadLagCcf;
import ai.foresignal.precursor.PrecursorRules;
import ai.foresignal.precursor.PrecursorSettings;
import ai.foresignal.precursor.Prewhitening;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Benchmarks 120 precursor mining ({@code precursor}) as the miner tests and validates candidates, in memory: the
 * default 365-day lookback ending at the scenario's end, split at the default discovery share (two thirds); event
 * candidates of every stream at every lag of the default grid (1, 3, 7, 14 days) through the 103
 * {@link PrecursorTester} with {@value PrecursorRules#MINING_SHIFTS} shifts and antecedents deduplicated over the lag;
 * series candidates in both directions through {@link Prewhitening} and {@link LeadLagCcf} up to 14 days; one
 * false-discovery adjustment per batch ({@link Fdr}, Benjamini–Yekutieli for the overlapping series pair); survivors
 * validated on the later window by {@link PrecursorRules}.
 *
 * <ul>
 *   <li>{@code null-seasonal}: validated precursors among independent streams and series.</li>
 *   <li>{@code common-driver}: validations of the shared-driver event stream and series pair that carry no
 *       co-occurrence or co-movement flag.</li>
 *   <li>{@code true-precursor}: whether the planted precursor validated, its lag (median within 12 h of 48 h) and
 *       hit rate (the planted rate, raised by chance hits from the background, inside the hit-rate interval)
 *       recovered, and decoy streams validated in the same batch.</li>
 * </ul>
 */
@Component
public class PrecursorBenchmark implements ComponentBenchmark {

    /** The component name. */
    public static final String NAME = "precursor";

    /** The series lags tested (days). */
    public static final int SERIES_MAX_LAG = 14;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String version() {
        return "precursor-tester " + PrecursorTester.VERSION + " + " + LeadLagCcf.NAME + " " + LeadLagCcf.VERSION;
    }

    @Override
    public List<ScenarioType> scenarios() {
        return List.of(ScenarioType.NULL_SEASONAL, ScenarioType.COMMON_DRIVER, ScenarioType.TRUE_PRECURSOR);
    }

    /**
     * The outcome of one event stream in a batch.
     *
     * @param validated true when a lag survived and validated
     * @param flagged true when a validated lag carried the co-occurrence flag
     * @param discovery the discovery result of the best validated lag, or null
     * @param lag the lag window of that result, or null
     */
    record StreamOutcome(boolean validated, boolean flagged, PrecursorTester.Result discovery, Duration lag) {}

    /**
     * The outcome of the series pair.
     *
     * @param forwardValidated the antecedent-leading candidate validated
     * @param forwardFlagged it carried the co-movement flag
     * @param reverseValidated the consequent-leading candidate validated
     * @param reverseFlagged it carried the co-movement flag
     */
    record PairOutcome(boolean forwardValidated, boolean forwardFlagged, boolean reverseValidated,
            boolean reverseFlagged) {}

    @Override
    public ScenarioMetrics run(ScenarioType scenario, SuiteParams params, JobMonitor monitor) {
        int replicates = params.replicates();
        int eventFalse = 0;
        int eventOpportunities = 0;
        int seriesFalse = 0;
        int seriesOpportunities = 0;
        int flaggedShare = 0;
        int recalled = 0;
        int lagRecovered = 0;
        int rateRecovered = 0;
        Map<String, Object> truth = null;
        for (int r = 0; r < replicates; r++) {
            monitor.checkpoint((double) r / replicates);
            ScenarioGenerator.EventStreamScenario sc = ScenarioGenerator.eventStreams(scenario, params.seed(), r);
            if (truth == null) {
                truth = new LinkedHashMap<>(sc.truth());
            }
            List<StreamOutcome> outcomes = eventBatch(sc, scenario.wire() + ":" + r);
            StreamOutcome main = outcomes.get(0);
            switch (scenario) {
                case TRUE_PRECURSOR -> {
                    if (main.validated()) {
                        recalled++;
                        PrecursorTester.Result d = main.discovery();
                        double lagHours = d.lagMedian() / 60.0;
                        if (Math.abs(lagHours - ScenarioGenerator.PLANTED_LAG.toHours()) <= 12) {
                            lagRecovered++;
                        }
                        double effective = effectiveHitRate(main.lag());
                        if (d.hitRateInterval()[0] <= effective && effective <= d.hitRateInterval()[1]) {
                            rateRecovered++;
                        }
                    }
                    for (int k = 1; k < outcomes.size(); k++) {
                        eventOpportunities++;
                        eventFalse += outcomes.get(k).validated() ? 1 : 0;
                    }
                }
                case COMMON_DRIVER -> {
                    eventOpportunities++;
                    if (main.validated() && !main.flagged()) {
                        eventFalse++;
                    }
                    if (main.validated() && main.flagged()) {
                        flaggedShare++;
                    }
                }
                default -> {
                    for (StreamOutcome o : outcomes) {
                        eventOpportunities++;
                        eventFalse += o.validated() ? 1 : 0;
                    }
                }
            }
            if (scenario != ScenarioType.TRUE_PRECURSOR) {
                ScenarioGenerator.SeriesPairScenario pair = ScenarioGenerator.seriesPair(scenario, params.seed(), r);
                if (r == 0) {
                    truth.put("seriesPair", pair.truth());
                }
                PairOutcome p = seriesBatch(pair, scenario.wire() + ":" + r);
                seriesOpportunities += 2;
                boolean common = scenario == ScenarioType.COMMON_DRIVER;
                seriesFalse += p.forwardValidated() && !(common && p.forwardFlagged()) ? 1 : 0;
                seriesFalse += p.reverseValidated() && !(common && p.reverseFlagged()) ? 1 : 0;
                flaggedShare += (p.forwardValidated() && p.forwardFlagged() ? 1 : 0)
                        + (p.reverseValidated() && p.reverseFlagged() ? 1 : 0);
            }
        }
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("eventFalseValidationRate", BenchmarkScorer.rate(eventFalse, eventOpportunities));
        if (scenario == ScenarioType.TRUE_PRECURSOR) {
            m.put("falseValidationRate", BenchmarkScorer.rate(eventFalse, eventOpportunities));
            m.put("recall", BenchmarkScorer.recall(recalled, replicates));
            m.put("lagRecoveredShare", BenchmarkScorer.rate(lagRecovered, replicates));
            m.put("rateRecoveredShare", BenchmarkScorer.rate(rateRecovered, replicates));
        } else {
            m.put("seriesFalseValidationRate", BenchmarkScorer.rate(seriesFalse, seriesOpportunities));
            m.put("falseValidationRate", BenchmarkScorer.rate(eventFalse + seriesFalse,
                    eventOpportunities + seriesOpportunities));
            m.put("flaggedValidations", (double) flaggedShare);
        }
        if (truth != null) {
            truth.put("replicates", replicates);
        }
        return new ScenarioMetrics(scenario.wire(), null, params.seed(), m, truth, List.of("Lookback "
                + PrecursorSettings.DEFAULT_LOOKBACK_DAYS + " days, discovery share 2/3, q = " + Fdr.DEFAULT_Q
                + "; a validation with a co-occurrence or co-movement flag is not counted as false on"
                + " common-driver."));
    }

    /**
     * The expected observed hit rate at lag window {@code lag}: planted hits plus chance hits from every consequent
     * (background and other antecedents' planted followers).
     */
    static double effectiveHitRate(Duration lag) {
        double days = lag.toHours() / 24.0;
        double h = ScenarioGenerator.PLANTED_HIT_RATE;
        double reach = lag.compareTo(ScenarioGenerator.PLANTED_LAG.plusHours(6)) >= 0 ? h : 0;
        double perDay = ScenarioGenerator.PRECURSOR_BACKGROUND_PER_DAY
                + ScenarioGenerator.PRECURSOR_ANTECEDENTS_PER_DAY * h;
        return reach + (1 - reach) * (1 - Math.exp(-perDay * days));
    }

    /** Tests and validates every event stream (the scenario's antecedents first, then the decoys). */
    static List<StreamOutcome> eventBatch(ScenarioGenerator.EventStreamScenario sc, String seedText) {
        Instant end = sc.rangeEnd();
        Instant start = end.minus(Duration.ofDays(PrecursorSettings.DEFAULT_LOOKBACK_DAYS));
        long span = Duration.between(start, end).toSeconds();
        Instant split = start.plusSeconds(Math.round(span * PrecursorSettings.DEFAULT_DISCOVERY_FRACTION));
        List<List<PrecursorTester.Occurrence>> streams = new ArrayList<>();
        streams.add(sc.antecedents());
        streams.addAll(sc.decoys());
        record Test(int stream, Duration lag, PrecursorTester.Result discovery) {}
        List<Test> tests = new ArrayList<>();
        for (int s = 0; s < streams.size(); s++) {
            for (Duration lag : CandidateGenerator.DEFAULT_LAG_GRID) {
                PrecursorTester.Result d = test(streams.get(s), sc.consequents(), lag, start, split,
                        seedText + ":" + s + ":" + lag + ":discovery");
                if (!d.insufficient()) {
                    tests.add(new Test(s, lag, d));
                }
            }
        }
        double[] p = tests.stream().mapToDouble(t -> t.discovery().pValue()).toArray();
        Fdr.Adjusted adjusted = Fdr.adjust(p, false, Fdr.DEFAULT_Q);
        List<StreamOutcome> out = new ArrayList<>();
        for (int s = 0; s < streams.size(); s++) {
            out.add(new StreamOutcome(false, false, null, null));
        }
        double[] bestP = new double[streams.size()];
        Arrays.fill(bestP, Double.POSITIVE_INFINITY);
        for (int k = 0; k < tests.size(); k++) {
            if (!adjusted.rejected()[k]) {
                continue;
            }
            Test t = tests.get(k);
            PrecursorTester.Result v = test(streams.get(t.stream()), sc.consequents(), t.lag(), split, end,
                    seedText + ":" + t.stream() + ":" + t.lag() + ":validation");
            if (PrecursorRules.eventValidationFailure(v) != null) {
                continue;
            }
            boolean flagged = t.discovery().coOccurrence() || v.coOccurrence();
            StreamOutcome current = out.get(t.stream());
            boolean better = adjusted.adjusted()[k] < bestP[t.stream()];
            out.set(t.stream(), new StreamOutcome(true, current.flagged() || flagged,
                    better ? t.discovery() : current.discovery(), better ? t.lag() : current.lag()));
            if (better) {
                bestP[t.stream()] = adjusted.adjusted()[k];
            }
        }
        return out;
    }

    /** One tester run over {@code [from, to)}, antecedents deduplicated over the lag as the miner does. */
    static PrecursorTester.Result test(List<PrecursorTester.Occurrence> antecedents,
            List<PrecursorTester.Occurrence> consequents, Duration lag, Instant from, Instant to, String seedText) {
        List<PrecursorTester.Occurrence> a = new ArrayList<>();
        for (PrecursorTester.Occurrence o : antecedents) {
            if (!o.time().isBefore(from) && o.time().isBefore(to)) {
                a.add(o);
            }
        }
        List<PrecursorTester.Occurrence> c = new ArrayList<>();
        for (PrecursorTester.Occurrence o : consequents) {
            if (!o.time().isBefore(from) && o.time().isBefore(to)) {
                c.add(o);
            }
        }
        return PrecursorTester.test(new PrecursorTester.Request(a, c, Duration.ZERO, lag,
                PrecursorTester.Spatial.none(), lag, from, to, 0.0, seedText), PrecursorRules.MINING_SHIFTS);
    }

    /** Tests and validates the series pair in both directions. */
    static PairOutcome seriesBatch(ScenarioGenerator.SeriesPairScenario pair, String seedText) {
        int n = pair.antecedent().length;
        int start = Math.max(0, n - PrecursorSettings.DEFAULT_LOOKBACK_DAYS);
        int split = start + (int) Math.round((n - start) * PrecursorSettings.DEFAULT_DISCOVERY_FRACTION);
        LeadLagCcf.Result fwd = lagTest(pair.antecedent(), pair.consequent(), start, split, seedText + ":fwd:d");
        LeadLagCcf.Result rev = lagTest(pair.consequent(), pair.antecedent(), start, split, seedText + ":rev:d");
        List<LeadLagCcf.Result> discovered = new ArrayList<>();
        List<Integer> direction = new ArrayList<>();
        if (!fwd.insufficient()) {
            discovered.add(fwd);
            direction.add(0);
        }
        if (!rev.insufficient()) {
            discovered.add(rev);
            direction.add(1);
        }
        double[] p = discovered.stream().mapToDouble(x -> x.forward().pValue()).toArray();
        Fdr.Adjusted adjusted = Fdr.adjust(p, true, Fdr.DEFAULT_Q);
        boolean[] validated = new boolean[2];
        boolean[] flagged = new boolean[2];
        for (int k = 0; k < discovered.size(); k++) {
            if (!adjusted.rejected()[k]) {
                continue;
            }
            int dir = direction.get(k);
            double[] a = dir == 0 ? pair.antecedent() : pair.consequent();
            double[] b = dir == 0 ? pair.consequent() : pair.antecedent();
            LeadLagCcf.Result v = lagTest(a, b, split, n, seedText + ":" + dir + ":v");
            if (PrecursorRules.seriesValidationFailure(discovered.get(k), v) == null) {
                validated[dir] = true;
                flagged[dir] = discovered.get(k).coMovement() || v.coMovement();
            }
        }
        return new PairOutcome(validated[0], flagged[0], validated[1], flagged[1]);
    }

    /** Pre-whitens the slice {@code [from, to)} of both series and runs the lead–lag test. */
    static LeadLagCcf.Result lagTest(double[] a, double[] b, int from, int to, String seedText) {
        double[] ra = Prewhitening.residuals(Arrays.copyOfRange(a, from, to), true, 7);
        double[] rb = Prewhitening.residuals(Arrays.copyOfRange(b, from, to), true, 7);
        return LeadLagCcf.test(ra, rb, SERIES_MAX_LAG, Seeds.of(seedText));
    }
}
