package ai.foresignal.source.spi;

/**
 * The places area of interest as an adapter sees it (prompt 047; Weather-Imagery-Design §5.1).
 *
 * <p>Adapters depend only on SPI types (Architecture §3), so the AOI reaches them through this
 * interface; {@code ai.foresignal.place.PlacesAoiService} implements it. The envelope is the
 * bounding box of the union of every place buffered to at least {@code max(radiusKm, 150 km)}, or
 * {@code weather.defaultAoi} (the Texas box) when there are no places.
 *
 * <p>{@link #version()} changes whenever the AOI is recomputed ({@code PlacesAoiChangedEvent}). An
 * adapter that filters by the AOI stores the version it filtered with and re-filters when it moves,
 * which is how it refreshes on the event without a dependency on the place package.
 */
public interface AreaOfInterest {

    /** The AOI's {@code [minLon, minLat, maxLon, maxLat]}, a fresh array on every call. */
    double[] envelope();

    /** The AOI version; any change means the envelope may have moved. */
    long version();

    /**
     * Registers {@code sourceId} as an AOI consumer, so it gets the health warning while the default
     * AOI is in force.
     */
    void registerConsumer(String sourceId);

    /**
     * True when the box {@code [minLon, minLat, maxLon, maxLat]} intersects {@code envelope}
     * (edges touching count).
     */
    static boolean intersects(double[] envelope, double minLon, double minLat, double maxLon, double maxLat) {
        return minLon <= envelope[2] && maxLon >= envelope[0] && minLat <= envelope[3] && maxLat >= envelope[1];
    }

    /** True when the point lies inside {@code envelope} (edges included). */
    static boolean contains(double[] envelope, double lon, double lat) {
        return lon >= envelope[0] && lon <= envelope[2] && lat >= envelope[1] && lat <= envelope[3];
    }
}
