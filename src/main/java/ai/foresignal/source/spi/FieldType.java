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
    /** An ISO-8601 instant carried as a string. */
    TIMESTAMP,
    OBJECT,
    ARRAY
}
