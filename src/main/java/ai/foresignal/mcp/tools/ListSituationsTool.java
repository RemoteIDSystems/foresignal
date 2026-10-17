package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.situation.SituationLifecycle;
import ai.foresignal.situation.SituationService;
import ai.foresignal.situation.SituationThemes;
import ai.foresignal.situation.SituationViews;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code list_situations} (read, prompt 118): situations by latest activity, private ones only with access. */
@RestrictedRead
@Component
public class ListSituationsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "status", stringArray("Statuses: emerging, active, dormant, closed.", 4),
            "theme", stringArray("Themes: " + String.join(", ", SituationThemes.ALL) + ".", 10),
            "region", stringArray("040 region codes, e.g. YE or US-TX.", 50),
            "since", dateTime("Only situations changed at or after this time."),
            "q", string("Text in the title or summary."),
            "limit", integer("Page size.", 1, 100, 25L),
            "cursor", string("The nextCursor of the previous page.")));

    private final SituationService situations;
    private final SituationViews views;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public ListSituationsTool(SituationService situations, SituationViews views, IntelVisibility visibility,
            JsonMapper json) {
        this.situations = situations;
        this.views = views;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "list_situations";
    }

    @Override
    public String title() {
        return "List situations";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Lists situations (persistent storylines of related events, findings, alerts and"
                + " records) by latest activity, with status, severity, signal families and the corroboration share."
                + " Use get_situation for the evidence and get_situation_changes for what changed since a day.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A page of situations");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        var statuses = McpArgs.strings(args, "status");
        for (String status : statuses) {
            if (!SituationLifecycle.ALL.contains(status)) {
                throw ValidationException.forField("status", "must be one of " + SituationLifecycle.ALL);
            }
        }
        var themes = McpArgs.strings(args, "theme");
        for (String theme : themes) {
            if (!SituationThemes.valid(theme)) {
                throw ValidationException.forField("theme", "must be one of " + SituationThemes.ALL);
            }
        }
        SituationService.ListQuery query = new SituationService.ListQuery(statuses, themes,
                McpArgs.strings(args, "region"), null, McpArgs.instant(args, "since"), McpArgs.text(args, "q"),
                McpArgs.text(args, "cursor"), Math.max(1, Math.min(100, McpArgs.integer(args, "limit", 25))));
        return ToolResult.json(json.valueToTree(views.page(situations.list(query,
                visibility.seesPrivate(context.caller())))));
    }
}
