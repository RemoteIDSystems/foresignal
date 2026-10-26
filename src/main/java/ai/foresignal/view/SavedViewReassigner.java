package ai.foresignal.view;

import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserOwnedConfigReassigner;
import ai.foresignal.security.user.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Moves or deletes a deleted user's saved views (prompt R2-012, the {@link UserOwnedConfigReassigner} of R2-005),
 * audit name {@code savedViews}.
 *
 * <p>Reassigning moves each view to the target admin's principal; with no target (no admin account exists) the views
 * become shared, which a view can safely be: it is a query, not a credential. Deleting removes them, exactly as
 * {@code deleteView} would.
 */
@Component
public class SavedViewReassigner implements UserOwnedConfigReassigner {

    private final UserRepository users;
    private final SavedViewService views;

    /**
     * Creates the reassigner.
     *
     * @param users the user store, mapping a user to their principal
     * @param views the saved view service
     */
    public SavedViewReassigner(UserRepository users, SavedViewService views) {
        this.users = users;
        this.views = views;
    }

    @Override
    public String name() {
        return "savedViews";
    }

    @Override
    public int reassign(UUID owner, UUID target) {
        String from = principal(owner);
        if (from == null) {
            return 0;
        }
        return views.reassignOwner(from, target == null ? null : principal(target));
    }

    @Override
    public int delete(UUID owner) {
        String from = principal(owner);
        return from == null ? 0 : views.deleteOwned(from);
    }

    private String principal(UUID user) {
        return users.findById(user).map(UserEntity::getPrincipalId).orElse(null);
    }
}
