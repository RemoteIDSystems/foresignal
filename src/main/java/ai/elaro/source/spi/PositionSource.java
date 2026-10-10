package ai.elaro.source.spi;

/** How a record's position was determined (Core-Data-Model §14). */
public enum PositionSource {
    /** Reported by the entity itself, for example ADS-B or AIS GNSS. */
    REPORTED,
    /** Computed by the upstream from receiver timing, for example MLAT. */
    CALCULATED,
    /** Inferred from a schedule or a route rather than measured. */
    INFERRED,
    /** Taken from a fixed location record, for example a station's catalogue entry. */
    STATIC,
    UNKNOWN;

    public String wireValue() {
        return name().toLowerCase();
    }
}
