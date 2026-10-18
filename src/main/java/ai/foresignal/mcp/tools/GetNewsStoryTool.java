package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.news.NewsViews;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_news_story} (read; prompt 133): one story with its articles grouped by outlet (each outlet's ratings per
 * provider with license and date, and the consensus), coverage statistics, linked situations and canonical events.
 * Articles of sources outside the caller's restriction are left out.
 */
@Component
@RestrictedRead("articles of sources outside the restriction are left out")
public class GetNewsStoryTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "storyId", JsonSchemas.string("The story id (news-story:{id} without the prefix also accepted)")),
            "storyId");

    private final NewsViews views;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetNewsStoryTool(NewsViews views, ObjectCallers callers, JsonMapper json) {
        this.views = views;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_news_story";
    }

    @Override
    public String title() {
        return "Get a news story";
    }

    @Override
    public String description() {
        return "Returns one news story: its deterministic title, articles grouped by outlet (outbound links only, no"
                + " bodies or bylines) with each outlet's ratings per provider and consensus, coverage balance,"
                + " blindspot, state-affiliated share, corroboration, and linked situations and canonical events."
                + " Coverage describes reporting, never truth.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("NewsStory");
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
        String id = ListNewsStoriesTool.text(args, "storyId");
        if (id == null) {
            return ToolResult.error("storyId is required");
        }
        if (id.startsWith("news-story:")) {
            id = id.substring("news-story:".length());
        }
        try {
            NewsViews.ArticleFilter filter = NewsViews.ArticleFilter.of(context.restriction(),
                    context.excludedSources());
            return ToolResult.json(json.valueToTree(views.storyDetail(id,
                    new ResolveContext(callers.of(context.caller()), null), EgressPath.MCP, filter)));
        } catch (NotFoundException e) {
            return ToolResult.error(e.getMessage());
        }
    }
}
