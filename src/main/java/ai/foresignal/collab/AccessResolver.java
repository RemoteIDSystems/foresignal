package ai.foresignal.collab;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.object.AnnotationEntity;
import ai.foresignal.object.AnnotationRepository;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectGrants;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.security.Tlp;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Decides who may do what with a work product (prompt 130, COL-1): {@code can(principal, objectRef, action)}.
 *
 * <p>A caller's role on a shareable object is the strongest of:
 * <ul>
 *   <li>{@code owner} for the registered owner, and for any admin token (admins see everything);</li>
 *   <li>a role the object itself gives (a 129 workspace member);</li>
 *   <li>unless the visibility is {@code private}: every live grant to the caller's principal, to a group the caller
 *       belongs to, or to {@code instance}; and {@code viewer} for everyone when the visibility is
 *       {@code instance}.</li>
 * </ul>
 * A note ({@code annotation}) on a shareable target is visible only to callers who can see that target. A caller with
 * no role does not see the object at all: APIs report it as not found ({@code 404}), never forbidden, so ids do not
 * leak. Non-shareable types (records, situations, findings…) are not decided here: they stay readable per scope.
 *
 * <p>Ownership records ({@code shareable_object}) are created by the owning services on creation, or lazily from
 * the type's {@link ShareableSource} defaults for objects that predate prompt 130.
 */
@Component
public class AccessResolver implements ObjectGrants {

    private static final String INSERT = """
            INSERT INTO shareable_object (object_ref, type, owner, visibility, tlp, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (object_ref) DO NOTHING
            """;

