package ai.foresignal.source.formats.cap;

/**
 * Limits applied by {@link CapParser} (prompt 067).
 *
 * @param maxBytes the largest document read, in bytes; a longer one is refused
 * @param maxElements the most elements read from one document, a guard against element floods
 */
public record CapParseOptions(long maxBytes, int maxElements) {

    /** The input cap of prompt 067: 2 MB. */
    public static final long DEFAULT_MAX_BYTES = 2L * 1024 * 1024;

    /** The default element cap. */
    public static final int DEFAULT_MAX_ELEMENTS = 50_000;

    /** The defaults: 2 MB and {@value #DEFAULT_MAX_ELEMENTS} elements. */
    public static final CapParseOptions DEFAULTS = new CapParseOptions(DEFAULT_MAX_BYTES, DEFAULT_MAX_ELEMENTS);

    /** Checks the limits. */
    public CapParseOptions {
        if (maxBytes <= 0) {
            throw new IllegalArgumentException("maxBytes must be positive");
        }
        if (maxElements <= 0) {
            throw new IllegalArgumentException("maxElements must be positive");
        }
    }
}
