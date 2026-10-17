package ai.foresignal.analytics.cooccurrence;

import ai.foresignal.analytics.AnalyticsSupport;
import ai.foresignal.analytics.family.SignalFamilyRegistry;
import ai.foresignal.analytics.finding.FindingCaveats;
import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesFrequency;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.store.query.SeqClockQuery;
import ai.foresignal.stats.RegionRollupComputer;
import ai.foresignal.stats.RegionRollupResolution;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Runs the {@link CoOccurrenceMiner} daily at 05:00 UTC (prompt 119) over the cross-source region-day rollups
 * (canonical events counted once), grouped by signal family: every region and kind with activity in the trailing year
 * is read through the {@link SeriesReader} (coverage-aware; an uncovered day of any kind makes its family's day
 * uncovered), summed per family, and mined for the last closed day. Survivors become {@code co-occurrence} findings
 * with the per-family p-values, Fisher's p, the adjusted p, the expected joint rate and the lift; every one carries
 * {@code MULTIPLE_TESTS}.
 */
@Service
public class CoOccurrenceService {

    private static final Logger log = LoggerFactory.getLogger(CoOccurrenceService.class);

    /** The false-discovery level. */
    public static final double Q = 0.05;

    /** The history read, days (the trailing year plus the baseline weeks). */
    static final int HISTORY_DAYS = 365 + 7 * CoOccurrenceMiner.MAX_WEEKS;

    private final NamedParameterJdbcTemplate jdbc;
    private final SeriesReader reader;
    private final SignalFamilyRegistry families;
    private final FindingService findings;
    private final SeqClockQuery seqClock;
    private final Clock clock;

    /** Creates the service. */
    public CoOccurrenceService(NamedParameterJdbcTemplate jdbc, SeriesReader reader, SignalFamilyRegistry families,
            FindingService findings, SeqClockQuery seqClock, Clock clock) {
        this.jdbc = jdbc;
        this.reader = reader;
        this.families = families;
        this.findings = findings;
        this.seqClock = seqClock;
        this.clock = clock;
    }

    /**
     * What a run did.
     *
     * @param tests the tests of the day
     * @param survivors the survivors
     * @param findings the findings written
     */
    public record RunResult(int tests, int survivors, int findings) {}

    /** Mines the UTC day before {@code now}. */
    public RunResult run(Instant now) {
        Instant end = SeriesFrequency.DAY.floor(now);
        Instant day = end.minus(Duration.ofDays(1));
        Instant from = end.minus(Duration.ofDays(HISTORY_DAYS));
        Map<String, Map<String, CoOccurrenceMiner.Daily>> counts = counts(from, end, now);
        List<CoOccurrenceMiner.Test> tests = CoOccurrenceMiner.mine(counts, Q);
        long asOfSeq = seqClock.seqAt(jdbc, now);
        int survivors = 0;
        int written = 0;
        for (CoOccurrenceMiner.Test t : tests) {
            if (!t.survives()) {
                continue;
            }
            survivors++;
            try {
                FindingService.Outcome outcome = findings.record(draft(t, day, now, tests.size(), asOfSeq));
                if (outcome.change() != FindingService.Change.UNCHANGED) {
                    written++;
                }
            } catch (RuntimeException e) {
                log.warn("Recording the co-occurrence {} {}+{} failed: {}", t.region(), t.familyA(), t.familyB(),
                        e.toString());
            }
        }
        return new RunResult(tests.size(), survivors, written);
    }

    /** The family counts per region over {@code [from, end)}, as of {@code asOf}. */
    Map<String, Map<String, CoOccurrenceMiner.Daily>> counts(Instant from, Instant end, Instant asOf) {
        List<String[]> keys = jdbc.query("SELECT DISTINCT kind, region_code FROM activity_rollup WHERE resolution ="
                        + " :resolution AND source_id = :all AND entities > 0 AND bucket_start >= :from AND"
                        + " bucket_start < :end AND region_code IS NOT NULL AND region_code <> :untagged ORDER BY 1, 2",
                new MapSqlParameterSource().addValue("resolution", RegionRollupResolution.DAY.code())
                        .addValue("all", RegionRollupComputer.ALL_SOURCES)
                        .addValue("from", Timestamp.from(end.minus(Duration.ofDays(365))))
                        .addValue("end", Timestamp.from(end))
                        .addValue("untagged", RegionRollupComputer.UNTAGGED),
                (rs, n) -> new String[] {rs.getString(1), rs.getString(2)});
        int days = (int) Duration.between(from, end).toDays();
        Map<String, Map<String, double[]>> sums = new TreeMap<>();
        Map<String, Map<String, boolean[]>> oks = new TreeMap<>();
        for (String[] key : keys) {
            String family = families.familyOfRecord(null, key[0]).orElse(null);
            if (family == null) {
                continue;
            }
            SeriesData data;
            try {
                data = reader.read(SeriesRef.rollup("day", SeriesRef.ALL_SOURCES, key[0], key[1]), from, end, asOf);
            } catch (RuntimeException e) {
                continue;
            }
            SeriesData.Grid grid = data.grid();
            double[] sum = sums.computeIfAbsent(key[1], k -> new TreeMap<>()).computeIfAbsent(family, f -> {
                double[] zero = new double[days];
                return zero;
            });
            boolean[] ok = oks.computeIfAbsent(key[1], k -> new TreeMap<>()).computeIfAbsent(family, f -> {
                boolean[] all = new boolean[days];
                java.util.Arrays.fill(all, true);
                return all;
            });
            for (int i = 0; i < days && i < grid.size(); i++) {
                if (grid.gap()[i]) {
                    ok[i] = false;
                } else {
                    sum[i] += grid.values()[i];
                }
            }
        }
        Map<String, Map<String, CoOccurrenceMiner.Daily>> out = new TreeMap<>();
        sums.forEach((region, fams) -> fams.forEach((family, values) -> out.computeIfAbsent(region,
                r -> new TreeMap<>()).put(family, new CoOccurrenceMiner.Daily(values, oks.get(region).get(family)))));
        return out;
    }

