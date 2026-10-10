package ai.elaro.source.spi;

/**
 * Which timestamp a record's TTL counts from (Core-Data-Model §2).
 *
 * <p>Effective {@code validUntil = explicit ?? anchor + recordTtl}.
 */
public enum TtlAnchor {
    /**
     * {@code observedAt}. Required for {@link HistoryTier#TRACK}, and right for measurements, where
     * what matters is how old the reading is.
     */
    OBSERVED,
    /**
     * {@code receivedAt}. The {@link HistoryTier#STANDARD} default, and right for revised events,
     * where a record stays current until Elaro stops being told about it.
     */
    RECEIVED;

    public String wireValue() {
        return name().toLowerCase();
    }

    /** The default for {@code tier}: OBSERVED for TRACK, RECEIVED for STANDARD. */
    public static TtlAnchor defaultFor(HistoryTier tier) {
        return tier == HistoryTier.TRACK ? OBSERVED : RECEIVED;
    }
}
