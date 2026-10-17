package ai.foresignal.collab;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One notification preference of a principal (table {@code principal_notification}, prompt 130): which personal
 * events ({@code mention}, {@code share}, {@code report}, {@code chronicle}, {@code assessment},
 * {@code comment-reply}) reach which 018 channel the principal owns, with optional quiet hours (stored on the
 * channel, where 018's worker applies them).
 */
@Entity
@Table(name = "principal_notification", indexes = @Index(name = "principal_notification_principal_idx",
        columnList = "principal_id"))
public class PrincipalNotificationEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "principal_id", length = 64, nullable = false)
    private String principalId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_types", nullable = false)
    private List<String> eventTypes = new ArrayList<>();

    @Column(name = "channel_id", nullable = false)
    private UUID channelId;

    @Column(name = "language", length = 35)
    private String language;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quiet_hours")
    private Map<String, Object> quietHours;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected PrincipalNotificationEntity() {}

    /**
     * A new preference.
     *
     * @param id its ULID
     * @param principalId the owning principal
     * @param channelId the principal-owned 018 channel
     * @param now the creation time
     */
    public PrincipalNotificationEntity(String id, String principalId, UUID channelId, Instant now) {
        this.id = id;
        this.principalId = principalId;
        this.channelId = channelId;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The owning principal. */
    public String getPrincipalId() {
        return principalId;
    }

    /** The personal event types it carries. */
    public List<String> getEventTypes() {
        return eventTypes;
    }

    /** Sets the event types. */
    public void setEventTypes(List<String> value) {
        this.eventTypes = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** The 018 channel. */
    public UUID getChannelId() {
        return channelId;
    }

    /** The BCP 47 language of the notifications, or null. */
    public String getLanguage() {
        return language;
    }

    /** Sets the language. */
    public void setLanguage(String value) {
        this.language = value;
    }

    /** The quiet hours, or null. */
    public Map<String, Object> getQuietHours() {
        return quietHours;
    }

    /** Sets the quiet hours. */
    public void setQuietHours(Map<String, Object> value) {
        this.quietHours = value;
    }

    /** True when deliveries are made. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Enables or disables it. */
    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets the change time. */
    public void setUpdatedAt(Instant value) {
        this.updatedAt = value;
    }
}
