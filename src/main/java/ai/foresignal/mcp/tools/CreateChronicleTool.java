package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.chronicle.ChronicleService;
import ai.foresignal.chronicle.ChronicleVocabulary;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code create_chronicle} (agent, prompt 131): creates a chronicle owned by the caller's principal, as
 * {@code POST /v1/chronicles}; it is {@code proposed} (no capture) until activated, unless {@code activate} is true.
 */
@RestrictedRead("returns the new chronicle's metadata, no captured records")
@Component
public class CreateChronicleTool implements ForesignalMcpTool {

    static final Map<String, Object> LAYER = JsonSchemas.openObject("A ChronicleLayerInput: name, selector (type"
            + " records with sources, kinds and conditions; saved-view; watchlist; entities; situations; findings;"
            + " series; raster-products; alerts), optional areaOverride, and capture (mode full, downsampled with"
            + " interval, events-only, latest-per-interval with interval, rollup-only, or frames with everyNthFrame)",
            props("name", string("The layer name."),
                    "selector", JsonSchemas.openObject("The ChronicleLayerSelector.", Map.of()),
                    "areaOverride", JsonSchemas.openObject("An optional ChronicleArea.", Map.of()),
                    "capture", JsonSchemas.openObject("The CaptureMode.", Map.of())),
            "name", "selector", "capture");

    static final Map<String, Object> INPUT = object(props(
            "name", string("The chronicle's name.", 1, 200),
            "description", string("An optional description.", 1, 20_000),
            "visibility", JsonSchemas.enumeration("Who sees it besides its access list.", "private", "private",
                    "shared", "instance"),
            "tlp", JsonSchemas.enumeration("Its TLP label.", null, "CLEAR", "GREEN", "AMBER", "AMBER+STRICT", "RED"),
            "area", JsonSchemas.openObject("An optional ChronicleArea: geometry, placeIds, regionCodes, bufferKm.",
                    Map.of()),
            "retention", string("forever (default) or an ISO-8601 duration kept after stop."),
            "sizeCapBytes", JsonSchemas.integer("The size cap in bytes (default 20 GB).", 1, Long.MAX_VALUE, null),
            "situationIds", JsonSchemas.stringArray("Linked situations.", 200),
            "hypothesisSetIds", JsonSchemas.stringArray("Linked hypothesis sets.", 200),
            "investigationIds", JsonSchemas.stringArray("Linked investigations.", 200),
            "topicIds", JsonSchemas.stringArray("Linked attention topics.", 200),
            "layers", Map.of("type", "array", "items", LAYER, "maxItems", 100),
            "fromWorkspaceId", string("A workspace whose layers, area and filters become chronicle layers."),
            "activate", JsonSchemas.bool("Start capture at once.", false)), "name");

    private final ChronicleService chronicles;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public CreateChronicleTool(ChronicleService chronicles, ObjectCallers callers, JsonMapper json) {
        this.chronicles = chronicles;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "create_chronicle";
    }

    @Override
    public String title() {
        return "Create a chronicle";
    }

    @Override
    public String description() {
        return "Creates a chronicle: a named monitoring thread whose layers (sources and kinds with conditions, saved"
                + " views, watchlists, entities, situations, findings, series, raster products, alerts) record their own"
                + " history while it is active, independent of global retention. It starts proposed (no capture)"
                + " unless activate is true. Check the projected storage first; the default cap is 20 GB.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The created Chronicle");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        ObjectCaller caller = callers.of(context.caller());
        if (caller.anonymous() || caller.principal() == null) {
            throw new McpToolException("Creating a chronicle needs a token.");
        }
        ChronicleEntity c = chronicles.create(args, caller, ChronicleVocabulary.ORIGIN_AGENT, null);
        return ToolResult.json(json.valueToTree(chronicles.view(c, caller, true)));
    }

    /** The members accepted, for the schema checks of tests. */
    static List<String> members() {
        return List.copyOf(((Map<?, ?>) INPUT.get("properties")).keySet().stream().map(String::valueOf).toList());
    }
}
