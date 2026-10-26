package ai.foresignal.preference;

import ai.foresignal.security.user.UserOwnedConfigReassigner;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * A deleted user's stored preferences (prompt R2-012, the {@link UserOwnedConfigReassigner} of R2-005), audit name
 * {@code preferences}.
 *
 * <p>Locale, theme and UI layout are one person's own; moving them onto the target admin would overwrite the admin's
 * own. So reassigning and deleting both remove the row. The timezone lives on the principal and the notification
 * switches on the notification preferences, which go their own ways.
 */
@Component
public class UserPreferencesReassigner implements UserOwnedConfigReassigner {

    private final UserPreferencesService preferences;

    /**
     * Creates the reassigner.
     *
     * @param preferences the preference service
     */
    public UserPreferencesReassigner(UserPreferencesService preferences) {
        this.preferences = preferences;
    }

    @Override
    public String name() {
        return "preferences";
    }

    @Override
    public int reassign(UUID owner, UUID target) {
        return preferences.delete(owner);
    }

    @Override
    public int delete(UUID owner) {
        return preferences.delete(owner);
    }
}
