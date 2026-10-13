package ai.foresignal.place;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.security.restrict.PlaceAreas;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.trigger.PlaceLookup;
import ai.foresignal.trigger.RuleValidator;
import ai.foresignal.trigger.TriggerRulesChangedEvent;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import ai.foresignal.source.spi.PlacePoints;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;

/**
 * Named places (prompt 036, task 1): validation, CRUD, the cache every consumer reads, and the
 * {@link PlaceLookup} trigger rules materialize {@code near.placeIds} from.
 *
 * <p><b>Saving.</b> Every save derives {@code area} ({@link PlaceGeometry}), bumps {@code version},
 * and, in the same transaction, re-materializes every rule whose {@code near} names the place (their
 * fence, hysteresis band and recorded place versions). After commit it publishes
 * {@link PlaceChangedEvent} (the AOI recomputes) and {@link TriggerRulesChangedEvent} (the gate and
 * the engine reload). Because the persist gate compares persisted state under the current fence,
 * editing a place never fires edges for stationary entities; an entity that moves after the edit
 * converges on its next observation.
 *
 * <p><b>Deleting.</b> A place referenced by rules, saved views or briefings is refused with
 * {@code 409 PLACE_IN_USE} listing them, unless {@code force=true}, which disables those rules
 * ({@code lastError} says why). Views and briefings keep the id and match nothing in that place
 * (fail closed). Tombstoned place-weather records (prompt 052) can still fire exit rules.
 *
 * <p>The service also installs {@link PlaceAreas}, so a token's {@code placeIds} restriction resolves
 * to the union of the places' areas, and implements {@link PlacePoints}, so an adapter parameter can
 * name a place (prompt 049: a SatDump station's {@code location}).
 */
