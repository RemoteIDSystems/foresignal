package ai.foresignal.chronicle.network;

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
 * A request for access to a related chronicle (table {@code chronicle_access_request}, prompt 134). It carries only
 * the requester's handle, its message and, if the requester chose, an offer to share one of its own chronicles: never
 * the requester's chronicle in the link.
 *
 * <p>A local request names the requesting principal and the owner of the target chronicle here. An outgoing remote
 * request names the target as {@code remote:{instanceId}/chronicle:{sketchId}}; an incoming remote request names the
 * requester's instance and the id the requester's instance gave it ({@code remote_request_id}). A request to an owner
 * who declined the same requester within 30 days is stored as {@code suppressed}: the owner never sees it.
 */
@Entity
@Table(name = "chronicle_access_request", indexes = {
        @Index(name = "chronicle_access_request_requester_idx", columnList = "requester_principal, created_at"),
        @Index(name = "chronicle_access_request_owner_idx", columnList = "owner_principal, created_at"),
        @Index(name = "chronicle_access_request_link_idx", columnList = "link_id")})
public class ChronicleAccessRequestEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "link_id", length = 26, nullable = false)
    private String linkId;

    @Column(name = "requester_principal", length = 200)
    private String requesterPrincipal;

    @Column(name = "requester_handle", length = 64, nullable = false)
    private String requesterHandle;

    @Column(name = "requester_instance", length = 128)
    private String requesterInstance;

    @Column(name = "remote_request_id", length = 26)
    private String remoteRequestId;

    @Column(name = "target_chronicle_id", length = 26)
    private String targetChronicleId;

    @Column(name = "target_node", length = 300)
    private String targetNode;

    @Column(name = "network_id", length = 64)
    private String networkId;

    @Column(name = "owner_principal", length = 200)
    private String ownerPrincipal;

    @Column(name = "message", length = 1000)
    private String message;

    @Column(name = "offer_share", length = 26)
    private String offerShare;

    @Column(name = "offer_remote", nullable = false)
    private boolean offerRemote;

    @Column(name = "status", length = 16, nullable = false)
    private String status = NetworkVocabulary.PENDING;

    @Column(name = "suppressed", nullable = false)
    private boolean suppressed;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "grant_info")
    private Map<String, Object> grant;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    /** For JPA. */
    protected ChronicleAccessRequestEntity() {}

    /**
     * A new request.
     *
     * @param id its ULID
     * @param linkId the link it is about
     * @param requesterHandle the requester's handle
     * @param now when it was made
     */
    public ChronicleAccessRequestEntity(String id, String linkId, String requesterHandle, Instant now) {
        this.id = id;
        this.linkId = linkId;
        this.requesterHandle = requesterHandle;
        this.createdAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The link. */
    public String getLinkId() {
        return linkId;
    }

    /** The local requesting principal, or null for a request from another instance. */
    public String getRequesterPrincipal() {
        return requesterPrincipal;
    }

    /** Sets the local requesting principal. */
    public void setRequesterPrincipal(String value) {
        this.requesterPrincipal = value;
    }

    /** The requester's handle. */
    public String getRequesterHandle() {
        return requesterHandle;
    }

    /** The requester's instance for a request from another instance, else null. */
    public String getRequesterInstance() {
        return requesterInstance;
    }

    /** Sets the requester's instance. */
    public void setRequesterInstance(String value) {
        this.requesterInstance = value;
    }

    /** The requester instance's id of an incoming remote request, else null. */
    public String getRemoteRequestId() {
        return remoteRequestId;
    }

    /** Sets the requester instance's id. */
    public void setRemoteRequestId(String value) {
        this.remoteRequestId = value;
    }

    /** The local target chronicle, or null for an outgoing remote request. */
    public String getTargetChronicleId() {
        return targetChronicleId;
    }

    /** Sets the local target chronicle. */
    public void setTargetChronicleId(String value) {
        this.targetChronicleId = value;
    }

    /** The remote target of an outgoing remote request, else null. */
    public String getTargetNode() {
        return targetNode;
    }

    /** Sets the remote target. */
    public void setTargetNode(String value) {
        this.targetNode = value;
    }

    /** The network of a remote request, else null. */
    public String getNetworkId() {
        return networkId;
    }

    /** Sets the network. */
    public void setNetworkId(String value) {
        this.networkId = value;
    }

    /** The owner of the local target chronicle, or null for an outgoing remote request. */
    public String getOwnerPrincipal() {
        return ownerPrincipal;
    }

    /** Sets the owner. */
    public void setOwnerPrincipal(String value) {
        this.ownerPrincipal = value;
    }

    /** The requester's message, or null. */
    public String getMessage() {
        return message;
    }

    /** Sets the message. */
    public void setMessage(String value) {
        this.message = value;
    }

    /** The chronicle a local requester offers to share, or null. */
    public String getOfferShare() {
        return offerShare;
    }

    /** Sets the offered chronicle. */
    public void setOfferShare(String value) {
        this.offerShare = value;
    }

    /** True when a remote requester offered to share a chronicle of its own. */
    public boolean isOfferRemote() {
        return offerRemote;
    }

    /** Records a remote share offer. */
    public void setOfferRemote(boolean value) {
        this.offerRemote = value;
    }

    /** {@code pending}, {@code granted} or {@code declined}. */
    public String getStatus() {
        return status;
    }

    /** True when it is suppressed (a repeat to an owner who declined within 30 days). */
    public boolean isSuppressed() {
        return suppressed;
    }

    /** Marks it suppressed. */
    public void setSuppressed(boolean value) {
        this.suppressed = value;
    }

    /** How it was granted ({@code deliveredAs}, {@code role}, {@code packageId}), or null. */
    public Map<String, Object> getGrant() {
        return grant;
    }

    /** When it was made. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it was decided, or null. */
    public Instant getDecidedAt() {
        return decidedAt;
    }

    /** Records the owner's decision. */
    public void decide(String value, Map<String, Object> grantInfo, Instant now) {
        this.status = value;
        this.grant = grantInfo == null ? null : new LinkedHashMap<>(grantInfo);
        this.decidedAt = now;
    }
}
