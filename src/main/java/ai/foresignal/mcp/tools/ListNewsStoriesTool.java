package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.news.NewsViews;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code list_news_stories} (read; prompt 133): news stories with coverage balance, blindspot and corroboration, newest
 * activity first. Stories are derived coverage counts; coverage describes reporting, never truth.
 */
@Component
@RestrictedRead("stories are derived coverage counts with no article content")
public class ListNewsStoriesTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "topic", JsonSchemas.string("Attention topic id"),
            "region", JsonSchemas.string("Region code (040)"),
            "situationId", JsonSchemas.string("Only stories that are members of this situation"),
            "since", JsonSchemas.dateTime("Stories last seen at or after this time"),
            "blindspot", JsonSchemas.enumeration("Only stories with a blindspot", null, "left", "right", "any"),
            "cursor", JsonSchemas.string("Cursor from a previous call"),
            "limit", JsonSchemas.integer("Page size", 1, 100, 25L)));

    private final NewsViews views;
    private final JsonMapper json;

    /** Creates the tool. */
    public ListNewsStoriesTool(NewsViews views, JsonMapper json) {
        this.views = views;
        this.json = json;
    }

    @Override
    public String name() {
        return "list_news_stories";
    }

    @Override
    public String title() {
        return "List news stories";
    }

    @Override
    public String description() {
        return "Lists news stories (clusters of articles about one event; wire reprints count once) with their coverage"
                + " balance by outlet lean (left, center, right, unrated, in evidence groups), state-affiliated share,"
                + " blindspot and whether non-news evidence corroborates them. Ratings come only from declared"
                + " providers; coverage describes reporting, never truth. Read one story with get_news_story.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("News story items");
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
    public boolean idempotentHint() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String blindspot = text(args, "blindspot");
        if (blindspot != null && !blindspot.equals("left") && !blindspot.equals("right") && !blindspot.equals("any")) {
            return ToolResult.error("blindspot must be left, right or any");
        }
        Instant since = null;
        if (text(args, "since") != null) {
            try {
                since = Instant.parse(text(args, "since"));
            } catch (DateTimeParseException e) {
                return ToolResult.error("since must be an ISO-8601 instant");
            }
        }
        int limit = args != null && args.has("limit") && args.get("limit").isNumber()
                ? Math.max(1, Math.min(100, args.get("limit").asInt())) : 25;
        try {
            NewsViews.Page page = views.stories(text(args, "topic"), text(args, "region"), text(args, "situationId"),
                    since, blindspot, text(args, "cursor"), limit);
            return ToolResult.json(json.valueToTree(page.toMap()));
        } catch (RuntimeException e) {
            return ToolResult.error(e.getMessage());
        }
    }

    static String text(JsonNode args, String field) {
        if (args == null || !args.has(field) || args.get(field).isNull()) {
            return null;
        }
        String s = args.get(field).asString();
        return s == null || s.isBlank() ? null : s.strip();
    }
}
