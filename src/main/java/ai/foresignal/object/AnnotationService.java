package ai.foresignal.object;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.AnalyticFindingRepository;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.intel.IntelEventService;
import ai.foresignal.intel.IntelligenceEventEntity;
import ai.foresignal.intel.IntelligenceEventRepository;
import ai.foresignal.security.Ulids;
import ai.foresignal.situation.SituationRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * Annotations and human validation of any object (prompt 129): notes by any token, verdicts by agent scope.
 *
 * <ul>
 *   <li>A verdict records the target's producer (a finding's detector, an intel event's recipe, the situation linker)
 *       for {@link PrecisionService}.</li>
 *   <li>A verdict on an object with a time window proposes a historical benchmark case
 *       ({@link BenchmarkCaseProposals}); nothing is added to the corpus automatically.</li>
 *   <li>A {@code rejected} verdict lowers a finding or an intel event to {@code dismissed} only when its agent-scope
 *       author says so explicitly ({@code applyStatus: true}).</li>
 * </ul>
 * Annotations of a private target are private; a private annotation is invisible to callers without private access.
 * Edits and deletes are for the author or an admin; changing a verdict still needs agent scope.
 */
@Service
public class AnnotationService {

    /** Annotation kinds (there is no {@code tag} kind: labels use 130 tags). */
    public static final List<String> KINDS = List.of("note", "verdict");

    /** Verdict values. */
    public static final List<String> VERDICTS = List.of("confirmed", "rejected", "needs-review", "duplicate-of");

    /** Longest body, in characters. */
    public static final int MAX_BODY = 10_000;

    /** Most tags on one annotation. */
    public static final int MAX_TAGS = 20;

    private static final Set<String> INPUT_FIELDS = Set.of("kind", "verdict", "duplicateOfRef", "body", "tags",
            "private", "applyStatus");
    private static final Set<String> PATCH_FIELDS = Set.of("verdict", "duplicateOfRef", "body", "tags", "private");

    private final AnnotationRepository annotations;
    private final ObjectService objects;
    private final BenchmarkCaseProposals proposals;
    private final AnalyticFindingRepository findings;
    private final IntelligenceEventRepository events;
    private final SituationRepository situations;
    private final FindingService findingService;
    private final IntelEventService eventService;
    private final Clock clock;

    /** Creates the service. */
    public AnnotationService(AnnotationRepository annotations, ObjectService objects,
            BenchmarkCaseProposals proposals, AnalyticFindingRepository findings, IntelligenceEventRepository events,
            SituationRepository situations, FindingService findingService, IntelEventService eventService,
            Clock clock) {
        this.annotations = annotations;
        this.objects = objects;
        this.proposals = proposals;
        this.findings = findings;
        this.events = events;
        this.situations = situations;
        this.findingService = findingService;
        this.eventService = eventService;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- views

    /** The {@code Annotation} view. */
    public static Map<String, Object> view(AnnotationEntity a) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", a.getId());
        out.put("ref", ObjectTypes.ANNOTATION + ":" + a.getId());
        out.put("targetRef", a.getTargetRef());
        out.put("kind", a.getKind());
        out.put("verdict", a.getVerdict());
        out.put("duplicateOfRef", a.getDuplicateOfRef());
        out.put("body", a.getBody());
        out.put("tags", a.getTags());
        out.put("author", a.getAuthor());
        out.put("private", a.isPrivateAnnotation());
        out.put("statusApplied", a.isStatusApplied());
        out.put("createdAt", a.getCreatedAt());
        out.put("updatedAt", a.getUpdatedAt());
        out.put("deletedAt", a.getDeletedAt());
        return out;
    }

    // ------------------------------------------------------------------------- reads

