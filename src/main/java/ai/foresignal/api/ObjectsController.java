package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.AsOfParam;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.ETags;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.object.AnnotationEntity;
import ai.foresignal.object.AnnotationService;
import ai.foresignal.object.ContextLinks;
import ai.foresignal.object.ContextSummaryService;
import ai.foresignal.object.DossierService;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectService;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.PrecisionService;
import ai.foresignal.object.RelationService;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.object.SensorDiscoveryService;
import ai.foresignal.security.restrict.RestrictedRead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The object contract endpoints (prompt 129, tag {@code Objects}): {@code getContract}, {@code getObject},
 * {@code resolveObjects}, {@code getObjectDossier}, {@code listRelatedObjects}, {@code summarizeContext},
 * {@code queryGraph}, {@code findCommonDependencies}, {@code createRelation}, {@code deleteRelation},
 * {@code listObjectAnnotations}, {@code createAnnotation}, {@code updateAnnotation}, {@code deleteAnnotation},
 * {@code getPrecision}, {@code listSensors} and {@code createContextLink}.
 *
 * <p>An object reference is one path segment with {@code /} percent-encoded ({@code record:adsblol-mil%2Fae1234}).
 * Private and unshared objects are invisible (404 {@code UNKNOWN_OBJECT}); annotations need a token.
 */
@RestController
@Tag(name = "Objects")
public class ObjectsController {

    private final ObjectService objects;
    private final ObjectCallers callers;
    private final DossierService dossiers;
    private final RelationService relations;
    private final ContextSummaryService summaries;
    private final AnnotationService annotations;
    private final PrecisionService precision;
    private final SensorDiscoveryService sensors;

    /** Creates the controller. */
    public ObjectsController(ObjectService objects, ObjectCallers callers, DossierService dossiers,
            RelationService relations, ContextSummaryService summaries, AnnotationService annotations,
            PrecisionService precision, SensorDiscoveryService sensors) {
        this.objects = objects;
        this.callers = callers;
        this.dossiers = dossiers;
        this.relations = relations;
        this.summaries = summaries;
        this.annotations = annotations;
        this.precision = precision;
        this.sensors = sensors;
    }

    /** {@code getContract}: the contract versions in force. */
    @GetMapping("/v1/contract")
    @Operation(operationId = "getContract", tags = "Objects")
    public Map<String, Object> getContract() {
        return contract();
    }

