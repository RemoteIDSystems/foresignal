package ai.foresignal.source;

import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import ai.foresignal.store.SourceCredentialRepository;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The only way to read a decrypted upstream credential.
 *
 * <p><strong>Not for controllers.</strong> This bean exists so that "credentials are never returned
 * by a public API method" is a structural property rather than a convention: {@link SourceService}
 * exposes only {@code credentialsSet}, and the decrypting method lives here, on a separate bean
 * whose single intended consumer is the {@code collect} package when it builds a
 * {@link ai.foresignal.source.spi.FetchContext}.
 *
 * <p>Keeping it separate also makes misuse visible in review: a controller injecting
 * {@code CredentialsAccess} is obviously wrong in a way that a controller calling
 * {@code sourceService.credentials(id)} would not be.
 */
@Component
public class CredentialsAccess {

    private final SourceCredentialRepository credentials;
    private final CredentialCipher cipher;

    public CredentialsAccess(SourceCredentialRepository credentials, CredentialCipher cipher) {
        this.credentials = credentials;
        this.cipher = cipher;
    }

    /**
     * The decrypted credentials for {@code sourceId}, or an empty map when none are stored.
     *
     * @throws ai.foresignal.security.CredentialDecryptionException when the stored bytes do not decrypt,
     *     which means the key file changed or the row was tampered with — an operator problem, not a
     *     caller's
     */
    @Transactional(readOnly = true)
    public Map<String, String> forSource(String sourceId) {
        return credentials
                .findById(sourceId)
                .map(entity -> cipher.decryptToMap(new Encrypted(entity.getCiphertext(), entity.getNonce())))
                .orElseGet(Map::of);
    }

    /** True when credentials are stored for {@code sourceId}. Safe to expose. */
    @Transactional(readOnly = true)
    public boolean isSet(String sourceId) {
        return credentials.existsById(sourceId);
    }
}
