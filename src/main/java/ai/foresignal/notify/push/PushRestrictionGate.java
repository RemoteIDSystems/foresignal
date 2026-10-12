package ai.foresignal.notify.push;

import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.security.ApiTokenEntity;
import ai.foresignal.security.ApiTokenRepository;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.TokenFilter;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.stream.sse.NotificationFilter;
import ai.foresignal.trigger.AlertStore;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Applies read restrictions to push fan-out (prompt 035): a Web Push subscription (031) or native
 * device (032) registered by a restricted token only receives notifications that pass that token's
 * restriction, exactly as its inbox and notification stream would show them
 * ({@link NotificationFilter}: alert notifications whose source, kind and record-snapshot geometry
 * are inside the restriction; nothing else). A target registered without a token is held to the
 * anonymous profile.
 */
@Component
public class PushRestrictionGate {

    private final ApiTokenRepository tokens;
    private final AppSettingService settings;
    private final AlertStore alerts;

    /** Creates the gate. */
    public PushRestrictionGate(ApiTokenRepository tokens, AppSettingService settings, AlertStore alerts) {
        this.tokens = tokens;
        this.settings = settings;
        this.alerts = alerts;
    }

    /** True when the target registered by {@code tokenId} (null for none) may receive {@code message}. */
    public boolean allows(UUID tokenId, NotificationMessage message) {
        ReadRestriction restriction = restriction(tokenId);
        if (!restriction.hasDataRestrictions()) {
            return true;
        }
        return new NotificationFilter(Set.of(), restriction,
                id -> alerts.find(id).map(a -> a.recordSnapshot()).orElse(null)).matches(message);
    }

    /** The effective restriction of {@code tokenId}: its token's, or the anonymous profile's. */
    ReadRestriction restriction(UUID tokenId) {
        if (tokenId == null) {
            SecuritySettings security =
                    settings.getOrDefault(SettingKeys.SECURITY, SecuritySettings.class, SecuritySettings::defaults);
            return ReadRestriction.of(security.effectiveAnonymous(), true);
        }
        Optional<ApiTokenEntity> token = tokens.findById(tokenId);
        return token.map(t -> TokenFilter.restrictionOf(AuthenticatedToken.of(t))).orElse(ReadRestriction.NONE);
    }
}
