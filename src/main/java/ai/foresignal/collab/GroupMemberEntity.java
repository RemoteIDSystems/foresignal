package ai.foresignal.collab;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * A principal's membership of a group (table {@code group_member}, prompt 130): {@code member} or {@code manager}.
 * Managers (and admins) change the group and its members.
 */
@Entity
@Table(name = "group_member", uniqueConstraints = @UniqueConstraint(name = "group_member_key",
        columnNames = {"group_id", "principal_id"}), indexes = {
                @Index(name = "group_member_principal_idx", columnList = "principal_id")})
public class GroupMemberEntity {

    /** The plain member role. */
    public static final String MEMBER = "member";

    /** The manager role. */
    public static final String MANAGER = "manager";

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "group_id", length = 26, nullable = false)
    private String groupId;

    @Column(name = "principal_id", length = 64, nullable = false)
    private String principalId;

    @Column(name = "role", length = 16, nullable = false)
    private String role;

    @Column(name = "added_by", length = 64)
    private String addedBy;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    /** For JPA. */
    protected GroupMemberEntity() {}

    /**
     * A new membership.
     *
     * @param id its ULID
     * @param groupId the group
     * @param principalId the member
     * @param role {@code member} or {@code manager}
     * @param addedBy who added it, or null
     * @param now when
     */
    public GroupMemberEntity(String id, String groupId, String principalId, String role, String addedBy,
            Instant now) {
        this.id = id;
        this.groupId = groupId;
        this.principalId = principalId;
        this.role = role;
        this.addedBy = addedBy;
        this.addedAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The group. */
    public String getGroupId() {
        return groupId;
    }

    /** The member. */
    public String getPrincipalId() {
        return principalId;
    }

    /** {@code member} or {@code manager}. */
    public String getRole() {
        return role;
    }

    /** Sets the role. */
    public void setRole(String value) {
        this.role = value;
    }

    /** Who added it, or null. */
    public String getAddedBy() {
        return addedBy;
    }

    /** When it was added. */
    public Instant getAddedAt() {
        return addedAt;
    }
}
