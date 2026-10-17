package ai.foresignal.mcp.tools;

import ai.foresignal.collab.MeService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.security.Scope;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code whoami} (read, prompt 130): the calling principal, its token, scopes, groups and notification preferences,
 * exactly as {@code GET /v1/me}.
 */
@Component
public class WhoamiTool implements ForesignalMcpTool {

    private final MeService me;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public WhoamiTool(MeService me, ObjectCallers callers, JsonMapper json) {
        this.me = me;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "whoami";
    }

    @Override
    public String title() {
        return "Who am I";
    }

    @Override
    public String description() {
        return "Returns the principal (person, agent or service) behind your token: its handle, organization, token"
                + " scope, groups and notification preferences, and the instance's default TLP label. Call it to know"
                + " which work products you own and which TLP boundaries apply to you.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return JsonSchemas.object(JsonSchemas.props());
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The Me document");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public boolean readOnlyHint() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        ObjectCaller caller = callers.of(context.caller());
        if (caller.anonymous() || context.caller().token() == null) {
            throw new McpToolException("whoami needs a token.");
        }
        return ToolResult.json(json.valueToTree(me.me(context.caller().token(), caller)));
    }
}
