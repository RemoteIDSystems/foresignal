package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.bool;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.trip.TripRunEntity;
import ai.foresignal.trip.TripService;
import ai.foresignal.trip.TripWatch;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code check_trip} (agent; prompt 114): runs a trip's pre-trip check now and returns the hazards on and near the
 * corridor, ordered along the route, with the briefing. A dry run by default (nothing is delivered); pass
 * {@code dryRun: false} to deliver to the trip's channels. Hazards from sources hidden from the caller are left out
 * of the result.
 */
@Component
@ai.foresignal.security.restrict.RestrictedRead
public class CheckTripTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "tripId", string("The trip id", 26, 26),
            "dryRun", bool("Return the briefing without delivering (default true)", Boolean.TRUE)), "tripId");

    private final TripWatch watch;
    private final JsonMapper json;

    /** Creates the tool. */
    public CheckTripTool(TripWatch watch, JsonMapper json) {
        this.watch = watch;
        this.json = json;
    }

    @Override
    public String name() {
        return "check_trip";
    }

    @Override
    public String title() {
        return "Check a trip corridor";
    }

    @Override
    public String description() {
        return "Pre-trip check of a saved trip or commute: hazards (weather alerts, road events, work zones, fire"
                + " perimeters, cyclone cones, M4+ earthquakes, incidents) on the route, near it (within 2 × the buffer)"
                + " and forecast for the trip window, ordered along the route, with the markdown briefing. Dry run by"
                + " default. Needs an agent token. " + UNTRUSTED_NOTE;
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A TripRun");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        JsonNode id = args == null ? null : args.get("tripId");
        if (id == null || !id.isString()) {
            return ToolResult.error("tripId is required");
        }
        JsonNode dry = args.get("dryRun");
        boolean dryRun = dry == null || dry.isNull() || dry.asBoolean(true);
        try {
            TripRunEntity run = watch.check(id.asString(), dryRun, null);
            Map<String, Object> out = new LinkedHashMap<>(TripService.runView(run));
            List<Map<String, Object>> visible = new ArrayList<>();
            for (Map<String, Object> hazard : run.getHazards()) {
                if (!context.excludedSources().contains(String.valueOf(hazard.get("sourceId")))) {
                    visible.add(hazard);
                }
            }
            out.put("hazards", visible);
            return ToolResult.json(json.valueToTree(out));
        } catch (ForesignalException e) {
            return ToolResult.error(e.getMessage());
        }
    }
}
