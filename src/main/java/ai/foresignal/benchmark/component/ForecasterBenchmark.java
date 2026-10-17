package ai.foresignal.benchmark.component;

import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.backtest.PrecursorTester;
import ai.foresignal.baserate.BaseRateCalculator;
import ai.foresignal.baserate.analog.AnalogSearch;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioMetrics;
import ai.foresignal.benchmark.BenchmarkResults.SuiteParams;
import ai.foresignal.benchmark.BenchmarkScorer;
import ai.foresignal.benchmark.scenario.ScenarioGenerator;
import ai.foresignal.benchmark.scenario.ScenarioType;
import ai.foresignal.forecast.ensemble.EnsembleMath;
import ai.foresignal.precursor.PrecursorRules;
import ai.foresignal.precursor.PrecursorSettings;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Benchmarks one forecaster on binary questions with known outcomes (prompt 123): Brier, log score, the largest
 * reliability deviation over populated bins, the CORP miscalibration and Brier skill against an honest naive
 * reference, pooled over replicates. Every forecast uses only history before the question opens.
 *
 * <ul>
 *   <li>{@code forecaster:base-rate}: the 121 {@link BaseRateCalculator} over the region's weekly counts before the
 *       question, four-week windows ({@code P28D}, the question's horizon); reference: pooled climatology (the share
 *       of four-week windows with an event over every region before the question).</li>
 *   <li>{@code forecaster:analog}: past four-week windows of the region whose preceding four weeks had the same count
 *       (capped at 3) are the analogs (the 20 most recent), their outcome frequency shrunk toward the base rate by the
 *       121 {@link AnalogSearch#distribution}; reference: the base-rate forecaster.</li>
 *   <li>{@code forecaster:ensemble}: the 122 log-odds pool ({@link EnsembleMath#combineBinary}) of the base-rate and
 *       analog forecasts with equal weights; reference: the base-rate forecaster.</li>
 *   <li>{@code forecaster:precursor}: on {@code true-precursor}, after every antecedent of the validation window,
 *       "a consequent within three days?" at the 120 {@link PrecursorRules#probability} of the discovery window's
 *       hits; reference: the discovery window's circular-shift base rate.</li>
 * </ul>
 */
public class ForecasterBenchmark implements ComponentBenchmark {

    /** The benchmarked forecasters. */
    public enum Kind {
        /** The base-rate forecaster. */
        BASE_RATE("forecaster:base-rate", BaseRateCalculator.NAME + " " + BaseRateCalculator.VERSION),
        /** The analog forecaster. */
        ANALOG("forecaster:analog", AnalogSearch.NAME + " " + AnalogSearch.VERSION),
        /** The precursor forecaster. */
        PRECURSOR("forecaster:precursor", "precursor-probability 1.0.0"),
        /** The ensemble. */
        ENSEMBLE("forecaster:ensemble", "ensemble-logodds 1.0.0");

        private final String component;
        private final String version;

        Kind(String component, String version) {
            this.component = component;
            this.version = version;
        }
    }

    /** The precursor question window. */
    static final Duration PRECURSOR_WINDOW = Duration.ofDays(3);

    private static final String SOURCE = "bench";

    private final Kind kind;

    /** The benchmark of one forecaster. */
    public ForecasterBenchmark(Kind kind) {
        this.kind = kind;
    }

    @Override
    public String name() {
        return kind.component;
    }

    @Override
    public String version() {
        return kind.version;
    }

    @Override
    public List<ScenarioType> scenarios() {
        return kind == Kind.PRECURSOR ? List.of(ScenarioType.TRUE_PRECURSOR)
                : List.of(ScenarioType.NULL_SEASONAL, ScenarioType.BASE_RATE_DRIFT);
    }

    /** Pooled forecasts, outcomes and reference forecasts. */
    static final class Pool {
        final List<Double> p = new ArrayList<>();
        final List<Boolean> o = new ArrayList<>();
        final List<Double> ref = new ArrayList<>();

        void add(double forecast, boolean outcome, double reference) {
            p.add(forecast);
            o.add(outcome);
            ref.add(reference);
        }

        double[] forecasts() {
            return p.stream().mapToDouble(Double::doubleValue).toArray();
        }

        double[] references() {
            return ref.stream().mapToDouble(Double::doubleValue).toArray();
        }

        boolean[] outcomes() {
            boolean[] out = new boolean[o.size()];
            for (int i = 0; i < out.length; i++) {
                out[i] = o.get(i);
            }
            return out;
        }
    }

    @Override
    public ScenarioMetrics run(ScenarioType scenario, SuiteParams params, JobMonitor monitor) {
        int replicates = params.replicates();
        Pool pool = new Pool();
        Map<String, Object> truth = null;
        for (int r = 0; r < replicates; r++) {
            monitor.checkpoint((double) r / replicates);
            if (kind == Kind.PRECURSOR) {
                ScenarioGenerator.EventStreamScenario sc = ScenarioGenerator.eventStreams(scenario, params.seed(), r);
                if (truth == null) {
                    truth = new LinkedHashMap<>(sc.truth());
                }
                precursorQuestions(sc, pool, scenario.wire() + ":" + r);
            } else {
                ScenarioGenerator.ForecastScenario sc = ScenarioGenerator.forecasting(scenario, params.seed(), r);
                if (truth == null) {
                    truth = new LinkedHashMap<>(sc.truth());
                }
                for (ScenarioGenerator.Question q : sc.questions()) {
                    double base = baseRate(sc, q.region(), q.openWeek());
                    double forecast;
                    double reference;
                    switch (kind) {
                        case ANALOG -> {
                            forecast = analog(sc, q.region(), q.openWeek(), base);
                            reference = base;
                        }
                        case ENSEMBLE -> {
                            double analog = analog(sc, q.region(), q.openWeek(), base);
                            forecast = EnsembleMath.combineBinary(new double[] {base, analog},
                                    new double[] {0.5, 0.5}, 1.0);
                            reference = base;
                        }
                        default -> {
                            forecast = base;
                            reference = climatology(sc, q.openWeek());
                        }
                    }
                    pool.add(forecast, q.outcome(), reference);
                }
            }
        }
        double[] p = pool.forecasts();
        boolean[] o = pool.outcomes();
        double brier = BenchmarkScorer.brier(p, o);
        double brierRef = BenchmarkScorer.brier(pool.references(), o);
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("questions", (double) p.length);
        m.put("brier", brier);
        m.put("brierReference", brierRef);
        m.put("skill", BenchmarkScorer.skill(brier, brierRef));
        m.put("logScore", BenchmarkScorer.logScore(p, o));
        m.put("reliabilityMaxDeviation", BenchmarkScorer.reliabilityMaxDeviation(p, o, params.minBinCount()));
        m.put("populatedBins", (double) BenchmarkScorer.populatedBins(p, o, params.minBinCount()));
        m.put("corpMiscalibration", BenchmarkScorer.corpMiscalibration(p, o));
        m.put("meanForecast", mean(p));
        m.put("observedFrequency", mean(BenchmarkScorer.toDoubles(o)));
        if (truth != null) {
            truth.put("replicates", replicates);
        }
        String reference = kind == Kind.BASE_RATE ? "pooled climatology" : kind == Kind.PRECURSOR
                ? "the circular-shift base rate" : "the base-rate forecaster";
        return new ScenarioMetrics(scenario.wire(), null, params.seed(), m, truth, List.of("Skill is Brier skill"
                + " against " + reference + "; a bin counts as populated with at least " + params.minBinCount()
                + " forecasts."));
    }

    /** The base-rate forecast of a region at a week: the 121 calculator over the counts before it, 4-week windows. */
    static double baseRate(ScenarioGenerator.ForecastScenario sc, String region, int openWeek) {
        LocalDate[] weeks = sc.weekStarts();
        long[] counts = sc.counts().get(region);
        Map<LocalDate, Long> byWeek = new TreeMap<>();
        for (int w = 0; w < openWeek; w++) {
            byWeek.put(weeks[w], counts[w]);
        }
        Map<String, Map<String, Map<LocalDate, Long>>> all = Map.of(region, Map.of(SOURCE, byWeek));
        BaseRateCalculator.Coverage coverage = BaseRateCalculator.Coverage.global(List.<LocalDate[]>of(
                new LocalDate[] {weeks[0], weeks[openWeek]}));
        BaseRateCalculator.Result result = BaseRateCalculator.compute(new BaseRateCalculator.Inputs(List.of(region),
                new BaseRateCalculator.Counts(all), Map.of(SOURCE, coverage), null, ScenarioGenerator.HORIZON_WEEKS,
                weeks[openWeek], null, null, 0));
        return Double.isFinite(result.pAtLeastOne()) ? result.pAtLeastOne() : 0.5;
    }

    /** Pooled climatology: the share of complete four-week tiles before {@code openWeek} with an event, all regions. */
    static double climatology(ScenarioGenerator.ForecastScenario sc, int openWeek) {
        int tiles = 0;
        int hits = 0;
        int h = ScenarioGenerator.HORIZON_WEEKS;
        for (long[] counts : sc.counts().values()) {
            for (int t = 0; t + h <= openWeek; t += h) {
                long sum = 0;
                for (int k = 0; k < h; k++) {
                    sum += counts[t + k];
                }
                tiles++;
                hits += sum >= 1 ? 1 : 0;
            }
        }
        return tiles == 0 ? 0.5 : (double) hits / tiles;
    }

    /** The analog forecast: same-signature past windows of the region, shrunk toward the base rate. */
    static double analog(ScenarioGenerator.ForecastScenario sc, String region, int openWeek, double base) {
        long[] counts = sc.counts().get(region);
        int h = ScenarioGenerator.HORIZON_WEEKS;
        long signature = Math.min(3, sum(counts, openWeek - h, openWeek));
        int analogs = 0;
        int hits = 0;
        for (int t = openWeek - h; t >= h && analogs < 20; t--) {
            if (Math.min(3, sum(counts, t - h, t)) != signature) {
                continue;
            }
            analogs++;
            hits += sum(counts, t, t + h) >= 1 ? 1 : 0;
        }
        return AnalogSearch.distribution(hits, analogs, base).probability();
    }

    private static long sum(long[] counts, int from, int to) {
        long out = 0;
        for (int i = Math.max(0, from); i < Math.min(counts.length, to); i++) {
            out += counts[i];
        }
        return out;
    }

    /** The precursor forecaster's questions on one replicate. */
    static void precursorQuestions(ScenarioGenerator.EventStreamScenario sc, Pool pool, String seedText) {
        Instant end = sc.rangeEnd();
        Instant start = end.minus(Duration.ofDays(PrecursorSettings.DEFAULT_LOOKBACK_DAYS));
        long span = Duration.between(start, end).toSeconds();
        Instant split = start.plusSeconds(Math.round(span * PrecursorSettings.DEFAULT_DISCOVERY_FRACTION));
        PrecursorTester.Result d = PrecursorBenchmark.test(sc.antecedents(), sc.consequents(), PRECURSOR_WINDOW, start,
                split, seedText + ":forecaster");
        double probability = PrecursorRules.probability(d.hits(), d.nAntecedent(), d.baseRate());
        double reference = Double.isFinite(d.baseRate()) ? d.baseRate() : 0.5;
        for (PrecursorTester.Occurrence a : sc.antecedents()) {
            if (a.time().isBefore(split) || !a.time().plus(PRECURSOR_WINDOW).isBefore(end)) {
                continue;
            }
            boolean outcome = false;
            for (PrecursorTester.Occurrence c : sc.consequents()) {
                if (c.time().isAfter(a.time()) && !c.time().isAfter(a.time().plus(PRECURSOR_WINDOW))) {
                    outcome = true;
                    break;
                }
            }
            pool.add(probability, outcome, reference);
        }
    }

    private static double mean(double[] values) {
        if (values.length == 0) {
            return Double.NaN;
        }
        double s = 0;
        for (double v : values) {
            s += v;
        }
        return s / values.length;
    }
}
