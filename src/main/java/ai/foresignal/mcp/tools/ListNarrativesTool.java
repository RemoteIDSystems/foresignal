package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.social.SocialViews;
import ai.foresignal.social.narrative.NarrativeViews;
import ai.foresignal.social.narrative.NarrativeVocabulary;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code list_narratives} (read; prompt 136): social narratives newest activity first, by attention topic, situation,
 * place or region, status, state and network. Each narrative counts its independent origins (evidence groups), so
 * thousands of reposts of one report count once; its earliest posts are the earliest observed, never originators.
 */
@Component
@RestrictedRead("narratives are aggregates; their member posts are read with listNarrativePosts under the restriction")
public class ListNarrativesTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "topic", JsonSchemas.string("Attention topic id"),
            "situationId", JsonSchemas.string("Linked situation id"),
            "place", JsonSchemas.string("036 place id or 040 region code"),
            "status", JsonSchemas.enumeration("137 status", null, NarrativeVocabulary.NARRATIVE_STATUSES.toArray(
                    String[]::new)),
            "state", JsonSchemas.enumeration("State", null, NarrativeVocabulary.STATES.toArray(String[]::new)),
            "platform", JsonSchemas.string("Network: bluesky, mastodon, reddit, youtube, x, threads, instagram"),
            "since", JsonSchemas.dateTime("Last activity at or after this time"),
            "cursor", JsonSchemas.string("Cursor from a previous call"),
            "limit", JsonSchemas.integer("Page size", 1, 100, 25L)));

    private final NarrativeViews views;
    private final JsonMapper json;

    /** Creates the tool. */
    public ListNarrativesTool(NarrativeViews views, JsonMapper json) {
        this.views = views;
        this.json = json;
    }

    @Override
    public String name() {
        return "list_narratives";
    }

    @Override
    public String title() {
        return "List social narratives";
    }

    @Override
    public String description() {
        return "Lists social media narratives (posts that say the same thing) newest activity first, with platforms,"
                + " post and author counts, independent origins (evidence groups), origin concentration, velocity,"
                + " state and status. A narrative is what people are saying, not what happened: its status comes only"
                + " from comparison with independent evidence. Use get_narrative for claims, media and coordination"
                + " indicators.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Narrative summaries");
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
        int limit = args != null && args.has("limit") && args.get("limit").isNumber()
                ? Math.max(1, Math.min(100, args.get("limit").asInt())) : 25;
        try {
            String status = ListNewsStoriesTool.text(args, "status");
            String state = ListNewsStoriesTool.text(args, "state");
            String since = ListNewsStoriesTool.text(args, "since");
            NarrativeViews.Page page = views.narratives(ListNewsStoriesTool.text(args, "topic"),
                    ListNewsStoriesTool.text(args, "situationId"), ListNewsStoriesTool.text(args, "place"),
                    status == null ? List.of() : List.of(status), state == null ? List.of() : List.of(state),
                    SocialViews.network("platform", ListNewsStoriesTool.text(args, "platform")),
                    since == null ? null : Instant.parse(since), ListNewsStoriesTool.text(args, "cursor"), limit);
            return ToolResult.json(json.valueToTree(page.toMap()));
        } catch (DateTimeParseException e) {
            return ToolResult.error("since must be an ISO-8601 instant");
        } catch (RuntimeException e) {
            return ToolResult.error(e.getMessage());
        }
    }
}
