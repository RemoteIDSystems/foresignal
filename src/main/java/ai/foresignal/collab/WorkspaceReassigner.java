package ai.foresignal.collab;

import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.WorkspaceEntity;
import ai.foresignal.object.WorkspaceRepository;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserOwnedConfigReassigner;
import ai.foresignal.security.user.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Moves or deletes a deleted user's workspaces (prompt R2-012, the {@link UserOwnedConfigReassigner} of R2-005),
 * audit name {@code workspaces}.
 *
 * <p>A workspace's owner ({@code workspace.owner}) is the principal of the caller that created it, which for a
 * signed-in user is the user's principal. Reassigning makes the target admin's principal the owner, in the workspace
 * and in its 130 access record when it has one (the same pair {@code ShareService} changes on an ownership transfer);
 * members and grants are kept, and the new owner is dropped from the member list because the owner role covers it.
 * {@code workspace.owner} cannot be empty, so with no target (no admin account exists) the workspaces stay with the
 * deleted user's principal, where an admin token can still read, transfer or delete them; nothing is counted. Deleting
 * removes them, exactly as {@code deleteWorkspace} would.
 */
@Component
public class WorkspaceReassigner implements UserOwnedConfigReassigner {

    private final UserRepository users;
    private final WorkspaceRepository workspaces;
    private final ShareableObjectRepository registry;
    private final Clock clock;

    /**
     * Creates the reassigner.
     *
     * @param users the user store, mapping a user to their principal
     * @param workspaces the workspace store
     * @param registry the 130 access records
     * @param clock the clock
     */
    public WorkspaceReassigner(UserRepository users, WorkspaceRepository workspaces,
            ShareableObjectRepository registry, Clock clock) {
        this.users = users;
        this.workspaces = workspaces;
        this.registry = registry;
        this.clock = clock;
    }

    @Override
    public String name() {
        return "workspaces";
    }

    @Override
    public int reassign(UUID owner, UUID target) {
        String from = principal(owner);
        String to = target == null ? null : principal(target);
        if (from == null || to == null) {
            return 0;
        }
        List<WorkspaceEntity> owned = workspaces.findByOwnerOrderByIdAsc(from);
        Instant now = clock.instant();
        for (WorkspaceEntity w : owned) {
            w.setOwner(to);
            if (w.getMembers().containsKey(to)) {
                Map<String, String> members = new LinkedHashMap<>(w.getMembers());
                members.remove(to);
                w.setMembers(members);
            }
            w.setUpdatedAt(now);
            registry.findById(ObjectTypes.WORKSPACE + ":" + w.getId()).ifPresent(rec -> {
                rec.setOwner(to);
                rec.setUpdatedAt(now);
                registry.save(rec);
            });
        }
        workspaces.saveAllAndFlush(owned);
        return owned.size();
    }

    @Override
    public int delete(UUID owner) {
        String from = principal(owner);
        if (from == null) {
            return 0;
        }
        List<WorkspaceEntity> owned = workspaces.findByOwnerOrderByIdAsc(from);
        workspaces.deleteAll(owned);
        workspaces.flush();
        return owned.size();
    }

    private String principal(UUID user) {
        return users.findById(user).map(UserEntity::getPrincipalId).orElse(null);
    }
}
