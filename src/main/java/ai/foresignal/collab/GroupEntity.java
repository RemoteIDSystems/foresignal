package ai.foresignal.collab;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * A named set of principals used as an access-list grantee (table {@code principal_group}, prompt 130; {@code group}
 * is an SQL keyword).
 */
@Entity
@Table(name = "principal_group")
public class GroupEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "created_by", length = 64, nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected GroupEntity() {}

    /**
     * A new group.
     *
     * @param id its ULID
     * @param name its name
     * @param createdBy the creating principal
     * @param now the creation time
     */
    public GroupEntity(String id, String name, String createdBy, Instant now) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The name. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String value) {
        this.name = value;
    }

    /** The description, or null. */
    public String getDescription() {
        return description;
    }

    /** Sets the description. */
    public void setDescription(String value) {
        this.description = value;
    }

    /** The creating principal. */
    public String getCreatedBy() {
        return createdBy;
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
