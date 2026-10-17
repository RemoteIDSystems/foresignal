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
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.RelationService;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_related} (read, prompt 129): the neighborhood of an object through the time-aware relation graph, each
 * neighbor with the relation (type, validity, confidence, label, provenance) and its envelope.
 */
@RestrictedRead("neighbors resolve through the object resolvers; private relations need private access")
@Component
public class GetRelatedTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "ref", string("The object reference {type}:{id}."),
            "types", JsonSchemas.enumArray("Relation types to keep (all when omitted).",
                    ObjectTypes.RELATION_TYPES.toArray(String[]::new)),
            "direction", JsonSchemas.enumeration("out (from the object), in (to it) or both.", "both", "out", "in",
                    "both"),
            "asOf", JsonSchemas.dateTime("Optional validity time."),
            "minConfidence", JsonSchemas.number("Lowest relation confidence kept.", 0, 1),
            "limit", JsonSchemas.integer("Most neighbors.", 1, 200, 50L)), "ref");

    private final RelationService relations;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetRelatedTool(RelationService relations, ObjectCallers callers, JsonMapper json) {
        this.relations = relations;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_related";
    }

    @Override
    public String title() {
        return "Get related objects";
    }

    @Override
    public String description() {
        return "Lists the objects related to an object through Foresignal's relation graph (members of situations,"
                + " evidence for or against events and hypotheses, same-as links, dependencies, places containing it),"
                + " valid at asOf or now, with each relation's confidence, label and provenance.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The related objects");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        ObjectRef ref = ObjectRef.parse(McpArgs.requiredText(args, "ref"));
        String direction = McpArgs.text(args, "direction");
        double min = args.hasNonNull("minConfidence") ? args.get("minConfidence").asDouble() : 0;
        Map<String, Object> out = relations.related(ref, RelationService.relationTypes(McpArgs.strings(args, "types")),
                direction == null ? "both" : direction, min, McpArgs.integer(args, "limit", 50),
                new ResolveContext(callers.of(context.caller()), McpArgs.instant(args, "asOf")));
        return ToolResult.json(json.valueToTree(out));
    }
}
