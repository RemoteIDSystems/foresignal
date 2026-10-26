package ai.foresignal.source.view;

import ai.foresignal.security.user.UserOwnedConfigReassigner;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * A deleted user's source view (prompt R2-010, the {@link UserOwnedConfigReassigner} of R2-005).
 *
 * <p>A view is a person's own reading preference, not configuration anyone else depends on, and moving it onto the
 * target admin would overwrite the admin's own view. So reassigning and deleting both remove the rows; the count is
 * still written to the audit log under {@code sourceViews}. The collection union no longer counts the user, so a source
 * only they wanted stops being pulled at the next reconcile.
 */
@Component
public class UserSourcePrefReassigner implements UserOwnedConfigReassigner {

    private final UserSourceViewService views;

    /**
     * Creates the reassigner.
     *
     * @param views the source view service
     */
    public UserSourcePrefReassigner(UserSourceViewService views) {
        this.views = views;
    }

    @Override
    public String name() {
        return "sourceViews";
    }

    @Override
    public int reassign(UUID owner, UUID target) {
        return views.deleteView(owner);
    }

    @Override
    public int delete(UUID owner) {
        return views.deleteView(owner);
    }
}
