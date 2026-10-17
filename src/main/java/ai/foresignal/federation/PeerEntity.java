package ai.foresignal.federation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A federation peer (prompt 113): another Foresignal instance this one consumes ({@code PULL}) or that pushes here
 * as an edge relay ({@code RECEIVE}). The remote token is stored only encrypted with the install key (SEC-3); the
 * peer ingest key only as a SHA-256 hash. The remote signing key is pinned on the first verified {@code /info}
 * ({@code pinned_kid}, the RFC 7638 thumbprint, with its raw point in {@code pinned_key}); only an admin changes it.
 */
@Entity
@Table(name = "peer", indexes = @Index(name = "peer_ingest_key_hash_idx", columnList = "ingest_key_hash"))
public class PeerEntity {

    @Id
    @Column(name = "id", length = 48, nullable = false)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "base_url", length = 2048, nullable = false)
    private String baseUrl;

    @Column(name = "token_ciphertext")
    private byte[] tokenCiphertext;

    @Column(name = "token_nonce")
    private byte[] tokenNonce;

    @Column(name = "pinned_kid", length = 128)
    private String pinnedKid;

    @Column(name = "pinned_key")
    private byte[] pinnedKey;

    @Column(name = "remote_instance_id", length = 128)
    private String remoteInstanceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "selection", columnDefinition = "jsonb default '{}'::jsonb")
    private Map<String, Object> selection = new LinkedHashMap<>();

    @Column(name = "mode", length = 16, nullable = false)
    private String mode;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "same_operator", nullable = false, columnDefinition = "boolean default false")
    private boolean sameOperator;

    @Column(name = "reliability", length = 1)
    private String reliability;

    @Column(name = "state", length = 32, nullable = false)
    private String state;

    @Column(name = "health_reason", length = 32)
    private String healthReason;

    @Column(name = "last_seq")
    private Long lastSeq;

    @Column(name = "remote_watermark")
    private Long remoteWatermark;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "ingest_key_hash", length = 64)
    private String ingestKeyHash;

    @Column(name = "ingest_key_prefix", length = 16)
    private String ingestKeyPrefix;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected PeerEntity() {}

    /** A new peer. */
    public PeerEntity(String id, String name, String baseUrl, String mode, Instant now) {
        this.id = id;
        this.name = name;
        this.baseUrl = baseUrl;
        this.mode = mode;
        this.state = PeerService.STATE_STOPPED;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** The slug id. */
    public String getId() {
        return id;
    }

    /** The display name. */
    public String getName() {
        return name;
    }

    /** Sets the display name. */
    public void setName(String name) {
        this.name = name;
    }

    /** The remote base URL. */
    public String getBaseUrl() {
        return baseUrl;
    }

    /** Sets the remote base URL. */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /** The encrypted remote token, or null. */
    public byte[] getTokenCiphertext() {
        return tokenCiphertext;
    }

    /** The nonce of the encrypted token, or null. */
    public byte[] getTokenNonce() {
        return tokenNonce;
    }

    /** Stores the encrypted remote token (both null to clear it). */
    public void setToken(byte[] ciphertext, byte[] nonce) {
        this.tokenCiphertext = ciphertext;
        this.tokenNonce = nonce;
    }

    /** The pinned key's thumbprint, or null before the first verified contact. */
    public String getPinnedKid() {
        return pinnedKid;
    }

    /** The pinned key's raw Ed25519 point, or null. */
    public byte[] getPinnedKey() {
        return pinnedKey;
    }

    /** Pins a key (both null to re-pin on the next verified contact). */
    public void setPinned(String kid, byte[] key) {
        this.pinnedKid = kid;
        this.pinnedKey = key;
    }

    /** The remote instance id, once known. */
    public String getRemoteInstanceId() {
        return remoteInstanceId;
    }

    /** Sets the remote instance id. */
    public void setRemoteInstanceId(String remoteInstanceId) {
        this.remoteInstanceId = remoteInstanceId;
    }

    /** The selection as stored JSON. */
    public Map<String, Object> getSelection() {
        return selection == null ? Map.of() : selection;
    }

    /** Stores the selection. */
    public void setSelection(Map<String, Object> selection) {
        this.selection = selection == null ? new LinkedHashMap<>() : new LinkedHashMap<>(selection);
    }

    /** {@code PULL} or {@code RECEIVE}. */
    public String getMode() {
        return mode;
    }

    /** True when the peer replicates. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Switches the peer on or off. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** True when the peer is the operator's own hub (the {@code LOCAL_ONLY} exception). */
    public boolean isSameOperator() {
        return sameOperator;
    }

    /** Sets {@link #isSameOperator()}. */
    public void setSameOperator(boolean sameOperator) {
        this.sameOperator = sameOperator;
    }

    /** The Admiralty reliability grade, or null for the default {@code F}. */
    public String getReliability() {
        return reliability;
    }

    /** Sets the reliability grade. */
    public void setReliability(String reliability) {
        this.reliability = reliability;
    }

    /** The runtime state. */
    public String getState() {
        return state;
    }

    /** Sets the runtime state. */
    public void setState(String state) {
        this.state = state;
    }

    /** The health reason ({@code PEER_KEY_MISMATCH}, {@code PEER_UNREACHABLE}, {@code PEER_RESYNC}), or null. */
    public String getHealthReason() {
        return healthReason;
    }

    /** Sets the health reason. */
    public void setHealthReason(String healthReason) {
        this.healthReason = healthReason;
    }

    /** The last remote seq applied, or null. */
    public Long getLastSeq() {
        return lastSeq;
    }

    /** Sets the last remote seq applied. */
    public void setLastSeq(Long lastSeq) {
        this.lastSeq = lastSeq;
    }

    /** The remote watermark last seen, or null. */
    public Long getRemoteWatermark() {
        return remoteWatermark;
    }

    /** Sets the remote watermark last seen. */
    public void setRemoteWatermark(Long remoteWatermark) {
        this.remoteWatermark = remoteWatermark;
    }

    /** The last error, or null. */
    public String getLastError() {
        return lastError;
    }

    /** Sets the last error (cut to 1,000 characters). */
    public void setLastError(String lastError) {
        this.lastError = lastError == null || lastError.length() <= 1000 ? lastError : lastError.substring(0, 1000);
    }

    /** The SHA-256 of the current peer ingest key, or null. */
    public String getIngestKeyHash() {
        return ingestKeyHash;
    }

    /** The prefix of the current peer ingest key, or null. */
    public String getIngestKeyPrefix() {
        return ingestKeyPrefix;
    }

    /** Stores a new peer ingest key's hash and prefix. */
    public void setIngestKey(String hash, String prefix) {
        this.ingestKeyHash = hash;
        this.ingestKeyPrefix = prefix;
    }

    /** When the peer was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When the peer last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets {@link #getUpdatedAt()}. */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
