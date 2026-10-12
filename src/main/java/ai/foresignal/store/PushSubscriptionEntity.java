package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A Web Push or UnifiedPush subscription (Architecture §7.1 and §24, prompt 031).
 *
 * <p>Beyond the Architecture columns: {@code status} ({@code pending} until the verification code
 * is echoed, then {@code active}; {@code disabled} after three consecutive failed send-time address
 * rechecks), the verification code's SHA-256 ({@code verify_code_hash}, the code itself is never
 * stored), its expiry and attempts, {@code endpoint_host} (the only part of the endpoint the API
 * shows), {@code transport_label} ({@code browser|unifiedpush|other}, display only),
 * {@code payload_format} ({@code foresignal|declarative}), {@code lang}, and failure counters.
 * {@code token_id} is the creating API token; revoking it deletes the row.
 */
@Entity
@Table(
        name = "push_subscription",
        indexes = {
            @Index(name = "push_subscription_token_idx", columnList = "token_id"),
            @Index(name = "push_subscription_status_idx", columnList = "status, created_at")
        })
public class PushSubscriptionEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", length = 200)
    private String name;

    @Column(name = "endpoint", nullable = false, columnDefinition = "text")
    private String endpoint;

    @Column(name = "p256dh", nullable = false, length = 100)
    private String p256dh;

    @Column(name = "auth", nullable = false, length = 40)
    private String auth;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filters", nullable = false, columnDefinition = "jsonb default '{}'::jsonb")
    private Map<String, Object> filters = new LinkedHashMap<>();

    @Column(name = "token_id")
    private UUID tokenId;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_success_at")
    private Instant lastSuccessAt;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "verify_code_hash", length = 64)
    private String verifyCodeHash;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "verify_expires_at")
    private Instant verifyExpiresAt;

    @Column(name = "verify_attempts", nullable = false, columnDefinition = "integer default 0")
    private int verifyAttempts;

    @Column(name = "endpoint_host", nullable = false, length = 255)
    private String endpointHost;

    @Column(name = "transport_label", nullable = false, length = 16)
    private String transportLabel;

    @Column(name = "payload_format", nullable = false, length = 16)
    private String payloadFormat;

    @Column(name = "lang", length = 35)
    private String lang;

    @Column(name = "failure_count", nullable = false, columnDefinition = "integer default 0")
    private int failureCount;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_failure_at")
    private Instant lastFailureAt;

    @Column(name = "last_status")
    private Integer lastStatus;

    @Column(name = "recheck_failures", nullable = false, columnDefinition = "integer default 0")
    private int recheckFailures;

    /** For JPA. */
    protected PushSubscriptionEntity() {}

    /** A new subscription. */
    public PushSubscriptionEntity(UUID id, String endpoint, String p256dh, String auth, Instant createdAt) {
        this.id = id;
        this.endpoint = endpoint;
        this.p256dh = p256dh;
        this.auth = auth;
        this.createdAt = createdAt;
    }

    /** The {@code id} column. */
    public UUID getId() {
        return id;
    }

    /** The {@code name} column. */
    public String getName() {
        return name;
    }

    /** Sets the {@code name} column. */
    public void setName(String value) {
        this.name = value;
    }

    /** The {@code endpoint} column. */
    public String getEndpoint() {
        return endpoint;
    }

    /** Sets the {@code endpoint} column. */
    public void setEndpoint(String value) {
        this.endpoint = value;
    }

    /** The {@code p256dh} column. */
    public String getP256dh() {
        return p256dh;
    }

    /** Sets the {@code p256dh} column. */
    public void setP256dh(String value) {
        this.p256dh = value;
    }

    /** The {@code auth} column. */
    public String getAuth() {
        return auth;
    }

    /** Sets the {@code auth} column. */
    public void setAuth(String value) {
        this.auth = value;
    }

    /** The {@code filters} column. */
    public Map<String, Object> getFilters() {
        return filters;
    }

    /** Sets the {@code filters} column. */
    public void setFilters(Map<String, Object> value) {
        this.filters = value;
    }

    /** The {@code token_id} column. */
    public UUID getTokenId() {
        return tokenId;
    }

    /** Sets the {@code token_id} column. */
    public void setTokenId(UUID value) {
        this.tokenId = value;
    }

    /** The {@code created_at} column. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets the {@code created_at} column. */
    public void setCreatedAt(Instant value) {
        this.createdAt = value;
    }

    /** The {@code last_success_at} column. */
    public Instant getLastSuccessAt() {
        return lastSuccessAt;
    }

    /** Sets the {@code last_success_at} column. */
    public void setLastSuccessAt(Instant value) {
        this.lastSuccessAt = value;
    }

    /** The {@code status} column. */
    public String getStatus() {
        return status;
    }

    /** Sets the {@code status} column. */
    public void setStatus(String value) {
        this.status = value;
    }

    /** The {@code verify_code_hash} column. */
    public String getVerifyCodeHash() {
        return verifyCodeHash;
    }

    /** Sets the {@code verify_code_hash} column. */
    public void setVerifyCodeHash(String value) {
        this.verifyCodeHash = value;
    }

    /** The {@code verify_expires_at} column. */
    public Instant getVerifyExpiresAt() {
        return verifyExpiresAt;
    }

    /** Sets the {@code verify_expires_at} column. */
    public void setVerifyExpiresAt(Instant value) {
        this.verifyExpiresAt = value;
    }

    /** The {@code verify_attempts} column. */
    public int getVerifyAttempts() {
        return verifyAttempts;
    }

    /** Sets the {@code verify_attempts} column. */
    public void setVerifyAttempts(int value) {
        this.verifyAttempts = value;
    }

    /** The {@code endpoint_host} column. */
    public String getEndpointHost() {
        return endpointHost;
    }

    /** Sets the {@code endpoint_host} column. */
    public void setEndpointHost(String value) {
        this.endpointHost = value;
    }

    /** The {@code transport_label} column. */
    public String getTransportLabel() {
        return transportLabel;
    }

    /** Sets the {@code transport_label} column. */
    public void setTransportLabel(String value) {
        this.transportLabel = value;
    }

    /** The {@code payload_format} column. */
    public String getPayloadFormat() {
        return payloadFormat;
    }

    /** Sets the {@code payload_format} column. */
    public void setPayloadFormat(String value) {
        this.payloadFormat = value;
    }

    /** The {@code lang} column. */
    public String getLang() {
        return lang;
    }

    /** Sets the {@code lang} column. */
    public void setLang(String value) {
        this.lang = value;
    }

    /** The {@code failure_count} column. */
    public int getFailureCount() {
        return failureCount;
    }

    /** Sets the {@code failure_count} column. */
    public void setFailureCount(int value) {
        this.failureCount = value;
    }

    /** The {@code last_failure_at} column. */
    public Instant getLastFailureAt() {
        return lastFailureAt;
    }

    /** Sets the {@code last_failure_at} column. */
    public void setLastFailureAt(Instant value) {
        this.lastFailureAt = value;
    }

    /** The {@code last_status} column. */
    public Integer getLastStatus() {
        return lastStatus;
    }

    /** Sets the {@code last_status} column. */
    public void setLastStatus(Integer value) {
        this.lastStatus = value;
    }

    /** The {@code recheck_failures} column. */
    public int getRecheckFailures() {
        return recheckFailures;
    }

    /** Sets the {@code recheck_failures} column. */
    public void setRecheckFailures(int value) {
        this.recheckFailures = value;
    }
}
