package ai.foresignal.analytics.changepoint;

import ai.foresignal.analytics.AnalyticsSupport;
import ai.foresignal.analytics.finding.FindingCaveats;
import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.analytics.finding.FindingSeverities;
import ai.foresignal.analytics.math.CusumThresholds;
import ai.foresignal.analytics.registry.AnalysisSeriesEntity;
import ai.foresignal.analytics.registry.AnalysisSeriesRepository;
import ai.foresignal.analytics.registry.AnalysisSeriesService;
import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesFrequency;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.observability.AbsenceCheck;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Runs the change-point detectors over the analysis-series registry (prompt 119, Intelligence-Design §5.2): daily and
 * weekly rows nightly at 03:00 UTC on the last closed day, hourly rows (opt-in) at :20 each hour. Each run reads the
 * series through the {@link SeriesReader} with {@code asOf} = the run start (INT-2) and writes {@code change-point}
 * findings with {@code stats {changeAt, levelBefore, levelAfter, effect, detectionDelay, bocpdPosterior}} and the
 * false-alarm base rate of the same settings on the series' own history.
 *
 * <p>Severity: a confirmed CUSUM candidate is {@code notable}; with a BOCPD posterior ≥ 0.8 {@code significant}. A
 * candidate cut by a coverage gap, or a downward change of counts that 116 detectability cannot support, is stored as
 * an {@code info} finding with {@code state: unknown} (and {@code LOW_DETECTABILITY}), never as a drop.
 */
@Service
public class ChangePointService {

    private static final Logger log = LoggerFactory.getLogger(ChangePointService.class);

    /** The detector parameters' default history per frequency, buckets read. */
    static final int HISTORY_DAYS = 400;

    private final AnalysisSeriesRepository registry;
    private final AnalysisSeriesService registryService;
    private final SeriesReader reader;
    private final FindingService findings;
    private final AnalyticsSupport support;
    private final Clock clock;
    private final CusumThresholds thresholds = new CusumThresholds();

    /** Creates the service. */
    public ChangePointService(AnalysisSeriesRepository registry, AnalysisSeriesService registryService,
            SeriesReader reader, FindingService findings, AnalyticsSupport support, Clock clock) {
        this.registry = registry;
        this.registryService = registryService;
        this.reader = reader;
        this.findings = findings;
        this.support = support;
        this.clock = clock;
    }

    /**
     * What a run did.
     *
     * @param series series evaluated
     * @param findings findings written (created or superseding)
     * @param errors series that failed
     */
    public record RunResult(int series, int findings, int errors) {}

    /** Runs over the enabled rows of {@code frequency} ({@code hour}, {@code day} or {@code week}) at {@code now}. */
    public RunResult run(String frequency, Instant now) {
        int evaluated = 0;
        int written = 0;
        int errors = 0;
        for (AnalysisSeriesEntity row : registry.findByEnabledTrueAndFrequencyOrderByRefAsc(frequency)) {
            if (!row.getDetectors().contains(ChangePointDetector.CUSUM)) {
                continue;
            }
            try {
                if (evaluate(row, now) != null) {
                    written++;
                }
                registryService.markRun(row.getRef(), now, null);
            } catch (RuntimeException e) {
                errors++;
                log.warn("Change-point run on {} failed: {}", row.getRef(), e.toString());
                registryService.markRun(row.getRef(), now, e.getMessage() == null ? e.toString() : e.getMessage());
            }
            evaluated++;
        }
        return new RunResult(evaluated, written, errors);
    }

    /** The threshold cache (tests). */
    CusumThresholds thresholds() {
        return thresholds;
    }

    /**
     * Evaluates one registry row at {@code now}.
     *
     * @return the outcome when a finding was written or matched, else null
     */
    public FindingService.Outcome evaluate(AnalysisSeriesEntity row, Instant now) {
        SeriesRef ref = SeriesRef.parse(row.getRef());
        SeriesFrequency frequency = SeriesFrequency.parse(row.getFrequency());
        if (frequency == null) {
            frequency = SeriesFrequency.DAY;
        }
        Instant to = frequency.floor(now);
        Instant from = frequency.plus(to, -historyBuckets(frequency));
        SeriesData data = reader.read(ref, from, to, now);
        if (data.frequency() != frequency) {
            data = data.resample(frequency);
        }
        return evaluate(row, data, frequency, now, row.getDetectors().contains(ChangePointDetector.BOCPD));
    }

    /** Evaluates already-read data (tests drive this with planted series). */
    FindingService.Outcome evaluate(AnalysisSeriesEntity row, SeriesData data, SeriesFrequency frequency, Instant now,
            boolean bocpd) {
        SeriesData.Grid grid = data.grid().knownBy(now);
        ChangePointDetector.Params params = params(frequency);
        ChangePointDetector.Result result = ChangePointDetector.detect(grid.starts(), grid.values(), grid.gap(),
                data.counts(), params, thresholds);
        if (result.state() == ChangePointDetector.State.NONE) {
            return null;
        }
        FindingDraft draft = draft(row, data, frequency, now, params, result, bocpd);
        return draft == null ? null : findings.record(draft);
    }

    /** The parameters of a frequency: slot period and how recent an alarm must be. */
    static ChangePointDetector.Params params(SeriesFrequency frequency) {
        return switch (frequency) {
            case HOUR -> ChangePointDetector.Params.defaults(168, 24);
            case WEEK -> ChangePointDetector.Params.defaults(1, 4);
            default -> ChangePointDetector.Params.defaults(7, 7);
        };
    }

