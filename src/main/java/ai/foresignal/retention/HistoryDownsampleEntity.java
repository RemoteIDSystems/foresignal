package ai.foresignal.retention;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Bookkeeping of history downsampling (table {@code history_downsample}, prompt 044): one row per
 * TRACK leaf that was, or is being, rewritten into a thinned copy.
 *
 * <p>Written only with plain JDBC on the {@code PartitionManager} connection by
 * {@link HistoryDownsampler} and read by {@link DownsampleRanges}; the entity maps the table so Hibernate
 * validates it against the Flyway schema. {@code leaf_name} is the key: a FAILED attempt is overwritten by the next one, and a leaf's
 * row is removed once retention has dropped the leaf.
 */
@Entity
@Table(name = "history_downsample")
public class HistoryDownsampleEntity {

    @Id
    @Column(name = "leaf_name", length = 63, nullable = false)
    private String leafName;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "history_tier", nullable = false)
    private Short historyTier;

    @Column(name = "from_seq", nullable = false)
    private long fromSeq;

    @Column(name = "to_seq", nullable = false)
    private long toSeq;

    @Column(name = "default_interval_s", nullable = false)
    private int defaultIntervalSeconds;

    @Column(name = "rows_before")
    private Long rowsBefore;

    @Column(name = "rows_after")
    private Long rowsAfter;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "completed_at")
    private Instant completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private DownsampleStatus status;

    @Column(name = "error", columnDefinition = "text")
    private String error;

    /** For JPA. */
    protected HistoryDownsampleEntity() {}

    /** The leaf table name, for example {@code rv_t_20000000}. */
    public String getLeafName() {
        return leafName;
    }

    /** The tier code (1 = TRACK). */
    public Short getHistoryTier() {
        return historyTier;
    }

    /** The leaf's inclusive lower seq bound. */
    public long getFromSeq() {
        return fromSeq;
    }

    /** The leaf's exclusive upper seq bound. */
    public long getToSeq() {
        return toSeq;
    }

    /** {@code retention.trackDownsampleInterval} in seconds at the time of the rewrite. */
    public int getDefaultIntervalSeconds() {
        return defaultIntervalSeconds;
    }

    /** Rows in the leaf before the rewrite, or null while unknown. */
    public Long getRowsBefore() {
        return rowsBefore;
    }

    /** Rows in the thinned copy, or null until DONE. */
    public Long getRowsAfter() {
        return rowsAfter;
    }

    /** When the rewrite started. */
    public Instant getStartedAt() {
        return startedAt;
    }

    /** When it finished (DONE or FAILED), or null while RUNNING. */
    public Instant getCompletedAt() {
        return completedAt;
    }

    /** The state. */
    public DownsampleStatus getStatus() {
        return status;
    }

    /** Why a FAILED attempt gave up, or null. */
    public String getError() {
        return error;
    }
}
