package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
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
 * {@code get_claims} (read; prompt 136): structured claims of social narratives newest first, by narrative, situation,
 * claim type, status and time. Claims are reports with an Admiralty grade; they can be added to hypotheses as evidence
 * with source {@code social-claim} and ref {@code claim:{id}}.
 */
@Component
@RestrictedRead("claims are aggregates of narratives; their member posts are read with listNarrativePosts")
public class GetClaimsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "narrativeId", JsonSchemas.string("Narrative id"),
            "situationId", JsonSchemas.string("Situations linked to the claim's narrative"),
            "type", JsonSchemas.string("Lexicon claim type, e.g. explosion, strike, outage"),
            "status", JsonSchemas.enumeration("137 status", null, NarrativeVocabulary.CLAIM_STATUSES.toArray(
                    String[]::new)),
            "since", JsonSchemas.dateTime("First observed at or after this time"),
            "cursor", JsonSchemas.string("Cursor from a previous call"),
            "limit", JsonSchemas.integer("Page size", 1, 100, 25L)));

    private final NarrativeViews views;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetClaimsTool(NarrativeViews views, JsonMapper json) {
        this.views = views;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_claims";
    }

    @Override
    public String title() {
        return "Get social claims";
    }

    @Override
    public String description() {
        return "Lists structured claims extracted from social narratives (what, where, when, who) with support,"
                + " independent origins, extraction method, extraction confidence, 137 status and Admiralty grade."
                + " A claim is a report, never an observation: its place is where people say something happened.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Claims");
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
            String since = ListNewsStoriesTool.text(args, "since");
            NarrativeViews.Page page = views.claims(ListNewsStoriesTool.text(args, "narrativeId"),
                    ListNewsStoriesTool.text(args, "situationId"), ListNewsStoriesTool.text(args, "type"),
                    status == null ? List.of() : List.of(status), null, since == null ? null : Instant.parse(since),
                    ListNewsStoriesTool.text(args, "cursor"), limit);
            return ToolResult.json(json.valueToTree(page.toMap()));
        } catch (DateTimeParseException e) {
            return ToolResult.error("since must be an ISO-8601 instant");
        } catch (RuntimeException e) {
            return ToolResult.error(e.getMessage());
        }
    }
}
