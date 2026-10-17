package ai.foresignal.collab;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.object.AnnotationEntity;
import ai.foresignal.object.AnnotationRepository;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectService;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.Tlp;
import ai.foresignal.security.Ulids;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * Tags (prompt 130): personal tags, visible only to their owner, and shared tags, which follow their access list
 * (default {@code instance}). Any principal who sees a tag assigns it to up to 500 object references at once;
 * references the caller cannot see are skipped, not errors. Envelopes carry the names of the caller's visible tags
 * ({@link CollabEnvelopeDecorator}). On start, any 129 annotation of the unspecified kind {@code tag} becomes a
 * personal tag assignment of its author.
 */
@Service
public class TagService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TagService.class);

    /** Most references per bulk assignment. */
    public static final int MAX_REFS = 500;

    private static final Set<String> INPUT = Set.of("name", "namespace", "color", "description", "visibility", "tlp");
    private static final Set<String> PATCH = Set.of("name", "color", "description", "visibility", "tlp");

    private final TagRepository tags;
    private final TagAssignmentRepository assignments;
    private final AccessResolver access;
    private final ShareService shares;
    private final ObjectService objects;
    private final AnnotationRepository annotations;
    private final PrincipalViews views;
    private final Clock clock;

    /** Creates the service. */
    public TagService(TagRepository tags, TagAssignmentRepository assignments, AccessResolver access,
            ShareService shares, ObjectService objects, AnnotationRepository annotations, PrincipalViews views,
            Clock clock) {
        this.tags = tags;
        this.assignments = assignments;
        this.access = access;
        this.shares = shares;
        this.objects = objects;
        this.annotations = annotations;
        this.views = views;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- visibility

    /** The reference of a tag. */
    public static String ref(String tagId) {
        return ObjectTypes.TAG + ":" + tagId;
    }

    /** True when the caller sees {@code tag}: a personal tag only by its owner, a shared one by its access list. */
    public boolean visible(TagEntity tag, ObjectCaller caller) {
        if (caller == null || caller.anonymous()) {
            return false;
        }
        Optional<ShareableObjectEntity> rec = access.record(ref(tag.getId()));
        if (tag.isPersonal()) {
            return rec.map(r -> r.getOwner().equals(caller.principal())).orElse(false);
        }
        return access.role(caller, ref(tag.getId())) != null;
    }

    /**
     * The tag {@code id} when the caller sees it.
     *
     * @throws NotFoundException {@code UNKNOWN_TAG} otherwise
     */
    @Transactional
    public TagEntity get(String id, ObjectCaller caller) {
        return tags.findById(id == null ? "" : id).filter(t -> visible(t, caller))
                .orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_TAG, "No tag '" + id
                        + "' exists, or you may not see it."));
    }

    /** The {@code Tag} view. */
    @Transactional
    public Map<String, Object> view(TagEntity t, ObjectCaller caller, Map<String, Map<String, Object>> cache) {
        ShareableObjectEntity rec = access.record(ref(t.getId())).orElse(null);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", t.getId());
        out.put("ref", ref(t.getId()));
        out.put("name", t.getName());
        out.put("namespace", t.getNamespace());
        out.put("color", t.getColor());
        out.put("description", t.getDescription());
        out.put("owner", views.summary(rec == null ? t.getCreatedBy() : rec.getOwner(), cache));
        out.put("visibility", rec == null ? Visibility.PRIVATE.wireValue() : rec.getVisibility().wireValue());
        out.put("tlp", rec == null ? access.defaultTlp().wireValue() : rec.getTlp());
        out.put("objectCount", visibleCount(t, caller));
        out.put("createdAt", t.getCreatedAt());
        out.put("updatedAt", t.getUpdatedAt());
        return out;
    }

    private int visibleCount(TagEntity t, ObjectCaller caller) {
        List<String> refs = assignments.findByTagId(t.getId()).stream().map(TagAssignmentEntity::getObjectRef)
                .toList();
        Map<String, AclRole> roles = access.roles(caller, refs);
        int n = 0;
        for (String r : refs) {
            if (!roles.containsKey(r) || roles.get(r) != null) {
                n++;
            }
        }
        return n;
    }

    // ------------------------------------------------------------------------- CRUD

    /**
     * Tags the caller sees, by id ({@code listTags}).
     *
     * @return the {@code TagPage}
     */
    @Transactional
    public Map<String, Object> list(ObjectCaller caller, String namespace, String q, String cursor, int limit) {
        if (namespace != null && !TagEntity.PERSONAL.equals(namespace) && !TagEntity.SHARED.equals(namespace)) {
            throw ValidationException.forField("namespace", "must be personal or shared");
        }
        String needle = q == null || q.isBlank() ? null : q.strip().toLowerCase(Locale.ROOT);
        int offset = CollabInputs.offset(cursor);
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Map<String, Object>> cache = PrincipalViews.cache();
        int seen = 0;
        boolean more = false;
        for (TagEntity t : tags.findAllByOrderByIdAsc()) {
            if ((namespace != null && !namespace.equals(t.getNamespace()))
                    || (needle != null && !t.getName().toLowerCase(Locale.ROOT).contains(needle))
                    || !visible(t, caller)) {
                continue;
            }
            if (seen++ < offset) {
                continue;
            }
            if (items.size() == limit) {
                more = true;
                break;
            }
            items.add(view(t, caller, cache));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", more ? CollabInputs.cursor(offset + limit) : null);
        return out;
    }

    /**
     * Creates a tag owned by the caller ({@code createTag}).
     *
     * @throws ConflictException {@code TAG_NAME_TAKEN} for a duplicate name in the namespace
     */
    @Transactional
    public TagEntity create(JsonNode body, ObjectCaller caller) {
        CollabInputs.object(body, "TagInput", INPUT);
        String name = CollabInputs.required(body, "name", 1, 64).strip();
        String namespace = CollabInputs.text(body, "namespace", 1, 16);
        namespace = namespace == null ? TagEntity.PERSONAL : namespace;
        if (!TagEntity.PERSONAL.equals(namespace) && !TagEntity.SHARED.equals(namespace)) {
            throw ValidationException.forField("namespace", "must be personal or shared");
        }
        String color = color(body);
        Tlp tlp = Tlp.parse("tlp", CollabInputs.text(body, "tlp", 1, 16), access.defaultTlp());
        Visibility visibility = TagEntity.PERSONAL.equals(namespace) ? Visibility.PRIVATE
                : Visibility.parse("visibility", CollabInputs.text(body, "visibility", 1, 16), Visibility.INSTANCE);
        requireUniqueName(namespace, name, caller.principal(), null);
        if (!TagEntity.PERSONAL.equals(namespace)) {
            shares.checkTlp(tlp, caller.principal(), List.of(), visibility);
        }
        Instant now = clock.instant();
        TagEntity t = new TagEntity(Ulids.next(now), name, namespace, caller.principal(), now);
        t.setColor(color);
        t.setDescription(CollabInputs.text(body, "description", 0, 2000));
        tags.save(t);
        access.register(ref(t.getId()), caller.principal(), visibility, tlp);
        return t;
    }

    /** Applies a JSON merge patch ({@code updateTag}; owner, or an editor of a shared tag). */
    @Transactional
    public TagEntity patch(String id, JsonNode body, ObjectCaller caller) {
        TagEntity t = get(id, caller);
        ShareableObjectEntity rec = requireEditor(t, caller);
        CollabInputs.object(body, "TagPatch", PATCH);
        if (body.has("name")) {
            String name = CollabInputs.required(body, "name", 1, 64).strip();
            requireUniqueName(t.getNamespace(), name, rec.getOwner(), t.getId());
            t.setName(name);
        }
        if (body.has("color")) {
            t.setColor(color(body));
        }
        if (body.has("description")) {
            t.setDescription(CollabInputs.text(body, "description", 0, 2000));
        }
        Tlp tlp = body.hasNonNull("tlp") ? Tlp.parse("tlp", CollabInputs.text(body, "tlp", 1, 16), null)
                : Tlp.fromWire(rec.getTlp());
        Visibility visibility = rec.getVisibility();
        if (body.hasNonNull("visibility")) {
            visibility = Visibility.parse("visibility", CollabInputs.text(body, "visibility", 1, 16), null);
            if (t.isPersonal() && visibility != Visibility.PRIVATE) {
                throw ValidationException.forField("visibility", "a personal tag is private");
            }
        }
        if (!t.isPersonal()) {
            shares.checkTlp(tlp, rec.getOwner(), acls(rec), visibility);
        }
        rec.setTlp(tlp.wireValue());
        rec.setVisibility(visibility);
        access.save(rec);
        t.setUpdatedAt(clock.instant());
        return tags.save(t);
    }

    /** Deletes a tag and its assignments ({@code deleteTag}; owner only). */
    @Transactional
    public void delete(String id, ObjectCaller caller) {
        TagEntity t = get(id, caller);
        ShareableObjectEntity rec = access.record(ref(t.getId())).orElseThrow();
        if (!caller.admin() && !rec.getOwner().equals(caller.principal())) {
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN,
                    "Only the tag's owner may delete it.");
        }
        assignments.deleteByTagId(t.getId());
        access.forget(ref(t.getId()));
        tags.delete(t);
    }

    // ------------------------------------------------------------------------- assignments

    /**
     * Assigns or removes a tag on up to 500 references ({@code assignTag}, {@code unassignTag}).
     *
     * @param id the tag
     * @param refs the references
     * @param assign true to assign, false to remove
     * @param caller who asks (must see the tag)
     * @return the {@code TagAssignResult}
     */
    @Transactional
    public Map<String, Object> bulk(String id, List<String> refs, boolean assign, ObjectCaller caller) {
        TagEntity t = get(id, caller);
        if (refs == null || refs.isEmpty()) {
            throw ValidationException.forField("refs", "must hold at least one reference");
        }
        if (refs.size() > MAX_REFS) {
            throw ValidationException.forField("refs", "holds more than " + MAX_REFS + " references");
        }
        List<Map<String, Object>> skipped = new ArrayList<>();
        Map<String, ObjectRef> parsed = new LinkedHashMap<>();
        for (String r : new LinkedHashSet<>(refs)) {
            if (r == null || !ObjectRef.isValid(r)) {
                skipped.add(Map.of("ref", String.valueOf(r), "reason", "invalid-ref"));
            } else {
                parsed.put(r, ObjectRef.parse(r));
            }
        }
        Map<ObjectRef, ObjectEnvelope> visible = objects.resolveRefs(parsed.values(), new ResolveContext(caller,
                null));
        int changed = 0;
        int unchanged = 0;
        Instant now = clock.instant();
        for (Map.Entry<String, ObjectRef> e : parsed.entrySet()) {
            ObjectEnvelope env = visible.get(e.getValue());
            if (env == null) {
                skipped.add(Map.of("ref", e.getKey(), "reason", "not-visible"));
                continue;
            }
            String canonical = env.objectRef().withoutSeq().format();
            Optional<TagAssignmentEntity> existing = assignments.findByTagIdAndObjectRef(t.getId(), canonical);
            if (assign) {
                if (existing.isPresent()) {
                    unchanged++;
                } else {
                    assignments.save(new TagAssignmentEntity(Ulids.next(now), t.getId(), canonical,
                            caller.principal(), now));
                    changed++;
                }
            } else if (existing.isPresent()) {
                assignments.delete(existing.get());
                changed++;
            } else {
                unchanged++;
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("tagId", t.getId());
        out.put("changed", changed);
        out.put("unchanged", unchanged);
        out.put("skipped", skipped);
        return out;
    }

    /**
     * The objects carrying a tag that the caller can see, by assignment ({@code listTaggedObjects}).
     *
     * @return the {@code ObjectEnvelopePage}
     */
    @Transactional
    public Map<String, Object> objects(String id, String type, String cursor, int limit, ObjectCaller caller) {
        TagEntity t = get(id, caller);
        if (type != null && !ObjectTypes.isType(type)) {
            throw ValidationException.forField("type", "is not an object type");
        }
        List<ObjectRef> refs = new ArrayList<>();
        for (TagAssignmentEntity a : assignments.findByTagId(t.getId())) {
            if ((type == null || a.getObjectRef().startsWith(type + ":")) && ObjectRef.isValid(a.getObjectRef())) {
                refs.add(ObjectRef.parse(a.getObjectRef()));
            }
        }
        refs.sort(java.util.Comparator.comparing(ObjectRef::format));
        Map<ObjectRef, ObjectEnvelope> envelopes = objects.resolveRefs(refs, new ResolveContext(caller, null));
        List<ObjectEnvelope> visible = new ArrayList<>();
        for (ObjectRef r : refs) {
            ObjectEnvelope e = envelopes.get(r);
            if (e != null) {
                visible.add(e);
            }
        }
        int offset = CollabInputs.offset(cursor);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", visible.subList(Math.min(offset, visible.size()), Math.min(offset + limit, visible.size())));
        out.put("nextCursor", offset + limit < visible.size() ? CollabInputs.cursor(offset + limit) : null);
        return out;
    }

    /** The caller's visible tags on an object ({@code listObjectTags}; the object must be visible). */
    @Transactional
    public List<Map<String, Object>> objectTags(ObjectRef ref, ObjectCaller caller) {
        ObjectEnvelope env = objects.get(ref, new ResolveContext(caller, null));
        String canonical = env.objectRef().withoutSeq().format();
        Map<String, Map<String, Object>> cache = PrincipalViews.cache();
        List<Map<String, Object>> out = new ArrayList<>();
        for (TagAssignmentEntity a : assignments.findByObjectRefOrderByAssignedAtAsc(canonical)) {
            TagEntity t = tags.findById(a.getTagId()).orElse(null);
            if (t == null || !visible(t, caller)) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("tagId", t.getId());
            row.put("tag", view(t, caller, cache));
            row.put("objectRef", canonical);
            row.put("assignedBy", views.summary(a.getAssignedBy(), cache));
            row.put("assignedAt", a.getAssignedAt());
            out.add(row);
        }
        return out;
    }

    /**
     * The names of the caller's visible tags on each of {@code refs} (envelopes).
     *
     * @return canonical reference → tag names; references without visible tags are left out
     */
    @Transactional
    public Map<String, List<String>> visibleNames(Collection<String> refs, ObjectCaller caller) {
        Map<String, List<String>> out = new HashMap<>();
        if (refs.isEmpty() || caller == null || caller.anonymous()) {
            return out;
        }
        List<TagAssignmentEntity> rows = assignments.findByObjectRefIn(refs);
        if (rows.isEmpty()) {
            return out;
        }
        Map<String, TagEntity> byId = new HashMap<>();
        for (TagEntity t : tags.findAllById(rows.stream().map(TagAssignmentEntity::getTagId).distinct().toList())) {
            byId.put(t.getId(), t);
        }
        Map<String, Boolean> seen = new HashMap<>();
        for (TagAssignmentEntity a : rows) {
            TagEntity t = byId.get(a.getTagId());
            if (t == null || !seen.computeIfAbsent(t.getId(), k -> visible(t, caller))) {
                continue;
            }
            out.computeIfAbsent(a.getObjectRef(), k -> new ArrayList<>()).add(t.getName());
        }
        return out;
    }

    /** The names of the caller's visible tags on {@code ref} (evidence item views). */
    @Transactional
    public List<String> namesOn(String ref, ObjectCaller caller) {
        return visibleNames(List.of(ref), caller).getOrDefault(ref, List.of());
    }

    // ------------------------------------------------------------------------- 129 migration

    /** Migrates any 129 {@code tag} annotations to tag assignments (none exist unless written by hand). */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        migrateTagAnnotations();
    }

    /**
     * Turns every live annotation of kind {@code tag} into personal tag assignments of its author (one per tag name
     * in its {@code tags}, else its body) and soft-deletes it.
     *
     * @return how many annotations were migrated
     */
    @Transactional
    public int migrateTagAnnotations() {
        List<AnnotationEntity> rows = annotations.findByKindAndDeletedAtIsNull("tag");
        Instant now = clock.instant();
        for (AnnotationEntity a : rows) {
            List<String> names = new ArrayList<>(a.getTags());
            if (names.isEmpty() && a.getBody() != null && !a.getBody().isBlank()) {
                names.add(a.getBody().strip());
            }
            for (String raw : names) {
                String name = raw.length() > 64 ? raw.substring(0, 64) : raw;
                TagEntity tag = tags.findByNamespaceAndNameIgnoreCase(TagEntity.PERSONAL, name).stream()
                        .filter(t -> access.record(ref(t.getId())).map(r -> r.getOwner().equals(a.getAuthor()))
                                .orElse(false))
                        .findFirst().orElseGet(() -> {
                            TagEntity t = tags.save(new TagEntity(Ulids.next(now), name, TagEntity.PERSONAL,
                                    a.getAuthor(), now));
                            access.register(ref(t.getId()), a.getAuthor(), Visibility.PRIVATE, null);
                            return t;
                        });
                if (assignments.findByTagIdAndObjectRef(tag.getId(), a.getTargetRef()).isEmpty()) {
                    assignments.save(new TagAssignmentEntity(Ulids.next(now), tag.getId(), a.getTargetRef(),
                            a.getAuthor(), now));
                }
            }
            a.setDeletedAt(now);
            a.setUpdatedAt(now);
            annotations.save(a);
        }
        if (!rows.isEmpty()) {
            log.info("Migrated {} tag annotations to tag assignments", rows.size());
        }
        return rows.size();
    }

    // ------------------------------------------------------------------------- helpers

    private ShareableObjectEntity requireEditor(TagEntity t, ObjectCaller caller) {
        ShareableObjectEntity rec = access.record(ref(t.getId())).orElseThrow();
        boolean owner = rec.getOwner().equals(caller.principal());
        if (t.isPersonal() ? !owner : !access.can(caller, ref(t.getId()), AccessAction.EDIT)) {
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN,
                    "Only the tag's owner or an editor may change it.");
        }
        return rec;
    }

    private List<String> acls(ShareableObjectEntity rec) {
        List<String> out = new ArrayList<>();
        shares.entries(rec.getObjectRef(), PrincipalViews.cache()).forEach(e -> out.add(String.valueOf(
                e.get("grantee"))));
        return out;
    }

    private void requireUniqueName(String namespace, String name, String owner, String exceptId) {
        for (TagEntity other : tags.findByNamespaceAndNameIgnoreCase(namespace, name)) {
            if (other.getId().equals(exceptId)) {
                continue;
            }
            boolean clash = TagEntity.SHARED.equals(namespace) || access.record(ref(other.getId()))
                    .map(r -> r.getOwner().equals(owner)).orElse(other.getCreatedBy().equals(owner));
            if (clash) {
                throw new ConflictException(ErrorCodes.TAG_NAME_TAKEN, "A tag named '" + name
                        + "' already exists in the " + namespace + " namespace.");
            }
        }
    }

    private static String color(JsonNode body) {
        String color = CollabInputs.text(body, "color", 7, 7);
        if (color != null && !CollabInputs.COLOR.matcher(color).matches()) {
            throw ValidationException.forField("color", "must be #rrggbb");
        }
        return color;
    }
}
