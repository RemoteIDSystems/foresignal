package ai.foresignal.notify.inbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One device's read cursor ({@code notification_read_cursor}, prompt 033): every notification with an
 * id at or below {@code read_up_to_id} is read on that device. {@code updated_at} is when the device
 * was last seen (a read or an open inbox stream); cursors unseen for 90 days are deleted. Declared
 * for Hibernate to own the table; {@link InboxService} uses native SQL.
 */
@Entity
@Table(name = "notification_read_cursor")
public class NotificationReadCursorEntity {

    @Id
    @Column(name = "device_key", nullable = false, length = 160)
    private String deviceKey;

    @Column(name = "read_up_to_id", nullable = false)
    private long readUpToId;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected NotificationReadCursorEntity() {}

    /** The device key. */
    public String getDeviceKey() {
        return deviceKey;
    }

    /** The cursor. */
    public long getReadUpToId() {
        return readUpToId;
    }

    /** When last seen. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
