package ai.foresignal.api;

import ai.foresignal.backtest.AlertQualityService;
import ai.foresignal.backtest.BacktestJobService;
import ai.foresignal.backtest.TuningSuggester;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.quality.RuleTuningLogEntity;
import ai.foresignal.quality.SuggestionExplainer;
import ai.foresignal.quality.TuningSuggestionEntity;
import ai.foresignal.quality.TuningWorkflowService;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Rule quality and tuning suggestions (prompt 103), and the tuning workflow of prompt 115: persisted suggestions,
 * {@code applyTuningSuggestion}, {@code dismissTuningSuggestion}, {@code undoTuning} and {@code listTuningLog}.
 */
@RestController
@Tag(name = "Analysis")
@RequestMapping("/v1/triggers")
public class RuleAnalysisController {

    private final AlertQualityService quality;
    private final TuningSuggester suggester;
    private final BacktestJobService jobs;
    private final TuningWorkflowService tuning;
    private final SuggestionExplainer explainer;
    private final tools.jackson.databind.json.JsonMapper json;
    private final java.time.Clock clock;

    /** Creates the controller. */
    public RuleAnalysisController(AlertQualityService quality, TuningSuggester suggester, BacktestJobService jobs,
            TuningWorkflowService tuning, SuggestionExplainer explainer, tools.jackson.databind.json.JsonMapper json,
            java.time.Clock clock) {
        this.quality = quality;
        this.suggester = suggester;
        this.jobs = jobs;
        this.tuning = tuning;
        this.explainer = explainer;
        this.json = json;
        this.clock = clock;
    }

    /** {@code getTriggerQuality}, with the 115 signals, signal score and {@code daily[]} trend. */
    @GetMapping("/{ruleId}/quality")
    @Operation(operationId = "getTriggerQuality", tags = "Analysis")
    public Map<String, Object> getTriggerQuality(@PathVariable String ruleId,
            @RequestParam(required = false) String since) {
        Instant from = since == null ? clock.instant().minus(java.time.Duration.ofDays(30))
                : QueryParams.instant("since", since);
        Map<String, Object> out = quality.forRule(rule(ruleId), from);
        out.put("since", from.toString());
        return out;
    }

