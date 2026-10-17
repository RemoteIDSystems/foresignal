package ai.foresignal.api;

import ai.foresignal.api.support.QueryParams;
import ai.foresignal.cycle.AnalysisCycleEntity;
import ai.foresignal.cycle.AssessmentService;
import ai.foresignal.cycle.CycleInputs;
import ai.foresignal.cycle.CycleRunEntity;
import ai.foresignal.cycle.CycleScheduler;
import ai.foresignal.cycle.CycleService;
import ai.foresignal.cycle.MonthlyMaintenance;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.security.restrict.RestrictedRead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The analysis cycle endpoints (prompt 128): {@code listAnalysisCycles} and {@code getAnalysisCycle} (read),
 * {@code createAnalysisCycle}, {@code updateAnalysisCycle}, {@code deleteAnalysisCycle}, {@code listCycleRuns} and
 * {@code getCycleRun} (agent), and {@code runAnalysisCycle} and {@code runAnalysisMaintenance} (agent, always
 * authenticated, 202).
 */
@RestController
@Tag(name = "Assessments")
public class CycleController {

    private final CycleService cycles;
    private final CycleScheduler scheduler;
    private final AssessmentService assessments;
    private final IntelVisibility visibility;

    /** Creates the controller. */
    public CycleController(CycleService cycles, CycleScheduler scheduler, AssessmentService assessments,
            IntelVisibility visibility) {
        this.cycles = cycles;
        this.scheduler = scheduler;
        this.assessments = assessments;
        this.visibility = visibility;
    }

    /** {@code listAnalysisCycles}. */
    @GetMapping("/v1/analysis/cycles")
    @RestrictedRead("cycle definitions carry no record content")
    @Operation(operationId = "listAnalysisCycles", tags = "Assessments")
    public Map<String, Object> listAnalysisCycles(@RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        CycleService.Page page = cycles.list(blank(cursor), QueryParams.intInRange("limit", limit, 1, 500, 100));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items().stream().map(AssessmentService::cycleView).toList());
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    /** {@code createAnalysisCycle}: 201. */
    @PostMapping("/v1/analysis/cycles")
    @Operation(operationId = "createAnalysisCycle", tags = "Assessments")
    @ApiResponse(responseCode = "404", description = "UNKNOWN_ANALYST_PROFILE")
    public ResponseEntity<Map<String, Object>> createAnalysisCycle(@RequestBody JsonNode body) {
        AnalysisCycleEntity cycle = cycles.create(CycleInputs.cycle(body));
        return ResponseEntity.created(URI.create("/v1/analysis/cycles/" + cycle.getId()))
                .body(AssessmentService.cycleView(cycle));
    }

    /** {@code getAnalysisCycle}. */
    @GetMapping("/v1/analysis/cycles/{cycleId}")
    @RestrictedRead("cycle definitions carry no record content")
    @Operation(operationId = "getAnalysisCycle", tags = "Assessments")
    @ApiResponse(responseCode = "404", description = "UNKNOWN_CYCLE")
    public Map<String, Object> getAnalysisCycle(@PathVariable String cycleId) {
        return AssessmentService.cycleView(cycles.get(cycleId));
    }

    /** {@code updateAnalysisCycle}. */
    @PutMapping("/v1/analysis/cycles/{cycleId}")
    @Operation(operationId = "updateAnalysisCycle", tags = "Assessments")
    @ApiResponse(responseCode = "404", description = "UNKNOWN_CYCLE")
    public Map<String, Object> updateAnalysisCycle(@PathVariable String cycleId, @RequestBody JsonNode body) {
        return AssessmentService.cycleView(cycles.replace(cycleId, CycleInputs.cycle(body)));
    }

    /** {@code deleteAnalysisCycle}: 204; runs and assessments are kept. */
    @DeleteMapping("/v1/analysis/cycles/{cycleId}")
    @Operation(operationId = "deleteAnalysisCycle", tags = "Assessments")
    @ApiResponse(responseCode = "409", description = "CYCLE_RUN_IN_PROGRESS")
    public ResponseEntity<Void> deleteAnalysisCycle(@PathVariable String cycleId) {
        cycles.delete(cycleId);
        return ResponseEntity.noContent().build();
    }

    /** {@code runAnalysisCycle}: 202 with the queued run. */
    @PostMapping("/v1/analysis/cycles/{cycleId}/run")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(operationId = "runAnalysisCycle", tags = "Assessments")
    @ApiResponse(responseCode = "409", description = "CYCLE_RUN_IN_PROGRESS, LLM_NOT_CONFIGURED")
    public Map<String, Object> runAnalysisCycle(@PathVariable String cycleId) {
        CycleRunEntity run = scheduler.runNow(cycles.get(cycleId));
        return assessments.runView(run, false);
    }

    /** {@code listCycleRuns}: newest first, packets omitted. */
    @GetMapping("/v1/analysis/cycles/{cycleId}/runs")
    @Operation(operationId = "listCycleRuns", tags = "Assessments")
    public Map<String, Object> listCycleRuns(@PathVariable String cycleId,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit) {
        AnalysisCycleEntity cycle = cycles.get(cycleId);
        AssessmentService.Page<CycleRunEntity> page = assessments.runs(cycle.getId(), blank(cursor),
                QueryParams.intInRange("limit", limit, 1, 500, 100));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items().stream().map(r -> assessments.runView(r, false)).toList());
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    /** {@code getCycleRun}: the run with its packet. */
    @GetMapping("/v1/analysis/runs/{runId}")
    @Operation(operationId = "getCycleRun", tags = "Assessments")
    @ApiResponse(responseCode = "404", description = "UNKNOWN_CYCLE_RUN")
    public Map<String, Object> getCycleRun(@PathVariable String runId, HttpServletRequest request) {
        return assessments.runView(assessments.run(runId, visibility.seesPrivate(request)), true);
    }

    /** {@code runAnalysisMaintenance}: 202 with the started maintenance. */
    @PostMapping("/v1/analysis/maintenance/run")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(operationId = "runAnalysisMaintenance", tags = "Assessments")
    @ApiResponse(responseCode = "409", description = "MAINTENANCE_IN_PROGRESS")
    public Map<String, Object> runAnalysisMaintenance() {
        return MonthlyMaintenance.view(scheduler.maintenanceNow("manual"));
    }

    private static String blank(String raw) {
        return raw == null || raw.isBlank() ? null : raw.strip();
    }
}
