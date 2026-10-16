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
    private final tools.jackson.databind.json.JsonMapper json;
    private final java.time.Clock clock;

    /** Creates the controller. */
    public RuleAnalysisController(AlertQualityService quality, TuningSuggester suggester, BacktestJobService jobs,
            tools.jackson.databind.json.JsonMapper json, java.time.Clock clock) {
        this.quality = quality;
        this.suggester = suggester;
        this.jobs = jobs;
        this.json = json;
        this.clock = clock;
    }

    /** {@code getTriggerQuality}. */
    @GetMapping("/{ruleId}/quality")
    @Operation(operationId = "getTriggerQuality", tags = "Analysis")
    public Map<String, Object> getTriggerQuality(@PathVariable String ruleId,
            @RequestParam(required = false) String since) {
        Instant from = since == null ? clock.instant().minus(java.time.Duration.ofDays(30))
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
        Instant from = since == null ? clock.instant().minus(java.time.Duration.ofDays(30))
                : QueryParams.instant("since", since);
        boolean simulateNow = QueryParams.bool("simulate", simulate, false);
        Map<String, Object> out = new LinkedHashMap<>();
        java.util.List<Map<String, Object>> suggestions = suggester.forRule(UUID.fromString(ruleId), from);
        out.put("suggestions", suggestions);
        java.util.Optional<CurrentToken> token = CurrentToken.of(request);
        if (simulateNow && token.map(c -> c.scope() != null && c.scope().includes(Scope.AGENT)).orElse(false)) {
            // The patched variants (condition patches) run side by side with the rule over the same range.
            java.util.List<Map<String, Object>> variants = new java.util.ArrayList<>();
            for (Map<String, Object> suggestion : suggestions) {
                if (suggestion.get("patch") instanceof Map<?, ?> patch && patch.get("conditions") != null) {
                    variants.add(Map.of("name", String.valueOf(suggestion.get("id")), "conditions",
                            patch.get("conditions")));
                }
            }
            Instant now = clock.instant();
            Instant start = from.isBefore(now.minus(java.time.Duration.ofDays(BacktestJobService.MAX_DAYS)))
                    ? now.minus(java.time.Duration.ofDays(BacktestJobService.MAX_DAYS)) : from;
            tools.jackson.databind.node.ObjectNode range = json.createObjectNode();
            range.put("from", start.toString());
            range.put("to", now.toString());
            var job = jobs.submit("rule", ruleId, null, range, variants, token.get().id().toString());
            out.put("jobId", job.getId());
        }
        return out;
    }
}
