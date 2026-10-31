package ai.foresignal.notify.threshold;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * The last time a notification for {@code (owner_principal_id, source_id)} actually went out
 * ({@code source_notify_state}, prompt SG-003), for cooldown enforcement. Separate from
 * {@link SourceThresholdEntity} so {@code getMySourceThresholds} returns only the entries the user set.
 */
@Entity
@Table(name = "source_notify_state")
@IdClass(SourceThresholdId.class)
public class SourceNotifyStateEntity {

    @Id
    @Column(name = "owner_principal_id", length = 64, nullable = false)
    private String ownerPrincipalId;

    @Id
    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_notified_at", nullable = false)
    private Instant lastNotifiedAt;

    /** For JPA. */
    protected SourceNotifyStateEntity() {}

    /** A new state row for {@code ownerPrincipalId} and {@code sourceId}. */
    public SourceNotifyStateEntity(String ownerPrincipalId, String sourceId) {
        this.ownerPrincipalId = ownerPrincipalId;
        this.sourceId = sourceId;
    }

    /** When a notification for this source last went out. */
    public Instant getLastNotifiedAt() {
        return lastNotifiedAt;
    }

    /** Sets the last-notified time. */
    public void setLastNotifiedAt(Instant lastNotifiedAt) {
        this.lastNotifiedAt = lastNotifiedAt;
    }
}
