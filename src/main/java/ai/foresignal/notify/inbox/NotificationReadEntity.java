package ai.foresignal.notify.inbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * A sparse read above a device's cursor ({@code notification_read}, prompt 033). Entries at or below
 * the cursor are deleted whenever it moves; at most 500 are kept per device, the oldest dropped first
 * (which only makes them unread again, never read). Declared for Hibernate to own the table.
 */
@Entity
@Table(name = "notification_read")
@IdClass(NotificationReadEntity.Key.class)
public class NotificationReadEntity {

    @Id
    @Column(name = "device_key", nullable = false, length = 160)
    private String deviceKey;

    @Id
    @Column(name = "notification_id", nullable = false)
    private long notificationId;

    /** For JPA. */
    protected NotificationReadEntity() {}

    /** The device key. */
    public String getDeviceKey() {
        return deviceKey;
    }

    /** The notification. */
    public long getNotificationId() {
        return notificationId;
    }

    /** The composite key. */
    public static class Key implements Serializable {

        private static final long serialVersionUID = 1L;

        private String deviceKey;
        private long notificationId;

        /** For JPA. */
        public Key() {}

        @Override
        public boolean equals(Object o) {
            return o instanceof Key k && notificationId == k.notificationId && Objects.equals(deviceKey, k.deviceKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(deviceKey, notificationId);
        }
    }
}
