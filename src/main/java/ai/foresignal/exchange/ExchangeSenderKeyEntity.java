package ai.foresignal.exchange;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A sender's public keys pinned by an admin (table {@code exchange_sender_key}, prompt 132): the JWKS given with an
 * import ({@code jwks}), used before anything fetched from a peer for every later package of that instance.
 */
@Entity
@Table(name = "exchange_sender_key")
public class ExchangeSenderKeyEntity {

    @Id
    @Column(name = "instance_id", length = 128, nullable = false)
    private String instanceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "jwks", nullable = false)
    private Map<String, Object> jwks;

    @Column(name = "pinned_by", length = 128)
    private String pinnedBy;

    @Column(name = "pinned_at", nullable = false)
    private Instant pinnedAt;

    /** For JPA. */
    protected ExchangeSenderKeyEntity() {}

    /**
     * A pinned JWKS.
     *
     * @param instanceId the sending instance
     * @param jwks the JWKS document
     * @param pinnedBy the admin principal
     * @param pinnedAt when
     */
    public ExchangeSenderKeyEntity(String instanceId, Map<String, Object> jwks, String pinnedBy, Instant pinnedAt) {
        this.instanceId = instanceId;
        this.jwks = jwks;
        this.pinnedBy = pinnedBy;
        this.pinnedAt = pinnedAt;
    }

    /** The sending instance. */
    public String getInstanceId() {
        return instanceId;
    }

    /** The JWKS document. */
    public Map<String, Object> getJwks() {
        return jwks;
    }

    /** Replaces the pinned JWKS. */
    public void repin(Map<String, Object> jwks, String pinnedBy, Instant pinnedAt) {
        this.jwks = jwks;
        this.pinnedBy = pinnedBy;
        this.pinnedAt = pinnedAt;
    }

    /** The admin principal who pinned it. */
    public String getPinnedBy() {
        return pinnedBy;
    }

    /** When it was pinned. */
    public Instant getPinnedAt() {
        return pinnedAt;
    }
}
