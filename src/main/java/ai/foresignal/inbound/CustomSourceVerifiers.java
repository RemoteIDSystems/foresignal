package ai.foresignal.inbound;

import ai.foresignal.inbound.signature.VerifierConfig;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads and writes a custom source's signature verifiers: the non-secret configuration as JSON on
 * the row, the secrets as one JSON object encrypted with {@link CredentialCipher} (prompt 024).
 * Secrets are write-only through the API.
 */
@Component
public class CustomSourceVerifiers {

    private static final TypeReference<LinkedHashMap<String, VerifierConfig>> CONFIGS = new TypeReference<>() {};

    private final CredentialCipher cipher;
    private final JsonMapper json;

    /** Creates the helper. */
    public CustomSourceVerifiers(CredentialCipher cipher, JsonMapper json) {
        this.cipher = cipher;
        this.json = json;
    }

    /** The verifier configuration per format; empty when none. */
    public Map<String, VerifierConfig> configs(CustomSourceEntity row) {
        if (row.getVerifiers() == null || row.getVerifiers().isBlank()) {
            return Map.of();
        }
        return json.readValue(row.getVerifiers(), CONFIGS);
    }

    /** The configuration for {@code format}, or null. */
    public VerifierConfig config(CustomSourceEntity row, String format) {
        return configs(row).get(format);
    }

    /** The decrypted secrets per format; empty when none. */
    public Map<String, String> secrets(CustomSourceEntity row) {
        if (row.getVerifierSecretCiphertext() == null) {
            return Map.of();
        }
        return cipher.decryptToMap(new Encrypted(row.getVerifierSecretCiphertext(), row.getVerifierSecretNonce()));
    }

    /**
     * Stores {@code configs} and merges {@code newSecrets} into the stored secrets; secrets of formats
     * no longer configured are dropped.
     */
    public void store(CustomSourceEntity row, Map<String, VerifierConfig> configs, Map<String, String> newSecrets) {
        Map<String, String> secrets = new TreeMap<>(secrets(row));
        secrets.putAll(newSecrets);
        secrets.keySet().retainAll(configs.keySet());
        row.setVerifiers(configs.isEmpty() ? null : json.writeValueAsString(configs));
        if (secrets.isEmpty()) {
            row.setVerifierSecret(null, null);
        } else {
            Encrypted encrypted = cipher.encrypt(secrets);
            row.setVerifierSecret(encrypted.ciphertext(), encrypted.nonce());
        }
    }
}
