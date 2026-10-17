package ai.foresignal.benchmark;

import ai.foresignal.analytics.AnalyticsSupport;
import ai.foresignal.analytics.changepoint.ChangePointDetector;
import ai.foresignal.analytics.changepoint.ChangePointService;
import ai.foresignal.analytics.math.CusumThresholds;
import ai.foresignal.analytics.math.Seeds;
import ai.foresignal.analytics.registry.AnalysisSeriesEntity;
import ai.foresignal.analytics.registry.AnalysisSeriesRepository;
import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesFrequency;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.baserate.BaseRateDefinition;
import ai.foresignal.baserate.BaseRateService;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioMetrics;
import ai.foresignal.benchmark.component.ChangePointBenchmark;
import ai.foresignal.benchmark.scenario.Rng;
import ai.foresignal.benchmark.scenario.ScenarioType;
import ai.foresignal.precursor.PrecursorEntity;
import ai.foresignal.precursor.PrecursorRepository;
import ai.foresignal.precursor.PrecursorRules;
import ai.foresignal.security.Ulids;
import ai.foresignal.stats.RegionRollupComputer;
import ai.foresignal.stats.RegionRollupResolution;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * The historical corpus (prompt 123): replays {@code benchmark_case} rows without look-ahead and samples null
 * region-weeks.
 *
 * <ul>
 *   <li><b>Findings</b>: {@code change-point} expectations replay the 119 detector over the case's series (its
 *       {@code seriesRef}, else the enabled registry rows of its regions, at most {@value #MAX_SERIES}) with
 *       {@code asOf} at the window start stepping forward a day at a time (at most {@value #MAX_DAYS} days), each step
 *       deciding at 03:00 UTC on the closed days before it as the nightly run does: every read is the
 *       {@link SeriesReader} as of the step, filtered by {@code knownAt}, so later revisions and late-arriving rows
 *       are never seen. Expected findings that never appear are misses; findings expected absent
 *       that appear (any confirmed change in an auto null case) are unexpected. Other finding types are not
 *       replayable yet and are listed as skipped.</li>
 *   <li><b>Precursors</b>: the named precursor's lifecycle status against {@code validated} / {@code not-validated}
 *       (its current status; precursor status is not time-versioned).</li>
 *   <li><b>Questions</b>: binary questions that carry a {@code baseRate} definition are forecast by the 121
 *       calculator as of the window start and scored (Brier).</li>
 *   <li><b>Auto null cases</b>: monthly, 20 region-weeks of the past year with region rollup rows, no intel event at
 *       warning or above and no situation activity, drawn deterministically from the month.</li>
 * </ul>
 *
 * <p>Nothing is written but {@code benchmark_case} rows by the sampler (and, for questions, the 121 calculator's
 * manifest).
 */
@Service
public class HistoricalSuite {

    private static final Logger log = LoggerFactory.getLogger(HistoricalSuite.class);

    /** The most series one expectation replays. */
    public static final int MAX_SERIES = 20;

    /** The most days one case replays. */
    public static final int MAX_DAYS = 92;

    /** A replay step decides at 03:00 UTC on the closed days before it, as the nightly change-point run does. */
    public static final Duration NIGHTLY_OFFSET = Duration.ofHours(3);

    /** The null region-weeks sampled per month. */
    public static final int NULL_CASES_PER_MONTH = 20;

    private final BenchmarkCaseRepository cases;
    private final SeriesReader reader;
    private final AnalysisSeriesRepository registry;
    private final AnalyticsSupport support;
    private final ObjectProvider<PrecursorRepository> precursors;
    private final ObjectProvider<BaseRateService> baseRates;
    private final NamedParameterJdbcTemplate jdbc;

    /** Creates the suite. */
    public HistoricalSuite(BenchmarkCaseRepository cases, SeriesReader reader, AnalysisSeriesRepository registry,
            AnalyticsSupport support, ObjectProvider<PrecursorRepository> precursors,
            ObjectProvider<BaseRateService> baseRates, NamedParameterJdbcTemplate jdbc) {
        this.cases = cases;
        this.reader = reader;
        this.registry = registry;
        this.support = support;
        this.precursors = precursors;
        this.baseRates = baseRates;
        this.jdbc = jdbc;
    }

    /**
     * Replays every case for the selected components.
     *
     * @param components the selected components
     * @param monitor cancellation and progress
     * @param from the progress at the start
     * @param to the progress at the end
     * @return per component, one metrics entry per replayed case
     */
    public Map<String, List<ScenarioMetrics>> run(Set<String> components, JobMonitor monitor, double from, double to) {
        Map<String, List<ScenarioMetrics>> out = new LinkedHashMap<>();
        List<BenchmarkCaseEntity> all = cases.findAllByOrderByIdAsc();
        CusumThresholds thresholds = new CusumThresholds();
        for (int i = 0; i < all.size(); i++) {
            monitor.checkpoint(from + (to - from) * i / Math.max(1, all.size()));
            BenchmarkCaseEntity c = all.get(i);
            if (components.contains(ChangePointBenchmark.NAME) && !list(c.getExpectations().get("findings"))
                    .isEmpty()) {
                out.computeIfAbsent(ChangePointBenchmark.NAME, k -> new ArrayList<>()).add(findings(c, thresholds));
            }
            if (components.contains("precursor") && !list(c.getExpectations().get("precursors")).isEmpty()) {
                out.computeIfAbsent("precursor", k -> new ArrayList<>()).add(precursors(c));
            }
            if (components.contains("forecaster:base-rate") && !list(c.getExpectations().get("questions"))
                    .isEmpty()) {
                out.computeIfAbsent("forecaster:base-rate", k -> new ArrayList<>()).add(questions(c));
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------- findings

    /** Replays a case's finding expectations. */
    ScenarioMetrics findings(BenchmarkCaseEntity c, CusumThresholds thresholds) {
        List<String> notes = new ArrayList<>();
        int expectations = 0;
        int skipped = 0;
        int missed = 0;
        int unexpected = 0;
        int series = 0;
        int decisions = 0;
        int violations = 0;
        ChangePointDetector.Params params = ChangePointService.params(SeriesFrequency.DAY);
        Instant start = SeriesFrequency.DAY.floor(c.getWindowFrom());
        Instant end = c.getWindowTo();
        if (Duration.between(start, end).toDays() > MAX_DAYS) {
            end = start.plus(Duration.ofDays(MAX_DAYS));
            notes.add("The window was replayed for its first " + MAX_DAYS + " days only.");
        }
        for (Object item : list(c.getExpectations().get("findings"))) {
            if (!(item instanceof Map<?, ?> f)) {
                continue;
            }
            expectations++;
            String type = String.valueOf(f.get("findingType"));
            if (!ChangePointBenchmark.NAME.equals(type)) {
                skipped++;
                notes.add("Finding type " + type + " is not replayable yet; skipped.");
                continue;
            }
            boolean present = Boolean.TRUE.equals(f.get("present"));
            Instant from = f.get("from") == null ? c.getWindowFrom() : Instant.parse(String.valueOf(f.get("from")));
            Instant to = f.get("to") == null ? c.getWindowTo() : Instant.parse(String.valueOf(f.get("to")));
            boolean significantOnly = "significant".equals(f.get("minSeverity"));
            List<SeriesRef> refs = refs(f, c);
            boolean found = false;
            for (SeriesRef ref : refs) {
                series++;
                boolean bocpd = registry.findById(ref.format()).map(r -> r.getDetectors()
                        .contains(ChangePointDetector.BOCPD)).orElse(true);
                for (Instant t = start; !t.isAfter(end); t = t.plus(Duration.ofDays(1))) {
                    decisions++;
                    Instant asOf = t.plus(NIGHTLY_OFFSET);
                    SeriesData data;
                    try {
                        data = reader.read(ref, SeriesFrequency.DAY.plus(t, -ChangePointService.historyBuckets(
                                SeriesFrequency.DAY)), t, asOf);
                    } catch (RuntimeException e) {
                        notes.add("Series " + ref.format() + " could not be read: " + e.getMessage());
                        break;
                    }
                    if (data.frequency() != SeriesFrequency.DAY) {
                        data = data.resample(SeriesFrequency.DAY);
                    }
                    SeriesData.Grid grid = data.grid().knownBy(asOf);
                    violations += ChangePointBenchmark.lateValues(grid, asOf);
                    ChangePointDetector.Result r = ChangePointDetector.detect(grid.starts(), grid.values(),
                            grid.gap(), data.counts(), params, thresholds);
                    final SeriesData read = data;
                    final Instant decision = asOf;
                    ChangePointService.Classification k = ChangePointService.classify(r, data.counts(), bocpd,
                            params.confirmPosterior(), () -> support.canSeeDecline(read, r.changeAt(),
                                    SeriesFrequency.DAY.next(SeriesFrequency.DAY.floor(decision))));
                    if (k == null || !k.confirmed() || r.changeAt() == null) {
                        continue;
                    }
                    if (significantOnly && !"significant".equals(k.severity())) {
                        continue;
                    }
                    if (!r.changeAt().isBefore(from) && r.changeAt().isBefore(to)) {
                        found = true;
                    }
                }
            }
            if (refs.isEmpty()) {
                notes.add("No series to replay for a " + type + " expectation (no seriesRef and no registry row in"
                        + " the case's regions).");
                skipped++;
                continue;
            }
            if (present && !found) {
                missed++;
            } else if (!present && found) {
                unexpected++;
            }
        }
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("expectations", (double) expectations);
        m.put("skippedExpectations", (double) skipped);
        m.put("missedExpectations", (double) missed);
        m.put("unexpectedFindings", (double) unexpected);
        m.put("seriesReplayed", (double) series);
        m.put("decisions", (double) decisions);
        m.put("lookAheadViolations", (double) violations);
        notes.add(0, "Replayed with asOf stepping a day at a time from " + start + ".");
        return new ScenarioMetrics(ScenarioType.HISTORICAL.wire(), c.getId(), null, m, caseTruth(c), notes);
    }

    /** The series of a finding expectation: its seriesRef, else the enabled registry rows of its regions. */
    List<SeriesRef> refs(Map<?, ?> f, BenchmarkCaseEntity c) {
        List<SeriesRef> out = new ArrayList<>();
        if (f.get("seriesRef") != null) {
            out.add(SeriesRef.parse(String.valueOf(f.get("seriesRef"))));
            return out;
        }
        Set<String> regions = new LinkedHashSet<>(c.getRegionCodes());
        for (Object r : list(f.get("regionCodes"))) {
            regions.add(String.valueOf(r));
        }
        for (AnalysisSeriesEntity row : registry.findByEnabledTrueOrderByRefAsc()) {
            if (out.size() >= MAX_SERIES) {
                break;
            }
            if (row.getRegionCode() != null && regions.contains(row.getRegionCode())
                    && row.getDetectors().contains(ChangePointDetector.CUSUM)) {
                out.add(SeriesRef.parse(row.getRef()));
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------- precursors

    /** Compares a case's precursor expectations with the precursors' lifecycle status. */
    ScenarioMetrics precursors(BenchmarkCaseEntity c) {
        List<String> notes = new ArrayList<>();
        int checked = 0;
        int mismatches = 0;
        int skipped = 0;
        PrecursorRepository repo = precursors.getIfAvailable();
        for (Object item : list(c.getExpectations().get("precursors"))) {
            if (!(item instanceof Map<?, ?> p)) {
                continue;
            }
            Object id = p.get("precursorId");
            PrecursorEntity entity = id == null || repo == null ? null : repo.findById(String.valueOf(id))
                    .orElse(null);
            if (entity == null) {
                skipped++;
                notes.add("Precursor expectation without a known precursorId (" + id + "); skipped.");
                continue;
            }
            checked++;
            boolean validated = !PrecursorRules.CANDIDATE.equals(entity.getStatus());
            boolean expected = "validated".equals(p.get("expected"));
            if (validated != expected) {
                mismatches++;
            }
        }
        notes.add("Precursor status is the current lifecycle status (validated, stale or retired count as having"
                + " validated); it is not time-versioned.");
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("precursorsChecked", (double) checked);
        m.put("precursorMismatches", (double) mismatches);
        m.put("skippedExpectations", (double) skipped);
        return new ScenarioMetrics(ScenarioType.HISTORICAL.wire(), c.getId(), null, m, caseTruth(c), notes);
    }

    // ------------------------------------------------------------------------- questions

    /** Scores the base-rate forecaster on a case's questions that carry a base-rate definition. */
    ScenarioMetrics questions(BenchmarkCaseEntity c) {
        List<String> notes = new ArrayList<>();
        List<Double> p = new ArrayList<>();
        List<Boolean> o = new ArrayList<>();
        int skipped = 0;
        BaseRateService service = baseRates.getIfAvailable();
        for (Object item : list(c.getExpectations().get("questions"))) {
            if (!(item instanceof Map<?, ?> q) || !"binary".equals(q.get("type"))
                    || !(q.get("baseRate") instanceof Map<?, ?> def) || service == null) {
                skipped++;
                continue;
            }
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> definition = (Map<String, Object>) def;
                BaseRateService.Computed computed = service.computeAsOf(BaseRateDefinition.parse(definition),
                        c.getWindowFrom());
                double forecast = computed.result().pAtLeastOne();
                if (!Double.isFinite(forecast)) {
                    skipped++;
                    continue;
                }
                p.add(forecast);
                o.add(outcome(q.get("outcome")));
            } catch (RuntimeException e) {
                skipped++;
                notes.add("A question could not be forecast: " + e.getMessage());
            }
        }
        double[] pa = p.stream().mapToDouble(Double::doubleValue).toArray();
        boolean[] oa = new boolean[o.size()];
        for (int i = 0; i < oa.length; i++) {
            oa[i] = o.get(i);
        }
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("questions", (double) pa.length);
        m.put("skippedQuestions", (double) skipped);
        m.put("brier", BenchmarkScorer.brier(pa, oa));
        m.put("logScore", BenchmarkScorer.logScore(pa, oa));
        notes.add("Binary questions with a baseRate definition are forecast as of the window start.");
        return new ScenarioMetrics(ScenarioType.HISTORICAL.wire(), c.getId(), null, m, caseTruth(c), notes);
    }

    static boolean outcome(Object value) {
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Number n) {
            return n.doubleValue() != 0;
        }
        String text = String.valueOf(value).trim().toLowerCase(java.util.Locale.ROOT);
        return text.equals("yes") || text.equals("true") || text.equals("1");
    }

    // ------------------------------------------------------------------------- auto null cases

    /**
     * Samples this month's null region-weeks (idempotent: a region-week already in the corpus is not added again).
     *
     * @param now the current time
     * @return the cases added
     */
    public int sampleNullCases(Instant now) {
        LocalDate thisWeek = LocalDate.ofInstant(now, ZoneOffset.UTC)
                .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        Instant to = thisWeek.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant from = to.minus(Duration.ofDays(364));
        List<String[]> candidates = nullCandidates(from, to);
        Set<String> existing = new LinkedHashSet<>();
        for (BenchmarkCaseEntity c : cases.findBySource("auto")) {
            existing.add(c.getName());
        }
        Rng rng = new Rng(Seeds.of("benchmark-null-cases", YearMonth.from(now.atZone(ZoneOffset.UTC)).toString()));
        for (int i = candidates.size() - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            String[] tmp = candidates.get(i);
            candidates.set(i, candidates.get(j));
            candidates.set(j, tmp);
        }
        int added = 0;
        for (String[] candidate : candidates) {
            if (added >= NULL_CASES_PER_MONTH) {
                break;
            }
            String name = nullCaseName(candidate[0], candidate[1]);
            if (existing.contains(name)) {
                continue;
            }
            Instant weekStart = LocalDate.parse(candidate[1]).atStartOfDay(ZoneOffset.UTC).toInstant();
            BenchmarkCaseEntity c = new BenchmarkCaseEntity(Ulids.next(now), name, weekStart,
                    weekStart.plus(Duration.ofDays(7)), "auto", now);
            c.setDescription("Sampled automatically: no intel event at warning or above and no situation activity in"
                    + " this region-week; no significant finding is expected.");
            c.setRegionCodes(List.of(candidate[0]));
            Map<String, Object> finding = new LinkedHashMap<>();
            finding.put("findingType", ChangePointBenchmark.NAME);
            finding.put("present", false);
            finding.put("regionCodes", List.of(candidate[0]));
            Map<String, Object> expectations = new LinkedHashMap<>();
            expectations.put("findings", List.of(finding));
            expectations.put("notes", "auto-sampled null region-week");
            c.setExpectations(expectations);
            c.setCreatedBy("scheduler");
            cases.save(c);
            existing.add(name);
            added++;
        }
        log.info("Benchmark corpus: {} null region-week case(s) sampled from {} candidate(s)", added,
                candidates.size());
        return added;
    }

    /** The name of a null case. */
    static String nullCaseName(String region, String weekStart) {
        return "Null region-week " + region + " " + weekStart;
    }

    /**
     * The region-weeks in {@code [from, to)} with region rollup rows, no intel event at warning or above and no
     * situation active, as {@code [region, week start]}, sorted.
     */
    List<String[]> nullCandidates(Instant from, Instant to) {
        String sql = "SELECT c.region_code, c.week FROM (SELECT DISTINCT region_code, CAST(date_trunc('week',"
                + " bucket_start AT TIME ZONE 'UTC') AS date) AS week FROM activity_rollup WHERE resolution ="
                + " :resolution AND region_code IS NOT NULL AND region_code <> :untagged AND bucket_start >= :from AND"
                + " bucket_start < :to) c WHERE NOT EXISTS (SELECT 1 FROM intelligence_event e WHERE e.severity IN"
                + " ('warning', 'critical') AND e.start_time >= (c.week AT TIME ZONE 'UTC') AND e.start_time <"
                + " ((c.week + 7) AT TIME ZONE 'UTC') AND jsonb_exists(e.region_tags, c.region_code)) AND NOT EXISTS"
                + " (SELECT 1 FROM situation s WHERE c.region_code = ANY(s.region_codes) AND s.started_at <"
                + " ((c.week + 7) AT TIME ZONE 'UTC') AND s.last_activity_at >= (c.week AT TIME ZONE 'UTC'))"
                + " ORDER BY 1, 2";
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("resolution", RegionRollupResolution.DAY.code())
                .addValue("untagged", RegionRollupComputer.UNTAGGED)
                .addValue("from", Timestamp.from(from))
                .addValue("to", Timestamp.from(to));
        return new ArrayList<>(jdbc.query(sql, p, (rs, n) -> new String[] {rs.getString(1),
                rs.getDate(2).toLocalDate().toString()}));
    }

    private static Map<String, Object> caseTruth(BenchmarkCaseEntity c) {
        Map<String, Object> out = new LinkedHashMap<>(c.getExpectations());
        out.put("name", c.getName());
        out.put("windowFrom", c.getWindowFrom().toString());
        out.put("windowTo", c.getWindowTo().toString());
        out.put("regionCodes", c.getRegionCodes());
        out.put("source", c.getSource());
        return out;
    }

    private static List<?> list(Object value) {
        return value instanceof List<?> l ? l : List.of();
    }
}
