package ai.foresignal.api;

import ai.foresignal.api.support.QueryParams;
import ai.foresignal.benchmark.BenchmarkCaseEntity;
import ai.foresignal.benchmark.BenchmarkInputs;
import ai.foresignal.benchmark.BenchmarkRunEntity;
import ai.foresignal.benchmark.BenchmarkService;
import ai.foresignal.benchmark.BenchmarkViews;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The analytic benchmark harness (prompt 123; OpenAPI tag {@code Analysis}). Runs, their results and the status per
 * component are reads; starting a run and the historical corpus are {@code agent} ({@code AccessPolicy}). Results are
 * statistics over synthetic scenarios and corpus cases: no record content.
 */
@RestController
@Tag(name = "Analysis")
public class BenchmarkController {

    private final BenchmarkService benchmarks;

    /** Creates the controller. */
    public BenchmarkController(BenchmarkService benchmarks) {
        this.benchmarks = benchmarks;
    }

    /** {@code createBenchmarkRun}: 202 with the queued run; 409 {@code BENCHMARK_IN_PROGRESS}. */
    @PostMapping("/v1/benchmarks/runs")
    @Operation(operationId = "createBenchmarkRun", tags = "Analysis")
    public ResponseEntity<Map<String, Object>> createBenchmarkRun(@RequestBody(required = false) JsonNode body,
            HttpServletRequest request) {
        BenchmarkInputs.RunRequest input = BenchmarkInputs.run(body);
        boolean admin = CurrentToken.of(request).map(t -> t.scope() == Scope.ADMIN).orElse(true);
        BenchmarkRunEntity run = benchmarks.submit(input, actor(request), admin ? "manual" : "agent");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(BenchmarkViews.run(run, benchmarks.job(run.getId())));
    }

    /** {@code listBenchmarkRuns}: newest first. */
    @GetMapping("/v1/benchmarks/runs")
    @Operation(operationId = "listBenchmarkRuns", tags = "Analysis")
    public Map<String, Object> listBenchmarkRuns(@RequestParam(required = false) String suite,
            @RequestParam(required = false) String status, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        String suiteFilter = suite == null || suite.isBlank() ? null
                : QueryParams.oneOf("suite", suite, BenchmarkInputs.SUITES, null);
        String statusFilter = status == null || status.isBlank() ? null
                : QueryParams.oneOf("status", status, List.of("pass", "warn", "fail"), null);
        BenchmarkService.Page<BenchmarkRunEntity> page = benchmarks.list(suiteFilter, statusFilter, cursor,
                QueryParams.intInRange("limit", limit, 1, 500, 100));
        List<Map<String, Object>> items = new ArrayList<>();
        for (BenchmarkRunEntity run : page.items()) {
            items.add(BenchmarkViews.run(run, benchmarks.job(run.getId())));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    /** {@code getBenchmarkRun}: 404 {@code UNKNOWN_BENCHMARK_RUN}. */
    @GetMapping("/v1/benchmarks/runs/{runId}")
    @Operation(operationId = "getBenchmarkRun", tags = "Analysis")
    public Map<String, Object> getBenchmarkRun(@PathVariable String runId) {
        BenchmarkRunEntity run = benchmarks.get(runId);
        return BenchmarkViews.run(run, benchmarks.job(run.getId()));
    }

    /** {@code getBenchmarkStatus}: the latest status per component and what it gates. */
    @GetMapping("/v1/benchmarks/status")
    @Operation(operationId = "getBenchmarkStatus", tags = "Analysis")
    public Map<String, Object> getBenchmarkStatus() {
        return benchmarks.status();
    }

    /** {@code listBenchmarkCases}: newest first. */
    @GetMapping("/v1/benchmarks/cases")
    @Operation(operationId = "listBenchmarkCases", tags = "Analysis")
    public Map<String, Object> listBenchmarkCases(@RequestParam(required = false) String source,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit) {
        String sourceFilter = source == null || source.isBlank() ? null
                : QueryParams.oneOf("source", source, BenchmarkInputs.SOURCES, null);
        BenchmarkService.Page<BenchmarkCaseEntity> page = benchmarks.listCases(sourceFilter, cursor,
                QueryParams.intInRange("limit", limit, 1, 500, 100));
        return BenchmarkViews.casePage(page.items(), page.nextCursor());
    }

    /** {@code createBenchmarkCase}: 201; source {@code operator} for admin tokens, else {@code agent}. */
    @PostMapping("/v1/benchmarks/cases")
    @Operation(operationId = "createBenchmarkCase", tags = "Analysis")
    public ResponseEntity<Map<String, Object>> createBenchmarkCase(@RequestBody(required = false) JsonNode body,
            HttpServletRequest request) {
        BenchmarkInputs.CaseInput input = BenchmarkInputs.caseInput(body);
        boolean admin = CurrentToken.of(request).map(t -> t.scope() == Scope.ADMIN).orElse(true);
        BenchmarkCaseEntity created = benchmarks.createCase(input, admin ? "operator" : "agent", actor(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(BenchmarkViews.benchmarkCase(created));
    }

    /** {@code deleteBenchmarkCase}: 204; 404 {@code UNKNOWN_BENCHMARK_CASE}. */
    @DeleteMapping("/v1/benchmarks/cases/{caseId}")
    @Operation(operationId = "deleteBenchmarkCase", tags = "Analysis")
    public ResponseEntity<Void> deleteBenchmarkCase(@PathVariable String caseId) {
        benchmarks.deleteCase(caseId);
        return ResponseEntity.noContent().build();
    }

    private static String actor(HttpServletRequest request) {
        return CurrentToken.of(request).map(t -> t.id().toString()).orElse("operator");
    }
}
