package ai.foresignal.collab;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectService;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.object.WorkspaceRepository;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalRepository;
import ai.foresignal.security.Tlp;
import ai.foresignal.security.TlpTarget;
import ai.foresignal.security.Ulids;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * Access lists, shares and the "shared with me" and "my items" listings (prompt 130).
 *
 * <ul>
 *   <li>{@code GET /v1/objects/{ref}/acl}: owner, visibility, TLP and grants (owner or editor; admins always).</li>
 *   <li>{@code PUT /v1/objects/{ref}/acl}: replaces the grants, visibility and TLP (owner or editor); only owners
 *       grant {@code owner} or transfer ownership.</li>
 *   <li>{@code POST /v1/objects/{ref}/share}: adds grants and notifies the grantees ({@code share.received}).</li>
 * </ul>
 * Every grant and visibility is checked against the object's TLP label by {@link EgressPolicy#tlp} ({@code 403
 * TLP_VIOLATION} names the rule), and every change is audited (COL-1, COL-2).
 */
@Service
public class ShareService {

    /** Most grants in one access list. */
    public static final int MAX_ENTRIES = 500;

    /** Most grantees in one share. */
    public static final int MAX_GRANTEES = 100;

    private static final Set<String> ACL_FIELDS = Set.of("visibility", "entries", "owner", "tlp");
    private static final Set<String> ENTRY_FIELDS = Set.of("grantee", "role", "expiresAt");
    private static final Set<String> SHARE_FIELDS = Set.of("grantees", "role", "message", "expiresAt");

    private final AccessResolver access;
    private final ObjectAclRepository acls;
    private final ShareableObjectRepository registry;
    private final PrincipalRepository principalRows;
    private final PrincipalAdminService principals;
    private final GroupService groups;
    private final GroupMemberRepository groupMembers;
    private final WorkspaceRepository workspaces;
    private final ObjectService objects;
    private final PrincipalViews views;
    private final PersonalNotifier notifier;
    private final CollabAudit audit;
    private final Clock clock;

    /** Creates the service. */
    public ShareService(AccessResolver access, ObjectAclRepository acls, ShareableObjectRepository registry,
            PrincipalRepository principalRows, PrincipalAdminService principals, GroupService groups,
            GroupMemberRepository groupMembers, WorkspaceRepository workspaces, ObjectService objects,
            PrincipalViews views, PersonalNotifier notifier, CollabAudit audit, Clock clock) {
        this.access = access;
        this.acls = acls;
        this.registry = registry;
        this.principalRows = principalRows;
        this.principals = principals;
        this.groups = groups;
        this.groupMembers = groupMembers;
        this.workspaces = workspaces;
        this.objects = objects;
        this.views = views;
        this.notifier = notifier;
        this.audit = audit;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- views

    /** The {@code ObjectAcl} of a registered object. */
    @Transactional(readOnly = true)
    public Map<String, Object> view(ShareableObjectEntity rec) {
        Map<String, Map<String, Object>> cache = PrincipalViews.cache();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ref", rec.getObjectRef());
        out.put("type", rec.getType());
        out.put("owner", views.summary(rec.getOwner(), cache));
        out.put("visibility", rec.getVisibility().wireValue());
        out.put("tlp", rec.getTlp());
        out.put("entries", entries(rec.getObjectRef(), cache));
        out.put("updatedAt", rec.getUpdatedAt());
        return out;
    }

    /** The {@code ObjectAclEntry} views of an object's grants. */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> entries(String ref, Map<String, Map<String, Object>> cache) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (ObjectAclEntity g : acls.findByObjectRefOrderByGrantedAtAscIdAsc(ref)) {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("grantee", g.getGrantee());
            e.put("granteeDisplay", display(g.getGrantee()));
            e.put("role", g.getRole().wireValue());
            e.put("grantedBy", views.summary(g.getGrantedBy(), cache));
            e.put("grantedAt", g.getGrantedAt());
            e.put("expiresAt", g.getExpiresAt());
            out.add(e);
        }
        return out;
    }

    private String display(String grantee) {
        if (grantee.startsWith("principal:")) {
            return principalRows.findById(grantee.substring("principal:".length())).map(PrincipalEntity::getHandle)
                    .orElse(null);
        }
        if (grantee.startsWith("group:")) {
            try {
                return groups.get(grantee.substring("group:".length())).getName();
            } catch (RuntimeException e) {
                return null;
            }
        }
        return "instance";
    }

    // ------------------------------------------------------------------------- reads

