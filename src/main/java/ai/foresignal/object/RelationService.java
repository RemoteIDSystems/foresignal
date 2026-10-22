package ai.foresignal.object;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.infra.InfraImpactService;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.spi.Geo;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The relation graph (prompt 129, docs/Client-Contract.md §4): operator relations, neighborhoods
 * ({@code listRelatedObjects}), subgraphs ({@code queryGraph}) and shared dependencies
 * ({@code findCommonDependencies}).
 *
 * <p>Everything is time-aware: a relation counts when its validity contains {@code asOf} (or now), it was written by
 * then and not retired by then. {@code located-in} is computed on demand (places containing the object, 036), never
 * stored. {@code causes} is never machine-asserted: an operator relation of that type is labeled at most
 * {@code possible} unless an admin token (the operator) confirms it.
 */
@Service
public class RelationService {

    /** Most seed references of a graph query. */
    public static final int MAX_GRAPH_REFS = 50;

    /** Most edges of a graph query. */
    public static final int MAX_GRAPH_EDGES = 2_000;

    /** Most references of a common-dependencies query. */
    public static final int MAX_COMMON_REFS = 20;

    /** Most hops followed for common dependencies. */
    static final int COMMON_HOPS = 4;

    /** The ownership / affiliation edge types walked by an ownership-chain query (prompt 158). */
    public static final Set<String> OWNERSHIP_EDGES = Set.of(ObjectTypes.OWNS, ObjectTypes.CONTROLS,
            ObjectTypes.OFFICER_OF, ObjectTypes.BENEFICIAL_OWNER, ObjectTypes.AFFILIATED_WITH);

    /** Most hops followed for an ownership chain. */
    static final int OWNERSHIP_HOPS = 6;

    /** Most related objects returned. */
    public static final int MAX_RELATED = 500;

    private static final Logger log = LoggerFactory.getLogger(RelationService.class);

    private final RelationRepository relations;
    private final RelationQuery query;
    private final ObjectService objects;
    private final ObjectProvider<PlaceService> places;
    private final ObjectProvider<InfraImpactService> infra;
    private final JsonMapper json;
    private final Clock clock;
    private final GeoJsonReader reader = new GeoJsonReader();

    /** Creates the service. */
    public RelationService(RelationRepository relations, RelationQuery query, ObjectService objects,
            ObjectProvider<PlaceService> places, ObjectProvider<InfraImpactService> infra, JsonMapper json,
            Clock clock) {
        this.relations = relations;
        this.query = query;
        this.objects = objects;
        this.places = places;
        this.infra = infra;
        this.json = json;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- views

    /** The {@code Relation} view of a row. */
    public static Map<String, Object> view(RelationEntity r) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", r.getId());
        out.put("fromRef", r.getFromRef());
        out.put("toRef", r.getToRef());
        out.put("type", r.getType());
        out.put("validFrom", r.getValidFrom());
        out.put("validTo", r.getValidTo());
        out.put("confidence", r.getConfidence());
        out.put("label", r.getLabel());
        Map<String, Object> provenance = new LinkedHashMap<>();
        provenance.put("component", r.getProvenance().get("component"));
        provenance.put("version", r.getProvenance().get("version"));
        provenance.put("manifestId", r.getProvenance().get("manifestId"));
        provenance.put("operator", r.getProvenance().get("operator"));
        out.put("provenance", provenance);
        out.put("projected", r.isProjected());
        out.put("private", r.isPrivateRelation());
        out.put("createdAt", r.getCreatedAt());
        out.put("retiredAt", r.getRetiredAt());
        return out;
    }

    // ------------------------------------------------------------------------- operator relations

