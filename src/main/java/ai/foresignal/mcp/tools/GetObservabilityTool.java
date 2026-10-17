package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.number;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.observability.AbsenceCheck;
import ai.foresignal.observability.ObservabilityService;
import ai.foresignal.observability.Place;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_observability} (read, prompt 116): the detection probability of a family at a point, a region or
 * everywhere at a time, with whether an absence there may be read as "nothing happened".
 */
@Component
@RestrictedRead("source footprints only; no records are returned")
public class GetObservabilityTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "family", string("The signal family, e.g. seismic, fire, conflict, outage, aircraft, vessel.", 1, 64),
            "region", string("A region code (040), e.g. US or US-CA.", 1, 16),
            "lon", number("Longitude of a point (with lat).", -180, 180),
            "lat", number("Latitude of a point (with lon).", -90, 90),
            "at", dateTime("The time the estimates must be valid at (default now)."),
            "band", string("A threshold band, e.g. M4.5-5.", 1, 32)), "family");

    private final ObservabilityService observability;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the tool. */
    public GetObservabilityTool(ObservabilityService observability, JsonMapper json, Clock clock) {
        this.observability = observability;
        this.json = json;
        this.clock = clock;
    }

    @Override
    public String name() {
        return "get_observability";
    }

    @Override
    public String title() {
        return "Detection probability";
    }

    @Override
    public String description() {
        return "How likely Foresignal's sources were to detect a phenomenon of a family at a place and time (p, an"
                + " interval and the methods: capture-recapture, presence-proxy, receiver-range or declared). When"
                + " absenceOk is false (reason LOW_DETECTABILITY) an absence of reports there must be stated as"
                + " unknown, never as \"nothing happened\". Estimates valid at the given time are used.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A Detectability");
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
    public ToolResult call(ToolContext context, JsonNode args) {
        try {
            String family = McpArgs.requiredText(args, "family");
            Instant at = McpArgs.instant(args, "at");
            String region = McpArgs.text(args, "region");
            Place place = Place.GLOBAL;
            if (args.hasNonNull("lon") && args.hasNonNull("lat")) {
                place = Place.point(args.get("lon").asDouble(), args.get("lat").asDouble());
            } else if (region != null) {
                place = Place.region(region);
            }
            ObservabilityService.Detectability d = observability.pDetect(family, place,
                    at == null ? clock.instant() : at, McpArgs.text(args, "band"));
            boolean ok = d.p() >= observability.absenceMinDetect();
            return ToolResult.json(json.valueToTree(d.view(ok, ok ? null : AbsenceCheck.LOW_DETECTABILITY, true)));
        } catch (ForesignalException e) {
            return ToolResult.error(e.getMessage());
        }
    }
}
