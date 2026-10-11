package ai.foresignal.ingest;

import java.util.Map;

/**
 * Receives the entity keys of persisted entities, after the writer's transaction commits
 * (prompt 009 calls every listener bean; prompt 098's entity registry implements one).
 *
 * <p>Called on the writer thread, so an implementation must be quick and must not block; anything
 * slow belongs on its own executor.
 */
@FunctionalInterface
public interface EntityKeyListener {

    /**
     * Called once per persisted entity with non-empty keys.
     *
     * @param sourceId the source
     * @param externalId the entity
     * @param keys key type to normalized value
     */
    void onEntityKeys(String sourceId, String externalId, Map<String, String> keys);
}
