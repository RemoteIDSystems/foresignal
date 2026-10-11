package ai.foresignal.source.spi;

/**
 * Per-record caps (Core-Data-Model §3).
 *
 * <p>A violation rejects that one record with a reason, not the batch: one malformed entry in a
 * feed of ten thousand should cost one entry.
 *
 * @param maxPayloadBytes serialized payload size
 * @param maxPayloadDepth nesting depth
 * @param maxPayloadKeys total keys
 * @param maxStringLength any single string value
 * @param maxGeometryVertices checked before geometry repair, so a pathological polygon cannot be
 *     expanded into one
 * @param maxObservers entries in {@code observers}
 */
public record RecordLimits(
        int maxPayloadBytes,
        int maxPayloadDepth,
        int maxPayloadKeys,
        int maxStringLength,
        int maxGeometryVertices,
        int maxObservers) {

    public static final int DEFAULT_MAX_PAYLOAD_BYTES = 64 * 1024;
    public static final int DEFAULT_MAX_PAYLOAD_DEPTH = 8;
    public static final int DEFAULT_MAX_PAYLOAD_KEYS = 512;
    public static final int DEFAULT_MAX_STRING_LENGTH = 8 * 1024;
    public static final int DEFAULT_MAX_GEOMETRY_VERTICES = 10_000;
    public static final int DEFAULT_MAX_OBSERVERS = 8;

    /** The limits from Core-Data-Model §3, used unless a descriptor overrides them. */
    public static RecordLimits defaults() {
        return new RecordLimits(
                DEFAULT_MAX_PAYLOAD_BYTES,
                DEFAULT_MAX_PAYLOAD_DEPTH,
                DEFAULT_MAX_PAYLOAD_KEYS,
                DEFAULT_MAX_STRING_LENGTH,
                DEFAULT_MAX_GEOMETRY_VERTICES,
                DEFAULT_MAX_OBSERVERS);
    }
}
