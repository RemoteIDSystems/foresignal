package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectService;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_object} (read, prompt 129): the uniform envelope of any object by its {@code ObjectRef}
 * ({@code situation:01J…}, {@code record:adsblol-mil/ae1234}, …), {@code asOf}-aware, with the actions the caller may
 * perform.
 */
@RestrictedRead("each resolver applies its object's private rule and the caller's excluded sources")
@Component
public class GetObjectTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "ref", string("The object reference {type}:{id}, e.g. situation:01J…, record:{sourceId}/{externalId}."),
            "asOf", JsonSchemas.dateTime("Optional knowledge cutoff (what Foresignal knew then).")), "ref");

    private final ObjectService objects;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetObjectTool(ObjectService objects, ObjectCallers callers, JsonMapper json) {
        this.objects = objects;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_object";
    }

    @Override
    public String title() {
        return "Get object";
    }

    @Override
    public String description() {
        return "Returns the envelope of any Foresignal object by its reference ({type}:{id}): title, summary, status,"
                + " severity, time, simplified geometry, provenance and the actions you may perform. Refer to objects"
                + " by these references in answers and workspace focus.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The object envelope");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        ObjectRef ref = ObjectRef.parse(McpArgs.requiredText(args, "ref"));
        Instant asOf = McpArgs.instant(args, "asOf");
        return ToolResult.json(json.valueToTree(objects.get(ref, new ResolveContext(callers.of(context.caller()),
                asOf))));
    }
}
