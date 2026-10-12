package ai.foresignal.inbound.edp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * One external id staged for a snapshot ({@code edp_snapshot_stage}, prompt 024). Written and read
 * with plain SQL by {@link EdpSnapshotService}; the entity exists so Hibernate owns the table.
 */
@Entity
@Table(name = "edp_snapshot_stage", indexes = @Index(name = "edp_snapshot_stage_key_idx",
        columnList = "snapshot_key, external_id", unique = true))
public class EdpSnapshotStageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "snapshot_key", length = 200, nullable = false)
    private String snapshotKey;

    @Column(name = "external_id", length = 320, nullable = false)
    private String externalId;

    /** For JPA. */
    protected EdpSnapshotStageEntity() {}

    /** The generated id. */
    public Long getId() {
        return id;
    }

    /** The snapshot's key. */
    public String getSnapshotKey() {
        return snapshotKey;
    }

    /** The staged external id. */
    public String getExternalId() {
        return externalId;
    }
}
