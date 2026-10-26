package ai.foresignal.security.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A human account ({@code "user"}, prompt R2-001; OpenAPI {@code User}).
 *
 * <p>The email is the primary identity. It is stored normalized ({@link #normalizeEmail}: trimmed and lower-cased)
 * under a unique constraint; the baseline enables no {@code citext} extension, so normalization happens here rather
 * than in the database. The password hash is null until the account is activated (hashing is R2-002). Every user owns
 * one {@code principal} of kind {@code person}, referenced by {@link #getPrincipalId()}, through which collaboration,
 * sharing and audit keep referring to a principal.
 */
@Entity
@Table(name = "`user`")
public class UserEntity {

    /** Longest stored email (RFC 5321 path limit, the same as {@code principal.email}). */
    public static final int EMAIL_LENGTH = 320;

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "email", length = EMAIL_LENGTH, nullable = false, unique = true)
    private String email;

    @Column(name = "full_name", length = 200, nullable = false)
    private String fullName;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @ColumnDefault("'invited'")
    @Column(name = "status", length = 16, nullable = false)
    private String status;

    @Column(name = "principal_id", length = 26, nullable = false)
    private String principalId;

    @ColumnDefault("false")
    @Column(name = "mfa_enabled", nullable = false)
    private boolean mfaEnabled;

    @Column(name = "created_by")
    private UUID createdBy;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    /** For JPA. */
    protected UserEntity() {}

    /**
     * A new, invited user with no password.
     *
     * @param id the user id
     * @param email the email, normalized on the way in
     * @param fullName the full name
     * @param principalId the ULID of the user's {@code person} principal
     * @param createdBy the admin who created the account, or null for the bootstrap first admin
     * @param now the creation time
     */
    public UserEntity(UUID id, String email, String fullName, String principalId, UUID createdBy, Instant now) {
        this.id = id;
        this.email = normalizeEmail(email);
        this.fullName = fullName;
        this.principalId = principalId;
        this.createdBy = createdBy;
        this.status = UserStatus.INVITED.wireValue();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * The stored form of an email: trimmed and lower-cased (root locale), so lookups and the unique constraint are
     * case-insensitive.
     *
     * @param email an email as entered, or null
     * @return the normalized email, or null for null
     */
    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /** The user id. */
    public UUID getId() {
        return id;
    }

    /** The normalized email. */
    public String getEmail() {
        return email;
    }

    /**
     * Changes the email.
     *
     * @param email the new email, normalized on the way in
     * @param now the change time
     */
    public void setEmail(String email, Instant now) {
        this.email = normalizeEmail(email);
        this.updatedAt = now;
    }

    /** The full name. */
    public String getFullName() {
        return fullName;
    }

    /**
     * Changes the full name.
     *
     * @param fullName the new name
     * @param now the change time
     */
    public void setFullName(String fullName, Instant now) {
        this.fullName = fullName;
        this.updatedAt = now;
    }

    /** The password hash, or null before activation. */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Stores a new password hash (never a plaintext password).
     *
     * @param passwordHash the encoded hash
     * @param now the change time
     */
    public void setPasswordHash(String passwordHash, Instant now) {
        this.passwordHash = passwordHash;
        this.updatedAt = now;
    }

    /** The lifecycle status. */
    public UserStatus getStatus() {
        return UserStatus.fromWire(status);
    }

    /**
     * Moves the user to a status.
     *
     * @param status the new status
     * @param now the change time
     */
    public void setStatus(UserStatus status, Instant now) {
        this.status = status.wireValue();
        this.updatedAt = now;
    }

    /** The ULID of the user's {@code person} principal. */
    public String getPrincipalId() {
        return principalId;
    }

    /** True when MFA is enrolled (the logic is a later R2 prompt). */
    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    /**
     * Records whether MFA is enrolled.
     *
     * @param mfaEnabled true when enrolled
     * @param now the change time
     */
    public void setMfaEnabled(boolean mfaEnabled, Instant now) {
        this.mfaEnabled = mfaEnabled;
        this.updatedAt = now;
    }

    /** The admin who created the account, or null for the bootstrap first admin. */
    public UUID getCreatedBy() {
        return createdBy;
    }

    /** When the account was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When the account last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** When the user last signed in, or null. */
    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    /**
     * Records a successful sign-in. Does not move {@link #getUpdatedAt()}: signing in is not an account change.
     *
     * @param at the sign-in time
     */
    public void recordLogin(Instant at) {
        this.lastLoginAt = at;
    }
}
