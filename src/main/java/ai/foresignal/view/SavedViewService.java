package ai.foresignal.view;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.FieldSelector;
import ai.foresignal.place.PlaceService;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.store.TriggerCondition;
import ai.foresignal.trigger.GeofenceMaterializer;
import ai.foresignal.trigger.IdentityOperator;
import ai.foresignal.trigger.JsonTrees;
import ai.foresignal.trigger.Operator;
import ai.foresignal.trigger.OperatorRegistry;
import ai.foresignal.trigger.RecordPredicate;
import ai.foresignal.trigger.RuleCondition;
import ai.foresignal.trigger.RuleValidator;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * Saved views (prompt 036, task 3): validation, CRUD and compilation into the {@link RecordPredicate}
 * shared with trigger rules.
 *
 * <p>A view's predicate is its sources and kinds, its conditions (the 017 grammar with
 * {@code in/notIn/exists/matches/startsWith} and {@code inWatchlist}, at most
 * {@value #MAX_CONDITIONS}), and an area: the intersection of its places' areas, its bbox and its own
 * area. Every save bumps {@code version}; tile URLs and cache keys carry it. A view that references a
 * place or a watchlist needs a token to render (its definition stays a normal {@code read}).
 *
 * <p>Prompt R2-012: a view has an owner ({@link SavedViewScope}). The listing a request sees is filtered by its scope
 * and a session's new views are its own; resolving a view by id or slug ({@link #get}) stays unscoped, because
 * briefings, chronicles, share links and workspaces name views by id, and the REST operations on one view are guarded
 * in front of the controller ({@code api/support/SavedViewOwnershipInterceptor}).
 */
@Service
public class SavedViewService {

    private static final Logger log = LoggerFactory.getLogger(SavedViewService.class);

    /** The slug grammar. */
    public static final Pattern SLUG = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    /** The most conditions of a view. */
    public static final int MAX_CONDITIONS = 20;

    /** The most projected fields of a view. */
    public static final int MAX_FIELDS = 50;

    static final Set<String> MEMBERS = Set.of("slug", "name", "sources", "kinds", "placeIds", "bbox", "area",
            "conditions", "timeField", "includeExpired", "fields");

    static final Set<String> OUTPUT_ONLY = Set.of("id", "version", "createdAt", "updatedAt", "ownerPrincipalId",
            "shared");

    static final List<String> TIME_FIELDS = List.of("effective", "observed", "event", "ingested");

    private static final GeometryFactory WGS84 = new GeometryFactory(new PrecisionModel(), 4326);

    private final SavedViewRepository views;
    private final SourceRegistry registry;
    private final OperatorRegistry operators;
    private final PlaceService places;
    private final GeofenceMaterializer geofences;
    private final Clock clock;

    /** Creates the service. */
    public SavedViewService(SavedViewRepository views, SourceRegistry registry, OperatorRegistry operators,
            PlaceService places, GeofenceMaterializer geofences, Clock clock) {
        this.views = views;
        this.registry = registry;
        this.operators = operators;
        this.places = places;
        this.geofences = geofences;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- reads

    /**
     * A page of views.
     *
     * @param items the views
     * @param nextCursor the next page's cursor, or null
     */
    public record Page(List<SavedViewEntity> items, String nextCursor) {}

    /** Every view, by slug. */
    public List<SavedViewEntity> all() {
        List<SavedViewEntity> out = new ArrayList<>(views.findAll());
        out.sort(Comparator.comparing(SavedViewEntity::getSlug));
        return out;
    }

    /** Every shared view (no owner), by slug: what a settings export carries (prompt R2-012). */
    public List<SavedViewEntity> allShared() {
        return all().stream().filter(v -> v.getOwnerPrincipalId() == null).toList();
    }

    /** A page of every view, keyset by slug. */
    public Page list(String cursor, int limit) {
        return list(cursor, limit, SavedViewScope.MACHINE);
    }

    /** A page of the views {@code scope} sees, keyset by slug (prompt R2-012). */
    public Page list(String cursor, int limit, SavedViewScope scope) {
        String after = decode(cursor);
        List<SavedViewEntity> matching = all().stream().filter(v -> scope.sees(v.getOwnerPrincipalId()))
                .filter(v -> after == null || v.getSlug().compareTo(after) > 0)
                .toList();
        List<SavedViewEntity> page = matching.size() > limit ? matching.subList(0, limit) : matching;
        return new Page(page, matching.size() > limit ? encode(page.getLast().getSlug()) : null);
    }

    /** The view with id or slug {@code idOrSlug}, if any. */
    public Optional<SavedViewEntity> find(String idOrSlug) {
        if (idOrSlug == null || idOrSlug.isBlank()) {
            return Optional.empty();
        }
        try {
            Optional<SavedViewEntity> byId = views.findById(UUID.fromString(idOrSlug));
            if (byId.isPresent()) {
                return byId;
            }
        } catch (IllegalArgumentException e) {
            // not a UUID: a slug
        }
        return views.findBySlug(idOrSlug);
    }

    /**
     * The view.
     *
     * @throws ForesignalException {@code 404 UNKNOWN_VIEW}
     */
    public SavedViewEntity get(String idOrSlug) {
        return find(idOrSlug).orElseThrow(() -> unknown(idOrSlug));
    }

    // ------------------------------------------------------------------------- writes

    /**
     * Creates a view.
     *
     * @throws ValidationException when the input is not valid
     * @throws ConflictException {@code 409 VIEW_EXISTS} when the slug is taken
     */
    public SavedViewEntity create(JsonNode body) {
        return create(UUID.randomUUID(), body);
    }

    /**
     * Creates a view owned by {@code owner} (prompt R2-012).
     *
     * @param body the {@code SavedViewInput}
     * @param owner the owning user's principal, or null for a shared view
     * @return the saved view
     */
    public SavedViewEntity create(JsonNode body, String owner) {
        return create(UUID.randomUUID(), body, owner);
    }

    /** Creates a shared view with {@code id} (settings import). */
    public SavedViewEntity create(UUID id, JsonNode body) {
        return create(id, body, null);
    }

    private SavedViewEntity create(UUID id, JsonNode body, String owner) {
        Input input = parse(body);
        if (views.findBySlug(input.slug()).isPresent()) {
            throw new ConflictException(ErrorCodes.VIEW_EXISTS, "A view with slug '" + input.slug() + "' already exists.");
        }
        Instant now = clock.instant();
        SavedViewEntity view = new SavedViewEntity(id);
        input.applyTo(view);
        view.setOwnerPrincipalId(owner);
        view.setVersion(1);
        view.setCreatedAt(now);
        view.setUpdatedAt(now);
        return views.saveAndFlush(view);
    }

    /**
     * Replaces a view whole; bumps its version.
     *
     * @throws ForesignalException {@code 404 UNKNOWN_VIEW}
     */
    public SavedViewEntity update(String idOrSlug, JsonNode body) {
        SavedViewEntity view = get(idOrSlug);
        Input input = parse(body);
        Optional<SavedViewEntity> other = views.findBySlug(input.slug());
        if (other.isPresent() && !other.get().getId().equals(view.getId())) {
            throw new ConflictException(ErrorCodes.VIEW_EXISTS, "A view with slug '" + input.slug() + "' already exists.");
        }
        input.applyTo(view);
        view.setVersion(view.getVersion() + 1);
        view.setUpdatedAt(clock.instant());
        return views.saveAndFlush(view);
    }

    /** Creates or replaces the view {@code id} (settings import). */
    public SavedViewEntity upsert(UUID id, JsonNode body) {
        return views.existsById(id) ? update(id.toString(), body) : create(id, body);
    }

    /**
     * Deletes a view.
     *
     * @throws ForesignalException {@code 404 UNKNOWN_VIEW}
     */
    public void delete(String idOrSlug) {
        SavedViewEntity view = get(idOrSlug);
        views.delete(view);
        views.flush();
    }

    /**
     * Moves every view {@code from} owns to {@code to} (user deletion, prompt R2-012).
     *
     * @param from the deleted user's principal
     * @param to the receiving admin's principal, or null to make the views shared
     * @return how many views moved
     */
    public int reassignOwner(String from, String to) {
        List<SavedViewEntity> owned = views.findByOwnerPrincipalIdOrderBySlugAsc(from);
        Instant now = clock.instant();
        for (SavedViewEntity view : owned) {
            view.setOwnerPrincipalId(to);
            view.setUpdatedAt(now);
        }
        views.saveAllAndFlush(owned);
        return owned.size();
    }

    /**
     * Deletes every view {@code owner} owns (user deletion, prompt R2-012).
     *
     * @param owner the deleted user's principal
     * @return how many views were deleted
     */
    public int deleteOwned(String owner) {
        List<SavedViewEntity> owned = views.findByOwnerPrincipalIdOrderBySlugAsc(owner);
        views.deleteAll(owned);
        views.flush();
        return owned.size();
    }

    // ------------------------------------------------------------------------- compiling

    /**
     * A view compiled for rendering.
     *
     * @param view the stored view
     * @param predicate the shared predicate
     * @param requiresToken true when it references a place or a watchlist
     * @param placeNames the names of its places (for briefings and Ask; never coordinates)
     */
    public record CompiledView(SavedViewEntity view, RecordPredicate predicate, boolean requiresToken,
            List<String> placeNames) {

        /** A fresh request filter (each has its own live-lane membership memory). */
        public PredicateFilter filter() {
            return new PredicateFilter(predicate, "view:" + view.getId() + ":v" + view.getVersion());
        }

        /** The {@code fields} projection as the {@code fields=} parameter, or null for every field. */
        public String fieldsParam() {
            return view.getFields().isEmpty() ? null : String.join(",", view.getFields());
        }
    }

    /**
     * Compiles a view: identity conditions resolve per source, places to their union area.
     *
     * @throws ForesignalException {@code 404 UNKNOWN_PLACE} is not thrown: a deleted place leaves an
     *     empty area, so the view matches nothing (fail closed)
     */
    public CompiledView compile(SavedViewEntity view) {
        List<RuleCondition> conditions = new ArrayList<>();
        boolean identity = false;
        for (TriggerCondition condition : view.getConditions()) {
            Operator op = operators.find(condition.op()).orElseThrow(() -> new IllegalStateException(
                    "Unknown operator '" + condition.op() + "' in view " + view.getSlug()));
            Object value = op.normalize(condition.value());
            identity |= op instanceof IdentityOperator;
            if (condition.path() == null && op instanceof IdentityOperator io) {
                conditions.add(new RuleCondition(null, op, value, io.identityPaths(value, view.getSources())));
            } else {
                conditions.add(new RuleCondition(condition.path(), op, value));
            }
        }
        Geometry area = null;
        List<String> names = new ArrayList<>();
        if (!view.getPlaceIds().isEmpty()) {
            List<Geometry> parts = new ArrayList<>();
            boolean missing = false;
            for (String id : view.getPlaceIds()) {
                Optional<ai.foresignal.place.PlaceView> place = places.find(id);
                if (place.isEmpty()) {
                    missing = true;
                    log.warn("View {} names the deleted place {}; it matches nothing there", view.getSlug(), id);
                } else {
                    parts.add(place.get().area());
                    names.add(place.get().name());
                }
            }
            area = missing || parts.isEmpty() ? WGS84.createPolygon()
                    : parts.size() == 1 ? parts.getFirst() : WGS84.buildGeometry(parts).union();
        }
        if (view.getBbox() != null) {
            area = intersect(area, ReadRestriction.rectangle(view.getBbox()));
        }
        if (view.getArea() != null) {
            area = intersect(area, view.getArea());
        }
        if (area != null) {
            area.setSRID(4326);
        }
        RecordPredicate predicate = new RecordPredicate(new LinkedHashSet<>(view.getSources()),
                new LinkedHashSet<>(view.getKinds()), conditions, area);
        return new CompiledView(view, predicate, !view.getPlaceIds().isEmpty() || identity, List.copyOf(names));
    }

    private static Geometry intersect(Geometry a, Geometry b) {
        if (a == null) {
            return b;
        }
        if (a.isEmpty() || b.isEmpty()) {
            return WGS84.createPolygon();
        }
        return a.intersection(b);
    }

    // ------------------------------------------------------------------------- input

    /**
     * A validated {@code SavedViewInput}.
     *
     * @param slug the slug
     * @param name the name
     * @param sources the sources
     * @param kinds the kinds
     * @param placeIds the place ids
     * @param bbox the bbox, or null
     * @param area the fixed area, or null
     * @param conditions the normalized conditions
     * @param timeField the time axis
     * @param includeExpired whether expired records are included
     * @param fields the projection
     */
    public record Input(String slug, String name, List<String> sources, List<String> kinds, List<String> placeIds,
            List<Double> bbox, MultiPolygon area, List<TriggerCondition> conditions, String timeField,
            boolean includeExpired, List<String> fields) {

        void applyTo(SavedViewEntity view) {
            view.setSlug(slug);
            view.setName(name);
            view.setSources(sources);
            view.setKinds(kinds);
            view.setPlaceIds(placeIds);
            view.setBbox(bbox);
            view.setArea(area);
            view.setConditions(conditions);
            view.setTimeField(timeField);
            view.setIncludeExpired(includeExpired);
            view.setFields(fields);
        }
    }

    /**
     * Validates a {@code SavedViewInput} without saving it (Ask proposals).
     *
     * @throws ValidationException listing every problem
     */
    public Input parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a SavedViewInput object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String member : body.propertyNames()) {
            if (!MEMBERS.contains(member) && !OUTPUT_ONLY.contains(member)) {
                errors.add(new FieldError(member, "is not a SavedViewInput member"));
            }
        }
        String slug = body.path("slug").isString() ? body.get("slug").asString() : null;
        if (slug == null || slug.length() > 64 || !SLUG.matcher(slug).matches()) {
            errors.add(new FieldError("slug", "must match " + SLUG.pattern() + " and be at most 64 characters"));
        }
        String name = body.path("name").isString() ? body.get("name").asString().strip() : null;
        if (name == null || name.isEmpty()) {
            errors.add(new FieldError("name", "is required"));
        } else if (name.length() > 200) {
            errors.add(new FieldError("name", "must be at most 200 characters"));
        }
        List<String> sources = strings(body, "sources", 50, errors);
        for (String id : sources) {
            if (!registry.contains(id)) {
                errors.add(new FieldError("sources", "unknown source '" + id + "'"));
            }
        }
        List<String> kinds = strings(body, "kinds", 50, errors);
        List<String> placeIds = strings(body, "placeIds", 50, errors);
        for (String id : placeIds) {
            if (places.find(id).isEmpty()) {
                errors.add(new FieldError("placeIds", "unknown place '" + id + "'"));
            }
        }
        List<Double> bbox = bbox(body.get("bbox"), errors);
        MultiPolygon area = area(body.get("area"), errors);
        List<TriggerCondition> conditions = conditions(body.get("conditions"), sources, errors);
        String timeField = body.path("timeField").isString() ? body.get("timeField").asString() : "effective";
        if (!TIME_FIELDS.contains(timeField)) {
            errors.add(new FieldError("timeField", "must be effective, observed, event or ingested"));
        }
        boolean includeExpired = false;
        JsonNode expired = body.get("includeExpired");
        if (expired != null && !expired.isNull()) {
            if (!expired.isBoolean()) {
                errors.add(new FieldError("includeExpired", "must be true or false"));
            } else {
                includeExpired = expired.asBoolean();
            }
        }
        List<String> fields = strings(body, "fields", MAX_FIELDS, errors);
        if (!fields.isEmpty()) {
            try {
                FieldSelector.parse(String.join(",", fields));
            } catch (ValidationException e) {
                errors.add(new FieldError("fields", e.getErrors().isEmpty() ? e.getMessage()
                        : e.getErrors().getFirst().message()));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The view is not valid.", errors);
        }
        return new Input(slug, name, sources, kinds, placeIds, bbox, area, conditions, timeField, includeExpired, fields);
    }

    private List<TriggerCondition> conditions(JsonNode node, List<String> sources, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return List.of();
        }
        if (!node.isArray()) {
            errors.add(new FieldError("conditions", "must be an array"));
            return List.of();
        }
        if (node.size() > MAX_CONDITIONS) {
            errors.add(new FieldError("conditions", "must have at most " + MAX_CONDITIONS + " conditions"));
            return List.of();
        }
        List<TriggerCondition> out = new ArrayList<>();
        for (int i = 0; i < node.size(); i++) {
            JsonNode item = node.get(i);
            String prefix = "conditions[" + i + "]";
            if (!item.isObject()) {
                errors.add(new FieldError(prefix, "must be an object with path, op and value"));
                continue;
            }
            JsonNode op = item.get("op");
            Optional<Operator> operator = op != null && op.isString() ? operators.find(op.asString()) : Optional.empty();
            if (operator.isEmpty()) {
                errors.add(new FieldError(prefix + ".op", "must be one of " + String.join(", ", operators.names())));
                continue;
            }
            boolean identity = operator.get() instanceof IdentityOperator;
            JsonNode path = item.get("path");
            boolean noPath = path == null || path.isNull();
            if (!(noPath && identity) && (path == null || !path.isString()
                    || !RuleValidator.PATH.matcher(path.asString()).matches())) {
                errors.add(new FieldError(prefix + ".path", "must match " + RuleValidator.PATH.pattern()));
                continue;
            }
            if (!item.has("value")) {
                errors.add(new FieldError(prefix + ".value", "is required"));
                continue;
            }
            try {
                Object value = operator.get().normalize(JsonTrees.toJava(item.get("value")));
                if (identity) {
                    IdentityOperator io = (IdentityOperator) operator.get();
                    if (!io.known(value)) {
                        errors.add(new FieldError(prefix + ".value", "names no watchlist"));
                        continue;
                    }
                    if (noPath && io.requiresPath(value)) {
                        errors.add(new FieldError(prefix + ".path", "is required: the watchlist's identity is custom"));
                        continue;
                    }
                    if (noPath) {
                        List<String> without = io.sourcesWithoutIdentity(value, sources);
                        if (!without.isEmpty()) {
                            log.warn("View condition {}: sources without the watchlist's identity never match: {}",
                                    prefix, without);
                        }
                    }
                }
                out.add(new TriggerCondition(noPath && identity ? null : path.asString(), operator.get().name(), value));
            } catch (IllegalArgumentException e) {
                errors.add(new FieldError(prefix + ".value", e.getMessage()));
            }
        }
        return out;
    }

    private static List<Double> bbox(JsonNode node, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isArray() || node.size() != 4) {
            errors.add(new FieldError("bbox", "must be [minLon, minLat, maxLon, maxLat]"));
            return null;
        }
        List<Double> values = new ArrayList<>();
        for (JsonNode item : node) {
            if (!item.isNumber() || !Double.isFinite(item.asDouble())) {
                errors.add(new FieldError("bbox", "must contain four finite numbers"));
                return null;
            }
            values.add(item.asDouble());
        }
        if (Math.abs(values.get(0)) > 180 || Math.abs(values.get(2)) > 180 || Math.abs(values.get(1)) > 90
                || Math.abs(values.get(3)) > 90 || values.get(0) >= values.get(2) || values.get(1) >= values.get(3)) {
            errors.add(new FieldError("bbox", "must lie within ±180/±90 with min below max"));
            return null;
        }
        return values;
    }

    private MultiPolygon area(JsonNode node, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            Geometry g = new GeoJsonReader(ReadRestriction.MAX_AREA_VERTICES * 5, 2).read(node);
            if (!(g instanceof Polygon) && !(g instanceof MultiPolygon)) {
                errors.add(new FieldError("area", "must be a GeoJSON Polygon or MultiPolygon"));
                return null;
            }
            MultiPolygon area = geofences.polygonal(g);
            if (area.isEmpty()) {
                errors.add(new FieldError("area", "is empty after fixing"));
                return null;
            }
            return area;
        } catch (InvalidInputException e) {
            errors.add(new FieldError("area", e.getMessage()));
            return null;
        }
    }

    private static List<String> strings(JsonNode body, String field, int max, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return List.of();
        }
        if (!node.isArray()) {
            errors.add(new FieldError(field, "must be an array of strings"));
            return List.of();
        }
        if (node.size() > max) {
            errors.add(new FieldError(field, "must have at most " + max + " items"));
            return List.of();
        }
        Set<String> out = new LinkedHashSet<>();
        for (JsonNode item : node) {
            if (!item.isString() || item.asString().isBlank()) {
                errors.add(new FieldError(field, "must contain non-blank strings"));
                return List.of();
            }
            out.add(item.asString().strip());
        }
        return List.copyOf(out);
    }

    /**
     * The {@code 404 UNKNOWN_VIEW} for {@code id}; also what another user's view looks like (prompt R2-012).
     *
     * @param id the id or slug asked for
     * @return the exception
     */
    public static ForesignalException unknown(String id) {
        return new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_VIEW, "No saved view has the id or slug '"
                + id + "'.");
    }

    private static String encode(String slug) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(("v:" + slug).getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String text = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            if (!text.startsWith("v:")) {
                throw new IllegalArgumentException();
            }
            return text.substring(2);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(ErrorCodes.INVALID_CURSOR, "The cursor is not valid.",
                    List.of(new FieldError("cursor", "is not a cursor of this listing")));
        }
    }
}
