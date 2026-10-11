package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * An outbound HTTP endpoint that receives alerts.
 *
 * <p>The shared secret used to sign deliveries is encrypted with the same AES-256-GCM key as
 * upstream credentials (Architecture §7.2), so it is never readable from a database dump alone.
 */
@Entity
@Table(name = "webhook")
public class WebhookEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "secret_ciphertext")
    private byte[] secretCiphertext;

    @Column(name = "secret_nonce")
    private byte[] secretNonce;

    /** Which rules feed this webhook; null means every rule. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rule_ids")
    private List<UUID> ruleIds;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected WebhookEntity() {
        // for JPA
    }

    public WebhookEntity(UUID id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public byte[] getSecretCiphertext() {
        return secretCiphertext;
    }

    public void setSecretCiphertext(byte[] secretCiphertext) {
        this.secretCiphertext = secretCiphertext;
    }

    public byte[] getSecretNonce() {
        return secretNonce;
    }

    public void setSecretNonce(byte[] secretNonce) {
        this.secretNonce = secretNonce;
    }

    public List<UUID> getRuleIds() {
        return ruleIds;
    }

    public void setRuleIds(List<UUID> ruleIds) {
        this.ruleIds = ruleIds;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
