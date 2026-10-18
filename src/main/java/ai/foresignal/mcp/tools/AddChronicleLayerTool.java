package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.chronicle.ChronicleLayerEntity;
import ai.foresignal.chronicle.ChronicleService;
import ai.foresignal.chronicle.ChronicleVocabulary;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code add_chronicle_layer} (agent, prompt 131): adds a layer to a chronicle the caller may edit, as
 * {@code POST /v1/chronicles/{id}/layers}; capture for it starts from the edit's seq onward.
 */
@RestrictedRead("returns the new layer's definition, no captured records")
@Component
public class AddChronicleLayerTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "chronicleId", string("The chronicle id or display id (CHR-yyyy-nnnn)."),
            "name", string("The layer name.", 1, 200),
            "selector", JsonSchemas.openObject("The ChronicleLayerSelector (type records, saved-view, watchlist,"
                    + " entities, situations, findings, series, raster-products or alerts).", Map.of()),
            "areaOverride", JsonSchemas.openObject("An optional ChronicleArea for this layer.", Map.of()),
            "capture", JsonSchemas.openObject("The CaptureMode (mode full, downsampled, events-only,"
                    + " latest-per-interval, rollup-only or frames).", Map.of()),
            "enabled", JsonSchemas.bool("Whether the layer captures.", true)), "chronicleId", "name", "selector",
            "capture");

    private final ChronicleService chronicles;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public AddChronicleLayerTool(ChronicleService chronicles, ObjectCallers callers, JsonMapper json) {
        this.chronicles = chronicles;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "add_chronicle_layer";
    }

    @Override
    public String title() {
        return "Add a chronicle layer";
    }

    @Override
    public String description() {
        return "Adds a layer to a chronicle you may edit (owner or editor). Capture for the layer starts from now"
                + " onward; earlier history is not copied. Returns the layer with its projected volume.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The created ChronicleLayer");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        ObjectCaller caller = callers.of(context.caller());
        if (caller.anonymous()) {
            throw new McpToolException("Changing a chronicle needs a token.");
        }
        String chronicleId = McpArgs.requiredText(args, "chronicleId");
        ObjectNode layer = ((ObjectNode) args).deepCopy();
        layer.remove("chronicleId");
        ChronicleLayerEntity l = chronicles.addLayer(chronicleId, layer, caller);
        ChronicleEntity c = chronicles.get(l.getChronicleId());
        return ToolResult.json(json.valueToTree(ChronicleService.layerView(l,
                ChronicleVocabulary.CAP_EXCEEDED.equals(c.getCapState()), true)));
    }
}
