package ai.elaro.source;

/**
 * Published after a source's credentials are stored or removed.
 *
 * <p>Carries no values, only the fact of the change: an event is a thing that gets logged and
 * forwarded, which is the last place a secret should be.
 *
 * @param sourceId the source
 * @param present true when credentials are now stored, false when they were removed
 */
public record SourceCredentialsChangedEvent(String sourceId, boolean present) {

    public static SourceCredentialsChangedEvent stored(String sourceId) {
        return new SourceCredentialsChangedEvent(sourceId, true);
    }

    public static SourceCredentialsChangedEvent removed(String sourceId) {
        return new SourceCredentialsChangedEvent(sourceId, false);
    }
}
