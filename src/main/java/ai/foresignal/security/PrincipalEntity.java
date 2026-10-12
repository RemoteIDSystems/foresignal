package ai.foresignal.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A person, agent or service that holds tokens ({@code principal}, v1.3, prompt 023).
 *
 * <p>Every {@link ApiTokenEntity} belongs to one principal: the bootstrap admin token to
 * {@code owner}, a token created without a principal to a new one named after the token. Audit
 * entries and ownership fields refer to the principal, not the token. There are no passwords:
 * principals authenticate only with their tokens and pairing codes.
 *
 * <p>Prompt 130 adds groups, profiles (email, avatar, timezone, organization), notification
 * preferences and access lists; this entity keeps only the columns 023 needs and is open for
 * those (Hibernate {@code update} adds new nullable columns).
 */
@Entity
@Table(name = "principal")
public class PrincipalEntity {

    /** The handle of the principal that owns the bootstrap admin token. */
    public static final String OWNER_HANDLE = "owner";

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "handle", length = 32, nullable = false, unique = true)
    private String handle;

    @Column(name = "display_name", length = 200, nullable = false)
    private String displayName;

    @Column(name = "kind", length = 16, nullable = false)
    private String kind;

    @Column(name = "disabled", nullable = false)
    private boolean disabled;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PrincipalEntity() {
        // for JPA
    }

    /** A new, enabled principal. */
    public PrincipalEntity(String id, String handle, String displayName, PrincipalKind kind, Instant now) {
        this.id = id;
        this.handle = handle;
        this.displayName = displayName;
        this.kind = kind.wireValue();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** The ULID. */
    public String getId() {
        return id;
    }

    /** The unique handle ({@code ^[a-z0-9][a-z0-9_.-]{1,31}$}). */
    public String getHandle() {
        return handle;
    }

    /** The display name. */
    public String getDisplayName() {
        return displayName;
    }

    /** {@code person}, {@code agent} or {@code service}. */
    public String getKind() {
        return kind;
    }

    /** True when the principal's tokens are refused. */
    public boolean isDisabled() {
        return disabled;
    }

    /** Enables or disables the principal. */
    public void setDisabled(boolean disabled, Instant now) {
        this.disabled = disabled;
        this.updatedAt = now;
    }

    /** When the principal was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When the principal last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