    /** The {@code Contract} view. */
    public static Map<String, Object> contract() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("contractVersion", ObjectTypes.CONTRACT_VERSION);
        out.put("objectTypes", ObjectTypes.ALL);
        out.put("relationTypes", ObjectTypes.RELATION_TYPES);
        out.put("contextSchemaVersion", ObjectTypes.CONTEXT_SCHEMA_VERSION);
        out.put("deepLinkVersion", ObjectTypes.DEEP_LINK_VERSION);
        return out;
    }

    /** {@code getObject}: the envelope, {@code asOf}-aware, with the caller's actions and an ETag. */
    @GetMapping("/v1/objects/{ref}")
    @RestrictedRead("each resolver applies its object's private rule and the caller's excluded sources")
    @Operation(operationId = "getObject", tags = "Objects")
    public ResponseEntity<ObjectEnvelope> getObject(@PathVariable String ref,
            @RequestParam(required = false) String asOf, HttpServletRequest request) {
        ObjectEnvelope envelope = objects.get(ObjectRef.parse(ref), context(request, asOf));
        return ResponseEntity.ok().eTag(ETags.forDocument(envelope.toString())).body(envelope);
    }

    /** {@code resolveObjects}: up to 500 references; missing ones are listed, not errors. */
    @PostMapping("/v1/objects/resolve")
    @RestrictedRead("each resolver applies its object's private rule; invisible references are listed as missing")
    @Operation(operationId = "resolveObjects", tags = "Objects")
    public Map<String, Object> resolveObjects(@RequestBody JsonNode body, HttpServletRequest request) {
        if (body == null || !body.isObject() || !body.has("refs") || !body.get("refs").isArray()) {
            throw ObjectRef.invalid("refs", "must be an array of references");
        }
        List<String> refs = new ArrayList<>();
        body.get("refs").forEach(r -> refs.add(r.asString()));
        String asOf = body.hasNonNull("asOf") ? body.get("asOf").asString() : null;
        ObjectService.Resolved resolved = objects.resolve(refs, context(request, asOf));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", resolved.items());
        out.put("missing", resolved.missing());
        out.put("asOf", resolved.asOf());
        return out;
    }

    /** {@code getObjectDossier}: the envelope plus bounded sections (≤ 64 KB), with an ETag. */
    @GetMapping("/v1/objects/{ref}/dossier")
    @RestrictedRead("sections read through the object resolvers, relations and record queries with the caller's"
            + " private rule and excluded sources")
    @Operation(operationId = "getObjectDossier", tags = "Objects")
    public ResponseEntity<Map<String, Object>> getObjectDossier(@PathVariable String ref,
            @RequestParam(required = false) List<String> include, @RequestParam(required = false) String asOf,
            @RequestParam(required = false) String facets,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        List<String> sections = DossierService.includes(QueryParams.multi("include", include));
        Map<String, Object> dossier = dossiers.dossier(ObjectRef.parse(ref), sections,
                QueryParams.bool("facets", facets, false), context(request, asOf));
        String etag = dossiers.etag(dossier);
        if (ETags.matches(ifNoneMatch, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }
        return ResponseEntity.ok().eTag(etag).contentType(MediaType.APPLICATION_JSON).body(dossier);
    }

    /** {@code listRelatedObjects}: the neighborhood through the relation graph. */
    @GetMapping("/v1/objects/{ref}/related")
    @RestrictedRead("neighbors resolve through the object resolvers; private relations need private access")
    @Operation(operationId = "listRelatedObjects", tags = "Objects")
    public Map<String, Object> listRelatedObjects(@PathVariable String ref,
            @RequestParam(required = false) List<String> types, @RequestParam(required = false) String direction,
            @RequestParam(required = false) String asOf, @RequestParam(required = false) String minConfidence,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        Set<String> typeSet = RelationService.relationTypes(QueryParams.multi("types", types));
        String dir = QueryParams.oneOf("direction", direction, List.of("out", "in", "both"), "both");
        Double min = QueryParams.doubleInRange("minConfidence", minConfidence, 0, 1);
        int max = QueryParams.intInRange("limit", limit, 1, RelationService.MAX_RELATED, 100);
        return relations.related(ObjectRef.parse(ref), typeSet, dir, min == null ? 0 : min, max,
                context(request, asOf));
    }

    /** {@code summarizeContext}: counts, activity, insights and related objects for a viewport and window. */
    @PostMapping("/v1/context/summary")
    @RestrictedRead("aggregated counts and objects under the caller's private rule and excluded sources")
    @Operation(operationId = "summarizeContext", tags = "Objects")
    public Map<String, Object> summarizeContext(@RequestBody JsonNode body, HttpServletRequest request) {
        return summaries.summarize(body, new ResolveContext(callers.of(request), null));
    }

    /** {@code queryGraph}: nodes and edges within depth ≤ 2 of up to 50 seeds. */
    @PostMapping("/v1/graph")
    @RestrictedRead("nodes resolve through the object resolvers; edges to invisible nodes are left out")
    @Operation(operationId = "queryGraph", tags = "Objects")
    public Map<String, Object> queryGraph(@RequestBody JsonNode body, HttpServletRequest request) {
        return relations.graph(body, context(request, asOfOf(body)));
    }

    /** {@code findCommonDependencies}: shared facilities through depends-on and located-in paths. */
    @PostMapping("/v1/graph/common-dependencies")
    @RestrictedRead("dependencies resolve through the object resolvers under the caller's private rule")
    @Operation(operationId = "findCommonDependencies", tags = "Objects")
    public Map<String, Object> findCommonDependencies(@RequestBody JsonNode body, HttpServletRequest request) {
        return relations.commonDependencies(body, context(request, asOfOf(body)));
    }

    /** {@code createRelation}: an operator or agent relation (agent). */
    @PostMapping("/v1/relations")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createRelation", tags = "Objects")
    public Map<String, Object> createRelation(@RequestBody JsonNode body, HttpServletRequest request) {
        return RelationService.view(relations.create(body, new ResolveContext(callers.authenticated(request), null)));
    }

    /** {@code deleteRelation}: retires an operator relation (agent; projected relations are 409). */
    @DeleteMapping("/v1/relations/{relationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteRelation", tags = "Objects")
    public void deleteRelation(@PathVariable String relationId, HttpServletRequest request) {
        relations.retire(relationId, callers.authenticated(request));
    }

    /** {@code listObjectAnnotations}: notes and verdicts visible to the caller, newest first (token required). */
    @GetMapping("/v1/objects/{ref}/annotations")
    @RestrictedRead("private annotations and annotations of private objects need private access")
    @Operation(operationId = "listObjectAnnotations", tags = "Objects")
    public Map<String, Object> listObjectAnnotations(@PathVariable String ref,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit,
            @RequestParam(required = false) String thread, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        return annotations.list(ObjectRef.parse(ref), cursor == null || cursor.isBlank() ? null : cursor.strip(),
                QueryParams.intInRange("limit", limit, 1, 200, 50), new ResolveContext(caller, null));
    }

    /** {@code createAnnotation}: notes by any token, verdicts by agent scope. */
    @PostMapping("/v1/objects/{ref}/annotations")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createAnnotation", tags = "Objects")
    public Map<String, Object> createAnnotation(@PathVariable String ref, @RequestBody JsonNode body,
            HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        AnnotationEntity a = annotations.create(ObjectRef.parse(ref), body, new ResolveContext(caller, null));
        return AnnotationService.view(a);
    }

    /** {@code updateAnnotation}: a JSON merge patch by the author or an admin. */
    @PatchMapping(value = "/v1/annotations/{annotationId}", consumes = "application/merge-patch+json")
    @Operation(operationId = "updateAnnotation", tags = "Objects")
    public Map<String, Object> updateAnnotation(@PathVariable String annotationId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return AnnotationService.view(annotations.patch(annotationId, body, callers.authenticated(request)));
    }

    /** {@code deleteAnnotation}: a soft delete by the author or an admin. */
    @DeleteMapping("/v1/annotations/{annotationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteAnnotation", tags = "Objects")
    public void deleteAnnotation(@PathVariable String annotationId, HttpServletRequest request) {
        annotations.delete(annotationId, callers.authenticated(request));
    }

    /** {@code getPrecision}: share confirmed among verdicts per producer, with Wilson intervals. */
    @GetMapping("/v1/analytics/precision")
    @Operation(operationId = "getPrecision", tags = "Objects")
    public Map<String, Object> getPrecision(@RequestParam(required = false) String component,
            @RequestParam(required = false) String since, HttpServletRequest request) {
        return precision.precision(component == null || component.isBlank() ? null : component.strip(),
                QueryParams.instant("since", since), callers.of(request).seesPrivate());
    }

    /** {@code listSensors}: what can observe an area in a window (read-only; no tasking). */
    @GetMapping("/v1/sensors")
    @RestrictedRead("sources, receivers and scenes the caller may not read are left out")
    @Operation(operationId = "listSensors", tags = "Objects")
    public Map<String, Object> listSensors(@RequestParam(required = false) String bbox,
            @RequestParam(required = false) String placeId, @RequestParam(required = false) String from,
            @RequestParam(required = false) String to, @RequestParam(required = false) List<String> kinds,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        Set<String> kindSet = new LinkedHashSet<>(QueryParams.enumSet("kinds", kinds,
                SensorDiscoveryService.KINDS));
        SensorDiscoveryService.Request q = new SensorDiscoveryService.Request(BboxParser.parse(bbox),
                placeId == null || placeId.isBlank() ? null : placeId.strip(), QueryParams.instant("from", from),
                QueryParams.instant("to", to), kindSet, QueryParams.intInRange("limit", limit, 1, 1000, 200));
        return sensors.discover(q, callers.of(request));
    }

    /** {@code createContextLink}: validates an ad-hoc context and returns its deep-link payload. */
    @PostMapping("/v1/context-links")
    @Operation(operationId = "createContextLink", tags = "Workspaces")
    public Map<String, Object> createContextLink(@RequestBody JsonNode body) {
        if (body == null || !body.isObject() || !body.hasNonNull("context")) {
            throw ValidationException.forField("context", "is required");
        }
        JsonNode ws = body.get("workspaceId");
        return ContextLinks.encode(ws == null || ws.isNull() ? null : ws.asString(), body.get("context"));
    }

    // ------------------------------------------------------------------------- helpers

    private ResolveContext context(HttpServletRequest request, String asOf) {
        AsOfParam parsed = asOf == null || asOf.isBlank() ? null : AsOfParam.parse(asOf);
        Instant at = parsed == null ? null : parsed.instant();
        return new ResolveContext(callers.of(request), at);
    }

    private static String asOfOf(JsonNode body) {
        return body != null && body.hasNonNull("asOf") ? body.get("asOf").asString() : null;
    }
}