    /** The access list of {@code ref} ({@code getObjectAcl}; owner or editor). */
    @Transactional
    public Map<String, Object> acl(String ref, ObjectCaller caller) {
        AccessResolver.requireShareable(ref);
        return view(access.require(caller, ref, AccessAction.SHARE));
    }

    // ------------------------------------------------------------------------- writes

    /**
     * Replaces the access list ({@code replaceObjectAcl}).
     *
     * @param ref the object
     * @param body the {@code ObjectAclInput} ({@code visibility}, {@code entries}, {@code owner}, {@code tlp})
     * @param caller an owner or editor
     * @param operation the audited operation name
     * @return the new {@code ObjectAcl}
     */
    @Transactional
    public Map<String, Object> replace(String ref, JsonNode body, ObjectCaller caller, String operation) {
        AccessResolver.requireShareable(ref);
        ShareableObjectEntity rec = access.require(caller, ref, AccessAction.SHARE);
        AclRole callerRole = access.role(caller, rec.getObjectRef());
        CollabInputs.object(body, "ObjectAclInput", ACL_FIELDS);
        JsonNode list = body.get("entries");
        if (list == null || !list.isArray()) {
            throw ValidationException.forField("entries", "must be an array");
        }
        if (list.size() > MAX_ENTRIES) {
            throw ValidationException.forField("entries", "holds more than " + MAX_ENTRIES + " grants");
        }
        String newOwner = null;
        if (body.hasNonNull("owner")) {
            if (callerRole != AclRole.OWNER) {
                throw forbidden("Only an owner may transfer ownership.");
            }
            newOwner = principals.get(CollabInputs.required(body, "owner", 1, 64)).getId();
        }
        Tlp tlp = body.hasNonNull("tlp") ? Tlp.parse("tlp", CollabInputs.text(body, "tlp", 1, 16), null)
                : Tlp.fromWire(rec.getTlp());
        Map<String, Grant> grants = new LinkedHashMap<>();
        for (JsonNode e : list) {
            CollabInputs.object(e, "ObjectAclEntryInput", ENTRY_FIELDS);
            String grantee = grantee(CollabInputs.required(e, "grantee", 1, 128));
            AclRole role = AclRole.parse("entries.role", CollabInputs.required(e, "role", 1, 16), null);
            if (role == AclRole.OWNER && callerRole != AclRole.OWNER) {
                throw forbidden("Only an owner may grant the owner role.");
            }
            grants.put(grantee, new Grant(grantee, role, CollabInputs.instant(e, "expiresAt")));
        }
        Visibility visibility = body.hasNonNull("visibility")
                ? Visibility.parse("visibility", CollabInputs.text(body, "visibility", 1, 16), null)
                : (rec.getVisibility() == Visibility.PRIVATE && !grants.isEmpty() ? Visibility.SHARED
                        : rec.getVisibility());
        String owner = newOwner == null ? rec.getOwner() : newOwner;
        checkTlp(tlp, owner, grants.keySet(), visibility);
        Map<String, ObjectAclEntity> old = new HashMap<>();
        for (ObjectAclEntity g : acls.findByObjectRefOrderByGrantedAtAscIdAsc(rec.getObjectRef())) {
            old.put(g.getGrantee(), g);
        }
        Instant now = clock.instant();
        for (ObjectAclEntity g : old.values()) {
            if (!grants.containsKey(g.getGrantee())) {
                acls.delete(g);
            }
        }
        acls.flush();
        for (Grant g : grants.values()) {
            ObjectAclEntity row = old.get(g.grantee());
            if (row == null) {
                row = new ObjectAclEntity(Ulids.next(now), rec.getObjectRef(), g.grantee(), g.role(),
                        caller.principal(), now);
            } else if (row.getRole() != g.role()) {
                row.setRole(g.role());
                row.setGrantedBy(caller.principal());
                row.setGrantedAt(now);
            }
            row.setExpiresAt(g.expiresAt());
            acls.save(row);
        }
        String previousOwner = rec.getOwner();
        rec.setOwner(owner);
        rec.setVisibility(visibility);
        rec.setTlp(tlp.wireValue());
        access.save(rec);
        if (newOwner != null && ObjectTypes.WORKSPACE.equals(rec.getType())) {
            String id = rec.getObjectRef().substring(rec.getObjectRef().indexOf(':') + 1);
            workspaces.findById(id).ifPresent(w -> {
                w.setOwner(owner);
                workspaces.save(w);
            });
        }
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("visibility", visibility.wireValue());
        detail.put("tlp", tlp.wireValue());
        detail.put("grants", grants.values().stream().map(g -> g.grantee() + "=" + g.role().wireValue()).toList());
        if (newOwner != null) {
            detail.put("ownerFrom", previousOwner);
            detail.put("ownerTo", newOwner);
        }
        audit.record(caller, operation, CollabAudit.TARGET_ACL, rec.getObjectRef(), detail);
        return view(rec);
    }

