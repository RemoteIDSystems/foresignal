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
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_eta} (read; prompt 114): the ETA of a record to a place or a {@code lon,lat} point along its current
 * track, in a compact shape. A place target needs an agent token (places are never coordinates for read tokens).
 */
@Component
@ai.foresignal.security.restrict.RestrictedRead
public class GetEtaTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "record", string("The record as {sourceId}/{externalId}", 3, 330),
            "to", string("A place id, or lon,lat", 1, 100)), "record", "to");

    private final KinematicService kinematics;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetEtaTool(KinematicService kinematics, JsonMapper json) {
        this.kinematics = kinematics;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_eta";
    }

    @Override
    public String title() {
        return "ETA to a place or point";
    }

    @Override
    public String description() {
        return "ETA of a moving record to a place id or lon,lat: distanceMeters, bearingDiffDeg, approaching (heading"
                + " within 45° at 0.5 m/s or more), etaSeconds and etaAt when approaching, and stale. Refuses states older"
                + " than 1 h. " + UNTRUSTED_NOTE;
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A compact EtaResult");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        try {
            RecordKey record = RecordKey.parse(GetCpaTool.text(args, "record"), "record");
            boolean mayReadPlaces = context.caller().scope() != null && context.caller().scope().includes(Scope.AGENT);
            Map<String, Object> full = kinematics.eta(record, GetCpaTool.text(args, "to"), mayReadPlaces,
                    context.excludedSources(), context.restriction());
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("record", record.toString());
            for (String key : new String[] {"distanceMeters", "bearingDeg", "bearingDiffDeg", "approaching", "etaSeconds",
                    "etaAt", "asOf", "stale"}) {
                out.put(key, full.get(key));
            }
            return ToolResult.json(json.valueToTree(out));
        } catch (ForesignalException e) {
            return ToolResult.error(e.getMessage());
        }
    }
}
