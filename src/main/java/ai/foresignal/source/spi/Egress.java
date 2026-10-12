package ai.foresignal.source.spi;

/**
 * Where a source's records may be served (descriptor {@code egress}, OpenAPI
 * {@code SourceDescriptor.egress}).
 *
 * <p>A default the operator can override per source (prompt 109); prompt 062 and the shared
 * {@code EgressPolicy} enforce it on every egress path. Prompt 024 sets it from an EDP manifest's
 * {@code licence.redistribution}.
 */
public enum Egress {
    /** May be served on every path the caller is allowed. */
    OPEN,
    /**
     * Served to this instance's own API clients and notifications only, never re-served by sinks,
     * federation, share links, feed keys or off-box exports.
     */
    LOCAL_ONLY;

    /** The wire value used in the API. */
    public String wireValue() {
        return name();
    }
}
