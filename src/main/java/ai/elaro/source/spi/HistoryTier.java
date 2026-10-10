package ai.elaro.source.spi;

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

    public String wireValue() {
        return name().toLowerCase();
    }
}
