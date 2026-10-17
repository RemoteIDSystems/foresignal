package ai.foresignal.collab;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/** A tag assigned to an object reference (table {@code tag_assignment}, prompt 130). */
@Entity
@Table(name = "tag_assignment", uniqueConstraints = @UniqueConstraint(name = "tag_assignment_key",
        columnNames = {"tag_id", "object_ref"}), indexes = {
                @Index(name = "tag_assignment_object_idx", columnList = "object_ref")})
public class TagAssignmentEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "tag_id", length = 26, nullable = false)
    private String tagId;

    @Column(name = "object_ref", length = 1024, nullable = false)
    private String objectRef;

    @Column(name = "assigned_by", length = 64)
    private String assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    /** For JPA. */
    protected TagAssignmentEntity() {}

    /**
     * A new assignment.
     *
     * @param id its ULID
     * @param tagId the tag
     * @param objectRef the canonical object reference
     * @param assignedBy the assigning principal
     * @param now when
     */
    public TagAssignmentEntity(String id, String tagId, String objectRef, String assignedBy, Instant now) {
        this.id = id;
        this.tagId = tagId;
        this.objectRef = objectRef;
        this.assignedBy = assignedBy;
        this.assignedAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The tag. */
    public String getTagId() {
        return tagId;
    }

    /** The object reference. */
    public String getObjectRef() {
        return objectRef;
    }

    /** The assigning principal, or null. */
    public String getAssignedBy() {
        return assignedBy;
    }

    /** When it was assigned. */
    public Instant getAssignedAt() {
        return assignedAt;
    }
}
