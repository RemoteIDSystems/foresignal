package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.intel.InvestigationService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code create_investigation} (agent, prompt 101): opens an operator case file, owned by the caller's principal
 * and {@code instance}-visible with the default TLP label (prompt 130).
 */
@ai.foresignal.security.restrict.RestrictedRead("returns the new investigation id and title, no record content")
@Component
public class CreateInvestigationTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "title", string("A short title.", 1, 512),
            "summary", string("An optional markdown summary.", 1, 20_000)));

    private final InvestigationService investigations;
    private final ai.foresignal.collab.AccessResolver access;
    private final ai.foresignal.object.ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public CreateInvestigationTool(InvestigationService investigations, ai.foresignal.collab.AccessResolver access,
            ai.foresignal.object.ObjectCallers callers, JsonMapper json) {
        this.investigations = investigations;
        this.access = access;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "create_investigation";
    }

    @Override
    public String title() {
        return "Create investigation";
    }

    @Override
    public String description() {
        return "Opens an investigation (a case file) with a human-readable display id. Add evidence with"
                + " add_to_investigation.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The created investigation");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        ai.foresignal.object.ObjectCaller caller = callers.of(context.caller());
        var investigation = investigations.create(args, context.actor());
        if (caller.principal() != null) {
            access.register("investigation:" + investigation.getId(), caller.principal(),
                    ai.foresignal.collab.Visibility.INSTANCE, null);
        }
        return ToolResult.json(json.valueToTree(Map.of("id", investigation.getId(),
                "displayId", investigation.getDisplayId(), "title", investigation.getTitle(),
                "status", investigation.getStatus())));
    }
}
