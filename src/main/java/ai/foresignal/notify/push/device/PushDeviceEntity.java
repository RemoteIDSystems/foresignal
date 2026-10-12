package ai.foresignal.notify.push.device;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A native app installation registered for push ({@code push_device}, prompt 032).
 *
 * <p>The device token is encrypted with {@code CredentialCipher} ({@code token_ciphertext} /
 * {@code token_nonce}) and never returned; {@code token_hash} (SHA-256) is unique with
 * {@code transport}, so re-registering the same token updates the row, and {@code token_suffix}
 * (the last 6 characters) is the only part shown. {@code token_id} is the API token that
 * registered it; revoking or expiring that token deletes the row. {@code device_name} and
 * {@code app_version} are copied from the token's 023 device metadata.
 */
@Entity
@Table(
        name = "push_device",
        uniqueConstraints = @UniqueConstraint(name = "push_device_token_uq", columnNames = {"transport", "token_hash"}),
        indexes = {
            @Index(name = "push_device_token_idx", columnList = "token_id"),
            @Index(name = "push_device_status_idx", columnList = "status, created_at")
        })
public class PushDeviceEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "transport", nullable = false, length = 8)
    private String transport;

    @Column(name = "platform", nullable = false, length = 16)
    private String platform;

    @Column(name = "app_id", nullable = false, length = 255)
    private String appId;

    @Column(name = "environment", length = 16)
    private String environment;

    @Column(name = "token_ciphertext", nullable = false)
    private byte[] tokenCiphertext;

    @Column(name = "token_nonce", nullable = false)
    private byte[] tokenNonce;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "token_suffix", nullable = false, length = 6)
    private String tokenSuffix;

    @Column(name = "token_id", nullable = false)
    private UUID tokenId;

    @Column(name = "name", length = 200)
    private String name;

    @Column(name = "device_name", length = 200)
    private String deviceName;

    @Column(name = "app_version", length = 64)
    private String appVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filters", nullable = false, columnDefinition = "jsonb default '{}'::jsonb")
    private Map<String, Object> filters = new LinkedHashMap<>();

    @Column(name = "content_level", nullable = false, length = 16)
    private String contentLevel;

    @Column(name = "lang", length = 35)
    private String lang;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_success_at")
    private Instant lastSuccessAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_failure_at")
    private Instant lastFailureAt;

    @Column(name = "failure_count", nullable = false, columnDefinition = "integer default 0")
    private int failureCount;

    @Column(name = "last_status")
    private Integer lastStatus;

    protected PushDeviceEntity() {
        // for JPA
    }

    /** A new device row with {@code id}. */
    public PushDeviceEntity(UUID id) {
        this.id = id;
    }

    /** The id. */
    public UUID getId() {
        return id;
    }

    /** {@code apns} or {@code fcm}. */
    public String getTransport() {
        return transport;
    }

    /** Sets transport. */
    public void setTransport(String transport) {
        this.transport = transport;
    }

    /** {@code ios}, {@code ipados}, {@code macos} or {@code android}. */
    public String getPlatform() {
        return platform;
    }

    /** Sets platform. */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /** The bundle id or package name. */
    public String getAppId() {
        return appId;
    }

    /** Sets appId. */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /** {@code sandbox} or {@code production} (APNs), or null. */
    public String getEnvironment() {
        return environment;
    }

    /** Sets environment. */
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    /** The encrypted device token. */
    public byte[] getTokenCiphertext() {
        return tokenCiphertext;
    }

    /** Sets tokenCiphertext. */
    public void setTokenCiphertext(byte[] tokenCiphertext) {
        this.tokenCiphertext = tokenCiphertext;
    }

    /** The device token's nonce. */
    public byte[] getTokenNonce() {
        return tokenNonce;
    }

    /** Sets tokenNonce. */
    public void setTokenNonce(byte[] tokenNonce) {
        this.tokenNonce = tokenNonce;
    }

    /** Lower-case hex SHA-256 of the device token. */
    public String getTokenHash() {
        return tokenHash;
    }

    /** Sets tokenHash. */
    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    /** The last 6 characters of the device token (display only). */
    public String getTokenSuffix() {
        return tokenSuffix;
    }

    /** Sets tokenSuffix. */
    public void setTokenSuffix(String tokenSuffix) {
        this.tokenSuffix = tokenSuffix;
    }

    /** The API token that registered the device. */
    public UUID getTokenId() {
        return tokenId;
    }

    /** Sets tokenId. */
    public void setTokenId(UUID tokenId) {
        this.tokenId = tokenId;
    }

    /** The display name, or null. */
    public String getName() {
        return name;
    }

    /** Sets name. */
    public void setName(String name) {
        this.name = name;
    }

    /** The device name from the token's 023 device metadata, or null. */
    public String getDeviceName() {
        return deviceName;
    }

    /** Sets deviceName. */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /** The app version from the token's 023 device metadata, or null. */
    public String getAppVersion() {
        return appVersion;
    }

    /** Sets appVersion. */
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    /** The 031 {@code PushFilters}. */
    public Map<String, Object> getFilters() {
        return filters;
    }

    /** Sets the filters. */
    public void setFilters(Map<String, Object> filters) {
        this.filters = filters == null ? new LinkedHashMap<>() : new LinkedHashMap<>(filters);
    }

    /** {@code minimal} or {@code short}. */
    public String getContentLevel() {
        return contentLevel;
    }

    /** Sets contentLevel. */
    public void setContentLevel(String contentLevel) {
        this.contentLevel = contentLevel;
    }

    /** The language, or null. */
    public String getLang() {
        return lang;
    }

    /** Sets lang. */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /** {@code active} or {@code disabled}. */
    public String getStatus() {
        return status;
    }

    /** Sets status. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** When it was registered. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets createdAt. */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /** The last accepted push, or null. */
    public Instant getLastSuccessAt() {
        return lastSuccessAt;
    }

    /** Sets lastSuccessAt. */
    public void setLastSuccessAt(Instant lastSuccessAt) {
        this.lastSuccessAt = lastSuccessAt;
    }

    /** The last failed push, or null. */
    public Instant getLastFailureAt() {
        return lastFailureAt;
    }

    /** Sets lastFailureAt. */
    public void setLastFailureAt(Instant lastFailureAt) {
        this.lastFailureAt = lastFailureAt;
    }

    /** Failures since the last success. */
    public int getFailureCount() {
        return failureCount;
    }

    /** Sets failureCount. */
    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    /** The push service's last HTTP status, or null. */
    public Integer getLastStatus() {
        return lastStatus;
    }

    /** Sets lastStatus. */
    public void setLastStatus(Integer lastStatus) {
        this.lastStatus = lastStatus;
    }
}
