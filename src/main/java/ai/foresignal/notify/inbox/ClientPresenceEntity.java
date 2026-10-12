package ai.foresignal.notify.inbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One device's presence ({@code client_presence}, prompt 033): {@code kind} {@code desktop},
 * {@code mobile} or {@code web}; {@code state} {@code active}, {@code idle} or {@code background};
 * valid until {@code expires_at}. Declared for Hibernate to own the table; {@link PresenceService}
 * uses native SQL.
 */
@Entity
@Table(name = "client_presence")
public class ClientPresenceEntity {

    @Id
    @Column(name = "device_key", nullable = false, length = 160)
    private String deviceKey;

    @Column(name = "kind", nullable = false, length = 16)
    private String kind;

    @Column(name = "state", nullable = false, length = 16)
    private String state;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected ClientPresenceEntity() {}

    /** The device key. */
    public String getDeviceKey() {
        return deviceKey;
    }

    /** The kind. */
    public String getKind() {
        return kind;
    }

    /** The state. */
    public String getState() {
        return state;
    }

    /** When it expires. */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /** When last updated. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
