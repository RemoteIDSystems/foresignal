package ai.foresignal.stats;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * The primary key of one {@code activity_rollup} row (prompt 038): {@code (resolution, bucket_start,
 * source_id, kind, cell)}.
 *
 * <p>{@code resolution} is generic so prompt 119 can add region resolutions (2 = region-hour,
 * 3 = region-day) without a redesign.
 */
@Embeddable
public class ActivityRollupId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "resolution", nullable = false)
    private short resolution;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "bucket_start", nullable = false)
    private Instant bucketStart;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "kind", length = 64, nullable = false)
    private String kind;

    @Column(name = "cell", nullable = false)
    private long cell;

    protected ActivityRollupId() {
        // for JPA
    }

    /** Creates the key. */
    public ActivityRollupId(short resolution, Instant bucketStart, String sourceId, String kind, long cell) {
        this.resolution = resolution;
        this.bucketStart = bucketStart;
        this.sourceId = sourceId;
        this.kind = kind;
        this.cell = cell;
    }

    /** 0 = hour, 1 = day ({@link RollupResolution}). */
    public short getResolution() {
        return resolution;
    }

    /** The bucket start on the transaction axis. */
    public Instant getBucketStart() {
        return bucketStart;
    }

    /** The source. */
    public String getSourceId() {
        return sourceId;
    }

    /** The kind. */
    public String getKind() {
        return kind;
    }

    /** The H3 cell, {@link H3Cells#NO_CELL} or {@link H3Cells#ALL_CELLS}. */
    public long getCell() {
        return cell;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ActivityRollupId other)) {
            return false;
        }
        return resolution == other.resolution && cell == other.cell && Objects.equals(bucketStart, other.bucketStart)
                && Objects.equals(sourceId, other.sourceId) && Objects.equals(kind, other.kind);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resolution, bucketStart, sourceId, kind, cell);
    }
}