    private final ShareableObjectRepository registry;
    private final ObjectAclRepository acls;
    private final GroupMemberRepository members;
    private final AnnotationRepository annotations;
    private final Map<String, ShareableSource> sources;
    private final CollabProperties properties;
    private final JdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the resolver over every {@link ShareableSource} bean. */
    public AccessResolver(ShareableObjectRepository registry, ObjectAclRepository acls, GroupMemberRepository members,
            AnnotationRepository annotations, List<ShareableSource> sources, CollabProperties properties,
            JdbcTemplate jdbc, Clock clock) {
        this.registry = registry;
        this.acls = acls;
        this.members = members;
        this.annotations = annotations;
        Map<String, ShareableSource> byType = new HashMap<>();
        for (ShareableSource s : sources) {
            byType.put(s.type(), s);
        }
        this.sources = Map.copyOf(byType);
        this.properties = properties;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- registration

    /** True when {@code ref} names a shareable type. */
    public static boolean isShareable(String ref) {
        int colon = ref == null ? -1 : ref.indexOf(':');
        return colon > 0 && ShareableTypes.isShareable(ref.substring(0, colon));
    }

    /**
     * The ownership record of {@code ref}, registering it from its source's defaults when it exists but has none.
     *
     * @param ref a reference (an investigation also by display id)
     * @return the record, or empty when the reference is not shareable or the object does not exist
     */
    public Optional<ShareableObjectEntity> record(String ref) {
        if (!isShareable(ref)) {
            return Optional.empty();
        }
        Optional<ShareableObjectEntity> found = registry.findById(ref);
        if (found.isPresent()) {
            return found;
        }
        String type = ref.substring(0, ref.indexOf(':'));
        String id = ref.substring(ref.indexOf(':') + 1);
        ShareableSource source = sources.get(type);
        if (source == null || id.isEmpty()) {
            return Optional.empty();
        }
        Optional<ShareableSource.Defaults> defaults = source.defaults(id);
        if (defaults.isEmpty()) {
            return Optional.empty();
        }
        ShareableSource.Defaults d = defaults.get();
        String canonical = type + ":" + d.canonicalId();
        if (!canonical.equals(ref)) {
            Optional<ShareableObjectEntity> existing = registry.findById(canonical);
            if (existing.isPresent()) {
                return existing;
            }
        }
        Instant now = clock.instant();
        Tlp tlp = d.tlp() == null ? properties.tlp() : d.tlp();
        jdbc.update(INSERT, canonical, type, d.owner(), d.visibility().wireValue(), tlp.wireValue(),
                Timestamp.from(now), Timestamp.from(now));
        return registry.findById(canonical);
    }

    /**
     * Registers a new work product (its service calls this on creation).
     *
     * @param ref the canonical reference
     * @param owner the owner's principal id
     * @param visibility the visibility
     * @param tlp the TLP label, or null for the instance default
     * @return the saved record
     */
    public ShareableObjectEntity register(String ref, String owner, Visibility visibility, Tlp tlp) {
        String type = ref.substring(0, ref.indexOf(':'));
        if (!ShareableTypes.isShareable(type)) {
            throw new IllegalArgumentException(type + " is not shareable");
        }
        ShareableObjectEntity row = registry.findById(ref).orElse(null);
        Instant now = clock.instant();
        String label = (tlp == null ? properties.tlp() : tlp).wireValue();
        if (row == null) {
            row = new ShareableObjectEntity(ref, type, owner, visibility, label, now);
        } else {
            row.setOwner(owner);
            row.setVisibility(visibility);
            row.setTlp(label);
            row.setUpdatedAt(now);
        }
        return registry.save(row);
    }

    /** Saves a changed record. */
    public ShareableObjectEntity save(ShareableObjectEntity row) {
        row.setUpdatedAt(clock.instant());
        return registry.save(row);
    }

    /** Forgets a deleted work product: its record and its grants. */
    public void forget(String ref) {
        acls.deleteByObjectRef(ref);
        registry.deleteById(ref);
    }

    /** The default TLP label. */
    public Tlp defaultTlp() {
        return properties.tlp();
    }

    // ------------------------------------------------------------------------- decisions

    /**
     * The caller's role on {@code ref}.
     *
     * @return the role, or null when the caller has none (or the object does not exist)
     */
    public AclRole role(ObjectCaller caller, String ref) {
        if (caller == null || caller.anonymous()) {
            return null;
        }
        Optional<ShareableObjectEntity> rec = record(ref);
        if (rec.isEmpty()) {
            return null;
        }
        if (caller.admin()) {
            return AclRole.OWNER;
        }
        return granted(rec.get(), caller, acls.findByObjectRefOrderByGrantedAtAscIdAsc(rec.get().getObjectRef()),
                groupIds(caller.principal()));
    }

    /** True when the caller may perform {@code action} on {@code ref}. */
    public boolean can(ObjectCaller caller, String ref, AccessAction action) {
        AclRole role = role(caller, ref);
        return role != null && role.atLeast(action.required());
    }

    /**
     * The record of {@code ref} when the caller may perform {@code action}.
     *
     * @throws NotFoundException {@code UNKNOWN_OBJECT} when the object does not exist or the caller may not see it
     * @throws ForesignalException 403 {@code FORBIDDEN} when the caller sees it but its role is too weak
     */
    public ShareableObjectEntity require(ObjectCaller caller, String ref, AccessAction action) {
        Optional<ShareableObjectEntity> rec = record(ref);
        AclRole role = rec.isEmpty() ? null : role(caller, rec.get().getObjectRef());
        if (role == null) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_OBJECT,
                    "No object '" + ref + "' exists, or you may not see it.");
        }
        if (!role.atLeast(action.required())) {
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN, "Your role (" + role.wireValue()
                    + ") does not allow " + action.name().toLowerCase(java.util.Locale.ROOT) + " on " + ref + ".");
        }
        return rec.get();
    }

    /**
     * The caller's roles on several references in one pass (envelopes, listings). References that are not shareable
     * are left out; shareable references the caller may not see map to null.
     */
    public Map<String, AclRole> roles(ObjectCaller caller, Collection<String> refs) {
        Map<String, AclRole> out = new LinkedHashMap<>();
        Set<String> shareable = new HashSet<>();
        for (String r : refs) {
            if (isShareable(r)) {
                shareable.add(r);
            }
        }
        if (shareable.isEmpty()) {
            return out;
        }
        Map<String, ShareableObjectEntity> recs = new HashMap<>();
        for (ShareableObjectEntity e : registry.findByObjectRefIn(shareable)) {
            recs.put(e.getObjectRef(), e);
        }
        for (String r : shareable) {
            if (!recs.containsKey(r)) {
                record(r).ifPresent(e -> recs.put(r, e));
            }
        }
        if (caller == null || caller.anonymous()) {
            shareable.forEach(r -> out.put(r, null));
            return out;
        }
        Map<String, List<ObjectAclEntity>> grants = new HashMap<>();
        for (ObjectAclEntity g : acls.findByObjectRefIn(recs.values().stream()
                .map(ShareableObjectEntity::getObjectRef).toList())) {
            grants.computeIfAbsent(g.getObjectRef(), k -> new java.util.ArrayList<>()).add(g);
        }
        Set<String> groups = groupIds(caller.principal());
        for (String r : shareable) {
            ShareableObjectEntity e = recs.get(r);
            if (e == null) {
                out.put(r, null);
            } else if (caller.admin()) {
                out.put(r, AclRole.OWNER);
            } else {
                out.put(r, granted(e, caller, grants.getOrDefault(e.getObjectRef(), List.of()), groups));
            }
        }
        return out;
    }

    /** The groups {@code principalId} belongs to. */
    public Set<String> groupIds(String principalId) {
        Set<String> out = new HashSet<>();
        if (principalId == null) {
            return out;
        }
        for (GroupMemberEntity m : members.findByPrincipalId(principalId)) {
            out.add(m.getGroupId());
        }
        return out;
    }

    /** The grantee strings that reach {@code principalId}: itself, its groups and {@code instance}. */
    public Set<String> granteesOf(String principalId) {
        Set<String> out = new HashSet<>();
        out.add(ObjectAclEntity.INSTANCE);
        if (principalId != null) {
            out.add("principal:" + principalId);
            for (String g : groupIds(principalId)) {
                out.add("group:" + g);
            }
        }
        return out;
    }

    private AclRole granted(ShareableObjectEntity rec, ObjectCaller caller, List<ObjectAclEntity> grants,
            Set<String> groups) {
        String principal = caller.principal();
        if (principal != null && principal.equals(rec.getOwner())) {
            return AclRole.OWNER;
        }
        if (ObjectTypes.ANNOTATION.equals(rec.getType()) && !targetVisible(rec, caller)) {
            return null;
        }
        ShareableSource source = sources.get(rec.getType());
        String id = rec.getObjectRef().substring(rec.getObjectRef().indexOf(':') + 1);
        AclRole best = source == null || principal == null ? null : source.intrinsicRole(id, principal);
        if (rec.getVisibility() == Visibility.PRIVATE) {
            return best;
        }
        Instant now = clock.instant();
        for (ObjectAclEntity g : grants) {
            if (!g.live(now)) {
                continue;
            }
            String grantee = g.getGrantee();
            boolean match = ObjectAclEntity.INSTANCE.equals(grantee)
                    || (principal != null && grantee.equals("principal:" + principal))
                    || (grantee.startsWith("group:") && groups.contains(grantee.substring("group:".length())));
            if (match) {
                best = AclRole.stronger(best, g.getRole());
            }
        }
        if (rec.getVisibility() == Visibility.INSTANCE) {
            best = AclRole.stronger(best, AclRole.VIEWER);
        }
        return best;
    }

    /** A note on a shareable target is visible only to callers who can see the target. */
    private boolean targetVisible(ShareableObjectEntity rec, ObjectCaller caller) {
        String id = rec.getObjectRef().substring(rec.getObjectRef().indexOf(':') + 1);
        Optional<AnnotationEntity> a = annotations.findById(id);
        if (a.isEmpty() || a.get().getDeletedAt() != null) {
            return false;
        }
        String target = a.get().getTargetRef();
        return !isShareable(target) || role(caller, target) != null;
    }

    // ------------------------------------------------------------------------- ObjectGrants

    @Override
    public String grantedRole(String objectRef, ObjectCaller caller) {
        if (caller == null || caller.anonymous()) {
            return null;
        }
        Optional<ShareableObjectEntity> rec = record(objectRef);
        if (rec.isEmpty()) {
            return null;
        }
        AclRole role = granted(rec.get(), caller, acls.findByObjectRefOrderByGrantedAtAscIdAsc(
                rec.get().getObjectRef()), groupIds(caller.principal()));
        return role == null ? null : role.wireValue();
    }

    @Override
    public Optional<String> tlp(String objectRef) {
        return record(objectRef).map(ShareableObjectEntity::getTlp);
    }

    /** The TLP labels of the registered references among {@code refs}. */
    public Map<String, String> tlps(Collection<String> refs) {
        Map<String, String> out = new HashMap<>();
        if (refs.isEmpty()) {
            return out;
        }
        for (ShareableObjectEntity e : registry.findByObjectRefIn(refs)) {
            out.put(e.getObjectRef(), e.getTlp());
        }
        return out;
    }

    /** The TLP label of {@code ref} (the default when it is not registered). */
    public Tlp tlpOf(String ref) {
        return record(ref).map(r -> Tlp.fromWire(r.getTlp())).orElse(properties.tlp());
    }

    /**
     * Rejects a reference that is not shareable ({@code 400 OBJECT_REF_INVALID}).
     *
     * @return the parsed reference
     */
    public static ObjectRef requireShareable(String ref) {
        ObjectRef parsed = ObjectRef.parse(ref);
        if (!ShareableTypes.isShareable(parsed.type())) {
            throw new ValidationException(ErrorCodes.OBJECT_REF_INVALID, "The object is not shareable.",
                    List.of(new ai.foresignal.api.error.FieldError("ref", "type " + parsed.type()
                            + " has no owner or access list; shareable types are " + ShareableTypes.ALL)));
        }
        return parsed;
    }
}
