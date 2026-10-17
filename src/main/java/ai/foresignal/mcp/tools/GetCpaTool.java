package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.kinematics.KinematicService;
import ai.foresignal.kinematics.KinematicService.RecordKey;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_cpa} (read; prompt 114): the closest point of approach of two records under constant velocity, in a
 * compact shape. Reads under the caller's excluded sources and restriction.
 */
@Component
@ai.foresignal.security.restrict.RestrictedRead
public class GetCpaTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "a", string("The first record as {sourceId}/{externalId}", 3, 330),
            "b", string("The second record as {sourceId}/{externalId}", 3, 330),
            "horizon", string("The CPA horizon, for example 2h (default 2h, at most 24h)")), "a", "b");

    private final KinematicService kinematics;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetCpaTool(KinematicService kinematics, JsonMapper json) {
        this.kinematics = kinematics;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_cpa";
    }

    @Override
    public String title() {
        return "Closest point of approach";
    }

    @Override
    public String description() {
        return "Closest point of approach of two moving records (aircraft, vessels, devices) under constant velocity:"
                + " dcpaMeters, tcpaSeconds (0 when diverging), cpaAt, vertical separation when both have altitude,"
                + " and stale. Refuses pairs more than 500 km apart and states older than 1 h. " + UNTRUSTED_NOTE;
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A compact CpaResult");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        try {
            RecordKey a = RecordKey.parse(text(args, "a"), "a");
            RecordKey b = RecordKey.parse(text(args, "b"), "b");
            Duration horizon = KinematicService.parseHorizon(text(args, "horizon"), "horizon");
            Map<String, Object> full = kinematics.cpa(a, b, horizon, context.excludedSources(), context.restriction());
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("a", a.toString());
            out.put("b", b.toString());
            for (String key : new String[] {"t0", "tcpaSeconds", "cpaAt", "dcpaMeters", "verticalSeparationM", "horizon",
                    "asOf", "stale"}) {
                out.put(key, full.get(key));
            }
            return ToolResult.json(json.valueToTree(out));
        } catch (ForesignalException e) {
            return ToolResult.error(e.getMessage());
        }
    }

    static String text(JsonNode args, String field) {
        JsonNode n = args == null ? null : args.get(field);
        return n == null || n.isNull() ? null : n.asString();
    }
}
