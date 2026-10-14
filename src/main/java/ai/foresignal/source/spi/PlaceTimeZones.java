package ai.foresignal.source.spi;

import java.time.ZoneId;
import java.util.Optional;

/**
 * Resolves the local timezone of a point, offline (prompt 095): the nearest populated place's IANA
 * timezone from 040's {@code gazetteer_place} table.
 *
 * <p>Contributor-generated sources such as iNaturalist and eBird publish local wall-clock times with
 * no offset; converting one to an instant needs the zone at the point. Adapters depend only on SPI
 * types (Architecture §3), so the reference table reaches them through this interface;
 * {@code ai.foresignal.reference.GazetteerTimeZones} implements it.
 */
public interface PlaceTimeZones {

    /**
     * The IANA zone of the nearest populated place to the point.
     *
     * @param lon longitude, WGS84 degrees
     * @param lat latitude, WGS84 degrees
     * @return the zone, or empty when the reference table is not loaded or no zone is known
     */
    Optional<ZoneId> byPoint(double lon, double lat);

    /** A lookup that never resolves a zone (unit tests, contexts without the reference table). */
    static PlaceTimeZones none() {
        return (lon, lat) -> Optional.empty();
    }
}
