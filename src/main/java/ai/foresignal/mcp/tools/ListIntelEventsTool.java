package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.number;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.intel.IntelEventMapper;
import ai.foresignal.intel.IntelEventService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** {@code list_intel_events} (read, prompt 101): the compact events, newest first. */
@RestrictedRead
@Component
public class ListIntelEventsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "type", string("Only this event type.", 1, 64),
            "status", string("Only this status.", 1, 16),
            "minConfidence", number("Only events at or above this confidence.", 0, 1),
            "limit", integer("How many at most.", 1, 500, 50L)));

    private final IntelEventService events;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public ListIntelEventsTool(IntelEventService events, McpOutputShaper shaper) {
        this.events = events;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "list_intel_events";
    }

    @Override
    public String title() {
        return "List intelligence events";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Lists intelligence events (typed, located findings built from pinned"
                + " evidence). Confidence is an uncalibrated score, not a probability.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Intelligence events");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        int limit = McpArgs.integer(args, "limit", 50);
        String confidence = McpArgs.numberText(args, "minConfidence");
        Double min = confidence == null ? null : Double.parseDouble(confidence);
        List<ObjectNode> items = new ArrayList<>();
        for (var event : events.list(McpArgs.text(args, "type"), McpArgs.text(args, "status"), null, min, limit)) {
            items.add(shaper.objectTree(IntelEventMapper.summary(event)));
        }
        return ToolResult.json(shaper.list(items, items.size(), false, Detail.SHORT, null));
    }
}
