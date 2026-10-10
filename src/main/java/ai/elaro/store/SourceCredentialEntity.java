package ai.elaro.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One upstream's credential, encrypted with AES-256-GCM (Architecture §7.2).
 *
 * <p>The key lives in a file on the data volume, never in this table, so a database dump alone
 * does not reveal upstream keys.
 */
@Entity
@Table(name = "source_credential")
public class SourceCredentialEntity {

    @Id
    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "ciphertext", nullable = false)
    private byte[] ciphertext;

    @Column(name = "nonce", nullable = false)
    private byte[] nonce;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SourceCredentialEntity() {
        // for JPA
    }

    public SourceCredentialEntity(String sourceId, byte[] ciphertext, byte[] nonce, Instant updatedAt) {
        this.sourceId = sourceId;
        this.ciphertext = ciphertext;
        this.nonce = nonce;
        this.updatedAt = updatedAt;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public byte[] getCiphertext() {
        return ciphertext;
    }

    public void setCiphertext(byte[] ciphertext) {
        this.ciphertext = ciphertext;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
