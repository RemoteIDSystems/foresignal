package ai.foresignal.source.group;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A source group ({@code source_group}, prompt SG-002): a named set of member sources (custom or catalog) a user
 * owns, with its own correlation and notification threshold. A group scopes an edition
 * ({@code BriefingScope.sourceGroupIds}) and a trigger rule ({@code trigger_rule.source_group_id}); its member
 * sources bound the scope. Owned by a v2.0 user ({@code owner_user_id}) and deleted with that user.
 */
@Entity
@Table(name = "source_group", indexes = @Index(name = "source_group_owner_user_idx", columnList = "owner_user_id, id"))
public class SourceGroupEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "source_ids", nullable = false, columnDefinition = "jsonb")
    private List<String> sourceIds = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "kinds", nullable = false, columnDefinition = "jsonb")
    private List<String> kinds = new ArrayList<>();

    /** The {@code GroupCorrelation} object, or null for the defaults. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "correlation", columnDefinition = "jsonb")
    private Map<String, Object> correlation;

    /** The {@code GroupThreshold} object, or null for the defaults. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notification_threshold", columnDefinition = "jsonb")
    private Map<String, Object> notificationThreshold;

    @Column(name = "edition_id")
    private UUID editionId;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected SourceGroupEntity() {}

    /** A new group with id {@code id} owned by {@code ownerUserId}. */
    public SourceGroupEntity(String id, UUID ownerUserId) {
        this.id = id;
        this.ownerUserId = ownerUserId;
    }

    /** The group id (server-generated). */
    public String getId() {
        return id;
    }

    /** The owning user. */
    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    /** The display name. */
    public String getName() {
        return name;
    }

    /** Sets the display name. */
    public void setName(String name) {
        this.name = name;
    }

    /** The description, or null. */
    public String getDescription() {
        return description;
    }

    /** Sets the description. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** The member source ids. */
    public List<String> getSourceIds() {
        return sourceIds;
    }

    /** Sets the member source ids. */
    public void setSourceIds(List<String> sourceIds) {
        this.sourceIds = sourceIds == null ? new ArrayList<>() : new ArrayList<>(sourceIds);
    }

    /** The record-kind filter (empty for all). */
    public List<String> getKinds() {
        return kinds;
    }

    /** Sets the record-kind filter. */
    public void setKinds(List<String> kinds) {
        this.kinds = kinds == null ? new ArrayList<>() : new ArrayList<>(kinds);
    }

    /** The {@code GroupCorrelation} object, or null. */
    public Map<String, Object> getCorrelation() {
        return correlation;
    }

    /** Sets the correlation object. */
    public void setCorrelation(Map<String, Object> correlation) {
        this.correlation = correlation == null ? null : new LinkedHashMap<>(correlation);
    }

    /** The {@code GroupThreshold} object, or null. */
    public Map<String, Object> getNotificationThreshold() {
        return notificationThreshold;
    }

    /** Sets the notification threshold object. */
    public void setNotificationThreshold(Map<String, Object> notificationThreshold) {
        this.notificationThreshold = notificationThreshold == null ? null : new LinkedHashMap<>(notificationThreshold);
    }

    /** The edition generated for this group, or null. */
    public UUID getEditionId() {
        return editionId;
    }

    /** Sets the edition generated for this group; null for none. */
    public void setEditionId(UUID editionId) {
        this.editionId = editionId;
    }

    /** When the group was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets the creation time. */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /** When the group last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets the update time. */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