    /**
     * A page of annotations of {@code target}, newest first.
     *
     * @param target the annotated object (must be visible to the caller)
     * @param cursor the last id of the previous page, or null
     * @param limit the page size
     * @param context the caller
     * @return the {@code AnnotationPage} view
     */
    public Map<String, Object> list(ObjectRef target, String cursor, int limit, ResolveContext context) {
        ObjectEnvelope envelope = objects.get(target, context);
        String key = envelope.objectRef().withoutSeq().format();
        List<AnnotationEntity> rows = new ArrayList<>();
        String after = cursor;
        // Private rows are filtered after the read, so read until a page is full or the target runs out.
        while (rows.size() <= limit) {
            List<AnnotationEntity> page = after == null
                    ? annotations.findByTargetRefAndDeletedAtIsNullOrderByIdDesc(key, Limit.of(limit + 1))
                    : annotations.findByTargetRefAndDeletedAtIsNullAndIdLessThanOrderByIdDesc(key, after,
                            Limit.of(limit + 1));
            for (AnnotationEntity a : page) {
                if (!a.isPrivateAnnotation() || context.seesPrivate()) {
                    rows.add(a);
                }
            }
            if (page.size() < limit + 1) {
                break;
            }
            after = page.get(page.size() - 1).getId();
        }
        boolean more = rows.size() > limit;
        List<AnnotationEntity> items = more ? rows.subList(0, limit) : rows;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items.stream().map(AnnotationService::view).toList());
        out.put("nextCursor", more ? items.get(items.size() - 1).getId() : null);
        return out;
    }

    /** The annotation {@code id} visible to the caller ({@code 404 UNKNOWN_ANNOTATION} otherwise). */
    public AnnotationEntity get(String id, ObjectCaller caller) {
        return annotations.findById(id).filter(a -> a.getDeletedAt() == null)
                .filter(a -> !a.isPrivateAnnotation() || caller.seesPrivate())
                .orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_ANNOTATION,
                        "No annotation '" + id + "' exists, or you may not see it."));
    }

    /** The annotation {@code id} if it exists, visible or not. */
    public Optional<AnnotationEntity> find(String id) {
        return annotations.findById(id);
    }

    /** Live verdicts created at or after {@code since}, oldest first (precision, the 128 cycle packet). */
    public List<AnnotationEntity> verdictsSince(Instant since) {
        return annotations.findByKindAndDeletedAtIsNullAndCreatedAtGreaterThanEqualOrderByCreatedAtAscIdAsc("verdict",
                since == null ? Instant.EPOCH : since);
    }

    // ------------------------------------------------------------------------- writes

    /**
     * Annotates {@code target} ({@code createAnnotation}).
     *
     * @param target the object
     * @param body the {@code AnnotationInput}
     * @param context the caller (a token)
     * @return the new annotation
     * @throws ForesignalException 403 when a verdict comes from a token without agent scope
     */
    @Transactional
    public AnnotationEntity create(ObjectRef target, JsonNode body, ResolveContext context) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The body must be an AnnotationInput object.");
        }
        for (String field : body.propertyNames()) {
            if (!INPUT_FIELDS.contains(field)) {
                throw ValidationException.forField(field, "is not an AnnotationInput member");
            }
        }
        String kind = text(body, "kind");
        if ("tag".equals(kind)) {
            throw ValidationException.forField("kind", "there is no tag kind: label objects with tags (prompt 130),"
                    + " or add tags to a note");
        }
        if (kind == null || !KINDS.contains(kind)) {
            throw ValidationException.forField("kind", "must be note or verdict");
        }
        ObjectCaller caller = context.caller();
        boolean verdict = "verdict".equals(kind);
        if (verdict && !caller.agent()) {
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN, "Verdicts need agent scope.");
        }
        ObjectEnvelope envelope = objects.get(target, new ResolveContext(caller, null));
        if (ObjectTypes.ANNOTATION.equals(envelope.type()) || ObjectTypes.WORKSPACE.equals(envelope.type())) {
            throw ValidationException.forField("ref", "annotations and workspaces are not annotated");
        }
        Instant now = clock.instant();
        AnnotationEntity a = new AnnotationEntity(Ulids.next(now), envelope.objectRef().withoutSeq().format(), kind,
                caller.principal(), now);
        a.setBody(bodyText(body.get("body")));
        a.setTags(tags(body.get("tags")));
        a.setPrivateAnnotation(bool(body, "private") || envelope.privateObject());
        boolean applyStatus = bool(body, "applyStatus");
        if (verdict) {
            String value = text(body, "verdict");
            if (value == null || !VERDICTS.contains(value)) {
                throw ValidationException.forField("verdict", "must be one of " + VERDICTS);
            }
            a.setVerdict(value);
            a.setDuplicateOfRef(duplicateOf(value, text(body, "duplicateOfRef"), caller));
            producer(a, envelope.objectRef());
        } else if (body.hasNonNull("verdict") || body.hasNonNull("duplicateOfRef")) {
            throw ValidationException.forField("verdict", "only a verdict carries verdict or duplicateOfRef");
        }
        if (a.getBody() == null && a.getTags().isEmpty() && !verdict) {
            throw ValidationException.forField("body", "a note needs a body or tags");
        }
        if (applyStatus) {
            applyStatus(a, envelope, caller);
        }
        annotations.save(a);
        if (verdict) {
            proposals.propose(a, envelope, findingType(envelope.objectRef()), regionCodes(envelope.objectRef()));
        }
        return a;
    }

    /**
     * Applies a JSON merge patch to an annotation ({@code updateAnnotation}).
     *
     * @throws ForesignalException 403 for anyone but the author or an admin, or a verdict change without agent scope
     */
    @Transactional
    public AnnotationEntity patch(String id, JsonNode body, ObjectCaller caller) {
        AnnotationEntity a = get(id, caller);
        authorOrAdmin(a, caller);
        if (body == null || !body.isObject()) {
            throw new ValidationException("The body must be an AnnotationPatch object.");
        }
        for (String field : body.propertyNames()) {
            if (!PATCH_FIELDS.contains(field)) {
                throw ValidationException.forField(field, "is not an AnnotationPatch member");
            }
        }
        if (body.has("verdict") || body.has("duplicateOfRef")) {
            if (!"verdict".equals(a.getKind())) {
                throw ValidationException.forField("verdict", "a note has no verdict");
            }
            if (!caller.agent()) {
                throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN,
                        "Changing a verdict needs agent scope.");
            }
            String value = body.has("verdict") ? text(body, "verdict") : a.getVerdict();
            if (value == null || !VERDICTS.contains(value)) {
                throw ValidationException.forField("verdict", "must be one of " + VERDICTS);
            }
            String dup = body.has("duplicateOfRef") ? text(body, "duplicateOfRef") : a.getDuplicateOfRef();
            a.setVerdict(value);
            a.setDuplicateOfRef(duplicateOf(value, "duplicate-of".equals(value) ? dup : null, caller));
        }
        if (body.has("body")) {
            a.setBody(bodyText(body.get("body")));
        }
        if (body.has("tags")) {
            a.setTags(tags(body.get("tags")));
        }
        if (body.has("private")) {
            boolean requested = body.get("private").asBoolean(false);
            // A private target keeps its annotations private.
            boolean targetPrivate = objects.find(ObjectRef.parse(a.getTargetRef()), new ResolveContext(caller, null))
                    .map(ObjectEnvelope::privateObject).orElse(a.isPrivateAnnotation());
            a.setPrivateAnnotation(requested || targetPrivate);
        }
        a.setUpdatedAt(clock.instant());
        return annotations.save(a);
    }

    /** Soft-deletes an annotation ({@code deleteAnnotation}; author or admin). */
    @Transactional
    public void delete(String id, ObjectCaller caller) {
        AnnotationEntity a = get(id, caller);
        authorOrAdmin(a, caller);
        Instant now = clock.instant();
        a.setDeletedAt(now);
        a.setUpdatedAt(now);
        annotations.save(a);
    }

    // ------------------------------------------------------------------------- helpers

    private static void authorOrAdmin(AnnotationEntity a, ObjectCaller caller) {
        if (!caller.admin() && (caller.principal() == null || !caller.principal().equals(a.getAuthor()))) {
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN,
                    "Only the author or an admin may change this annotation.");
        }
    }

    private void applyStatus(AnnotationEntity a, ObjectEnvelope target, ObjectCaller caller) {
        if (!"rejected".equals(a.getVerdict())) {
            throw ValidationException.forField("applyStatus", "applies only to a rejected verdict");
        }
        String reason = "Rejected by verdict annotation:" + a.getId()
                + (a.getBody() == null ? "" : ": " + clip(a.getBody(), 1500));
        String id = target.objectRef().id();
        switch (target.type()) {
            case ObjectTypes.FINDING -> findingService.dismiss(id, reason, caller.actor(), caller.seesPrivate());
            case ObjectTypes.INTEL_EVENT -> {
                ObjectNode patch = JsonNodeFactory.instance.objectNode();
                patch.put("status", "dismissed");
                eventService.patch(id, patch, caller.actor(), caller.seesPrivate());
            }
            default -> throw ValidationException.forField("applyStatus",
                    "applies only to findings and intel events");
        }
        a.setStatusApplied(true);
    }

    /** Records the target's producer on a verdict, for precision. */
    void producer(AnnotationEntity a, ObjectRef target) {
        switch (target.type()) {
            case ObjectTypes.FINDING -> findings.findById(target.id()).ifPresent(f -> {
                a.setTargetComponent("finding");
                a.setTargetKey(detectorKey(f));
                a.setPrecursorKey(f.getPrecursorId());
            });
            case ObjectTypes.INTEL_EVENT -> events.findById(target.id()).ifPresent(e -> {
                a.setTargetComponent("intel-event");
                a.setTargetKey(recipeKey(e));
            });
            case ObjectTypes.SITUATION -> situations.findById(target.id()).ifPresent(s -> {
                a.setTargetComponent("situation");
                a.setTargetKey("linker " + (s.getLinkerVersion() == null ? "unknown" : s.getLinkerVersion()));
            });
            default -> a.setTargetComponent(target.type());
        }
    }

    /** {@code name version} of a finding's detector. */
    static String detectorKey(AnalyticFindingEntity f) {
        Map<String, Object> d = f.getDetector() == null ? Map.of() : f.getDetector();
        Object name = d.getOrDefault("name", f.getFindingType());
        Object version = d.get("version");
        return version == null ? String.valueOf(name) : name + " " + version;
    }

    /** {@code recipe version} of an intel event (its detector name for events without a recipe). */
    static String recipeKey(IntelligenceEventEntity e) {
        if (e.getRecipeId() != null) {
            return e.getRecipeId() + (e.getRecipeVersion() == null ? "" : " " + e.getRecipeVersion());
        }
        Object name = e.getDetector() == null ? null : e.getDetector().get("name");
        return name == null ? (e.getOrigin() == null ? "manual" : e.getOrigin()) : String.valueOf(name);
    }

    private String findingType(ObjectRef target) {
        if (!ObjectTypes.FINDING.equals(target.type())) {
            return null;
        }
        return findings.findById(target.id()).map(AnalyticFindingEntity::getFindingType).orElse(null);
    }

    private List<String> regionCodes(ObjectRef target) {
        return switch (target.type()) {
            case ObjectTypes.FINDING -> findings.findById(target.id()).map(AnalyticFindingEntity::getRegionCodes)
                    .orElse(List.of());
            case ObjectTypes.INTEL_EVENT -> events.findById(target.id())
                    .map(IntelligenceEventEntity::getRegionTags).orElse(List.of());
            case ObjectTypes.SITUATION -> situations.findById(target.id()).map(s -> s.getRegionCodes())
                    .orElse(List.of());
            default -> List.of();
        };
    }

    private String duplicateOf(String verdict, String ref, ObjectCaller caller) {
        if (!"duplicate-of".equals(verdict)) {
            if (ref != null) {
                throw ValidationException.forField("duplicateOfRef", "only a duplicate-of verdict names an original");
            }
            return null;
        }
        if (ref == null) {
            throw ValidationException.forField("duplicateOfRef", "is required for a duplicate-of verdict");
        }
        ObjectRef original = ObjectRef.parse(ref, "duplicateOfRef");
        return objects.get(original, new ResolveContext(caller, null)).objectRef().withoutSeq().format();
    }

    private static String bodyText(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString()) {
            throw ValidationException.forField("body", "must be a string");
        }
        String s = node.asString();
        if (s.length() > MAX_BODY) {
            throw ValidationException.forField("body", "must be at most " + MAX_BODY + " characters");
        }
        return s.isBlank() ? null : s;
    }

    private static List<String> tags(JsonNode node) {
        List<String> out = new ArrayList<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (!node.isArray()) {
            throw ValidationException.forField("tags", "must be an array of strings");
        }
        for (JsonNode t : node) {
            if (!t.isString() || t.asString().isBlank() || t.asString().length() > 64) {
                throw ValidationException.forField("tags", "each tag is a string of 1 to 64 characters");
            }
            if (!out.contains(t.asString().strip())) {
                out.add(t.asString().strip());
            }
        }
        if (out.size() > MAX_TAGS) {
            throw ValidationException.forField("tags", "at most " + MAX_TAGS + " tags");
        }
        return out;
    }

    private static String text(JsonNode body, String field) {
        JsonNode n = body.get(field);
        return n == null || n.isNull() ? null : n.asString();
    }

    private static boolean bool(JsonNode body, String field) {
        JsonNode n = body.get(field);
        if (n == null || n.isNull()) {
            return false;
        }
        if (!n.isBoolean()) {
            throw ValidationException.forField(field, "must be a boolean");
        }
        return n.asBoolean();
    }

    private static String clip(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
