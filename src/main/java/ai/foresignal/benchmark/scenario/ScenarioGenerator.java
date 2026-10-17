package ai.foresignal.benchmark.scenario;

import ai.foresignal.analytics.changepoint.ChangePointService;
import ai.foresignal.analytics.series.SeriesFrequency;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.backtest.PrecursorTester;
import ai.foresignal.evidence.ObservationFacts;
import ai.foresignal.situation.LinkerInput;
import ai.foresignal.situation.SituationMemberTypes;
import ai.foresignal.situation.SituationThemes;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * The deterministic scenario generator {@value ScenarioType#SUITE_VERSION} (prompt 123). Every generator takes the
 * suite seed and a replicate number and draws from its own {@link Rng} stream, so the same seed always gives the same
 * data; every scenario carries its truth record (planted times, sizes, lags, groups), and the forecasting scenarios
 * carry binary questions with known outcomes. Times are fixed (from {@link #EPOCH}), never relative to the clock.
 */
public final class ScenarioGenerator {

    /** The first bucket of every synthetic scenario: Monday 2024-01-01 00:00 UTC. */
    public static final Instant EPOCH = Instant.parse("2024-01-01T00:00:00Z");

    /** The daily history a change-point run reads (119: 400 days). */
    public static final int HISTORY_DAYS = ChangePointService.historyBuckets(SeriesFrequency.DAY);

    /** The evaluated days after the history. */
    public static final int EVAL_DAYS = 84;

    /** The mean daily count of the count scenarios. */
    public static final double BASE_MEAN = 20;

    /** The hourly mean of the anomaly scenarios. */
    public static final double HOURLY_MEAN = 6;

    /** The weeks of hourly history before the anomaly evaluation (100: at most 12 weeks of slot history). */
    public static final int HOURLY_HISTORY_WEEKS = 12;

    /** The evaluated hours of the anomaly scenarios (two weeks). */
    public static final int HOURLY_EVAL = 2 * 168;

    /** The days of a market scenario. */
    public static final int MARKET_DAYS = 600;

    /** The daily return standard deviation of a market scenario. */
    public static final double MARKET_SIGMA = 0.01;

    /** The planted abnormal return before a true event, in standard deviations per day. */
    public static final double PRE_EVENT_SD = 3.0;

    /** The days of a precursor scenario. */
    public static final int PRECURSOR_DAYS = 720;

    /** The planted lag of the true precursor. */
    public static final Duration PLANTED_LAG = Duration.ofHours(48);

    /** The planted hit rate of the true precursor. */
    public static final double PLANTED_HIT_RATE = 0.6;

    /** The echoes of a duplicate-echo scenario. */
    public static final int ECHOES = 10;

    /** The regions of a forecasting scenario. */
    public static final int FORECAST_REGIONS = 30;

    /** The weeks of a forecasting scenario. */
    public static final int FORECAST_WEEKS = 208;

    /** The question horizon in weeks. */
    public static final int HORIZON_WEEKS = 4;

    /** The first week a question opens (one year of history). */
    public static final int FIRST_QUESTION_WEEK = 52;

    /** The week the event rate changes in {@code base-rate-drift}. */
    public static final int DRIFT_WEEK = 104;

    private static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);
    private static final double[] RATES = {0.05, 0.1, 0.2, 0.4, 0.8};

    private ScenarioGenerator() {}

    // ------------------------------------------------------------------------- daily count series

    /**
     * A daily count series scenario.
     *
     * @param type the scenario type
     * @param replicate the replicate number
     * @param series the series (history plus evaluation)
     * @param evalFrom the first evaluated bucket
     * @param truth the truth record
     */
    public record SeriesScenario(ScenarioType type, int replicate, InMemorySeries series, int evalFrom,
            Map<String, Object> truth) {}

    /** The in-control mean of day {@code t}: weekly (30%) and annual (10%) seasonality around {@link #BASE_MEAN}. */
    public static double seasonalMean(int t) {
        return BASE_MEAN * (1 + 0.3 * Math.sin(2 * Math.PI * (t % 7) / 7.0))
                * (1 + 0.1 * Math.sin(2 * Math.PI * t / 365.25));
    }

    /**
     * A daily count scenario: {@code null-seasonal}, {@code step-change}, {@code spike}, {@code coverage-outage} or
     * {@code late-revision}. Values are first published at the bucket's close.
     *
     * @param type the scenario
     * @param seed the suite seed
     * @param replicate the replicate
     * @return the scenario
     */
    public static SeriesScenario dailyCounts(ScenarioType type, long seed, int replicate) {
        Rng rng = Rng.of(seed, type.wire(), "daily", Integer.toString(replicate));
        int n = HISTORY_DAYS + EVAL_DAYS;
        SeriesRef ref = SeriesRef.rollup("day", SeriesRef.ALL_SOURCES, "bench-event", "XB-" + (replicate % 90 + 10));
        InMemorySeries series = new InMemorySeries(ref, SeriesFrequency.DAY, EPOCH, n, "count");
        Map<String, Object> truth = new LinkedHashMap<>();
        truth.put("kind", "daily-count");
        truth.put("baseMean", BASE_MEAN);
        truth.put("seasonality", "weekly 30%, annual 10%");
        truth.put("evalFrom", series.startOf(HISTORY_DAYS).toString());
        double[] mean = new double[n];
        for (int t = 0; t < n; t++) {
            mean[t] = seasonalMean(t);
        }
        int changeIndex = -1;
        int direction = 0;
        double effectSd = 0;
        int spikeIndex = -1;
        int outageFrom = -1;
        int outageTo = -1;
        boolean gapVariant = false;
        int revisedFrom = -1;
        int revisedTo = -1;
        switch (type) {
            case STEP_CHANGE -> {
                changeIndex = HISTORY_DAYS + EVAL_DAYS / 3 + rng.nextInt(7);
                effectSd = replicate % 2 == 0 ? 2.0 : 3.0;
                direction = (replicate / 2) % 2 == 0 ? 1 : -1;
                for (int t = changeIndex; t < n; t++) {
                    mean[t] = Math.max(1, mean[t] + direction * effectSd * Math.sqrt(mean[t]));
                }
                truth.put("changeIndex", changeIndex);
                truth.put("changeAt", series.startOf(changeIndex).toString());
                truth.put("effectSd", effectSd);
                truth.put("direction", direction > 0 ? "up" : "down");
            }
            case SPIKE -> {
                spikeIndex = HISTORY_DAYS + EVAL_DAYS / 2 + rng.nextInt(7);
                mean[spikeIndex] *= 4;
                mean[spikeIndex + 1] *= 4;
                truth.put("spikeIndexes", List.of(spikeIndex, spikeIndex + 1));
                truth.put("spikeAt", series.startOf(spikeIndex).toString());
                truth.put("factor", 4);
            }
            case COVERAGE_OUTAGE -> {
                outageFrom = HISTORY_DAYS + EVAL_DAYS / 3 + rng.nextInt(7);
                outageTo = outageFrom + 10;
                gapVariant = replicate % 2 == 0;
                truth.put("outageFrom", series.startOf(outageFrom).toString());
                truth.put("outageTo", series.startOf(outageTo).toString());
                truth.put("outageFromIndex", outageFrom);
                truth.put("outageToIndex", outageTo);
                truth.put("variant", gapVariant ? "gap" : "low-detectability");
                truth.put("trueRateUnchanged", true);
            }
            case LATE_REVISION -> {
                revisedFrom = HISTORY_DAYS + EVAL_DAYS / 3;
                revisedTo = revisedFrom + 20;
                truth.put("revisedFromIndex", revisedFrom);
                truth.put("revisedToIndex", revisedTo);
                truth.put("revisedFrom", series.startOf(revisedFrom).toString());
                truth.put("revisionFactor", 1.6);
                truth.put("revisionDelayDays", 30);
            }
            default -> {
                // null-seasonal: no signal
            }
        }
        for (int t = 0; t < n; t++) {
            int count = rng.poisson(mean[t]);
            Instant close = series.endOf(t);
            if (t >= outageFrom && t < outageTo) {
                int reported = rng.thin(count, 0.3);
                series.publish(t, reported, close);
                if (gapVariant) {
                    series.coverage(t, 0.3);
                } else {
                    series.detectability(t, 0.3);
                }
            } else {
                series.publish(t, count, close);
            }
            if (t >= revisedFrom && t < revisedTo) {
                series.publish(t, Math.round(count * 1.6), close.plus(Duration.ofDays(30)));
            }
        }
        return new SeriesScenario(type, replicate, series, HISTORY_DAYS, truth);
    }

    // ------------------------------------------------------------------------- hourly counts (anomalies)

    /**
     * An hourly count scenario for the 100 anomaly detector.
     *
     * @param counts the counts per hour
     * @param coverage the coverage ratio per hour
     * @param evalFrom the first evaluated hour
     * @param truth the truth record
     */
    public record HourlyScenario(int[] counts, double[] coverage, int evalFrom, Map<String, Object> truth) {}

    /** The hourly mean of hour {@code h} (diurnal 40%, weekday 15%). */
    public static double hourlyMean(int h) {
        int hourOfDay = h % 24;
        int day = (h / 24) % 7;
        return HOURLY_MEAN * (1 + 0.4 * Math.sin(2 * Math.PI * (hourOfDay - 6) / 24.0))
                * (day >= 5 ? 0.85 : 1.0);
    }

    /**
     * An hourly scenario: {@code null-seasonal}, {@code spike} (three hours at four times the mean) or
     * {@code coverage-outage} (twelve hours at 30% coverage, counts thinned to match).
     *
     * @param type the scenario
     * @param seed the suite seed
     * @param replicate the replicate
     * @return the scenario
     */
    public static HourlyScenario hourlyCounts(ScenarioType type, long seed, int replicate) {
        Rng rng = Rng.of(seed, type.wire(), "hourly", Integer.toString(replicate));
        int evalFrom = HOURLY_HISTORY_WEEKS * 168;
        int n = evalFrom + HOURLY_EVAL;
        int[] counts = new int[n];
        double[] coverage = new double[n];
        Map<String, Object> truth = new LinkedHashMap<>();
        truth.put("kind", "hourly-count");
        truth.put("hourlyMean", HOURLY_MEAN);
        int spike = -1;
        int outage = -1;
        if (type == ScenarioType.SPIKE) {
            spike = evalFrom + 24 + rng.nextInt(HOURLY_EVAL - 48);
            truth.put("spikeFromIndex", spike);
            truth.put("spikeHours", 3);
            truth.put("factor", 4);
        } else if (type == ScenarioType.COVERAGE_OUTAGE) {
            outage = evalFrom + 24 + rng.nextInt(HOURLY_EVAL - 48);
            truth.put("outageFromIndex", outage);
            truth.put("outageHours", 12);
            truth.put("coverage", 0.3);
        }
        for (int h = 0; h < n; h++) {
            double m = hourlyMean(h);
            if (spike >= 0 && h >= spike && h < spike + 3) {
                m *= 4;
            }
            int c = rng.poisson(m);
            coverage[h] = 1.0;
            if (outage >= 0 && h >= outage && h < outage + 12) {
                c = rng.thin(c, 0.3);
                coverage[h] = 0.3;
            }
            counts[h] = c;
        }
        return new HourlyScenario(counts, coverage, evalFrom, truth);
    }

    // ------------------------------------------------------------------------- pre-event moves (markets)

    /**
     * A market scenario for the 119 pre-event check.
     *
     * @param series the daily price series
     * @param trueEvents the planted events with an abnormal move before them
     * @param placeboEvents events without a move
     * @param truth the truth record
     */
    public record MarketScenario(InMemorySeries series, List<Instant> trueEvents, List<Instant> placeboEvents,
            Map<String, Object> truth) {}

    /**
     * {@code pre-event-move}: a log-normal price walk with six planted events, each preceded by an abnormal return of
     * {@value #PRE_EVENT_SD} standard deviations a day over the five days before it, and six placebo events without a
     * move, 25 days apart in the last half.
     *
     * @param seed the suite seed
     * @param replicate the replicate
     * @return the scenario
     */
    public static MarketScenario preEventMove(long seed, int replicate) {
        Rng rng = Rng.of(seed, ScenarioType.PRE_EVENT_MOVE.wire(), Integer.toString(replicate));
        InMemorySeries series = new InMemorySeries(SeriesRef.series("bench-market", "price-" + replicate),
                SeriesFrequency.DAY, EPOCH, MARKET_DAYS, "price");
        List<Integer> slots = new ArrayList<>();
        for (int k = 0; k < 12; k++) {
            slots.add(300 + 25 * k);
        }
        List<Integer> trueIdx = new ArrayList<>();
        List<Integer> placeboIdx = new ArrayList<>();
        List<Integer> pool = new ArrayList<>(slots);
        for (int k = 0; k < 6; k++) {
            trueIdx.add(pool.remove(rng.nextInt(pool.size())));
        }
        placeboIdx.addAll(pool);
        trueIdx.sort(Integer::compareTo);
        placeboIdx.sort(Integer::compareTo);
        boolean[] abnormal = new boolean[MARKET_DAYS];
        for (int e : trueIdx) {
            for (int d = 1; d <= 5; d++) {
                abnormal[e - d] = true;
            }
        }
        double price = 100;
        for (int t = 0; t < MARKET_DAYS; t++) {
            double r = MARKET_SIGMA * rng.normal() + (abnormal[t] ? PRE_EVENT_SD * MARKET_SIGMA : 0);
            price *= Math.exp(r);
            series.publish(t, price, series.endOf(t));
        }
        List<Instant> trueEvents = new ArrayList<>();
        for (int e : trueIdx) {
            trueEvents.add(series.startOf(e).plus(Duration.ofHours(12)));
        }
        List<Instant> placebo = new ArrayList<>();
        for (int e : placeboIdx) {
            placebo.add(series.startOf(e).plus(Duration.ofHours(12)));
        }
        Map<String, Object> truth = new LinkedHashMap<>();
        truth.put("trueEvents", trueEvents.stream().map(Instant::toString).toList());
        truth.put("placeboEvents", placebo.stream().map(Instant::toString).toList());
        truth.put("abnormalSdPerDay", PRE_EVENT_SD);
        truth.put("window", List.of(-5, -1));
        truth.put("sigma", MARKET_SIGMA);
        return new MarketScenario(series, trueEvents, placebo, truth);
    }

    // ------------------------------------------------------------------------- co-occurrence

    /**
     * Daily family counts per region for the 119 co-occurrence miner.
     *
     * @param counts region → family → daily counts (oldest first)
     * @param evalFrom the first mined day
     * @param planted the planted joint spikes as {@code day|region|familyA|familyB} (families alphabetical)
     * @param truth the truth record
     */
    public record CoOccurrenceScenario(Map<String, Map<String, double[]>> counts, int evalFrom, Set<String> planted,
            Map<String, Object> truth) {}

    /**
     * Four regions with four families each over 120 days (weekly seasonality); {@code spike} plants joint threefold
     * spikes of one family pair in one region on four of the last 28 days, {@code null-seasonal} plants nothing.
     *
     * @param type {@code null-seasonal} or {@code spike}
     * @param seed the suite seed
     * @param replicate the replicate
     * @return the scenario
     */
    public static CoOccurrenceScenario coOccurrence(ScenarioType type, long seed, int replicate) {
        Rng rng = Rng.of(seed, type.wire(), "cooccurrence", Integer.toString(replicate));
        int days = 120;
        int evalFrom = days - 28;
        String[] regions = {"XA", "XC", "XD", "XE"};
        String[] families = {"fam-a", "fam-b", "fam-c", "fam-d"};
        Map<String, Map<Integer, String[]>> spikes = new TreeMap<>();
        java.util.Set<String> planted = new java.util.TreeSet<>();
        if (type == ScenarioType.SPIKE) {
            List<Integer> chosen = new ArrayList<>();
            while (chosen.size() < 4) {
                int d = evalFrom + rng.nextInt(28);
                if (!chosen.contains(d)) {
                    chosen.add(d);
                }
            }
            for (int d : chosen) {
                String region = regions[rng.nextInt(regions.length)];
                int i = rng.nextInt(families.length);
                int j = (i + 1 + rng.nextInt(families.length - 1)) % families.length;
                String a = families[Math.min(i, j)];
                String b = families[Math.max(i, j)];
                spikes.computeIfAbsent(region, k -> new TreeMap<>()).put(d, new String[] {a, b});
                planted.add(d + "|" + region + "|" + a + "|" + b);
            }
        }
        Map<String, Map<String, double[]>> counts = new TreeMap<>();
        for (String region : regions) {
            Map<String, double[]> byFamily = new TreeMap<>();
            for (int f = 0; f < families.length; f++) {
                double base = 5 + 3 * f;
                double[] v = new double[days];
                for (int t = 0; t < days; t++) {
                    double m = base * (1 + 0.25 * Math.sin(2 * Math.PI * (t % 7) / 7.0));
                    String[] pair = spikes.getOrDefault(region, Map.of()).get(t);
                    if (pair != null && (pair[0].equals(families[f]) || pair[1].equals(families[f]))) {
                        m *= 3;
                    }
                    v[t] = rng.poisson(m);
                }
                byFamily.put(families[f], v);
            }
            counts.put(region, byFamily);
        }
        Map<String, Object> truth = new LinkedHashMap<>();
        truth.put("regions", List.of(regions));
        truth.put("families", List.of(families));
        truth.put("plantedJointSpikes", List.copyOf(planted));
        truth.put("spikeFactor", type == ScenarioType.SPIKE ? 3 : null);
        return new CoOccurrenceScenario(counts, evalFrom, planted, truth);
    }

    // ------------------------------------------------------------------------- precursors

    /**
     * Event streams for the 103/120 precursor test.
     *
     * @param antecedents the antecedent occurrences
     * @param consequents the consequent occurrences
     * @param decoys independent streams tested in the same batch (each against the consequents)
     * @param rangeStart the start of the range
     * @param rangeEnd the end of the range
     * @param truth the truth record
     */
    public record EventStreamScenario(List<PrecursorTester.Occurrence> antecedents,
            List<PrecursorTester.Occurrence> consequents, List<List<PrecursorTester.Occurrence>> decoys,
            Instant rangeStart, Instant rangeEnd, Map<String, Object> truth) {}

    /**
     * Event streams over {@value #PRECURSOR_DAYS} days: {@code true-precursor} (antecedents at 0.2 a day, each
     * followed with probability {@value #PLANTED_HIT_RATE} by a consequent 48 h ± 6 h later, plus background
     * consequents at 0.1 a day), {@code common-driver} (both streams driven by one annual season, no lag) or
     * {@code null-seasonal} (independent streams). Four independent decoy antecedent streams join the batch.
     *
     * @param type the scenario
     * @param seed the suite seed
     * @param replicate the replicate
     * @return the scenario
     */
    public static EventStreamScenario eventStreams(ScenarioType type, long seed, int replicate) {
        Rng rng = Rng.of(seed, type.wire(), "events", Integer.toString(replicate));
        Instant end = EPOCH.plus(Duration.ofDays(PRECURSOR_DAYS));
        List<PrecursorTester.Occurrence> ant = new ArrayList<>();
        List<PrecursorTester.Occurrence> con = new ArrayList<>();
        Map<String, Object> truth = new LinkedHashMap<>();
        int planted = 0;
        for (int hour = 0; hour < PRECURSOR_DAYS * 24; hour++) {
            double season = type == ScenarioType.COMMON_DRIVER
                    ? 1 + 0.9 * Math.sin(2 * Math.PI * hour / (365.25 * 24)) : 1;
            Instant t = EPOCH.plus(Duration.ofHours(hour)).plus(Duration.ofMinutes(rng.nextInt(60)));
            if (rng.bernoulli(0.2 / 24 * season)) {
                ant.add(occurrence(t));
                if (type == ScenarioType.TRUE_PRECURSOR && rng.bernoulli(PLANTED_HIT_RATE)) {
                    Instant c = t.plus(PLANTED_LAG).plus(Duration.ofMinutes(rng.nextInt(721) - 360));
                    if (c.isBefore(end)) {
                        con.add(occurrence(c));
                        planted++;
                    }
                }
            }
            double background = type == ScenarioType.TRUE_PRECURSOR ? 0.1 : 0.2;
            if (rng.bernoulli(background / 24 * season)) {
                con.add(occurrence(EPOCH.plus(Duration.ofHours(hour)).plus(Duration.ofMinutes(rng.nextInt(60)))));
            }
        }
        con.sort(Comparator.comparing(PrecursorTester.Occurrence::time));
        List<List<PrecursorTester.Occurrence>> decoys = new ArrayList<>();
        for (int d = 0; d < 4; d++) {
            List<PrecursorTester.Occurrence> decoy = new ArrayList<>();
            for (int hour = 0; hour < PRECURSOR_DAYS * 24; hour++) {
                if (rng.bernoulli(0.2 / 24)) {
                    decoy.add(occurrence(EPOCH.plus(Duration.ofHours(hour)).plus(Duration.ofMinutes(rng.nextInt(60)))));
                }
            }
            decoys.add(decoy);
        }
        truth.put("antecedents", ant.size());
        truth.put("consequents", con.size());
        truth.put("decoyStreams", decoys.size());
        switch (type) {
            case TRUE_PRECURSOR -> {
                truth.put("plantedLagHours", PLANTED_LAG.toHours());
                truth.put("lagJitterHours", 6);
                truth.put("plantedHitRate", PLANTED_HIT_RATE);
                truth.put("plantedFollowers", planted);
                truth.put("backgroundPerDay", 0.1);
            }
            case COMMON_DRIVER -> {
                truth.put("driver", "annual season, amplitude 90%");
                truth.put("leadLag", "none");
            }
            default -> truth.put("leadLag", "none");
        }
        return new EventStreamScenario(ant, con, decoys, EPOCH, end, truth);
    }

    private static PrecursorTester.Occurrence occurrence(Instant t) {
        return new PrecursorTester.Occurrence(t, null, null, "XB", null);
    }

    /**
     * Two daily series for the 120 lead–lag test.
     *
     * @param antecedent the candidate leading series (counts, oldest first)
     * @param consequent the candidate following series
     * @param truth the truth record
     */
    public record SeriesPairScenario(double[] antecedent, double[] consequent, Map<String, Object> truth) {}

    /**
     * {@code common-driver}: two count series sharing weekly (30%) and annual (20%) seasonality and a trend, with
     * independent noise and no lead–lag; {@code null-seasonal}: independent series with their own weekly patterns.
     *
     * @param type the scenario
     * @param seed the suite seed
     * @param replicate the replicate
     * @return the scenario
     */
    public static SeriesPairScenario seriesPair(ScenarioType type, long seed, int replicate) {
        Rng rng = Rng.of(seed, type.wire(), "pair", Integer.toString(replicate));
        int n = PRECURSOR_DAYS;
        double[] a = new double[n];
        double[] b = new double[n];
        for (int t = 0; t < n; t++) {
            double weekly = 1 + 0.3 * Math.sin(2 * Math.PI * (t % 7) / 7.0);
            double annual = 1 + 0.2 * Math.sin(2 * Math.PI * t / 365.25);
            double trend = 1 + 0.001 * t;
            if (type == ScenarioType.COMMON_DRIVER) {
                double driver = weekly * annual * trend;
                a[t] = rng.poisson(10 * driver);
                b[t] = rng.poisson(15 * driver);
            } else {
                a[t] = rng.poisson(10 * weekly);
                b[t] = rng.poisson(15 * (1 + 0.3 * Math.cos(2 * Math.PI * (t % 7) / 7.0)));
            }
        }
        Map<String, Object> truth = new LinkedHashMap<>();
        truth.put("days", n);
        truth.put("leadLag", "none");
        truth.put("driver", type == ScenarioType.COMMON_DRIVER ? "shared weekly 30%, annual 20%, trend 0.1%/day"
                : "independent weekly patterns");
        return new SeriesPairScenario(a, b, truth);
    }

    // ------------------------------------------------------------------------- duplicate echoes

    /**
     * One member of an evidence scenario.
     *
     * @param key the member key
     * @param sourceId the source
     * @param citation the cited original source, or null
     * @param signalFamily the signal family it reports under (aggregator copies report as {@code media-report})
     * @param echo true for an echo of another member
     */
    public record EchoMember(String key, String sourceId, String citation, String signalFamily, boolean echo) {}

    /**
     * {@code duplicate-echo} evidence: the original reports of one to three independent originators (the agency
     * first), and ten aggregator re-publications of the agency's report under {@code media-report} (half cite its
     * domain, half come from a wire service that declares it aggregates the agency), so an echo counted as independent
     * would add a family and raise confidence.
     *
     * @param originals the original members
     * @param echoes the echoes
     * @param truth the truth record
     */
    public record EchoScenario(List<EchoMember> originals, List<EchoMember> echoes, Map<String, Object> truth) {}

    /** The synthetic originator registry of the duplicate-echo scenario (the {@code intel/originators.json} shape). */
    public static Map<String, Object> echoRegistryDocument() {
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("version", "bench-1");
        doc.put("originators", List.of(
                Map.of("id", "bench-agency", "class", "agency", "independenceGroup", "bench-agency",
                        "domains", List.of("agency.bench.example")),
                Map.of("id", "bench-sensor", "class", "sensor-network", "independenceGroup", "bench-sensor",
                        "domains", List.of("sensor.bench.example")),
                Map.of("id", "bench-media", "class", "media", "independenceGroup", "bench-media",
                        "domains", List.of("media.bench.example")),
                Map.of("id", "bench-wire", "class", "aggregator", "aggregates", List.of("bench-agency"))));
        Map<String, Object> sources = new LinkedHashMap<>();
        sources.put("bench-src-agency", "bench-agency");
        sources.put("bench-src-sensor", "bench-sensor");
        sources.put("bench-src-media", "bench-media");
        for (int i = 1; i <= ECHOES; i++) {
            if (i % 2 == 0) {
                sources.put("bench-agg-" + i, "bench-wire");
            }
        }
        doc.put("sources", sources);
        return doc;
    }

    /**
     * {@code duplicate-echo} evidence for one replicate.
     *
     * @param seed the suite seed
     * @param replicate the replicate
     * @return the scenario
     */
    public static EchoScenario duplicateEcho(long seed, int replicate) {
        Rng rng = Rng.of(seed, ScenarioType.DUPLICATE_ECHO.wire(), "echo", Integer.toString(replicate));
        String[] sources = {"bench-src-agency", "bench-src-sensor", "bench-src-media"};
        String[] domains = {"agency.bench.example", "sensor.bench.example", "media.bench.example"};
        String[] families = {"official-report", "sensor", "media-report"};
        int k = 1 + replicate % 3;
        int first = 0;
        List<EchoMember> originals = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            originals.add(new EchoMember("orig-" + i, sources[i], null, families[i], false));
        }
        List<EchoMember> echoes = new ArrayList<>();
        String cited = "https://www." + domains[first] + "/report/" + replicate + "-" + rng.nextInt(1_000_000);
        for (int i = 1; i <= ECHOES; i++) {
            echoes.add(new EchoMember("echo-" + i, "bench-agg-" + i, i % 2 == 0 ? null : cited, "media-report",
                    true));
        }
        Map<String, Object> truth = new LinkedHashMap<>();
        truth.put("independentOriginators", k);
        truth.put("echoes", ECHOES);
        truth.put("echoedSource", sources[first]);
        truth.put("expectedGroups", k);
        return new EchoScenario(originals, echoes, truth);
    }

    /**
     * Multi-source canonical events ({@code duplicate-echo}): observations of planted seismic events from two or
     * three sources with time (up to 20 s), distance (up to 15 km) and magnitude (±0.15, to one decimal) jitter
     * inside the bundled seismic tolerances, plus confusable neighbors (the same minute, about 145 km apart) that must
     * stay separate.
     *
     * @param observations the observations in arrival order (seq order)
     * @param truthEvent the planted event of each observation, aligned
     * @param truth the truth record
     */
    public record LinkingScenario(List<ObservationFacts> observations, List<String> truthEvent,
            Map<String, Object> truth) {}

    /**
     * Linking observations for one replicate.
     *
     * @param seed the suite seed
     * @param replicate the replicate
     * @return the scenario
     */
    public static LinkingScenario canonicalEvents(long seed, int replicate) {
        Rng rng = Rng.of(seed, ScenarioType.DUPLICATE_ECHO.wire(), "linking", Integer.toString(replicate));
        String[] sources = {"bench-seis-a", "bench-seis-b", "bench-seis-c"};
        record Planted(String id, Instant at, double lon, double lat, double mag) {}
        List<Planted> events = new ArrayList<>();
        Instant base = EPOCH.plus(Duration.ofDays(replicate));
        for (int i = 0; i < 8; i++) {
            Instant at = base.plus(Duration.ofMinutes(45L * i + rng.nextInt(10)));
            events.add(new Planted("ev-" + i, at, 20 + 5 * rng.uniform(), 35 + 5 * rng.uniform(),
                    3 + 3 * rng.uniform()));
        }
        for (int i = 0; i < 2; i++) {
            Planted p = events.get(i * 3);
            // A confusable neighbor: the same minute, about 150 km east.
            events.add(new Planted("ev-near-" + i, p.at().plusSeconds(10), p.lon() + 1.7, p.lat(), p.mag()));
        }
        record Obs(ObservationFacts facts, String event) {}
        List<Obs> obs = new ArrayList<>();
        for (Planted p : events) {
            int seen = 2 + rng.nextInt(2);
            int offset = rng.nextInt(3);
            for (int s = 0; s < seen; s++) {
                String source = sources[(offset + s) % 3];
                Instant t = p.at().plusSeconds(rng.nextInt(21));
                double bearing = 2 * Math.PI * rng.uniform();
                double km = 15 * rng.uniform();
                double dLat = km / 111.0 * Math.cos(bearing);
                double dLon = km / (111.0 * Math.cos(Math.toRadians(p.lat()))) * Math.sin(bearing);
                double mag = Math.round((p.mag() + 0.3 * (rng.uniform() - 0.5)) * 10) / 10.0;
                ObservationFacts f = new ObservationFacts(source, p.id() + "-" + source, 0, 1, t, t,
                        p.lon() + dLon, p.lat() + dLat, mag, null, null, null, null, null, List.of(), null);
                obs.add(new Obs(f, p.id()));
            }
        }
        obs.sort(Comparator.comparing((Obs o) -> o.facts().observedAt()).thenComparing(o -> o.facts().externalId()));
        List<ObservationFacts> out = new ArrayList<>();
        List<String> truthEvent = new ArrayList<>();
        long seq = 1;
        for (Obs o : obs) {
            ObservationFacts f = o.facts();
            out.add(new ObservationFacts(f.sourceId(), f.externalId(), seq++, f.version(), f.eventTime(),
                    f.observedAt(), f.lon(), f.lat(), f.attribute(), null, null, null, null, null, List.of(), null));
            truthEvent.add(o.event());
        }
        Map<String, Object> truth = new LinkedHashMap<>();
        truth.put("family", "seismic");
        truth.put("events", events.size());
        truth.put("confusablePairs", 2);
        truth.put("observations", out.size());
        return new LinkingScenario(out, truthEvent, truth);
    }

    // ------------------------------------------------------------------------- storylines (situations)

    /**
     * Linker inputs with their planted storyline.
     *
     * @param inputs the inputs in seq order
     * @param truthGroup the planted storyline (or background id) of each input, aligned
     * @param truth the truth record
     */
    public record StorylineScenario(List<LinkerInput> inputs, List<String> truthGroup, Map<String, Object> truth) {}

    /**
     * {@code spike}: five planted storylines (eight related inputs each over three days within 30 km, two of them in
     * the same country but different first-level regions 400 km apart) interleaved with ten unrelated background
     * inputs; {@code null-seasonal}: thirty unrelated inputs only, each in its own region.
     *
     * @param type {@code spike} or {@code null-seasonal}
     * @param seed the suite seed
     * @param replicate the replicate
     * @return the scenario
     */
    public static StorylineScenario storylines(ScenarioType type, long seed, int replicate) {
        Rng rng = Rng.of(seed, type.wire(), "storylines", Integer.toString(replicate));
        record Item(Instant at, double lon, double lat, String country, String admin1, String group) {}
        List<Item> items = new ArrayList<>();
        Instant base = EPOCH.plus(Duration.ofDays(30L * replicate));
        int storylines = 0;
        if (type == ScenarioType.SPIKE) {
            String[][] regions = {{"XA", "XA-01"}, {"XC", "XC-01"}, {"XD", "XD-01"}, {"XE", "XE-01"}, {"XE", "XE-02"}};
            double[][] centers = {{30, 10}, {40, 10}, {50, 10}, {60, 10}, {63.65, 10}};
            storylines = regions.length;
            for (int j = 0; j < regions.length; j++) {
                for (int k = 0; k < 8; k++) {
                    double bearing = 2 * Math.PI * rng.uniform();
                    double km = 30 * rng.uniform();
                    items.add(new Item(base.plus(Duration.ofMinutes(rng.nextInt(72 * 60))),
                            centers[j][0] + km / 111.0 * Math.sin(bearing), centers[j][1] + km / 111.0
                                    * Math.cos(bearing), regions[j][0], regions[j][1], "story-" + j));
                }
            }
        }
        int background = type == ScenarioType.SPIKE ? 10 : 30;
        for (int k = 0; k < background; k++) {
            String country = "Y" + (char) ('A' + k % 26);
            items.add(new Item(base.plus(Duration.ofMinutes(rng.nextInt(72 * 60))), -60 + 4 * k, -20 + 2 * (k % 5),
                    country, country + "-" + String.format("%02d", k), "bg-" + k));
        }
        items.sort(Comparator.comparing(Item::at).thenComparing(Item::group));
        List<LinkerInput> inputs = new ArrayList<>();
        List<String> groups = new ArrayList<>();
        long seq = 1;
        for (Item item : items) {
            String ref = "bench-" + replicate + "-" + seq;
            inputs.add(new LinkerInput(seq, SituationMemberTypes.INTEL_EVENT, ref, "foresignal-intel", ref, null,
                    "intelligence-event", "intel-event", "critical", "Benchmark input " + seq, "Benchmark storyline",
                    item.at(), GEOMETRY.createPoint(new Coordinate(item.lon(), item.lat())),
                    Set.of(item.country(), item.admin1()), item.admin1(), Set.of(), "", SituationThemes.CONFLICT,
                    "table", true, "core", List.of(), null));
            groups.add(item.group());
            seq++;
        }
        Map<String, Object> truth = new LinkedHashMap<>();
        truth.put("storylines", storylines);
        truth.put("inputsPerStoryline", storylines == 0 ? 0 : 8);
        truth.put("backgroundInputs", background);
        truth.put("sameCountryDifferentAdmin1", storylines == 0 ? 0 : 1);
        return new StorylineScenario(inputs, groups, truth);
    }

    // ------------------------------------------------------------------------- forecasting

    /**
     * One binary question with a known outcome.
     *
     * @param region the region
     * @param openWeek the week it opens (history before it may be used)
     * @param outcome true when at least one event happened in the next {@value #HORIZON_WEEKS} weeks
     */
    public record Question(String region, int openWeek, boolean outcome) {}

    /**
     * Weekly event counts per region and the questions about them.
     *
     * @param weekStarts the Monday of each week
     * @param counts region → weekly counts
     * @param questions the questions, ordered by week then region
     * @param truth the truth record
     */
    public record ForecastScenario(LocalDate[] weekStarts, Map<String, long[]> counts, List<Question> questions,
            Map<String, Object> truth) {}

    /**
     * {@code base-rate-drift} (each region's weekly event rate doubles or halves at week {@value #DRIFT_WEEK}) or
     * {@code null-seasonal} (stationary rates): {@value #FORECAST_REGIONS} regions with weekly rates from 0.05 to
     * 0.8 over {@value #FORECAST_WEEKS} weeks; every region gets a question every four weeks from week
     * {@value #FIRST_QUESTION_WEEK}: "at least one event in the next four weeks?".
     *
     * @param type the scenario
     * @param seed the suite seed
     * @param replicate the replicate
     * @return the scenario
     */
    public static ForecastScenario forecasting(ScenarioType type, long seed, int replicate) {
        Rng rng = Rng.of(seed, type.wire(), "forecast", Integer.toString(replicate));
        LocalDate first = LocalDate.ofInstant(EPOCH, ZoneOffset.UTC);
        LocalDate[] weeks = new LocalDate[FORECAST_WEEKS];
        for (int w = 0; w < FORECAST_WEEKS; w++) {
            weeks[w] = first.plusWeeks(w);
        }
        Map<String, long[]> counts = new TreeMap<>();
        Map<String, Object> rates = new LinkedHashMap<>();
        for (int r = 0; r < FORECAST_REGIONS; r++) {
            String region = "XR-" + String.format("%02d", r);
            double rate = RATES[r % RATES.length];
            double after = type == ScenarioType.BASE_RATE_DRIFT ? (r % 2 == 0 ? rate * 2 : rate / 2) : rate;
            long[] c = new long[FORECAST_WEEKS];
            for (int w = 0; w < FORECAST_WEEKS; w++) {
                c[w] = rng.poisson(w < DRIFT_WEEK ? rate : after);
            }
            counts.put(region, c);
            rates.put(region, type == ScenarioType.BASE_RATE_DRIFT ? List.of(rate, after) : List.of(rate));
        }
        List<Question> questions = new ArrayList<>();
        for (int w = FIRST_QUESTION_WEEK; w + HORIZON_WEEKS <= FORECAST_WEEKS; w += HORIZON_WEEKS) {
            for (Map.Entry<String, long[]> e : counts.entrySet()) {
                long sum = 0;
                for (int k = 0; k < HORIZON_WEEKS; k++) {
                    sum += e.getValue()[w + k];
                }
                questions.add(new Question(e.getKey(), w, sum >= 1));
            }
        }
        Map<String, Object> truth = new LinkedHashMap<>();
        truth.put("weeklyRates", rates);
        truth.put("driftWeek", type == ScenarioType.BASE_RATE_DRIFT ? DRIFT_WEEK : null);
        truth.put("horizonWeeks", HORIZON_WEEKS);
        truth.put("questions", questions.size());
        return new ForecastScenario(weeks, counts, questions, truth);
    }
}
