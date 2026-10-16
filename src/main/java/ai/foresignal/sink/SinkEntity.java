package ai.foresignal.sink;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** An outbound sink (prompt 110, table {@code sink}). Secrets are encrypted and never returned. */
@Entity
@Table(name = "sink")
public class SinkEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "type", length = 32, nullable = false)
    private String type;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config", columnDefinition = "jsonb default '{}'::jsonb")
    private Map<String, Object> config = new LinkedHashMap<>();

    @Column(name = "secret_ciphertext")
    private byte[] secretCiphertext;

    @Column(name = "secret_nonce")
    private byte[] secretNonce;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filter", columnDefinition = "jsonb default '{}'::jsonb")
    private Map<String, Object> filter = new LinkedHashMap<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** JPA. */
    protected SinkEntity() {}

    /** Creates a sink. */
    public SinkEntity(String id, String type, String name, Instant createdAt) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config == null ? new LinkedHashMap<>() : config;
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

    public Map<String, Object> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, Object> filter) {
        this.filter = filter == null ? new LinkedHashMap<>() : filter;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