    static int historyBuckets(SeriesFrequency frequency) {
        return switch (frequency) {
            case HOUR -> 24 * 60;
            case WEEK -> 260;
            default -> HISTORY_DAYS;
        };
    }

    private FindingDraft draft(AnalysisSeriesEntity row, SeriesData data, SeriesFrequency frequency, Instant now,
            ChangePointDetector.Params params, ChangePointDetector.Result r, boolean bocpd) {
        List<Map<String, Object>> caveats = new ArrayList<>();
        caveats.add(FindingCaveats.notGroundTruth());
        String severity;
        String state;
        if (r.state() == ChangePointDetector.State.UNKNOWN) {
            if (r.changeAt() == null) {
                return null;
            }
            severity = FindingSeverities.INFO;
            state = "unknown";
            caveats.add(FindingCaveats.of(FindingCaveats.COVERAGE_GAPS, "A coverage gap fell between the estimated"
                    + " change and the end of the series; the change cannot be judged."));
        } else {
            boolean confirmed = bocpd && Double.isFinite(r.bocpdPosterior())
                    && r.bocpdPosterior() >= params.confirmPosterior();
            severity = confirmed ? FindingSeverities.SIGNIFICANT : FindingSeverities.NOTABLE;
            state = "confirmed";
        }
        if (r.downward() && data.counts() && !"unknown".equals(state)) {
            AbsenceCheck check = support.canSeeDecline(data, r.changeAt(), frequency.next(frequency.floor(now)));
            if (!check.ok()) {
                severity = FindingSeverities.INFO;
                state = "unknown";
                caveats.add(FindingCaveats.of(FindingCaveats.LOW_DETECTABILITY, "The sources could not have seen the"
                        + " decline reliably (" + check.reason() + "); a fall in reports is not read as a fall in"
                        + " activity."));
            }
        }
        if (AnalyticsSupport.market(data)) {
            caveats.add(FindingCaveats.dailyResolutionOnly());
        }
        double perYear = frequency.perYear();
        double rate = r.alarmsPerYear(perYear);
        long historyDays = Math.round(r.historyBuckets() * frequency.approximateWidth().toHours() / 24.0);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("changeAt", r.changeAt().toString());
        stats.put("alarmAt", r.alarmAt() == null ? null : r.alarmAt().toString());
        stats.put("direction", r.direction() > 0 ? "up" : r.direction() < 0 ? "down" : null);
        stats.put("levelBefore", AnalyticsSupport.round(r.levelBefore()));
        stats.put("levelAfter", AnalyticsSupport.round(r.levelAfter()));
        stats.put("effect", AnalyticsSupport.round(r.effect()));
        stats.put("effectUnit", data.counts() ? "relative" : "sd");
        stats.put("detectionDelay", r.detectionDelay());
        stats.put("bocpdPosterior", bocpd && Double.isFinite(r.bocpdPosterior())
                ? Math.round(r.bocpdPosterior() * 100) / 100.0 : null);
        stats.put("threshold", AnalyticsSupport.round(r.threshold()));
        stats.put("model", r.model());
        stats.put("state", state);
        Map<String, Object> baseRate = new LinkedHashMap<>();
        baseRate.put("value", AnalyticsSupport.round(rate));
        baseRate.put("unit", "alarms-per-year");
        baseRate.put("method", "detector-false-alarm-rate");
        baseRate.put("n", r.historicalAlarms());
        Map<String, Object> period = new LinkedHashMap<>();
        period.put("start", data.from().toString());
        period.put("end", r.changeAt().toString());
        baseRate.put("period", period);
        baseRate.put("text", "the same settings flagged " + r.historicalAlarms() + " change"
                + (r.historicalAlarms() == 1 ? "" : "s") + " in the previous " + historyDays + " days of this series"
                + (Double.isFinite(rate) ? " (" + AnalyticsSupport.num(rate) + " a year)" : ""));
        boolean localOnly = data.localOnly();
        String label = AnalyticsSupport.label(data.ref());
        String verb = r.direction() > 0 ? "rose" : "fell";
        String change = data.counts() ? AnalyticsSupport.num(Math.abs(r.effect()) * 100) + "%"
                : AnalyticsSupport.num(Math.abs(r.effect())) + " SD";
        String explanation = label + " " + verb + " by " + change + " from " + r.changeAt()
                + (localOnly ? "" : " (level " + AnalyticsSupport.num(r.levelBefore()) + " before, "
                        + AnalyticsSupport.num(r.levelAfter()) + " after)")
                + ", flagged after " + r.detectionDelay() + " bucket(s); " + baseRate.get("text") + "."
                + ("unknown".equals(state) ? " The change could not be confirmed; it is not reported as a "
                        + (r.downward() ? "drop." : "rise.") : "");
        Map<String, Object> detectorParams = params.toMap();
        detectorParams.put("bocpd", bocpd);
        List<String> regions = data.regionCode() == null ? List.of() : List.of(data.regionCode());
        Instant windowEnd = frequency.next(r.alarmAt());
        return new FindingDraft("change-point", "Change point: " + label + " " + (r.direction() > 0 ? "up" : "down")
                + " " + change, severity, null, regions, r.changeAt(), windowEnd, now, List.of(data.ref().format()),
                List.of(), data.asOfSeq(), stats, baseRate, caveats, ChangePointDetector.CUSUM,
                ChangePointDetector.VERSION, detectorParams, explanation, support.privateSeries(data),
                row.getFamily() != null ? row.getFamily() : data.signalFamily(), support.localOnlySources(data),
                localOnly ? List.of("levelBefore", "levelAfter") : List.of(), data.ref().format());
    }
}
