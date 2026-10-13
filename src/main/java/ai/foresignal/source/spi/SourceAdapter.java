package ai.foresignal.source.spi;

/**
 * Anything that describes a source.
 *
 * <p>Adapters are Spring {@code @Component}s; {@link ai.foresignal.source.SourceRegistry} discovers them
 * by type. A duplicate id fails startup rather than letting one adapter silently shadow another.
 */
public interface SourceAdapter {

    /** This source's immutable description. Must return the same value every call. */
    SourceDescriptor descriptor();

    /**
     * Checks rules that span several parameters, after each one passed its own {@link ParamSpec}
     * (prompt 049: a SatDump station needs at least one of {@code apiUrl} and {@code inboxPath}).
     *
     * <p>{@code ParamValidator} reports every entry as a field error {@code params.<key>} of the 400
     * {@code VALIDATION_FAILED} problem. The default finds nothing.
     *
     * @param params the coerced parameters with defaults applied
     * @return parameter name to problem, in a stable order; empty when the combination is valid
     */
    default java.util.Map<String, String> checkParams(ValidatedParams params) {
        return java.util.Map.of();
    }

    /**
     * Checks rules that span several credential fields (prompt 062: {@code opensky-states} needs
     * {@code clientId} and {@code clientSecret} together or neither).
     *
     * <p>{@code SourceService.setCredentials} reports every entry as a field error {@code values.<key>} of
     * the 400 {@code VALIDATION_FAILED} problem, after the per-field checks. The default finds nothing.
     *
     * @param values the supplied credential values, never echoed in the problems
     * @return credential field name to problem, in a stable order; empty when the combination is valid
     */
    default java.util.Map<String, String> checkCredentials(java.util.Map<String, String> values) {
        return java.util.Map.of();
    }

    /** Convenience for {@code descriptor().id()}. */
    default String id() {
        return descriptor().id();
    }
}
