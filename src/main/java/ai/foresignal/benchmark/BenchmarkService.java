package ai.foresignal.benchmark;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.backtest.BacktestJobEntity;
import ai.foresignal.backtest.BacktestJobService;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.benchmark.BenchmarkResults.ComponentResult;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioMetrics;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioResult;
import ai.foresignal.benchmark.BenchmarkResults.SuiteParams;
import ai.foresignal.benchmark.component.ComponentBenchmark;
import ai.foresignal.benchmark.scenario.ScenarioType;
import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.security.Ulids;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
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
import org.springframework.transaction.annotation.Transactional;

/**
 * Benchmark runs and the historical corpus (prompt 123). A run is a 103 job of type {@value #TYPE}: the synthetic
 * suite ({@link SyntheticSuite}), the historical corpus ({@link HistoricalSuite}) or both, over the selected
 * components, checked against {@link BenchmarkThresholds}. Only {@code benchmark_run} and {@code benchmark_case} rows
 * are written (plus the job row and the run's 116 manifest); no finding, event, situation, forecast or hypothesis is
 * (BEN-1). One run at a time: 409 {@code BENCHMARK_IN_PROGRESS} while one is queued or running.
 */
@Service
public class BenchmarkService {

    private static final Logger log = LoggerFactory.getLogger(BenchmarkService.class);

    /** The 103 job type. */
    public static final String TYPE = "benchmark";

    /** The harness version recorded in manifests. */
    public static final String VERSION = "1.0.0";

    /** The harness name recorded in manifests. */
    public static final String NAME = "benchmark-harness";