@Service
public class PlaceService implements PlaceLookup, PlacePoints, IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(PlaceService.class);

    /** The place id grammar. */
    public static final Pattern ID = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    /** Ids that would collide with literal sub-paths of {@code /v1/places}. */
    static final Set<String> RESERVED_IDS = Set.of("aoi");

    /** The members of {@code PlaceInput}. */
    static final Set<String> MEMBERS = Set.of("id", "name", "shape", "geometry", "radiusKm", "bufferM", "timezone",
            "kind", "tags", "products", "sensitive", "attributes");

    /** Output-only {@code Place} members, accepted and ignored on a replace. */
    static final Set<String> OUTPUT_ONLY = Set.of("area", "version", "createdAt", "updatedAt", "external");

    static final Set<String> SHAPES = Set.of("point", "polygon", "route");

    static final Set<String> KINDS = Set.of("user", "airport", "asset");

    /** The default radius of a point place. */
    public static final double DEFAULT_RADIUS_KM = 20;

    static final Pattern ZIP = Pattern.compile("^\\d{5}$");

    private final PlaceRepository places;
    private final TriggerRuleRepository rules;
    private final PlaceGeometry geometry;
    private final RuleValidator validator;
    private final AppSettingService settings;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final ApplicationEventPublisher events;
    private final Clock clock;
    private final Map<String, PlaceView> cache = new ConcurrentHashMap<>();
    private volatile boolean loaded;

    /** Creates the service. */
    public PlaceService(PlaceRepository places, TriggerRuleRepository rules, PlaceGeometry geometry,
            RuleValidator validator, AppSettingService settings, JdbcTemplate jdbc, TransactionTemplate transactions,
            ApplicationEventPublisher events, Clock clock) {
        this.places = places;
        this.rules = rules;
        this.geometry = geometry;
        this.validator = validator;
        this.settings = settings;
        this.jdbc = jdbc;
        this.transactions = transactions;
        this.events = events;
        this.clock = clock;
    }

    /** Loads the cache and installs the restriction resolver once the schema exists. */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        reload();
        PlaceAreas.install(this::areaOrNull, id -> find(id).isPresent());
    }

    /** Removes the restriction resolver. */
    @PreDestroy
    public void stop() {
        PlaceAreas.uninstall();
    }

    /** Reloads the cache from the database (the test cleaner truncated it). */
    @Override
    public void resetState() {
        reload();
    }

    private synchronized void reload() {
        Map<String, PlaceView> fresh = new LinkedHashMap<>();
        for (PlaceEntity entity : places.findAll()) {
            fresh.put(entity.getId(), PlaceView.of(entity));
        }
        cache.clear();
        cache.putAll(fresh);
        loaded = true;
    }

    private Map<String, PlaceView> cache() {
        if (!loaded) {
            reload();
        }
        return cache;
    }

    // ------------------------------------------------------------------------- reads

    /** Every place, sorted by id. */
    public List<PlaceView> all() {
        return cache().values().stream().sorted(Comparator.comparing(PlaceView::id)).toList();
    }

    /**
     * A page of places.
     *
     * @param kind only this kind, or null
     * @param tag only places with this tag, or null
     * @param cursor the previous page's {@code nextCursor}, or null
     * @param limit the page size
     */
    public Page list(String kind, String tag, String cursor, int limit) {
        String after = decodeCursor(cursor);
        List<PlaceView> matching = all().stream()
                .filter(p -> kind == null || kind.equals(p.kind()))
                .filter(p -> tag == null || p.tags().contains(tag))
                .filter(p -> after == null || p.id().compareTo(after) > 0)
                .toList();
        List<PlaceView> page = matching.size() > limit ? matching.subList(0, limit) : matching;
        String next = matching.size() > limit ? encodeCursor(page.getLast().id()) : null;
        return new Page(page, next);
    }

    /**
     * A page of places.
     *
     * @param items the places
     * @param nextCursor the cursor of the next page, or null at the end
     */
    public record Page(List<PlaceView> items, String nextCursor) {}

    /** The place, if it exists. */
    public Optional<PlaceView> find(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(cache().get(id));
    }

    /**
     * The place.
     *
     * @throws ForesignalException {@code 404 UNKNOWN_PLACE}
     */
    public PlaceView get(String id) {
        return find(id).orElseThrow(() -> unknown(id));
    }

    /**
     * The places of {@code ids}, in order.
     *
     * @throws ForesignalException {@code 404 UNKNOWN_PLACE} naming the first unknown id
     */
    public List<PlaceView> resolve(Collection<String> ids) {
        List<PlaceView> out = new ArrayList<>();
        for (String id : new LinkedHashSet<>(ids)) {
            out.add(get(id));
        }
        return out;
    }

    /**
     * The union of the areas of {@code ids}, optionally buffered by {@code extraKm} on the
     * ellipsoid, as one fixed MultiPolygon.
     *
     * @throws ForesignalException {@code 404 UNKNOWN_PLACE}
     */
    public MultiPolygon union(Collection<String> ids, double extraKm) {
        List<Geometry> areas = new ArrayList<>();
        for (PlaceView place : resolve(ids)) {
            areas.add(place.area());
        }
        Geometry union = areas.size() == 1 ? areas.getFirst()
                : GeometryFixer.fix(areas.getFirst().getFactory().buildGeometry(areas).union());
        MultiPolygon area = ai.foresignal.trigger.Antimeridian.toMulti(union);
        return extraKm > 0 ? geometry.buffer(area, extraKm * 1000.0) : area;
    }

    /** The union of the areas of {@code ids}, or null when any is unknown (for {@link PlaceAreas}). */
    Geometry areaOrNull(Collection<String> ids) {
        try {
            return union(ids, 0);
        } catch (ForesignalException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------------- PlaceLookup

    @Override
    public Optional<Geometry> geometry(String placeId) {
        return find(placeId).map(PlaceView::area);
    }

    @Override
    public Optional<Integer> version(String placeId) {
        return find(placeId).map(PlaceView::version);
    }

    @Override
    public Optional<String> name(String placeId) {
        return find(placeId).map(PlaceView::name);
    }

    @Override
    public Optional<Geometry> origin(String placeId) {
        return find(placeId).map(PlaceView::geometry);
    }

    /**
     * The place's representative point (prompt 049): the point itself, or the interior point of a
     * polygon or route, as {@code {lon, lat}}.
     */
    @Override
    public Optional<double[]> point(String placeId) {
        return origin(placeId).filter(g -> !g.isEmpty()).map(g -> {
            org.locationtech.jts.geom.Point p = g instanceof org.locationtech.jts.geom.Point point ? point
                    : g.getInteriorPoint();
            return new double[] {p.getX(), p.getY()};
        });
    }

    /**
     * Every place for an adapter (prompt 053): id, name, representative point, products and version, ordered
     * by id. Places without a usable geometry are left out.
     */
    @Override
    public List<PlacePoints.Summary> summaries() {
        return all().stream()
                .sorted(java.util.Comparator.comparing(PlaceView::id))
                .map(v -> {
                    org.locationtech.jts.geom.Geometry g = v.geometry() == null || v.geometry().isEmpty() ? v.area() : v.geometry();
                    if (g == null || g.isEmpty()) {
                        return null;
                    }
                    org.locationtech.jts.geom.Point p = g instanceof org.locationtech.jts.geom.Point point ? point
                            : g.getInteriorPoint();
                    return new PlacePoints.Summary(v.id(), v.name(), p.getX(), p.getY(), v.products(), v.version());
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    // ------------------------------------------------------------------------- writes

    /**
     * Creates a place from a {@code PlaceInput}.
     *
     * @throws ValidationException when the input is not valid
     * @throws ConflictException {@code 409 PLACE_EXISTS} when the id is taken
     */
    public PlaceView create(JsonNode body) {
        Input input = parse(body, null);
        if (places.existsById(input.id())) {
            throw new ConflictException(ErrorCodes.PLACE_EXISTS, "A place with id '" + input.id() + "' already exists.");
        }
        return save(input, null);
    }

    /**
     * Creates or replaces the place {@code id} (settings import).
     *
     * @throws ValidationException when the input is not valid
     */
    public PlaceView upsert(String id, JsonNode body) {
        Input input = parse(body, id);
        return save(input, places.existsById(id) ? id : null);
    }

    /**
     * Replaces the place {@code id} whole with a {@code PlaceInput} (its {@code id} must match or be
     * absent).
     *
     * @throws ForesignalException {@code 404 UNKNOWN_PLACE}
     * @throws ValidationException when the input is not valid
     */
    public PlaceView update(String id, JsonNode body) {
        if (!places.existsById(id)) {
            throw unknown(id);
        }
        return save(parse(body, id), id);
    }

    private PlaceView save(Input input, String existing) {
        Instant now = clock.instant();
        List<UUID> broken = new ArrayList<>();
        PlaceEntity saved = transactions.execute(status -> {
            PlaceEntity entity = existing == null ? new PlaceEntity(input.id())
                    : places.findById(existing).orElseThrow(() -> unknown(existing));
            entity.setName(input.name());
            entity.setShape(input.shape());
            entity.setGeom(input.geometry());
            entity.setRadiusKm("point".equals(input.shape()) ? input.radiusKm() : null);
            entity.setBufferM("route".equals(input.shape()) ? input.bufferM() : null);
            entity.setArea(input.area());
            entity.setTimezone(input.timezone());
            entity.setKind(input.kind());
            entity.setTags(input.tags());
            entity.setProducts(input.products());
            entity.setAttributes(input.attributes());
            entity.setSensitive(input.sensitive());
            entity.setVersion(entity.getVersion() + 1);
            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(now);
            }
            entity.setUpdatedAt(now);
            PlaceEntity written = places.saveAndFlush(entity);
            cache.put(written.getId(), PlaceView.of(written));
            broken.addAll(rematerialize(written.getId(), now));
            return written;
        });
        cache.put(saved.getId(), PlaceView.of(saved));
        events.publishEvent(new PlaceChangedEvent(saved.getId(), saved.getVersion(), false));
        events.publishEvent(new TriggerRulesChangedEvent(null));
        if (!broken.isEmpty()) {
            log.warn("Place {} saved; {} referencing rule(s) could not be re-materialized and were disabled: {}",
                    saved.getId(), broken.size(), broken);
        }
        return PlaceView.of(saved);
    }

    /**
     * Re-materializes every rule whose {@code near} names {@code placeId}, inside the caller's
     * transaction; a rule whose fence can no longer be built is disabled with {@code lastError}.
     *
     * @return the rules that were disabled
     */
    List<UUID> rematerialize(String placeId, Instant now) {
        List<UUID> disabled = new ArrayList<>();
        for (UUID ruleId : referencingRules(placeId)) {
            TriggerRuleEntity rule = rules.findById(ruleId).orElse(null);
            if (rule == null || rule.getNear() == null) {
                continue;
            }
            try {
                RuleValidator.Materialized m = validator.rematerialize(rule.getNear(), rule.getHysteresisMeters());
                rule.setGeofence(m.fence());
                rule.setGeofenceInner(m.inner());
                rule.setGeofenceOuter(m.outer());
                rule.setNear(m.near());
                rule.setGeofenceOrigin("near");
            } catch (ForesignalException e) {
                rule.setEnabled(false);
                rule.setLastError("The place '" + placeId + "' changed and the rule's fence could not be rebuilt: "
                        + e.getMessage());
                rule.setLastErrorAt(now);
                disabled.add(ruleId);
            }
            rule.setUpdatedAt(now);
            rules.saveAndFlush(rule);
        }
        return disabled;
    }

    /**
     * Deletes a place.
     *
     * @param force disable referencing rules instead of refusing
     * @throws ForesignalException {@code 404 UNKNOWN_PLACE}, or {@code 409 PLACE_IN_USE} listing the
     *     referencing rules, views and briefings
     */
    public void delete(String id, boolean force) {
        if (!places.existsById(id)) {
            throw unknown(id);
        }
        References refs = references(id);
        if (!refs.isEmpty() && !force) {
            throw new ForesignalException(HttpStatus.CONFLICT, ErrorCodes.PLACE_IN_USE,
                    "The place '" + id + "' is referenced by " + refs.describe()
                            + ". Remove the references or delete with force=true, which disables the rules.")
                    .withProperty("references", refs.toMap());
        }
        Instant now = clock.instant();
        int version = cache().containsKey(id) ? cache.get(id).version() : 0;
        transactions.executeWithoutResult(status -> {
            for (UUID ruleId : refs.rules()) {
                rules.findById(ruleId).ifPresent(rule -> {
                    rule.setEnabled(false);
                    rule.setLastError("The place '" + id + "' was deleted; the rule was disabled.");
                    rule.setLastErrorAt(now);
                    rule.setUpdatedAt(now);
                    rules.saveAndFlush(rule);
                });
            }
            places.deleteById(id);
            places.flush();
        });
        cache.remove(id);
        events.publishEvent(new PlaceChangedEvent(id, version, true));
        if (!refs.rules().isEmpty()) {
            events.publishEvent(new TriggerRulesChangedEvent(null));
        }
    }

    /**
     * What references a place.
     *
     * @param rules trigger rules whose {@code near} names it
     * @param views saved views listing it
     * @param briefings briefings whose scope lists it
     */
    public record References(List<UUID> rules, List<UUID> views, List<UUID> briefings) {

        /** True when nothing references the place. */
        public boolean isEmpty() {
            return rules.isEmpty() && views.isEmpty() && briefings.isEmpty();
        }

        String describe() {
            return rules.size() + " rule(s), " + views.size() + " view(s) and " + briefings.size() + " briefing(s)";
        }

        Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("rules", rules.stream().map(UUID::toString).toList());
            out.put("views", views.stream().map(UUID::toString).toList());
            out.put("briefings", briefings.stream().map(UUID::toString).toList());
            return out;
        }
    }

    /** What references {@code id}. */
    public References references(String id) {
        List<UUID> views = tableExists("saved_view")
                ? jdbc.queryForList("SELECT id FROM saved_view WHERE ? = ANY(place_ids) ORDER BY id", UUID.class, id)
                : List.of();
        List<UUID> briefings = jdbc.queryForList(
                "SELECT id FROM briefing WHERE (scope -> 'placeIds') @> to_jsonb(CAST(? AS text)) ORDER BY id",
                UUID.class, id);
        return new References(referencingRules(id), views, briefings);
    }

    private List<UUID> referencingRules(String id) {
        return jdbc.queryForList("SELECT id FROM trigger_rule WHERE (near -> 'placeIds') @> to_jsonb(CAST(? AS text))"
                + " OR near ->> 'placeId' = ? ORDER BY id", UUID.class, id, id);
    }

    private boolean tableExists(String table) {
        Boolean exists = jdbc.queryForObject("SELECT to_regclass(?) IS NOT NULL", Boolean.class, table);
        return Boolean.TRUE.equals(exists);
    }

    // ------------------------------------------------------------------------- input

    /**
     * A validated {@code PlaceInput} with its derived area.
     *
     * @param id the id
     * @param name the name
     * @param shape the shape
     * @param geometry the geometry
     * @param radiusKm the radius (point)
     * @param bufferM the buffer (route)
     * @param area the derived area
     * @param timezone the timezone
     * @param kind the kind
     * @param tags the tags
     * @param products the products
     * @param attributes the attributes
     * @param sensitive the sensitive flag
     */
    record Input(String id, String name, String shape, Geometry geometry, Double radiusKm, Double bufferM,
            MultiPolygon area, String timezone, String kind, List<String> tags, List<String> products,
            Map<String, Object> attributes, boolean sensitive) {}

    /**
     * Validates a {@code PlaceInput}; with {@code pathId} the body's {@code id} must match it or be
     * absent.
     */
    Input parse(JsonNode body, String pathId) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a PlaceInput object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String member : body.propertyNames()) {
            if (!MEMBERS.contains(member) && !OUTPUT_ONLY.contains(member)) {
                errors.add(new FieldError(member, "is not a PlaceInput member"));
            }
        }
        String id = text(body, "id", errors);
        if (pathId != null) {
            if (id != null && !id.equals(pathId)) {
                errors.add(new FieldError("id", "must match the place id in the path"));
            }
            id = pathId;
        }
        if (id == null) {
            errors.add(new FieldError("id", "is required"));
        } else if (id.length() > 64 || !ID.matcher(id).matches()) {
            errors.add(new FieldError("id", "must match " + ID.pattern() + " and be at most 64 characters"));
        } else if (RESERVED_IDS.contains(id)) {
            errors.add(new FieldError("id", "'" + id + "' is reserved"));
        }
        String name = text(body, "name", errors);
        if (name == null || name.isBlank()) {
            errors.add(new FieldError("name", "is required"));
        } else if (name.length() > 200) {
            errors.add(new FieldError("name", "must be at most 200 characters"));
        }
        String shape = text(body, "shape", errors);
        if (shape == null || !SHAPES.contains(shape)) {
            errors.add(new FieldError("shape", "must be point, polygon or route"));
            shape = null;
        }
        Double radiusKm = number(body, "radiusKm", errors);
        Double bufferM = number(body, "bufferM", errors);
        if (shape != null) {
            if ("point".equals(shape)) {
                radiusKm = radiusKm == null ? DEFAULT_RADIUS_KM : radiusKm;
                if (radiusKm < 0.1 || radiusKm > 500) {
                    errors.add(new FieldError("radiusKm", "must be between 0.1 and 500"));
                }
            } else if (body.hasNonNull("radiusKm")) {
                errors.add(new FieldError("radiusKm", "applies only to a point place"));
            }
            if ("route".equals(shape)) {
                if (bufferM == null) {
                    errors.add(new FieldError("bufferM", "is required for a route place"));
                } else if (bufferM < 10 || bufferM > 50_000) {
                    errors.add(new FieldError("bufferM", "must be between 10 and 50000"));
                }
            } else if (body.hasNonNull("bufferM")) {
                errors.add(new FieldError("bufferM", "applies only to a route place"));
            }
        }
        Geometry geom = null;
        JsonNode geometryNode = body.get("geometry");
        if (geometryNode == null || geometryNode.isNull()) {
            errors.add(new FieldError("geometry", "is required"));
        } else if (shape != null) {
            try {
                geom = geometry.read(shape, geometryNode);
            } catch (InvalidInputException e) {
                errors.add(new FieldError("geometry", e.getMessage()));
            }
        }
        String timezone = text(body, "timezone", errors);
        if (timezone == null || timezone.isBlank()) {
            timezone = serverZone();
        } else {
            try {
                ZoneId.of(timezone);
            } catch (DateTimeException e) {
                errors.add(new FieldError("timezone", "is not a known IANA timezone"));
            }
        }
        String kind = text(body, "kind", errors);
        kind = kind == null ? "user" : kind;
        if (!KINDS.contains(kind)) {
            errors.add(new FieldError("kind", "must be user, airport or asset"));
        }
        List<String> tags = strings(body, "tags", errors);
        List<String> products = strings(body, "products", errors);
        boolean sensitive = true;
        JsonNode sensitiveNode = body.get("sensitive");
        if (sensitiveNode != null && !sensitiveNode.isNull()) {
            if (!sensitiveNode.isBoolean()) {
                errors.add(new FieldError("sensitive", "must be true or false"));
            } else {
                sensitive = sensitiveNode.asBoolean();
            }
        }
        Map<String, Object> attributes = new LinkedHashMap<>();
        JsonNode attributesNode = body.get("attributes");
        if (attributesNode != null && !attributesNode.isNull()) {
            if (!attributesNode.isObject()) {
                errors.add(new FieldError("attributes", "must be an object"));
            } else {
                JsonNode zip = attributesNode.get("zip");
                if (zip != null && !zip.isNull() && (!zip.isString() || !ZIP.matcher(zip.asString()).matches())) {
                    errors.add(new FieldError("attributes.zip", "must be a five-digit US ZIP code"));
                }
                for (String key : attributesNode.propertyNames()) {
                    attributes.put(key, ai.foresignal.trigger.JsonTrees.toJava(attributesNode.get(key)));
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The place is not valid.", errors);
        }
        MultiPolygon area;
        try {
            area = geometry.area(shape, geom, radiusKm == null ? 0 : radiusKm, bufferM == null ? 0 : bufferM);
        } catch (InvalidInputException e) {
            throw ValidationException.forField("geometry", e.getMessage());
        }
        return new Input(id, name.strip(), shape, geom, radiusKm, bufferM, area, timezone, kind, tags, products,
                attributes, sensitive);
    }

    private String serverZone() {
        ServerSettings server = settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class, ServerSettings::defaults);
        return server == null || server.timezone() == null ? ServerSettings.DEFAULT_TIMEZONE : server.timezone();
    }

    private static String text(JsonNode body, String field, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString()) {
            errors.add(new FieldError(field, "must be a string"));
            return null;
        }
        return node.asString();
    }

    private static Double number(JsonNode body, String field, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isNumber() || !Double.isFinite(node.asDouble())) {
            errors.add(new FieldError(field, "must be a number"));
            return null;
        }
        return node.asDouble();
    }

    private static List<String> strings(JsonNode body, String field, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return List.of();
        }
        if (!node.isArray()) {
            errors.add(new FieldError(field, "must be an array of strings"));
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

    static ForesignalException unknown(String id) {
        return new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_PLACE, "No place has the id '" + id + "'.");
    }

    static String encodeCursor(String id) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(("p:" + id).getBytes(StandardCharsets.UTF_8));
    }

    static String decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String text = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            if (!text.startsWith("p:")) {
                throw new IllegalArgumentException();
            }
            return text.substring(2);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(ErrorCodes.INVALID_CURSOR, "The cursor is not valid.",
                    List.of(new FieldError("cursor", "is not a cursor of this listing")));
        }
    }
}
