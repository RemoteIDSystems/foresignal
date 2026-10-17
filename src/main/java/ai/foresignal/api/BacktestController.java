package ai.foresignal.api;

import ai.foresignal.backtest.BacktestJobEntity;
import ai.foresignal.backtest.BacktestJobService;
import ai.foresignal.backtest.BacktestViews;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.security.CurrentToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.JsonNode;

/** Backtests and generic jobs (prompt 103). */
@RestController
@Tag(name = "Analysis")
@RequestMapping("/v1")
public class BacktestController {

    private final BacktestJobService jobs;
    private final tools.jackson.databind.json.JsonMapper json;

    /** Creates the controller. */
    public BacktestController(BacktestJobService jobs, tools.jackson.databind.json.JsonMapper json) {
        this.jobs = jobs;
        this.json = json;
    }

    /** {@code createBacktest}: 202 with the queued job. */
    @PostMapping("/backtests")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(operationId = "createBacktest", tags = "Analysis")
    public Map<String, Object> createBacktest(@RequestBody JsonNode body, HttpServletRequest request) {
        String type = body.path("type").asString("rule");
        String targetId = body.path("targetId").asString(null);
        JsonNode range = body.has("range") ? body.get("range") : body;
        List<Map<String, Object>> variants = body.has("variants") && body.get("variants").isArray()
                ? variantsOf(body.get("variants")) : null;
        String tokenId = CurrentToken.of(request).map(c -> c.id().toString()).orElse(null);
        BacktestJobEntity job = jobs.submit(type, targetId, body.get("definition"), range, variants, tokenId);
        return BacktestViews.job(job);
    }

    private List<Map<String, Object>> variantsOf(JsonNode node) {
        List<Map<String, Object>> out = new java.util.ArrayList<>();
        for (JsonNode item : node) {
            out.add(json.readValue(item.toString(),
                    new tools.jackson.core.type.TypeReference<Map<String, Object>>() {}));
        }
        return out;
    }

    /** {@code listBacktests}. */
    @GetMapping("/backtests")
    @Operation(operationId = "listBacktests", tags = "Analysis")
    public Map<String, Object> listBacktests(@RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        int size = QueryParams.intInRange("limit", limit, 1, 200, 50);
        return BacktestViews.page(jobs.list(status, type, size).stream().map(BacktestViews::job).toList());
    }

    /** {@code getBacktest}. */
    @GetMapping("/backtests/{id}")
    @Operation(operationId = "getBacktest", tags = "Analysis")
    public Map<String, Object> getBacktest(@PathVariable String id) {
        return BacktestViews.job(jobs.get(id));
    }

    /** {@code cancelBacktest}. */
    @DeleteMapping("/backtests/{id}")
    @Operation(operationId = "cancelBacktest", tags = "Analysis")
    public ResponseEntity<Void> cancelBacktest(@PathVariable String id) {
        jobs.cancel(id);
        return ResponseEntity.noContent().build();
    }

    /** {@code getJob}: one path for every job type. */
    @GetMapping("/jobs/{jobId}")
    @Operation(operationId = "getJob", tags = "Analysis")
    public Map<String, Object> getJob(@PathVariable String jobId) {
        return BacktestViews.job(jobs.get(jobId));
    }

    /** {@code cancelJob}. */
    @DeleteMapping("/jobs/{jobId}")
    @Operation(operationId = "cancelJob", tags = "Analysis")
    @ApiResponse(responseCode = "204", description = "Canceled")
    public ResponseEntity<Void> cancelJob(@PathVariable String jobId) {
        jobs.cancel(jobId);
        return ResponseEntity.noContent().build();
    }
}
