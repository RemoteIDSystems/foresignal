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
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code search_social_posts} (read; prompt 135): stored {@code social-post} records newest first, by attention topic,
 * network, text and time. Accounts are reporting sources: handles are shown so an analyst can judge a post, and
 * nothing here profiles an account. Deleted posts appear as tombstones without text or media.
 */
@Component
@RestrictedRead("posts of sources outside the restriction are left out; spatial restrictions see none")
public class SearchSocialPostsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "topic", JsonSchemas.string("Attention topic id"),
            "platform", JsonSchemas.string("Network: bluesky, mastodon, reddit, youtube, x, threads, instagram"),
            "q", JsonSchemas.string("Text the post must contain (case-insensitive)"),
            "since", JsonSchemas.dateTime("Posts created at or after this time"),
            "until", JsonSchemas.dateTime("Posts created before this time"),
            "cursor", JsonSchemas.string("Cursor from a previous call"),
            "limit", JsonSchemas.integer("Page size", 1, 100, 25L)));

    private final SocialViews views;
    private final JsonMapper json;

    /** Creates the tool. */
    public SearchSocialPostsTool(SocialViews views, JsonMapper json) {
        this.views = views;
        this.json = json;
    }

    @Override
    public String name() {
        return "search_social_posts";
    }

    @Override
    public String title() {
        return "Search social posts";
    }

    @Override
    public String description() {
        return "Searches stored public social posts (Bluesky, Mastodon, Reddit, YouTube, X, Threads, Instagram and"
                + " operator-connected providers) newest first, by attention topic, network, text and time. Each post"
                + " carries its network, handle, text, links, hashtags, language, how it was collected (stream, search,"
                + " sample, cue) and the topics it matched. Posts are reports by accounts, not observations; use"
                + " get_social_volume for how much a topic is discussed.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Social post items");
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
        String q = ListNewsStoriesTool.text(args, "q");
        if (q != null && q.length() > 500) {
            return ToolResult.error("q must be at most 500 characters");
        }
        int limit = args != null && args.has("limit") && args.get("limit").isNumber()
                ? Math.max(1, Math.min(100, args.get("limit").asInt())) : 25;
        try {
            Instant since = instant(ListNewsStoriesTool.text(args, "since"));
            Instant until = instant(ListNewsStoriesTool.text(args, "until"));
            SocialViews.PostFilter filter = SocialViews.PostFilter.of(context.restriction(),
                    context.excludedSources());
            SocialViews.Page page = views.posts(ListNewsStoriesTool.text(args, "topic"), SocialViews.network(
                    "platform", ListNewsStoriesTool.text(args, "platform")), q, null, since, until,
                    ListNewsStoriesTool.text(args, "cursor"), limit, filter);
            return ToolResult.json(json.valueToTree(page.toMap()));
        } catch (DateTimeParseException e) {
            return ToolResult.error("since and until must be ISO-8601 instants");
        } catch (RuntimeException e) {
            return ToolResult.error(e.getMessage());
        }
    }

    private static Instant instant(String text) {
        return text == null ? null : Instant.parse(text);
    }
}
