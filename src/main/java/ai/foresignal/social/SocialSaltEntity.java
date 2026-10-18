package ai.foresignal.social;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * The instance social salt (prompt 135, table {@code social_salt}), stored with the secret key material: encrypted
 * with the instance's secret key ({@code CredentialCipher}), never exported or returned. It keys every
 * {@code authorKey}, so it is generated once and never rotated (a rotation would split every distinct-author count).
 */
@Entity
@Table(name = "social_salt")
public class SocialSaltEntity {

    /** The id of the one row. */
    public static final String INSTANCE = "instance";

    @Id
    @Column(name = "id", length = 32, nullable = false)
    private String id;

    @Column(name = "ciphertext", nullable = false)
    private byte[] ciphertext;

    @Column(name = "nonce", nullable = false)
    private byte[] nonce;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** For JPA. */
    protected SocialSaltEntity() {}

    /**
     * The salt row.
     *
     * @param ciphertext the encrypted salt
     * @param nonce the nonce
     * @param createdAt when it was generated
     */
    public SocialSaltEntity(byte[] ciphertext, byte[] nonce, Instant createdAt) {
        this.id = INSTANCE;
        this.ciphertext = ciphertext.clone();
        this.nonce = nonce.clone();
        this.createdAt = createdAt;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The encrypted salt. */
    public byte[] getCiphertext() {
        return ciphertext.clone();
    }

    /** The nonce. */
    public byte[] getNonce() {
        return nonce.clone();
    }

    /** When the salt was generated. */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
