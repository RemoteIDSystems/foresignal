package ai.foresignal.chronicle.network;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * A deleted chronicle remembered for the churn rule (table {@code chronicle_network_churn}, prompt 134): its owner,
 * when it was created and when it was deleted. Rows older than a day are pruned.
 */
@Entity
@Table(name = "chronicle_network_churn", indexes = @Index(name = "chronicle_network_churn_owner_idx",
        columnList = "owner, deleted_at"))
public class ChronicleChurnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "owner", length = 200, nullable = false)
    private String owner;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "deleted_at", nullable = false)
    private Instant deletedAt;

    /** For JPA. */
    protected ChronicleChurnEntity() {}

    /**
     * A deletion.
     *
     * @param owner the owner's principal id
     * @param createdAt when the chronicle was created
     * @param deletedAt when it was deleted
     */
    public ChronicleChurnEntity(String owner, Instant createdAt, Instant deletedAt) {
        this.owner = owner;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    /** The row id. */
    public Long getId() {
        return id;
    }

    /** The owner. */
    public String getOwner() {
        return owner;
    }

    /** When the deleted chronicle was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it was deleted. */
    public Instant getDeletedAt() {
        return deletedAt;
    }
}
