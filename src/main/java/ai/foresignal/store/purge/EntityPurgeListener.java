package ai.foresignal.store.purge;

/**
 * A store derived from an entity's versions that the 039 per-entity purge must also erase (prompt 114: the
 * pattern-of-life stays, places and subjects). {@link PurgeFanout} calls every listener after the writer committed
 * the purge, each in a transaction of its own; a failing listener is logged and never undoes an earlier step.
 */
public interface EntityPurgeListener {

    /**
     * Erases what the listener derived from {@code sourceId}/{@code externalId}.
     *
     * @return the rows removed, for the log
     */
    int onEntityPurged(String sourceId, String externalId);
}
