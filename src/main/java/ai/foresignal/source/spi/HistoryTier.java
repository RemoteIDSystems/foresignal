package ai.foresignal.source.spi;

/**
 * How much history a source's records justify keeping (Core-Data-Model §3).
 *
 * <p>Fixed per source and not overridable by an operator, because the tier decides the shape of the
 * storage, not just its size.
 */
public enum HistoryTier {
    /**
     * Discrete records whose history is worth keeping as-is: earthquakes, alerts, outages.
     * Retention is a duration the operator chooses.
     */
    STANDARD,
    /**
     * High-rate position streams: aircraft, vessels, transit. These need a TTL and an
     * {@code OBSERVED} anchor, and their retention is fixed by the tier rather than configurable,
     * because a track is only meaningful as a continuous series.
     */
    TRACK;

    /** The lowercase name used on the wire and in metric tags. */
    public String wireValue() {
        return name().toLowerCase();
    }

    /**
     * The stored {@code record_version.history_tier} value, which is also the list-partition key
     * (Core-Data-Model §9): {@code STANDARD=0}, {@code TRACK=1}.
     */
    public short code() {
        return switch (this) {
            case STANDARD -> 0;
            case TRACK -> 1;
        };
    }

    /**
     * The tier stored as {@code code}.
     *
     * @throws IllegalArgumentException when no tier has that code
     */
    public static HistoryTier fromCode(short code) {
        return switch (code) {
            case 0 -> STANDARD;
            case 1 -> TRACK;
            default -> throw new IllegalArgumentException("Unknown history tier code " + code);
        };
    }
}
