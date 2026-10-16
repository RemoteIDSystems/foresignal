package ai.foresignal.export;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * How far the cold archive has written one retention scope (prompt 111, table
 * {@code archive_watermark}): every row of the scope with a seq below {@code throughSeq} is in a
 * verified part file. The next run archives only from here, so a delete that failed after the write
 * never causes the same rows to be archived twice.
 *
 * <p>The id is {@code {tier}:{sourceId}:{scope}}, the scope being {@code *} (the whole source), a kind,
 * or {@code !k1,k2} (every kind but those), as {@code RetentionPlanner.StandardScope#scopeKey}.
 */
@Entity
@Table(name = "archive_watermark")
public class ArchiveWatermarkEntity {

    @Id
    @Column(name = "id", length = 512, nullable = false)
    private String id;

    @Column(name = "through_seq", nullable = false)
    private long throughSeq;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** JPA. */
    protected ArchiveWatermarkEntity() {}

    /** Creates a watermark. */
    public ArchiveWatermarkEntity(String id, long throughSeq, Instant updatedAt) {
        this.id = id;
        this.throughSeq = throughSeq;
        this.updatedAt = updatedAt;
    }

    /** The scope id. */
    public String getId() {
        return id;
    }

    /** Rows below this seq are archived. */
    public long getThroughSeq() {
        return throughSeq;
    }

    /** Raises the watermark (never lowers it). */
    public void advance(long seq, Instant at) {
        if (seq > throughSeq) {
            throughSeq = seq;
        }
        updatedAt = at;
    }

    /** When the watermark last moved. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** The id of a scope. */
    public static String id(String tier, String sourceId, String scope) {
        return tier + ":" + sourceId + ":" + scope;
    }
}
