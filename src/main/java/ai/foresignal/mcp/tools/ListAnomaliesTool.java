package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.trigger.window.AnomalyObservationEntity;
import ai.foresignal.trigger.window.AnomalyObservationRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code list_anomalies} (read, prompt 100): the evaluated anomaly buckets, newest first. The
 * p-value assumes the stated count model; it is not the probability that something is happening.
 */
@Component
public class ListAnomaliesTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "ruleId", string("Only this rule.", 1, 64),
            "groupKey", string("Only this group.", 1, 256),
            "limit", integer("How many results at most.", 1, 500, 100L)));

    private final AnomalyObservationRepository anomalies;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public ListAnomaliesTool(AnomalyObservationRepository anomalies, McpOutputShaper shaper) {
        this.anomalies = anomalies;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "list_anomalies";
    }

    @Override
    public String title() {
        return "List anomalies";
    }

    @Override
    public String description() {
        return "Lists evaluated anomaly buckets: observed vs expected counts, the p-value under the stated"
                + " Poisson or negative-binomial model, the classification and the baseline summary. A p-value is"
                + " not the probability that something is happening; state it as a model tail, never as certainty."
                + " Coverage drops are reported as coverage-drop, not as activity anomalies.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Anomaly buckets, newest first");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String ruleId = McpArgs.text(args, "ruleId");
        String groupKey = McpArgs.text(args, "groupKey");
        int limit = McpArgs.integer(args, "limit", 100);
        List<AnomalyObservationEntity> rows;
        if (ruleId != null && groupKey != null) {
            rows = anomalies.findTop200ByRuleIdAndGroupKeyOrderByIdDesc(UUID.fromString(ruleId), groupKey);
        } else if (ruleId != null) {
            rows = anomalies.findTop200ByRuleIdOrderByIdDesc(UUID.fromString(ruleId));
        } else {
            rows = anomalies.findAll();
        }
        List<ObjectNode> items = new ArrayList<>();
        for (AnomalyObservationEntity row : rows) {
            if (items.size() >= limit) {
                break;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.getId());
            item.put("ruleId", row.getRuleId());
            item.put("groupKey", row.getGroupKey());
            item.put("bucketStart", row.getBucketStart());
            item.put("observed", row.getObserved());
            item.put("expected", row.getExpected());
            item.put("pValue", row.getpValue());
            item.put("classification", row.getClassification());
            item.put("detector", row.getDetector());
            item.put("baseline", row.getBaseline());
            item.put("manifestId", row.getManifestId());
            items.add(shaper.objectTree(item));
        }
        return ToolResult.json(shaper.list(items, items.size(), false, Detail.SHORT, null));
    }
}
