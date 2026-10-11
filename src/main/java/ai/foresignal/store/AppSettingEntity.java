package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One typed setting, stored as jsonb.
 *
 * <p>Foresignal has no UI, so everything configurable other than the bootstrap environment variables
 * lives here and is managed through the API.
 */
@Entity
@Table(name = "app_setting")
public class AppSettingEntity {

    @Id
    @Column(name = "key", length = 128, nullable = false)
    private String key;

    /** Serialized settings value; shape depends on the key. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "value", nullable = false)
    private String value;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AppSettingEntity() {
        // for JPA
    }

    public AppSettingEntity(String key, String value, Instant updatedAt) {
        this.key = key;
        this.value = value;
        this.updatedAt = updatedAt;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
