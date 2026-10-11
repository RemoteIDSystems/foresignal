package ai.foresignal.source.spi;

/**
 * The JSON type a declared payload field holds (descriptor {@code fieldSpecs}, Gap-Analysis §8.A
 * item 1).
 *
 * <p>Display and schema metadata only: the normalizer does not coerce payload values to these types.
 */
public enum FieldType {
    STRING,
    INTEGER,
    NUMBER,
    BOOLEAN,
    /**
     * An instant: an ISO-8601 string, or an integer count since the Unix epoch in the field's
     * declared unit ({@code ms} or {@code s}), as some upstreams send it verbatim (USGS
     * {@code time} and {@code updated} are epoch milliseconds).
     */
    TIMESTAMP,
    OBJECT,
    ARRAY
}
