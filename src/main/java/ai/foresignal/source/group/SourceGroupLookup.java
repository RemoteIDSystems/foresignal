package ai.foresignal.source.group;

import ai.foresignal.security.user.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves a source group to its member sources for the engines that use a group as a scope (prompt SG-002): the
 * briefing scope resolver ({@code BriefingScope.sourceGroupIds}) and the trigger evaluator
 * ({@code trigger_rule.source_group_id}).
 *
 * <p>A group id is server-generated and globally unique, so the briefing path resolves it by id alone
 * ({@link #memberSources(String)}); the scope was authored by someone with access to the group, and an unknown or
 * deleted group resolves to empty so the caller fails closed. The trigger path additionally checks the group is owned
 * by the rule's owner: rules key on {@code owner_principal_id} (a string) while groups key on {@code owner_user_id}
 * (a uuid), so {@link #memberSourcesForPrincipal} maps the rule's owning principal to that user before matching.
 */
@Component
public class SourceGroupLookup {

    private final SourceGroupRepository groups;
    private final UserRepository users;

    /** Creates the lookup. */
    public SourceGroupLookup(SourceGroupRepository groups, UserRepository users) {
        this.groups = groups;
        this.users = users;
    }

    /**
     * The member source ids of the group {@code groupId}, or empty when there is no such group (deleted or unknown).
     * Used by the briefing scope resolver, which fails closed on an empty result.
     */
    @Transactional(readOnly = true)
    public Optional<List<String>> memberSources(String groupId) {
        if (groupId == null) {
            return Optional.empty();
        }
        return groups.findById(groupId).map(g -> List.copyOf(g.getSourceIds()));
    }

    /**
     * True when {@code groupId} names a group owned by the user whose principal is {@code ownerPrincipalId} (prompt
     * SG-002). A null principal (a shared rule) owns no group.
     */
    @Transactional(readOnly = true)
    public boolean existsForPrincipal(String groupId, String ownerPrincipalId) {
        return ownerFor(ownerPrincipalId).flatMap(owner -> groups.findByIdAndOwnerUserId(groupId, owner)).isPresent();
    }

    /**
     * The member source ids of {@code groupId} when it is owned by the user whose principal is {@code ownerPrincipalId}
     * (prompt SG-002), or empty when there is no such group or it belongs to another user. Used by the trigger
     * evaluator, which fails closed on an empty result.
     */
    @Transactional(readOnly = true)
    public Optional<List<String>> memberSourcesForPrincipal(String groupId, String ownerPrincipalId) {
        if (groupId == null) {
            return Optional.empty();
        }
        return ownerFor(ownerPrincipalId)
                .flatMap(owner -> groups.findByIdAndOwnerUserId(groupId, owner))
                .map(g -> List.copyOf(g.getSourceIds()));
    }

    private Optional<UUID> ownerFor(String ownerPrincipalId) {
        if (ownerPrincipalId == null) {
            return Optional.empty();
        }
        return users.findByPrincipalId(ownerPrincipalId).map(ai.foresignal.security.user.UserEntity::getId);
    }
}
