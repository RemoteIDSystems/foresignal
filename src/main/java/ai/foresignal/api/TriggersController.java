package ai.foresignal.api;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.mapper.TriggerMapper;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.trigger.TriggerBacktester;
import ai.foresignal.trigger.TriggerRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The {@code Triggers} operations (prompt 017): {@code listTriggers}, {@code getTrigger},
 * {@code createTrigger}, {@code updateTrigger}, {@code deleteTrigger}, {@code testTrigger},
 * {@code snoozeTrigger} and {@code unsnoozeTrigger}.
 *
 * <p>Reads need {@code read}; every write and the backtest need {@code agent} ({@code AccessPolicy}).
 * Bodies are taken as JSON trees and validated by {@code RuleValidator}, so every field problem is
 * reported in one {@code 400} with field errors. A rule id that is not a UUID is simply not found.
 */
@RestController
@Tag(name = "Triggers")
@RequestMapping("/v1/triggers")
public class TriggersController {

    private final TriggerRuleService rules;
    private final TriggerBacktester backtester;
    private final TriggerMapper mapper;
    private final RecordMapper records;

    /** Creates the controller. */
    public TriggersController(
            TriggerRuleService rules, TriggerBacktester backtester, TriggerMapper mapper, RecordMapper records) {
        this.rules = rules;
        this.backtester = backtester;
        this.mapper = mapper;
        this.records = records;
    }

    /** {@code listTriggers}. */
    @GetMapping
    @Operation(operationId = "listTriggers", tags = "Triggers")
    public List<Map<String, Object>> listTriggers(
            @RequestParam(required = false) String tag, @RequestParam(required = false) String enabled) {
        Boolean enabledFilter = enabled == null ? null : QueryParams.bool("enabled", enabled, true);
        return rules.list(tag, enabledFilter).stream().map(mapper::rule).toList();
    }

    /** {@code getTrigger}. */
    @GetMapping("/{ruleId}")
    @Operation(operationId = "getTrigger", tags = "Triggers")
    public Map<String, Object> getTrigger(@PathVariable String ruleId) {
        return mapper.rule(rules.get(id(ruleId)));
    }

    /** {@code createTrigger}: 201 with {@code Location}. */
    @PostMapping
    @Operation(operationId = "createTrigger", tags = "Triggers")
    public ResponseEntity<Map<String, Object>> createTrigger(@RequestBody JsonNode body) {
        TriggerRuleService.RuleView view = rules.create(body);
        return ResponseEntity.created(URI.create("/v1/triggers/" + view.rule().getId())).body(mapper.rule(view));
    }

    /** {@code updateTrigger}: a full replace. */
    @PutMapping("/{ruleId}")
    @Operation(operationId = "updateTrigger", tags = "Triggers")
    public Map<String, Object> updateTrigger(@PathVariable String ruleId, @RequestBody JsonNode body) {
        return mapper.rule(rules.update(id(ruleId), body));
    }

    /** {@code deleteTrigger}: 204; the rule's alerts are kept. */
    @DeleteMapping("/{ruleId}")
    @Operation(operationId = "deleteTrigger", tags = "Triggers")
    public ResponseEntity<Void> deleteTrigger(@PathVariable String ruleId) {
        rules.delete(id(ruleId));
        return ResponseEntity.noContent().build();
    }

    /** {@code testTrigger}: a backtest that creates nothing. */
    @PostMapping("/{ruleId}/test")
    @Operation(operationId = "testTrigger", tags = "Triggers")
    public Map<String, Object> testTrigger(
            @PathVariable String ruleId,
            @RequestParam(required = false) String since,
            @RequestParam(required = false) String until,
            @RequestParam(required = false) String fromSeq,
            @RequestParam(required = false) String toSeq,
            @RequestParam(required = false) String limit,
            @RequestParam(required = false) String simulate) {
        TriggerBacktester.Request request = new TriggerBacktester.Request(
                QueryParams.nonNegativeLong("fromSeq", fromSeq),
                QueryParams.nonNegativeLong("toSeq", toSeq),
                QueryParams.instant("since", since),
                QueryParams.instant("until", until),
                QueryParams.intInRange("limit", limit, 1, TriggerBacktester.MAX_LIMIT, 100),
                QueryParams.bool("simulate", simulate, false));
        TriggerBacktester.Result result = backtester.test(id(ruleId), request);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ruleId", result.ruleId());
        out.put("evaluatedFrom", result.evaluatedFrom());
        List<Map<String, Object>> matches = new ArrayList<>();
        for (RecordRow row : result.matches()) {
            matches.add(records.record(row, FieldSelection.ALL, RecordMapper.Shape.VERSION));
        }
        out.put("matches", matches);
        out.put("truncated", result.truncated());
        out.put("fromSeq", result.fromSeq());
        out.put("toSeq", result.toSeq());
        out.put("matchCount", result.matchCount());
        out.put("simulated", result.simulated());
        out.put("wouldFire", result.wouldFire());
        out.put("wouldSuppress", result.wouldSuppress());
        out.put("truncatedByRetention", result.truncatedByRetention());
        out.put("enrich", result.enrich());
        out.put("warnings", List.of());
        return out;
    }

    /** {@code snoozeTrigger}: {@code until} or {@code for}, at most 7 days. */
    @PostMapping("/{ruleId}/snooze")
    @Operation(operationId = "snoozeTrigger", tags = "Triggers")
    public Map<String, Object> snoozeTrigger(@PathVariable String ruleId, @RequestBody JsonNode body) {
        return mapper.rule(rules.snooze(id(ruleId), body));
    }

    /** {@code unsnoozeTrigger}: idempotent. */
    @DeleteMapping("/{ruleId}/snooze")
    @Operation(operationId = "unsnoozeTrigger", tags = "Triggers")
    public Map<String, Object> unsnoozeTrigger(@PathVariable String ruleId) {
        return mapper.rule(rules.unsnooze(id(ruleId)));
    }

    /** {@code ruleId} as a UUID; anything else names no rule. */
    static UUID id(String ruleId) {
        try {
            return UUID.fromString(ruleId);
        } catch (IllegalArgumentException e) {
            throw NotFoundException.of("Trigger rule", ruleId);
        }
    }
}
