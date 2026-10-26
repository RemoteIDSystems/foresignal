package ai.foresignal.api;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.egress.EgressProbe;
import ai.foresignal.egress.EgressRouteService;
import ai.foresignal.egress.EgressRouteStore;
import ai.foresignal.egress.EgressRouter;
import ai.foresignal.egress.EgressSettingsService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Egress routing & IP rotation (OpenAPI tag {@code Egress}, prompt R2-018; admin, {@code network.manage}): the egress
 * pool ({@code listEgressRoutes}, {@code createEgressRoute}, {@code getEgressRoute}, {@code updateEgressRoute},
 * {@code deleteEgressRoute}, {@code testEgressRoute}, {@code getEgressRouteHealth}), the policy
 * ({@code getEgressSettings}, {@code updateEgressSettings}) and the per-source assignment ({@code assignSourceEgress}).
 */
@RestController
@Tag(name = "Egress")
public class EgressController {

    /** {@code assignSourceEgress} of an unknown source. */
    public static final String UNKNOWN_SOURCE = "UNKNOWN_SOURCE";

    private final EgressRouteService routes;
    private final EgressRouteStore store;
    private final EgressSettingsService settings;
    private final EgressProbe probe;
    private final EgressRouter router;
    private final SourceRegistry registry;
    private final SourceService sources;

    /** Creates the controller. */
    public EgressController(EgressRouteService routes, EgressRouteStore store, EgressSettingsService settings,
            EgressProbe probe, EgressRouter router, SourceRegistry registry, SourceService sources) {
        this.routes = routes;
        this.store = store;
        this.settings = settings;
        this.probe = probe;
        this.router = router;
        this.registry = registry;
        this.sources = sources;
    }

    /** {@code listEgressRoutes}: the pool by name, with each route's health and exit IP. */
    @GetMapping("/v1/egress")
    @Operation(operationId = "listEgressRoutes", tags = "Egress")
    public Map<String, Object> listEgressRoutes(@RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        EgressRouteStore.Page page;
        try {
            page = store.page(cursor == null || cursor.isBlank() ? null : cursor,
                    BulkExportsController.parseLimit(limit));
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField("cursor", e.getMessage());
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items().stream().map(routes::view).toList());
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    /** {@code createEgressRoute}: 201 with the route; its secrets are sealed and never returned. */
    @PostMapping("/v1/egress")
    @Operation(operationId = "createEgressRoute", tags = "Egress")
    public ResponseEntity<Map<String, Object>> createEgressRoute(@RequestBody(required = false) JsonNode body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routes.view(routes.create(body)));
    }

    /** {@code getEgressRoute}. */
    @GetMapping("/v1/egress/{id}")
    @Operation(operationId = "getEgressRoute", tags = "Egress")
    public Map<String, Object> getEgressRoute(@PathVariable String id) {
        return routes.view(routes.get(routeId(id)));
    }

    /** {@code updateEgressRoute}: replaces the route; an omitted secret keeps the stored one. */
    @PutMapping("/v1/egress/{id}")
    @Operation(operationId = "updateEgressRoute", tags = "Egress")
    public Map<String, Object> updateEgressRoute(@PathVariable String id,
            @RequestBody(required = false) JsonNode body) {
        return routes.view(routes.replace(routeId(id), body));
    }

    /** {@code deleteEgressRoute}: 204; pinned sources fall back to the default route or rotation. */
    @DeleteMapping("/v1/egress/{id}")
    @Operation(operationId = "deleteEgressRoute", tags = "Egress")
    public ResponseEntity<Void> deleteEgressRoute(@PathVariable String id) {
        routes.delete(routeId(id));
        return ResponseEntity.noContent().build();
    }

    /** {@code testEgressRoute}: one call to the exit-IP service through the route. */
    @PostMapping("/v1/egress/{id}/test")
    @Operation(operationId = "testEgressRoute", tags = "Egress")
    public Map<String, Object> testEgressRoute(@PathVariable String id) {
        return probe.test(routes.get(routeId(id)));
    }

    /** {@code getEgressRouteHealth}: reachability, latency, exit IP and the sources held off the route. */
    @GetMapping("/v1/egress/{id}/health")
    @Operation(operationId = "getEgressRouteHealth", tags = "Egress")
    public Map<String, Object> getEgressRouteHealth(@PathVariable String id) {
        return routes.healthView(routes.get(routeId(id)));
    }

    /** {@code getEgressSettings}. */
    @GetMapping("/v1/settings/egress")
    @Operation(operationId = "getEgressSettings", tags = "Egress")
    public Map<String, Object> getEgressSettings() {
        return EgressSettingsService.view(settings.get());
    }

    /** {@code updateEgressSettings}: replaces the rotation policy and default route. */
    @PutMapping("/v1/settings/egress")
    @Operation(operationId = "updateEgressSettings", tags = "Egress")
    public Map<String, Object> updateEgressSettings(@RequestBody(required = false) JsonNode body) {
        return EgressSettingsService.view(settings.update(body));
    }

    /** {@code assignSourceEgress}: pins a source to a route, or keeps it out of rotation. */
    @PutMapping("/v1/sources/{sourceId}/egress")
    @Operation(operationId = "assignSourceEgress", tags = "Egress")
    public Map<String, Object> assignSourceEgress(@PathVariable String sourceId,
            @RequestBody(required = false) JsonNode body) {
        if (!registry.contains(sourceId)) {
            throw new NotFoundException(UNKNOWN_SOURCE, "No source has the id " + sourceId + ".");
        }
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a SourceEgressAssignment object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String name : body.propertyNames()) {
            if (!name.equals("routeId") && !name.equals("excludeFromRotation")) {
                errors.add(new FieldError(name, "is not a member of SourceEgressAssignment"));
            }
        }
        UUID routeId = null;
        JsonNode routeNode = body.get("routeId");
        if (routeNode != null && !routeNode.isNull()) {
            try {
                routeId = UUID.fromString(routeNode.asString());
                if (store.find(routeId).isEmpty()) {
                    errors.add(new FieldError("routeId", "names no egress route"));
                }
            } catch (IllegalArgumentException | UnsupportedOperationException e) {
                errors.add(new FieldError("routeId", "must be an egress route id (a UUID)"));
            }
        }
        boolean excluded = false;
        JsonNode excludedNode = body.get("excludeFromRotation");
        if (excludedNode != null && !excludedNode.isNull()) {
            if (!excludedNode.isBoolean()) {
                errors.add(new FieldError("excludeFromRotation", "must be a boolean"));
            } else {
                excluded = excludedNode.asBoolean();
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The egress assignment is not valid.", errors);
        }
        sources.setEgressAssignment(sourceId, routeId, excluded);
        router.invalidate();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("routeId", routeId);
        out.put("excludeFromRotation", excluded);
        return out;
    }

    private static UUID routeId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException(EgressRouteService.UNKNOWN_EGRESS, "No egress route has the id " + id + ".");
        }
    }
}
