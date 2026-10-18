package ai.foresignal.cli.output;

/**
 * The {@code --output} formats.
 */
public enum OutputFormat {
    /** Aligned columns for people (the default). */
    table,
    /** Pretty-printed JSON, exactly as the API returned it. */
    json,
    /** One compact JSON document per line (one per list item). */
    ndjson
}
