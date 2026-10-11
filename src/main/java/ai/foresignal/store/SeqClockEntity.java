package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Maps wall-clock minutes to the first {@code seq} ingested in them (Core-Data-Model §8).
 *
 * <p>The writer upserts the current minute on every batch. {@code cutoffSeq(t)} is the
 * {@code first_seq} of the latest minute at or before {@code t}: every row with a smaller seq was
 * ingested before {@code t}. asOf queries and retention use it to turn a time into a seq bound
 * without an index on {@code record_version.ingested_at}.
 */
@Entity
@Table(name = "seq_clock")
public class SeqClockEntity {

    /** The minute, truncated, in UTC. */
    @Id
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "minute", nullable = false)
    private Instant minute;

    /** The first seq allocated in that minute. */
    @Column(name = "first_seq", nullable = false)
    private long firstSeq;

    protected SeqClockEntity() {
        // for JPA
    }

    /** Creates the mapping {@code minute -> firstSeq}. */
    public SeqClockEntity(Instant minute, long firstSeq) {
        this.minute = minute;
        this.firstSeq = firstSeq;
    }

    /** The minute. */
    public Instant getMinute() {
        return minute;
    }

    /** The first seq allocated in the minute. */
    public long getFirstSeq() {
        return firstSeq;
    }

    /** Sets the first seq allocated in the minute. */
    public void setFirstSeq(long firstSeq) {
        this.firstSeq = firstSeq;
    }
}
