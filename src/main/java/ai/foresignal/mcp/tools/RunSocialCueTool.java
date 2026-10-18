package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.social.correlation.SocialCueService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code run_social_cue} (agent; prompt 137): queues a targeted, budgeted social search around a place, area, entity
 * or situation, as {@code POST /v1/social/cues}.
 */
@Component
public class RunSocialCueTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "placeId", JsonSchemas.string("036 place id"),
            "regionCode", JsonSchemas.string("040 region code"),
            "bbox", JsonSchemas.string("minLon,minLat,maxLon,maxLat"),
            "entityRef", JsonSchemas.string("098 entity ref, entity:{id}"),
            "situationId", JsonSchemas.string("118 situation id"),
            "terms", JsonSchemas.stringArray("Extra terms added to the generated queries", 20),
            "families", JsonSchemas.stringArray("Signal families whose lexicon terms are added", 20),
            "platforms", JsonSchemas.stringArray("bluesky-search, reddit, x, threads; default every enabled one", 4),
            "windowStart", JsonSchemas.dateTime("Window start"),
            "windowEnd", JsonSchemas.dateTime("Window end (optional)"),
            "maxCostUsd", JsonSchemas.number("Cost cap in USD; default and ceiling social.cues.maxCostUsdPerCue", 0,
                    1_000)), "windowStart");

    private final SocialCueService cues;
    private final JsonMapper json;

    /** Creates the tool. */
    public RunSocialCueTool(SocialCueService cues, JsonMapper json) {
        this.cues = cues;
        this.json = json;
    }

    @Override
    public String name() {
        return "run_social_cue";
    }

    @Override
    public String title() {
        return "Run a social cue";
    }

    @Override
    public String description() {
        return "Queues a targeted social search built from a place, area, entity or situation (names and multilingual"
                + " aliases, nearby facilities, event terms of the families in the region's languages) on the enabled"
                + " search-capable platforms within a cost cap. Results are a targeted sample: they join narratives and"
                + " are compared with physical evidence, but never feed baselines or corroborate what cued them.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("SocialCue");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public boolean costBearing() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            for (String f : new String[] {"placeId", "regionCode", "bbox", "entityRef", "situationId", "terms",
                    "families", "platforms", "maxCostUsd"}) {
                if (args != null && args.has(f) && !args.get(f).isNull()) {
                    body.put(f, json.treeToValue(args.get(f), Object.class));
                }
            }
            Map<String, Object> window = new LinkedHashMap<>();
            window.put("start", ListNewsStoriesTool.text(args, "windowStart"));
            if (ListNewsStoriesTool.text(args, "windowEnd") != null) {
                window.put("end", ListNewsStoriesTool.text(args, "windowEnd"));
            }
            body.put("window", window);
            return ToolResult.json(json.valueToTree(cues.createManual(SocialCueService.parse(json.valueToTree(body)),
                    context.actor())));
        } catch (RuntimeException e) {
            return ToolResult.error(e.getMessage());
        }
    }
}
