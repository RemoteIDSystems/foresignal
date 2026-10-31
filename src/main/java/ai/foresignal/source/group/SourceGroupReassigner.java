package ai.foresignal.source.group;

import ai.foresignal.security.user.UserOwnedConfigReassigner;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * A deleted user's source groups (prompt SG-002, the {@link UserOwnedConfigReassigner} of R2-005), audit name
 * {@code sourceGroups}.
 *
 * <p>A group is a person's own set of sources with their own analysis and notification settings, keyed on
 * {@code owner_user_id} (uuid); moving it onto another admin would be meaningless, so reassigning and deleting both
 * remove the rows (like {@code sourceViews}). The count is written to the audit log. An edition that referenced the
 * group keeps working — its {@code source_group_id} resolves to nothing and fails closed.
 */
@Component
public class SourceGroupReassigner implements UserOwnedConfigReassigner {

    private final SourceGroupRepository groups;

    /** Creates the reassigner. */
    public SourceGroupReassigner(SourceGroupRepository groups) {
        this.groups = groups;
    }

    @Override
    public String name() {
        return "sourceGroups";
    }

    @Override
    @Transactional
    public int reassign(UUID owner, UUID target) {
        return (int) groups.deleteByOwnerUserId(owner);
    }

    @Override
    @Transactional
    public int delete(UUID owner) {
        return (int) groups.deleteByOwnerUserId(owner);
    }
}