    /**
     * Shares an object ({@code shareObject}): adds grants (an existing stronger grant is kept) and notifies the
     * principals reached, directly or through a group, with {@code share.received}. A private object becomes shared.
     *
     * @param ref the object
     * @param body the {@code ShareRequest}
     * @param caller an owner or editor
     * @param operation the audited operation name
     * @return the new {@code ObjectAcl}
     */
    @Transactional
    public Map<String, Object> share(String ref, JsonNode body, ObjectCaller caller, String operation) {
        AccessResolver.requireShareable(ref);
        ShareableObjectEntity rec = access.require(caller, ref, AccessAction.SHARE);
        CollabInputs.object(body, "ShareRequest", SHARE_FIELDS);
        List<String> raw = CollabInputs.strings(body, "grantees", MAX_GRANTEES);
        if (raw.isEmpty()) {
            throw ValidationException.forField("grantees", "must hold at least one grantee");
        }
        AclRole role = AclRole.parse("role", CollabInputs.text(body, "role", 1, 16), AclRole.VIEWER);
        if (role == AclRole.OWNER) {
            throw ValidationException.forField("role", "must be viewer, commenter or editor");
        }
        String message = CollabInputs.text(body, "message", 0, 2000);
        Instant expiresAt = CollabInputs.instant(body, "expiresAt");
        Set<String> grantees = new LinkedHashSet<>();
        for (String g : raw) {
            grantees.add(grantee(g));
        }
        Tlp tlp = Tlp.fromWire(rec.getTlp());
        Visibility visibility = rec.getVisibility() == Visibility.PRIVATE ? Visibility.SHARED : rec.getVisibility();
        checkTlp(tlp, rec.getOwner(), grantees, Visibility.SHARED);
        Instant now = clock.instant();
        for (String g : grantees) {
            ObjectAclEntity row = acls.findByObjectRefAndGrantee(rec.getObjectRef(), g).orElse(null);
            if (row == null) {
                row = new ObjectAclEntity(Ulids.next(now), rec.getObjectRef(), g, role, caller.principal(), now);
            } else {
                row.setRole(AclRole.stronger(row.getRole(), role));
                row.setGrantedBy(caller.principal());
                row.setGrantedAt(now);
            }
            row.setExpiresAt(expiresAt);
            acls.save(row);
        }
        rec.setVisibility(visibility);
        access.save(rec);
        audit.record(caller, operation, CollabAudit.TARGET_ACL, rec.getObjectRef(), Map.of("shared",
                new ArrayList<>(grantees), "role", role.wireValue(), "tlp", tlp.wireValue()));
        notifyShare(rec, grantees, role, message, caller);
        return view(rec);
    }

    private void notifyShare(ShareableObjectEntity rec, Set<String> grantees, AclRole role, String message,
            ObjectCaller caller) {
        Set<String> people = new LinkedHashSet<>();
        for (String g : grantees) {
            if (g.startsWith("principal:")) {
                people.add(g.substring("principal:".length()));
            } else if (g.startsWith("group:")) {
                people.addAll(groups.memberIds(g.substring("group:".length())));
            }
        }
        people.remove(caller.principal());
        if (people.isEmpty()) {
            return;
        }
        Map<String, Object> sharedBy = views.summary(caller.principal());
        Instant now = clock.instant();
        String title = rec.getObjectRef();
        try {
            title = objects.find(ObjectRef.parse(rec.getObjectRef()), new ResolveContext(caller, null))
                    .map(ObjectEnvelope::title).orElse(title);
        } catch (RuntimeException e) {
            // keep the reference as the title
        }
        String who = sharedBy == null ? "Someone" : String.valueOf(sharedBy.get("displayName"));
        for (String p : people) {
            notifier.send(new PersonalNotifier.Notice(p, MeEvents.SHARE_RECEIVED, who + " shared " + title
                    + " with you", message, rec.getObjectRef(), caller.principal(), rec.getTlp(),
                    MeEvents.shareReceived(rec.getObjectRef(), null, role.wireValue(), sharedBy, message, now)));
        }
    }

