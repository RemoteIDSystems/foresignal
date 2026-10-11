package ai.foresignal.stream;

import java.util.List;

/**
 * The live updates and touches one committed ingest batch wrote (Core-Data-Model §10.1).
 *
 * <p>Published after the batch's {@link ChangeBatch}, never before.
 *
 * @param sourceId the source
 * @param updates at most one per entity
 * @param touches at most one per entity, for entities without an update in this batch
 */
public record LiveBatch(String sourceId, List<LiveUpdate> updates, List<Touch> touches) {

    public LiveBatch {
        updates = updates == null ? List.of() : List.copyOf(updates);
        touches = touches == null ? List.of() : List.copyOf(touches);
    }

    /** True when the batch carries neither updates nor touches. */
    public boolean isEmpty() {
        return updates.isEmpty() && touches.isEmpty();
    }
}
