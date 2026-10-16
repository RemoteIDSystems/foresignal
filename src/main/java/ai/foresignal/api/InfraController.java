package ai.foresignal.api;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.infra.InfraBuildEntity;
import ai.foresignal.infra.InfraGraphBuilder;
import ai.foresignal.infra.InfraImpactService;
import ai.foresignal.infra.InfraNodeQuery;
import ai.foresignal.infra.InfraRebuildScheduler;
import ai.foresignal.intel.IntelEventService;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.source.spi.Geo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The infrastructure graph API (prompt 104). Reads serve only the current (newest successful)
 * build; {@code POST /v1/infra/rebuild} answers 202 at once and builds in the background, or 409
 * {@code INFRA_REBUILD_RUNNING} while a build is already running.
 */
@RestController
@Tag(name = "Infrastructure")
@RequestMapping("/v1/infra")
public class InfraController {

    private final JdbcTemplate jdbc;
    private final InfraGraphBuilder builder;
    private final InfraRebuildScheduler rebuilds;
    private final InfraImpactService impact;
    private final InfraNodeQuery query;
    private final IntelEventService events;
    private final IntelVisibility visibility;

    /** Creates the controller. */
    public InfraController(JdbcTemplate jdbc, InfraGraphBuilder builder, InfraRebuildScheduler rebuilds,
            InfraImpactService impact, InfraNodeQuery query, IntelEventService events, IntelVisibility visibility) {
        this.jdbc = jdbc;
        this.builder = builder;
        this.rebuilds = rebuilds;
        this.impact = impact;
        this.query = query;
        this.events = events;
        this.visibility = visibility;
    }

    /** {@code listInfraNodes}. */
    @GetMapping("/nodes")
    @Operation(operationId = "listInfraNodes", tags = "Infrastructure")
    public Map<String, Object> listInfraNodes(@RequestParam(required = false) String bbox,
            @RequestParam(required = false) List<String> type,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        int size = QueryParams.intInRange("limit", limit, 1, 500, 100);
        return query.nodes(bbox, type == null ? List.of() : type, q, cursor, size);
    }

    /** {@code getInfraNode}. */
    @GetMapping("/nodes/{id}")
    @Operation(operationId = "getInfraNode", tags = "Infrastructure")
    public Map<String, Object> getInfraNode(@PathVariable long id) {
        return query.node(id);
    }

    /** {@code listInfraEdges}. */
    @GetMapping("/edges")
    @Operation(operationId = "listInfraEdges", tags = "Infrastructure")
    public Map<String, Object> listInfraEdges(@RequestParam(required = false) String bbox,
            @RequestParam(required = false) List<String> method,
            @RequestParam(required = false) List<String> label,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        int size = QueryParams.intInRange("limit", limit, 1, 500, 100);
        return query.edges(bbox, method == null ? List.of() : method, label == null ? List.of() : label, cursor, size);
    }

    /** {@code computeAffectedInfrastructure}: one of {@code geometry}, {@code eventId} or {@code record}. */
    @PostMapping("/affected")
    @Operation(operationId = "computeAffectedInfrastructure", tags = "Infrastructure")
    public Map<String, Object> computeAffectedInfrastructure(@RequestBody JsonNode body, HttpServletRequest request) {
        InfraImpactService.Seed seed = seed(impact, body, events, visibility.seesPrivate(request));
        int depth = body.path("depth").asInt(2);
        String minLabel = body.path("minLabel").asString("possible");
        List<String> types = new ArrayList<>();
        if (body.has("types") && body.get("types").isArray()) {
            for (JsonNode node : body.get("types")) {
                types.add(node.asString());
            }
        }
        return impact.affected(seed, depth, minLabel, types);
    }

