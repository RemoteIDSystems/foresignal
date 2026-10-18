package ai.foresignal.chronicle.network;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A sketch package received from a peer (table {@code chronicle_remote_sketch}, prompt 134): a fingerprint of one of
 * its {@code network}-discoverable chronicles under the shared network salt. It holds no content: MinHash slots per key
 * type, log2 size bands, the capture span at ISO-week granularity, and the title or owner handle only when that owner
 * revealed them. It expires 30 days after it was last refreshed and only ever yields opaque links.
 */
@Entity
@Table(name = "chronicle_remote_sketch", uniqueConstraints = @UniqueConstraint(name = "chronicle_remote_sketch_key",
        columnNames = {"instance_id", "sketch_id"}), indexes = {
        @Index(name = "chronicle_remote_sketch_salt_idx", columnList = "salt_id"),
        @Index(name = "chronicle_remote_sketch_expiry_idx", columnList = "expires_at")})
public class ChronicleRemoteSketchEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "peer_id", length = 64)
    private String peerId;

    @Column(name = "instance_id", length = 128, nullable = false)
    private String instanceId;

    @Column(name = "network_id", length = 64, nullable = false)
    private String networkId;

    @Column(name = "salt_id", length = 64, nullable = false)
    private String saltId;

    @Column(name = "sketch_id", length = 64, nullable = false)
    private String sketchId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "key_types", nullable = false)
    private List<String> keyTypes = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "minhash", nullable = false)
    private Map<String, String> minhash = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "size_band", nullable = false)
    private Map<String, Integer> sizeBand = new LinkedHashMap<>();

    @Column(name = "capture_from", length = 10, nullable = false)
    private String captureFrom;

    @Column(name = "capture_to", length = 10)
    private String captureTo;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "owner_handle", length = 64)
    private String ownerHandle;

    @Column(name = "tlp", length = 16, nullable = false)
    private String tlp;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** For JPA. */
    protected ChronicleRemoteSketchEntity() {}

    /**
     * A received sketch.
     *
     * @param id its ULID here
     * @param instanceId the sending instance
     * @param sketchId the sender's sketch id
     */
    public ChronicleRemoteSketchEntity(String id, String instanceId, String sketchId) {
        this.id = id;
        this.instanceId = instanceId;
        this.sketchId = sketchId;
    }

    /** The id here. */
    public String getId() {
        return id;
    }

    /** The peer it came through, or null. */
    public String getPeerId() {
        return peerId;
    }

    /** Sets the peer. */
    public void setPeerId(String value) {
        this.peerId = value;
    }

    /** The sending instance. */
    public String getInstanceId() {
        return instanceId;
    }

    /** The network. */
    public String getNetworkId() {
        return networkId;
    }

    /** Sets the network. */
    public void setNetworkId(String value) {
        this.networkId = value;
    }

    /** The local id of the network salt it was received under. */
    public String getSaltId() {
        return saltId;
    }

    /** Sets the salt. */
    public void setSaltId(String value) {
        this.saltId = value;
    }

    /** The sender's sketch id. */
    public String getSketchId() {
        return sketchId;
    }

    /** The link endpoint {@code remote:{instanceId}/chronicle:{sketchId}}. */
    public String node() {
        return NetworkVocabulary.remoteNode(instanceId, sketchId);
    }

    /** The key types it carries. */
    public List<String> getKeyTypes() {
        return keyTypes == null ? List.of() : keyTypes;
    }

    /** Sets the key types. */
    public void setKeyTypes(List<String> value) {
        this.keyTypes = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** Key type → base64 MinHash slots. */
    public Map<String, String> getMinhash() {
        return minhash == null ? Map.of() : minhash;
    }

    /** Sets the MinHash slots. */
    public void setMinhash(Map<String, String> value) {
        this.minhash = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** Key type → log2 size band. */
    public Map<String, Integer> getSizeBand() {
        return sizeBand == null ? Map.of() : sizeBand;
    }

    /** Sets the size bands. */
    public void setSizeBand(Map<String, Integer> value) {
        this.sizeBand = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** The first ISO week of capture ({@code 2026-W40}). */
    public String getCaptureFrom() {
        return captureFrom;
    }

    /** The last ISO week of capture, or null while it runs. */
    public String getCaptureTo() {
        return captureTo;
    }

    /** Sets the capture span. */
    public void setCaptureSpan(String from, String to) {
        this.captureFrom = from;
        this.captureTo = to;
    }

    /** The revealed title, or null. */
    public String getTitle() {
        return title;
    }

    /** Sets the revealed title. */
    public void setTitle(String value) {
        this.title = value;
    }

    /** The revealed owner handle, or null. */
    public String getOwnerHandle() {
        return ownerHandle;
    }

    /** Sets the revealed owner handle. */
    public void setOwnerHandle(String value) {
        this.ownerHandle = value;
    }

    /** The TLP label. */
    public String getTlp() {
        return tlp;
    }

    /** Sets the TLP label. */
    public void setTlp(String value) {
        this.tlp = value;
    }

    /** When the sender issued it. */
    public Instant getIssuedAt() {
        return issuedAt;
    }

    /** Sets when it was issued. */
    public void setIssuedAt(Instant value) {
        this.issuedAt = value;
    }

    /** When it was last received. */
    public Instant getReceivedAt() {
        return receivedAt;
    }

    /** When it expires unless refreshed. */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /** Records a (re)delivery at {@code now}: it now expires 30 days later. */
    public void refreshed(Instant now) {
        this.receivedAt = now;
        this.expiresAt = now.plus(NetworkVocabulary.SKETCH_EXPIRY);
    }
}
