package ai.foresignal.source.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One source in a user's view ({@code user_source_pref}, prompt R2-010): whether the user sees it, and an optional
 * label and color. A source with no row is in the view with no overrides. The rows are deleted by the database with
 * the user.
 */
@Entity
@Table(name = "user_source_pref")
@IdClass(UserSourcePrefEntity.Key.class)
public class UserSourcePrefEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Id
    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected UserSourcePrefEntity() {}

    /**
     * A preference.
     *
     * @param userId the user
     * @param sourceId the source
     * @param enabled whether the user sees the source
     * @param displayName the label override, or null
     * @param color the color override ({@code #RRGGBB}), or null
     * @param updatedAt when it was written
     */
    public UserSourcePrefEntity(UUID userId, String sourceId, boolean enabled, String displayName, String color,
            Instant updatedAt) {
        this.userId = userId;
        this.sourceId = sourceId;
        this.enabled = enabled;
        this.displayName = displayName;
        this.color = color;
        this.updatedAt = updatedAt;
    }

    /** The user. */
    public UUID getUserId() {
        return userId;
    }

    /** The source. */
    public String getSourceId() {
        return sourceId;
    }

    /** Whether the user sees the source. */
    public boolean isEnabled() {
        return enabled;
    }

    /** The label override, or null. */
    public String getDisplayName() {
        return displayName;
    }

    /** The color override, or null. */
    public String getColor() {
        return color;
    }

    /** When the row was written. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** The composite key. */
    public static class Key implements Serializable {

        private static final long serialVersionUID = 1L;

        private UUID userId;
        private String sourceId;

        /** For JPA. */
        public Key() {}

        /**
         * A key.
         *
         * @param userId the user
         * @param sourceId the source
         */
        public Key(UUID userId, String sourceId) {
            this.userId = userId;
            this.sourceId = sourceId;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Key k && Objects.equals(userId, k.userId) && Objects.equals(sourceId, k.sourceId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, sourceId);
        }
    }
}
