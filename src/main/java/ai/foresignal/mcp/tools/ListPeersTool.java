package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.federation.PeerService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code list_peers} (admin; prompt 113): the federation peers with their mode, state, health reason and lag.
 * Tokens and ingest keys are never returned.
 */
@Component
public class ListPeersTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props());

    private final PeerService peers;
    private final JsonMapper json;

    /** Creates the tool. */
    public ListPeersTool(PeerService peers, JsonMapper json) {
        this.peers = peers;
        this.json = json;
    }

    @Override
    public String name() {
        return "list_peers";
    }

    @Override
    public String title() {
        return "List federation peers";
    }

    @Override
    public String description() {
        return "Lists the Foresignal instances this one replicates from (PULL) or receives edge pushes from"
                + " (RECEIVE), with state, health reason, last applied seq and lag. Needs an admin token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Peer items");
    }

    @Override
    public Scope requiredScope() {
        return Scope.ADMIN;
    }

    @Override
    public boolean readOnlyHint() {
        return true;
    }

    @Override
    public boolean idempotentHint() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        List<Map<String, Object>> items = peers.views();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("total", items.size());
        out.put("truncated", false);
        return ToolResult.json(json.valueToTree(out));
    }
}
