package ai.foresignal.api;

import ai.foresignal.backtest.AlertQualityService;
import ai.foresignal.backtest.BacktestJobService;
import ai.foresignal.backtest.TuningSuggester;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Rule quality and tuning suggestions (prompt 103). */
@RestController
@Tag(name = "Analysis")
@RequestMapping("/v1/triggers")
public class RuleAnalysisController {

    private final AlertQualityService quality;
    private final TuningSuggester suggester;
    private final BacktestJobService jobs;

    /** Creates the controller. */
    public RuleAnalysisController(AlertQualityService quality, TuningSuggester suggester, BacktestJobService jobs) {
        this.quality = quality;
        this.suggester = suggester;
        this.jobs = jobs;
    }

    /** {@code getTriggerQuality}. */
    @GetMapping("/{ruleId}/quality")
    @Operation(operationId = "getTriggerQuality", tags = "Analysis")
    public Map<String, Object> getTriggerQuality(@PathVariable String ruleId,
            @RequestParam(required = false) String since) {
        Instant from = since == null ? Instant.now().minus(java.time.Duration.ofDays(30))
                : QueryParams.instant("since", since);
        return quality.forRule(UUID.fromString(ruleId), from);
    }

    /** {@code getTriggerSuggestions}. */
    @GetMapping("/{ruleId}/suggestions")
    @Operation(operationId = "getTriggerSuggestions", tags = "Analysis")
    public Map<String, Object> getTriggerSuggestions(@PathVariable String ruleId,
            @RequestParam(required = false) String since,
            @RequestParam(required = false) String simulate,
            HttpServletRequest request) {
        Instant from = since == null ? Instant.now().minus(java.time.Duration.ofDays(30))
                : QueryParams.instant("since", since);
        boolean simulateNow = QueryParams.bool("simulate", simulate, false);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("suggestions", suggester.forRule(UUID.fromString(ruleId), from));
        if (simulateNow && CurrentToken.of(request).map(c -> c.scope() != null && c.scope().includes(Scope.AGENT))
                .orElse(false)) {
            var job = jobs.submit("rule", ruleId, null, null, null, null);
            out.put("jobId", job.getId());
        }
        return out;
    }
}
