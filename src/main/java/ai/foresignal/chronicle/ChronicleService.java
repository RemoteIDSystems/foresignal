package ai.foresignal.chronicle;

import ai.foresignal.analyst.AnalystProposalRepository;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collab.AccessAction;
import ai.foresignal.collab.AccessResolver;
import ai.foresignal.collab.AclRole;
import ai.foresignal.collab.CollabInputs;
import ai.foresignal.collab.ShareService;
import ai.foresignal.collab.ShareableObjectEntity;
import ai.foresignal.collab.Visibility;
import ai.foresignal.object.JsonMergePatch;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.WorkspaceEntity;
import ai.foresignal.object.WorkspaceRepository;
import ai.foresignal.object.WorkspaceService;
import ai.foresignal.place.PlaceService;
import ai.foresignal.security.Tlp;
import ai.foresignal.security.Ulids;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.stream.ChangeBus;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Chronicles as work products (prompt 131): create, read, change and delete them, their layers and their lifecycle.
 *
 * <ul>
 *   <li><b>Ownership.</b> A chronicle is owned by its creator's principal and registered with 130's
 *       {@link AccessResolver} (visibility and TLP); a caller without a role sees {@code 404 UNKNOWN_CHRONICLE};
 *       editing, layers and lifecycle need {@code editor}; deleting needs {@code owner} and the display id as
 *       confirmation. A new chronicle inherits the strictest TLP of its linked investigations and of the workspace it
 *       was made from.</li>
 *   <li><b>Workspace.</b> Each chronicle owns a 129 workspace (context: its layers and its span); the client keeps its
 *       layout there. Access to the chronicle gives the same access to its workspace.</li>
 *   <li><b>Lifecycle.</b> proposed → active ⇄ paused → stopped → archived (cold package, restorable) → stopped;
 *       anything else is {@code 409 CHRONICLE_STATE_INVALID}. Activation creates the storage leaf and starts capture
 *       at the change bus's watermark; layer edits apply from the edit's seq onward. Every transition writes a
 *       {@code status-change} thread item (SSE {@code chronicle.status}).</li>
 * </ul>
 */
@Service
public class ChronicleService {

    private static final Logger log = LoggerFactory.getLogger(ChronicleService.class);

    /** The advisory-lock key that serializes display-id allocation. */
    static final long DISPLAY_ID_LOCK = 0x43485249L;

    /** The members of {@code ChronicleInput}. */
    static final Set<String> INPUT_FIELDS = Set.of("name", "description", "visibility", "tlp", "area", "retention",
            "sizeCapBytes", "situationIds", "hypothesisSetIds", "investigationIds", "topicIds", "layers",
            "fromWorkspaceId", "activate");

    /** The members of {@code ChroniclePatch}. */
    static final Set<String> PATCH_FIELDS = Set.of("name", "description", "visibility", "tlp", "area", "retention",
            "sizeCapBytes", "situationIds", "hypothesisSetIds", "investigationIds", "topicIds");

    /** The members of {@code ChronicleLayerInput}. */
    static final Set<String> LAYER_FIELDS = Set.of("name", "selector", "areaOverride", "capture", "enabled");

    /** Most layers per chronicle. */
    public static final int MAX_LAYERS = 100;

    private final ChronicleRepository chronicles;
    private final ChronicleLayerRepository layers;
    private final ChronicleSelectorValidator selectors;
    private final ChronicleEstimator estimator;
    private final ChronicleStorage storage;
    private final ChronicleThreadService thread;
    private final ChronicleCaptureService capture;
    private final ChronicleJobs jobs;
    private final AccessResolver access;
    private final ShareService shares;
    private final WorkspaceRepository workspaces;
    private final WorkspaceService workspaceService;
    private final PlaceService places;
    private final SourceRegistry registry;
    private final AppSettingService settings;
    private final ChangeBus changeBus;
    private final ObjectProvider<AnalystProposalRepository> proposals;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;
    private final TransactionTemplate tx;

    /** Creates the service. */
    public ChronicleService(ChronicleRepository chronicles, ChronicleLayerRepository layers,
            ChronicleSelectorValidator selectors, ChronicleEstimator estimator, ChronicleStorage storage,
            ChronicleThreadService thread, ChronicleCaptureService capture, ChronicleJobs jobs, AccessResolver access,
            ShareService shares, WorkspaceRepository workspaces, WorkspaceService workspaceService, PlaceService places,
            SourceRegistry registry, AppSettingService settings, ChangeBus changeBus,
            ObjectProvider<AnalystProposalRepository> proposals, JdbcTemplate jdbc, JsonMapper json, Clock clock,
            PlatformTransactionManager transactions) {
        this.chronicles = chronicles;
        this.layers = layers;
        this.selectors = selectors;
        this.estimator = estimator;
        this.storage = storage;
        this.thread = thread;
        this.capture = capture;
        this.jobs = jobs;
        this.access = access;
        this.shares = shares;
        this.workspaces = workspaces;
        this.workspaceService = workspaceService;
        this.places = places;
        this.registry = registry;
        this.settings = settings;
        this.changeBus = changeBus;
        this.proposals = proposals;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
        this.tx = new TransactionTemplate(transactions);
    }

    /** The object reference of a chronicle. */
    public static String ref(String id) {
        return "chronicle:" + id;
    }

    // ------------------------------------------------------------------------- reads

    /** A chronicle by id or display id, or {@code 404 UNKNOWN_CHRONICLE}. */
    public ChronicleEntity get(String id) {
        return find(id).orElseThrow(() -> unknown(id));
    }

    /** A chronicle by id or display id. */
    public Optional<ChronicleEntity> find(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        Optional<ChronicleEntity> found = Ulids.isUlid(id) ? chronicles.findById(id) : Optional.empty();
        if (found.isEmpty() && id.startsWith("CHR-")) {
            found = chronicles.findByDisplayId(id);
        }
        return found;
    }

    /**
     * The chronicle when the caller may perform {@code action}.
     *
     * @throws NotFoundException {@code UNKNOWN_CHRONICLE} when it does not exist or the caller may not see it
     * @throws ForesignalException 403 {@code FORBIDDEN} when the caller's role is too weak
     */
    public ChronicleEntity require(String id, ObjectCaller caller, AccessAction action) {
        ChronicleEntity c = find(id).orElseThrow(() -> unknown(id));
        AclRole role = access.role(caller, ref(c.getId()));
        if (role == null) {
            throw unknown(id);
        }
        access.require(caller, ref(c.getId()), action);
        return c;
    }

    /** The caller's role on a chronicle, or null. */
    public AclRole role(ChronicleEntity c, ObjectCaller caller) {
        return access.role(caller, ref(c.getId()));
    }

