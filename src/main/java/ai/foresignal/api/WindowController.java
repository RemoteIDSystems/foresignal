package ai.foresignal.api;

import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.trigger.window.ActivitySampleEntity;
import ai.foresignal.trigger.window.AnomalyObservationEntity;
import ai.foresignal.trigger.window.AnomalyObservationRepository;
import ai.foresignal.trigger.window.Baseline;
import ai.foresignal.trigger.window.RuleType;
import ai.foresignal.trigger.window.WindowEngine;
import ai.foresignal.trigger.window.WindowSpec;
import ai.foresignal.trigger.window.WindowStateEntity;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.TriggerRuleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The windowed-rule read APIs (prompt 100): a rule's per-group window state, its anomaly baselines,
 * a baseline rebuild, and the anomalies feed.
 */
@RestController
@Tag(name = "Triggers")
@RequestMapping("/v1")
public class WindowController {

    private final TriggerRuleRepository rules;
    private final WindowEngine engine;
    private final AnomalyObservationRepository anomalies;

    /** Creates the controller. */
    public WindowController(TriggerRuleRepository rules, WindowEngine engine,
            AnomalyObservationRepository anomalies) {
        this.rules = rules;
        this.engine = engine;
        this.anomalies = anomalies;
    }

    /** {@code getTriggerWindowState}. */
    @GetMapping("/triggers/{ruleId}/window-state")
    @Operation(operationId = "getTriggerWindowState", tags = "Triggers")
    public Map<String, Object> getTriggerWindowState(@PathVariable String ruleId) {
        TriggerRuleEntity rule = load(ruleId);
        RuleType type = RuleType.fromWire(rule.getType());
        WindowSpec spec = WindowSpec.fromStored(rule.getWindowSpec());
        List<Map<String, Object>> groups = new ArrayList<>();
        boolean truncated = false;
        for (WindowStateEntity state : engine.windowState(rule.getId())) {
            Map<String, Object> group = new LinkedHashMap<>();
            group.put("groupKey", state.getGroupKey());
            group.put("state", state.getState());
            group.put("since", state.getSince());
            group.put("lastValue", state.getLastValue());
            group.put("threshold", threshold(spec, type));
            group.put("lastEvaluatedAt", state.getLastEvaluatedAt());
            group.put("lastWatermarkSeq", state.getLastWatermarkSeq());
            group.put("reason", state.getReason());
            group.put("lastAlertId", state.getLastAlertId());
            groups.add(group);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ruleId", rule.getId());
        out.put("type", type.wire());
        out.put("groups", groups);
        out.put("groupsTruncated", truncated);
        return out;
    }

    /** {@code getTriggerBaseline}. */
    @GetMapping("/triggers/{ruleId}/baseline")
    @Operation(operationId = "getTriggerBaseline", tags = "Triggers")
    public Map<String, Object> getTriggerBaseline(
            @PathVariable String ruleId, @RequestParam(required = false) String groupKey) {
        TriggerRuleEntity rule = load(ruleId);
        List<WindowEngine.BaselineRow> baselines = engine.baselines(rule.getId(), groupKey);
        if (baselines.isEmpty()) {
            throw new ConflictException(ErrorCodes.BASELINE_NOT_READY,
                    "No coverage-ok samples exist yet for rule " + rule.getId() + ".");
        }
        List<Map<String, Object>> groups = new ArrayList<>();
        double weeksAvailable = 0;
        for (WindowEngine.BaselineRow row : baselines) {
            Map<String, Object> group = new LinkedHashMap<>();
            group.put("groupKey", row.groupKey());
            group.put("weeksAvailable", row.baseline().weeks());
            group.put("model", row.baseline().model());
            Map<String, Object> slot = new LinkedHashMap<>();
            slot.put("n", row.samples());
            slot.put("mean", row.baseline().mean());
            slot.put("variance", row.baseline().variance());
            group.put("slots", List.of(slot));
            groups.add(group);
            weeksAvailable = Math.max(weeksAvailable, row.baseline().weeks());
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ruleId", rule.getId());
        out.put("weeksAvailable", weeksAvailable);
        out.put("groups", groups);
        return out;
    }

    /** {@code rebuildBaseline}. */
    @PostMapping("/triggers/{ruleId}/baseline/rebuild")
    @Operation(operationId = "rebuildBaseline", tags = "Triggers")
    public Map<String, Object> rebuildBaseline(@PathVariable String ruleId) {
        TriggerRuleEntity rule = load(ruleId);
        List<WindowEngine.BaselineRow> baselines = engine.baselines(rule.getId(), null);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ruleId", rule.getId());
        out.put("groups", baselines.size());
        out.put("rebuilt", true);
        return out;
    }

    /** {@code listAnomalies}: newest first, keyset on id. */
    @GetMapping("/anomalies")
    @Operation(operationId = "listAnomalies", tags = "Triggers")
    public Map<String, Object> listAnomalies(
            @RequestParam(required = false) String ruleId,
            @RequestParam(required = false) String groupKey,
            @RequestParam(required = false) List<String> classification,
            @RequestParam(required = false) String since,
            @RequestParam(required = false) String until,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        int size = QueryParams.intInRange("limit", limit, 1, 500, 100);
        Instant from = since == null ? Instant.EPOCH : QueryParams.instant("since", since);
        Instant to = until == null ? Instant.EPOCH.plusSeconds(315532800L) : QueryParams.instant("until", until);
        long before = cursor == null ? Long.MAX_VALUE : Long.parseLong(cursor);
        List<AnomalyObservationEntity> rows;
        if (ruleId != null && groupKey != null) {
            rows = anomalies.findTop200ByRuleIdAndGroupKeyOrderByIdDesc(UUID.fromString(ruleId), groupKey);
        } else if (ruleId != null) {
            rows = anomalies.findTop200ByRuleIdOrderByIdDesc(UUID.fromString(ruleId));
        } else {
            rows = anomalies
                    .findTop500ByBucketStartGreaterThanEqualAndBucketStartLessThanAndIdLessThanOrderByIdDesc(from, to,
                            before);
        }
        List<Map<String, Object>> items = new ArrayList<>();
        Long nextCursor = null;
        for (AnomalyObservationEntity row : rows) {
            if (items.size() >= size) {
                nextCursor = row.getId();
                break;
            }
            if (classification != null && !classification.isEmpty()
                    && !classification.contains(row.getClassification())) {
                continue;
            }
            if (row.getBucketStart().isBefore(from) || !row.getBucketStart().isBefore(to)) {
                continue;
            }
            items.add(item(row));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", nextCursor == null ? null : nextCursor.toString());
        return out;
    }

    private static Map<String, Object> item(AnomalyObservationEntity row) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", row.getId());
        item.put("ruleId", row.getRuleId());
        item.put("groupKey", row.getGroupKey());
        item.put("bucketStart", row.getBucketStart());
        item.put("bucketEnd", row.getBucketEnd());
        item.put("observed", row.getObserved());
        item.put("expected", row.getExpected());
        item.put("exposure", row.getExposure());
        item.put("pValue", row.getpValue());
        item.put("score", row.getScore());
        item.put("direction", row.getDirection());
        item.put("classification", row.getClassification());
        item.put("lowConfidence", row.isLowConfidence());
        item.put("baseline", row.getBaseline());
        item.put("detector", row.getDetector());
        item.put("evidence", row.getEvidence());
        item.put("watermarkSeq", row.getWatermarkSeq());
        return item;
    }

    private static Double threshold(WindowSpec spec, RuleType type) {
        if (spec == null) {
            return null;
        }
        if (type == RuleType.COUNT && spec.threshold() != null) {
            return (double) spec.threshold();
        }
        return null;
    }

    private TriggerRuleEntity load(String ruleId) {
        UUID id = UUID.fromString(ruleId);
        return rules.findById(id).orElseThrow(() -> new NotFoundException("No such rule: " + ruleId));
    }

    /** A baseline's jsonb form for other callers. */
    static Map<String, Object> baselineMap(Baseline baseline) {
        return baseline.toMap();
    }

    /** An activity sample's row for tests. */
    record SampleRow(String groupKey, Instant bucketStart, int count, boolean coverageOk) {

        static SampleRow of(ActivitySampleEntity entity) {
            return new SampleRow(entity.getGroupKey(), entity.getBucketStart(), entity.getCount(), entity.isCoverageOk());
        }
    }
}
