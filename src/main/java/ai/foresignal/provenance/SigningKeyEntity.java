package ai.foresignal.provenance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One instance signing key (prompt 109): the Ed25519 key pair used for detached JWS over exports and
 * briefings. The private key is stored encrypted (AES-GCM under the data-volume key, the VAPID
 * pattern of prompt 031); the {@code kid} is the RFC 7638 thumbprint of the OKP JWK.
 */
@Entity
@Table(name = "signing_key")
public class SigningKeyEntity {

    /** The active status. */
    public static final String ACTIVE = "active";

    /** The retired status (kept for verification). */
    public static final String RETIRED = "retired";

    @Id
    @Column(name = "kid", length = 64, nullable = false)
    private String kid;

    @Column(name = "private_key", nullable = false)
    private byte[] privateKey;

    @Column(name = "public_key", nullable = false)
    private byte[] publicKey;

    @Column(name = "status", length = 16, nullable = false)
    private String status = ACTIVE;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "retired_at")
    private Instant retiredAt;

    /** JPA. */
    protected SigningKeyEntity() {}

    /** Creates a key row. */
    public SigningKeyEntity(String kid, byte[] privateKey, byte[] publicKey, Instant createdAt) {
        this.kid = kid;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.createdAt = createdAt;
    }

    public String getKid() {
        return kid;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(Instant retiredAt) {
        this.retiredAt = retiredAt;
    }
}
