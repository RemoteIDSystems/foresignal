package ai.foresignal.stats;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * One row of {@code activity_rollup} (prompt 038): the activity of one source and kind in one H3 cell
 * during one bucket on the transaction axis. Rows hold no entity ids, so they survive retention without
 * keeping anything personal.
 *
 * <p>Measures (HEARTBEAT and KEYFRAME versions are never counted):
 *
 * <ul>
 *   <li>{@code versions}: persisted versions;
 *   <li>{@code entities}: distinct {@code external_id}s among them;
 *   <li>{@code firsts}: FIRST and REAPPEAR versions;
 *   <li>{@code tombstones}: TOMBSTONE versions;
 *   <li>{@code edges}: TRIGGER_EDGE versions;
 *   <li>{@code changes}: SIGNIFICANT and CHANGE versions (for the briefing digest's {@code changed}).
 * </ul>
 *
 * <p>Besides one row per occupied cell ({@link H3Cells#NO_CELL} for versions without a geometry), each
 * {@code (source, kind)} with activity has one {@link H3Cells#ALL_CELLS} row whose {@code entities} is
 * exact over every cell; queries without a spatial filter read only those rows. {@code cell_lon} and
 * {@code cell_lat} are the cell centre (null for the two special cells), used for bbox and area
 * filters at cell resolution.
 *
 * <p>Written only through SQL by the rollup job; the entity exists so Hibernate owns the table.
 */
@Entity
@Table(name = "activity_rollup", indexes = {
    @Index(name = "activity_rollup_resolution_bucket_idx", columnList = "resolution, bucket_start"),
    @Index(name = "activity_rollup_source_resolution_bucket_idx", columnList = "source_id, resolution, bucket_start")
})
public class ActivityRollupEntity {

    @EmbeddedId
    private ActivityRollupId id;

    @Column(name = "cell_lon")
    private Double cellLon;

    @Column(name = "cell_lat")
    private Double cellLat;

    @Column(name = "versions", nullable = false)
    private long versions;

    @Column(name = "entities", nullable = false)
    private long entities;

    @Column(name = "firsts", nullable = false)
    private long firsts;

    @Column(name = "tombstones", nullable = false)
    private long tombstones;

    @Column(name = "edges", nullable = false)
    private long edges;

    @Column(name = "changes", nullable = false)
    private long changes;

    protected ActivityRollupEntity() {
        // for JPA
    }

    /** The key. */
    public ActivityRollupId getId() {
        return id;
    }

    /** The cell centre longitude, or null. */
    public Double getCellLon() {
        return cellLon;
    }

    /** The cell centre latitude, or null. */
    public Double getCellLat() {
        return cellLat;
    }

    /** Persisted versions. */
    public long getVersions() {
        return versions;
    }

    /** Distinct entities. */
    public long getEntities() {
        return entities;
    }

    /** FIRST and REAPPEAR versions. */
    public long getFirsts() {
        return firsts;
    }

    /** TOMBSTONE versions. */
    public long getTombstones() {
        return tombstones;
    }

    /** TRIGGER_EDGE versions. */
    public long getEdges() {
        return edges;
    }

    /** SIGNIFICANT and CHANGE versions. */
    public long getChanges() {
        return changes;
    }
}
