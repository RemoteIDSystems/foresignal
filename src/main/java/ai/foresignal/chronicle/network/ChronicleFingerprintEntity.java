package ai.foresignal.chronicle.network;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * The MinHash sketch of one chronicle's key set of one key type under one salt (table {@code chronicle_fingerprint},
 * prompt 134): the {@value MinHashSketch#PERMUTATIONS} slots as bytes and the exact set size. Written by
 * {@link ChronicleSketcher} with native upserts; Hibernate only owns the table definition.
 */
@Entity
@Table(name = "chronicle_fingerprint", uniqueConstraints = @UniqueConstraint(name = "chronicle_fingerprint_unique",
        columnNames = {"chronicle_id", "key_type", "salt_id"}))
public class ChronicleFingerprintEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "chronicle_id", length = 26, nullable = false)
    private String chronicleId;

    @Column(name = "key_type", length = 16, nullable = false)
    private String keyType;

    @Column(name = "salt_id", length = 64, nullable = false)
    private String saltId;

    @Column(name = "minhash", nullable = false)
    private byte[] minhash;

    @Column(name = "size", nullable = false)
    private long size;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected ChronicleFingerprintEntity() {}

    /** The row id. */
    public Long getId() {
        return id;
    }

    /** The chronicle. */
    public String getChronicleId() {
        return chronicleId;
    }

    /** The key type. */
    public String getKeyType() {
        return keyType;
    }

    /** The salt. */
    public String getSaltId() {
        return saltId;
    }

    /** The signature bytes. */
    public byte[] getMinhash() {
        return minhash == null ? null : minhash.clone();
    }

    /** The exact set size. */
    public long getSize() {
        return size;
    }

    /** When it last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
