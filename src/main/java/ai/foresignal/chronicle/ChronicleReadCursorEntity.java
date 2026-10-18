package ai.foresignal.chronicle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * A principal's read position in a chronicle's thread (table {@code chronicle_read_cursor}, prompt 131, the 033
 * pattern): every item with an id at or below {@code up_to_id} is read. Written by native upsert; one row per
 * {@code (chronicle_id, principal_id)}.
 */
@Entity
@Table(name = "chronicle_read_cursor", uniqueConstraints = @UniqueConstraint(name = "chronicle_read_cursor_key",
        columnNames = {"chronicle_id", "principal_id"}))
public class ChronicleReadCursorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "chronicle_id", length = 26, nullable = false)
    private String chronicleId;

    @Column(name = "principal_id", length = 200, nullable = false)
    private String principalId;

    @Column(name = "up_to_id", nullable = false)
    private long upToId;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected ChronicleReadCursorEntity() {}

    /** The row id. */
    public Long getId() {
        return id;
    }

    /** The chronicle. */
    public String getChronicleId() {
        return chronicleId;
    }

    /** The principal. */
    public String getPrincipalId() {
        return principalId;
    }

    /** The last read item id. */
    public long getUpToId() {
        return upToId;
    }

    /** When the cursor last moved. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
