package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.federation.PeerService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code peer_status} (admin; prompt 113): one federation peer's replication state. */
@Component
public class PeerStatusTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props("peerId", string("The peer id", 1, 48)), "peerId");

    private final PeerService peers;
    private final JsonMapper json;

    /** Creates the tool. */
    public PeerStatusTool(PeerService peers, JsonMapper json) {
        this.peers = peers;
        this.json = json;
    }

    @Override
    public String name() {
        return "peer_status";
    }

    @Override
    public String title() {
        return "Federation peer status";
    }

    @Override
    public String description() {
        return "One federation peer: mode, state (connecting, streaming, resyncing, failing, receiving, stopped),"
                + " health reason (PEER_KEY_MISMATCH, PEER_UNREACHABLE, PEER_RESYNC), pinned key, last applied seq"
                + " and lag. Needs an admin token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The Peer");
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
        JsonNode id = args == null ? null : args.get("peerId");
        if (id == null || !id.isString() || id.asString().isBlank()) {
            return ToolResult.error("peerId is required");
        }
        try {
            return ToolResult.json(json.valueToTree(peers.view(peers.get(id.asString().strip()))));
        } catch (NotFoundException e) {
            return ToolResult.error("peer '" + id.asString() + "' does not exist");
        }
    }
}
