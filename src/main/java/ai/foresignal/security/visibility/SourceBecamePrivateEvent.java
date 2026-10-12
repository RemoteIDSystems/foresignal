package ai.foresignal.security.visibility;

import java.util.Objects;

/**
 * Published by {@link SourceVisibility} when a registered source becomes private (an operator
 * tightening, a custom source switched to {@code private}, or a new private descriptor), after the
 * change is committed. Listeners delete what a private source may not keep, such as its embedding
 * rows (prompt 039, §2 "Search and embeddings").
 *
 * @param sourceId the source that is now private
 */
public record SourceBecamePrivateEvent(String sourceId) {

    /** Requires the id. */
    public SourceBecamePrivateEvent {
        Objects.requireNonNull(sourceId, "sourceId");
    }
}
