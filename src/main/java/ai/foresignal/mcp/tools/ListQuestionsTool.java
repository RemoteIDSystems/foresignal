package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.enumArray;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecastStatus;
import ai.foresignal.forecast.ForecastViews;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code list_questions} (read, prompt 122): forecast questions with their latest ensemble probability and ICD term,
 * as {@code GET /v1/forecasts/questions} lists them.
 */
@RestrictedRead("ledger questions hold numbers and refs, no record content; private questions need private access")
@Component
public class ListQuestionsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "status", enumArray("Statuses (default: all).", ForecastStatus.ALL.toArray(String[]::new)),
            "category", stringArray("Categories, e.g. conflict, maritime, markets (default: all).", 50),
            "linked", string("Questions about this object: situation:{id}, finding:{id}, precursor:{id},"
                    + " hypothesis-set:{id} or a series reference."),
            "resolver", enumArray("Resolver types (default: all).", Ledger.RESOLVER_TYPES.toArray(String[]::new)),
            "dueBefore", dateTime("Only questions resolving before this time."),
            "limit", integer("How many, 1 to 200.", 1, 200, 50L)));

    private final ForecastService ledger;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public ListQuestionsTool(ForecastService ledger, IntelVisibility visibility, JsonMapper json) {
        this.ledger = ledger;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "list_questions";
    }

    @Override
    public String title() {
        return "List forecast questions";
    }

    @Override
    public String description() {
        return "Lists forecast-ledger questions (newest first) with status, category, resolver type, dates, the"
                + " latest ensemble probability and its ICD 203 term. Read one with get_question for the verbatim"
                + " resolution criteria before forecasting.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A page of questions");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String due = McpArgs.text(args, "dueBefore");
        ForecastService.ListQuery query = new ForecastService.ListQuery(McpArgs.strings(args, "status"),
                McpArgs.strings(args, "category"), McpArgs.text(args, "linked"), McpArgs.strings(args, "resolver"),
                due == null ? null : Instant.parse(due));
        ForecastService.Page page = ledger.list(query, null, Math.max(1, Math.min(200, McpArgs.integer(args, "limit",
                50))), visibility.seesPrivate(context.caller()));
        List<Map<String, Object>> items = new ArrayList<>();
        for (ForecastQuestionEntity q : page.items()) {
            items.add(ledger.summary(q));
        }
        return ToolResult.json(json.valueToTree(ForecastViews.page(items, page.nextCursor())));
    }
}
