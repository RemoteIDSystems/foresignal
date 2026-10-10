package ai.elaro.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Encrypts upstream credentials at rest with AES-256-GCM (Architecture §7.2, NFR-7).
 *
 * <p>GCM authenticates as well as encrypts, so tampering with either the ciphertext or the nonce is
 * detected on decryption rather than producing garbage plaintext. A fresh random nonce per
 * encryption is required for GCM's security, so {@link #encrypt} never reuses one.
 */
@Component
public class CredentialCipher {

    /** The nonce length GCM is specified for, and the only one that is efficient. */
    static final int NONCE_LENGTH_BYTES = 12;

    /** 128-bit authentication tag, the GCM maximum. */
    static final int TAG_LENGTH_BITS = 128;

    static final String TRANSFORMATION = "AES/GCM/NoPadding";

    private static final TypeReference<Map<String, String>> STRING_MAP = new TypeReference<>() {};

    private final SecretKeyStore keyStore;
    private final JsonMapper jsonMapper;
    private final SecureRandom random = new SecureRandom();

    public CredentialCipher(SecretKeyStore keyStore, JsonMapper jsonMapper) {
        this.keyStore = keyStore;
        this.jsonMapper = jsonMapper;
    }

    /** Encrypts {@code plaintext} under a freshly generated nonce. */
    public Encrypted encrypt(byte[] plaintext) {
        byte[] nonce = new byte[NONCE_LENGTH_BYTES];
        random.nextBytes(nonce);
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keyStore.key(), new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            return new Encrypted(cipher.doFinal(plaintext), nonce);
        } catch (GeneralSecurityException e) {
            // A configuration or JVM problem, not attacker-controlled input.
            throw new IllegalStateException("Could not encrypt a credential: " + e.getMessage(), e);
        }
    }

    /** Encrypts a UTF-8 string. */
    public Encrypted encrypt(String plaintext) {
        return encrypt(plaintext.getBytes(StandardCharsets.UTF_8));
    }

    /** Encrypts a set of named credential fields as JSON. */
    public Encrypted encrypt(Map<String, String> credentials) {
        return encrypt(jsonMapper.writeValueAsString(credentials));
    }

    /**
     * Decrypts and authenticates.
     *
     * @throws CredentialDecryptionException if the ciphertext or nonce was altered, or the key no
     *     longer matches
     */
    public byte[] decrypt(Encrypted encrypted) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    keyStore.key(),
                    new GCMParameterSpec(TAG_LENGTH_BITS, encrypted.nonce()));
            return cipher.doFinal(encrypted.ciphertext());
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new CredentialDecryptionException(
                    "Stored credential could not be decrypted. The data may have been altered, or "
                            + "the key in "
                            + keyStore.keyFile()
                            + " is not the one it was encrypted with.",
                    e);
        }
    }

    /** Decrypts to a UTF-8 string. */
    public String decryptToString(Encrypted encrypted) {
        return new String(decrypt(encrypted), StandardCharsets.UTF_8);
    }

    /** Decrypts a set of named credential fields. */
    public Map<String, String> decryptToMap(Encrypted encrypted) {
        String json = decryptToString(encrypted);
        try {
            return jsonMapper.readValue(json, STRING_MAP);
        } catch (JacksonException e) {
            throw new CredentialDecryptionException(
                    "Stored credential decrypted but is not a JSON object of strings.", e);
        }
    }
}
