package ai.foresignal.source.kit.websub;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One WebSub subscription ({@code websub_subscription}, prompt 073), at most one per feed-kit source.
 *
 * <p>{@code token} is the unguessable callback path segment; {@code secret_enc} is the {@code hub.secret}, encrypted
 * with the install key ({@code CredentialCipher}: nonce followed by ciphertext). {@code state} is one of
 * {@value #PENDING} (requested, awaiting the hub's verification), {@value #ACTIVE} (verified, lease running) and
 * {@value #LOST} (refused, denied or not renewed: the source polls). {@code requested_at} records the last subscribe
 * request, so a pending request is not repeated on every poll.
 */
@Entity
@Table(name = "websub_subscription")
public class WebSubSubscriptionEntity {

    /** Requested; awaiting the hub's intent verification. */
    public static final String PENDING = "pending";

    /** Verified; the lease is running. */
    public static final String ACTIVE = "active";

    /** Refused, denied or not renewed; the source polls normally. */
    public static final String LOST = "lost";

    @Id
    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "hub", nullable = false, length = 2048)
    private String hub;

    @Column(name = "topic", nullable = false, length = 2048)
    private String topic;

    @Column(name = "token", nullable = false, length = 128)
    private String token;

    @Column(name = "secret_enc", nullable = false)
    private byte[] secretEnc;

    @Column(name = "state", nullable = false, length = 16)
    private String state;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "lease_expires_at")
    private Instant leaseExpiresAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_verified_at")
    private Instant lastVerifiedAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_push_at")
    private Instant lastPushAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "requested_at")
    private Instant requestedAt;

    /** For JPA. */
    protected WebSubSubscriptionEntity() {}

    /**
     * A new subscription.
     *
     * @param sourceId the feed source
     * @param hub the hub URL
     * @param topic the topic (the feed's self link)
     * @param token the callback token
     * @param secretEnc the encrypted secret
     */
    public WebSubSubscriptionEntity(String sourceId, String hub, String topic, String token, byte[] secretEnc) {
        this.sourceId = sourceId;
        this.hub = hub;
        this.topic = topic;
        this.token = token;
        this.secretEnc = secretEnc;
        this.state = PENDING;
    }

    /** The feed source. */
    public String getSourceId() {
        return sourceId;
    }

    /** The hub URL. */
    public String getHub() {
        return hub;
    }

    /** Sets the hub URL. */
    public void setHub(String hub) {
        this.hub = hub;
    }

    /** The topic. */
    public String getTopic() {
        return topic;
    }

    /** Sets the topic. */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** The callback token. */
    public String getToken() {
        return token;
    }

    /** The encrypted secret (nonce followed by ciphertext). */
    public byte[] getSecretEnc() {
        return secretEnc;
    }

    /** Replaces the encrypted secret. */
    public void setSecretEnc(byte[] secretEnc) {
        this.secretEnc = secretEnc;
    }

    /** The state. */
    public String getState() {
        return state;
    }

    /** Sets the state. */
    public void setState(String state) {
        this.state = state;
    }

    /** When the lease ends, or null. */
    public Instant getLeaseExpiresAt() {
        return leaseExpiresAt;
    }

    /** Sets when the lease ends. */
    public void setLeaseExpiresAt(Instant leaseExpiresAt) {
        this.leaseExpiresAt = leaseExpiresAt;
    }

    /** When the hub last verified the intent, or null. */
    public Instant getLastVerifiedAt() {
        return lastVerifiedAt;
    }

    /** Sets when the hub last verified the intent. */
    public void setLastVerifiedAt(Instant lastVerifiedAt) {
        this.lastVerifiedAt = lastVerifiedAt;
    }

    /** When content was last delivered, or null. */
    public Instant getLastPushAt() {
        return lastPushAt;
    }

    /** Sets when content was last delivered. */
    public void setLastPushAt(Instant lastPushAt) {
        this.lastPushAt = lastPushAt;
    }

    /** When the last subscribe request was sent, or null. */
    public Instant getRequestedAt() {
        return requestedAt;
    }

    /** Sets when the last subscribe request was sent. */
    public void setRequestedAt(Instant requestedAt) {
        this.requestedAt = requestedAt;
    }
}
