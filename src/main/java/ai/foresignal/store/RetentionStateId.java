package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Identifies one {@code retention_state} row: a source and a kind, where kind
 * {@link RetentionStateEntity#ALL_KINDS} stands for every kind of the source (prompt 019).
 */
@Embeddable
public class RetentionStateId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "kind", length = 64, nullable = false)
    private String kind;

    protected RetentionStateId() {
        // for JPA
    }

    /** Creates the id. */
    public RetentionStateId(String sourceId, String kind) {
        this.sourceId = sourceId;
        this.kind = kind;
    }

    /** The source. */
    public String getSourceId() {
        return sourceId;
    }

    /** The kind, or {@link RetentionStateEntity#ALL_KINDS}. */
    public String getKind() {
        return kind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RetentionStateId other)) {
            return false;
        }
        return Objects.equals(sourceId, other.sourceId) && Objects.equals(kind, other.kind);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, kind);
    }

    @Override
    public String toString() {
        return sourceId + "/" + kind;
    }
}
