package ai.foresignal.collab;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * One access-list grant (table {@code object_acl}, prompt 130): a role on a work product for a principal
 * ({@code principal:{id}}), a group ({@code group:{id}}) or the whole instance ({@code instance}), optionally until
 * {@code expires_at}. One grant per object and grantee.
 */
@Entity
@Table(name = "object_acl", uniqueConstraints = @UniqueConstraint(name = "object_acl_object_grantee_key",
        columnNames = {"object_ref", "grantee"}), indexes = {
                @Index(name = "object_acl_grantee_idx", columnList = "grantee, granted_at")})
public class ObjectAclEntity {

    /** The grantee of an instance-wide grant. */
    public static final String INSTANCE = "instance";

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "object_ref", length = 128, nullable = false)
    private String objectRef;

    @Column(name = "grantee", length = 128, nullable = false)
    private String grantee;

    @Column(name = "role", length = 16, nullable = false)
    private String role;

    @Column(name = "granted_by", length = 64)
    private String grantedBy;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    /** For JPA. */
    protected ObjectAclEntity() {}

    /**
     * A new grant.
     *
     * @param id its ULID
     * @param objectRef the work product
     * @param grantee {@code principal:{id}}, {@code group:{id}} or {@code instance}
     * @param role the role
     * @param grantedBy the granting principal
     * @param grantedAt when
     */
    public ObjectAclEntity(String id, String objectRef, String grantee, AclRole role, String grantedBy,
            Instant grantedAt) {
        this.id = id;
        this.objectRef = objectRef;
        this.grantee = grantee;
        this.role = role.wireValue();
        this.grantedBy = grantedBy;
        this.grantedAt = grantedAt;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The work product. */
    public String getObjectRef() {
        return objectRef;
    }

    /** The grantee. */
    public String getGrantee() {
        return grantee;
    }

    /** The role. */
    public AclRole getRole() {
        return AclRole.fromWire(role);
    }

    /** Sets the role. */
    public void setRole(AclRole value) {
        this.role = value.wireValue();
    }

    /** The granting principal, or null. */
    public String getGrantedBy() {
        return grantedBy;
    }

    /** Sets the granting principal. */
    public void setGrantedBy(String value) {
        this.grantedBy = value;
    }

    /** When it was granted. */
    public Instant getGrantedAt() {
        return grantedAt;
    }

    /** Sets the grant time. */
    public void setGrantedAt(Instant value) {
        this.grantedAt = value;
    }

    /** When it expires, or null. */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /** Sets the expiry. */
    public void setExpiresAt(Instant value) {
        this.expiresAt = value;
    }

    /** True when the grant is in force at {@code now}. */
    public boolean live(Instant now) {
        return expiresAt == null || expiresAt.isAfter(now);
    }
}
