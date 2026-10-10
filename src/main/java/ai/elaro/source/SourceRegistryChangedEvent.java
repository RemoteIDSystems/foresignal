package ai.elaro.source;

/**
 * Published when a dynamic source is registered or unregistered.
 *
 * <p>Lets the scheduler and the API notice a custom source without polling the registry.
 *
 * @param sourceId the source that appeared or disappeared
 * @param change what happened
 */
public record SourceRegistryChangedEvent(String sourceId, Change change) {

    public enum Change {
        REGISTERED,
        UNREGISTERED
    }

    public static SourceRegistryChangedEvent registered(String sourceId) {
        return new SourceRegistryChangedEvent(sourceId, Change.REGISTERED);
    }

    public static SourceRegistryChangedEvent unregistered(String sourceId) {
        return new SourceRegistryChangedEvent(sourceId, Change.UNREGISTERED);
    }
}
