package ai.foresignal.collab;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.Ulids;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * Groups of principals (prompt 130), used as access-list grantees ({@code group:{id}}). Anyone with a token lists
 * and reads groups; creating one needs agent scope and makes the creator a manager; managers and admins change the
 * group and its members. Deleting a group removes its grants.
 */
@Service
public class GroupService {

    /** Most members added in one request. */
    public static final int MAX_MEMBERS = 500;

    private static final Set<String> INPUT = Set.of("name", "description", "members");
    private static final Set<String> PATCH = Set.of("name", "description");

    private final GroupRepository groups;
    private final GroupMemberRepository members;
    private final ObjectAclRepository acls;
    private final PrincipalAdminService principals;
    private final PrincipalViews views;
    private final CollabAudit audit;
    private final Clock clock;

    /** Creates the service. */
    public GroupService(GroupRepository groups, GroupMemberRepository members, ObjectAclRepository acls,
            PrincipalAdminService principals, PrincipalViews views, CollabAudit audit, Clock clock) {
        this.groups = groups;
        this.members = members;
        this.acls = acls;
        this.principals = principals;
        this.views = views;
        this.audit = audit;
        this.clock = clock;
    }

    /**
     * The group {@code id}.
     *
     * @throws NotFoundException {@code UNKNOWN_GROUP} when there is none
     */
    @Transactional(readOnly = true)
    public GroupEntity get(String id) {
        return (id == null ? java.util.Optional.<GroupEntity>empty() : groups.findById(id))
                .orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_GROUP, "No group '" + id + "' exists."));
    }

    /** The {@code Group} view, with the members when {@code withMembers}. */
    @Transactional(readOnly = true)
    public Map<String, Object> view(GroupEntity g, boolean withMembers) {
        Map<String, Map<String, Object>> cache = PrincipalViews.cache();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", g.getId());
        out.put("ref", "group:" + g.getId());
        out.put("name", g.getName());
        out.put("description", g.getDescription());
        out.put("createdBy", views.summary(g.getCreatedBy(), cache));
        List<GroupMemberEntity> rows = members.findByGroupIdOrderByAddedAtAscIdAsc(g.getId());
        out.put("memberCount", rows.size());
        if (withMembers) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (GroupMemberEntity m : rows) {
                Map<String, Object> member = new LinkedHashMap<>();
                member.put("principal", views.summary(m.getPrincipalId(), cache));
                member.put("role", m.getRole());
                member.put("addedAt", m.getAddedAt());
                member.put("addedBy", views.summary(m.getAddedBy(), cache));
                list.add(member);
            }
            out.put("members", list);
        }
        out.put("createdAt", g.getCreatedAt());
        out.put("updatedAt", g.getUpdatedAt());
        return out;
    }

    /**
     * A page of groups by id ({@code listGroups}).
     *
     * @param q a substring of the name, or null
     * @param member a principal id or handle the groups must contain, or null
     * @param cursor the opaque cursor, or null
     * @param limit the page size
     * @return the {@code GroupPage}
     */
    @Transactional(readOnly = true)
    public Map<String, Object> list(String q, String member, String cursor, int limit) {
        String needle = q == null || q.isBlank() ? null : q.strip().toLowerCase(Locale.ROOT);
        Set<String> memberOf = null;
        if (member != null && !member.isBlank()) {
            PrincipalEntity p = principals.get(member);
            memberOf = new java.util.HashSet<>();
            for (GroupMemberEntity m : members.findByPrincipalId(p.getId())) {
                memberOf.add(m.getGroupId());
            }
        }
        int offset = CollabInputs.offset(cursor);
        List<Map<String, Object>> items = new ArrayList<>();
        int seen = 0;
        boolean more = false;
        for (GroupEntity g : groups.findByIdGreaterThanOrderByIdAsc("", Limit.unlimited())) {
            if (needle != null && !g.getName().toLowerCase(Locale.ROOT).contains(needle)) {
                continue;
            }
            if (memberOf != null && !memberOf.contains(g.getId())) {
                continue;
            }
            if (seen++ < offset) {
                continue;
            }
            if (items.size() == limit) {
                more = true;
                break;
            }
            items.add(view(g, false));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", more ? CollabInputs.cursor(offset + limit) : null);
        return out;
    }

    /** Creates a group; the creator becomes a manager ({@code createGroup}). */
    @Transactional
    public GroupEntity create(JsonNode body, ObjectCaller caller) {
        CollabInputs.object(body, "GroupInput", INPUT);
        Instant now = clock.instant();
        GroupEntity g = new GroupEntity(Ulids.next(now), CollabInputs.required(body, "name", 1, 200).strip(),
                caller.principal(), now);
        g.setDescription(CollabInputs.text(body, "description", 0, 2000));
        groups.save(g);
        if (caller.principal() != null && principals.find(caller.principal()).isPresent()) {
            members.save(new GroupMemberEntity(Ulids.next(now), g.getId(), caller.principal(),
                    GroupMemberEntity.MANAGER, caller.principal(), now));
        }
        addAll(g, body.get("members"), caller, now);
        audit.record(caller, "createGroup", CollabAudit.TARGET_GROUP, g.getId(), Map.of("name", g.getName()));
        return g;
    }

    /** Applies a JSON merge patch ({@code updateGroup}; managers or admin). */
    @Transactional
    public GroupEntity patch(String id, JsonNode body, ObjectCaller caller) {
        GroupEntity g = get(id);
        requireManager(g, caller);
        CollabInputs.object(body, "GroupPatch", PATCH);
        if (body.has("name")) {
            g.setName(CollabInputs.required(body, "name", 1, 200).strip());
        }
        if (body.has("description")) {
            g.setDescription(CollabInputs.text(body, "description", 0, 2000));
        }
        g.setUpdatedAt(clock.instant());
        audit.record(caller, "updateGroup", CollabAudit.TARGET_GROUP, g.getId(), Map.of("fields",
                new ArrayList<>(body.propertyNames())));
        return groups.save(g);
    }

    /** Deletes a group, its members and its grants ({@code deleteGroup}; managers or admin). */
    @Transactional
    public void delete(String id, ObjectCaller caller) {
        GroupEntity g = get(id);
        requireManager(g, caller);
        members.deleteByGroupId(g.getId());
        acls.deleteByGrantee("group:" + g.getId());
        groups.delete(g);
        audit.record(caller, "deleteGroup", CollabAudit.TARGET_GROUP, g.getId(), Map.of("name", g.getName()));
    }

    /** Adds or updates members ({@code addGroupMembers}; managers or admin). */
    @Transactional
    public GroupEntity addMembers(String id, JsonNode body, ObjectCaller caller) {
        GroupEntity g = get(id);
        requireManager(g, caller);
        CollabInputs.object(body, "GroupMembersInput", Set.of("members"));
        JsonNode list = body.get("members");
        if (list == null || !list.isArray() || list.isEmpty()) {
            throw ValidationException.forField("members", "must hold at least one member");
        }
        addAll(g, list, caller, clock.instant());
        g.setUpdatedAt(clock.instant());
        return groups.save(g);
    }

    /** Removes a member ({@code removeGroupMembers}; managers or admin), as {@link #removeMembers}. */
    @Transactional
    public GroupEntity removeMember(String id, String principalId, ObjectCaller caller) {
        return removeMembers(id, principalId == null ? List.of() : List.of(principalId), caller);
    }

    /**
     * Removes the listed principals, ids or handles ({@code removeGroupMembers}): managers or admin, or a
     * principal removing only itself. Every principal is resolved before anything is removed, so an unknown
     * one changes nothing. Each removal is audited.
     */
    @Transactional
    public GroupEntity removeMembers(String id, List<String> principalIds, ObjectCaller caller) {
        GroupEntity g = get(id);
        List<String> keys = principalIds == null ? List.of()
                : principalIds.stream().filter(k -> k != null && !k.isBlank()).toList();
        if (keys.isEmpty()) {
            throw ValidationException.forField("principalId", "is required");
        }
        Set<String> ids = new LinkedHashSet<>();
        for (String key : keys) {
            ids.add(principals.get(key).getId());
        }
        boolean selfOnly = caller.principal() != null && ids.equals(Set.of(caller.principal()));
        if (!selfOnly) {
            requireManager(g, caller);
        }
        for (String principalId : ids) {
            members.findByGroupIdAndPrincipalId(g.getId(), principalId).ifPresent(members::delete);
            audit.record(caller, "removeGroupMembers", CollabAudit.TARGET_GROUP, g.getId(),
                    Map.of("principalId", principalId));
        }
        g.setUpdatedAt(clock.instant());
        return groups.save(g);
    }

    /** The member ids of a group. */
    @Transactional(readOnly = true)
    public List<String> memberIds(String groupId) {
        return members.findByGroupIdOrderByAddedAtAscIdAsc(groupId).stream().map(GroupMemberEntity::getPrincipalId)
                .toList();
    }

    /** The groups a principal belongs to, with its role in each ({@code Me.groups}). */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> membershipsOf(String principalId) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (principalId == null) {
            return out;
        }
        for (GroupMemberEntity m : members.findByPrincipalId(principalId)) {
            groups.findById(m.getGroupId()).ifPresent(g -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", g.getId());
                row.put("name", g.getName());
                row.put("role", m.getRole());
                out.add(row);
            });
        }
        return out;
    }

    private void addAll(GroupEntity g, JsonNode list, ObjectCaller caller, Instant now) {
        if (list == null || list.isNull()) {
            return;
        }
        if (!list.isArray()) {
            throw ValidationException.forField("members", "must be an array");
        }
        if (list.size() > MAX_MEMBERS) {
            throw ValidationException.forField("members", "holds more than " + MAX_MEMBERS + " members");
        }
        List<String> added = new ArrayList<>();
        for (JsonNode m : list) {
            String pid = m == null ? null : CollabInputs.text(m, "principalId", 1, 64);
            if (pid == null) {
                throw ValidationException.forField("members", "each member needs a principalId");
            }
            String role = CollabInputs.text(m, "role", 1, 16);
            role = role == null ? GroupMemberEntity.MEMBER : role;
            if (!GroupMemberEntity.MEMBER.equals(role) && !GroupMemberEntity.MANAGER.equals(role)) {
                throw ValidationException.forField("members.role", "must be member or manager");
            }
            PrincipalEntity p = principals.get(pid);
            GroupMemberEntity row = members.findByGroupIdAndPrincipalId(g.getId(), p.getId()).orElse(null);
            if (row == null) {
                members.save(new GroupMemberEntity(Ulids.next(now), g.getId(), p.getId(), role, caller.principal(),
                        now));
            } else {
                row.setRole(role);
                members.save(row);
            }
            added.add(p.getId());
        }
        if (!added.isEmpty()) {
            audit.record(caller, "addGroupMembers", CollabAudit.TARGET_GROUP, g.getId(), Map.of("principalIds",
                    added));
        }
    }

    private void requireManager(GroupEntity g, ObjectCaller caller) {
        if (caller.admin()) {
            return;
        }
        boolean manager = caller.principal() != null && members.findByGroupIdAndPrincipalId(g.getId(),
                caller.principal()).map(m -> GroupMemberEntity.MANAGER.equals(m.getRole())).orElse(false);
        if (!manager) {
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN,
                    "Only the group's managers or an admin may change it.");
        }
    }
}