    /**
     * {@code getTriggerSuggestions}: generates and persists the suggestions (prompt 115), or with {@code status}
     * lists the persisted ones in that status. {@code explain=true} adds a plain-language summary per suggestion
     * for an authenticated caller with a chat provider; {@code simulate=true} (agent) submits a backtest.
     */
    @GetMapping("/{ruleId}/suggestions")
    @Operation(operationId = "getTriggerSuggestions", tags = "Analysis")
    public Map<String, Object> getTriggerSuggestions(@PathVariable String ruleId,
            @RequestParam(required = false) String since,
            @RequestParam(required = false) String simulate,
            @RequestParam(required = false) String explain,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        UUID id = rule(ruleId);
        Instant from = since == null ? clock.instant().minus(java.time.Duration.ofDays(30))
                : QueryParams.instant("since", since);
        boolean simulateNow = QueryParams.bool("simulate", simulate, false);
        boolean explainNow = QueryParams.bool("explain", explain, false);
        List<TuningSuggestionEntity> records = status == null ? tuning.generate(id, from) : tuning.list(id, status);
        java.util.Optional<CurrentToken> token = CurrentToken.of(request);
        List<Map<String, Object>> suggestions = new ArrayList<>();
        for (TuningSuggestionEntity record : records) {
            Map<String, Object> view = TuningWorkflowService.view(record);
            // The LLM summary spends budget, so it needs a token even while reads are open (SEC-5).
            if (explainNow && token.isPresent()) {
                explainer.explain(view).ifPresent(text -> view.put("explanation", text));
            }
            suggestions.add(view);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ruleId", id.toString());
        out.put("since", from.toString());
        out.put("suggesterVersion", TuningSuggester.VERSION);
        out.put("suggestions", suggestions);
        out.put("simulationJobId", null);
        if (simulateNow && token.map(c -> c.scope() != null && c.scope().includes(Scope.AGENT)).orElse(false)) {
            // The patched variants (condition patches) run side by side with the rule over the same range.
            List<Map<String, Object>> variants = new ArrayList<>();
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
            out.put("simulationJobId", job.getId());
            out.put("jobId", job.getId());
        }
        return out;
    }

    /** {@code applyTuningSuggestion}: applies the patch through the normal rule update and logs it. */
    @PostMapping("/{ruleId}/suggestions/{suggestionId}/apply")
    @Operation(operationId = "applyTuningSuggestion", tags = "Analysis")
    public Map<String, Object> applyTuningSuggestion(@PathVariable String ruleId, @PathVariable String suggestionId,
            @RequestBody(required = false) JsonNode body, HttpServletRequest request) {
        RuleTuningLogEntity entry = tuning.apply(rule(ruleId), suggestionId, bodyText(body, "note"),
                ExercisesController.actor(request));
        return TuningWorkflowService.view(entry);
    }

    /** {@code dismissTuningSuggestion}. */
    @PostMapping("/{ruleId}/suggestions/{suggestionId}/dismiss")
    @Operation(operationId = "dismissTuningSuggestion", tags = "Analysis")
    public Map<String, Object> dismissTuningSuggestion(@PathVariable String ruleId, @PathVariable String suggestionId,
            @RequestBody(required = false) JsonNode body, HttpServletRequest request) {
        return TuningWorkflowService.view(tuning.dismiss(rule(ruleId), suggestionId, bodyText(body, "reason"),
                ExercisesController.actor(request)));
    }

    /** {@code undoTuning}. */
    @PostMapping("/{ruleId}/tuning-log/{logId}/undo")
    @Operation(operationId = "undoTuning", tags = "Analysis")
    public Map<String, Object> undoTuning(@PathVariable String ruleId, @PathVariable String logId,
            HttpServletRequest request) {
        return TuningWorkflowService.view(tuning.undo(rule(ruleId), logId, ExercisesController.actor(request)));
    }

    /** {@code listTuningLog}: newest first. */
    @GetMapping("/{ruleId}/tuning-log")
    @Operation(operationId = "listTuningLog", tags = "Analysis")
    public Map<String, Object> listTuningLog(@PathVariable String ruleId,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit) {
        int size = QueryParams.intInRange("limit", limit, 1, 500, 100);
        List<RuleTuningLogEntity> page = tuning.log(rule(ruleId), ExercisesController.decode(cursor), size + 1);
        String next = null;
        if (page.size() > size) {
            page = page.subList(0, size);
            next = ExercisesController.encode(page.getLast().getId());
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.stream().map(TuningWorkflowService::view).toList());
        out.put("nextCursor", next);
        return out;
    }

    private static UUID rule(String ruleId) {
        try {
            return UUID.fromString(ruleId);
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField("ruleId", "must be a rule id (UUID)");
        }
    }

    /**
     * The optional string member {@code field} of a {@code TuningSuggestionApply} or {@code TuningSuggestionDismiss}
     * body (null for none), at most {@value TuningWorkflowService#MAX_NOTE} characters.
     *
     * @throws ValidationException when the body is not an object or the member is not a string, or too long
     */
    public static String bodyText(JsonNode body, String field) {
        if (body == null || body.isNull() || body.isMissingNode()) {
            return null;
        }
        if (!body.isObject()) {
            throw new ValidationException("The body must be a JSON object.");
        }
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString()) {
            throw ValidationException.forField(field, "must be a string");
        }
        if (node.asString().length() > TuningWorkflowService.MAX_NOTE) {
            throw ValidationException.forField(field, "must be at most " + TuningWorkflowService.MAX_NOTE
                    + " characters");
        }
        return node.asString();
    }
}
