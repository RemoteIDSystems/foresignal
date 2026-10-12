package ai.foresignal.inbound.edp;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * {@code selfRegisterExternalSource} in one transaction (prompt 024): the registration token's use
 * is counted only when the provider is actually registered, {@code pending}, limited to the token's
 * {@code sourceIdPrefix}.
 */
@Service
public class EdpSelfRegistration {

    private final RegistrationTokenService tokens;
    private final EdpManifestService manifests;

    /** Creates the service. */
    public EdpSelfRegistration(RegistrationTokenService tokens, EdpManifestService manifests) {
        this.tokens = tokens;
        this.manifests = manifests;
    }

    /** Registers {@code manifest} with the presented registration token. */
    @Transactional
    public EdpManifestService.Registered register(String presentedToken, JsonNode manifest) {
        RegistrationTokenEntity token = tokens.use(presentedToken);
        return manifests.register(manifest, new EdpManifestService.RegisterOptions(false,
                ExternalSourceEntity.VIA_TOKEN, false, "registration-token:" + token.getPrefix(),
                token.getSourceIdPrefix()));
    }
}
