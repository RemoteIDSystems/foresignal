package ai.foresignal.analytics.eventstudy;

import ai.foresignal.analytics.AnalyticsSupport;
import ai.foresignal.analytics.finding.FindingCaveats;
import ai.foresignal.analytics.math.Seeds;
import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesFrequency;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.backtest.BacktestJobEntity;
import ai.foresignal.backtest.BacktestJobService;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.backtest.JobRunner;
import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Batch event studies (prompt 119, Intelligence-Design §5.3): {@code POST /v1/analytics/event-studies} validates an
 * {@code EventStudyRequest} and submits a 103 job of type {@value #TYPE}; as the job's {@link JobRunner} it resolves
 * the events, reads the target series through the {@link SeriesReader} as of the replay's knowledge cutoff (INT-2), runs
 * the {@link EventStudyEngine} and returns the {@code EventStudyResult} (per-event abnormal paths, CAAR per window, test
 * statistics, the placebo distribution and caveats) with its 116 manifest.
 */
@Service
public class EventStudyService implements JobRunner {

    /** The 103 job type. */
    public static final String TYPE = "event-study";

    /** The longest event range. */
    public static final Duration MAX_RANGE = Duration.ofDays(3653);

    /** The most per-event abnormal paths a result carries. */
    static final int MAX_PATHS = 1_000;

    private final ObjectProvider<BacktestJobService> jobs;
    private final EventSelectorResolver selectors;
    private final SeriesReader reader;
    private final ObjectProvider<ManifestService> manifests;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public EventStudyService(ObjectProvider<BacktestJobService> jobs, EventSelectorResolver selectors,
            SeriesReader reader, ObjectProvider<ManifestService> manifests, JsonMapper json, Clock clock) {
        this.jobs = jobs;
        this.selectors = selectors;
        this.reader = reader;
        this.manifests = manifests;
        this.json = json;
        this.clock = clock;
    }

    @Override
    public String type() {
        return TYPE;
    }

    /**
     * Validates a request and submits the job.
     *
     * @param tokenId the caller's token id (one queued or running job per token), or null
     * @throws ValidationException 400 {@code EVENT_STUDY_INVALID} or {@code SERIES_REF_INVALID}
     * @throws NotFoundException 404 {@code SERIES_NOT_FOUND}
     */
    public BacktestJobEntity submit(JsonNode body, String tokenId) {
        Map<String, Object> definition = validate(body);
        String target = definition.get("target").toString();
        return jobs.getObject().submitRunnerJob(TYPE, target.length() <= 64 ? target : null, definition, tokenId);
    }

    /** The validated definition snapshot of a request. */
    @SuppressWarnings("unchecked")
    Map<String, Object> validate(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw invalid("body", "must be an EventStudyRequest object");
        }
        Set<String> allowed = Set.of("name", "selector", "target", "from", "to", "asOf");
        for (String field : body.propertyNames()) {
            if (!allowed.contains(field)) {
                throw invalid(field, "is not an EventStudyRequest member");
            }
        }
        SeriesRef target = SeriesRef.parse(body.hasNonNull("target") ? body.get("target").asString() : null);
        if (!body.hasNonNull("selector") || !body.get("selector").isObject()) {
            throw invalid("selector", "is required");
        }
        Map<String, Object> selector = json.convertValue(body.get("selector"), Map.class);
        selectors.validate(selector);
        Instant from = instant(body, "from", true);
        Instant to = instant(body, "to", true);
        Instant now = clock.instant();
        Instant asOf = instant(body, "asOf", false);
        if (!from.isBefore(to)) {
            throw invalid("to", "must be after from");
        }
        if (Duration.between(from, to).compareTo(MAX_RANGE) > 0) {
            throw invalid("to", "the range may span at most 10 years");
        }
        if (asOf != null && asOf.isAfter(now)) {
            throw invalid("asOf", "must not be in the future");
        }
        if (!reader.exists(target)) {
            throw new NotFoundException(ErrorCodes.SERIES_NOT_FOUND, "No series '" + target.format() + "'.");
        }
        SeriesFrequency frequency = reader.meta(target).frequency();
        if (frequency != SeriesFrequency.DAY && frequency != SeriesFrequency.WEEK
                && frequency != SeriesFrequency.HOUR) {
            throw invalid("target", "event studies need a daily, weekly or hourly series, not " + frequency.wire());
        }
        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("name", body.hasNonNull("name") ? body.get("name").asString() : null);
        definition.put("selector", selector);
        definition.put("target", target.format());
        definition.put("from", from.toString());
        definition.put("to", to.toString());
        definition.put("asOf", (asOf == null ? now : asOf).toString());
        return definition;
    }

    @Override
    public Map<String, Object> run(BacktestJobEntity job, JobMonitor monitor) {
        Map<String, Object> d = job.getDefinition();
        @SuppressWarnings("unchecked")
        Map<String, Object> selector = (Map<String, Object>) d.get("selector");
        SeriesRef target = SeriesRef.parse(String.valueOf(d.get("target")));
        Instant from = Instant.parse(String.valueOf(d.get("from")));
        Instant to = Instant.parse(String.valueOf(d.get("to")));
        Instant asOf = Instant.parse(String.valueOf(d.get("asOf")));
        return study(selector, target, from, to, asOf, job.getDefinitionHash(), monitor);
    }

    /** Runs a study (the job body; tests call it directly). */
    public Map<String, Object> study(Map<String, Object> selector, SeriesRef target, Instant from, Instant to,
            Instant asOf, String definitionHash, JobMonitor monitor) {
        List<EventSelectorResolver.Occurrence> occurrences = new ArrayList<>();
        for (EventSelectorResolver.Occurrence o : selectors.resolve(selector, from, to)) {
            if (!o.at().isAfter(asOf)) {
                occurrences.add(o);
            }
        }
        SeriesReader.Meta meta = reader.meta(target);
        SeriesFrequency frequency = meta.frequency() == SeriesFrequency.HOUR ? SeriesFrequency.DAY : meta.frequency();
        EventStudyEngine.Windows windows = EventStudyEngine.Windows.of(frequency);
        Instant readFrom = frequency.plus(frequency.floor(from), windows.estFrom() - 2);
        Instant readTo = frequency.plus(frequency.floor(to), windows.evTo() + 1);
        if (readTo.isAfter(asOf)) {
            readTo = asOf;
        }
        if (!readFrom.isBefore(readTo)) {
            readTo = readFrom.plus(frequency.approximateWidth());
        }
        SeriesData data = reader.read(target, readFrom, readTo, asOf);
        if (data.frequency() != frequency) {
            data = data.resample(frequency);
        }
        if (monitor != null) {
            monitor.progress(0.5);
        }
        SeriesData.Grid grid = data.grid();
        EventStudyEngine.Transform transform = EventStudyEngine.Transform.of(data.counts(), data.transform(),
                data.seriesFamily());
        long seed = Seeds.of("event-study", String.valueOf(definitionHash), from.toString(), to.toString());
        List<EventStudyEngine.Event> events = new ArrayList<>();
        for (EventSelectorResolver.Occurrence o : occurrences) {
            events.add(new EventStudyEngine.Event(o.at(), o.ref()));
        }
        EventStudyEngine.Result result = EventStudyEngine.run(new EventStudyEngine.Input(grid.starts(), grid.values(),
                frequency, transform, windows, events, List.of(), seed, ai.foresignal.analytics.math.PlaceboSampler
                        .DEFAULT_COUNT, true));
        boolean lagged = lagged(grid, frequency, windows, result);
        List<Map<String, Object>> caveats = caveats(result, data, lagged);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("windows", windows.toMap());
        params.put("transform", transform.wire());
        params.put("placeboCount", ai.foresignal.analytics.math.PlaceboSampler.DEFAULT_COUNT);
        params.put("minEstimation", windows.minEstimation());
        Map<String, Object> out = new LinkedHashMap<>();
        Map<String, Object> detector = new LinkedHashMap<>();
        detector.put("name", EventStudyEngine.NAME);
        detector.put("version", EventStudyEngine.VERSION);
        detector.put("params", params);
        out.put("detector", detector);
        out.put("definitionHash", definitionHash);
        out.put("target", target.format());
        out.put("frequency", frequency.wire());
        out.put("transform", transform.wire());
        out.put("windows", windows.toMap());
        Map<String, Object> range = new LinkedHashMap<>();
        range.put("start", from.toString());
        range.put("end", to.toString());
        out.put("range", range);
        out.put("asOfSeq", data.asOfSeq());
        out.put("placeboSeed", Seeds.hex("event-study", String.valueOf(definitionHash), from.toString(),
                to.toString()));
        out.put("nEvents", result.events().size());
        out.put("nClusters", result.clusters());
        out.put("nExcluded", result.excluded());
        List<Map<String, Object>> paths = new ArrayList<>();
        int i = 0;
        for (EventStudyEngine.EventOutcome e : result.events()) {
            if (i++ >= MAX_PATHS) {
                break;
            }
            paths.add(path(e, windows));
        }
        out.put("events", paths);
        Map<String, Object> caar = new LinkedHashMap<>();
        caar.put("preEvent", result.pre().toMap());
        caar.put("event", result.event().toMap());
        out.put("caar", caar);
        out.put("caveats", caveats);
        out.put("valuesWithheld", false);
        out.put("truncated", result.events().size() > MAX_PATHS);
        out.put("manifestId", manifest(params, data, definitionHash, target));
        if (monitor != null) {
            monitor.progress(1.0);
        }
        return out;
    }

    /** True when a used event's pre-event window holds a point published after the event. */
    static boolean lagged(SeriesData.Grid grid, SeriesFrequency frequency, EventStudyEngine.Windows w,
            EventStudyEngine.Result result) {
        for (EventStudyEngine.EventOutcome e : result.events()) {
            if (e.excluded() != null) {
                continue;
            }
            for (int off = w.preFrom(); off <= w.preTo(); off++) {
                int t = e.index() + off;
                if (t >= 0 && t < grid.size() && grid.knownAt()[t] != null && grid.knownAt()[t].isAfter(e.at())) {
                    return true;
                }
            }
        }
        return false;
    }

    static List<Map<String, Object>> caveats(EventStudyEngine.Result result, SeriesData data, boolean lagged) {
        List<Map<String, Object>> out = new ArrayList<>();
        boolean fewPoints = result.events().stream().anyMatch(e -> e.excluded() != null
                && e.excluded().startsWith(FindingCaveats.FEW_EVENTS));
        if (result.clusters() < 5 || fewPoints) {
            out.add(FindingCaveats.of(FindingCaveats.FEW_EVENTS, "Fewer than five independent event clusters, or"
                    + " events with too short an estimation window; the averages are unstable."));
        }
        if (result.clusters() < 10) {
            out.add(FindingCaveats.of(FindingCaveats.SMALL_SAMPLE, "Fewer than ten samples."));
        }
        if (result.events().stream().anyMatch(e -> e.excluded() != null && e.excluded().contains("coverage gap"))) {
            out.add(FindingCaveats.of(FindingCaveats.COVERAGE_GAPS, "Events whose windows had missing or uncovered"
                    + " points were excluded."));
        }
        if (lagged) {
            out.add(FindingCaveats.of(FindingCaveats.LAGGED_PUBLICATION, "Some pre-event values were published after"
                    + " the event; a live observer could not have seen them in time."));
        }
        if (AnalyticsSupport.market(data)) {
            out.add(FindingCaveats.dailyResolutionOnly());
        }
        out.add(FindingCaveats.notGroundTruth());
        return out;
    }

    private static Map<String, Object> path(EventStudyEngine.EventOutcome e, EventStudyEngine.Windows w) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("at", e.at().toString());
        m.put("ref", e.ref());
        m.put("clusterId", e.clusterId());
        m.put("excluded", e.excluded());
        m.put("estimationPoints", e.estimationPoints());
        List<Map<String, Object>> abnormal = new ArrayList<>();
        double cumulative = 0;
        boolean broken = false;
        for (int k = 0; k < e.abnormal().length; k++) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("offset", w.preFrom() + k);
            double v = e.abnormal()[k];
            point.put("value", EventStudyEngine.finite(v));
            if (Double.isFinite(v) && !broken) {
                cumulative += v;
                point.put("cumulative", cumulative);
            } else {
                broken = true;
                point.put("cumulative", null);
            }
            abnormal.add(point);
        }
        m.put("abnormal", abnormal);
        Map<String, Object> car = new LinkedHashMap<>();
        car.put("preEvent", EventStudyEngine.finite(e.carPre()));
        car.put("event", EventStudyEngine.finite(e.carEvent()));
        m.put("car", car);
        return m;
    }

    private String manifest(Map<String, Object> params, SeriesData data, String definitionHash, SeriesRef target) {
        ManifestService service = manifests.getIfAvailable();
        if (service == null) {
            return null;
        }
        Map<String, Object> config = new LinkedHashMap<>(params);
        config.put("definitionHash", definitionHash);
        return service.record(ManifestRequest.of(EventStudyEngine.NAME, EventStudyEngine.VERSION).config(config)
                .asOf(data.asOfSeq()).inputs(List.of(target.format())));
    }

    private static Instant instant(JsonNode body, String field, boolean required) {
        if (!body.hasNonNull(field)) {
            if (required) {
                throw invalid(field, "is required");
            }
            return null;
        }
        try {
            return Instant.parse(body.get(field).asString());
        } catch (RuntimeException e) {
            throw invalid(field, "must be a date-time");
        }
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(ErrorCodes.EVENT_STUDY_INVALID, "The event study is invalid: " + field + " "
                + message + ".", List.of(new FieldError(field, message)));
    }
}
