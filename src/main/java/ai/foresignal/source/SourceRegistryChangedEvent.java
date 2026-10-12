package ai.foresignal.source;

/**
 * Published when a dynamic source is registered, unregistered or redefined.
 *
 * <p>Lets the scheduler and the API notice a custom source without polling the registry.
 *
 * @param sourceId the source that appeared or disappeared
 * @param change what happened
 */
public record SourceRegistryChangedEvent(String sourceId, Change change) {

    public enum Change {
        REGISTERED,
        UNREGISTERED,
        /** A dynamic source's definition changed in place (prompt 024). */
        REDEFINED
    }

    public static SourceRegistryChangedEvent registered(String sourceId) {
        return new SourceRegistryChangedEvent(sourceId, Change.REGISTERED);
    }

    public static SourceRegistryChangedEvent unregistered(String sourceId) {
        return new SourceRegistryChangedEvent(sourceId, Change.UNREGISTERED);
    }

    /** A dynamic source was redefined (an edited custom source or a changed EDP manifest). */
    public static SourceRegistryChangedEvent redefined(String sourceId) {
        return new SourceRegistryChangedEvent(sourceId, Change.REDEFINED);
    }
}
