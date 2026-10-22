package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

class CredentialCipherTest {

    private CredentialCipher cipher;
    private Path dataDir;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        this.dataDir = tempDir;
        this.cipher = new CredentialCipher(new SecretKeyStore(tempDir), JsonMapper.builder().build());
    }

    @Test
    void roundTripsBytes() {
        byte[] plaintext = "an-upstream-api-key".getBytes(StandardCharsets.UTF_8);

        Encrypted encrypted = cipher.encrypt(plaintext);

        assertThat(cipher.decrypt(encrypted)).isEqualTo(plaintext);
    }

    @Test
    void roundTripsString() {
        assertThat(cipher.decryptToString(cipher.encrypt("hunter2"))).isEqualTo("hunter2");
    }

    @Test
    @DisplayName("a map of named fields round trips")
    void roundTripsMap() {
        var credentials = new LinkedHashMap<String, String>();
        credentials.put("clientId", "abc123");
        credentials.put("clientSecret", "s3cr3t");

        Map<String, String> decrypted = cipher.decryptToMap(cipher.encrypt(credentials));

        assertThat(decrypted).containsExactlyInAnyOrderEntriesOf(credentials);
    }

    @Test
    void roundTripsEmptyAndUnicode() {
        assertThat(cipher.decryptToString(cipher.encrypt(""))).isEmpty();
        assertThat(cipher.decryptToString(cipher.encrypt("key-日本語-🔑"))).isEqualTo("key-日本語-🔑");
    }

    @Test
    @DisplayName("the ciphertext does not contain the plaintext")
    void ciphertextHidesPlaintext() {
        Encrypted encrypted = cipher.encrypt("recognizable-secret");

        assertThat(new String(encrypted.ciphertext(), StandardCharsets.ISO_8859_1))
                .doesNotContain("recognizable-secret");
    }

    @Test
    @DisplayName("every encryption uses a fresh nonce")
    void usesAFreshNoncePerCall() {
        Encrypted first = cipher.encrypt("same-input");
        Encrypted second = cipher.encrypt("same-input");

        assertThat(first.nonce()).hasSize(CredentialCipher.NONCE_LENGTH_BYTES);
        assertThat(second.nonce()).hasSize(CredentialCipher.NONCE_LENGTH_BYTES);
        assertThat(first.nonce()).isNotEqualTo(second.nonce());
        // Nonce reuse under one key is the classic way to break GCM, so identical plaintext must
        // still produce different ciphertext.
        assertThat(first.ciphertext()).isNotEqualTo(second.ciphertext());
    }

    @Test
    @DisplayName("a tampered ciphertext is rejected, not silently decrypted")
    void rejectsTamperedCiphertext() {
        Encrypted encrypted = cipher.encrypt("an-upstream-api-key");
        byte[] tampered = encrypted.ciphertext().clone();
        tampered[0] ^= 0x01;

        assertThatThrownBy(() -> cipher.decrypt(new Encrypted(tampered, encrypted.nonce())))
                .isInstanceOf(CredentialDecryptionException.class)
                .hasMessageContaining("could not be decrypted");
    }

    @Test
    void rejectsTamperedNonce() {
        Encrypted encrypted = cipher.encrypt("an-upstream-api-key");
        byte[] tampered = encrypted.nonce().clone();
        tampered[tampered.length - 1] ^= 0x01;

        assertThatThrownBy(() -> cipher.decrypt(new Encrypted(encrypted.ciphertext(), tampered)))
                .isInstanceOf(CredentialDecryptionException.class);
    }

    @Test
    void rejectsTruncatedCiphertext() {
        Encrypted encrypted = cipher.encrypt("an-upstream-api-key");
        byte[] truncated = Arrays.copyOf(encrypted.ciphertext(), encrypted.ciphertext().length - 1);

        assertThatThrownBy(() -> cipher.decrypt(new Encrypted(truncated, encrypted.nonce())))
                .isInstanceOf(CredentialDecryptionException.class);
    }

    @Test
    void rejectsNonceOfTheWrongLength() {
        Encrypted encrypted = cipher.encrypt("an-upstream-api-key");

        assertThatThrownBy(() -> cipher.decrypt(new Encrypted(encrypted.ciphertext(), new byte[4])))
                .isInstanceOf(CredentialDecryptionException.class);
    }

    @Test
    @DisplayName("a different key cannot decrypt, and the message says so")
    void rejectsWrongKey(@TempDir Path otherDataDir) {
        Encrypted encrypted = cipher.encrypt("an-upstream-api-key");
        var otherCipher =
                new CredentialCipher(new SecretKeyStore(otherDataDir), JsonMapper.builder().build());

        assertThatThrownBy(() -> otherCipher.decrypt(encrypted))
                .isInstanceOf(CredentialDecryptionException.class)
                .hasMessageContaining(otherDataDir.resolve(SecretKeyStore.KEY_FILE_NAME).toString());
    }

    @Test
    @DisplayName("the failure message never contains the plaintext")
    void failureMessageLeaksNothing() {
        Encrypted encrypted = cipher.encrypt("super-secret-value");
        byte[] tampered = encrypted.ciphertext().clone();
        tampered[1] ^= 0x7F;

        assertThatThrownBy(() -> cipher.decrypt(new Encrypted(tampered, encrypted.nonce())))
                .hasMessageNotContaining("super-secret-value");
    }

    @Test
    @DisplayName("decrypting something that is not a JSON object fails cleanly")
    void rejectsNonJsonMap() {
        Encrypted encrypted = cipher.encrypt("not-json");

        assertThatThrownBy(() -> cipher.decryptToMap(encrypted))
                .isInstanceOf(CredentialDecryptionException.class)
                .hasMessageContaining("not a JSON object");
    }

    @Test
    @DisplayName("a large credential round trips")
    void roundTripsLargeValue() {
        String large = "x".repeat(100_000);

        assertThat(cipher.decryptToString(cipher.encrypt(large))).isEqualTo(large);
    }

    @Test
    void keyFileIsWhereTheStoreSaysItIs() {
        assertThat(new SecretKeyStore(dataDir).keyFile().getParent()).isEqualTo(dataDir);
    }
}
