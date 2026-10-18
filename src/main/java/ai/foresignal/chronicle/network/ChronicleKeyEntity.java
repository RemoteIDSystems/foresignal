package ai.foresignal.chronicle.network;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * One keyed global evidence key of one of this instance's chronicles (table {@code chronicle_key}, prompt 134): the
 * 64-bit HMAC of the key under one salt, its key type and when the chronicle first saw it. Rows under the instance
 * salt also keep the key text and the object it came from (a canonical event or an entity key), so full disclosure can
 * list the shared canonical events and entities exactly; rows under a network salt keep neither. Key sets never leave
 * the instance. Written by {@link ChronicleSketcher} with native inserts; Hibernate only owns the table definition.
 */
@Entity
@Table(name = "chronicle_key", uniqueConstraints = @UniqueConstraint(name = "chronicle_key_unique",
        columnNames = {"chronicle_id", "salt_id", "key_type", "hkey"}), indexes = {
        @Index(name = "chronicle_key_hkey_idx", columnList = "salt_id, key_type, hkey")})
public class ChronicleKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "chronicle_id", length = 26, nullable = false)
    private String chronicleId;

    @Column(name = "salt_id", length = 64, nullable = false)
    private String saltId;

    @Column(name = "key_type", length = 16, nullable = false)
    private String keyType;

    @Column(name = "hkey", nullable = false)
    private long hkey;

    @Column(name = "first_seen", nullable = false)
    private Instant firstSeen;

    @Column(name = "key_text", length = 700)
    private String keyText;

    @Column(name = "ref", length = 200)
    private String ref;

    /** For JPA. */
    protected ChronicleKeyEntity() {}

    /** The row id. */
    public Long getId() {
        return id;
    }

    /** The chronicle. */
    public String getChronicleId() {
        return chronicleId;
    }

    /** The salt the key is hashed under. */
    public String getSaltId() {
        return saltId;
    }

    /** The key type. */
    public String getKeyType() {
        return keyType;
    }

    /** The 64-bit keyed hash. */
    public long getHkey() {
        return hkey;
    }

    /** When the chronicle first saw the key. */
    public Instant getFirstSeen() {
        return firstSeen;
    }

    /** The key text (instance salt only), or null. */
    public String getKeyText() {
        return keyText;
    }

    /** The object the key came from (instance salt only), or null. */
    public String getRef() {
        return ref;
    }
}
