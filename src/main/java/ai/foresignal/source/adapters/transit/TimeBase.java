package ai.foresignal.source.adapters.transit;

/**
 * Where a GTFS-RT VehiclePositions feed's {@code observedAt} comes from (Core-Data-Model §2, GTFS-RT row; §3 rule
 * 13). Fixed per feed in the descriptor's feed entry, never chosen per record.
 */
public enum TimeBase {

    /**
     * {@code VehiclePosition.timestamp}. A vehicle without one is dropped and counted
     * ({@code foresignal.transit.untimed}), never given the header time. Chosen when at least 95% of a feed's
     * entities carry a timestamp.
     */
    VEHICLE,

    /** {@code FeedHeader.timestamp} for every vehicle of the message. */
    HEADER
}
