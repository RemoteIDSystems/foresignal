package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.bool;
import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.nonNegative;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.briefing.DigestService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/** {@code get_digest} (read): the catch-up digest since a seq cursor, as {@code getDigest} (prompt 029). */
@Component
public class GetDigestTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "sinceSeq", nonNegative("The seq cursor: next.sinceSeq from the previous call. Omit for the last 24 hours."),
            "sinceAlertId", nonNegative("The alert cursor: next.sinceAlertId from the previous call. Omit for alerts of"
                    + " the last 24 hours."),
            "source", stringArray("Source ids (from list_sources); omit for every source.", 50),
            "kind", stringArray("Record kinds (from list_kinds); omit for every kind.", 50),
            "bbox", string("Area filter as minLon,minLat,maxLon,maxLat in WGS84 degrees."),
            "limit", integer("Notable records per kind.", 1, DigestService.MAX_LIMIT, (long) DigestService.DEFAULT_LIMIT),
            "includeHealth", bool("Include degraded and failing sources (default true).", true),
            "detail", detail()));

    private final DigestService digests;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public GetDigestTool(DigestService digests, McpOutputShaper shaper) {
        this.digests = digests;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "get_digest";
    }

    @Override
    public String title() {
        return "Catch-up digest";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Summarizes what changed since a cursor: version counts per source and kind (new,"
                + " changed, removed, trigger edges; routine keyframes and samples are reported separately and are not"
                + " change), entities active from live state, alerts since an alert id (critical first), notable records"
                + " with persisted seqs to cite as [source:externalId@seq], and source health. gap=true means sinceSeq is"
                + " below the retention floor and counts are incomplete. Pass next.sinceSeq and next.sinceAlertId on the"
                + " following call.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The Digest document");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        DigestService.Params params = new DigestService.Params(McpArgs.optionalLong(args, "sinceSeq"),
                McpArgs.optionalLong(args, "sinceAlertId"), McpArgs.strings(args, "source"),
                McpArgs.strings(args, "kind"), McpArgs.text(args, "bbox"),
                McpArgs.integer(args, "limit", DigestService.DEFAULT_LIMIT), McpArgs.bool(args, "includeHealth", true),
                List.of());
        Map<String, Object> digest = digests.digest(params, context.caller());
        return ToolResult.json(shaper.object(shaper.objectTree(digest), Detail.of(args)));
    }
}
