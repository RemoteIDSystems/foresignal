package ai.foresignal.chronicle;

/**
 * Told about chronicle lifecycle events that other components keep state for (prompt 134: the chronicle network
 * refreshes a stopped chronicle's sketch and forgets a deleted chronicle's keys, sketches and links). Called after the
 * change is saved; implementations must not throw.
 */
public interface ChronicleLifecycleListener {

    /**
     * Capture stopped.
     *
     * @param chronicle the stopped chronicle
     */
    default void stopped(ChronicleEntity chronicle) {}

    /**
     * The chronicle was deleted.
     *
     * @param chronicle the deleted chronicle (no longer stored)
     * @param owner its owner's principal id, or null
     */
    default void deleted(ChronicleEntity chronicle, String owner) {}
}
