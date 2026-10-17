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
 * <p>Prompt 130 adds the profile: an optional email (used only for this principal's own email
 * notifications), an avatar (a 043 media id), a timezone and a free-text organization, which draws the
 * TLP {@code AMBER} and {@code AMBER+STRICT} boundaries. Groups, notification preferences and access
 * lists live in {@code ai.foresignal.collab}.
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

    @Column(name = "email", length = 320)
    private String email;

    @Column(name = "avatar_media_id", length = 80)
    private String avatarMediaId;

    @Column(name = "timezone", length = 64)
    private String timezone;

    @Column(name = "organization", length = 200)
    private String organization;

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

    /** Changes the display name. */
    public void setDisplayName(String displayName, Instant now) {
        this.displayName = displayName;
        this.updatedAt = now;
    }

    /** The email used only for this principal's email notifications, or null. */
    public String getEmail() {
        return email;
    }

    /** Sets the email. */
    public void setEmail(String email, Instant now) {
        this.email = email;
        this.updatedAt = now;
    }

    /** The 043 media id of the avatar, or null. */
    public String getAvatarMediaId() {
        return avatarMediaId;
    }

    /** Sets the avatar. */
    public void setAvatarMediaId(String avatarMediaId, Instant now) {
        this.avatarMediaId = avatarMediaId;
        this.updatedAt = now;
    }

    /** The IANA timezone, or null. */
    public String getTimezone() {
        return timezone;
    }

    /** Sets the timezone. */
    public void setTimezone(String timezone, Instant now) {
        this.timezone = timezone;
        this.updatedAt = now;
    }

    /** The free-text organization (TLP AMBER boundaries), or null. */
    public String getOrganization() {
        return organization;
    }

    /** Sets the organization. */
    public void setOrganization(String organization, Instant now) {
        this.organization = organization;
        this.updatedAt = now;
    }
}
