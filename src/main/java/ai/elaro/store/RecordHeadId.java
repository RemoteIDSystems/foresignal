package ai.elaro.store;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/** Identifies one upstream record: its source plus that source's own id for it. */
@Embeddable
public class RecordHeadId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "external_id", length = 256, nullable = false)
    private String externalId;

    protected RecordHeadId() {
        // for JPA
    }

    public RecordHeadId(String sourceId, String externalId) {
        this.sourceId = sourceId;
        this.externalId = externalId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getExternalId() {
        return externalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecordHeadId other)) {
            return false;
        }
        return Objects.equals(sourceId, other.sourceId)
                && Objects.equals(externalId, other.externalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, externalId);
    }

    @Override
    public String toString() {
        return sourceId + "/" + externalId;
    }
}