    private final BenchmarkRunRepository runs;
    private final BenchmarkCaseRepository cases;
    private final BacktestJobService jobs;
    private final ObjectProvider<ComponentBenchmark> components;
    private final BenchmarkThresholds thresholds;
    private final HistoricalSuite historical;
    private final ObjectProvider<ManifestService> manifests;
    private final BenchmarkGate gate;
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the service. */
    public BenchmarkService(BenchmarkRunRepository runs, BenchmarkCaseRepository cases, BacktestJobService jobs,
            ObjectProvider<ComponentBenchmark> components, BenchmarkThresholds thresholds, HistoricalSuite historical,
            ObjectProvider<ManifestService> manifests, BenchmarkGate gate, NamedParameterJdbcTemplate jdbc,
            Clock clock) {
        this.runs = runs;
        this.cases = cases;
        this.jobs = jobs;
        this.components = components;
        this.thresholds = thresholds;
        this.historical = historical;
        this.manifests = manifests;
        this.gate = gate;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /** The component benchmarks in the documented order. */
    public List<ComponentBenchmark> components() {
        Map<String, ComponentBenchmark> byName = new LinkedHashMap<>();
        for (ComponentBenchmark c : components.orderedStream().toList()) {
            byName.put(c.name(), c);
        }
        List<ComponentBenchmark> out = new ArrayList<>();
        for (String name : BenchmarkInputs.COMPONENTS) {
            if (byName.containsKey(name)) {
                out.add(byName.remove(name));
            }
        }
        out.addAll(byName.values());
        return out;
    }

    /** Each component's current version. */
    public Map<String, Object> componentVersions() {
        Map<String, Object> out = new LinkedHashMap<>();
        for (ComponentBenchmark c : components()) {
            out.put(c.name(), c.version());
        }
        return out;
    }

    /**
     * Queues a run.
     *
     * @param request the validated request
     * @param createdBy the principal, or {@code scheduler}
     * @param trigger {@code manual}, {@code agent}, {@code schedule} or {@code version-change}
     * @return the queued run
     * @throws ConflictException 409 {@code BENCHMARK_IN_PROGRESS} while a run is queued or running
     */
    @Transactional
    public BenchmarkRunEntity submit(BenchmarkInputs.RunRequest request, String createdBy, String trigger) {
        List<String> active = inProgress();
        if (!active.isEmpty()) {
            throw new ConflictException(ErrorCodes.BENCHMARK_IN_PROGRESS, "Benchmark run " + active.get(0)
                    + " is still queued or running.");
        }
        SuiteParams params = thresholds.suite(request.seed());
        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("suite", request.suite());
        definition.put("components", request.components());
        definition.putAll(params.toMap());
        BacktestJobEntity job = jobs.submitRunnerJob(TYPE, null, definition, null);
        BenchmarkRunEntity run = new BenchmarkRunEntity(job.getId(), request.suite(), clock.instant());
        run.setSuiteVersion(ScenarioType.SUITE_VERSION);
        run.setScorerVersion(BenchmarkScorer.NAME + " " + BenchmarkScorer.VERSION);
        run.setThresholdsVersion(thresholds.version());
        run.setSeed(params.seed());
        run.setParams(params.toMap());
        run.setComponents(request.components());
        run.setComponentVersions(componentVersions());
        run.setTrigger(trigger);
        run.setCreatedBy(createdBy);
        return runs.save(run);
    }

    /** The runs whose job is queued or running. */
    public List<String> inProgress() {
        return jdbc.queryForList("SELECT r.id FROM benchmark_run r JOIN backtest_job j ON j.id = r.id WHERE"
                + " r.finished_at IS NULL AND j.status IN ('queued', 'running') ORDER BY r.id", Map.of(),
                String.class);
    }

    /**
     * Runs a queued run (the job body).
     *
     * @param job the claimed job
     * @param monitor cancellation and progress
     * @return the job result
     */
    public Map<String, Object> run(BacktestJobEntity job, JobMonitor monitor) {
        Map<String, Object> d = job.getDefinition();
        BenchmarkRunEntity run = runs.findById(job.getId()).orElseGet(() -> {
            BenchmarkRunEntity created = new BenchmarkRunEntity(job.getId(), String.valueOf(d.get("suite")),
                    clock.instant());
            created.setSuiteVersion(ScenarioType.SUITE_VERSION);
            created.setScorerVersion(BenchmarkScorer.NAME + " " + BenchmarkScorer.VERSION);
            created.setTrigger("manual");
            return created;
        });
        run.setStartedAt(clock.instant());
        run = runs.save(run);
        try {
            SuiteParams params = new SuiteParams(((Number) d.getOrDefault("seed", thresholds.suite(null).seed()))
                    .longValue(), ((Number) d.getOrDefault("replicates", 10)).intValue(),
                    ((Number) d.getOrDefault("minBinCount", 100)).intValue());
            List<String> names = d.get("components") instanceof List<?> l ? l.stream().map(String::valueOf).toList()
                    : BenchmarkInputs.COMPONENTS;
            List<ComponentResult> results = execute(String.valueOf(d.get("suite")), new LinkedHashSet<>(names),
                    params, monitor);
            Verdict status = results.isEmpty() ? Verdict.WARN
                    : Verdict.worst(results.stream().map(ComponentResult::status).toList());
            List<Map<String, Object>> views = new ArrayList<>();
            for (ComponentResult r : results) {
                views.add(r.toMap());
            }
            run.setResults(views);
            run.setStatus(status.wire());
            run.setSeed(params.seed());
            run.setParams(params.toMap());
            run.setComponents(new ArrayList<>(names));
            run.setThresholdsVersion(thresholds.version());
            run.setComponentVersions(componentVersions());
            if (results.isEmpty()) {
                run.setError("Nothing was measured: no historical case expects anything of the selected"
                        + " components.");
            }
            run.setManifestId(manifest(run));
            run.setFinishedAt(clock.instant());
            runs.save(run);
            gate.invalidate();
            log.info("Benchmark run {} ({}): {} over {} component(s)", run.getId(), run.getSuite(), status.wire(),
                    results.size());
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("runId", run.getId());
            out.put("status", status.wire());
            out.put("components", results.size());
            return out;
        } catch (RuntimeException e) {
            run.setError(e instanceof JobMonitor.Canceled ? "canceled" : String.valueOf(e.getMessage() == null ? e
                    : e.getMessage()));
            run.setFinishedAt(clock.instant());
            runs.save(run);
            throw e;
        }
    }

    /**
     * Runs the suites over the named components (pure apart from the historical corpus reads).
     *
     * @param suite {@code synthetic}, {@code historical} or {@code all}
     * @param names the component names
     * @param params the seed and parameters
     * @param monitor cancellation and progress
     * @return one result per component that measured anything
     */
    public List<ComponentResult> execute(String suite, Set<String> names, SuiteParams params, JobMonitor monitor) {
        List<ComponentBenchmark> selected = new ArrayList<>();
        for (ComponentBenchmark c : components()) {
            if (names.contains(c.name())) {
                selected.add(c);
            }
        }
        boolean synthetic = !"historical".equals(suite);
        boolean corpus = !"synthetic".equals(suite);
        Map<String, ComponentResult> byName = new LinkedHashMap<>();
        if (synthetic) {
            for (ComponentResult r : SyntheticSuite.run(selected, params, thresholds, monitor, 0,
                    corpus ? 0.8 : 1.0)) {
                byName.put(r.component(), r);
            }
        }
        if (corpus) {
            Map<String, List<ScenarioMetrics>> cases = historical.run(names, monitor, synthetic ? 0.8 : 0, 1.0);
            for (ComponentBenchmark c : selected) {
                List<ScenarioMetrics> metrics = cases.get(c.name());
                if (metrics == null || metrics.isEmpty()) {
                    continue;
                }
                List<ScenarioResult> scenarios = new ArrayList<>();
                ComponentResult existing = byName.get(c.name());
                if (existing != null) {
                    scenarios.addAll(existing.scenarios());
                }
                for (ScenarioMetrics m : metrics) {
                    scenarios.add(thresholds.evaluate(c.name(), m));
                }
                byName.put(c.name(), new ComponentResult(c.name(), c.version(), scenarios, List.of()));
            }
        }
        List<ComponentResult> out = new ArrayList<>();
        for (ComponentBenchmark c : selected) {
            if (byName.containsKey(c.name())) {
                out.add(byName.get(c.name()));
            }
        }
        return out;
    }

    private String manifest(BenchmarkRunEntity run) {
        ManifestService service = manifests.getIfAvailable();
        if (service == null) {
            return null;
        }
        try {
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("suiteVersion", run.getSuiteVersion());
            config.put("scorerVersion", run.getScorerVersion());
            config.put("thresholdsVersion", run.getThresholdsVersion());
            config.put("params", run.getParams());
            config.put("componentVersions", run.getComponentVersions());
            Map<String, Object> query = new LinkedHashMap<>();
            query.put("suite", run.getSuite());
            query.put("components", run.getComponents());
            query.put("runId", run.getId());
            return service.record(ManifestRequest.of(NAME, VERSION).config(config)
                    .registries("benchmark-thresholds.json").query(query));
        } catch (RuntimeException e) {
            log.warn("Could not record the manifest of benchmark run {}: {}", run.getId(), e.toString());
            return null;
        }
    }

    // ------------------------------------------------------------------------- reads

    /**
     * A page of runs or cases.
     *
     * @param items the items
     * @param nextCursor the next cursor, or null
     * @param <T> the item type
     */
    public record Page<T>(List<T> items, String nextCursor) {}

    /** Runs newest first, optionally filtered. */
    public Page<BenchmarkRunEntity> list(String suite, String status, String cursor, int limit) {
        StringBuilder sql = new StringBuilder("SELECT id FROM benchmark_run WHERE 1 = 1");
        MapSqlParameterSource p = new MapSqlParameterSource("limit", limit + 1);
        if (suite != null) {
            sql.append(" AND suite = :suite");
            p.addValue("suite", suite);
        }
        if (status != null) {
            sql.append(" AND status = :status");
            p.addValue("status", status);
        }
        String after = decode(cursor);
        if (after != null) {
            sql.append(" AND id < :after");
            p.addValue("after", after);
        }
        sql.append(" ORDER BY id DESC LIMIT :limit");
        List<String> ids = jdbc.queryForList(sql.toString(), p, String.class);
        List<BenchmarkRunEntity> items = new ArrayList<>();
        for (int i = 0; i < ids.size() && i < limit; i++) {
            runs.findById(ids.get(i)).ifPresent(items::add);
        }
        String next = ids.size() > limit && !items.isEmpty() ? encode(items.get(items.size() - 1).getId()) : null;
        return new Page<>(items, next);
    }

    /**
     * One run.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_BENCHMARK_RUN}
     */
    public BenchmarkRunEntity get(String id) {
        return runs.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_BENCHMARK_RUN,
                "No benchmark run '" + id + "'."));
    }

    /** The job of a run, or null when it was purged. */
    public BacktestJobEntity job(String id) {
        try {
            return jobs.get(id);
        } catch (NotFoundException e) {
            return null;
        }
    }

    /** The {@code BenchmarkStatus} object. */
    public Map<String, Object> status() {
        return gate.view();
    }

    // ------------------------------------------------------------------------- cases

    /** Cases newest first, optionally of one source. */
    public Page<BenchmarkCaseEntity> listCases(String source, String cursor, int limit) {
        StringBuilder sql = new StringBuilder("SELECT id FROM benchmark_case WHERE 1 = 1");
        MapSqlParameterSource p = new MapSqlParameterSource("limit", limit + 1);
        if (source != null) {
            sql.append(" AND source = :source");
            p.addValue("source", source);
        }
        String after = decode(cursor);
        if (after != null) {
            sql.append(" AND id < :after");
            p.addValue("after", after);
        }
        sql.append(" ORDER BY id DESC LIMIT :limit");
        List<String> ids = jdbc.queryForList(sql.toString(), p, String.class);
        List<BenchmarkCaseEntity> items = new ArrayList<>();
        for (int i = 0; i < ids.size() && i < limit; i++) {
            cases.findById(ids.get(i)).ifPresent(items::add);
        }
        String next = ids.size() > limit && !items.isEmpty() ? encode(items.get(items.size() - 1).getId()) : null;
        return new Page<>(items, next);
    }

    /**
     * Adds an operator or agent case.
     *
     * @param input the validated case
     * @param source {@code operator} or {@code agent}
     * @param actor who added it
     * @return the case
     */
    @Transactional
    public BenchmarkCaseEntity createCase(BenchmarkInputs.CaseInput input, String source, String actor) {
        Instant now = clock.instant();
        BenchmarkCaseEntity c = new BenchmarkCaseEntity(Ulids.next(now), input.name(), input.windowFrom(),
                input.windowTo(), source, now);
        c.setDescription(input.description());
        c.setRegionCodes(input.regionCodes());
        c.setExpectations(input.expectations());
        c.setCreatedBy(actor);
        return cases.save(c);
    }

    /**
     * Deletes a case; past run results keep their copy.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_BENCHMARK_CASE}
     */
    @Transactional
    public void deleteCase(String id) {
        BenchmarkCaseEntity c = cases.findById(id).orElseThrow(() -> new NotFoundException(
                ErrorCodes.UNKNOWN_BENCHMARK_CASE, "No benchmark case '" + id + "'."));
        cases.delete(c);
    }

    private static String encode(String id) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(id.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String id = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            if (Ulids.isUlid(id)) {
                return id;
            }
        } catch (IllegalArgumentException e) {
            // fall through
        }
        throw ValidationException.forField("cursor", "is not a cursor of this list");
    }
}
