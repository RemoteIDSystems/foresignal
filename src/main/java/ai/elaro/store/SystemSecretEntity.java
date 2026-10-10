package ai.elaro.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Opaque secret bytes keyed by name, for example {@code admin_token_hash} (Architecture §7.2).
 *
 * <p>Only hashes and encrypted material belong here. The AES key that protects credentials is
 * deliberately kept in a file on the data volume instead.
 */
@Entity
@Table(name = "system_secret")
public class SystemSecretEntity {

    @Id
    @Column(name = "key", length = 128, nullable = false)
    private String key;

    @Column(name = "value", nullable = false)
    private byte[] value;

    protected SystemSecretEntity() {
        // for JPA
    }

    public SystemSecretEntity(String key, byte[] value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }
}
