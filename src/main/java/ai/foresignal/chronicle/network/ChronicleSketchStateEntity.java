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
 * The sketching state of one chronicle under one salt (table {@code chronicle_sketch_state}, prompt 134): the highest
 * captured seq already turned into keys, when the sketch last changed, the total key count at the last discovery (the
 * 10% trigger), whether discovery is due, and, under a network salt, the random sketch id sent to peers and when it was
 * withdrawn. Written with native SQL; Hibernate only owns the table definition.
 */
@Entity
@Table(name = "chronicle_sketch_state", uniqueConstraints = @UniqueConstraint(name = "chronicle_sketch_state_unique",
        columnNames = {"chronicle_id", "salt_id"}))
public class ChronicleSketchStateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "chronicle_id", length = 26, nullable = false)
    private String chronicleId;

    @Column(name = "salt_id", length = 64, nullable = false)
    private String saltId;

    @Column(name = "key_seq", nullable = false)
    private long keySeq;

    @Column(name = "total_keys", nullable = false)
    private long totalKeys;

    @Column(name = "discovered_keys", nullable = false)
    private long discoveredKeys;

    @Column(name = "discovery_due", nullable = false)
    private boolean discoveryDue;

    @Column(name = "sketched_at")
    private Instant sketchedAt;

    @Column(name = "sketch_id", length = 32)
    private String sketchId;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "withdrawn_at")
    private Instant withdrawnAt;

    /** For JPA. */
    protected ChronicleSketchStateEntity() {}

    /** The row id. */
    public Long getId() {
        return id;
    }

    /** The chronicle. */
    public String getChronicleId() {
        return chronicleId;
    }

    /** The salt. */
    public String getSaltId() {
        return saltId;
    }

    /** The highest captured seq turned into keys. */
    public long getKeySeq() {
        return keySeq;
    }

    /** The keys under this salt, all key types together. */
    public long getTotalKeys() {
        return totalKeys;
    }

    /** The total at the last discovery. */
    public long getDiscoveredKeys() {
        return discoveredKeys;
    }

    /** True when the sketch changed by more than 10% since the last discovery. */
    public boolean isDiscoveryDue() {
        return discoveryDue;
    }

    /** When the sketch last changed, or null. */
    public Instant getSketchedAt() {
        return sketchedAt;
    }

    /** The random sketch id under a network salt, or null. */
    public String getSketchId() {
        return sketchId;
    }

    /** When the sketch was last sent to peers, or null. */
    public Instant getSentAt() {
        return sentAt;
    }

    /** When the sketch was withdrawn from peers, or null. */
    public Instant getWithdrawnAt() {
        return withdrawnAt;
    }
}
