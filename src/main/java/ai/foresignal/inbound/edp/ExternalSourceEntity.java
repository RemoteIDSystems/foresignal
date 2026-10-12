package ai.foresignal.inbound.edp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A registered EDP provider ({@code external_source}, prompt 024): its manifest, approval state and
 * pull and activity bookkeeping. The provider is also a {@code custom_source} row (ingest key and
 * caps) and a dynamic source in the registry.
 */
@Entity
@Table(name = "external_source")
public class ExternalSourceEntity {

    /** Awaiting the operator's approval; nothing is stored (invariant EXT-1). */
    public static final String PENDING = "pending";

    /** Approved: pushes are accepted and pulls run. */
    public static final String ACTIVE = "active";

    /** Stopped by the operator; records stay. */
    public static final String SUSPENDED = "suspended";

    /** Registered by an admin. */
    public static final String VIA_OPERATOR = "operator";

    /** Self-registered with a registration token. */
    public static final String VIA_TOKEN = "registration-token";

    @Id
    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    /** The manifest as submitted, raw JSON text. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "manifest", nullable = false, columnDefinition = "jsonb")
    private String manifest;

    @Column(name = "manifest_sha256", length = 64, nullable = false)
    private String manifestSha256;

    @Column(name = "state", length = 16, nullable = false)
    private String state = PENDING;

    @Column(name = "registered_via", length = 32, nullable = false)
    private String registeredVia = VIA_OPERATOR;

    @Column(name = "approved_by", length = 200)
    private String approvedBy;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "allow_private_network", nullable = false)
    private boolean allowPrivateNetwork;

    @Column(name = "pull_cursor", length = 2048)
    private String pullCursor;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_poll_at")
    private Instant lastPollAt;

    @Column(name = "last_pull_error", length = 2000)
    private String lastPullError;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_item_at")
    private Instant lastItemAt;

    /**
     * Activity as raw JSON text: per-stream counts, last validation errors, provider-reported
     * status and held snapshots ({@link EdpStats}).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stats", columnDefinition = "jsonb")
    private String stats;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected ExternalSourceEntity() {}

    /** A new row for {@code sourceId}. */
    public ExternalSourceEntity(String sourceId) {
        this.sourceId = sourceId;
    }

    /** The source id ({@code ext-…}). */
    public String getSourceId() {
        return sourceId;
    }

    /** The manifest JSON. */
    public String getManifest() {
        return manifest;
    }

    /** Sets the manifest JSON and its hash. */
    public void setManifest(String manifest, String sha256) {
        this.manifest = manifest;
        this.manifestSha256 = sha256;
    }

    /** The manifest's SHA-256. */
    public String getManifestSha256() {
        return manifestSha256;
    }

    /** {@link #PENDING}, {@link #ACTIVE} or {@link #SUSPENDED}. */
    public String getState() {
        return state;
    }

    /** Sets the state. */
    public void setState(String state) {
        this.state = state;
    }

    /** How the provider was registered. */
    public String getRegisteredVia() {
        return registeredVia;
    }

    /** Sets how the provider was registered. */
    public void setRegisteredVia(String registeredVia) {
        this.registeredVia = registeredVia;
    }

    /** Who approved it, or null. */
    public String getApprovedBy() {
        return approvedBy;
    }

    /** When it was approved, or null. */
    public Instant getApprovedAt() {
        return approvedAt;
    }

    /** Records an approval. */
    public void setApproval(String approvedBy, Instant approvedAt) {
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
    }

    /** True when provider URLs may resolve to private networks (a LAN provider). */
    public boolean isAllowPrivateNetwork() {
        return allowPrivateNetwork;
    }

    /** Sets the private-network permission. */
    public void setAllowPrivateNetwork(boolean allowPrivateNetwork) {
        this.allowPrivateNetwork = allowPrivateNetwork;
    }

    /** The stored pull cursor, or null. */
    public String getPullCursor() {
        return pullCursor;
    }

    /** Sets the pull cursor. */
    public void setPullCursor(String pullCursor) {
        this.pullCursor = pullCursor;
    }

    /** When the provider was last polled, or null. */
    public Instant getLastPollAt() {
        return lastPollAt;
    }

    /** Sets the last poll time. */
    public void setLastPollAt(Instant lastPollAt) {
        this.lastPollAt = lastPollAt;
    }

    /** The last pull error, or null. */
    public String getLastPullError() {
        return lastPullError;
    }

    /** Sets the last pull error. */
    public void setLastPullError(String lastPullError) {
        this.lastPullError = lastPullError == null || lastPullError.length() <= 2000
                ? lastPullError
                : lastPullError.substring(0, 2000);
    }

    /** When an item was last accepted, or null. */
    public Instant getLastItemAt() {
        return lastItemAt;
    }

    /** Sets the last item time. */
    public void setLastItemAt(Instant lastItemAt) {
        this.lastItemAt = lastItemAt;
    }

    /** The activity JSON, or null. */
    public String getStats() {
        return stats;
    }

    /** Sets the activity JSON. */
    public void setStats(String stats) {
        this.stats = stats;
    }

    /** When it was registered. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets the registration time. */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /** When the row last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets the update time. */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
