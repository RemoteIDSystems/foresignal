package ai.foresignal.doctor;

import ai.foresignal.api.error.BudgetExceededProblem;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.BudgetGovernor;
import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.ProbeService;
import ai.foresignal.collect.RecordedExchange;
import ai.foresignal.collect.RequestRecorder;
import ai.foresignal.collect.SourceHealthView;
import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.CoverageSample;
import ai.foresignal.store.FetchLogEntity;
import ai.foresignal.store.FetchLogRepository;
import ai.foresignal.store.SourceHealthEntity;
import ai.foresignal.store.SourceHealthRepository;
import jakarta.annotation.PreDestroy;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Runs the adapter doctor (prompt 140, task 4): {@code run(sourceId, DoctorOptions, trigger)}.
 *
 * <ol>
 *   <li>Probe the upstream through the normal budgeted client (the 010 probe, {@link ProbeService#capture}); with no
 *       budget left or a backoff in force the probe is skipped ({@code PROBE_SKIPPED_BUDGET}) and the diagnosis uses the
 *       history only. The body is held in memory only, capped at {@value #PROBE_MAX_BYTES} bytes; it is never
 *       persisted.
 *   <li>Extract the structure of the probe body and of the fixture ({@link FixtureCatalog}), then diff them.
 *   <li>Run the deterministic rules over health, the last 20 {@code fetch_log} rows, budget and backoff, the probe and
 *       the diff.
 *   <li>The LLM step, when enabled and a chat provider exists ({@link DoctorLlm}).
 *   <li>Persist a {@code doctor_report}.
 * </ol>
 *
 * <p>At most one run per source per {@value #MIN_INTERVAL_MINUTES} minutes, and never two at once
 * ({@code 409 DOCTOR_BUSY}); manual runs bypass the automatic 6 h cool-off ({@link DoctorAutoRunner}) but not this
 * limit, nor the source's budget.
 */
@Service
public class DoctorService {

    private static final Logger log = LoggerFactory.getLogger(DoctorService.class);

    /** The probe body cap: 1 MB, held in memory only. */
    public static final int PROBE_MAX_BYTES = 1_048_576;

    /** The stream capture window of a streaming source's probe, in seconds. */
    public static final int PROBE_STREAM_SECONDS = 10;

    /** The shortest interval between two runs of one source, in minutes. */
    public static final int MIN_INTERVAL_MINUTES = 10;

    /** The fetch history the rules read. */
    public static final int HISTORY = 20;

    /** The body head kept in memory for block-page and login-wall detection. */
    static final int BODY_HEAD = 4_096;

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final SourceRegistry registry;
    private final SourceService sources;
    private final ProbeService probes;
    private final HealthService health;
    private final SourceHealthRepository healthRows;
    private final FetchLogRepository fetchLog;
    private final BackoffRegistry backoff;
    private final BudgetGovernor budgets;
    private final FixtureCatalog fixtures;
    private final AdapterSourceCatalog adapterSources;
    private final DoctorLlm llm;
    private final DoctorReportRepository reports;
    private final ForesignalVersion version;
    private final JsonMapper json;
    private final Clock clock;
    private final StructureExtractor extractor;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    /** Source id → the id of the report being produced. */
    private final Map<String, String> running = new ConcurrentHashMap<>();

    /** Report id → the run producing it, while it runs. */
    private final Map<String, CompletableFuture<DoctorReportView>> inFlight = new ConcurrentHashMap<>();

    /** Creates the service. */
    public DoctorService(SourceRegistry registry, SourceService sources, ProbeService probes, HealthService health,
            SourceHealthRepository healthRows, FetchLogRepository fetchLog, BackoffRegistry backoff,
            BudgetGovernor budgets, FixtureCatalog fixtures, AdapterSourceCatalog adapterSources, DoctorLlm llm,
            DoctorReportRepository reports, ForesignalVersion version, JsonMapper json, Clock clock) {
        this.registry = registry;
        this.sources = sources;
        this.probes = probes;
        this.health = health;
        this.healthRows = healthRows;
        this.fetchLog = fetchLog;
        this.backoff = backoff;
        this.budgets = budgets;
        this.fixtures = fixtures;
        this.adapterSources = adapterSources;
        this.llm = llm;
        this.reports = reports;
        this.version = version;
        this.json = json;
        this.clock = clock;
        this.extractor = new StructureExtractor(json);
    }

    /** Stops the run executor. */
    @PreDestroy
    void close() {
        executor.shutdownNow();
    }

    /**
     * A started run.
     *
     * @param reportId the id the report will have
     * @param result completes with the persisted report, or exceptionally
     */
    public record Started(String reportId, CompletableFuture<DoctorReportView> result) {}

    /**
     * Runs the doctor and waits for the report.
     *
     * @throws NotFoundException (404) for an unknown source
     * @throws ConflictException (409) {@code DOCTOR_BUSY} when a run is in progress or ran within 10 minutes
     */
    public DoctorReportView run(String sourceId, DoctorOptions options, DoctorTrigger trigger) {
        Started started = start(sourceId, options, trigger);
        try {
            return started.result().join();
        } catch (java.util.concurrent.CompletionException e) {
            if (e.getCause() instanceof RuntimeException r) {
                throw r;
            }
            throw e;
        }
    }

    /**
     * Starts a run in the background after the source and busy checks.
     *
     * @throws NotFoundException (404) for an unknown source
     * @throws ConflictException (409) {@code DOCTOR_BUSY}
     */
    public Started start(String sourceId, DoctorOptions options, DoctorTrigger trigger) {
        registry.descriptor(sourceId);
        DoctorOptions opts = options == null ? DoctorOptions.DEFAULTS : options;
        Instant now = clock.instant();
        String reportId = Ulids.next(now);
        if (running.putIfAbsent(sourceId, reportId) != null) {
            throw new ConflictException(ErrorCodes.DOCTOR_BUSY,
                    "A doctor run for '" + sourceId + "' is in progress; wait for its report.");
        }
        Optional<DoctorReportEntity> last = reports.findFirstBySourceIdOrderByIdDesc(sourceId);
        if (last.isPresent() && last.get().getCreatedAt().isAfter(now.minus(Duration.ofMinutes(MIN_INTERVAL_MINUTES)))) {
            running.remove(sourceId, reportId);
            throw new ConflictException(ErrorCodes.DOCTOR_BUSY, "The doctor ran for '" + sourceId + "' at "
                    + last.get().getCreatedAt() + "; at most one run per source per " + MIN_INTERVAL_MINUTES
                    + " minutes (report " + last.get().getId() + ").");
        }
        CompletableFuture<DoctorReportView> future = new CompletableFuture<>();
        inFlight.put(reportId, future);
        executor.submit(() -> {
            try {
                future.complete(execute(reportId, sourceId, opts, trigger, now));
            } catch (Throwable t) {
                log.warn("The doctor run {} for {} failed: {}", reportId, sourceId, t.toString());
                future.completeExceptionally(t);
            } finally {
                running.remove(sourceId, reportId);
                inFlight.remove(reportId);
            }
        });
        return new Started(reportId, future);
    }

    /** True while the run producing {@code reportId} is in progress. */
    public boolean isRunning(String reportId) {
        return inFlight.containsKey(reportId);
    }

    /** One report, if persisted. */
    public Optional<DoctorReportView> report(String reportId) {
        return reports.findById(reportId).map(this::view);
    }

    /**
     * One report.
     *
     * @throws NotFoundException (404) {@code UNKNOWN_DOCTOR_REPORT}
     */
    public DoctorReportView requireReport(String reportId) {
        return report(reportId).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_DOCTOR_REPORT,
                "There is no doctor report '" + reportId + "'."));
    }

    /**
     * A source's reports, newest first.
     *
     * @param cursor the last id of the previous page, or null
     * @param limit 1..500
     * @throws NotFoundException (404) for an unknown source
     */
    public DoctorReportPage page(String sourceId, String cursor, int limit) {
        registry.descriptor(sourceId);
        int n = Math.max(1, Math.min(500, limit));
        List<DoctorReportEntity> rows = cursor == null || cursor.isBlank()
                ? reports.findBySourceIdOrderByIdDesc(sourceId, Limit.of(n + 1))
                : reports.findBySourceIdAndIdLessThanOrderByIdDesc(sourceId, cursor, Limit.of(n + 1));
        boolean more = rows.size() > n;
        List<DoctorReportView> items = rows.subList(0, Math.min(n, rows.size())).stream().map(this::view).toList();
        return new DoctorReportPage(items, more ? items.get(items.size() - 1).id() : null);
    }

    /** The newest report of a source, as a summary. */
    public Optional<DoctorReportSummary> latest(String sourceId) {
        return reports.findFirstBySourceIdOrderByIdDesc(sourceId).map(this::view).map(DoctorReportView::summary);
    }

    /** The newest report of every source that has one, as summaries by source id. */
    public Map<String, DoctorReportSummary> latestPerSource() {
        Map<String, DoctorReportSummary> out = new LinkedHashMap<>();
        for (DoctorReportEntity row : reports.findLatestPerSource()) {
            out.put(row.getSourceId(), view(row).summary());
        }
        return out;
    }

    /** The newest automatic report of a source, if any. */
    public Optional<DoctorReportEntity> latestAuto(String sourceId) {
        return reports.findFirstBySourceIdAndTriggerOrderByIdDesc(sourceId, DoctorTrigger.AUTO.name());
    }

    // ---------- the run ----------

    DoctorReportView execute(String reportId, String sourceId, DoctorOptions options, DoctorTrigger trigger,
            Instant createdAt) {
        SourceDescriptor descriptor = registry.descriptor(sourceId);
        Instant now = clock.instant();

        // 1. Probe.
        Probe probe = probe(descriptor, options);

        // 2. Structures and diff.
        Structure probeStructure = probe.body == null || probe.body.isEmpty() ? null
                : extractor.extract(probe.body, probe.facts.contentType());
        Optional<FixtureCatalog.FixtureFile> fixture = fixtures.primary(sourceId,
                probeStructure == null ? null : probeStructure.format());
        Structure fixtureStructure = fixture.flatMap(f -> fixtures.read(sourceId, f.file()))
                .map(bytes -> extractor.extract(bytes, null)).orElse(null);
        List<AdapterSourceCatalog.SourceFile> files = adapterSources.files(sourceId);
        ParserLiterals literals = ParserLiterals.scan(files.stream().map(AdapterSourceCatalog.SourceFile::text)
                .toList());
        StructureDiff diff = probeStructure == null ? null : StructureDiff.compare(fixtureStructure, probeStructure,
                fixture.map(FixtureCatalog.FixtureFile::file).orElse(null), literals);

        // 3. Deterministic rules.
        SourceHealthView h = health.health(sourceId);
        List<DiagnosisInput.FetchFacts> history = new ArrayList<>();
        for (FetchLogEntity row : fetchLog.findBySourceIdOrderByIdDesc(sourceId, Limit.of(HISTORY))) {
            history.add(new DiagnosisInput.FetchFacts(row.getStatus() == null ? null : row.getStatus().name(),
                    row.getHttpStatus(), row.getError(), row.getRecordsSeen(), row.getStartedAt()));
        }
        List<CoverageSample> coverage = healthRows.findById(sourceId).map(SourceHealthEntity::getRecentCoverage)
                .orElse(List.of());
        DiagnosisInput.BudgetFacts budget = budgetFacts(sourceId, h);
        DiagnosisInput input = new DiagnosisInput(sourceId, descriptor.zeroResultsNormal(),
                new DiagnosisInput.HealthFacts(h.status() == null ? null : h.status().value(), h.statusReason(),
                        h.consecutiveFailures()),
                history, coverage == null ? List.of() : coverage, budget, backoff.backoffUntil(sourceId), now,
                probe.facts, probeStructure, fixtureStructure, diff, literals);
        List<DoctorFinding> findings = DiagnosisRules.diagnose(input);

        // 4. LLM.
        Map<String, Object> llmBlock = switch (options.llm()) {
            case DoctorOptions.LLM_OFF -> DoctorLlm.skipped(DoctorLlm.OFF);
            case DoctorOptions.LLM_AUTO -> llm.available()
                    ? llm.explain(sourceId, descriptorSummary(descriptor, fixture.orElse(null)), findings, diff,
                            probe.body, options.full())
                    : DoctorLlm.skipped(DoctorLlm.NO_PROVIDER);
            default -> llm.explain(sourceId, descriptorSummary(descriptor, fixture.orElse(null)), findings, diff,
                    probe.body, options.full());
        };

        // 5. Persist (no raw body: the probe map is a summary, the diff holds paths, types and capped examples).
        DoctorStatus status = DoctorStatus.of(findings);
        DoctorReportEntity row = new DoctorReportEntity(reportId, sourceId, createdAt, trigger.name(), status.name());
        row.setFindings(findings.stream().map(f -> json.convertValue(f, MAP)).toList());
        row.setDiff(diff == null ? null : json.convertValue(diff, MAP));
        row.setProbe(probe.summary);
        row.setFixture(fixture.map(DoctorService::fixtureMap).orElse(null));
        row.setLlm(llmBlock);
        row.setAdapterVersion(version.version());
        reports.save(row);
        log.info("Doctor report {} for {} ({}): {}, {} findings, top {}", reportId, sourceId, trigger, status,
                findings.size(), findings.isEmpty() ? "none" : findings.get(0).code());
        return view(row);
    }

    private DiagnosisInput.BudgetFacts budgetFacts(String sourceId, SourceHealthView h) {
        Budget budget = sources.effectiveBudget(sourceId);
        if (budget == null || budget.perMinute() < 1 || budget.perDay() < 1) {
            return null;
        }
        boolean exhausted;
        try {
            exhausted = budgets.status(sourceId).isExhausted();
        } catch (RuntimeException e) {
            exhausted = false;
        }
        return new DiagnosisInput.BudgetFacts(budget.perMinute(), budget.perDay(), h.budgetUsedToday(),
                h.requestsLastHour(), exhausted);
    }

    /** What the probe step produced: facts for the rules, a summary to store, and the body held in memory only. */
    private record Probe(DiagnosisInput.ProbeFacts facts, Map<String, Object> summary, String body) {}

    private Probe probe(SourceDescriptor descriptor, DoctorOptions options) {
        String id = descriptor.id();
        if (!options.probe()) {
            return skippedProbe("not-requested");
        }
        if (!descriptor.mode().allowsUpstreamRequests()) {
            return skippedProbe("no-upstream");
        }
        if (backoff.isInBackoff(id)) {
            return skippedProbe("backoff");
        }
        Budget budget = sources.effectiveBudget(id);
        if (budget != null && budget.perMinute() >= 1 && budget.perDay() >= 1) {
            try {
                if (budgets.status(id).isExhausted()) {
                    return skippedProbe("budget");
                }
            } catch (RuntimeException e) {
                log.debug("Budget status of {} unavailable; probing anyway", id, e);
            }
        }
        ProbeService.Capture capture;
        try {
            capture = probes.capture(id, PROBE_MAX_BYTES, PROBE_STREAM_SECONDS);
        } catch (BudgetExceededProblem e) {
            return skippedProbe("budget");
        } catch (ConflictException e) {
            return skippedProbe("no-upstream");
        }
        List<RecordedExchange> exchanges = capture.exchanges();
        RecordedExchange last = null;
        for (RecordedExchange e : exchanges) {
            if (e.status() > 0 || last == null) {
                last = e;
            }
        }
        List<DiagnosisInput.Redirect> redirects = new ArrayList<>();
        for (RecordedExchange e : exchanges) {
            if (e.status() >= 300 && e.status() < 400 && e.status() != 304) {
                String location = header(e, "location");
                redirects.add(new DiagnosisInput.Redirect(e.status(), host(e.redactedUrl()), location,
                        location == null ? null : host(location)));
            }
        }
        String body = last == null ? null : last.body();
        if (body != null && body.endsWith(RequestRecorder.TRUNCATION_MARKER)) {
            body = body.substring(0, body.length() - RequestRecorder.TRUNCATION_MARKER.length());
        }
        Integer httpStatus = last == null || last.status() <= 0 ? null : last.status();
        String parseError = capture.result().parseError();
        int records = capture.result().records().size();
        DiagnosisInput.ProbeFacts facts = new DiagnosisInput.ProbeFacts(false, null, httpStatus,
                last == null ? null : last.contentType(),
                body == null ? null : body.substring(0, Math.min(BODY_HEAD, body.length())), parseError, records,
                redirects);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("skipped", false);
        summary.put("status", parseError != null ? "parse-error"
                : httpStatus == null ? "failed" : httpStatus >= 400 ? "http-error" : "ok");
        summary.put("httpStatus", httpStatus);
        summary.put("contentType", last == null ? null : last.contentType());
        summary.put("bytes", last == null ? null : last.bytes());
        summary.put("url", last == null ? null : last.redactedUrl());
        summary.put("durationMs", last == null || last.duration() == null ? null : last.duration().toMillis());
        summary.put("requests", exchanges.size());
        summary.put("records", records);
        if (last != null && last.failure() != null) {
            summary.put("error", StructureExtractor.cap(last.failure(), 300));
        }
        return new Probe(facts, summary, body);
    }

    private static Probe skippedProbe(String reason) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("skipped", true);
        summary.put("skipReason", reason);
        return new Probe(DiagnosisInput.ProbeFacts.skipped(reason), summary, null);
    }

    private static String header(RecordedExchange e, String name) {
        for (Map.Entry<String, List<String>> h : e.responseHeaders().entrySet()) {
            if (h.getKey().equalsIgnoreCase(name) && !h.getValue().isEmpty()) {
                return h.getValue().get(0);
            }
        }
        return null;
    }

    /** The host of a URL, or null when it has none (a relative location) or cannot be parsed. */
    static String host(String url) {
        if (url == null) {
            return null;
        }
        try {
            return URI.create(url.strip()).getHost();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Map<String, Object> fixtureMap(FixtureCatalog.FixtureFile f) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("file", f.file());
        out.put("capturedAt", f.capturedAt());
        out.put("sha256", f.sha256());
        out.put("truncated", f.truncated());
        return out;
    }

    /** The descriptor summary the LLM sees. */
    static Map<String, Object> descriptorSummary(SourceDescriptor d, FixtureCatalog.FixtureFile fixture) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", d.id());
        out.put("name", d.name());
        out.put("mode", d.mode().wireValue());
        out.put("kinds", d.kinds());
        out.put("homepage", d.homepage());
        out.put("zeroResultsNormal", d.zeroResultsNormal());
        out.put("fixtureCapturedAt", fixture == null ? null : fixture.capturedAt());
        return out;
    }

    /** The API view of a row. */
    DoctorReportView view(DoctorReportEntity row) {
        List<DoctorFinding> findings = new ArrayList<>();
        if (row.getFindings() != null) {
            for (Map<String, Object> f : row.getFindings()) {
                try {
                    findings.add(json.convertValue(f, DoctorFinding.class));
                } catch (RuntimeException e) {
                    log.debug("Unreadable finding in doctor report {}", row.getId(), e);
                }
            }
        }
        return new DoctorReportView(row.getId(), row.getSourceId(), row.getCreatedAt(),
                DoctorTrigger.valueOf(row.getTrigger()), DoctorStatus.valueOf(row.getStatus()), findings,
                row.getDiff(), row.getProbe(), row.getFixture(), row.getLlm(), row.getAdapterVersion());
    }
}
