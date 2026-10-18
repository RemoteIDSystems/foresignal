package ai.foresignal.exchange;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A foreign object received in a package (table {@code received_object}, prompt 132): its
 * {@code remote:{instanceId}/{type}:{id}} reference and the local snapshot of the envelope the sender cited. It is
 * read-only and visible exactly to the recipients of its package.
 */
@Entity
@Table(name = "received_object", indexes = {
    @Index(name = "received_object_ref_idx", columnList = "remote_ref"),
    @Index(name = "received_object_package_idx", columnList = "received_id, remote_ref", unique = true)
})
public class ReceivedObjectEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "received_id", length = 26, nullable = false)
    private String receivedId;

    @Column(name = "remote_ref", length = 1100, nullable = false)
    private String remoteRef;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "envelope", nullable = false)
    private Map<String, Object> envelope;

    /** For JPA. */
    protected ReceivedObjectEntity() {}

    /**
     * A new received object.
     *
     * @param id the ULID
     * @param receivedId the received package
     * @param remoteRef the remote reference
     * @param envelope the sender's envelope snapshot
     */
    public ReceivedObjectEntity(String id, String receivedId, String remoteRef, Map<String, Object> envelope) {
        this.id = id;
        this.receivedId = receivedId;
        this.remoteRef = remoteRef;
        this.envelope = envelope;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The received package. */
    public String getReceivedId() {
        return receivedId;
    }

    /** The remote reference. */
    public String getRemoteRef() {
        return remoteRef;
    }

    /** The sender's envelope snapshot. */
    public Map<String, Object> getEnvelope() {
        return envelope;
    }
}
