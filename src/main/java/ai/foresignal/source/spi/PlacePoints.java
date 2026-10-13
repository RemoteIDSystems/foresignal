package ai.foresignal.source.spi;

import java.util.Optional;

/**
 * Resolves a place id to a point, for an adapter parameter that may name a place instead of
 * coordinates (prompt 049: a SatDump station's {@code location}).
 *
 * <p>Adapters depend only on SPI types (Architecture §3), so places reach them through this interface;
 * {@code ai.foresignal.place.PlaceService} implements it.
 */
public interface PlacePoints {

    /**
     * The place's representative point as {@code {lon, lat}} in WGS84 degrees (the point itself, or
     * the interior point of a polygon or route), or empty when there is no such place.
     */
    Optional<double[]> point(String placeId);
}
