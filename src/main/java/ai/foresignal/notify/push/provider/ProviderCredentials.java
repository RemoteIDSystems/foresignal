package ai.foresignal.notify.push.provider;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;

/**
 * The decrypted, ready-to-use credentials of one enabled provider (prompt 032). Built per send by
 * {@link PushProviderService}; never logged ({@code toString} omits the key).
 */
public sealed interface ProviderCredentials {

    /** The provider id. */
    String providerId();

    /**
     * Changes whenever the provider's configuration or key changes ({@code id:updatedAt}), so
     * caches of minted tokens and HTTP clients keyed by it never serve a stale key.
     */
    String fingerprint();

    /**
     * An APNs token-based connection: an ES256 auth key of an Apple team.
     *
     * @param providerId the provider id
     * @param teamId the 10-character Team ID ({@code iss})
     * @param keyId the 10-character Key ID ({@code kid})
     * @param keyEnvironment {@code sandbox}, {@code production} or {@code both} (legacy keys)
     * @param timeSensitive whether the app has the Time Sensitive capability
     * @param key the P-256 private key
     * @param fingerprint see {@link ProviderCredentials#fingerprint()}
     */
    record Apns(String providerId, String teamId, String keyId, String keyEnvironment, boolean timeSensitive,
            ECPrivateKey key, String fingerprint) implements ProviderCredentials {

        /** True when the key may be used in {@code environment}. */
        public boolean covers(String environment) {
            return "both".equals(keyEnvironment) || keyEnvironment.equals(environment);
        }

        @Override
        public String toString() {
            return "Apns[" + providerId + ", " + keyId + "]";
        }
    }

    /**
     * An FCM HTTP v1 sender: a Firebase service account.
     *
     * @param providerId the provider id
     * @param projectId the Firebase project id
     * @param clientEmail the service account's email ({@code iss})
     * @param tokenUri the OAuth 2.0 token endpoint ({@code aud})
     * @param key the service account's RSA private key
     * @param fingerprint see {@link ProviderCredentials#fingerprint()}
     */
    record Fcm(String providerId, String projectId, String clientEmail, String tokenUri, RSAPrivateKey key,
            String fingerprint) implements ProviderCredentials {

        @Override
        public String toString() {
            return "Fcm[" + providerId + ", " + projectId + "]";
        }
    }
}
