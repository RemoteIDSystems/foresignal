package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * What retention has done per source and kind (prompt 019): the {@code retention_state} table,
 * which {@code RetentionStateStore} loads at startup and rewrites after every run.
 *
 * <ul>
 *   <li>{@code floor_seq}: every version of this source (and kind) below it is gone. For TRACK it is
 *       the lower bound of the oldest remaining track leaf; for STANDARD the last completed delete's
 *       cutoff.
 *   <li>{@code purge_seq}: the watermark recorded when a head purge last removed at least one head of
 *       the source (kept on the {@link #ALL_KINDS} row only); 0 when it never has.
 * </ul>
 *
 * <p>Written only through native SQL by the store; the entity exists so Hibernate owns the table.
 */
@Entity
@Table(name = "retention_state")
public class RetentionStateEntity {

    /** The kind value of a row that covers every kind of its source. */
    public static final String ALL_KINDS = "*";

    @EmbeddedId
    private RetentionStateId id;

    @Column(name = "floor_seq", nullable = false)
    private long floorSeq;

    @Column(name = "purge_seq", nullable = false)
    private long purgeSeq;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "purged_at")
    private Instant purgedAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_run_at")
    private Instant lastRunAt;

    protected RetentionStateEntity() {
        // for JPA
    }

    /** Creates a row. */
    public RetentionStateEntity(RetentionStateId id, long floorSeq, long purgeSeq, Instant purgedAt, Instant lastRunAt) {
        this.id = id;
        this.floorSeq = floorSeq;
        this.purgeSeq = purgeSeq;
        this.purgedAt = purgedAt;
        this.lastRunAt = lastRunAt;
    }

    /** The id. */
    public RetentionStateId getId() {
        return id;
    }

    /** The floor seq. */
    public long getFloorSeq() {
        return floorSeq;
    }

    /** The purge seq. */
    public long getPurgeSeq() {
        return purgeSeq;
    }

    /** When heads were last purged, or null. */
    public Instant getPurgedAt() {
        return purgedAt;
    }

    /** When retention last ran for this row, or null. */
    public Instant getLastRunAt() {
        return lastRunAt;
    }
}
