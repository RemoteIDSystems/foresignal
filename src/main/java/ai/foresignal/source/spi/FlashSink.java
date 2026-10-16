package ai.foresignal.source.spi;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalDouble;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * The GLM flash store as an adapter sees it: {@code ctx.rasters().flashes()} (prompt 048,
 * Weather-Imagery-Design §2.4 and §2.6). Implemented by {@code ai.foresignal.raster.lightning.FlashStore}.
 *
 * <p>Two stores behind one interface:
 *
 * <ul>
 *   <li><b>Archive:</b> {@code ${FORESIGNAL_DATA_DIR}/rasters/glm/{sat}/{yyyymmdd}.bin}, append-only,
 *       16 bytes per flash (little-endian {@code int32 msOfDayUtc}, {@code float32 lat},
 *       {@code float32 lon}, {@code float32 energyFj}), the full field of view, kept 14 days.
 *   <li><b>Ring:</b> in memory, the last 60 minutes, only flashes inside the places AOI (or CONUS in the
 *       large profile with {@code foresignal.raster.glm-ring-scope=conus}).
 * </ul>
 *
 * <p>Consumers tell "no lightning" from "no data" with {@link #newestFlashFileTime()}: an empty answer
 * from the read methods means nothing only when the newest processed file is recent. The read methods
 * answer from the ring, so they only see the last 60 minutes inside the ring's scope.
 *
 * <p>During a probe the sink is {@linkplain #probing() probing}: {@link #append} writes nothing.
 */
public interface FlashSink {

    /**
     * Appends the flashes of one processed GLM file and marks the file processed.
     *
     * <p>Every flash goes to the archive of its own UTC day (a granule can straddle midnight), which is
     * fsynced before this returns; those inside the ring's scope also go to the ring. Call it for every
     * processed file, also with {@link FlashBatch#EMPTY}, because the call is what advances
     * {@link #newestFlashFileTime()}.
     *
     * @param satellite the platform, for example {@code G19}; names the archive directory
     * @param flashes the file's flashes; the adapter has already dropped every flash whose
     *     {@code flash_quality_flag} is not 0
     * @param fileTime the end of the file's coverage ({@code e} time)
     */
    void append(String satellite, FlashBatch flashes, Instant fileTime);

    /**
     * Appends flashes tagged with their origin (prompt 107): {@code internet} for the NODD
     * {@code goes-glm} source, {@code local} for a {@code glm-local-{station}} GRB station. The ring is
     * shared: the store drops a flash when the ring already holds the same flash (±1 s, ±0.05°) from the
     * other origin, so the copy that arrives second is dropped whichever feed delivered first. The
     * default delegates to {@link #append(String, FlashBatch, Instant)} (test fakes and probe views).
     *
     * @param origin {@code internet} or {@code local}
     */
    default void append(String satellite, String origin, FlashBatch flashes, Instant fileTime) {
        append(satellite, flashes, fileTime);
    }

    /**
     * The ring flashes with {@code from <= time <= to}, oldest bucket first.
     *
     * @param from the earliest flash time, inclusive
     * @param to the latest flash time, inclusive
     */
    FlashBatch between(Instant from, Instant to);

    /**
     * The ring flashes within {@code radiusKm} (haversine) of {@code point} since {@code since}.
     *
     * @param point a WGS84 point
     * @param radiusKm the radius, km
     * @param since the earliest flash time, inclusive
     */
    FlashBatch flashesWithin(Point point, double radiusKm, Instant since);

    /**
     * The number of ring flashes inside {@code geometry} since {@code since}.
     *
     * @param geometry a WGS84 geometry
     * @param since the earliest flash time, inclusive
     */
    int countWithin(Geometry geometry, Instant since);

    /**
     * The distance to the nearest ring flash since {@code since}, km; empty when there is none.
     *
     * @param point a WGS84 point
     * @param since the earliest flash time, inclusive
     */
    OptionalDouble nearestKm(Point point, Instant since);

    /** The coverage end of the newest processed file, or empty before the first one. */
    Optional<Instant> newestFlashFileTime();

    /** True during a probe: {@link #append} writes nothing. */
    default boolean probing() {
        return false;
    }
}