    private FindingDraft draft(CoOccurrenceMiner.Test t, Instant day, Instant now, int tests, long asOfSeq) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("families", List.of(t.familyA(), t.familyB()));
        Map<String, Object> pValues = new LinkedHashMap<>();
        pValues.put(t.familyA(), AnalyticsSupport.round(t.pA()));
        pValues.put(t.familyB(), AnalyticsSupport.round(t.pB()));
        stats.put("pValues", pValues);
        Map<String, Object> observed = new LinkedHashMap<>();
        observed.put(t.familyA(), t.countA());
        observed.put(t.familyB(), t.countB());
        stats.put("counts", observed);
        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put(t.familyA(), AnalyticsSupport.round(t.expectedA()));
        expected.put(t.familyB(), AnalyticsSupport.round(t.expectedB()));
        stats.put("expected", expected);
        stats.put("fisherP", AnalyticsSupport.round(t.fisherP()));
        stats.put("adjustedP", AnalyticsSupport.round(t.adjustedP()));
        double perYear = t.historyDays() == 0 ? Double.NaN : t.expectedJointDays() * 365.0 / t.historyDays();
        stats.put("expectedJointRate", AnalyticsSupport.round(perYear));
        stats.put("jointDays", t.jointDays());
        stats.put("historyDays", t.historyDays());
        stats.put("lift", Double.isInfinite(t.lift()) ? null : AnalyticsSupport.round(t.lift()));
        stats.put("tests", tests);
        stats.put("day", day.toString());
        Map<String, Object> baseRate = new LinkedHashMap<>();
        baseRate.put("value", AnalyticsSupport.round(perYear));
        baseRate.put("unit", "expected-joint-days-per-year");
        baseRate.put("method", "independence-baseline");
        baseRate.put("n", t.historyDays());
        baseRate.put("text", "if the two families were independent, both would exceed their baselines on about "
                + AnalyticsSupport.num(perYear) + " days a year; it happened on " + t.jointDays() + " of the last "
                + t.historyDays() + " days");
        List<Map<String, Object>> caveats = new ArrayList<>();
        caveats.add(FindingCaveats.of(FindingCaveats.MULTIPLE_TESTS, tests + " region and family pairs were tested"
                + " today; the false-discovery rate is controlled at " + Q + " (Benjamini–Hochberg)."));
        caveats.add(FindingCaveats.notGroundTruth());
        String explanation = t.familyA() + " (" + (long) t.countA() + " vs about " + AnalyticsSupport.num(t.expectedA())
                + " expected) and " + t.familyB() + " (" + (long) t.countB() + " vs about "
                + AnalyticsSupport.num(t.expectedB()) + ") both ran above their baselines in " + t.region() + " on "
                + day.toString().substring(0, 10) + " (Fisher p " + AnalyticsSupport.num(t.fisherP()) + ", adjusted "
                + AnalyticsSupport.num(t.adjustedP()) + "); " + baseRate.get("text") + ". Co-occurrence is not"
                + " causation.";
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("q", Q);
        params.put("exceedanceP", CoOccurrenceMiner.EXCEEDANCE_P);
        params.put("baselineWeeks", List.of(CoOccurrenceMiner.MIN_WEEKS, CoOccurrenceMiner.MAX_WEEKS));
        params.put("significantLift", CoOccurrenceMiner.SIGNIFICANT_LIFT);
        return new FindingDraft("co-occurrence", "Co-occurrence: " + t.familyA() + " and " + t.familyB() + " in "
                + t.region(), t.severity(), null, List.of(t.region()), day, day.plus(Duration.ofDays(1)), now,
                List.of(), List.of(), asOfSeq, stats, baseRate, caveats, CoOccurrenceMiner.NAME,
                CoOccurrenceMiner.VERSION, params, explanation, false, t.familyA(), List.of(), List.of(),
                t.region() + "|" + t.familyA() + "+" + t.familyB());
    }
}
