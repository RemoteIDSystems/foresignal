package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Identifies one source's usage counters for one minute. */
@Embeddable
public class SourceUsageId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    /** The minute this row counts, truncated to the minute. */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "minute", nullable = false)
    private Instant minute;

    protected SourceUsageId() {
        // for JPA
    }

    public SourceUsageId(String sourceId, Instant minute) {
        this.sourceId = sourceId;
        this.minute = minute;
    }

    public String getSourceId() {
        return sourceId;
    }

    public Instant getMinute() {
        return minute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SourceUsageId other)) {
            return false;
        }
        return Objects.equals(sourceId, other.sourceId) && Objects.equals(minute, other.minute);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, minute);
    }

    @Override
    public String toString() {
        return sourceId + "@" + minute;
    }
}