    /**
     * Adds an operator or agent relation ({@code createRelation}).
     *
     * @param body the {@code RelationInput}
     * @param context the caller (agent scope, checked by the access policy)
     * @return the new relation
     */
    @Transactional
    public RelationEntity create(JsonNode body, ResolveContext context) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The body must be a RelationInput object.");
        }
        ObjectRef from = ObjectRef.parse(text(body, "fromRef"), "fromRef");
        ObjectRef to = ObjectRef.parse(text(body, "toRef"), "toRef");
        String type = text(body, "type");
        if (!ObjectTypes.isRelationType(type)) {
            throw ValidationException.forField("type", "must be one of " + ObjectTypes.RELATION_TYPES);
        }
        if (ObjectTypes.LOCATED_IN.equals(type)) {
            throw ValidationException.forField("type", "located-in is computed on demand and is not stored");
        }
        if (from.withoutSeq().equals(to.withoutSeq())) {
            throw ValidationException.forField("toRef", "must differ from fromRef");
        }
        double confidence = body.has("confidence") && !body.get("confidence").isNull()
                ? body.get("confidence").asDouble() : 1.0;
        if (!(confidence >= 0 && confidence <= 1)) {
            throw ValidationException.forField("confidence", "must be between 0 and 1");
        }
        String label = text(body, "label");
        if (label != null && !ObjectTypes.LABELS.contains(label)) {
            throw ValidationException.forField("label", "must be one of " + ObjectTypes.LABELS);
        }
        if (label == null) {
            label = ObjectTypes.labelFor(confidence);
        }
        if (ObjectTypes.CAUSES.equals(type) && !context.caller().admin()) {
            // Never machine- or agent-asserted beyond "possible" until the operator confirms it.
            label = ObjectTypes.weaker(label, "possible");
            confidence = Math.min(confidence, 0.5);
        }
        Instant now = clock.instant();
        Instant validFrom = instant(body, "validFrom", now);
        Instant validTo = instant(body, "validTo", null);
        if (validTo != null && !validTo.isAfter(validFrom)) {
            throw ValidationException.forField("validTo", "must be after validFrom");
        }
        ResolveContext present = new ResolveContext(context.caller(), null);
        ObjectEnvelope fromEnvelope = objects.get(from, present);
        ObjectEnvelope toEnvelope = objects.get(to, present);
        boolean priv = (body.has("private") && body.get("private").asBoolean(false)) || fromEnvelope.privateObject()
                || toEnvelope.privateObject();
        RelationEntity r = new RelationEntity(Ulids.next(now), from.withoutSeq().format(), to.withoutSeq().format(),
                type, validFrom, now);
        r.setValidTo(validTo);
        r.setConfidence(confidence);
        r.setLabel(label);
        Map<String, Object> provenance = new LinkedHashMap<>();
        provenance.put("operator", context.caller().principal());
        r.setProvenance(provenance);
        r.setPrivateRelation(priv);
        return relations.save(r);
    }

    /**
     * Retires an operator relation ({@code deleteRelation}).
     *
     * @throws NotFoundException {@code UNKNOWN_RELATION}
     * @throws ConflictException {@code RELATION_NOT_EDITABLE} for a projected relation
     */
    @Transactional
    public void retire(String id, ObjectCaller caller) {
        RelationEntity r = relations.findById(id).filter(x -> x.getRetiredAt() == null)
                .filter(x -> !x.isPrivateRelation() || caller.seesPrivate())
                .orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_RELATION,
                        "No relation '" + id + "' exists, or you may not see it."));
        if (r.isProjected()) {
            throw new ConflictException(ErrorCodes.RELATION_NOT_EDITABLE,
                    "Relation '" + id + "' is projected from " + r.getOwnerKey() + " and is never edited by hand.");
        }
        Instant now = clock.instant();
        r.setRetiredAt(now);
        if (r.getValidTo() == null || r.getValidTo().isAfter(now)) {
            r.setValidTo(now);
        }
        relations.save(r);
    }

    // ------------------------------------------------------------------------- neighborhood

    /**
     * One related object.
     *
     * @param relation the relation (stored or computed)
     * @param direction {@code out} when the object is the relation's target, {@code in} when it is its source
     * @param object the neighbor's envelope
     * @param distanceKm the distance between the two objects' bbox centers, or null
     */
    public record Related(Map<String, Object> relation, String direction, ObjectEnvelope object, Double distanceKm) {}

    /**
     * The neighborhood of {@code ref} ({@code listRelatedObjects}).
     *
     * @param ref the object
     * @param types relation types (empty for all)
     * @param direction {@code out}, {@code in} or {@code both}
     * @param minConfidence the lowest confidence kept
     * @param limit the most items
     * @param context the caller and {@code asOf}
     * @return the {@code RelatedObjects} view
     */
    public Map<String, Object> related(ObjectRef ref, Set<String> types, String direction, double minConfidence,
            int limit, ResolveContext context) {
        ObjectEnvelope self = objects.get(ref, context);
        List<Related> items = neighbors(self, types, direction, minConfidence, limit + 1, context);
        boolean truncated = items.size() > limit;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ref", self.ref());
        out.put("asOf", context.asOf());
        out.put("items", items.subList(0, Math.min(limit, items.size())).stream().map(RelationService::relatedView)
                .toList());
        out.put("truncated", truncated);
        return out;
    }

    /** The {@code RelatedObject} view. */
    public static Map<String, Object> relatedView(Related r) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("relation", r.relation());
        out.put("direction", r.direction());
        out.put("object", r.object());
        out.put("distanceKm", r.distanceKm());
        return out;
    }

    /** The neighbors of an already-resolved object, stored and computed. */
    public List<Related> neighbors(ObjectEnvelope self, Set<String> types, String direction, double minConfidence,
            int limit, ResolveContext context) {
        Instant at = context.asOf() == null ? clock.instant() : context.asOf();
        Set<String> keys = keys(self.objectRef());
        List<RelationEntity> rows = query.touching(keys, types, direction, at, context.asOf() != null, minConfidence,
                context.seesPrivate(), Math.min(limit, MAX_GRAPH_EDGES) * 2);
        Map<ObjectRef, String> other = new LinkedHashMap<>();
        Map<ObjectRef, RelationEntity> via = new LinkedHashMap<>();
        for (RelationEntity r : rows) {
            boolean outgoing = keys.contains(r.getFromRef());
            ObjectRef neighbor = parseQuietly(outgoing ? r.getToRef() : r.getFromRef());
            if (neighbor == null || via.containsKey(neighbor)) {
                continue;
            }
            other.put(neighbor, outgoing ? "out" : "in");
            via.put(neighbor, r);
        }
        Map<ObjectRef, ObjectEnvelope> envelopes = objects.resolveRefs(other.keySet(), context);
        List<Related> out = new ArrayList<>();
        for (Map.Entry<ObjectRef, String> e : other.entrySet()) {
            ObjectEnvelope neighbor = envelopes.get(e.getKey());
            if (neighbor != null) {
                out.add(new Related(view(via.get(e.getKey())), e.getValue(), neighbor, distanceKm(self, neighbor)));
            }
        }
        boolean wantLocated = types.isEmpty() || types.contains(ObjectTypes.LOCATED_IN);
        if (wantLocated && !"in".equals(direction) && minConfidence <= 1.0) {
            out.addAll(locatedIn(self, context));
        }
        return out.size() > limit ? out.subList(0, limit) : out;
    }

    /** {@code located-in} computed on demand: the places (036) whose area contains or intersects the object. */
    public List<Related> locatedIn(ObjectEnvelope self, ResolveContext context) {
        PlaceService service = places.getIfAvailable();
        if (service == null || context.caller().anonymous() || self.geometry() == null
                || ObjectTypes.PLACE.equals(self.type())) {
            return List.of();
        }
        Geometry geometry = geometry(self);
        if (geometry == null) {
            return List.of();
        }
        List<Related> out = new ArrayList<>();
        for (PlaceView place : service.all()) {
            Geometry area = place.area() != null ? place.area() : place.geometry();
            try {
                if (area == null || !area.intersects(geometry)) {
                    continue;
                }
            } catch (RuntimeException e) {
                continue;
            }
            ObjectRef placeRef = ObjectRef.of(ObjectTypes.PLACE, place.id());
            objects.find(placeRef, context).ifPresent(env -> out.add(new Related(
                    computed(self.ref(), placeRef.format(), ObjectTypes.LOCATED_IN, "confirmed", 1.0,
                            place.createdAt()), "out", env, distanceKm(self, env))));
        }
        return out;
    }

    // ------------------------------------------------------------------------- subgraph

    /**
     * A subgraph from seed references ({@code queryGraph}).
     *
     * @param body the {@code GraphRequest}
     * @param context the caller and {@code asOf}
     * @return the {@code GraphResult} view
     */
    public Map<String, Object> graph(JsonNode body, ResolveContext context) {
        List<ObjectRef> seeds = refs(body, "refs", 1, MAX_GRAPH_REFS);
        int depth = body.has("depth") ? body.get("depth").asInt(1) : 1;
        if (depth < 1 || depth > 2) {
            throw ValidationException.forField("depth", "must be 1 or 2");
        }
        int limit = body.has("limit") ? body.get("limit").asInt(500) : 500;
        if (limit < 1 || limit > MAX_GRAPH_EDGES) {
            throw ValidationException.forField("limit", "must be between 1 and " + MAX_GRAPH_EDGES);
        }
        double minConfidence = body.has("minConfidence") ? body.get("minConfidence").asDouble(0) : 0;
        Set<String> types = relationTypes(body.get("types"), "types");
        return graph(seeds, depth, types, minConfidence, limit, context);
    }

    /** {@link #graph(JsonNode, ResolveContext)} with parsed parameters. */
    public Map<String, Object> graph(List<ObjectRef> seeds, int depth, Set<String> types, double minConfidence,
            int limit, ResolveContext context) {
        Instant at = context.asOf() == null ? clock.instant() : context.asOf();
        Map<String, RelationEntity> edges = new LinkedHashMap<>();
        Set<String> seen = new LinkedHashSet<>();
        Set<String> frontier = new LinkedHashSet<>();
        for (ObjectRef seed : seeds) {
            frontier.addAll(keys(seed));
        }
        seen.addAll(frontier);
        boolean truncated = false;
        for (int level = 0; level < depth && !frontier.isEmpty() && !truncated; level++) {
            List<RelationEntity> rows = query.touching(frontier, types, "both", at, context.asOf() != null,
                    minConfidence, context.seesPrivate(), limit - edges.size() + 1);
            Set<String> next = new LinkedHashSet<>();
            for (RelationEntity r : rows) {
                if (edges.size() >= limit) {
                    truncated = true;
                    break;
                }
                edges.putIfAbsent(r.getId(), r);
                for (String end : List.of(r.getFromRef(), r.getToRef())) {
                    if (seen.add(end)) {
                        next.add(end);
                    }
                }
            }
            frontier = next;
        }
        Set<ObjectRef> nodeRefs = new LinkedHashSet<>(seeds);
        for (RelationEntity r : edges.values()) {
            ObjectRef a = parseQuietly(r.getFromRef());
            ObjectRef b = parseQuietly(r.getToRef());
            if (a != null) {
                nodeRefs.add(a);
            }
            if (b != null) {
                nodeRefs.add(b);
            }
        }
        Map<ObjectRef, ObjectEnvelope> envelopes = objects.resolveRefs(nodeRefs, context);
        Set<String> visible = new LinkedHashSet<>();
        List<ObjectEnvelope> nodes = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (ObjectRef ref : nodeRefs) {
            ObjectEnvelope e = envelopes.get(ref);
            if (e == null) {
                missing.add(ref.format());
            } else {
                nodes.add(e);
                visible.add(ref.format());
                visible.add(ref.withoutSeq().format());
            }
        }
        List<Map<String, Object>> edgeViews = new ArrayList<>();
        for (RelationEntity r : edges.values()) {
            if (visible.contains(r.getFromRef()) && visible.contains(r.getToRef())) {
                edgeViews.add(view(r));
            }
        }
        if (types.isEmpty() || types.contains(ObjectTypes.LOCATED_IN)) {
            for (ObjectRef seed : seeds) {
                ObjectEnvelope e = envelopes.get(seed);
                if (e == null) {
                    continue;
                }
                for (Related loc : locatedIn(e, context)) {
                    if (edgeViews.size() >= limit) {
                        truncated = true;
                        break;
                    }
                    edgeViews.add(loc.relation());
                    if (visible.add(loc.object().ref())) {
                        nodes.add(loc.object());
                    }
                }
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("nodes", nodes);
        out.put("edges", edgeViews);
        out.put("missing", missing);
        out.put("truncated", truncated);
        out.put("asOf", context.asOf());
        return out;
    }

    // ------------------------------------------------------------------------- common dependencies

    /**
     * Facilities and infrastructure that the given objects share through {@code depends-on} and {@code located-in}
     * paths ({@code findCommonDependencies}). An input with a geometry that is not itself a facility enters the 104
     * graph through the facilities its geometry intersects (a computed {@code near} hop labeled {@code possible}).
     *
     * @param body the {@code CommonDependenciesRequest}
     * @param context the caller and {@code asOf}
     * @return the {@code CommonDependencies} view
     */
    public Map<String, Object> commonDependencies(JsonNode body, ResolveContext context) {
        List<ObjectRef> inputs = refs(body, "refs", 2, MAX_COMMON_REFS);
        return commonDependencies(inputs, context);
    }

    /** One hop of a dependency path. */
    record Hop(String ref, String type, String label) {}

    /** The best path found from an input to a node. */
    record Path(List<Hop> hops, String weakest) {}

    /** {@link #commonDependencies(JsonNode, ResolveContext)} with parsed references. */
    public Map<String, Object> commonDependencies(List<ObjectRef> inputs, ResolveContext context) {
        Instant at = context.asOf() == null ? clock.instant() : context.asOf();
        Map<String, Map<String, Path>> reachedBy = new LinkedHashMap<>();
        List<String> inputKeys = new ArrayList<>();
        Map<ObjectRef, ObjectEnvelope> inputEnvelopes = objects.resolveRefs(inputs, context);
        for (ObjectRef input : inputs) {
            ObjectEnvelope env = inputEnvelopes.get(input);
            if (env == null) {
                throw new NotFoundException(ErrorCodes.UNKNOWN_OBJECT,
                        "No object '" + input + "' exists, or you may not see it.");
            }
            String key = input.withoutSeq().format();
            inputKeys.add(key);
            Map<String, Path> best = walkDependencies(env, at, context);
            best.forEach((node, path) -> reachedBy.computeIfAbsent(node, n -> new LinkedHashMap<>()).put(key, path));
        }
        List<Map<String, Object>> shared = new ArrayList<>();
        Map<ObjectRef, ObjectEnvelope> nodes = objects.resolveRefs(reachedBy.keySet().stream()
                .filter(n -> reachedBy.get(n).size() >= 2 && !inputKeys.contains(n))
                .map(RelationService::parseQuietly).filter(java.util.Objects::nonNull).toList(), context);
        for (Map.Entry<String, Map<String, Path>> e : reachedBy.entrySet()) {
            if (e.getValue().size() < 2 || inputKeys.contains(e.getKey())) {
                continue;
            }
            ObjectRef ref = parseQuietly(e.getKey());
            ObjectEnvelope node = ref == null ? null : nodes.get(ref);
            if (node == null) {
                continue;
            }
            String weakest = null;
            List<Map<String, Object>> paths = new ArrayList<>();
            for (Map.Entry<String, Path> p : e.getValue().entrySet()) {
                weakest = weakest == null ? p.getValue().weakest() : ObjectTypes.weaker(weakest, p.getValue().weakest());
                Map<String, Object> path = new LinkedHashMap<>();
                path.put("from", p.getKey());
                path.put("hops", p.getValue().hops().stream().map(Hop::ref).toList());
                path.put("relationTypes", p.getValue().hops().stream().map(Hop::type).toList());
                path.put("weakestEdgeLabel", p.getValue().weakest());
                paths.add(path);
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("node", node);
            row.put("sharedBy", List.copyOf(e.getValue().keySet()));
            row.put("weakestEdgeLabel", weakest);
            row.put("paths", paths);
            shared.add(row);
        }
        shared.sort(Comparator.<Map<String, Object>>comparingInt(r -> -((List<?>) r.get("sharedBy")).size())
                .thenComparingInt(r -> ObjectTypes.labelRank((String) r.get("weakestEdgeLabel")))
                .thenComparing(r -> ((ObjectEnvelope) r.get("node")).ref()));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("refs", inputs.stream().map(ObjectRef::format).toList());
        out.put("asOf", context.asOf());
        out.put("generatedAt", clock.instant());
        out.put("dependencies", shared);
        return out;
    }

    // ------------------------------------------------------------------------- ownership chain

    /**
     * The ownership / affiliation graph around people and organizations ({@code ownershipChain}, prompt 158): the
     * sibling of {@link #commonDependencies} that walks the ownership edges ({@code owns}, {@code controls},
     * {@code officer-of}, {@code beneficial-owner}, {@code affiliated-with}) instead of {@code depends-on} /
     * {@code located-in}. Each seed's reachable nodes come back as chains with their strongest, shortest ownership
     * path; a node reached from two or more seeds is a shared attribute (a common owner, officer, address or
     * affiliate — the entities "sharing address/phone/officer").
     *
     * <p>This is the query model only; the external corporate / UBO / PEP / leak data that populates the edges is
     * deferred (docs/Future-Roadmap.md).
     *
     * @param body the {@code OwnershipChainRequest}
     * @param context the caller and {@code asOf}
     * @return the {@code OwnershipChain} view
     */
    public Map<String, Object> ownershipChain(JsonNode body, ResolveContext context) {
        List<ObjectRef> seeds = refs(body, "refs", 1, MAX_GRAPH_REFS);
        int depth = body.has("depth") ? body.get("depth").asInt(OWNERSHIP_HOPS) : OWNERSHIP_HOPS;
        if (depth < 1 || depth > OWNERSHIP_HOPS) {
            throw ValidationException.forField("depth", "must be between 1 and " + OWNERSHIP_HOPS);
        }
        String direction = direction(body);
        Set<String> types = ownershipTypes(body == null ? null : body.get("types"));
        double minConfidence = body != null && body.has("minConfidence") ? body.get("minConfidence").asDouble(0) : 0;
        return ownershipChain(seeds, depth, direction, types, minConfidence, context);
    }

    /** {@link #ownershipChain(JsonNode, ResolveContext)} with parsed parameters. */
    public Map<String, Object> ownershipChain(List<ObjectRef> seeds, int depth, String direction, Set<String> types,
            double minConfidence, ResolveContext context) {
        Instant at = context.asOf() == null ? clock.instant() : context.asOf();
        Map<ObjectRef, ObjectEnvelope> seedEnvelopes = objects.resolveRefs(seeds, context);
        Map<String, Map<String, Path>> reachedBy = new LinkedHashMap<>();
        List<String> seedKeys = new ArrayList<>();
        for (ObjectRef seed : seeds) {
            ObjectEnvelope env = seedEnvelopes.get(seed);
            if (env == null) {
                throw new NotFoundException(ErrorCodes.UNKNOWN_OBJECT,
                        "No object '" + seed + "' exists, or you may not see it.");
            }
            String key = seed.withoutSeq().format();
            seedKeys.add(key);
            walkOwnership(env, types, direction, depth, minConfidence, at, context)
                    .forEach((node, path) -> reachedBy.computeIfAbsent(node, n -> new LinkedHashMap<>()).put(key, path));
        }
        Map<ObjectRef, ObjectEnvelope> nodes = objects.resolveRefs(reachedBy.keySet().stream()
                .filter(n -> !seedKeys.contains(n)).map(RelationService::parseQuietly)
                .filter(java.util.Objects::nonNull).toList(), context);
        List<Map<String, Object>> chains = new ArrayList<>();
        List<Map<String, Object>> shared = new ArrayList<>();
        for (Map.Entry<String, Map<String, Path>> e : reachedBy.entrySet()) {
            if (seedKeys.contains(e.getKey())) {
                continue;
            }
            ObjectRef ref = parseQuietly(e.getKey());
            ObjectEnvelope node = ref == null ? null : nodes.get(ref);
            if (node == null) {
                continue;
            }
            String weakest = null;
            List<Map<String, Object>> paths = new ArrayList<>();
            for (Map.Entry<String, Path> p : e.getValue().entrySet()) {
                weakest = weakest == null ? p.getValue().weakest() : ObjectTypes.weaker(weakest, p.getValue().weakest());
                Map<String, Object> path = new LinkedHashMap<>();
                path.put("from", p.getKey());
                path.put("hops", p.getValue().hops().stream().map(Hop::ref).toList());
                path.put("relationTypes", p.getValue().hops().stream().map(Hop::type).toList());
                path.put("weakestEdgeLabel", p.getValue().weakest());
                paths.add(path);
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("node", node);
            row.put("reachedFrom", List.copyOf(e.getValue().keySet()));
            row.put("weakestEdgeLabel", weakest);
            row.put("paths", paths);
            chains.add(row);
            if (e.getValue().size() >= 2) {
                shared.add(row);
            }
        }
        Comparator<Map<String, Object>> order = Comparator
                .<Map<String, Object>>comparingInt(r -> -((List<?>) r.get("reachedFrom")).size())
                .thenComparingInt(r -> ObjectTypes.labelRank((String) r.get("weakestEdgeLabel")))
                .thenComparing(r -> ((ObjectEnvelope) r.get("node")).ref());
        chains.sort(order);
        shared.sort(order);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("refs", seeds.stream().map(ObjectRef::format).toList());
        out.put("direction", direction);
        out.put("asOf", context.asOf());
        out.put("generatedAt", clock.instant());
        out.put("chains", chains);
        out.put("shared", shared);
        return out;
    }

    /**
     * The strongest, shortest ownership / affiliation path from {@code start} to every entity it reaches, walking
     * only {@code edgeTypes} in {@code direction} for up to {@code maxHops}. Unlike {@link #walkDependencies} there
     * is no geographic ({@code located-in} / {@code near}) seeding — ownership is not spatial.
     */
    Map<String, Path> walkOwnership(ObjectEnvelope start, Set<String> edgeTypes, String direction, int maxHops,
            double minConfidence, Instant at, ResolveContext context) {
        Map<String, Path> best = new HashMap<>();
        String origin = start.objectRef().withoutSeq().format();
        Map<String, Path> frontier = new LinkedHashMap<>();
        frontier.put(origin, new Path(List.of(), "confirmed"));
        for (int hop = 0; hop < maxHops && !frontier.isEmpty(); hop++) {
            List<RelationEntity> rows = query.touching(frontier.keySet(), edgeTypes, direction, at,
                    context.asOf() != null, minConfidence, context.seesPrivate(), MAX_GRAPH_EDGES);
            Map<String, Path> next = new LinkedHashMap<>();
            for (RelationEntity r : rows) {
                String near;
                String far;
                if (frontier.containsKey(r.getFromRef())) {
                    near = r.getFromRef();
                    far = r.getToRef();
                } else if (frontier.containsKey(r.getToRef())) {
                    near = r.getToRef();
                    far = r.getFromRef();
                } else {
                    continue;
                }
                if (far.equals(origin)) {
                    continue;
                }
                Path from = frontier.get(near);
                List<Hop> hops = new ArrayList<>(from.hops());
                hops.add(new Hop(far, r.getType(), r.getLabel()));
                Path candidate = new Path(hops, from.hops().isEmpty() ? r.getLabel()
                        : ObjectTypes.weaker(from.weakest(), r.getLabel()));
                Path existing = best.get(far);
                if (existing == null || better(candidate, existing)) {
                    best.put(far, candidate);
                    next.put(far, candidate);
                }
            }
            frontier = next;
        }
        return best;
    }

    /** The ownership / affiliation edge types of a request field, defaulting to all of them. */
    static Set<String> ownershipTypes(JsonNode node) {
        if (node == null || node.isNull()) {
            return OWNERSHIP_EDGES;
        }
        Set<String> out = relationTypes(node, "types");
        for (String type : out) {
            if (!OWNERSHIP_EDGES.contains(type)) {
                throw ValidationException.forField("types", "'" + type + "' is not an ownership or affiliation edge");
            }
        }
        return out.isEmpty() ? OWNERSHIP_EDGES : out;
    }

    /** The walk direction of a request ({@code in}, {@code out} or {@code both}; default {@code both}). */
    private static String direction(JsonNode body) {
        String value = body == null ? null : text(body, "direction");
        if (value == null || value.isBlank()) {
            return "both";
        }
        if (!value.equals("in") && !value.equals("out") && !value.equals("both")) {
            throw ValidationException.forField("direction", "must be 'in', 'out' or 'both'");
        }
        return value;
    }

    /** The strongest, shortest path from {@code start} to every dependency it reaches. */
    Map<String, Path> walkDependencies(ObjectEnvelope start, Instant at, ResolveContext context) {
        Map<String, Path> best = new HashMap<>();
        String origin = start.objectRef().withoutSeq().format();
        Map<String, Path> frontier = new LinkedHashMap<>();
        frontier.put(origin, new Path(List.of(), "confirmed"));
        for (Related loc : locatedIn(start, context)) {
            Path p = new Path(List.of(new Hop(loc.object().ref(), ObjectTypes.LOCATED_IN, "confirmed")), "confirmed");
            best.put(loc.object().ref(), p);
        }
        for (Map.Entry<String, String> facility : facilitiesNear(start).entrySet()) {
            if (!facility.getKey().equals(origin)) {
                Path p = new Path(List.of(new Hop(facility.getKey(), ObjectTypes.NEAR, facility.getValue())),
                        facility.getValue());
                best.putIfAbsent(facility.getKey(), p);
                frontier.putIfAbsent(facility.getKey(), p);
            }
        }
        Set<String> types = Set.of(ObjectTypes.DEPENDS_ON, ObjectTypes.LOCATED_IN);
        for (int hop = 0; hop < COMMON_HOPS && !frontier.isEmpty(); hop++) {
            List<RelationEntity> rows = query.touching(frontier.keySet(), types, "out", at, context.asOf() != null, 0,
                    context.seesPrivate(), MAX_GRAPH_EDGES);
            Map<String, Path> next = new LinkedHashMap<>();
            for (RelationEntity r : rows) {
                Path from = frontier.get(r.getFromRef());
                if (from == null) {
                    continue;
                }
                List<Hop> hops = new ArrayList<>(from.hops());
                hops.add(new Hop(r.getToRef(), r.getType(), r.getLabel()));
                Path candidate = new Path(hops, from.hops().isEmpty() ? r.getLabel()
                        : ObjectTypes.weaker(from.weakest(), r.getLabel()));
                Path existing = best.get(r.getToRef());
                if (r.getToRef().equals(origin)) {
                    continue;
                }
                if (existing == null || better(candidate, existing)) {
                    best.put(r.getToRef(), candidate);
                    next.put(r.getToRef(), candidate);
                }
            }
            frontier = next;
        }
        return best;
    }

    /** Facilities (104 infra nodes, as record refs) intersecting the object's geometry, with the hop label. */
    Map<String, String> facilitiesNear(ObjectEnvelope start) {
        InfraImpactService service = infra.getIfAvailable();
        if (service == null || start.geometry() == null) {
            return Map.of();
        }
        Geometry geometry = geometry(start);
        if (geometry == null) {
            return Map.of();
        }
        Map<String, String> out = new LinkedHashMap<>();
        try {
            Map<String, Object> affected = service.affected(geometry, 1, "speculative", List.of());
            if (affected.get("direct") instanceof List<?> direct) {
                for (Object n : direct) {
                    if (n instanceof Map<?, ?> node && node.get("sourceId") != null && node.get("externalId") != null) {
                        out.put(ObjectRef.record(node.get("sourceId").toString(), node.get("externalId").toString(),
                                null).format(), "possible");
                    }
                }
            }
        } catch (RuntimeException e) {
            log.debug("No infrastructure seed for {}: {}", start.ref(), e.toString());
        }
        return out;
    }

    private static boolean better(Path candidate, Path existing) {
        int c = ObjectTypes.labelRank(candidate.weakest());
        int e = ObjectTypes.labelRank(existing.weakest());
        return c < e || (c == e && candidate.hops().size() < existing.hops().size());
    }

    // ------------------------------------------------------------------------- helpers

    /** A computed (unstored) relation view with a deterministic id. */
    public static Map<String, Object> computed(String from, String to, String type, String label, double confidence,
            Instant validFrom) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", deterministicId(from, to, type));
        out.put("fromRef", from);
        out.put("toRef", to);
        out.put("type", type);
        out.put("validFrom", validFrom == null ? Instant.EPOCH : validFrom);
        out.put("validTo", null);
        out.put("confidence", confidence);
        out.put("label", label);
        Map<String, Object> provenance = new LinkedHashMap<>();
        provenance.put("component", RelationProjector.COMPONENT);
        provenance.put("version", RelationProjector.VERSION);
        provenance.put("manifestId", null);
        provenance.put("operator", null);
        out.put("provenance", provenance);
        out.put("projected", true);
        out.put("private", false);
        out.put("createdAt", validFrom == null ? Instant.EPOCH : validFrom);
        out.put("retiredAt", null);
        return out;
    }

    /** A 26-character Crockford id derived from the parts (stable across calls). */
    public static String deterministicId(String... parts) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            for (String part : parts) {
                md.update(String.valueOf(part).getBytes(StandardCharsets.UTF_8));
                md.update((byte) 0);
            }
            byte[] hash = md.digest();
            StringBuilder out = new StringBuilder(Ulids.LENGTH);
            for (int i = 0; i < Ulids.LENGTH; i++) {
                out.append(Ulids.CROCKFORD.charAt((hash[i] & 0xff) % 32));
            }
            return out.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** The stored keys of a reference: itself without a pinned version. */
    static Set<String> keys(ObjectRef ref) {
        Set<String> out = new LinkedHashSet<>();
        out.add(ref.withoutSeq().format());
        return out;
    }

    /** The relation types of a request field (empty for all). */
    public static Set<String> relationTypes(JsonNode node, String field) {
        Set<String> out = new LinkedHashSet<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (!node.isArray()) {
            throw ValidationException.forField(field, "must be an array of relation types");
        }
        for (JsonNode t : node) {
            String type = t.asString();
            if (!ObjectTypes.isRelationType(type)) {
                throw ValidationException.forField(field, "has unknown relation type '" + type + "'");
            }
            out.add(type);
        }
        return out;
    }

    /** The relation types of a query parameter list (empty for all). */
    public static Set<String> relationTypes(Collection<String> values) {
        Set<String> out = new LinkedHashSet<>();
        for (String value : values) {
            if (!ObjectTypes.isRelationType(value)) {
                throw ValidationException.forField("types", "has unknown relation type '" + value + "'");
            }
            out.add(value);
        }
        return out;
    }

    private List<ObjectRef> refs(JsonNode body, String field, int min, int max) {
        if (body == null || !body.isObject() || !body.has(field) || !body.get(field).isArray()) {
            throw ObjectRef.invalid(field, "must be an array of references");
        }
        JsonNode array = body.get(field);
        if (array.size() < min || array.size() > max) {
            throw ObjectRef.invalid(field, "must hold " + min + " to " + max + " references");
        }
        List<ObjectRef> out = new ArrayList<>();
        int i = 0;
        for (JsonNode n : array) {
            ObjectRef ref = ObjectRef.parse(n.asString(), field + "[" + i++ + "]");
            if (!out.contains(ref)) {
                out.add(ref);
            }
        }
        return out;
    }

    private Geometry geometry(ObjectEnvelope e) {
        try {
            return reader.read(json.valueToTree(e.geometry()));
        } catch (RuntimeException ex) {
            return null;
        }
    }

    /** The distance between two envelopes' bbox centers, in km, or null when either has none. */
    static Double distanceKm(ObjectEnvelope a, ObjectEnvelope b) {
        if (a.bbox() == null || b.bbox() == null) {
            return null;
        }
        double lonA = (a.bbox().get(0) + a.bbox().get(2)) / 2;
        double latA = (a.bbox().get(1) + a.bbox().get(3)) / 2;
        double lonB = (b.bbox().get(0) + b.bbox().get(2)) / 2;
        double latB = (b.bbox().get(1) + b.bbox().get(3)) / 2;
        return Math.round(Geo.haversineKm(latA, lonA, latB, lonB) * 100) / 100.0;
    }

    static ObjectRef parseQuietly(String text) {
        try {
            return ObjectRef.parse(text);
        } catch (ValidationException e) {
            return null;
        }
    }

    private static String text(JsonNode body, String field) {
        JsonNode n = body.get(field);
        return n == null || n.isNull() ? null : n.asString();
    }

    private static Instant instant(JsonNode body, String field, Instant fallback) {
        String value = text(body, field);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Instant.parse(value);
        } catch (RuntimeException e) {
            throw ValidationException.forField(field, "must be an RFC 3339 date-time");
        }
    }
}
