package ai.foresignal.notify.threshold;

import java.io.Serializable;
import java.util.Objects;

/**
 * The composite key of {@link SourceThresholdEntity} and {@link SourceNotifyStateEntity} (prompt SG-003): a source
 * threshold and its cooldown state belong to one owner principal and one source.
 */
public class SourceThresholdId implements Serializable {

    private String ownerPrincipalId;
    private String sourceId;

    /** For JPA. */
    public SourceThresholdId() {}

    /** The key for {@code ownerPrincipalId} and {@code sourceId}. */
    public SourceThresholdId(String ownerPrincipalId, String sourceId) {
        this.ownerPrincipalId = ownerPrincipalId;
        this.sourceId = sourceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof SourceThresholdId other
                && Objects.equals(ownerPrincipalId, other.ownerPrincipalId)
                && Objects.equals(sourceId, other.sourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ownerPrincipalId, sourceId);
    }
}
