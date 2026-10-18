package ai.foresignal.chronicle.network;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * A discovery salt (table {@code chronicle_salt}, prompt 134), stored like other secret material: encrypted with the
 * instance's secret key ({@code CredentialCipher}) and never exported or returned. The instance salt ({@code kind}
 * {@code instance}) is generated here and rotated yearly; a network salt ({@code kind} {@code network}) is installed
 * by an admin with the value agreed out of band with the peers of {@code network_id}, and is used for 90 days.
 */
@Entity
@Table(name = "chronicle_salt", indexes = @Index(name = "chronicle_salt_kind_idx", columnList = "kind, network_id"))
public class ChronicleSaltEntity {

    /** The instance salt's kind. */
    public static final String INSTANCE = "instance";

    /** A network salt's kind. */
    public static final String NETWORK = "network";

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "kind", length = 16, nullable = false)
    private String kind;

    @Column(name = "network_id", length = 64)
    private String networkId;

    @Column(name = "ciphertext", nullable = false)
    private byte[] ciphertext;

    @Column(name = "nonce", nullable = false)
    private byte[] nonce;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "rotates_at", nullable = false)
    private Instant rotatesAt;

    @Column(name = "retired_at")
    private Instant retiredAt;

    /** For JPA. */
    protected ChronicleSaltEntity() {}

    /**
     * A new salt.
     *
     * @param id its id
     * @param kind {@value #INSTANCE} or {@value #NETWORK}
     * @param networkId the network, or null for the instance salt
     * @param ciphertext the encrypted value
     * @param nonce the nonce
     * @param createdAt when it was created
     * @param rotatesAt when it is due for rotation
     */
    public ChronicleSaltEntity(String id, String kind, String networkId, byte[] ciphertext, byte[] nonce,
            Instant createdAt, Instant rotatesAt) {
        this.id = id;
        this.kind = kind;
        this.networkId = networkId;
        this.ciphertext = ciphertext.clone();
        this.nonce = nonce.clone();
        this.createdAt = createdAt;
        this.rotatesAt = rotatesAt;
    }

    /** The salt id. */
    public String getId() {
        return id;
    }

    /** {@value #INSTANCE} or {@value #NETWORK}. */
    public String getKind() {
        return kind;
    }

    /** The network, or null. */
    public String getNetworkId() {
        return networkId;
    }

    /** The encrypted value. */
    public byte[] getCiphertext() {
        return ciphertext.clone();
    }

    /** The nonce. */
    public byte[] getNonce() {
        return nonce.clone();
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it is due for rotation. */
    public Instant getRotatesAt() {
        return rotatesAt;
    }

    /** When it was retired, or null while current. */
    public Instant getRetiredAt() {
        return retiredAt;
    }

    /** Retires the salt. */
    public void retire(Instant at) {
        this.retiredAt = at;
    }

    /** True while the salt is current at {@code now}: not retired and not past its rotation date. */
    public boolean current(Instant now) {
        return retiredAt == null && now.isBefore(rotatesAt);
    }
}