    /**
     * The seed of an affected-dependents request: exactly one of {@code geometry}, {@code eventId} or
     * {@code record {sourceId, externalId}}. The event is read through the shared visibility rule
     * ({@link IntelEventService#visible}), so a hidden or missing event answers {@code 404 UNKNOWN_EVENT}
     * exactly as the intel endpoints do.
     *
     * @param impact the impact service
     * @param body the request body or tool arguments
     * @param events the intelligence events
     * @param seesPrivate the caller may see private events ({@link IntelVisibility#seesPrivate})
     * @return the seed
     */
    public static InfraImpactService.Seed seed(InfraImpactService impact, JsonNode body, IntelEventService events,
            boolean seesPrivate) {
        int given = (body.hasNonNull("geometry") ? 1 : 0) + (body.hasNonNull("eventId") ? 1 : 0)
                + (body.hasNonNull("record") ? 1 : 0);
        if (given != 1) {
            throw ai.foresignal.api.error.ValidationException.forField("geometry",
                    "give exactly one of geometry, eventId or record");
        }
        if (body.hasNonNull("geometry")) {
            return new InfraImpactService.Seed(Geo.fromGeoJson(body.get("geometry").toString()), false, "geometry");
        }
        if (body.hasNonNull("eventId")) {
            return impact.eventSeed(events.visible(body.get("eventId").asString(), seesPrivate));
        }
        JsonNode record = body.get("record");
        if (!record.hasNonNull("sourceId") || !record.hasNonNull("externalId")) {
            throw ai.foresignal.api.error.ValidationException.forField("record", "needs sourceId and externalId");
        }
        return impact.recordSeed(record.get("sourceId").asString(), record.get("externalId").asString());
    }

    /** {@code getInfraStatus}: whether a graph is built or building, the last build and node counts. */
    @GetMapping("/status")
    @Operation(operationId = "getInfraStatus", tags = "Infrastructure")
    public Map<String, Object> getInfraStatus() {
        Map<String, Object> out = new LinkedHashMap<>();
        InfraBuildEntity current = builder.current().orElse(null);
        out.put("built", current != null);
        out.put("building", builder.running().isPresent());
        if (current != null) {
            out.put("id", current.getId());
            out.put("started", current.getStarted());
            out.put("finished", current.getFinished());
            out.put("rulesVersion", current.getRulesVersion());
            out.put("nodes", current.getNodes());
            out.put("edgesByMethod", current.getEdgesByMethod());
            out.put("edgesByLabel", current.getEdgesByLabel());
            out.put("lastBuild", build(current));
            out.put("osmExtractDate", current.getOsmExtractDate());
            Map<String, Object> byType = new LinkedHashMap<>();
            jdbc.query("SELECT facility_type, count(*) FROM infra_node WHERE build_id = ? GROUP BY facility_type"
                    + " ORDER BY facility_type", rs -> {
                        byType.put(rs.getString(1), rs.getLong(2));
                    }, current.getId());
            out.put("nodesByType", byType);
        }
        return out;
    }

    /**
     * {@code rebuildInfraGraph}: 202 with the new build, which runs in the background.
     *
     * @throws ConflictException 409 {@code INFRA_REBUILD_RUNNING} while a build is already running
     */
    @PostMapping("/rebuild")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(operationId = "rebuildInfraGraph", tags = "Infrastructure")
    public Map<String, Object> rebuildInfraGraph() {
        InfraGraphBuilder.Started started = rebuilds.startRebuild();
        if (!started.created()) {
            throw new ConflictException(ErrorCodes.INFRA_REBUILD_RUNNING,
                    "An infrastructure graph build is already running: " + started.build().getId());
        }
        return build(started.build());
    }

    /** The API shape of a build record. */
    static Map<String, Object> build(InfraBuildEntity build) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", build.getId());
        out.put("buildId", build.getId());
        out.put("status", build.status());
        out.put("startedAt", build.getStarted());
        out.put("finishedAt", build.getFinished());
        out.put("rulesVersion", String.valueOf(build.getRulesVersion()));
        out.put("inputs", build.getInputs() == null ? Map.of() : build.getInputs());
        out.put("nodes", build.getNodes());
        out.put("edgesByMethod", build.getEdgesByMethod() == null ? Map.of() : build.getEdgesByMethod());
        out.put("edgesByLabel", build.getEdgesByLabel() == null ? Map.of() : build.getEdgesByLabel());
        out.put("warnings", build.getWarnings() == null ? List.of() : build.getWarnings());
        out.put("error", build.getError());
        return out;
    }
}
