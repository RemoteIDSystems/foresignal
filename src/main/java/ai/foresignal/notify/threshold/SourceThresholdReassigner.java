package ai.foresignal.notify.threshold;

import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserOwnedConfigReassigner;
import ai.foresignal.security.user.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * A deleted user's per-source thresholds (prompt SG-003, the {@link UserOwnedConfigReassigner} of R2-005), audit name
 * {@code sourceThresholds}.
 *
 * <p>A threshold is a person's own notification preference keyed on their principal; moving it onto another admin
 * would be meaningless, so reassigning and deleting both remove the rows (like {@code channels} when it has no
 * target). The cooldown state rows are removed with them. The count written to the audit log is the thresholds.
 */
@Component
public class SourceThresholdReassigner implements UserOwnedConfigReassigner {

    private final SourceThresholdRepository thresholds;
    private final SourceNotifyStateRepository state;
    private final UserRepository users;

    /** Creates the reassigner. */
    public SourceThresholdReassigner(SourceThresholdRepository thresholds, SourceNotifyStateRepository state,
            UserRepository users) {
        this.thresholds = thresholds;
        this.state = state;
        this.users = users;
    }

    @Override
    public String name() {
        return "sourceThresholds";
    }

    @Override
    @Transactional
    public int reassign(UUID owner, UUID target) {
        return remove(owner);
    }

    @Override
    @Transactional
    public int delete(UUID owner) {
        return remove(owner);
    }

    private int remove(UUID owner) {
        String principal = users.findById(owner).map(UserEntity::getPrincipalId).orElse(null);
        if (principal == null) {
            return 0;
        }
        int removed = (int) thresholds.deleteByOwnerPrincipalId(principal);
        state.deleteByOwnerPrincipalId(principal);
        return removed;
    }
}
