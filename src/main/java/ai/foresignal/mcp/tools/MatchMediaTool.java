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
import ai.foresignal.social.narrative.Pdq;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code match_media} (read; prompt 136): matches a PDQ hash against the media fingerprints of social posts and returns
 * the matches with first-seen dates and narratives, and the {@code REFERENCE_WINDOW} caveat (Foresignal only knows
 * media it has fingerprinted). Image uploads use {@code POST /v1/social/media/match}.
 */
@Component
@RestrictedRead("fingerprints are hashes; the posts that used them are read with listNarrativePosts")
public class MatchMediaTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "pdq", JsonSchemas.string("256-bit PDQ hash, 64 lower-case hex characters", 64, 64),
            "maxDistance", JsonSchemas.integer("Largest Hamming distance (PDQ recommends 31)", 0, 63,
                    (long) NarrativeVocabulary.PDQ_MATCH_DISTANCE),
            "limit", JsonSchemas.integer("Most matches", 1, 100, 20L)), "pdq");

    private final NarrativeViews views;
    private final JsonMapper json;

    /** Creates the tool. */
    public MatchMediaTool(NarrativeViews views, JsonMapper json) {
        this.views = views;
        this.json = json;
    }

    @Override
    public String name() {
        return "match_media";
    }

    @Override
    public String title() {
        return "Match an image hash";
    }

    @Override
    public String description() {
        return "Matches a PDQ image hash against the fingerprints of images seen in social posts and returns each match"
                + " with its Hamming distance, first-seen date and post, and the narratives it appeared in. Foresignal"
                + " only knows media it has fingerprinted since the store's start, so no match does not mean an image"
                + " is new.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("Media match result");
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
        String pdq = ListNewsStoriesTool.text(args, "pdq");
        if (pdq == null || !pdq.matches("^[0-9a-f]{64}$")) {
            return ToolResult.error("pdq is required: 64 lower-case hex characters");
        }
        int maxDistance = args.has("maxDistance") && args.get("maxDistance").isNumber()
                ? Math.max(0, Math.min(63, args.get("maxDistance").asInt())) : NarrativeVocabulary.PDQ_MATCH_DISTANCE;
        int limit = args.has("limit") && args.get("limit").isNumber()
                ? Math.max(1, Math.min(100, args.get("limit").asInt())) : 20;
        return ToolResult.json(json.valueToTree(views.match(Pdq.parse(pdq), "pdq", false, maxDistance, limit)));
    }
}
