package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.enumArray;
import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.precursor.PrecursorEntity;
import ai.foresignal.precursor.PrecursorService;
import ai.foresignal.precursor.PrecursorViews;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code list_precursors} (read, prompt 120): the precursor library, newest first, each entry with its description,
 * both windows' statistics, batch size and the association note.
 */
@RestrictedRead
@Component
public class ListPrecursorsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "status", enumArray("Statuses.", PrecursorService.STATUSES.toArray(String[]::new)),
            "kind", enumeration("Kind.", null, PrecursorService.KINDS.toArray(String[]::new)),
            "region", stringArray("040 region codes, e.g. YE or US-TX.", 50),
            "family", stringArray("Antecedent or consequent families.", 20),
            "limit", integer("Page size.", 1, 100, 25L),
            "cursor", string("The nextCursor of the previous page.")));

    private final PrecursorService precursors;
    private final JsonMapper json;

    /** Creates the tool. */
    public ListPrecursorsTool(PrecursorService precursors, JsonMapper json) {
        this.precursors = precursors;
        this.json = json;
    }

    @Override
    public String name() {
        return "list_precursors";
    }

    @Override
    public String title() {
        return "List precursors";
    }

    @Override
    public String description() {
        return "Lists the precursor library: patterns where one kind of event or series movement tended to precede"
                + " another in stored data, found with false-discovery control and confirmed on later data. Each is an"
                + " association, never causation; quote its hit rate, base rate and caveats with any claim.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A page of precursors");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        List<String> statuses = ListFindingsTool.check("status", McpArgs.strings(args, "status"),
                PrecursorService.STATUSES);
        String kind = McpArgs.text(args, "kind");
        if (kind != null) {
            ListFindingsTool.check("kind", List.of(kind), PrecursorService.KINDS);
        }
        PrecursorService.Page<PrecursorEntity> page = precursors.list(statuses, kind, McpArgs.strings(args, "region"),
                McpArgs.strings(args, "family"), McpArgs.text(args, "cursor"),
                Math.max(1, Math.min(100, McpArgs.integer(args, "limit", 25))));
        return ToolResult.json(json.valueToTree(PrecursorViews.page(page.items().stream()
                .map(PrecursorViews::precursor).toList(), page.nextCursor())));
    }
}
