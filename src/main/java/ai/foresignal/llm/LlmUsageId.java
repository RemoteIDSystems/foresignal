package ai.foresignal.llm;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Identifies one provider's usage counters for one minute. */
@Embeddable
public class LlmUsageId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "provider_id", length = 64, nullable = false)
    private String providerId;

    /** The minute this row counts, truncated to the minute. */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "minute", nullable = false)
    private Instant minute;

    protected LlmUsageId() {
        // for JPA
    }

    /** Creates the id. */
    public LlmUsageId(String providerId, Instant minute) {
        this.providerId = providerId;
        this.minute = minute;
    }

    /** The provider. */
    public String getProviderId() {
        return providerId;
    }

    /** The minute. */
    public Instant getMinute() {
        return minute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LlmUsageId other)) {
            return false;
        }
        return Objects.equals(providerId, other.providerId) && Objects.equals(minute, other.minute);
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerId, minute);
    }

    @Override
    public String toString() {
        return providerId + "@" + minute;
    }
}
