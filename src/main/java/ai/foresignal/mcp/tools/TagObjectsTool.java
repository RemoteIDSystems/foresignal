package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.collab.TagEntity;
import ai.foresignal.collab.TagRepository;
import ai.foresignal.collab.TagService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.security.Scope;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code tag_objects} (read, prompt 130): assigns a tag to up to 500 object references at once (or removes it), as
 * {@code POST /v1/tags/{id}/assign} and {@code /unassign}. The tag is named by id, or by name among the tags the
 * caller sees; a name no visible tag has creates a personal tag.
 */
@Component
public class TagObjectsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "tagId", string("The tag id."),
            "tag", string("A tag name, when tagId is absent: your personal tag or a shared tag you see; a new"
                    + " personal tag is created when none has the name.", 1, 64),
            "refs", JsonSchemas.stringArray("Object references {type}:{id} (at most 500).", 500),
            "remove", JsonSchemas.bool("Remove the tag instead of assigning it.", false)), "refs");

    private final TagService tags;
    private final TagRepository tagRows;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public TagObjectsTool(TagService tags, TagRepository tagRows, ObjectCallers callers, JsonMapper json) {
        this.tags = tags;
        this.tagRows = tagRows;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "tag_objects";
    }

    @Override
    public String title() {
        return "Tag objects";
    }

    @Override
    public String description() {
        return "Labels many Foresignal objects at once with a personal or shared tag (or removes it with"
                + " remove=true). References you cannot see are skipped and reported, not errors.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The TagAssignResult");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        ObjectCaller caller = callers.of(context.caller());
        if (caller.anonymous()) {
            throw new McpToolException("Tagging needs a token.");
        }
        String tagId = McpArgs.text(args, "tagId");
        if (tagId == null) {
            String name = McpArgs.text(args, "tag");
            if (name == null || name.isBlank()) {
                throw new McpToolException("Name the tag with tagId or tag.");
            }
            tagId = byName(name.strip(), caller);
        }
        List<String> refs = McpArgs.strings(args, "refs");
        return ToolResult.json(json.valueToTree(tags.bulk(tagId, refs, !McpArgs.bool(args, "remove", false),
                caller)));
    }

    private String byName(String name, ObjectCaller caller) {
        for (String namespace : List.of(TagEntity.PERSONAL, TagEntity.SHARED)) {
            for (TagEntity t : tagRows.findByNamespaceAndNameIgnoreCase(namespace, name)) {
                if (tags.visible(t, caller)) {
                    return t.getId();
                }
            }
        }
        ObjectNode input = json.createObjectNode();
        input.put("name", name);
        input.put("namespace", TagEntity.PERSONAL);
        return tags.create(input, caller).getId();
    }
}