    /** The live layers of a chronicle (removed ones left out), in the order added. */
    public List<ChronicleLayerEntity> liveLayers(String chronicleId) {
        return layers.findByChronicleIdOrderByAddedAtAscIdAsc(chronicleId).stream()
                .filter(ChronicleLayerEntity::live).toList();
    }

    /** A live layer of a chronicle, or {@code 404 UNKNOWN_CHRONICLE_LAYER}. */
    public ChronicleLayerEntity layer(String chronicleId, String layerId) {
        return layers.findById(layerId == null ? "" : layerId)
                .filter(l -> l.getChronicleId().equals(chronicleId) && l.live())
                .orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_CHRONICLE_LAYER,
                        "The chronicle has no layer '" + layerId + "'."));
    }

    /**
     * The chronicles the caller can see, newest change first.
     *
     * @param caller the caller
     * @param status only this status, or null
     * @param scope {@code mine}, {@code shared} or {@code all}
     * @param situationId only chronicles linked to this situation, or null
     * @param q a case-insensitive name match, or null
     * @param cursor the opaque cursor, or null
     * @param limit the page size
     * @return the {@code ChroniclePage}
     */
    public Map<String, Object> list(ObjectCaller caller, String status, String scope, String situationId, String q,
            String cursor, int limit) {
        List<ChronicleEntity> all = chronicles.findAllByOrderByUpdatedAtDescIdAsc();
        Map<String, AclRole> roles = access.roles(caller, all.stream().map(c -> ref(c.getId())).toList());
        List<ChronicleEntity> visible = new ArrayList<>();
        String needle = q == null ? null : q.toLowerCase(java.util.Locale.ROOT);
        for (ChronicleEntity c : all) {
            if (roles.get(ref(c.getId())) == null) {
                continue;
            }
            if (status != null && !status.equals(c.getStatus())) {
                continue;
            }
            if (situationId != null && !c.getSituationIds().contains(situationId)) {
                continue;
            }
            if (needle != null && !c.getName().toLowerCase(java.util.Locale.ROOT).contains(needle)) {
                continue;
            }
            String owner = access.record(ref(c.getId())).map(ShareableObjectEntity::getOwner).orElse(null);
            boolean mine = caller.principal() != null && caller.principal().equals(owner);
            if (("mine".equals(scope) && !mine) || ("shared".equals(scope) && mine)) {
                continue;
            }
            visible.add(c);
        }
        int offset = CollabInputs.offset(cursor);
        List<Map<String, Object>> items = new ArrayList<>();
        for (ChronicleEntity c : visible.subList(Math.min(offset, visible.size()),
                Math.min(offset + limit, visible.size()))) {
            items.add(view(c, caller, false));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", offset + limit < visible.size() ? CollabInputs.cursor(offset + limit) : null);
        return out;
    }

    // ------------------------------------------------------------------------- create

    /**
     * Creates a chronicle from a {@code ChronicleInput}.
     *
     * @param body the input
     * @param caller the caller (the owner)
     * @param origin {@code operator}, {@code agent} or {@code analyst}
     * @param proposalId the 102 proposal behind it, or null
     * @return the chronicle (activated when the input asks)
     */
    public ChronicleEntity create(JsonNode body, ObjectCaller caller, String origin, String proposalId) {
        CollabInputs.object(body, "ChronicleInput", INPUT_FIELDS);
        String name = CollabInputs.required(body, "name", 1, 200).strip();
        String description = CollabInputs.text(body, "description", 0, 20_000);
        Visibility visibility = Visibility.parse("visibility", CollabInputs.text(body, "visibility", 1, 16),
                Visibility.PRIVATE);
        Tlp tlp = Tlp.parse("tlp", CollabInputs.text(body, "tlp", 1, 16), access.defaultTlp());
        String retention = retention(CollabInputs.text(body, "retention", 1, 32));
        long cap = sizeCap(body.get("sizeCapBytes"));
        ChronicleArea area = ChronicleArea.parse(body.get("area"), "area", id -> places.find(id).isPresent());
        List<String> situationIds = ulids(CollabInputs.strings(body, "situationIds", 200), "situationIds");
        List<String> setIds = ulids(CollabInputs.strings(body, "hypothesisSetIds", 200), "hypothesisSetIds");
        List<String> investigationIds = CollabInputs.strings(body, "investigationIds", 200);
        List<String> topicIds = CollabInputs.strings(body, "topicIds", 200);
        List<PendingLayer> pending = new ArrayList<>();
        JsonNode layerNodes = body.get("layers");
        if (layerNodes != null && !layerNodes.isNull()) {
            if (!layerNodes.isArray()) {
                throw ValidationException.forField("layers", "must be an array of ChronicleLayerInput");
            }
            if (layerNodes.size() > MAX_LAYERS) {
                throw ValidationException.forField("layers", "holds more than " + MAX_LAYERS + " layers");
            }
            int i = 0;
            for (JsonNode n : layerNodes) {
                pending.add(pendingLayer(n, "layers[" + i++ + "]"));
            }
        }
        String fromWorkspace = CollabInputs.text(body, "fromWorkspaceId", 26, 26);
        if (fromWorkspace != null) {
            WorkspaceEntity w = workspaceService.get(fromWorkspace, caller);
            FromWorkspace derived = fromWorkspace(w);
            if (area == null) {
                area = derived.area();
            }
            pending.addAll(derived.layers());
            for (String s : derived.situationIds()) {
                if (!situationIds.contains(s)) {
                    situationIds.add(s);
                }
            }
            for (String h : derived.hypothesisSetIds()) {
                if (!setIds.contains(h)) {
                    setIds.add(h);
                }
            }
            tlp = Tlp.stricter(tlp, access.tlpOf("workspace:" + w.getId()));
        }
        tlp = inheritTlp(tlp, investigationIds);
        String owner = caller.principal();
        if (owner == null) {
            throw new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                    "Creating a chronicle needs a token.");
        }
        shares.checkTlp(tlp, owner, List.of(), visibility);
        boolean activate = CollabInputs.bool(body, "activate", false);
        final ChronicleArea finalArea = area;
        final Tlp finalTlp = tlp;
        ChronicleEntity created = tx.execute(s -> {
            Instant now = clock.instant();
            ChronicleEntity c = new ChronicleEntity(Ulids.next(now), nextDisplayId(now), name, cap, now);
            c.setDescription(description);
            c.setArea(finalArea == null ? null : finalArea.toMap());
            c.setRetention(retention);
            c.setSituationIds(situationIds);
            c.setHypothesisSetIds(setIds);
            c.setInvestigationIds(investigationIds);
            c.setTopicIds(topicIds);
            c.setOrigin(ChronicleVocabulary.ORIGINS.contains(origin) ? origin : ChronicleVocabulary.ORIGIN_OPERATOR);
            c.setProposalId(proposalId);
            c.setCreatedBy(caller.actor());
            chronicles.save(c);
            List<String> layerIds = new ArrayList<>();
            for (PendingLayer p : pending) {
                layerIds.add(saveLayer(c, p, now, null).getId());
            }
            WorkspaceEntity w = new WorkspaceEntity(Ulids.next(now), workspaceName(c), owner, now);
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("layers", layerIds);
            context.put("timeRange", Map.of("start", now.toString()));
            w.setContext(context);
            workspaces.save(w);
            c.setWorkspaceId(w.getId());
            chronicles.save(c);
            access.register(ref(c.getId()), owner, visibility, finalTlp);
            access.register("workspace:" + w.getId(), owner, Visibility.PRIVATE, finalTlp);
            return c;
        });
        capture.markDirty();
        if (activate) {
            return activate(created.getId(), null, caller, false);
        }
        return created;
    }

    /** The name of a chronicle's workspace. */
    static String workspaceName(ChronicleEntity c) {
        String name = c.getDisplayId() + " " + c.getName();
        return name.length() > 200 ? name.substring(0, 200) : name;
    }

    private Tlp inheritTlp(Tlp tlp, List<String> investigationIds) {
        Tlp out = tlp;
        for (String inv : investigationIds) {
            Optional<ShareableObjectEntity> rec = access.record("investigation:" + inv);
            if (rec.isPresent()) {
                out = Tlp.stricter(out, Tlp.fromWire(rec.get().getTlp()));
            }
        }
        return out;
    }

    /**
     * A validated layer not yet saved.
     *
     * @param name the name
     * @param selector the selector
     * @param areaOverride the area override, or null
     * @param capture the capture mode
     * @param enabled whether it captures
     */
    public record PendingLayer(String name, ChronicleSelector selector, ChronicleArea areaOverride, CaptureMode capture,
            boolean enabled) {}

    /** Parses and checks a {@code ChronicleLayerInput}. */
    public PendingLayer pendingLayer(JsonNode n, String field) {
        if (n == null || !n.isObject()) {
            throw ChronicleSelector.invalid(field, "must be a ChronicleLayerInput object");
        }
        for (String member : n.propertyNames()) {
            if (!LAYER_FIELDS.contains(member)) {
                throw ChronicleSelector.invalid(field + "." + member, "is not a ChronicleLayerInput member");
            }
        }
        JsonNode nameNode = n.get("name");
        if (nameNode == null || !nameNode.isString() || nameNode.asString().isBlank()
                || nameNode.asString().length() > 200) {
            throw ChronicleSelector.invalid(field + ".name", "is required (1 to 200 characters)");
        }
        ChronicleSelector selector = selectors.parse(n.get("selector"), field + ".selector");
        ChronicleArea override = ChronicleArea.parse(n.get("areaOverride"), field + ".areaOverride",
                id -> places.find(id).isPresent());
        CaptureMode mode = CaptureMode.parse(n.get("capture"), field + ".capture");
        checkMode(selector, mode, field + ".capture");
        boolean enabled = n.hasNonNull("enabled") ? n.get("enabled").asBoolean(true) : true;
        return new PendingLayer(nameNode.asString().strip(), selector, override, mode, enabled);
    }

    /** {@code frames} needs raster products; raster products need {@code frames}, {@code full} or events-only. */
    static void checkMode(ChronicleSelector selector, CaptureMode mode, String field) {
        boolean raster = selector instanceof ChronicleSelector.RasterProducts;
        if (CaptureMode.FRAMES.equals(mode.mode()) && !raster) {
            throw ChronicleSelector.invalid(field + ".mode", "frames applies only to raster-products layers");
        }
        if (raster && !List.of(CaptureMode.FRAMES, CaptureMode.FULL, CaptureMode.EVENTS_ONLY).contains(mode.mode())) {
            throw ChronicleSelector.invalid(field + ".mode", "raster-products layers capture frames, full or"
                    + " events-only");
        }
        if (selector instanceof ChronicleSelector.Alerts && !CaptureMode.FULL.equals(mode.mode())) {
            throw ChronicleSelector.invalid(field + ".mode", "alerts layers capture full (every alert is a thread"
                    + " item)");
        }
    }

    private ChronicleLayerEntity saveLayer(ChronicleEntity c, PendingLayer p, Instant now, Long addedAtSeq) {
        ChronicleLayerEntity l = new ChronicleLayerEntity(Ulids.next(now), c.getId(), p.name(), now);
        l.setSelector(p.selector().toMap());
        l.setAreaOverride(p.areaOverride() == null ? null : p.areaOverride().toMap());
        l.setCapture(p.capture().toMap());
        l.setEnabled(p.enabled());
        l.setAddedAtSeq(addedAtSeq);
        l.setEstimate(estimator.project(c, p.selector(), p.capture()).toMap());
        return layers.save(l);
    }

    /**
     * What a workspace context gives a new chronicle.
     *
     * @param area the viewport as an area, or null
     * @param layers layers from the context's layers and filters
     * @param situationIds the active situation, if any
     * @param hypothesisSetIds the active hypothesis set, if any
     */
    record FromWorkspace(ChronicleArea area, List<PendingLayer> layers, List<String> situationIds,
            List<String> hypothesisSetIds) {}

    /**
     * Derives layers and an area from a workspace context: a registered source id becomes a {@code records} layer of
     * that source, a known kind a {@code records} layer of that kind (both narrowed by the {@code kinds} or
     * {@code sources} filters), {@code view:{id}} a saved-view layer; the viewport becomes the area; the active
     * situation and set are linked, the situation also as a {@code situations} layer. Other context layers are client
     * layers and are skipped.
     */
    FromWorkspace fromWorkspace(WorkspaceEntity w) {
        Map<String, Object> context = w.getContext() == null ? Map.of() : w.getContext();
        JsonNode ctx = json.valueToTree(context);
        ChronicleArea area = null;
        JsonNode viewport = ctx.get("viewport");
        if (viewport != null && viewport.isObject()) {
            area = viewportArea(viewport);
        }
        List<String> filterKinds = filterValues(ctx.path("filters"), "kinds", "kind");
        List<String> filterSources = filterValues(ctx.path("filters"), "sources", "source");
        List<PendingLayer> out = new ArrayList<>();
        Set<String> knownKinds = registry.kinds();
        JsonNode ctxLayers = ctx.get("layers");
        if (ctxLayers != null && ctxLayers.isArray()) {
            for (JsonNode l : ctxLayers) {
                String id = l.asString();
                if (id.startsWith("view:")) {
                    String viewId = id.substring("view:".length());
                    out.add(new PendingLayer("View " + viewId, selectors.parse(json.valueToTree(Map.of("type",
                            ChronicleSelector.SAVED_VIEW, "viewId", viewId)), "context.layers"), null,
                            CaptureMode.FULL_MODE, true));
                } else if (registry.contains(id)) {
                    boolean track = registry.descriptor(id).isTrack();
                    out.add(new PendingLayer(id, new ChronicleSelector.Records(List.of(id), filterKinds, List.of()),
                            null, track ? CaptureMode.downsampled(CaptureMode.DEFAULT_INTERVAL) : CaptureMode.FULL_MODE,
                            true));
                } else if (knownKinds.contains(id)) {
                    out.add(new PendingLayer(id, new ChronicleSelector.Records(filterSources, List.of(id), List.of()),
                            null, CaptureMode.FULL_MODE, true));
                }
            }
        }
        List<String> situations = new ArrayList<>();
        JsonNode s = ctx.get("activeSituationId");
        if (s != null && s.isString() && Ulids.isUlid(s.asString())) {
            situations.add(s.asString());
            out.add(new PendingLayer("Situation members", new ChronicleSelector.Situations(List.of(s.asString())),
                    null, CaptureMode.FULL_MODE, true));
        }
        List<String> sets = new ArrayList<>();
        JsonNode h = ctx.get("activeHypothesisSetId");
        if (h != null && h.isString() && Ulids.isUlid(h.asString())) {
            sets.add(h.asString());
        }
        return new FromWorkspace(area, out, situations, sets);
    }

    private static List<String> filterValues(JsonNode filters, String plural, String singular) {
        List<String> out = new ArrayList<>();
        for (String key : List.of(plural, singular)) {
            JsonNode v = filters.get(key);
            if (v == null || v.isNull()) {
                continue;
            }
            if (v.isArray()) {
                v.forEach(e -> {
                    if (e.isString() && !out.contains(e.asString())) {
                        out.add(e.asString());
                    }
                });
            } else if (v.isString() && !out.contains(v.asString())) {
                out.add(v.asString());
            }
        }
        return out;
    }

    private ChronicleArea viewportArea(JsonNode viewport) {
        JsonNode bbox = viewport.get("bbox");
        if (bbox != null && bbox.isArray() && bbox.size() == 4) {
            double w = bbox.get(0).asDouble();
            double s = bbox.get(1).asDouble();
            double e = bbox.get(2).asDouble();
            double n = bbox.get(3).asDouble();
            Map<String, Object> polygon = Map.of("type", "Polygon", "coordinates",
                    List.of(List.of(List.of(w, s), List.of(e, s), List.of(e, n), List.of(w, n), List.of(w, s))));
            return ChronicleArea.parse(json.valueToTree(Map.of("geometry", polygon)), "context.viewport", null);
        }
        JsonNode center = viewport.get("center");
        JsonNode radius = viewport.get("radiusKm");
        if (center != null && center.isArray() && center.size() == 2 && radius != null && radius.isNumber()) {
            Map<String, Object> point = Map.of("type", "Point", "coordinates",
                    List.of(center.get(0).asDouble(), center.get(1).asDouble()));
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("geometry", point);
            a.put("bufferKm", Math.min(ChronicleArea.MAX_BUFFER_KM, radius.asDouble()));
            return ChronicleArea.parse(json.valueToTree(a), "context.viewport", null);
        }
        return null;
    }

    // ------------------------------------------------------------------------- patch and delete

    /**
     * Applies a {@code ChroniclePatch} (JSON merge patch; owner or editor).
     *
     * @param id the chronicle
     * @param body the patch
     * @param ifMatch the {@code If-Match} header (the version as its ETag), or null
     * @param caller the caller
     * @return the changed chronicle
     * @throws ForesignalException {@code 412 CHRONICLE_VERSION_STALE}, {@code 409 CHRONICLE_CAP_EXCEEDED} when the cap
     *     falls below the bytes used, {@code 403 TLP_VIOLATION}
     */
    public ChronicleEntity patch(String id, JsonNode body, String ifMatch, ObjectCaller caller) {
        ChronicleEntity found = require(id, caller, AccessAction.EDIT);
        CollabInputs.object(body, "ChroniclePatch", PATCH_FIELDS);
        if (ifMatch != null && !ifMatch.isBlank() && !matches(ifMatch, found.getVersion())) {
            throw new ForesignalException(HttpStatus.PRECONDITION_FAILED, ErrorCodes.CHRONICLE_VERSION_STALE,
                    "The chronicle changed since version " + ifMatch + "; it is at version " + found.getVersion() + ".")
                    .withProperty("version", found.getVersion());
        }
        ChronicleEntity changed = capture.exclusive(() -> tx.execute(s -> {
            ChronicleEntity c = chronicles.findById(found.getId()).orElseThrow(() -> unknown(id));
            if (body.has("name")) {
                c.setName(CollabInputs.required(body, "name", 1, 200).strip());
            }
            if (body.has("description")) {
                c.setDescription(CollabInputs.text(body, "description", 0, 20_000));
            }
            if (body.has("area")) {
                ChronicleArea area = ChronicleArea.parse(body.get("area"), "area", pid -> places.find(pid).isPresent());
                c.setArea(area == null ? null : area.toMap());
            }
            if (body.has("retention")) {
                c.setRetention(retention(CollabInputs.text(body, "retention", 1, 32)));
            }
            if (body.has("sizeCapBytes")) {
                long cap = sizeCap(body.get("sizeCapBytes"));
                long used = storage.usage(c.getId())[0];
                if (cap < used) {
                    throw new ConflictException(ErrorCodes.CHRONICLE_CAP_EXCEEDED, "The cap (" + cap
                            + " bytes) is below the " + used + " bytes the chronicle already holds.");
                }
                c.setSizeCapBytes(cap);
                c.setCapState(ChronicleCaptureService.capState(used, cap));
            }
            if (body.has("situationIds")) {
                c.setSituationIds(ulids(CollabInputs.strings(body, "situationIds", 200), "situationIds"));
            }
            if (body.has("hypothesisSetIds")) {
                c.setHypothesisSetIds(ulids(CollabInputs.strings(body, "hypothesisSetIds", 200), "hypothesisSetIds"));
            }
            if (body.has("investigationIds")) {
                c.setInvestigationIds(CollabInputs.strings(body, "investigationIds", 200));
            }
            if (body.has("topicIds")) {
                c.setTopicIds(CollabInputs.strings(body, "topicIds", 200));
            }
            if (body.has("visibility") || body.has("tlp") || body.has("investigationIds")) {
                ShareableObjectEntity rec = access.record(ref(c.getId())).orElseThrow(() -> unknown(id));
                Visibility vis = body.has("visibility") ? Visibility.parse("visibility",
                        CollabInputs.text(body, "visibility", 1, 16), rec.getVisibility()) : rec.getVisibility();
                Tlp tlp = body.has("tlp") ? Tlp.parse("tlp", CollabInputs.text(body, "tlp", 1, 16),
                        Tlp.fromWire(rec.getTlp())) : Tlp.fromWire(rec.getTlp());
                tlp = inheritTlp(tlp, c.getInvestigationIds());
                shares.checkTlp(tlp, rec.getOwner(), shares.entries(rec.getObjectRef(),
                        ai.foresignal.collab.PrincipalViews.cache()).stream()
                        .map(e -> String.valueOf(e.get("grantee"))).toList(), vis);
                rec.setVisibility(vis);
                rec.setTlp(tlp.wireValue());
                access.save(rec);
            }
            c.touch(clock.instant());
            return chronicles.save(c);
        }));
        return changed;
    }

    /** True when an {@code If-Match} value names {@code version} ({@code "3"}, {@code W/"3"} or {@code 3}). */
    static boolean matches(String ifMatch, int version) {
        String v = ifMatch.strip();
        if ("*".equals(v)) {
            return true;
        }
        if (v.startsWith("W/")) {
            v = v.substring(2);
        }
        v = v.replace("\"", "");
        return v.equals(String.valueOf(version));
    }

    /** The ETag of a chronicle. */
    public static String etag(ChronicleEntity c) {
        return "\"" + c.getVersion() + "\"";
    }

    /**
     * Deletes a chronicle: its storage leaf, captured series, rollups and frame pins, its thread, layers, workspace
     * and access list (owner only, with {@code confirm} equal to its display id).
     *
     * @throws ValidationException {@code 400 CHRONICLE_CONFIRMATION_REQUIRED}
     */
    public void delete(String id, String confirm, ObjectCaller caller) {
        ChronicleEntity c = require(id, caller, AccessAction.DELETE);
        if (confirm == null || !confirm.strip().equals(c.getDisplayId())) {
            throw new ValidationException(ErrorCodes.CHRONICLE_CONFIRMATION_REQUIRED,
                    "Deleting a chronicle needs confirm equal to its display id.",
                    List.of(new FieldError("confirm", "must be " + c.getDisplayId())));
        }
        capture.exclusive(() -> {
            storage.deleteCaptured(c.getId());
            tx.executeWithoutResult(s -> {
                thread.deleteThread(c.getId());
                layers.deleteByChronicleId(c.getId());
                if (c.getWorkspaceId() != null) {
                    workspaces.findById(c.getWorkspaceId()).ifPresent(workspaces::delete);
                    access.forget("workspace:" + c.getWorkspaceId());
                }
                access.forget(ref(c.getId()));
                chronicles.deleteById(c.getId());
            });
            jobs.deletePackage(c.getId());
            return null;
        });
    }

    // ------------------------------------------------------------------------- layers

    /** Adds a layer (owner or editor); capture for it starts from the edit's seq onward. */
    public ChronicleLayerEntity addLayer(String id, JsonNode body, ObjectCaller caller) {
        ChronicleEntity c = require(id, caller, AccessAction.EDIT);
        requireNotArchived(c);
        PendingLayer p = pendingLayer(body, "layer");
        if (liveLayers(c.getId()).size() >= MAX_LAYERS) {
            throw ChronicleSelector.invalid("layer", "the chronicle already has " + MAX_LAYERS + " layers");
        }
        return capture.exclusive(() -> tx.execute(s -> {
            Instant now = clock.instant();
            Long seq = ChronicleVocabulary.PROPOSED.equals(c.getStatus()) ? null : changeBus.lastPublishedSeq() + 1;
            ChronicleLayerEntity l = saveLayer(c, p, now, seq);
            layerEdit(c, l, "added", null);
            syncWorkspaceLayers(c);
            return l;
        }));
    }

    /** Applies a {@code ChronicleLayerPatch} (owner or editor); the new definition applies from the edit's seq. */
    public ChronicleLayerEntity patchLayer(String id, String layerId, JsonNode body, ObjectCaller caller) {
        ChronicleEntity c = require(id, caller, AccessAction.EDIT);
        requireNotArchived(c);
        ChronicleLayerEntity existing = layer(c.getId(), layerId);
        CollabInputs.object(body, "ChronicleLayerPatch", LAYER_FIELDS);
        Map<String, Object> current = new LinkedHashMap<>();
        current.put("name", existing.getName());
        current.put("selector", existing.getSelector());
        current.put("areaOverride", existing.getAreaOverride());
        current.put("capture", existing.getCapture());
        current.put("enabled", existing.isEnabled());
        JsonNode merged = JsonMergePatch.apply(json.valueToTree(current), body);
        if (merged instanceof ObjectNode o && (o.get("areaOverride") == null)) {
            o.putNull("areaOverride");
        }
        PendingLayer p = pendingLayer(merged, "layer");
        return capture.exclusive(() -> tx.execute(s -> {
            ChronicleLayerEntity l = layer(c.getId(), layerId);
            l.setName(p.name());
            l.setSelector(p.selector().toMap());
            l.setAreaOverride(p.areaOverride() == null ? null : p.areaOverride().toMap());
            l.setCapture(p.capture().toMap());
            l.setEnabled(p.enabled());
            if (!ChronicleVocabulary.PROPOSED.equals(c.getStatus())) {
                l.setAddedAtSeq(changeBus.lastPublishedSeq() + 1);
            }
            l.setEstimate(estimator.project(c, p.selector(), p.capture()).toMap());
            l.setUpdatedAt(clock.instant());
            ChronicleLayerEntity saved = layers.save(l);
            layerEdit(c, saved, "changed", body);
            return saved;
        }));
    }

    /** Removes a layer; its captured data stays unless {@code purge}. */
    public void removeLayer(String id, String layerId, boolean purge, ObjectCaller caller) {
        ChronicleEntity c = require(id, caller, AccessAction.EDIT);
        requireNotArchived(c);
        layer(c.getId(), layerId);
        capture.exclusive(() -> tx.execute(s -> {
            ChronicleLayerEntity l = layer(c.getId(), layerId);
            l.setRemovedAt(clock.instant());
            l.setEnabled(false);
            l.setUpdatedAt(clock.instant());
            layers.save(l);
            if (purge) {
                long[] removed = storage.purgeLayer(c.getId(), l.getId());
                storage.advance(c.getId(), null, clock.instant(), -removed[0], -removed[1]);
                storage.layerCounters(l.getId(), -l.getRowsCaptured(), -l.getBytesCaptured(), null);
            }
            layerEdit(c, l, purge ? "removed and purged" : "removed", null);
            syncWorkspaceLayers(c);
            return null;
        }));
    }

    private void layerEdit(ChronicleEntity c, ChronicleLayerEntity l, String what, JsonNode patch) {
        ChronicleEventEntity item = thread.item(c.getId(), ChronicleVocabulary.ITEM_LAYER_EDIT, clock.instant(),
                "Layer " + what + ": " + l.getName());
        item.setLayerId(l.getId());
        item.setSeq(l.getAddedAtSeq());
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("layerId", l.getId());
        snapshot.put("name", l.getName());
        snapshot.put("selector", l.getSelector());
        snapshot.put("capture", l.getCapture());
        snapshot.put("enabled", l.isEnabled());
        snapshot.put("addedAtSeq", l.getAddedAtSeq());
        if (patch != null) {
            snapshot.put("patch", json.convertValue(patch, Map.class));
        }
        item.setSnapshot(snapshot);
        thread.append(item);
    }

    private void syncWorkspaceLayers(ChronicleEntity c) {
        if (c.getWorkspaceId() == null) {
            return;
        }
        workspaces.findById(c.getWorkspaceId()).ifPresent(w -> {
            Map<String, Object> context = new LinkedHashMap<>(w.getContext() == null ? Map.of() : w.getContext());
            context.put("layers", liveLayers(c.getId()).stream().map(ChronicleLayerEntity::getId).toList());
            w.setContext(context);
            w.setContextVersion(w.getContextVersion() + 1);
            w.setUpdatedAt(clock.instant());
            workspaces.save(w);
        });
    }

    private static void requireNotArchived(ChronicleEntity c) {
        if (ChronicleVocabulary.ARCHIVED.equals(c.getStatus())) {
            throw new ConflictException(ErrorCodes.CHRONICLE_STATE_INVALID,
                    "The chronicle is archived; restore it first.");
        }
    }

    // ------------------------------------------------------------------------- lifecycle

    /**
     * Activates a proposed chronicle: creates its leaf and starts capture at the change bus's watermark; an optional
     * backfill copies matching history still retained globally. Accepting a proposal of the analysis cycles also
     * marks the 102 proposal executed.
     *
     * @param id the chronicle
     * @param body the {@code ChronicleActivateRequest}, or null
     * @param caller the caller (editor)
     * @param auto true when the analysis cycles activate it on their own
     * @return the active chronicle
     */
    public ChronicleEntity activate(String id, JsonNode body, ObjectCaller caller, boolean auto) {
        ChronicleEntity c = require(id, caller, AccessAction.EDIT);
        Instant backfillFrom = null;
        if (body != null && !body.isNull()) {
            CollabInputs.object(body, "ChronicleActivateRequest", Set.of("backfill"));
            JsonNode b = body.get("backfill");
            if (b != null && !b.isNull()) {
                if (!b.isObject()) {
                    throw ValidationException.forField("backfill", "must be an object {from}");
                }
                backfillFrom = CollabInputs.instant(b, "from");
                if (backfillFrom == null) {
                    throw ValidationException.forField("backfill.from", "is required");
                }
            }
        }
        ChronicleEntity active = transition(c, Set.of(ChronicleVocabulary.PROPOSED), ChronicleVocabulary.ACTIVE,
                auto ? "activated by the analysis cycles" : "activated", ch -> {
                    long watermark = changeBus.lastPublishedSeq();
                    storage.createLeaf(ch.getId());
                    ch.setLeafCreated(true);
                    ch.setStartedAt(clock.instant());
                    ch.setCaptureFromSeq(watermark);
                    ch.setCursorSeq(watermark);
                    if (auto) {
                        ch.setAutoActivatedAt(clock.instant());
                    }
                    for (ChronicleLayerEntity l : liveLayers(ch.getId())) {
                        if (l.getAddedAtSeq() == null) {
                            l.setAddedAtSeq(watermark + 1);
                            layers.save(l);
                        }
                    }
                });
        markProposalExecuted(active, caller, auto);
        if (backfillFrom != null) {
            jobs.backfill(active.getId(), backfillFrom);
        }
        return active;
    }

    /** Pauses capture; history is kept. */
    public ChronicleEntity pause(String id, ObjectCaller caller) {
        ChronicleEntity c = require(id, caller, AccessAction.EDIT);
        return transition(c, Set.of(ChronicleVocabulary.ACTIVE), ChronicleVocabulary.PAUSED, "paused", ch -> { });
    }

    /** Resumes a paused chronicle from the present: the paused interval is not captured. */
    public ChronicleEntity resume(String id, ObjectCaller caller) {
        ChronicleEntity c = require(id, caller, AccessAction.EDIT);
        return transition(c, Set.of(ChronicleVocabulary.PAUSED), ChronicleVocabulary.ACTIVE,
                "resumed; the paused interval is not captured", ch -> ch.setCursorSeq(changeBus.lastPublishedSeq()));
    }

    /** Ends capture; history is kept and replayable. */
    public ChronicleEntity stop(String id, ObjectCaller caller) {
        ChronicleEntity c = require(id, caller, AccessAction.EDIT);
        return transition(c, Set.of(ChronicleVocabulary.ACTIVE, ChronicleVocabulary.PAUSED),
                ChronicleVocabulary.STOPPED, "stopped", ch -> {
                    Long cursor = jdbc.queryForObject("SELECT cursor_seq FROM chronicle WHERE id = ?", Long.class,
                            ch.getId());
                    ch.setCursorSeq(cursor);
                    ch.setStoppedAt(clock.instant());
                    ch.setCaptureToSeq(cursor);
                });
    }

    /** Archives a stopped chronicle to a cold package and drops its leaf (asynchronous; restorable). */
    public ChronicleEntity archive(String id, ObjectCaller caller) {
        ChronicleEntity c = require(id, caller, AccessAction.EDIT);
        if (!ChronicleVocabulary.STOPPED.equals(c.getStatus())) {
            throw stateInvalid(c, "archive", "stopped");
        }
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("state", "pending");
        state.put("exportId", null);
        state.put("archivedAt", null);
        state.put("bytes", null);
        state.put("error", null);
        ChronicleEntity pending = tx.execute(s -> {
            ChronicleEntity ch = chronicles.findById(c.getId()).orElseThrow(() -> unknown(id));
            ch.setArchive(state);
            ch.touch(clock.instant());
            return chronicles.save(ch);
        });
        jobs.archive(c.getId());
        return chronicles.findById(c.getId()).orElse(pending);
    }

    /** Restores an archived chronicle from its cold package (asynchronous); it returns as {@code stopped}. */
    public ChronicleEntity restore(String id, ObjectCaller caller) {
        ChronicleEntity c = require(id, caller, AccessAction.EDIT);
        if (!ChronicleVocabulary.ARCHIVED.equals(c.getStatus())) {
            throw stateInvalid(c, "restore", "archived");
        }
        ChronicleEntity pending = tx.execute(s -> {
            ChronicleEntity ch = chronicles.findById(c.getId()).orElseThrow(() -> unknown(id));
            Map<String, Object> archive = new LinkedHashMap<>(ch.getArchive() == null ? Map.of() : ch.getArchive());
            archive.put("state", "restoring");
            ch.setArchive(archive);
            ch.touch(clock.instant());
            return chronicles.save(ch);
        });
        jobs.restore(c.getId());
        return chronicles.findById(c.getId()).orElse(pending);
    }

    /** A lifecycle step applied to a chronicle inside its transition. */
    @FunctionalInterface
    interface Step {

        /** Applies the step. */
        void apply(ChronicleEntity chronicle);
    }

    /**
     * Moves a chronicle from one of {@code from} to {@code to} under the capture lock, writes the
     * {@code status-change} item and marks the index stale.
     *
     * @throws ConflictException {@code 409 CHRONICLE_STATE_INVALID}
     */
    ChronicleEntity transition(ChronicleEntity c, Set<String> from, String to, String reason, Step step) {
        return capture.exclusive(() -> tx.execute(s -> {
            ChronicleEntity ch = chronicles.findById(c.getId()).orElseThrow(() -> unknown(c.getId()));
            if (!from.contains(ch.getStatus())) {
                throw stateInvalid(ch, to, String.join(" or ", from));
            }
            String previous = ch.getStatus();
            step.apply(ch);
            ch.setStatus(to);
            ch.touch(clock.instant());
            ChronicleEntity saved = chronicles.save(ch);
            statusItem(saved, previous, reason);
            return saved;
        }));
    }

    /** Writes the {@code status-change} item ({@code ChronicleStatusEvent} snapshot) of a transition. */
    void statusItem(ChronicleEntity c, String previous, String reason) {
        long used = Optional.ofNullable(storage.usage(c.getId())).map(u -> u[0]).orElse(c.getBytesUsed());
        thread.statusChange(c, previous, reason, used);
    }

    private void markProposalExecuted(ChronicleEntity c, ObjectCaller caller, boolean auto) {
        if (c.getProposalId() == null || auto) {
            return;
        }
        AnalystProposalRepository repo = proposals.getIfAvailable();
        if (repo == null) {
            return;
        }
        try {
            repo.findById(c.getProposalId()).filter(p -> "pending".equals(p.getStatus())).ifPresent(p -> {
                p.setStatus("executed");
                p.setExecutedRef(ref(c.getId()));
                p.setDecidedBy(caller.actor());
                p.setDecidedAt(clock.instant());
                repo.save(p);
            });
        } catch (RuntimeException e) {
            log.warn("Could not mark proposal {} executed: {}", c.getProposalId(), e.toString());
        }
    }

    // ------------------------------------------------------------------------- estimate

    /**
     * The projected storage of the chronicle's current layers ({@code ChronicleEstimate}).
     *
     * @param c the chronicle
     * @return the estimate
     */
    public Map<String, Object> estimate(ChronicleEntity c) {
        List<Map<String, Object>> rows = new ArrayList<>();
        double totalRows = 0;
        double totalBytes = 0;
        for (ChronicleLayerEntity l : liveLayers(c.getId())) {
            CaptureMode mode = CaptureMode.fromMap(l.getCapture());
            ChronicleEstimator.Projection p = estimator.project(c, ChronicleSelector.fromMap(l.getSelector(), json),
                    mode);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("layerId", l.getId());
            row.put("name", l.getName());
            row.put("capture", mode.toMap());
            row.put("rowsPerDay", p.toMap().get("rowsPerDay"));
            row.put("bytesPerDay", p.toMap().get("bytesPerDay"));
            row.put("basis", p.basis());
            rows.add(row);
            if (l.isEnabled()) {
                totalRows += p.rowsPerDay();
                totalBytes += p.bytesPerDay();
            }
        }
        long used = Optional.ofNullable(storage.usage(c.getId())).map(u -> u[0]).orElse(0L);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("chronicleId", c.getId());
        out.put("computedAt", clock.instant().toString());
        out.put("layers", rows);
        out.put("totalRowsPerDay", Math.round(totalRows * 10.0) / 10.0);
        out.put("totalBytesPerDay", Math.round(totalBytes * 10.0) / 10.0);
        out.put("sizeCapBytes", c.getSizeCapBytes());
        out.put("bytesUsed", used);
        out.put("daysToCap", totalBytes <= 0 ? null : Math.max(0, (c.getSizeCapBytes() - used) / totalBytes));
        List<Map<String, Object>> caveats = new ArrayList<>();
        caveats.add(Map.of("code", "UPPER_BOUND", "text", "Projected from the last 24 hours of global history before"
                + " area and payload conditions; the chronicle will usually capture less."));
        out.put("caveats", caveats);
        return out;
    }

    // ------------------------------------------------------------------------- views

    /**
     * The {@code Chronicle} view.
     *
     * @param c the chronicle
     * @param caller the caller (its unread count and tags)
     * @param withLayers false for listings (layers are still included, without their estimates)
     * @return the view
     */
    public Map<String, Object> view(ChronicleEntity c, ObjectCaller caller, boolean withLayers) {
        ChronicleEntity fresh = chronicles.findById(c.getId()).orElse(c);
        ShareableObjectEntity rec = access.record(ref(fresh.getId())).orElse(null);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", fresh.getId());
        out.put("ref", ref(fresh.getId()));
        out.put("displayId", fresh.getDisplayId());
        out.put("name", fresh.getName());
        out.put("description", fresh.getDescription());
        if (rec != null) {
            out.put("owner", ownerSummary(rec.getOwner()));
            out.put("visibility", rec.getVisibility().wireValue());
            out.put("tlp", rec.getTlp());
        }
        out.put("status", fresh.getStatus());
        out.put("area", fresh.getArea());
        out.put("startedAt", str(fresh.getStartedAt()));
        out.put("stoppedAt", str(fresh.getStoppedAt()));
        out.put("captureFromSeq", fresh.getCaptureFromSeq());
        out.put("captureToSeq", fresh.getCaptureToSeq());
        out.put("retention", fresh.getRetention());
        out.put("sizeCapBytes", fresh.getSizeCapBytes());
        out.put("bytesUsed", fresh.getBytesUsed());
        out.put("capState", fresh.getCapState());
        out.put("workspaceId", fresh.getWorkspaceId());
        out.put("situationIds", fresh.getSituationIds());
        out.put("hypothesisSetIds", fresh.getHypothesisSetIds());
        out.put("investigationIds", fresh.getInvestigationIds());
        out.put("topicIds", fresh.getTopicIds());
        out.put("origin", fresh.getOrigin());
        out.put("proposalId", fresh.getProposalId());
        boolean exceeded = ChronicleVocabulary.CAP_EXCEEDED.equals(fresh.getCapState());
        List<Map<String, Object>> layerViews = new ArrayList<>();
        for (ChronicleLayerEntity l : liveLayers(fresh.getId())) {
            layerViews.add(layerView(l, exceeded, withLayers));
        }
        out.put("layers", layerViews);
        out.put("archive", fresh.getArchive());
        out.put("discoverability", "none");
        out.put("revealTitle", false);
        out.put("revealOwner", false);
        out.put("unreadCount", thread.unread(fresh.getId(), caller));
        out.put("tags", tags(fresh, caller));
        out.put("version", fresh.getVersion());
        out.put("createdAt", fresh.getCreatedAt().toString());
        out.put("updatedAt", fresh.getUpdatedAt().toString());
        return out;
    }

    private Object ownerSummary(String principalId) {
        try {
            return ownerViews().summary(principalId, ai.foresignal.collab.PrincipalViews.cache());
        } catch (RuntimeException e) {
            return Map.of("id", principalId);
        }
    }

    private ai.foresignal.collab.PrincipalViews ownerViews() {
        return principalViews;
    }

    private ai.foresignal.collab.PrincipalViews principalViews;

    /** Injects the principal summaries (setter, to keep the constructor stable). */
    @org.springframework.beans.factory.annotation.Autowired
    void setPrincipalViews(ai.foresignal.collab.PrincipalViews views) {
        this.principalViews = views;
    }

    private ai.foresignal.collab.TagService tagService;

    /** Injects the tag service when present (tags in views). */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    void setTagService(ai.foresignal.collab.TagService tagService) {
        this.tagService = tagService;
    }

    private List<String> tags(ChronicleEntity c, ObjectCaller caller) {
        if (tagService == null || caller == null) {
            return List.of();
        }
        try {
            return tagService.namesOn(ref(c.getId()), caller);
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    /**
     * The {@code ChronicleLayer} view.
     *
     * @param l the layer
     * @param capExceeded whether the chronicle is over its cap (the effective capture then degrades)
     * @param withEstimate whether to include the estimate
     * @return the view
     */
    public static Map<String, Object> layerView(ChronicleLayerEntity l, boolean capExceeded, boolean withEstimate) {
        CaptureMode mode = CaptureMode.fromMap(l.getCapture());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", l.getId());
        out.put("chronicleId", l.getChronicleId());
        out.put("name", l.getName());
        out.put("selector", l.getSelector());
        out.put("areaOverride", l.getAreaOverride());
        out.put("capture", mode.toMap());
        out.put("effectiveCapture", mode.effective(capExceeded).toMap());
        out.put("enabled", l.isEnabled());
        out.put("addedAt", l.getAddedAt().toString());
        out.put("addedAtSeq", l.getAddedAtSeq());
        out.put("removedAt", str(l.getRemovedAt()));
        out.put("estimate", withEstimate ? l.getEstimate() : null);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("rows", l.getRowsCaptured());
        stats.put("bytes", l.getBytesCaptured());
        out.put("stats", stats);
        return out;
    }

    // ------------------------------------------------------------------------- helpers

    private String nextDisplayId(Instant now) {
        int year = now.atZone(ZoneOffset.UTC).getYear();
        String prefix = "CHR-" + year + "-";
        jdbc.queryForObject("SELECT count(*) FROM (SELECT pg_advisory_xact_lock(?)) AS held", Long.class,
                DISPLAY_ID_LOCK);
        Integer max = jdbc.queryForObject("SELECT max(CAST(substring(display_id FROM ?) AS integer))"
                + " FROM chronicle WHERE display_id LIKE ? AND substring(display_id FROM ?) ~ '^[0-9]+$'",
                Integer.class, prefix.length() + 1, prefix + "%", prefix.length() + 1);
        int next = max == null ? 1 : max + 1;
        return prefix + String.format("%04d", next);
    }

    /** {@code forever} or an ISO-8601 duration of at least one hour. */
    static String retention(String value) {
        if (value == null || "forever".equals(value)) {
            return "forever";
        }
        try {
            Duration d = Duration.parse(value);
            if (d.compareTo(Duration.ofHours(1)) < 0) {
                throw ValidationException.forField("retention", "must be forever or a duration of at least PT1H");
            }
            return d.toString();
        } catch (DateTimeParseException e) {
            throw ValidationException.forField("retention", "must be forever or an ISO-8601 duration such as P365D");
        }
    }

    private long sizeCap(JsonNode node) {
        if (node == null || node.isNull()) {
            return defaultCap();
        }
        if (!node.isIntegralNumber() || node.asLong() < 1) {
            throw ValidationException.forField("sizeCapBytes", "must be a positive integer");
        }
        return node.asLong();
    }

    /** The default size cap ({@code chronicle.defaultCapGb}, 20 GB). */
    public long defaultCap() {
        double gb = 20;
        try {
            gb = settings.get(SettingKeys.CHRONICLE_DEFAULT_CAP_GB, Double.class).filter(v -> v > 0).orElse(20.0);
        } catch (RuntimeException e) {
            gb = 20;
        }
        return (long) (gb * ChronicleSizes.GB);
    }

    private static List<String> ulids(List<String> ids, String field) {
        for (String id : ids) {
            if (!Ulids.isUlid(id)) {
                throw ValidationException.forField(field, "'" + id + "' is not a ULID");
            }
        }
        return new ArrayList<>(new LinkedHashSet<>(ids));
    }

    private static String str(Instant t) {
        return t == null ? null : t.toString();
    }

    /** {@code 404 UNKNOWN_CHRONICLE}. */
    static NotFoundException unknown(String id) {
        return new NotFoundException(ErrorCodes.UNKNOWN_CHRONICLE, "No chronicle '" + id + "' exists, or you may not"
                + " see it.");
    }

    /** {@code 409 CHRONICLE_STATE_INVALID}. */
    static ConflictException stateInvalid(ChronicleEntity c, String action, String needed) {
        return new ConflictException(ErrorCodes.CHRONICLE_STATE_INVALID, "Cannot " + action + " a chronicle that is "
                + c.getStatus() + "; it must be " + needed + ".");
    }
}