    // ------------------------------------------------------------------------- listings

    /**
     * Objects shared with the caller, newest share first ({@code listSharedWithMe}): grants to its principal, its
     * groups or the instance, and {@code instance}-visible objects, excluding its own.
     *
     * @return the {@code SharedItemPage}
     */
    @Transactional
    public Map<String, Object> sharedWithMe(ObjectCaller caller, String type, String cursor, int limit) {
        if (type != null && !ShareableTypes.isShareable(type)) {
            throw ValidationException.forField("type", "must be one of " + ShareableTypes.ALL);
        }
        String me = caller.principal();
        Instant now = clock.instant();
        Map<String, Shared> byRef = new LinkedHashMap<>();
        Set<String> grantees = access.granteesOf(me);
        for (ObjectAclEntity g : acls.findByGranteeInOrderByGrantedAtDescIdAsc(grantees)) {
            if (!g.live(now) || (type != null && !g.getObjectRef().startsWith(type + ":"))) {
                continue;
            }
            String via = g.getGrantee().startsWith("principal:") ? "principal"
                    : g.getGrantee().startsWith("group:") ? "group" : "instance";
            String groupId = "group".equals(via) ? g.getGrantee().substring("group:".length()) : null;
            byRef.merge(g.getObjectRef(), new Shared(g.getObjectRef(), via, groupId, g.getGrantedBy(),
                    g.getGrantedAt()), ShareService::newer);
        }
        for (ShareableObjectEntity r : registry.findByVisibilityOrderByUpdatedAtDescObjectRefAsc(
                Visibility.INSTANCE.wireValue(), Limit.of(5_000))) {
            if (type != null && !type.equals(r.getType())) {
                continue;
            }
            byRef.putIfAbsent(r.getObjectRef(), new Shared(r.getObjectRef(), "instance", null, r.getOwner(),
                    r.getUpdatedAt()));
        }
        Map<String, ShareableObjectEntity> recs = new HashMap<>();
        for (ShareableObjectEntity r : registry.findByObjectRefIn(byRef.keySet())) {
            recs.put(r.getObjectRef(), r);
        }
        List<Shared> rows = new ArrayList<>();
        for (Shared s : byRef.values()) {
            ShareableObjectEntity r = recs.get(s.ref());
            if (r == null || (me != null && me.equals(r.getOwner())) || r.getVisibility() == Visibility.PRIVATE) {
                continue;
            }
            rows.add(s);
        }
        rows.sort(Comparator.comparing(Shared::sharedAt).reversed().thenComparing(Shared::ref));
        int offset = CollabInputs.offset(cursor);
        List<Shared> page = rows.subList(Math.min(offset, rows.size()), Math.min(offset + limit, rows.size()));
        Map<ObjectRef, ObjectEnvelope> envelopes = objects.resolveRefs(page.stream().map(s -> ObjectRef.parse(
                s.ref())).toList(), new ResolveContext(caller, null));
        Map<String, AclRole> roles = access.roles(caller, page.stream().map(Shared::ref).toList());
        Map<String, Map<String, Object>> cache = PrincipalViews.cache();
        List<Map<String, Object>> items = new ArrayList<>();
        for (Shared s : page) {
            ObjectEnvelope e = envelopes.get(ObjectRef.parse(s.ref()));
            AclRole role = roles.get(s.ref());
            if (e == null || role == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("envelope", e);
            item.put("role", role.wireValue());
            item.put("via", s.via());
            item.put("groupId", s.groupId());
            item.put("sharedBy", views.summary(s.sharedBy(), cache));
            item.put("sharedAt", s.sharedAt());
            items.add(item);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", offset + limit < rows.size() ? CollabInputs.cursor(offset + limit) : null);
        return out;
    }

    /**
     * The caller's own work products, newest change first ({@code listMyObjects}).
     *
     * @return the {@code ObjectEnvelopePage}
     */
    @Transactional
    public Map<String, Object> mine(ObjectCaller caller, String type, String cursor, int limit) {
        if (type != null && !ShareableTypes.isShareable(type)) {
            throw ValidationException.forField("type", "must be one of " + ShareableTypes.ALL);
        }
        int offset = CollabInputs.offset(cursor);
        Limit max = Limit.of(offset + limit + 1);
        List<ShareableObjectEntity> rows = type == null
                ? registry.findByOwnerOrderByUpdatedAtDescObjectRefAsc(caller.principal(), max)
                : registry.findByOwnerAndTypeOrderByUpdatedAtDescObjectRefAsc(caller.principal(), type, max);
        boolean more = rows.size() > offset + limit;
        List<ShareableObjectEntity> page = rows.subList(Math.min(offset, rows.size()), Math.min(offset + limit,
                rows.size()));
        List<ObjectRef> refs = new ArrayList<>();
        for (ShareableObjectEntity r : page) {
            if (ObjectRef.isValid(r.getObjectRef())) {
                refs.add(ObjectRef.parse(r.getObjectRef()));
            }
        }
        Map<ObjectRef, ObjectEnvelope> envelopes = objects.resolveRefs(refs, new ResolveContext(caller, null));
        List<ObjectEnvelope> items = new ArrayList<>();
        for (ObjectRef r : refs) {
            ObjectEnvelope e = envelopes.get(r);
            if (e != null) {
                items.add(e);
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", more ? CollabInputs.cursor(offset + limit) : null);
        return out;
    }

    // ------------------------------------------------------------------------- TLP

    /**
     * Checks grants and a visibility against a TLP label for an object owned by {@code ownerId}.
     *
     * @throws ForesignalException 403 {@code TLP_VIOLATION} naming the rule
     */
    public void checkTlp(Tlp tlp, String ownerId, java.util.Collection<String> grantees, Visibility visibility) {
        PrincipalEntity owner = principalRows.findById(ownerId).orElse(null);
        String ownerOrg = PrincipalAdminService.normalize(owner == null ? null : owner.getOrganization());
        if (visibility == Visibility.INSTANCE || grantees.contains(ObjectAclEntity.INSTANCE)) {
            enforce(EgressPolicy.tlp(tlp, TlpTarget.instance(instanceSameOrganization(ownerOrg))));
        }
        for (String g : grantees) {
            if (g.startsWith("principal:")) {
                PrincipalEntity p = principalRows.findById(g.substring("principal:".length())).orElse(null);
                enforce(EgressPolicy.tlp(tlp, TlpTarget.principal(PrincipalAdminService.sameOrganization(owner, p))));
            } else if (g.startsWith("group:")) {
                enforce(EgressPolicy.tlp(tlp, TlpTarget.group(groupSameOrganization(
                        g.substring("group:".length()), ownerOrg))));
            }
        }
    }

    /** The {@code TLP_VIOLATION} problem of a refused decision. */
    public static void enforce(TlpTarget.Decision decision) {
        if (!decision.allowed()) {
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.TLP_VIOLATION,
                    "The object's TLP label forbids this share: " + decision.rule() + ".")
                    .withProperty("rule", decision.rule());
        }
    }

    private boolean instanceSameOrganization(String ownerOrg) {
        for (PrincipalEntity p : principalRows.findAll()) {
            if (!p.isDisabled() && !ownerOrg.equals(PrincipalAdminService.normalize(p.getOrganization()))) {
                return false;
            }
        }
        return true;
    }

    private boolean groupSameOrganization(String groupId, String ownerOrg) {
        for (GroupMemberEntity m : groupMembers.findByGroupIdOrderByAddedAtAscIdAsc(groupId)) {
            PrincipalEntity p = principalRows.findById(m.getPrincipalId()).orElse(null);
            if (p != null && !ownerOrg.equals(PrincipalAdminService.normalize(p.getOrganization()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * The canonical grantee of a request value.
     *
     * @throws ai.foresignal.api.error.NotFoundException {@code UNKNOWN_PRINCIPAL} or {@code UNKNOWN_GROUP}
     */
    public String grantee(String value) {
        String v = value.strip();
        if (ObjectAclEntity.INSTANCE.equals(v)) {
            return v;
        }
        if (v.startsWith("principal:") && v.length() > "principal:".length()) {
            return "principal:" + principals.get(v.substring("principal:".length())).getId();
        }
        if (v.startsWith("group:") && v.length() > "group:".length()) {
            return "group:" + groups.get(v.substring("group:".length())).getId();
        }
        throw ValidationException.forField("grantee", "must be principal:{id}, group:{id} or instance");
    }

    private static ForesignalException forbidden(String detail) {
        return new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN, detail);
    }

    private static Shared newer(Shared a, Shared b) {
        return a.sharedAt().isAfter(b.sharedAt()) ? a : b;
    }

    private record Grant(String grantee, AclRole role, Instant expiresAt) {}

    private record Shared(String ref, String via, String groupId, String sharedBy, Instant sharedAt) {}
}
