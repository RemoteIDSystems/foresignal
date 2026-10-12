package ai.foresignal.notify.push.provider;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A native push provider ({@code push_provider}, prompt 032): the operator's own APNs auth key or
 * Firebase service account, and the apps it serves.
 *
 * <p>{@code config} holds the non-secret settings ({@code teamId}, {@code keyId},
 * {@code keyEnvironment}, {@code timeSensitive} for APNs; {@code projectId}, {@code clientEmail},
 * {@code tokenUri} for FCM). The secret (the {@code .p8} PEM, or the service account's
 * {@code private_key}) is encrypted with {@code CredentialCipher} into
 * {@code secret_ciphertext}/{@code secret_nonce} and never returned. {@code last_error} is a short
 * code-level description (never a secret, a device token or alert text).
 */
@Entity
@Table(name = "push_provider")
public class PushProviderEntity {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "type", nullable = false, length = 8)
    private String type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "app_ids", nullable = false, columnDefinition = "jsonb")
    private List<String> appIds = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> config = new LinkedHashMap<>();

    @Column(name = "secret_ciphertext")
    private byte[] secretCiphertext;

    @Column(name = "secret_nonce")
    private byte[] secretNonce;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_test_at")
    private Instant lastTestAt;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PushProviderEntity() {
        // for JPA
    }

    /** A provider with an id and a type. */
    public PushProviderEntity(String id, String type) {
        this.id = id;
        this.type = type;
    }

    /** The slug id. */
    public String getId() {
        return id;
    }

    /** {@code apns} or {@code fcm}. */
    public String getType() {
        return type;
    }

    /** Sets the type. */
    public void setType(String type) {
        this.type = type;
    }

    /** The bundle ids or package names served. */
    public List<String> getAppIds() {
        return appIds;
    }

    /** Sets the apps served. */
    public void setAppIds(List<String> appIds) {
        this.appIds = appIds == null ? new ArrayList<>() : new ArrayList<>(appIds);
    }

    /** The non-secret configuration. */
    public Map<String, Object> getConfig() {
        return config;
    }

    /** Sets the non-secret configuration. */
    public void setConfig(Map<String, Object> config) {
        this.config = config == null ? new LinkedHashMap<>() : new LinkedHashMap<>(config);
    }

    /** The encrypted secret, or null. */
    public byte[] getSecretCiphertext() {
        return secretCiphertext;
    }

    /** The secret's nonce, or null. */
    public byte[] getSecretNonce() {
        return secretNonce;
    }

    /** Sets (or, with nulls, clears) the encrypted secret. */
    public void setSecret(byte[] ciphertext, byte[] nonce) {
        this.secretCiphertext = ciphertext;
        this.secretNonce = nonce;
    }

    /** True when a secret is stored. */
    public boolean hasSecret() {
        return secretCiphertext != null && secretNonce != null;
    }

    /** Whether the provider is used. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Sets whether the provider is used. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** The last test, or null. */
    public Instant getLastTestAt() {
        return lastTestAt;
    }

    /** Sets the last test. */
    public void setLastTestAt(Instant lastTestAt) {
        this.lastTestAt = lastTestAt;
    }

    /** The last provider error, or null. */
    public String getLastError() {
        return lastError;
    }

    /** Sets the last provider error. */
    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    /** When the configuration last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets when the configuration last changed. */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
