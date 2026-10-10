package ai.elaro.source.spi;

/**
 * Where {@code observedAt} comes from (Core-Data-Model §2).
 *
 * <p>Never mixed within a source: an entity that switched between timestamp fields from record to
 * record would make the late-observation check meaningless.
 */
public enum ObservedAtMode {
    /**
     * The adapter supplies it. A record without {@code observedAt} is rejected with
     * {@code MISSING_OBSERVED_AT}.
     */
    UPSTREAM,
    /**
     * The framework sets {@code observedAt = receivedAt} for every record of the source and flags
     * {@code TIME_IMPUTED}. Any adapter value is ignored. For upstreams that publish no usable
     * timestamp at all.
     */
    RECEIVED;

    public String wireValue() {
        return name().toLowerCase();
    }
}
